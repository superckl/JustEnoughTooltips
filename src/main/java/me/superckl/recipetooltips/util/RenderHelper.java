package me.superckl.recipetooltips.util;

import java.util.Locale;

import org.lwjgl.opengl.GL11;

import me.superckl.recipetooltips.reference.ModData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderHelper {

	public static final ResourceLocation widgets = new ResourceLocation(ModData.MOD_ID.toLowerCase(Locale.ENGLISH), "textures/gui/widgets.png");

	public static void drawTexturedRect(final ResourceLocation texture, final double x, final double y, final int u, final int v, final int width, final int height, final int imageWidth, final int imageHeight, final double scale) {
		RenderHelper.drawTexturedRect(texture, x, y, 0D, u, v, width, height, imageWidth, imageHeight, scale);
	}

	public static void drawTexturedRect(final ResourceLocation texture, final double x, final double y, final double z, final int u, final int v, final int width, final int height, final int imageWidth, final int imageHeight, final double scale) {
		final double minU = (double)u / (double)imageWidth;
		final double maxU = (double)(u + width) / (double)imageWidth;
		final double minV = (double)v / (double)imageHeight;
		final double maxV = (double)(v + height) / (double)imageHeight;
		RenderHelper.drawTexturedRect(texture, x, y, z, minU, maxU, minV, maxV, width, height, imageWidth, imageHeight, scale);
	}

	public static void drawTexturedRect(final ResourceLocation texture, final double x, final double y, final double z, final double minU, final double maxU, final double minV, final double maxV, final int width, final int height, final int imageWidth, final int imageHeight, final double scale) {
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldRender = tessellator.getWorldRenderer();
		worldRender.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldRender.pos(x, y + (scale*height), z).tex(minU, maxV).endVertex();
		worldRender.pos(x + (scale*width), y + (scale*height), z).tex(maxU, maxV).endVertex();
		worldRender.pos(x + (scale*width), y, z).tex(maxU, minV).endVertex();
		worldRender.pos(x, y, z).tex(minU, minV).endVertex();
		tessellator.draw();
	}

	public static void setGLColorFromInt(final int color) {
		final float red = ((color >> 16) & 255) / 255.0F;
		final float green = ((color >> 8) & 255) / 255.0F;
		final float blue = (color & 255) / 255.0F;
		GL11.glColor4f(red, green, blue, 1.0F);
	}

	public static void renderItem(final ItemStack itemstack, final int rX, final int rY, final float partialTicks, final RenderItem itemRenderer, final FontRenderer fontRenderer){
		final float f = itemstack.animationsToGo - partialTicks;

		if (f > 0.0F)
		{
			GlStateManager.pushMatrix();
			final float f1 = 1.0F + f / 5.0F;
			GlStateManager.translate(rX + 8, rY + 12, 0.0F);
			GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
			GlStateManager.translate((-(rX + 8)), (-(rY + 12)), 0.0F);
		}

		itemRenderer.renderItemAndEffectIntoGUI(itemstack, rX, rY);

		if (f > 0.0F)
			GlStateManager.popMatrix();

		itemRenderer.renderItemOverlays(fontRenderer, itemstack, rX, rY);
	}

	public static void outlineGuiArea(final int x, final int y, final int z, final int width, final int height, final float scale){
		final double six = 6D/256D;
		final double seven = 7D/256D;
		final double thirteen = 13D/256D;
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x-6, y-6, z, 0, 0, 6, 6, 256, 256, scale);//top left
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x-6, y, z, 0, six, six, seven, 6, height, 256, 256, scale);//left
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x-6, y+height, z, 0, 7, 6, 6, 256, 256, scale);//bottom left
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x, y+height, z, six, seven, seven, thirteen, width, 6, 256, 256, scale);//bottom
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x+width, y+height, z, 7, 7, 6, 6, 256, 256, scale);//bottom right
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x+width, y, z, seven, thirteen, six, seven, 6, height, 256, 256, scale);//right
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x+width, y-6, z, 7, 0, 6, 6, 256, 256, scale);//top right
		RenderHelper.drawTexturedRect(RenderHelper.widgets, x, y-6, z, six, seven, 0, six, width, 6, 256, 256, scale);//top
	}

}
