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

package com.teammoeg.caupona.fluid;

import java.util.List;

import com.google.common.collect.Lists;
import com.teammoeg.caupona.util.FloatemStack;
import com.teammoeg.caupona.util.StewInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class SoupFluid extends ForgeFlowingFluid {

	@Override
	public Fluid getSource() {
		return this;
	}

	@Override
	public Fluid getFlowing() {
		return this;
	}

	@Override
	public Item getBucket() {
		return Items.AIR;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSame(Fluid fluidIn) {
		return fluidIn == this;
	}

	@Override
	public boolean isSource(FluidState p_207193_1_) {
		return true;
	}

	public static StewInfo getInfo(FluidStack stack) {
		if (stack.hasTag()) {
			CompoundTag nbt = stack.getChildTag("soup");
			if (nbt != null)
				return new StewInfo(nbt);
		}
		return new StewInfo(stack.getFluid().getRegistryName());
	}

	public static void setInfo(FluidStack stack, StewInfo si) {
		if (!si.isEmpty())
			stack.getOrCreateTag().put("soup", si.save());
	}

	public static List<FloatemStack> getItems(FluidStack stack) {
		if (stack.hasTag()) {
			CompoundTag nbt = stack.getChildTag("soup");
			if (nbt != null)
				return StewInfo.getStacks(nbt);
		}
		return Lists.newArrayList();
	}

	@Override
	public int getAmount(FluidState p_207192_1_) {
		return 0;
	}

	public SoupFluid(Properties properties) {
		super(properties);
	}

	public static class SoupAttributes extends FluidAttributes {
		// private static final String DefName="fluid."+Main.MODID+".soup";
		Fluid f;

		public SoupAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
			f = fluid;
		}

		@Override
		public int getColor(FluidStack stack) {
			return super.getColor();
		}

		@Override
		public String getTranslationKey(FluidStack stack) {
			ResourceLocation f = stack.getFluid().getRegistryName();
			return "item." + f.getNamespace() + "." + f.getPath();
		}

		/**
		 * Returns the localized name of this fluid.
		 */
		public Component getDisplayName(FluidStack stack) {
			return new TranslatableComponent(getTranslationKey(stack));
		}

		private static class SoupAttributesBuilder extends Builder {

			protected SoupAttributesBuilder(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
				super(stillTexture, flowingTexture, SoupAttributes::new);
			}

		}

		public static Builder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			return new SoupAttributesBuilder(stillTexture, flowingTexture);
		}

		@Override
		public String getTranslationKey() {
			return "item." + f.getRegistryName().getNamespace() + "." + f.getRegistryName().getPath();
		}
	}

	public static ResourceLocation getBase(FluidStack stack) {
		if (stack.hasTag()) {
			CompoundTag nbt = stack.getChildTag("soup");
			if (nbt != null)
				return new ResourceLocation(StewInfo.getRegName(nbt));
		}
		return stack.getFluid().getRegistryName();
	}

}