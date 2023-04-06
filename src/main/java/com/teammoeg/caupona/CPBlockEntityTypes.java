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

package com.teammoeg.caupona;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.teammoeg.caupona.blocks.dolium.CounterDoliumBlockEntity;
import com.teammoeg.caupona.blocks.foods.BowlBlockEntity;
import com.teammoeg.caupona.blocks.foods.DishBlockEntity;
import com.teammoeg.caupona.blocks.fumarole.FumaroleVentBlockEntity;
import com.teammoeg.caupona.blocks.hypocaust.CaliductBlockEntity;
import com.teammoeg.caupona.blocks.hypocaust.FireboxBlockEntity;
import com.teammoeg.caupona.blocks.hypocaust.WolfStatueBlockEntity;
import com.teammoeg.caupona.blocks.others.CPSignBlockEntity;
import com.teammoeg.caupona.blocks.pan.PanBlockEntity;
import com.teammoeg.caupona.blocks.pot.StewPotBlockEntity;
import com.teammoeg.caupona.blocks.stove.ChimneyPotBlockEntity;
import com.teammoeg.caupona.blocks.stove.KitchenStoveT1;
import com.teammoeg.caupona.blocks.stove.KitchenStoveT2;
import com.teammoeg.caupona.blocks.stove.KitchenStoveBlockEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CPBlockEntityTypes {
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITIES, Main.MODID);

	public static final RegistryObject<BlockEntityType<StewPotBlockEntity>> STEW_POT = REGISTER.register("stew_pot",
			makeType(StewPotBlockEntity::new, () -> CPBlocks.stew_pot));
	public static final RegistryObject<BlockEntityType<KitchenStoveBlockEntity>> STOVE1 = REGISTER
			.register("kitchen_stove_basic", makeType(KitchenStoveT1::new, () -> CPBlocks.stove1));
	public static final RegistryObject<BlockEntityType<KitchenStoveBlockEntity>> STOVE2 = REGISTER
			.register("kitchen_stove_fast", makeTypes(KitchenStoveT2::new,
					() -> new Block[] { CPBlocks.stove2, CPBlocks.stove3, CPBlocks.stove4, CPBlocks.stove5 }));
	public static final RegistryObject<BlockEntityType<BowlBlockEntity>> BOWL = REGISTER.register("bowl",
			makeType(BowlBlockEntity::new, () -> CPBlocks.bowl));
	public static final RegistryObject<BlockEntityType<CPSignBlockEntity>> SIGN = REGISTER.register("sign",
			makeTypes(CPSignBlockEntity::new, () -> CPBlocks.signs.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<ChimneyPotBlockEntity>> CHIMNEY = REGISTER.register("chimney_pot",
			makeTypes(ChimneyPotBlockEntity::new, () -> CPBlocks.chimney.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<FumaroleVentBlockEntity>> FUMAROLE = REGISTER
			.register("fumarole_vent", makeType(FumaroleVentBlockEntity::new, () -> CPBlocks.FUMAROLE_VENT));
	public static final RegistryObject<BlockEntityType<PanBlockEntity>> PAN = REGISTER.register("pan", makeTypes(
			PanBlockEntity::new, () -> new Block[] { CPBlocks.STONE_PAN, CPBlocks.COPPER_PAN, CPBlocks.IRON_PAN }));
	public static final RegistryObject<BlockEntityType<CounterDoliumBlockEntity>> DOLIUM = REGISTER.register("dolium",
			makeTypes(CounterDoliumBlockEntity::new, () -> CPBlocks.dolium.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<DishBlockEntity>> DISH = REGISTER.register("dish",
			makeTypes(DishBlockEntity::new, () -> CPBlocks.dishes.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<CaliductBlockEntity>> CALIDUCT = REGISTER.register("caliduct",
			makeTypes(CaliductBlockEntity::new, () -> CPBlocks.caliduct.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<FireboxBlockEntity>> FIREBOX = REGISTER.register("hypocast_firebox",
			makeTypes(FireboxBlockEntity::new, () -> CPBlocks.firebox.toArray(new Block[0])));
	public static final RegistryObject<BlockEntityType<WolfStatueBlockEntity>> WOLF = REGISTER.register("wolf_statue",
			makeType(WolfStatueBlockEntity::new, () -> CPBlocks.WOLF));

	private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(BlockEntitySupplier<T> create,
			Supplier<Block> valid) {
		return () -> new BlockEntityType<>(create, ImmutableSet.of(valid.get()), null);
	}

	private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeTypes(BlockEntitySupplier<T> create,
			Supplier<Block[]> valid) {
		return () -> new BlockEntityType<>(create, ImmutableSet.copyOf(valid.get()), null);
	}
}