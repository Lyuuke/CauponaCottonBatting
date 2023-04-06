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

package com.teammoeg.caupona.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.teammoeg.caupona.Config;
import com.teammoeg.caupona.api.CauponaHooks;
import com.teammoeg.caupona.data.recipes.FluidFoodValueRecipe;
import com.teammoeg.caupona.data.recipes.FoodValueRecipe;
import com.teammoeg.caupona.util.FloatemStack;
import com.teammoeg.caupona.util.StewInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.diet.api.DietApi;
import top.theillusivec4.diet.api.IDietGroup;
import top.theillusivec4.diet.api.IDietResult;
import top.theillusivec4.diet.common.impl.DietApiImpl;
import top.theillusivec4.diet.common.util.DietResult;

//As Diet's author didn't add such a more flexible api, I have to resort to mixin.
@Mixin(DietApiImpl.class)
public class DietApiImplMixin extends DietApi {
	private static void CP$getResult(Player player, ItemStack input, CallbackInfoReturnable<IDietResult> result) {
		CauponaHooks.getInfo(input).ifPresent(ois -> {

			float harmMod = Config.SERVER.harmfulMod.get();
			float goodMod = Config.SERVER.benefitialMod.get();
			List<FloatemStack> is = ois.getStacks();
			Map<IDietGroup, Float> groups = new HashMap<>();
			for (FloatemStack sx : is) {
				FoodValueRecipe fvr = FoodValueRecipe.recipes.get(sx.getItem());
				ItemStack stack;
				if (fvr == null || fvr.getRepersent() == null)
					stack = sx.getStack();
				else
					stack = fvr.getRepersent();
				IDietResult dr = DietApiImpl.getInstance().get(player, stack);
				if (dr != DietResult.EMPTY)
					for (Entry<IDietGroup, Float> me : dr.get().entrySet())
						groups.merge(me.getKey(),
								me.getValue() * sx.getCount() * (me.getKey().isBeneficial() ? goodMod : harmMod),
								Float::sum);
			}
			if(ois instanceof StewInfo si) {
				FluidFoodValueRecipe ffvr = FluidFoodValueRecipe.recipes.get(si.base);
				if (ffvr != null && ffvr.getRepersent() != null) {
					IDietResult dr = DietApiImpl.getInstance().get(player, ffvr.getRepersent());
					if (dr != DietResult.EMPTY)
						for (Entry<IDietGroup, Float> me : dr.get().entrySet())
							groups.merge(me.getKey(), me.getValue() * (si.shrinkedFluid + 1) / ffvr.parts
									* (me.getKey().isBeneficial() ? goodMod : harmMod), Float::sum);
				}
			}
			result.setReturnValue(new DietResult(groups));
		});
	}

	@Inject(at = @At("HEAD"), require = 1, method = "get(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Ltop/theillusivec4/diet/api/IDietResult;", cancellable = true, remap = false)
	public void get(Player player, ItemStack input, CallbackInfoReturnable<IDietResult> result) {
		CP$getResult(player, input, result);
	}

	/**
	 * @param heal
	 * @param sat
	 */

	@Inject(at = @At("HEAD"), require = 1, method = "get(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;IF)Ltop/theillusivec4/diet/api/IDietResult;", cancellable = true, remap = false)
	public void get(Player player, ItemStack input, int heal, float sat, CallbackInfoReturnable<IDietResult> result) {
		CP$getResult(player, input, result);
	}

}
