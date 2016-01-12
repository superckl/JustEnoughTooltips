package me.superckl.recipetooltips.integration;

import java.util.List;

import me.superckl.recipetooltips.recipe.RecipeStack;
import me.superckl.recipetooltips.recipe.RecipeWrapper;

public abstract class RecipeRegistryAdapter implements Comparable<RecipeRegistryAdapter>{


	public abstract List<RecipeWrapper> getRecipes();
	public abstract List<RecipeWrapper> getRecipesWithOutput(RecipeStack stack, boolean strict);
	public abstract List<RecipeWrapper> getRecipesWithInput(RecipeStack stack, boolean strict);
	public abstract int getWeight();
	public abstract boolean isAddon();

	@Override
	public int compareTo(final RecipeRegistryAdapter o) {
		return this.getWeight() == o.getWeight() ? 0:this.getWeight() > o.getWeight() ? 1:-1;
	}



}
