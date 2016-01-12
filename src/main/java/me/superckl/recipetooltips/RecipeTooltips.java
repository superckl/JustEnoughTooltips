package me.superckl.recipetooltips;

import org.lwjgl.input.Keyboard;

import me.superckl.recipetooltips.handler.RenderTickHandler;
import me.superckl.recipetooltips.reference.ModData;
import mezz.jei.JustEnoughItems;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModData.MOD_ID, name = ModData.NAME, version = ModData.VERSION, acceptableRemoteVersions = "*", dependencies = "required-after:JEI")
public class RecipeTooltips {

	@Instance(ModData.MOD_ID)
	public static RecipeTooltips instance;
	@Instance("JEI")
	public static JustEnoughItems jeiInstance;
	//private IntegrationHandler integrationHandler;

	@EventHandler
	public void onPreInit(final FMLPreInitializationEvent e){
		//this.integrationHandler = new IntegrationHandler();
		//this.integrationHandler.preInit();
	}

	@EventHandler
	public void onInit(final FMLInitializationEvent e){
		KeyBindings.DISPLAY_1 = new KeyBinding("Display 1", Keyboard.KEY_LMENU, ModData.NAME);
		KeyBindings.NEXT_CATEGORY = new KeyBinding("Next Category", Keyboard.KEY_X, ModData.NAME);
		KeyBindings.SWITCH_USES_RECIPES = new KeyBinding("Swtich Uses & Recipes", Keyboard.KEY_Z, ModData.NAME);
		ClientRegistry.registerKeyBinding(KeyBindings.DISPLAY_1);
		ClientRegistry.registerKeyBinding(KeyBindings.NEXT_CATEGORY);
		ClientRegistry.registerKeyBinding(KeyBindings.SWITCH_USES_RECIPES);
		MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
	}

}
