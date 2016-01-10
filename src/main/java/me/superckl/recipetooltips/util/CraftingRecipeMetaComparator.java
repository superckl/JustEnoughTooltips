package me.superckl.recipetooltips.util;

import java.util.Comparator;

import me.superckl.recipetooltips.recipe.CraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

public class CraftingRecipeMetaComparator implements Comparator<CraftingRecipeWrapper>{

	@Override
	public int compare(final CraftingRecipeWrapper o1, final CraftingRecipeWrapper o2) {
		final ItemStack i1 = o1.getOutput();
		final ItemStack i2 = o2.getOutput();
		return i1.getItemDamage() == i2.getItemDamage() ? 0:i1.getItemDamage() > i2.getItemDamage() ? 1:-1;
	}

}
