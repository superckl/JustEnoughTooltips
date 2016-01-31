package me.superckl.jet.integration.vanilla;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import me.superckl.jet.integration.RecipeRegistryAdapter;
import me.superckl.jet.recipe.CraftingRecipeWrapper;
import me.superckl.jet.recipe.FurnaceRecipeWrapper;
import me.superckl.jet.recipe.RecipeMultiItemStack;
import me.superckl.jet.recipe.RecipeStack;
import me.superckl.jet.recipe.RecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

public class VanillaRecipeRegistryAdapter extends RecipeRegistryAdapter{

	private List<RecipeWrapper> recipes;

	@Override
	public List<RecipeWrapper> getRecipes() {
		if(this.recipes == null)
			this.wrapRecipes();
		return this.recipes;
	}

	@Override
	public List<RecipeWrapper> getRecipesWithOutput(final RecipeStack stack, final boolean strict) {
		final List<RecipeWrapper> wrappers = this.getRecipes();
		final List<RecipeWrapper> forOutput = Lists.newArrayList();
		for(final RecipeWrapper wrapper:wrappers){

		}
		return null;
	}

	@Override
	public List<RecipeWrapper> getRecipesWithInput(final RecipeStack stack, final boolean strict) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAddon() {
		return false;
	}

	@Override
	public int getWeight() {
		return 0;
	}

	public void wrapRecipes(){
		this.recipes = Lists.newArrayList();
		for(final IRecipe recipe:CraftingManager.getInstance().getRecipeList())
			if(CraftingRecipeWrapper.isValid(recipe))
				this.recipes.add(CraftingRecipeWrapper.fromRecipe(recipe));
		for(final Entry<ItemStack, ItemStack> entry:FurnaceRecipes.instance().getSmeltingList().entrySet())
			if(FurnaceRecipeWrapper.isValid(entry.getKey(), entry.getValue()))
				this.recipes.add(new FurnaceRecipeWrapper(RecipeMultiItemStack.fromOreDict(entry.getKey()), RecipeMultiItemStack.fromOreDict(entry.getValue())));
	}

	/*private void findRecipes(final ItemStack toCheck){
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
}*/

}
