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

package com.teammoeg.caupona.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import com.teammoeg.caupona.Main;
import com.teammoeg.caupona.data.recipes.AspicMeltingRecipe;
import com.teammoeg.caupona.data.recipes.BoilingRecipe;
import com.teammoeg.caupona.data.recipes.BowlContainingRecipe;
import com.teammoeg.caupona.data.recipes.CountingTags;
import com.teammoeg.caupona.data.recipes.DissolveRecipe;
import com.teammoeg.caupona.data.recipes.DoliumRecipe;
import com.teammoeg.caupona.data.recipes.FluidFoodValueRecipe;
import com.teammoeg.caupona.data.recipes.FoodValueRecipe;
import com.teammoeg.caupona.data.recipes.SauteedRecipe;
import com.teammoeg.caupona.data.recipes.SpiceRecipe;
import com.teammoeg.caupona.data.recipes.StewCookingRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeReloadListener implements ResourceManagerReloadListener {
	ReloadableServerResources data;
	public static final Logger logger = LogManager.getLogger(Main.MODNAME + " recipe generator");

	public RecipeReloadListener(ReloadableServerResources dpr) {
		data = dpr;
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
		buildRecipeLists(data.getRecipeManager());
	}

	RecipeManager clientRecipeManager;

	/**
	 * @param event  
	 */
	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		if (FoodValueRecipe.recipeset != null)
			FoodValueRecipe.recipeset.forEach(FoodValueRecipe::clearCache);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRecipesUpdated(RecipesUpdatedEvent event) {
		buildRecipeLists(event.getRecipeManager());
	}

	static int generated_fv = 0;
	
	private static FoodValueRecipe addCookingTime(Item i, ItemStack iis, List<SmokingRecipe> irs, boolean force) {
		if (FoodValueRecipe.recipes.containsKey(i))
			return FoodValueRecipe.recipes.get(i);
		for (SmokingRecipe sr : irs) {
			if(sr.getIngredients().size()>0)
			if (sr.getIngredients().get(0).test(iis)) {
				SimpleContainer fake=new SimpleContainer(3);
				fake.setItem(0,iis);
				ItemStack reslt = sr.assemble(fake);
				if (DissolveRecipe.recipes.stream().anyMatch(e -> e.test(reslt)))
					continue;
				FoodValueRecipe ret = addCookingTime(reslt.getItem(), reslt, irs, true);
				FoodProperties of = reslt.getFoodProperties(null);
				if (of != null && of.getNutrition() > ret.heal) {
					ret.effects = of.getEffects();
					ret.heal = of.getNutrition();
					ret.sat = of.getSaturationModifier();
					ret.setRepersent(iis);
				}
				FoodValueRecipe.recipes.put(i, ret);
				ret.processtimes.put(i, sr.getCookingTime() + ret.processtimes.getOrDefault(reslt.getItem(), 0));
				return ret;
			}
		}
		if (force) {
			FoodProperties of = iis.getFoodProperties(null);
			FoodValueRecipe ret = FoodValueRecipe.recipes.computeIfAbsent(i,
					e -> new FoodValueRecipe(new ResourceLocation(Main.MODID, "food/generated/" + (generated_fv++)), 0,
							0, iis, e));
			if (of != null && of.getNutrition() > ret.heal) {
				ret.effects = of.getEffects();
				ret.heal = of.getNutrition();
				ret.sat = of.getSaturationModifier();
				ret.setRepersent(iis);
			}
			return ret;
		}
		return null;
	}

	public static void buildRecipeLists(RecipeManager recipeManager) {

		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		if (recipes.size() == 0)
			return;

		logger.info("Building recipes...");
		Stopwatch sw = Stopwatch.createStarted();
		BowlContainingRecipe.recipes = filterRecipes(recipes, BowlContainingRecipe.class, BowlContainingRecipe.TYPE)
				.collect(Collectors.toMap(e -> e.fluid, UnaryOperator.identity()));

		FoodValueRecipe.recipes = filterRecipes(recipes, FoodValueRecipe.class, FoodValueRecipe.TYPE)
				.flatMap(t -> t.processtimes.keySet().stream().map(i -> new Pair<>(i, t)))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
		List<SmokingRecipe> irs = recipeManager.getAllRecipesFor(RecipeType.SMOKING);

		DissolveRecipe.recipes = filterRecipes(recipes, DissolveRecipe.class, DissolveRecipe.TYPE)
				.collect(Collectors.toList());

		BoilingRecipe.recipes = filterRecipes(recipes, BoilingRecipe.class, BoilingRecipe.TYPE)
				.collect(Collectors.toMap(e -> e.before, UnaryOperator.identity()));

		FluidFoodValueRecipe.recipes = filterRecipes(recipes, FluidFoodValueRecipe.class, FluidFoodValueRecipe.TYPE)
				.collect(Collectors.toMap(e -> e.f, UnaryOperator.identity()));

		StewCookingRecipe.recipes = filterRecipes(recipes, StewCookingRecipe.class, StewCookingRecipe.TYPE)
				.collect(Collectors.toMap(e -> e.output, UnaryOperator.identity()));
		StewCookingRecipe.cookables = StewCookingRecipe.recipes.values().stream()
				.flatMap(StewCookingRecipe::getAllNumbers).collect(Collectors.toSet());
		StewCookingRecipe.sorted = new ArrayList<>(StewCookingRecipe.recipes.values());
		StewCookingRecipe.sorted.sort((t2, t1) -> t1.getPriority() - t2.getPriority());

		CountingTags.tags = Stream
				.concat(filterRecipes(recipes, CountingTags.class, CountingTags.TYPE).flatMap(r -> r.tag.stream()),
						StewCookingRecipe.recipes.values().stream().flatMap(StewCookingRecipe::getTags))
				.collect(Collectors.toSet());
		// CountingTags.tags.forEach(System.out::println);

		SauteedRecipe.recipes = filterRecipes(recipes, SauteedRecipe.class, SauteedRecipe.TYPE)
				.collect(Collectors.toMap(e -> e.output, UnaryOperator.identity()));
		SauteedRecipe.cookables = SauteedRecipe.recipes.values().stream().flatMap(SauteedRecipe::getAllNumbers)
				.collect(Collectors.toSet());
		SauteedRecipe.sorted = new ArrayList<>(SauteedRecipe.recipes.values());
		SauteedRecipe.sorted.sort((t2, t1) -> t1.getPriority() - t2.getPriority());

		DoliumRecipe.recipes = filterRecipes(recipes, DoliumRecipe.class, DoliumRecipe.TYPE)
				.collect(Collectors.toList());
		DoliumRecipe.recipes
				.sort(((Comparator<DoliumRecipe>) (c1, c2) -> Integer.compare(c2.items.size(), c1.items.size()))
						.thenComparing((c1, c2) -> Integer.compare(
								c2.items.stream().reduce(0, (a, b) -> a + b.getSecond(), (a, b) -> a + b),
								c1.items.stream().reduce(0, (a, b) -> a + b.getSecond(), (a, b) -> a + b))));

		AspicMeltingRecipe.recipes = filterRecipes(recipes, AspicMeltingRecipe.class, AspicMeltingRecipe.TYPE)
				.collect(Collectors.toList());

		SpiceRecipe.recipes = filterRecipes(recipes, SpiceRecipe.class, SpiceRecipe.TYPE).collect(Collectors.toList());

		for (Item i : ForgeRegistries.ITEMS) {
			ItemStack iis = new ItemStack(i);
			if (FoodValueRecipe.recipes.containsKey(i))
				continue;
			if (DissolveRecipe.recipes.stream().anyMatch(e -> e.test(iis)))
				continue;
			addCookingTime(i, iis, irs, false);
		}

		FoodValueRecipe.recipeset = new HashSet<>(FoodValueRecipe.recipes.values());

		sw.stop();
		logger.info("Recipes built, cost {}", sw);
	}

	static <R extends Recipe<?>> Stream<R> filterRecipes(Collection<Recipe<?>> recipes, Class<R> recipeClass,
			RecipeType<?> recipeType) {
		return recipes.stream().filter(iRecipe -> iRecipe.getType() == recipeType).map(recipeClass::cast);
	}
}
