package me.superckl.recipetooltips.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.CraftingRecipeWrapper;
import me.superckl.recipetooltips.recipe.FurnaceRecipeWrapper;
import me.superckl.recipetooltips.recipe.RecipeMultiItemStack;
import me.superckl.recipetooltips.recipe.RecipeNotFound;
import me.superckl.recipetooltips.recipe.RecipeWrapper;
import me.superckl.recipetooltips.util.RecipeWrapperMetaComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
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

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final List<RecipeWrapper> recipes = Lists.newArrayList();
	private ItemStack lastStack;
	private int renderTick;
	private int recipeIndex;

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(this.mc.currentScreen != null || e.type != ElementType.CROSSHAIRS || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
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
		if(this.recipes.isEmpty() || this.recipes.get(0).getWrappedRecipe() instanceof RecipeNotFound)
			return;
		this.checkLastItem(toCheck);
		final RecipeWrapper wrapper = this.recipes.get(this.recipeIndex);
		final float scale = .8F;
		final int x = Math.round(e.resolution.getScaledWidth()/2 - (wrapper.getRenderWidth()/2)*scale);
		final int y = e.resolution.getScaledHeight()/2+8;
		wrapper.renderToScreen(x, y, scale, e.partialTicks, this.renderTick, this.mc.getRenderItem(), this.mc.fontRendererObj);
		this.renderTick++;
		this.lastStack = toCheck;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.recipes.isEmpty() || this.recipes.get(0).getWrappedRecipe() instanceof RecipeNotFound || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		if(e.dwheel != 0){
			e.setCanceled(true);
			this.recipeIndex += Math.signum(e.dwheel);
			if(this.recipeIndex < 0)
				this.recipeIndex = this.recipes.size()-1;
			else if(this.recipeIndex >= this.recipes.size())
				this.recipeIndex = 0;
		}
	}

	@SubscribeEvent
	public void onMouseInput2(final MouseInputEvent e){
		if(this.recipes.isEmpty() || this.recipes.get(0).getWrappedRecipe() instanceof RecipeNotFound || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		if(Mouse.getEventDWheel() != 0){
			e.setCanceled(true);
			this.recipeIndex += Math.signum(Mouse.getEventDWheel());
			if(this.recipeIndex < 0)
				this.recipeIndex = this.recipes.size()-1;
			else if(this.recipeIndex >= this.recipes.size())
				this.recipeIndex = 0;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderTooltip(final ItemTooltipEvent e){
		if(e.itemStack == null || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		this.findRecipes(e.itemStack);
		if(this.recipes.isEmpty() || this.recipes.get(0).getWrappedRecipe() instanceof RecipeNotFound)
			return;
		this.checkLastItem(e.itemStack);
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final RecipeWrapper wrapper = this.recipes.get(this.recipeIndex);
		final float scale = .8F;
		final int x = Math.round(Mouse.getEventX() / resolution.getScaleFactor()-wrapper.getRenderWidth()*scale-8);
		final int y = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();
		wrapper.renderToScreen(x, y, scale, this.mc.timer.renderPartialTicks, this.renderTick, this.mc.getRenderItem(), this.mc.fontRendererObj);
		this.renderTick++;
		this.lastStack = e.itemStack;
	}

	private void findRecipes(final ItemStack toCheck){
		if(!this.recipes.isEmpty())
			if(toCheck.getItem() != this.recipes.get(0).getOutput().getPrimaryStack().getItem()){
				this.recipes.clear();
				this.renderTick = 0;
				this.recipeIndex = 0;
			}
		if(this.recipes.isEmpty()){
			for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList()){
				if(!CraftingRecipeWrapper.isValid(recipe))
					continue;
				final ItemStack stack = recipe.getRecipeOutput();
				if(toCheck.getItem() == stack.getItem() && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe))
					this.recipes.add(CraftingRecipeWrapper.fromRecipe(recipe));
			}
			for(final Entry<ItemStack, ItemStack> entry:FurnaceRecipes.instance().getSmeltingList().entrySet()){
				if(!FurnaceRecipeWrapper.isValid(entry.getKey(), entry.getValue()))
					continue;
				final ItemStack stack = entry.getValue();
				if(stack.getItem() == toCheck.getItem())
					this.recipes.add(new FurnaceRecipeWrapper(RecipeMultiItemStack.fromOreDict(entry.getKey()), RecipeMultiItemStack.from(stack)));
			}
			if(this.recipes.isEmpty()){
				final ItemStack temp = toCheck;
				this.recipes.add(CraftingRecipeWrapper.fromRecipe(new RecipeNotFound() {

					@Override
					public ItemStack getRecipeOutput() {
						return temp;
					}
				}));
			}else{
				Collections.sort(this.recipes, new RecipeWrapperMetaComparator(toCheck.getItemDamage()));
				Collections.reverse(this.recipes);
			}
		}
	}

	private void checkLastItem(final ItemStack toCheck){
		if(this.lastStack != null && !toCheck.isItemEqual(this.lastStack)){
			this.renderTick = 0;
			this.recipeIndex = 0;
			Collections.sort(this.recipes, new RecipeWrapperMetaComparator(toCheck.getItemDamage()));
			Collections.reverse(this.recipes);
		}
	}

}
