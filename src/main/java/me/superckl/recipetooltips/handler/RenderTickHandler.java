package me.superckl.recipetooltips.handler;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.RecipeNotFound;
import me.superckl.recipetooltips.util.CraftingGridHelper;
import me.superckl.recipetooltips.util.IRecipeMetaComparator;
import me.superckl.recipetooltips.util.LogHelper;
import me.superckl.recipetooltips.util.RecipeSpacer;
import me.superckl.recipetooltips.util.RenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RenderTickHandler {

	public static final ResourceLocation craftingTable = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipe.png");

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final List<IRecipe> lastRecipes = Lists.newArrayList();
	private final Random random = new Random();
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
		//LogHelper.info("Found block");
		if(!this.lastRecipes.isEmpty())
			//LogHelper.info(toCheck.toString()+"|"+this.lastRecipes.get(0).getRecipeOutput().toString());
			if(toCheck.getItem() != this.lastRecipes.get(0).getRecipeOutput().getItem()){
				this.lastRecipes.clear();
				this.itemIndex = 0;
				this.recipeIndex = 0;
			}
		//LogHelper.info("cleared recipes");
		if(this.lastRecipes.isEmpty()){
			for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList()){
				if(recipe == null)
					continue;
				final ItemStack stack = recipe.getRecipeOutput();
				//LogHelper.info("Checking recipe...");
				if(ItemStack.areItemsEqual(toCheck, stack) && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe))
					this.lastRecipes.add(recipe);
				//LogHelper.info("Added recipe");
			}
			if(!this.lastRecipes.isEmpty())
				//LogHelper.info("Sorting");
				this.lastRecipes.sort(new IRecipeMetaComparator());
			else{
				final ItemStack temp = toCheck;
				//LogHelper.info("Setting temp recipe");
				this.lastRecipes.add(new RecipeNotFound() {

					@Override
					public ItemStack getRecipeOutput() {
						return temp;
					}
				});
			}
		}
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0) instanceof RecipeNotFound)
			//LogHelper.info("Last recipes empty");
			return;
		//LogHelper.info("Rendering");
		//0, 0; 128x66;
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		//GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		//GlStateManager.scale(1F, 1F, 1F);
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

		List<ItemStack> items = Collections.EMPTY_LIST;
		final IRecipe recipe = this.lastRecipes.get(this.recipeIndex);

		if(recipe instanceof ShapedRecipes){
			items = Lists.newArrayList();
			final ShapedRecipes sRecipe = (ShapedRecipes) recipe;
			final ItemStack[] input = sRecipe.recipeItems;
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(row < sRecipe.recipeHeight && column < sRecipe.recipeWidth && row+column*sRecipe.recipeWidth < input.length)
						items.add(input[row+column*sRecipe.recipeWidth]);
					else
						items.add(null);
		}else if(recipe instanceof ShapelessRecipes)
			items = ((ShapelessRecipes)recipe).recipeItems;
		else if(recipe instanceof ShapelessOreRecipe){
			items = Lists.newArrayList();
			for(final Object obj:((ShapelessOreRecipe)recipe).getInput())
				if(obj instanceof ItemStack)
					items.add((ItemStack) obj);
				else if(obj instanceof List){
					final List<ItemStack> list = (List<ItemStack>) obj;
					if(list.isEmpty())
						items.add(null);
					else{
						final List<ItemStack> newList = Lists.newArrayList();
						for(final ItemStack stack:list)
							if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
								stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabAllSearch, newList);
							else
								newList.add(stack);
						items.add(newList.get((int) (Math.floor((this.itemIndex/80F)) % newList.size())));
					}
				}
		}else if(recipe instanceof ShapedOreRecipe){
			items = Lists.newArrayList();
			final ShapedOreRecipe sRecipe = (ShapedOreRecipe) recipe;
			final Object[] input = sRecipe.getInput();
			final int widthHeight = CraftingGridHelper.getWidthHeight(input.length);
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < widthHeight && row < widthHeight && row+column*widthHeight < input.length){
						final Object obj = input[row+column*widthHeight];
						if(obj == null)
							items.add(null);
						else if(obj instanceof ItemStack)
							items.add((ItemStack) obj);
						else if(obj instanceof List){
							final List<ItemStack> list = (List<ItemStack>) obj;
							if(list.isEmpty())
								items.add(null);
							else{
								final List<ItemStack> newList = Lists.newArrayList();
								for(final ItemStack stack:list)
									if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
										stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabAllSearch, newList);
									else
										newList.add(stack);
								items.add(newList.get((int) (Math.floor(this.itemIndex/80F) % newList.size())));
							}
						}
					}else
						items.add(null);
		}
		//TODO cache
		final RecipeSpacer spacer = new RecipeSpacer(3, 3);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.8D, .8D, .8D);
		for(final ItemStack itemstack:items){
			if(!spacer.hasNext())
				break;
			final int[] xy = spacer.next();
			if(itemstack == null)
				continue;
			final int rX = Math.round(x/scale+(xy[0]+7));
			final int rY = Math.round(y/scale+(xy[1]+7));
			RenderHelper.renderItem(itemstack, rX, rY, e.partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);
		}
		//97, 21; 4 in
		RenderHelper.renderItem(recipe.getRecipeOutput(), Math.round(x/scale+101), Math.round(y/scale+25), e.partialTicks, this.mc.getRenderItem(), this.mc.fontRendererObj);

		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.itemIndex++;
	}

	@SubscribeEvent
	public void onMouseInput(MouseEvent e){
		if(this.lastRecipes.isEmpty() || this.lastRecipes.get(0) instanceof RecipeNotFound || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
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
