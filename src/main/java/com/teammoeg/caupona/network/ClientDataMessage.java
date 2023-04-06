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

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ClientDataMessage {
	private final short type;
	private final int message;
	private final BlockPos pos;

	public ClientDataMessage(BlockPos pos, short type, int message) {
		this.pos = pos;
		this.type = type;
		this.message = message;
	}

	ClientDataMessage(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		type = buffer.readShort();
		message = buffer.readInt();
	}

	void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeShort(type);
		buffer.writeInt(message);
	}

	@SuppressWarnings({ "resource" })
	void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerLevel world = Objects.requireNonNull(context.get().getSender()).getLevel();
			if (world.isLoaded(pos)) {
				if (world.getBlockEntity(pos) instanceof CPBaseBlockEntity entity)
					entity.handleMessage(type, message);
			}
		});
		context.get().setPacketHandled(true);
	}
}
