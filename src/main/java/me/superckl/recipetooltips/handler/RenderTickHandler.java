package me.superckl.recipetooltips.handler;

import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.CraftingRecipeWrapper;
import me.superckl.recipetooltips.recipe.RecipeItemStack;
import me.superckl.recipetooltips.recipe.RecipeNotFound;
import me.superckl.recipetooltips.util.CraftingRecipeMetaComparator;
import me.superckl.recipetooltips.util.RecipeSpacer;
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
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RenderTickHandler {

	public static final ResourceLocation craftingTable = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipe.png");

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final List<CraftingRecipeWrapper> lastRecipes = Lists.newArrayList();
	private final Random random = new Random();
	private final RecipeSpacer spacer = new RecipeSpacer(3, 3);
	private int itemIndex;
	private int recipeIndex;

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
		if(!this.lastRecipes.isEmpty())
			if(toCheck.getItem() != this.lastRecipes.get(0).getOutput().getItem()){
				this.lastRecipes.clear();
				this.itemIndex = 0;
				this.recipeIndex = 0;
			}
		if(this.lastRecipes.isEmpty()){
			for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList()){
				if(recipe == null)
					continue;
				final ItemStack stack = recipe.getRecipeOutput();
				if(ItemStack.areItemsEqual(toCheck, stack) && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe))
					this.lastRecipes.add(CraftingRecipeWrapper.fromRecipe(recipe));
			}
			if(!this.lastRecipes.isEmpty())
				this.lastRecipes.sort(new CraftingRecipeMetaComparator());
			else{
				final ItemStack temp = toCheck;
				this.lastRecipes.add(CraftingRecipeWrapper.fromRecipe(new RecipeNotFound() {

					@Override
					public ItemStack getRecipeOutput() {
						return temp;
					}
				}));
			}
		}
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0).getWrappedRecipe() instanceof RecipeNotFound)
			return;
		//0, 0; 128x66;
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.enableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		final float scale = .8F;
		final int x = Math.round(e.resolution.getScaledWidth()/2 - 64*scale);
		final int y = e.resolution.getScaledHeight()/2+8;
		RenderHelper.drawTexturedRect(RenderTickHandler.craftingTable, x, y, 0, 0, 128, 66, 128, 66, scale);
		GlStateManager.popMatrix();

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

		final CraftingRecipeWrapper wrapper = this.lastRecipes.get(this.recipeIndex);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.8D, .8D, .8D);
		final RecipeItemStack[] stacks = wrapper.getIngredients();
		this.spacer.reset();
		for (final RecipeItemStack stack : stacks) {
			if(!this.spacer.hasNext())
				break;
			final int[] xy = this.spacer.next();
			if(stack == null)
				continue;
			final int rX = Math.round(x/scale+(xy[0]+7));
			final int rY = Math.round(y/scale+(xy[1]+7));
			RenderHelper.renderItem(stack.get(Math.round(this.itemIndex/80F)), rX, rY, e.partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);
		}
		//97, 21; 4 in
		RenderHelper.renderItem(wrapper.getOutput(), Math.round(x/scale+101), Math.round(y/scale+25), e.partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);

		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.itemIndex++;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0).getWrappedRecipe() instanceof RecipeNotFound || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		if(e.dwheel != 0){
			e.setCanceled(true);
			this.recipeIndex += Math.signum(e.dwheel);
			if(this.recipeIndex < 0)
				this.recipeIndex = this.lastRecipes.size()-1;
			else if(this.recipeIndex >= this.lastRecipes.size())
				this.recipeIndex = 0;
		}
	}

}
