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

package com.teammoeg.caupona.blocks.pot;

import com.teammoeg.caupona.CPGui;
import com.teammoeg.caupona.container.HidableSlot;
import com.teammoeg.caupona.container.OutputSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class StewPotContainer extends AbstractContainerMenu {
	StewPotBlockEntity tile;

	public StewPotBlockEntity getBlock() {
		return tile;
	}

	public StewPotContainer(int id, Inventory inv, FriendlyByteBuf buffer) {
		this(id, inv, (StewPotBlockEntity) inv.player.level.getBlockEntity(buffer.readBlockPos()));
	}

	public StewPotContainer(int id, Inventory inv, StewPotBlockEntity blockEntity) {
		super(CPGui.STEWPOT.get(), id);
		tile = blockEntity;
		for (int i = 0; i < 9; i++)
			this.addSlot(new HidableSlot(blockEntity.getInv(), i, 45 + (i % 3) * 18, 17 + (i / 3) * 18, () -> blockEntity.proctype != 2));
		this.addSlot(new SlotItemHandler(blockEntity.getInv(), 9, 154, 17));

		this.addSlot(new OutputSlot(blockEntity.getInv(), 10, 143, 51));
		this.addSlot(new SlotItemHandler(blockEntity.getInv(), 11, 132, 17));
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		for (int i = 0; i < 9; i++)
			addSlot(new Slot(inv, i, 8 + i * 18, 142));
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			itemStack = slotStack.copy();
			if (index == 10 || index == 11) {
				if (!this.moveItemStackTo(slotStack, 12, 48, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(slotStack, itemStack);
			} else if (index > 11) {
				if (!this.moveItemStackTo(slotStack, 9, 10, false))
					if (!this.moveItemStackTo(slotStack, 11, 12, false))
						if (!this.moveItemStackTo(slotStack, 0, 9, false))
							if (index < 39) {
								if (!this.moveItemStackTo(slotStack, 39, 48, false))
									return ItemStack.EMPTY;
							} else if (index < 48 && !this.moveItemStackTo(slotStack, 12, 39, false))
								return ItemStack.EMPTY;

			} else if (!this.moveItemStackTo(slotStack, 12, 47, false)) {
				return ItemStack.EMPTY;
			}
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (slotStack.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, slotStack);
		}
		return itemStack;
	}
}
