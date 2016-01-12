package me.superckl.recipetooltips.integration.jei;

import lombok.Getter;
import me.superckl.recipetooltips.util.LogHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;

@Getter
@JEIPlugin
public class JEIIntegrationModule implements IModPlugin{

	public static IJeiHelpers jeiHelpers;
	public static IItemRegistry itemRegistry;
	public static IRecipeRegistry recipeRegistry;

	@Override
	public void onJeiHelpersAvailable(final IJeiHelpers jeiHelpers) {
		JEIIntegrationModule.jeiHelpers = jeiHelpers;
	}

	@Override
	public void onItemRegistryAvailable(final IItemRegistry itemRegistry) {
		JEIIntegrationModule.itemRegistry = itemRegistry;
	}

	@Override
	public void register(final IModRegistry registry) {}

	@Override
	public void onRecipeRegistryAvailable(final IRecipeRegistry recipeRegistry) {
		JEIIntegrationModule.recipeRegistry = recipeRegistry;
		for(final IRecipeCategory cat:JEIIntegrationModule.recipeRegistry.getRecipeCategories())
			for(final Object recipe:JEIIntegrationModule.recipeRegistry.getRecipes(cat))
				LogHelper.info(recipe.toString());
	}

}
