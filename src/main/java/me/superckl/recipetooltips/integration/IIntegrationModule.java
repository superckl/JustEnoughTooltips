package me.superckl.recipetooltips.integration;

public interface IIntegrationModule {

	public String getName();
	public RecipeRegistryAdapter[] getRecipeRegistryAdapters();
}
