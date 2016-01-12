package me.superckl.recipetooltips.gui;

import java.util.List;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.Config;
import me.superckl.recipetooltips.reference.ModData;
import me.superckl.recipetooltips.util.LogHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class RecipeTooltipsConfigGui extends GuiConfig{

	public RecipeTooltipsConfigGui(final GuiScreen parent) {
		super(parent, RecipeTooltipsConfigGui.getConfigElements(), ModData.MOD_ID, false, false, "RecipeTooltips Configuration");
	}

	private static List<IConfigElement> getConfigElements() {
		final ConfigCategory categoryRendering = Config.config.getCategory(Config.CATEGORY_RENDERING);

		final List<IConfigElement> configElements = Lists.newArrayList();

		LogHelper.info(categoryRendering.getValues().size());

		configElements.add(new ConfigElement(categoryRendering));

		return configElements;
	}


	/*public static class RenderingCategory extends CategoryEntry{

		public RenderingCategory(final GuiConfig owningScreen,
				final GuiConfigEntries owningEntryList, final IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement);
		}

		@Override
		protected GuiScreen buildChildScreen(){
			return new RecipeTooltipsConfigGui(this.owningScreen, new ConfigElement(Config.config.getCategory(Config.CATEGORY_RENDERING)).getChildElements()
					, ModData.MOD_ID, false, false, "Rendering Options"));
		}

	}*/

}
