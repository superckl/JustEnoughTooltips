package me.superckl.recipetooltips.handler;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.CraftingRecipeWrapper;
import me.superckl.recipetooltips.recipe.RecipeMultiItemStack;
import me.superckl.recipetooltips.recipe.RecipeNotFound;
import me.superckl.recipetooltips.util.CraftingRecipeMetaComparator;
import me.superckl.recipetooltips.util.LogHelper;
import me.superckl.recipetooltips.util.RecipeSpacer;
import me.superckl.recipetooltips.util.RenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
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
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RenderTickHandler {

	public static final ResourceLocation craftingTable = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipe.png");

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final List<CraftingRecipeWrapper> lastRecipes = Lists.newArrayList();
	private final Random random = new Random();
	private final RecipeSpacer spacer = new RecipeSpacer(3, 3);
	private ItemStack lastStack;
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
		this.findRecipes(toCheck);
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0).getWrappedRecipe() instanceof RecipeNotFound)
			return;
		this.checkLastItem(toCheck);
		//0, 0; 128x66;
		final float scale = .8F;
		final int x = Math.round(e.resolution.getScaledWidth()/2 - 64*scale);
		final int y = e.resolution.getScaledHeight()/2+8;
		this.renderCraftingMatrix(this.lastRecipes.get(this.recipeIndex), x, y, scale, e.partialTicks);
		this.itemIndex++;
		this.lastStack = toCheck;
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderTooltip(final ItemTooltipEvent e){
		if(e.itemStack == null || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		this.findRecipes(e.itemStack);
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0).getWrappedRecipe() instanceof RecipeNotFound)
			return;
		this.checkLastItem(e.itemStack);
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final float scale = .8F;
		final int x = Math.round(Mouse.getEventX() / resolution.getScaleFactor()-128*scale-8);//Math.round(resolution.getScaledWidth()/2 - 64*scale);
		final int y = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();//resolution.getScaledHeight()/2+8;
		this.renderCraftingMatrix(this.lastRecipes.get(this.recipeIndex), x, y, scale, this.mc.timer.renderPartialTicks);
		this.itemIndex++;
		this.lastStack = e.itemStack;
	}

	private void findRecipes(final ItemStack toCheck){
		if(!this.lastRecipes.isEmpty())
			if(toCheck.getItem() != this.lastRecipes.get(0).getOutput().get(0).getItem()){
				this.lastRecipes.clear();
				this.itemIndex = 0;
				this.recipeIndex = 0;
			}
		if(this.lastRecipes.isEmpty()){
			for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList()){
				if(!CraftingRecipeWrapper.isValid(recipe))
					continue;
				final ItemStack stack = recipe.getRecipeOutput();
				if(stack != null && toCheck.getItem() == stack.getItem() && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe))
					this.lastRecipes.add(CraftingRecipeWrapper.fromRecipe(recipe));
			}
			if(this.lastRecipes.isEmpty()){
				final ItemStack temp = toCheck;
				this.lastRecipes.add(CraftingRecipeWrapper.fromRecipe(new RecipeNotFound() {

					@Override
					public ItemStack getRecipeOutput() {
						return temp;
					}
				}));
			}
		}
	}

	private void checkLastItem(final ItemStack toCheck){
		if(this.lastStack != null && !toCheck.isItemEqual(this.lastStack)){
			LogHelper.info("resetting index");
			this.itemIndex = 0;
			this.recipeIndex = 0;
			Collections.sort(this.lastRecipes, new CraftingRecipeMetaComparator(toCheck.getItemDamage()));
			Collections.reverse(this.lastRecipes);
		}
	}


	private void renderCraftingMatrix(final CraftingRecipeWrapper wrapper, final int x, final int y, final float scale, final float partialTicks){
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.enableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		RenderHelper.drawTexturedRect(RenderTickHandler.craftingTable, x, y, 500, 0, 0, 128, 66, 128, 66, scale);
		GlStateManager.popMatrix();

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		GlStateManager.scale(.8D, .8D, .8D);
		final RecipeMultiItemStack[] stacks = wrapper.getIngredients();
		this.spacer.reset();
		final float oldZ = this.mc.getRenderItem().zLevel;
		this.mc.getRenderItem().zLevel = 800;
		for(int row = 0; row < 3; row++)
			for(int column = 0; column < 3; column++){
				final int[] xy = this.spacer.next();
				if(column < wrapper.getDimension() && row < wrapper.getDimension() && column+row*3 < stacks.length){
					final RecipeMultiItemStack stack = stacks[column+row*3];
					if(stack == null)
						continue;
					final int rX = Math.round(x/scale+(xy[0]+7));
					final int rY = Math.round(y/scale+(xy[1]+7));
					RenderHelper.renderItem(stack.get(Math.round(this.itemIndex/80F)), rX, rY, partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);
				}
			}
		//97, 21; 4 in
		RenderHelper.renderItem(wrapper.getOutput().get(0), Math.round(x/scale+101), Math.round(y/scale+25), partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);
		this.mc.getRenderItem().zLevel = oldZ;
		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

}
