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

package com.wildfire.mixins.accessors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EquipmentRenderer.TrimSpriteKey.class)
@Environment(EnvType.CLIENT)
public interface TrimSpriteKeyConstructorAccessor {
    // Yes, it would've been possible to simply access widener the constructor along with the record itself, but I
    // am absolutely not willing to maintain such an access widener entry when I could alternatively use Mixin, which
    // at least will fail in a way that's significantly less of a headache to update.
    @Invoker("<init>")
    static EquipmentRenderer.TrimSpriteKey newKey(ArmorTrim armorTrim, EquipmentModel.LayerType layerType, RegistryKey<EquipmentAsset> registryKey) {
        throw new UnsupportedOperationException("Something's gone very seriously wrong if we've gotten here!");
    }
}
