package me.superckl.recipetooltips.recipe;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.recipetooltips.util.CraftingGridHelper;
import me.superckl.recipetooltips.util.ItemStackHelper;
import me.superckl.recipetooltips.util.LogHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CraftingRecipeWrapper extends RecipeWrapper{

	private final int dimension;
	private final RecipeMultiItemStack[] ingredients;
	private final RecipeMultiItemStack output;
	private final IRecipe wrappedRecipe;

	public static CraftingRecipeWrapper fromRecipe(final IRecipe recipe){
		final int dimension = CraftingGridHelper.getWidthHeight(recipe.getRecipeSize());
		int height = dimension, width = dimension;
		RecipeMultiItemStack[] ingredients = new RecipeMultiItemStack[0];
		if(recipe instanceof ShapedRecipes){
			final ShapedRecipes sRecipe = (ShapedRecipes) recipe;
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			LogHelper.info(Arrays.toString(sRecipe.recipeItems));
			int index = 0;
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < sRecipe.recipeWidth && row < sRecipe.recipeHeight && index < sRecipe.recipeItems.length)
						items.add(RecipeMultiItemStack.fromOreDict(sRecipe.recipeItems[index++]));
					else
						items.add(null);
			height = 3;
			width = 3;
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}else if(recipe instanceof ShapelessRecipes)
			ingredients = ItemStackHelper.fromItemStacks(((ShapelessRecipes) recipe).recipeItems.toArray(new ItemStack[dimension*dimension]), true);
		else if(recipe instanceof ShapelessOreRecipe){
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			for(final Object obj:((ShapelessOreRecipe)recipe).getInput())
				if(obj instanceof ItemStack)
					items.add(RecipeMultiItemStack.fromOreDict((ItemStack) obj));
				else if(obj instanceof List){
					final List<ItemStack> list = (List<ItemStack>) obj;
					if(list.isEmpty())
						items.add(null);
					else
						items.add(RecipeMultiItemStack.fromOreDict(list));
				}else if(obj == null)
					items.add(null);
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}else if(recipe instanceof ShapedOreRecipe){
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			final ShapedOreRecipe sRecipe = (ShapedOreRecipe) recipe;
			final Object[] input = sRecipe.getInput();
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < dimension && row < dimension && column+row*dimension < input.length){
						final Object obj = input[column+row*dimension];
						if(obj == null)
							items.add(null);
						else if(obj instanceof ItemStack)
							items.add(RecipeMultiItemStack.fromOreDict((ItemStack) obj));
						else if(obj instanceof List){
							final List<ItemStack> list = (List<ItemStack>) obj;
							if(list.isEmpty())
								items.add(null);
							else
								items.add(RecipeMultiItemStack.fromOreDict(list));
						}
					}else
						items.add(null);
			height = width = 3;
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}
		LogHelper.info(Arrays.toString(ingredients));
		final RecipeMultiItemStack[] rStacks = new RecipeMultiItemStack[9];

		int index = 0;
		for(int row = 0; row < 3; row++)
			for(int column = 0; column < 3; column++)
				if(column < width && row < height && index < ingredients.length)
					rStacks[column+row*3] = ingredients[index++];
				else
					rStacks[column+row*3] = null;

		return new CraftingRecipeWrapper(dimension, rStacks, RecipeMultiItemStack.from(recipe.getRecipeOutput()), recipe);
	}

	public static boolean isValid(final IRecipe recipe){
		return recipe != null && recipe.getRecipeOutput() != null && recipe.getRecipeSize() != 0;
	}

}
