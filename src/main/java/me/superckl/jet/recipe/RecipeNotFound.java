package me.superckl.jet.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public abstract class RecipeNotFound implements IRecipe{

	@Override
	public boolean matches(final InventoryCrafting inv, final World worldIn) {
		return false;
	}

	@Override
	public ItemStack getCraftingResult(final InventoryCrafting inv) {
		return this.getRecipeOutput();
	}

	@Override
	public int getRecipeSize() {
		return 0;
	}

	@Override
	public ItemStack[] getRemainingItems(final InventoryCrafting inv) {
		return new ItemStack[0];
	}

}
