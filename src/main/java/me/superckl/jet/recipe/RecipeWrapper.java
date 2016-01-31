package me.superckl.jet.recipe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.crafting.IRecipe;

public abstract class RecipeWrapper {

	public abstract RecipeStack[] getIngredients();
	public abstract RecipeStack getOutput();
	public abstract IRecipe getWrappedRecipe();
	public abstract void renderToScreen(final int x, final int y, final float scale, final float partialTicks, int itemIndex, final RenderItem itemRenderer, final FontRenderer fontRenderer);
	public abstract int getRenderWidth();
	public abstract int getRenderHeight();

}
