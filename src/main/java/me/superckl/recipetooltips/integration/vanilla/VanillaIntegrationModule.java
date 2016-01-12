package me.superckl.recipetooltips.integration.vanilla;

import me.superckl.recipetooltips.integration.IIntegrationModule;
import me.superckl.recipetooltips.integration.RecipeRegistryAdapter;

public class VanillaIntegrationModule implements IIntegrationModule{

	@Override
	public String getName() {
		return "Vanilla Integration";
	}

	@Override
	public RecipeRegistryAdapter[] getRecipeRegistryAdapters() {
		return new RecipeRegistryAdapter[] {new VanillaRecipeRegistryAdapter()};
	}

}
