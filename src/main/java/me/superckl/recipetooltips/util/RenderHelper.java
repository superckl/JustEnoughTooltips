package me.superckl.recipetooltips.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderHelper {

	public static void drawTexturedRect(final ResourceLocation texture, final double x, final double y, final int u, final int v, final int width, final int height, final int imageWidth, final int imageHeight, final double scale) {
		RenderHelper.drawTexturedRect(texture, x, y, 0D, u, v, width, height, imageWidth, imageHeight, scale);
	}

	public static void drawTexturedRect(final ResourceLocation texture, final double x, final double y, final double z, final int u, final int v, final int width, final int height, final int imageWidth, final int imageHeight, final double scale) {
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
		final double minU = (double)u / (double)imageWidth;
		final double maxU = (double)(u + width) / (double)imageWidth;
		final double minV = (double)v / (double)imageHeight;
		final double maxV = (double)(v + height) / (double)imageHeight;
		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldRender = tessellator.getWorldRenderer();
		worldRender.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldRender.finishDrawing();
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

}
