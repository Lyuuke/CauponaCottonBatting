/*
 * Copyright (c) 2022 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.caupona.data.recipes.baseconditions;

import com.google.gson.JsonObject;
import com.teammoeg.caupona.data.TranslationProvider;
import com.teammoeg.caupona.data.recipes.StewBaseCondition;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidTypeType implements StewBaseCondition {
	ResourceLocation of;

	public FluidTypeType(JsonObject jo) {
		of = new ResourceLocation(jo.get("base").getAsString());
	}

	public FluidTypeType(ResourceLocation of) {
		super();
		this.of = of;
	}

	public FluidTypeType(Fluid of) {
		super();
		this.of = of.getRegistryName();
	}

	@Override
	public Integer apply(ResourceLocation t, ResourceLocation u) {
		return test(u) ? 2 : 0;
	}

	public boolean test(ResourceLocation t) {
		return of.equals(t);
	}

	@Override
	public boolean test(Fluid f) {
		return f.getRegistryName().equals(of);
	}

	public JsonObject serialize() {
		JsonObject jo = new JsonObject();
		jo.addProperty("base", of.toString());
		return jo;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(of);
	}

	public FluidTypeType(FriendlyByteBuf buffer) {
		of = buffer.readResourceLocation();
	}

	@Override
	public String getType() {
		return "fluid_type";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((of == null) ? 0 : of.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FluidTypeType))
			return false;
		FluidTypeType other = (FluidTypeType) obj;
		if (of == null) {
			if (other.of != null)
				return false;
		} else if (!of.equals(other.of))
			return false;
		return true;
	}

	@Override
	public String getTranslation(TranslationProvider p) {
		return p.getTranslation("fluid." + ForgeRegistries.FLUIDS.getValue(of).getAttributes().getTranslationKey());
	}

}
