package me.superckl.jet.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.jet.util.CraftingGridHelper;
import me.superckl.jet.util.ItemStackHelper;
import me.superckl.jet.util.RecipeSpacer;
import me.superckl.jet.util.RenderHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CraftingRecipeWrapper extends RecipeWrapper{

	public static final ResourceLocation craftingTableTexture = new ResourceLocation("recipetooltips", "textures/gui/tooltiprecipecrafting.png");

	private final int dimension;
	private final RecipeMultiItemStack[] ingredients;
	private final RecipeMultiItemStack output;
	private final IRecipe wrappedRecipe;

	public static CraftingRecipeWrapper fromRecipe(final IRecipe recipe){
		final int dimension = CraftingGridHelper.getWidthHeight(recipe.getRecipeSize());
		int height = dimension, width = dimension;
		RecipeMultiItemStack[] ingredients = new RecipeMultiItemStack[0];
		if(recipe instanceof ShapedRecipes){
			final ShapedRecipes sRecipe = (ShapedRecipes) recipe;
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			int index = 0;
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < sRecipe.recipeWidth && row < sRecipe.recipeHeight && index < sRecipe.recipeItems.length)
						items.add(RecipeMultiItemStack.fromOreDict(sRecipe.recipeItems[index++]));
					else
						items.add(null);
			height = 3;
			width = 3;
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}else if(recipe instanceof ShapelessRecipes)
			ingredients = ItemStackHelper.fromItemStacks(((ShapelessRecipes) recipe).recipeItems.toArray(new ItemStack[dimension*dimension]), true);
		else if(recipe instanceof ShapelessOreRecipe){
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			for(final Object obj:((ShapelessOreRecipe)recipe).getInput())
				if(obj instanceof ItemStack)
					items.add(RecipeMultiItemStack.fromOreDict((ItemStack) obj));
				else if(obj instanceof List){
					final List<ItemStack> list = (List<ItemStack>) obj;
					if(list.isEmpty())
						items.add(null);
					else
						items.add(RecipeMultiItemStack.fromOreDict(list));
				}else if(obj == null)
					items.add(null);
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}else if(recipe instanceof ShapedOreRecipe){
			final List<RecipeMultiItemStack> items = Lists.newArrayList();
			final ShapedOreRecipe sRecipe = (ShapedOreRecipe) recipe;
			final Object[] input = sRecipe.getInput();
			for(int row = 0; row < 3; row++)
				for(int column = 0; column < 3; column++)
					if(column < dimension && row < dimension && column+row*dimension < input.length){
						final Object obj = input[column+row*dimension];
						if(obj == null)
							items.add(null);
						else if(obj instanceof ItemStack)
							items.add(RecipeMultiItemStack.fromOreDict((ItemStack) obj));
						else if(obj instanceof List){
							final List<ItemStack> list = (List<ItemStack>) obj;
							if(list.isEmpty())
								items.add(null);
							else
								items.add(RecipeMultiItemStack.fromOreDict(list));
						}
					}else
						items.add(null);
			height = width = 3;
			ingredients = items.toArray(new RecipeMultiItemStack[items.size()]);
		}
		final RecipeMultiItemStack[] rStacks = new RecipeMultiItemStack[9];

		int index = 0;
		for(int row = 0; row < 3; row++)
			for(int column = 0; column < 3; column++)
				if(column < width && row < height && index < ingredients.length)
					rStacks[column+row*3] = ingredients[index++];
				else
					rStacks[column+row*3] = null;

		return new CraftingRecipeWrapper(dimension, rStacks, RecipeMultiItemStack.from(recipe.getRecipeOutput()), recipe);
	}

	public static boolean isValid(final IRecipe recipe){
		return recipe != null && recipe.getRecipeOutput() != null && recipe.getRecipeSize() != 0 && (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe);
	}

	@Override
	public void renderToScreen(final int x, final int y, final float scale, final float partialTicks, final int renderTick, final RenderItem itemRenderer, final FontRenderer fontRenderer) {
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.enableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		RenderHelper.drawTexturedRect(CraftingRecipeWrapper.craftingTableTexture, x, y, 500, 0, 0, 128, 66, 128, 66, scale);
		GlStateManager.popMatrix();

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, scale);
		final RecipeMultiItemStack[] stacks = this.getIngredients();
		final RecipeSpacer spacer = new RecipeSpacer(3, 3);
		final float oldZ = itemRenderer.zLevel;
		itemRenderer.zLevel = 800;
		for(int row = 0; row < 3; row++)
			for(int column = 0; column < 3; column++){
				final int[] xy = spacer.next();
				if(column < this.getDimension() && row < this.getDimension() && column+row*3 < stacks.length){
					final RecipeMultiItemStack stack = stacks[column+row*3];
					if(stack == null)
						continue;
					final int rX = Math.round(x/scale+(xy[0]+7));
					final int rY = Math.round(y/scale+(xy[1]+7));
					RenderHelper.renderItem(stack.get((int) Math.floor(renderTick/80F)), rX, rY, partialTicks, itemRenderer, fontRenderer);
				}
			}
		//97, 21; 4 in
		RenderHelper.renderItem(this.getOutput().get(0), Math.round(x/scale+101), Math.round(y/scale+25), partialTicks, itemRenderer, fontRenderer);
		itemRenderer.zLevel = oldZ;
		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

	@Override
	public int getRenderWidth() {
		return 128;
	}

	@Override
	public int getRenderHeight() {
		return 66;
	}

}
