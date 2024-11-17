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
import net.minecraft.client.session.Session;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
	private static final List<Instant> fetchErrors = new ArrayList<>();
	private static @Nullable Instant disableFetchingUntil;

	private static final Executor EXECUTOR = Util.getIoWorkerExecutor().named("wildfire_gender$cloudSync");
	private static final Gson GSON = new Gson();
	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	private static final String USER_AGENT =
			"WildfireGender/" + StringUtils.split(WildfireHelper.getModVersion(WildfireGender.MODID), '+')[0]
					+ " Minecraft/" + WildfireHelper.getModVersion("minecraft");

	private static final String DEFAULT_CLOUD_URL = "https://wfgm.celestialfault.dev";
	public static final Duration SYNC_COOLDOWN = Duration.of(15, ChronoUnit.SECONDS);

	/**
	 * @return {@code true} if the last sync was within the last {@link #SYNC_COOLDOWN 15 seconds}
	 */
	public static boolean syncOnCooldown() {
		return lastSync.plus(SYNC_COOLDOWN).isAfter(Instant.now());
	}

	/**
	 * @return {@code true} if syncing is available; currently, this only checks for a valid Minecraft session.
	 */
	public static boolean isAvailable() {
		return MinecraftClient.getInstance().getSession().getAccountType() == Session.AccountType.MSA;
	}

	/**
	 * @return {@code true} if syncing is enabled; this will always return {@code false} if {@link #isAvailable() syncing is unavailable}.
	 */
	public static boolean isEnabled() {
		return isAvailable() && GlobalConfig.INSTANCE.get(GlobalConfig.CLOUD_SYNC_ENABLED);
	}

	/**
	 * @return The URL of the sync server currently being used
	 */
	public static String getCloudServer() {
		var url = GlobalConfig.INSTANCE.get(GlobalConfig.CLOUD_SERVER);
		return url.isBlank() ? DEFAULT_CLOUD_URL : url;
	}

	private static void markFetchError() {
		fetchErrors.add(Instant.now());
		fetchErrors.removeIf(e -> e.plus(30, ChronoUnit.SECONDS).isBefore(Instant.now()));
		if(fetchErrors.size() >= 10) {
			WildfireGender.LOGGER.error("Too many recent sync errors, disabling future lookups for 5 minutes");
			disableFetchingUntil = Instant.now().plus(5, ChronoUnit.MINUTES);
		}
	}

	private static HttpRequest.Builder createConnection(URI uri) throws IOException {
		WildfireGender.LOGGER.debug("Connecting to {}", uri);
		return HttpRequest.newBuilder(uri)
				.header("User-Agent", USER_AGENT)
				.timeout(Duration.ofSeconds(5));
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

	/**
	 * Send the client player config to the cloud sync server for syncing to other players
	 *
	 * @param config The config of the client player
	 *
	 * @return A {@link CompletableFuture} indicating when the process has finished, or with an exception if
	 *         syncing failed.
	 */
	public static synchronized CompletableFuture<Void> sync(@NotNull PlayerConfig config) {
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
		var json = config.toJson().toString();
		var serverId = generateServerId();

		return CompletableFuture.runAsync(() -> {
			var params = HttpAuthenticationService.buildQuery(Map.of(
					"serverId", Objects.requireNonNull(serverId),
					"username", username
			));

			URI url = URI.create(getCloudServer() + "/" + config.uuid + "?" + params);
			beginAuth(serverId);

			try {
				var request = createConnection(url)
						.PUT(HttpRequest.BodyPublishers.ofString(json))
						.header("Content-Type", "application/json; charset=UTF-8")
						.build();
				var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
				if(response.statusCode() >= 400) {
					throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
				}
				WildfireGender.LOGGER.debug("Server responded to update: {}", response.body());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}, EXECUTOR);
	}

	/**
	 * Fetch player data from the sync server
	 *
	 * @param uuid The UUID of the player to get data for
	 *
	 * @return A {@link CompletableFuture} containing a {@link JsonObject} of the player's data if they have any data
	 * 		   stored in the sync server, or {@code null} otherwise.
	 */
	public static CompletableFuture<@Nullable JsonObject> getProfile(UUID uuid) {
		if(!isEnabled() || disableFetchingUntil != null && disableFetchingUntil.isAfter(Instant.now())) {
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.supplyAsync(() -> {
			URI url = URI.create(getCloudServer() + "/" + uuid);

			try {
				var request = createConnection(url).GET().build();
				var response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
				if(response.statusCode() == 404) {
					WildfireGender.LOGGER.debug("Server replied no data for {}", uuid);
					return null;
				} else if(response.statusCode() >= 400) {
					markFetchError();
					throw new RuntimeException("Server responded " + response.statusCode() + ": " + response.body());
				}

				return GSON.fromJson(response.body(), JsonObject.class);
			} catch(IOException e) {
				markFetchError();
				throw new RuntimeException(e);
			}
		}, EXECUTOR);
	}
}
