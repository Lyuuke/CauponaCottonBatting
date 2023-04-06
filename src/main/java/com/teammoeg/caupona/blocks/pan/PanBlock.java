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

package com.teammoeg.caupona.blocks.pan;

import com.teammoeg.caupona.CPBlocks;
import com.teammoeg.caupona.CPBlockEntityTypes;
import com.teammoeg.caupona.blocks.CPHorizontalEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class PanBlock extends CPHorizontalEntityBlock<PanBlockEntity> {

	public PanBlock(Properties p_54120_) {
		super(CPBlockEntityTypes.PAN, p_54120_);
	}

	static final VoxelShape bshape = Block.box(1, 0, 1, 15, 2, 15);
	static final VoxelShape sshape = Block.box(3, 0, 3, 13, 3, 13);

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (state.getBlock() == CPBlocks.STONE_PAN)
			return bshape;
		return sshape;
	}

	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		InteractionResult p = super.use(state, worldIn, pos, player, handIn, hit);
		if (p.consumesAction())
			return p;
		PanBlockEntity blockEntity = (PanBlockEntity) worldIn.getBlockEntity(pos);
		if (handIn == InteractionHand.MAIN_HAND) {
			if (blockEntity != null && !worldIn.isClientSide)
				NetworkHooks.openGui((ServerPlayer) player, blockEntity, blockEntity.getBlockPos());
			return InteractionResult.SUCCESS;
		}
		return p;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()&&worldIn.getBlockEntity(pos) instanceof PanBlockEntity pan) {
			if (pan.processMax == 0)
				for (int i = 0; i < 9; i++) {
					ItemStack is = pan.inv.getStackInSlot(i);
					if (!is.isEmpty())
						super.popResource(worldIn, pos, is);
				}
			for (int i = 9; i < 12; i++) {
				ItemStack is = pan.inv.getStackInSlot(i);
				if (!is.isEmpty())
					super.popResource(worldIn, pos, is);
			}
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		
		if(pLevel.getBlockEntity(pPos) instanceof PanBlockEntity pan)
			if (pan.processMax == 0) {
				int ret = 1;
				if(!pan.sout.isEmpty()||!pan.inv.getStackInSlot(10).isEmpty()) {
					return 15;
				}
				for (int i = 0; i < 9; i++) {
					ItemStack is = pan.getInv().getStackInSlot(i);
					if (!is.isEmpty())
						ret++;
				}
				
				return ret;
			}
		return 0;
	}
}
