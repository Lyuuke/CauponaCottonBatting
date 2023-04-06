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

package com.teammoeg.caupona.blocks.foods;

import java.util.function.BiFunction;

import com.teammoeg.caupona.blocks.CPRegisteredEntityBlock;
import com.teammoeg.caupona.items.StewItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;

public class BowlBlock extends CPRegisteredEntityBlock<BowlBlockEntity> {

	public BowlBlock(String name, Properties blockProps, RegistryObject<BlockEntityType<BowlBlockEntity>> ste,
			BiFunction<Block, Item.Properties, Item> createItemBlock) {
		super(name, blockProps, ste, createItemBlock);
	}

	static final VoxelShape shape = Block.box(2.8, 0, 2.8, 13.2, 5.2, 13.2);

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return shape;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock() && worldIn.getBlockEntity(pos) instanceof BowlBlockEntity bowl) {
			super.popResource(worldIn, pos, bowl.internal);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		InteractionResult p = super.use(state, worldIn, pos, player, handIn, hit);
		if (p.consumesAction())
			return p;
		if (worldIn.getBlockEntity(pos) instanceof BowlBlockEntity bowl&&bowl.internal != null && bowl.internal.getItem() instanceof StewItem
				&& bowl.internal.isEdible()) {
			FoodProperties fp = bowl.internal.getFoodProperties(player);
			if (bowl.isInfinite) {
				if (player.canEat(fp.canAlwaysEat())) {
					player.eat(worldIn, bowl.internal.copy());
					bowl.syncData();
				}
			} else {
				if (player.canEat(fp.canAlwaysEat())) {
					ItemStack iout = bowl.internal.getContainerItem();
					player.eat(worldIn, bowl.internal);
					bowl.internal = iout;
					bowl.syncData();
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		if (pLevel.getBlockEntity(pPos) instanceof BowlBlockEntity bowl) {
			bowl.internal = ItemHandlerHelper.copyStackWithSize(pStack, 1);
		}
	}

	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
			Player player) {
		if (level.getBlockEntity(pos) instanceof BowlBlockEntity bowl) {
			if (bowl.internal == null)
				return ItemStack.EMPTY;
			return bowl.internal.copy();
		}
		return this.getCloneItemStack(state, target, level, pos, player);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof BowlBlockEntity bowl&&bowl.internal != null && !bowl.internal.isEmpty() && bowl.internal.isEdible()) {
			return 15;
		}
		return 0;
	}
}
