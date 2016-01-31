package me.superckl.jet.recipe;

import lombok.RequiredArgsConstructor;
import me.superckl.jet.util.RenderHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor
public class FurnaceRecipeWrapper extends RecipeWrapper{

	public static final ResourceLocation furnaceTexture = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipefurnace.png");

	private final RecipeMultiItemStack input;
	private final RecipeMultiItemStack output;

	@Override
	public RecipeStack[] getIngredients() {
		return new RecipeMultiItemStack[] {this.input, this.output};
	}

	@Override
	public RecipeStack getOutput() {
		return this.output;
	}

	@Override
	public IRecipe getWrappedRecipe() {
		return null;
	}

	@Override
	public void renderToScreen(final int x, final int y, final float scale, final float partialTicks, final int renderTick, final RenderItem itemRenderer, final FontRenderer fontRenderer) {
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.enableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		RenderHelper.drawTexturedRect(FurnaceRecipeWrapper.furnaceTexture, x, y, 500, 0, 0, 94, 66, 256, 256, scale);
		RenderHelper.drawTexturedRect(FurnaceRecipeWrapper.furnaceTexture, x+30*scale, y+25*scale, 500, 94, 14, (renderTick % 100)*24/100, 17, 256, 256, scale);
		final int height = 14-(renderTick % 100)*14/100;
		RenderHelper.drawTexturedRect(FurnaceRecipeWrapper.furnaceTexture, x+8*scale, y+(27+14-height)*scale, 500, 94, 14-height, 14, height, 256, 256, scale);
		GlStateManager.popMatrix();

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, scale);
		final float oldZ = itemRenderer.zLevel;
		itemRenderer.zLevel = 800;
		RenderHelper.renderItem(this.input.get((int) Math.floor(renderTick/100F)), Math.round(x/scale+7), Math.round(y/scale+7), partialTicks, itemRenderer, fontRenderer);
		RenderHelper.renderItem(this.output.getPrimaryStack(), Math.round(x/scale+66), Math.round(y/scale+25), partialTicks, itemRenderer, fontRenderer);
		itemRenderer.zLevel = oldZ;
		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

	public static boolean isValid(final ItemStack input, final ItemStack output){
		return input != null && output != null;
	}

	@Override
	public int getRenderWidth() {
		return 94;
	}

	@Override
	public int getRenderHeight() {
		return 66;
	}

}
