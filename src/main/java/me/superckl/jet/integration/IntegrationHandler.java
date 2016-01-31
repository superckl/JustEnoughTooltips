package me.superckl.jet.integration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.superckl.jet.util.LogHelper;
import net.minecraftforge.fml.common.Loader;

public class IntegrationHandler {

	public static final Map<List<String>, String> modules = Maps.newIdentityHashMap();

	static{
		IntegrationHandler.modules.put(Lists.<String>newArrayList(), "me.superckl.recipetooltips.integration.vanilla.VanillaIntegrationModule");
		IntegrationHandler.modules.put(Lists.newArrayList("JEI"), "me.superckl.recipetooltips.integration.jei.JEIIntegrationModule");
	}

	private final List<IIntegrationModule> loadedModules = Lists.newArrayList();
	private RecipeRegistryAdapter mainRecipeAdapter;
	private final List<RecipeRegistryAdapter> addonRecipeAdapters = Lists.newArrayList();

	public void preInit(){
		outside:
			for(final Entry<List<String>, String> entry:IntegrationHandler.modules.entrySet()){
				for(final String mod:entry.getKey())
					if(!Loader.isModLoaded(mod))
						continue outside;
				try {
					final Class<? extends IIntegrationModule> clazz = (Class<? extends IIntegrationModule>) Class.forName(entry.getValue());
					final IIntegrationModule module = clazz.newInstance();
					this.loadedModules.add(module);
					LogHelper.info("Loaded integration module "+module.getName());
				} catch (final Exception e) {
					LogHelper.error("An error occurred while loading an integration module!");
					e.printStackTrace();
				}
			}

	}

	public void postInit(){
		for(final IIntegrationModule module:this.loadedModules)
			for(final RecipeRegistryAdapter adapter:module.getRecipeRegistryAdapters())
				if(adapter.isAddon())
					this.addonRecipeAdapters.add(adapter);
				else if(this.mainRecipeAdapter == null || adapter.compareTo(this.mainRecipeAdapter) > 0)
					this.mainRecipeAdapter = adapter;
		Collections.sort(this.addonRecipeAdapters);
		Collections.reverse(this.addonRecipeAdapters);
	}

}
