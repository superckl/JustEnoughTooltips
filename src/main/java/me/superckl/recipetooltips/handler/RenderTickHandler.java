package me.superckl.recipetooltips.handler;

import java.lang.reflect.Field;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.superckl.recipetooltips.Config;
import me.superckl.recipetooltips.KeyBindings;
import me.superckl.recipetooltips.util.LogHelper;
import me.superckl.recipetooltips.util.RenderHelper;
import mezz.jei.GuiEventHandler;
import mezz.jei.JustEnoughItems;
import mezz.jei.ProxyCommonClient;
import mezz.jei.gui.Focus;
import mezz.jei.gui.Focus.Mode;
import mezz.jei.gui.IRecipeGuiLogic;
import mezz.jei.gui.RecipeGuiLogic;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.RecipesGui;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class RenderTickHandler {

	public static Field recipesGui;
	public static Field guiEventHandler;

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final IRecipeGuiLogic logic = new RecipeGuiLogic();
	private RecipesGui gui;
	private Mode mode = Mode.OUTPUT;
	private ItemStack lastStack;
	private RecipeLayout layout;
	private boolean needsReset;

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(!Config.renderInGame || this.mc.currentScreen != null || e.type != ElementType.CROSSHAIRS || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		ItemStack toCheck = null;
		if(this.mc.thePlayer.getHeldItem() != null)
			toCheck = this.mc.thePlayer.getHeldItem();
		else{
			final MovingObjectPosition pos = this.mc.getRenderViewEntity().rayTrace(this.mc.playerController.getBlockReachDistance(), 1.0F);
			if(pos.typeOfHit == MovingObjectType.BLOCK){
				final IBlockState block = this.mc.theWorld.getBlockState(pos.getBlockPos());
				if(block != null)
					toCheck = block.getBlock().getPickBlock(pos, this.mc.theWorld, pos.getBlockPos(), this.mc.thePlayer);
			}
		}
		if(toCheck == null)
			return;
		final float scale = 1F;
		int x = Math.round(e.resolution.getScaledWidth()/2);
		int y = e.resolution.getScaledHeight()/2+13;
		this.checkLastItem(toCheck, x, y);
		if(this.layout != null){
			final int width = this.logic.getRecipeCategory().getBackground().getWidth();
			final int height = this.logic.getRecipeCategory().getBackground().getHeight();
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			x-= width/2;
			if(Config.xPosInGame >= 0)
				x = Config.xPosInGame;
			if(Config.yPosInGame >= 0)
				y = Config.yPosInGame;
			x += Config.xPaddingInGame;
			y += Config.yPaddingInGame;
			RenderHelper.outlineGuiArea(x, y, 500, width, height, scale);
			RenderHelper.fillGuiArea(x, y, 500, width, height, scale);
			GlStateManager.popMatrix();

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			GlStateManager.translate(x-this.layout.getPosX(), y-this.layout.getPosY(), 501F);
			this.layout.draw(this.mc, 0, 0);
			GlStateManager.popMatrix();
		}
		this.lastStack = toCheck;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.layout == null || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		if(e.dwheel != 0){
			e.setCanceled(true);
			if(e.dwheel > 0){
				this.logic.nextPage();
				this.needsReset = true;
			}else{
				this.logic.previousPage();
				this.needsReset = true;
			}
		}
	}

	@SubscribeEvent
	public void onMouseInput2(final MouseInputEvent e){
		if(this.layout == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		if(Mouse.getEventDWheel() != 0){
			e.setCanceled(true);
			if(Mouse.getEventDWheel() > 0){
				this.logic.nextPage();
				this.needsReset = true;
			}else{
				this.logic.previousPage();
				this.needsReset = true;
			}
		}
	}

	@SubscribeEvent//not fired when in GUI
	public void onKeyPress(final KeyInputEvent e){
		if(!Keyboard.getEventKeyState())
			return;
		if(this.layout == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		final int key = Keyboard.getEventKey();
		if(KeyBindings.NEXT_CATEGORY.getKeyCode() == key){
			if(this.logic.hasMultipleCategories()){
				this.logic.nextRecipeCategory();
				this.needsReset = true;
			}
		}else if(KeyBindings.SWITCH_USES_RECIPES.getKeyCode() == key){
			this.mode = this.mode == Mode.OUTPUT ? Mode.INPUT:Mode.OUTPUT;
			this.needsReset = true;
		}else if(this.mc.currentScreen == null && (mezz.jei.config.KeyBindings.showRecipe.getKeyCode() == key))
			try {
				this.getRecipesGui();
				if(this.lastStack != null){
					this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
					if(this.mode == Mode.OUTPUT)
						this.gui.showRecipes(new Focus(this.lastStack));
					else
						this.gui.showUses(new Focus(this.lastStack));
				}
			} catch (final Exception e1) {
				LogHelper.error("An error occurred when opening the recipes gui!");
				e1.printStackTrace();
			}
	}

	@SubscribeEvent //fired while in GUI, but isPressed returns false
	public void onKeyPress2(final KeyboardInputEvent.Pre e){
		this.onKeyPress(new KeyInputEvent());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderTooltip(final ItemTooltipEvent e){
		if(!Config.renderInTooltips || e.itemStack == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		if(this.gui == null)
			try{
				this.getRecipesGui();
			}catch (final Exception e1){
				LogHelper.error("An error occurred while getting the recipes gui!");
				e1.printStackTrace();
				return;
			}
		if(this.gui.isOpen())
			return;
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final float scale = 1F;
		int x = Math.round(Mouse.getEventX() / resolution.getScaleFactor()-8);
		int y = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();
		this.checkLastItem(e.itemStack, x, y);
		if(this.layout != null){
			final int width = this.logic.getRecipeCategory().getBackground().getWidth();
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			x -= width;
			if(Config.xPosInTooltip >= 0)
				x = Config.xPosInTooltip;
			if(Config.yPosInTooltip >= 0)
				y = Config.yPosInTooltip;
			x += Config.xPaddingInTooltip;
			y += Config.yPaddingInTooltip;
			RenderHelper.outlineGuiArea(x, y, 500, width, this.logic.getRecipeCategory().getBackground().getHeight(), scale);
			GlStateManager.popMatrix();

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			GlStateManager.translate(x-this.layout.getPosX(), y-this.layout.getPosY(), 501F);
			this.layout.draw(this.mc, 0, 0);
			GlStateManager.popMatrix();
		}
		this.lastStack = e.itemStack;
	}

	private void checkLastItem(final ItemStack toCheck, final int x, final int y){
		final boolean changedStacks = (this.lastStack != null && !toCheck.isItemEqual(this.lastStack));
		if(changedStacks)
			this.mode = Mode.OUTPUT;
		this.tempMode = this.mode;
		if(this.needsReset || changedStacks || this.lastStack == null)
			this.resetGuiLogic(toCheck, x, y);
	}

	private Mode tempMode;

	private void resetGuiLogic(final ItemStack toCheck, final int x, final int y){
		this.needsReset = false;
		final Focus focus = new Focus(toCheck);
		focus.setMode(this.mode);
		if(!this.logic.setFocus(focus)){
			this.layout = null;
			if(this.tempMode != this.mode)
				return;
			this.tempMode = this.mode;
			this.mode = this.mode == Mode.INPUT ? Mode.OUTPUT:Mode.INPUT;
			this.resetGuiLogic(toCheck, x, y);
			return;
		}
		this.logic.setRecipesPerPage(1);
		final List<RecipeLayout> layouts = this.logic.getRecipeWidgets(x, y, 0);
		this.layout = layouts.isEmpty() ? null:layouts.get(0);
	}

	private void getRecipesGui() throws Exception{
		if(RenderTickHandler.guiEventHandler == null){
			RenderTickHandler.guiEventHandler = ProxyCommonClient.class.getDeclaredField("guiEventHandler");
			RenderTickHandler.guiEventHandler.setAccessible(true);
		}
		if(RenderTickHandler.recipesGui == null){
			RenderTickHandler.recipesGui = GuiEventHandler.class.getDeclaredField("recipesGui");
			RenderTickHandler.recipesGui.setAccessible(true);
		}
		if(this.gui == null){
			final Object guiEventHandler = RenderTickHandler.guiEventHandler.get(JustEnoughItems.getProxy());
			this.gui = (RecipesGui) RenderTickHandler.recipesGui.get(guiEventHandler);
		}
	}

}
