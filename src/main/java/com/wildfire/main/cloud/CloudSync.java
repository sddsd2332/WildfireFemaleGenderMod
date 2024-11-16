/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.main.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class CloudSync {
	private CloudSync() {
		throw new UnsupportedOperationException();
	}

	private static Instant lastSync = Instant.EPOCH;

	private static final Executor EXECUTOR = Util.getIoWorkerExecutor().named("wildfire_gender$cloudSync");
	private static final Gson GSON = new Gson();
	private static final String USER_AGENT =
			"WildfireGender/" + StringUtils.split(WildfireHelper.getModVersion(WildfireGender.MODID), '+')[0]
					+ " Minecraft/" + WildfireHelper.getModVersion("minecraft");

	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int READ_TIMEOUT_MS = 5000;

	private static final String DEFAULT_CLOUD_URL = "https://wfgm.celestialfault.dev";
	public static final Duration SYNC_COOLDOWN = Duration.of(15, ChronoUnit.SECONDS);

	public static boolean syncOnCooldown() {
		return lastSync.plus(SYNC_COOLDOWN).isAfter(Instant.now());
	}

	public static boolean isEnabled() {
		return GlobalConfig.INSTANCE.get(GlobalConfig.CLOUD_SYNC_ENABLED);
	}

	public static String getCloudServer() {
		var url = GlobalConfig.INSTANCE.get(GlobalConfig.CLOUD_SERVER);
		return url.isBlank() ? DEFAULT_CLOUD_URL : url;
	}

	private static HttpURLConnection createConnection(URL url) throws IOException {
		WildfireGender.LOGGER.debug("Connecting to {}", url);
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
		connection.setReadTimeout(READ_TIMEOUT_MS);
		connection.setUseCaches(false);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		return connection;
	}

	private static String generateServerId() {
		// https://github.com/hibiii/Kappa/blob/main/src/main/java/hibiii/kappa/Provider.java#L40-L42
		BigInteger intA = new BigInteger(128, new Random());
		BigInteger intB = new BigInteger(128, new Random(System.identityHashCode(new Object())));
		return intA.xor(intB).toString(16);
	}

	private static void beginAuth(String serverId) {
		var client = MinecraftClient.getInstance();
		var session = client.getSession();
		try {
			client.getSessionService().joinServer(Objects.requireNonNull(session.getUuidOrNull()), session.getAccessToken(), serverId);
		} catch(AuthenticationException e) {
			throw new RuntimeException(e);
		}
	}

	public static CompletableFuture<Void> sync(@NotNull PlayerConfig config) {
		if(!isEnabled()) {
			return CompletableFuture.completedFuture(null);
		}

		// Force a 15s cooldown on syncing
		if(syncOnCooldown()) {
			var future = new CompletableFuture<Void>();
			future.completeExceptionally(new SyncingTooFrequentlyException());
			return future;
		}
		lastSync = Instant.now();

		var client = MinecraftClient.getInstance();
		var username = client.getSession().getUsername();
		var json = config.toJson().toString().getBytes(StandardCharsets.UTF_8);
		var serverId = generateServerId();

		return CompletableFuture.runAsync(() -> {
			var params = HttpAuthenticationService.buildQuery(Map.of(
					"serverId", Objects.requireNonNull(serverId),
					"username", username
			));

			URL url;
			try {
				url = URI.create(getCloudServer() + "/" + config.uuid + "?" + params).toURL();
			} catch(MalformedURLException e) {
				throw new RuntimeException(e);
			}
			beginAuth(serverId);

			try {
				var connection = createConnection(url);
				connection.setRequestMethod("PUT");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				connection.setFixedLengthStreamingMode(json.length);
				connection.connect();
				try(var out = connection.getOutputStream()) {
					out.write(json);
				}
				int code = connection.getResponseCode();
				if(code >= 400 || code == -1) {
					String response;
					try(var stream = connection.getErrorStream()) {
						response = IOUtils.toString(stream, StandardCharsets.UTF_8);
					}
					throw new RuntimeException("Server returned " + code + " response code: " + response);
				}
				try(var stream = connection.getInputStream()) {
					var response = IOUtils.toString(stream, StandardCharsets.UTF_8);
					WildfireGender.LOGGER.debug("Server replied to update: {}", response);
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}, EXECUTOR);
	}

	public static CompletableFuture<@Nullable JsonObject> getProfile(UUID uuid) {
		if(!isEnabled()) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			URL url;
			try {
				url = URI.create(getCloudServer() + "/" + uuid).toURL();
			} catch(MalformedURLException e) {
				throw new RuntimeException(e);
			}

			try {
				var connection = createConnection(url);
				connection.connect();
				int code = connection.getResponseCode();
				if(code == 404) {
					return null;
				} else if(code >= 400 || code == -1) {
					throw new RuntimeException("Server responded with " + code + " response code");
				}

				String response;
				try(var stream = connection.getInputStream()) {
					response = IOUtils.toString(stream, StandardCharsets.UTF_8);
					WildfireGender.LOGGER.debug("Server response for {}: {}", uuid, response);
				}

				return GSON.fromJson(response, JsonObject.class);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}, EXECUTOR);
	}
}
