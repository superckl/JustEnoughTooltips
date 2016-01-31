package me.superckl.jet.integration.vanilla;

import me.superckl.jet.integration.IIntegrationModule;
import me.superckl.jet.integration.RecipeRegistryAdapter;

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
