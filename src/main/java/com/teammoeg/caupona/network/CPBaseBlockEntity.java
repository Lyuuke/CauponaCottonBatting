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

package com.teammoeg.caupona.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CPBaseBlockEntity extends BlockEntity {

	public CPBaseBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
		super(pType, pWorldPosition, pBlockState);
	}

	public abstract void handleMessage(short type, int data);

	public void sendMessage(short type, int data) {
		PacketHandler.sendToServer(new ClientDataMessage(this.worldPosition, type, data));
	}

	public void syncData() {
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		this.setChanged();
	}

	public abstract void readCustomNBT(CompoundTag nbt, boolean isClient);

	public abstract void writeCustomNBT(CompoundTag nbt, boolean isClient);

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.readCustomNBT(pkt.getTag(), true);
	}

	public abstract void tick();

	@Override
	public void load(CompoundTag nbt) {
		this.readCustomNBT(nbt, false);
		super.load(nbt);

	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		this.writeCustomNBT(compound, false);
		super.saveAdditional(compound);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag cnbt = new CompoundTag();
		writeCustomNBT(cnbt, true);
		return ClientboundBlockEntityDataPacket.create(this, i -> cnbt);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = super.getUpdateTag();
		writeCustomNBT(nbt, true);
		return nbt;
	}
}
