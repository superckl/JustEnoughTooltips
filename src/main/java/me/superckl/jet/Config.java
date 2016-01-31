package me.superckl.jet;

import java.io.File;

import me.superckl.jet.reference.ModData;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {

	public static final String CATEGORY_RENDERING = "rendering";

	public static Configuration config;
	public static boolean renderInGame;
	public static boolean renderInTooltips;
	public static int xPosInGame;
	public static int yPosInGame;
	public static int xPosInTooltip;
	public static int yPosInTooltip;
	public static int xPaddingInGame;
	public static int yPaddingInGame;
	public static int xPaddingInTooltip;
	public static int yPaddingInTooltip;
	public static float scaleInGame;
	public static float scaleInTooltip;

	public static void init(final File file){
		if(file != null){
			final Configuration config = new Configuration(file);
			Config.config = config;
		}
		Config.renderInGame = Config.config.getBoolean("In World", Config.CATEGORY_RENDERING, true, "If false, RecipeTooltips will not render recipes below the crosshair.");
		Config.renderInTooltips = Config.config.getBoolean("In Tooltips", Config.CATEGORY_RENDERING, true, "If false, RecipeTooltips will not render recipes in tooltips.");
		Config.xPosInGame = Config.config.getInt("In Game x Position", Config.CATEGORY_RENDERING, -1, -1, Integer.MAX_VALUE, "If greater than 0, this value will override the default x position of the in-game rendering.");
		Config.yPosInGame = Config.config.getInt("In Game y Position", Config.CATEGORY_RENDERING, -1, -1, Integer.MAX_VALUE, "If greater than 0, this value will override the default y position of the in-game rendering.");
		Config.xPosInTooltip = Config.config.getInt("Tooltip x Position", Config.CATEGORY_RENDERING, -1, -1, Integer.MAX_VALUE, "If greater than 0, this value will override the default x position of the tooltip rendering.");
		Config.yPosInTooltip = Config.config.getInt("Tooltip y Position", Config.CATEGORY_RENDERING, -1, -1, Integer.MAX_VALUE, "If greater than 0, this value will override the default y position of the toolitp rendering.");
		Config.xPaddingInGame = Config.config.getInt("In Game x Padding", Config.CATEGORY_RENDERING, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "This value will be added to the x position of the in-game rendering.");
		Config.yPaddingInGame = Config.config.getInt("In Game y Padding", Config.CATEGORY_RENDERING, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "This value will be added to the y position of the in-game rendering.");
		Config.xPaddingInTooltip = Config.config.getInt("Tooltip x Padding", Config.CATEGORY_RENDERING, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "This value will be added to the x position of the tooltip rendering.");
		Config.yPaddingInTooltip = Config.config.getInt("Tooltip y Padding", Config.CATEGORY_RENDERING, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "This value will be added to the y position of the toolitp rendering.");
		Config.scaleInGame = Config.config.getFloat("In Game Scale", Config.CATEGORY_RENDERING, .9F, 0, Integer.MAX_VALUE, "The scale of the in-game rendering.");
		Config.scaleInTooltip = Config.config.getFloat("Tooltip Scale", Config.CATEGORY_RENDERING, .9F, 0, Integer.MAX_VALUE, "The scale of the tooltip rendering.");

		Config.config.save();
	}

	@SubscribeEvent
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent e){
		if(e.modID.equals(ModData.MOD_ID))
			Config.init(null);
	}

}
