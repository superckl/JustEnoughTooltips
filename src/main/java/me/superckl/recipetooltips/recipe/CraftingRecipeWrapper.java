package me.superckl.recipetooltips.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.recipetooltips.util.CraftingGridHelper;
import me.superckl.recipetooltips.util.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CraftingRecipeWrapper {

	private final int dimension;
	private final RecipeItemStack[] ingredients;
	private final ItemStack output;
	private final IRecipe wrappedRecipe;

	public static CraftingRecipeWrapper fromRecipe(final IRecipe recipe){
		//TODO dye recipes not complete
		final int dimension = CraftingGridHelper.getWidthHeight(recipe.getRecipeSize());
		RecipeItemStack[] ingredients = new RecipeItemStack[0];
		if(recipe instanceof ShapedRecipes){
			ingredients = ItemStackHelper.fromItemStacks(((ShapedRecipes)recipe).recipeItems);

			final List<RecipeItemStack> items = Lists.newArrayList();
			final ShapedRecipes sRecipe = (ShapedRecipes) recipe;
			final ItemStack[] input = sRecipe.recipeItems;
			int index = 0;
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < dimension && row < dimension && index < input.length)
						items.add(RecipeItemStack.fromOreDict(input[index++]));
					else
						items.add(null);
			ingredients = items.toArray(new RecipeItemStack[items.size()]);
		}else if(recipe instanceof ShapelessRecipes)
			ingredients = ItemStackHelper.fromItemStacks(((ShapelessRecipes) recipe).recipeItems.toArray(new ItemStack[dimension*dimension]));
		else if(recipe instanceof ShapelessOreRecipe){
			final List<RecipeItemStack> items = Lists.newArrayList();
			for(final Object obj:((ShapelessOreRecipe)recipe).getInput())
				if(obj instanceof ItemStack)
					items.add(RecipeItemStack.fromOreDict((ItemStack) obj));
				else if(obj instanceof List){
					final List<ItemStack> list = (List<ItemStack>) obj;
					if(list.isEmpty())
						items.add(null);
					else
						items.add(RecipeItemStack.fromOreDict(list));
				}
			ingredients = items.toArray(new RecipeItemStack[items.size()]);
		}else if(recipe instanceof ShapedOreRecipe){
			final List<RecipeItemStack> items = Lists.newArrayList();
			final ShapedOreRecipe sRecipe = (ShapedOreRecipe) recipe;
			final Object[] input = sRecipe.getInput();
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < dimension && row < dimension && row+column*dimension < input.length){
						final Object obj = input[row+column*dimension];
						if(obj == null)
							items.add(null);
						else if(obj instanceof ItemStack)
							items.add(RecipeItemStack.fromOreDict((ItemStack) obj));
						else if(obj instanceof List){
							final List<ItemStack> list = (List<ItemStack>) obj;
							if(list.isEmpty())
								items.add(null);
							else
								items.add(RecipeItemStack.fromOreDict(list));
						}
					}else
						items.add(null);
			ingredients = items.toArray(new RecipeItemStack[items.size()]);
		}

		if(ingredients.length != 9){
			final RecipeItemStack[] stacks = new RecipeItemStack[9];
			System.arraycopy(ingredients, 0, stacks, 0, ingredients.length);
			ingredients = stacks;
		}
		return new CraftingRecipeWrapper(dimension, ingredients, recipe.getRecipeOutput(), recipe);
	}

}
