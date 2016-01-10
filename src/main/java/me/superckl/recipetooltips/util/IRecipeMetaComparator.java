package me.superckl.recipetooltips.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class IRecipeMetaComparator implements Comparator<IRecipe>{

	@Override
	public int compare(final IRecipe o1, final IRecipe o2) {
		final ItemStack i1 = o1.getRecipeOutput();
		final ItemStack i2 = o2.getRecipeOutput();
		return i1.getItemDamage() == i2.getItemDamage() ? 0:i1.getItemDamage() > i2.getItemDamage() ? 1:-1;
	}

}
