package me.superckl.recipetooltips;

import me.superckl.recipetooltips.handler.RenderTickHandler;
import me.superckl.recipetooltips.integration.IntegrationHandler;
import me.superckl.recipetooltips.reference.ModData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModData.MOD_ID, name = ModData.NAME, version = ModData.VERSION, acceptableRemoteVersions = "*", dependencies = "required-after:JEI")
public class RecipeTooltips {

	@Instance(ModData.MOD_ID)
	public static RecipeTooltips instance;

	//private IntegrationHandler integrationHandler;

	@EventHandler
	public void onPreInit(final FMLPreInitializationEvent e){
		//this.integrationHandler = new IntegrationHandler();
		//this.integrationHandler.preInit();
	}

	@EventHandler
	public void onInit(final FMLInitializationEvent e){
		MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
	}

}
