package me.superckl.jet;

import org.lwjgl.input.Keyboard;

import lombok.Getter;
import me.superckl.jet.handler.RenderTickHandler;
import me.superckl.jet.integration.JERIntegrationModule;
import me.superckl.jet.integration.JERScissorHook;
import me.superckl.jet.reference.ModData;
import mezz.jei.JustEnoughItems;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModData.MOD_ID, name = ModData.NAME, version = ModData.VERSION, acceptableRemoteVersions = "*", guiFactory = "me.superckl.jet.gui.JETGuiFactory", dependencies = "required-after:JEI@[2.23.0.108,)")
public class JustEnoughTooltips {

	@Instance(ModData.MOD_ID)
	public static JustEnoughTooltips instance;
	@Instance("JEI")
	public static JustEnoughItems jeiInstance;
	@Getter
	private final JERScissorHook scissorHook = new JERScissorHook();

	@EventHandler
	public void onPreInit(final FMLPreInitializationEvent e){
		Config.init(e.getSuggestedConfigurationFile());

	}

	@EventHandler
	public void onInit(final FMLInitializationEvent e){
		if(JERIntegrationModule.JER_API != null)
			JERIntegrationModule.JER_API.getMobRegistry().registerScissorHook(RenderTickHandler.class, this.scissorHook);

		KeyBindings.DISPLAY_1 = new KeyBinding("Display Recipe", Keyboard.KEY_LMENU, ModData.NAME);
		KeyBindings.NEXT_CATEGORY = new KeyBinding("Next Category", Keyboard.KEY_X, ModData.NAME);
		KeyBindings.SWITCH_USES_RECIPES = new KeyBinding("Swtich Uses & Recipes", Keyboard.KEY_Z, ModData.NAME);
		KeyBindings.FILL_RECIPE = new KeyBinding("Fill Recipe", Keyboard.KEY_F, ModData.NAME);
		ClientRegistry.registerKeyBinding(KeyBindings.DISPLAY_1);
		ClientRegistry.registerKeyBinding(KeyBindings.NEXT_CATEGORY);
		ClientRegistry.registerKeyBinding(KeyBindings.SWITCH_USES_RECIPES);
		ClientRegistry.registerKeyBinding(KeyBindings.FILL_RECIPE);
		MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
		MinecraftForge.EVENT_BUS.register(new Config());
	}

}
