package me.superckl.recipetooltips.handler;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.RecipeNotFound;
import me.superckl.recipetooltips.util.IRecipeMetaComparator;
import me.superckl.recipetooltips.util.LogHelper;
import me.superckl.recipetooltips.util.RenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RenderTickHandler {

	public static final ResourceLocation craftingTable = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipe.png");

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final List<IRecipe> lastRecipes = Lists.newArrayList();

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(e.type != ElementType.CROSSHAIRS || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		ItemStack toCheck = null;
		if(this.mc.thePlayer.getHeldItem() != null)
			toCheck = this.mc.thePlayer.getHeldItem();
		else{
			final MovingObjectPosition pos = this.mc.getRenderViewEntity().rayTrace(this.mc.playerController.getBlockReachDistance(), 1.0F);
			if(pos.typeOfHit == MovingObjectType.BLOCK){
				final IBlockState block = this.mc.theWorld.getBlockState(pos.getBlockPos());
				if(block != null)
					toCheck = block.getBlock().getPickBlock(pos, this.mc.theWorld, pos.getBlockPos(), this.mc.thePlayer);
			}
		}
		if(toCheck == null)
			return;
		LogHelper.info("Found block");
		if(!this.lastRecipes.isEmpty())
			if(toCheck.getItem() != this.lastRecipes.get(0).getRecipeOutput().getItem()){
				this.lastRecipes.clear();
				LogHelper.info("cleared recipes");
			}
		if(this.lastRecipes.isEmpty()){
			for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList()){
				if(recipe == null || recipe.getRecipeOutput() == null)
					continue;
				final ItemStack stack = recipe.getRecipeOutput();
				if(stack.getItem() == toCheck.getItem() && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe)){ //TODO ore recipes
					this.lastRecipes.add(recipe);
					LogHelper.info("Added recipe");
				}
			}
			if(!this.lastRecipes.isEmpty()){
				LogHelper.info("Sorting");
				this.lastRecipes.sort(new IRecipeMetaComparator());
			}else{
				final ItemStack temp = toCheck;
				LogHelper.info("Setting temp recipe");
				this.lastRecipes.add(new RecipeNotFound() {

					@Override
					public ItemStack getRecipeOutput() {
						return temp;
					}
				});
			}
		}
		if(this.lastRecipes.isEmpty()){
			LogHelper.info("Last recipes empty");
			return;
		}
		LogHelper.info("Rendering");
		//0, 0; 128x66;
		GlStateManager.pushMatrix();
		//GlStateManager.disableLighting();
		//GlStateManager.disableFog();
		//GlStateManager.enableTexture2D();
		//GlStateManager.enableBlend();
		//GlStateManager.scale(1F, 1F, 1F);
		GlStateManager.color(1F, 1F, 1F, 1F);
		final int x = e.resolution.getScaledWidth()/2 - 64;
		final int y = e.resolution.getScaledHeight()/2+8;
		RenderHelper.drawTexturedRect(RenderTickHandler.craftingTable, x, y, 0, 0, 128, 66, 128, 66, 1F);
		GlStateManager.popMatrix();
	}

}
