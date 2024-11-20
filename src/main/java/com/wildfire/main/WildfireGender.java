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

package com.wildfire.main;

import java.util.*;

import com.mojang.logging.LogUtils;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WildfireGender implements ModInitializer {
	public static final String MODID = "wildfire_gender";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Map<UUID, PlayerConfig> PLAYER_CACHE = new HashMap<>();

	public static final UUID CREATOR_UUID = UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946");
	public static final List<UUID> CONTRIBUTOR_UUIDS = Arrays.asList(
			UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"), //celeste
			UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"), //pupnewfster
			UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"), //Kichura
			UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"), //BlueLight
			UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"), //Diademiemi
			UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"), //Arcti.cc
			UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"), //IzzyBizzy45
			UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b") //Powerless001
			//UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946") //WildfireFGM (I'm not a contributor, silly!)
	);

	@Override
	public void onInitialize() {
		WildfireSync.register();
		WildfireEventHandler.registerCommonEvents();
		GlobalConfig.INSTANCE.load();
	}

	public static @Nullable PlayerConfig getPlayerById(UUID id) {
		return PLAYER_CACHE.get(id);
	}

	public static @NotNull PlayerConfig getOrAddPlayerById(UUID id) {
		return PLAYER_CACHE.computeIfAbsent(id, PlayerConfig::new);
	}
}
