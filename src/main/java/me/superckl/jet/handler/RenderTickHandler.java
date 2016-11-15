package me.superckl.jet.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.superckl.jet.Config;
import me.superckl.jet.KeyBindings;
import me.superckl.jet.RecipeGuiLogic;
import me.superckl.jet.integration.JEIIntegrationModule;
import me.superckl.jet.util.LogHelper;
import me.superckl.jet.util.RecipeDrawingException;
import me.superckl.jet.util.RenderHelper;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
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

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private RecipeGuiLogic logic;
	private ItemStack lastStack;
	private IRecipeTransferError error;

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(!Config.renderInGame || this.mc.currentScreen != null || e.getType() != ElementType.CROSSHAIRS || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic(JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		ItemStack toCheck = null;
		if(!this.mc.thePlayer.isSneaking() && this.mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND) != null)
			toCheck = this.mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND);
		else{
			final RayTraceResult pos = this.mc.getRenderViewEntity().rayTrace(this.mc.playerController.getBlockReachDistance(), 1.0F);
			if(pos.typeOfHit == RayTraceResult.Type.BLOCK){
				final IBlockState block = this.mc.theWorld.getBlockState(pos.getBlockPos());
				if(block != null)
					toCheck = block.getBlock().getPickBlock(block, pos, this.mc.theWorld, pos.getBlockPos(), this.mc.thePlayer);
			}
		}
		if(toCheck == null)
			return;
		final float scale = Config.scaleInGame;
		int x = Math.round(e.getResolution().getScaledWidth()/2);
		int y = e.getResolution().getScaledHeight()/2+13;
		this.checkLastItem(toCheck);
		if(this.logic.getCurrentLayout() != null){
			final int width = this.logic.getSelectedRecipeCategory().getBackground().getWidth();
			final int height = this.logic.getSelectedRecipeCategory().getBackground().getHeight();
			/*if(this.layout.getClass() instanceof SmeltingRecipe)
				width += 8;*/ //TODO broken now
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			x-= width/2*scale;
			if(Config.xPosInGame >= 0)
				x = Config.xPosInGame;
			if(Config.yPosInGame >= 0)
				y = Config.yPosInGame;
			x += Config.xPaddingInGame;
			y += Config.yPaddingInGame;
			if(y < 12)
				y = 12;
			else if(y + height*scale + 12*scale > e.getResolution().getScaledHeight())
				y = Math.round(e.getResolution().getScaledHeight()-(height*scale+12*scale));
			if(x < 12)
				x = 12;
			else if(x + width*scale + 12*scale > e.getResolution().getScaledWidth())
				x = Math.round(e.getResolution().getScaledWidth()-(width*scale+12*scale));
			RenderHelper.outlineGuiArea(x, y, 500, width, height, scale);
			RenderHelper.fillGuiArea(x, y, 500, width, height, scale);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.scale(scale, scale, 1F);
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			GlStateManager.translate(x/scale/*-this.layout.getPosX()*/, y/scale/*-this.layout.getPosY()*/, 501F);
			//final JERScissorHook hook = JustEnoughTooltips.instance.getScissorHook();
			//hook.setScale(scale).setX(x).setY(y).setResolution(e.getResolution()).setHeight(height).setWidth(width).setApply(true);
			try{
				//Fake mouse parameters, middle of recipe layout
				this.logic.getCurrentLayout().draw(this.mc, Math.round(/*this.layout.getPosX()+*/width/2), Math.round(/*this.layout.getPosY()+*/height/2));
			}catch(final Exception e1){
				throw new RecipeDrawingException("An error ocurred while drawing a recipe with category: "+this.logic.getSelectedRecipeCategory().getTitle(), e1);
			}
			//hook.setApply(false);
			GlStateManager.popMatrix();
		}
		this.lastStack = toCheck;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.logic == null || this.logic.getCurrentLayout() == null || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic(JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		if(e.getDwheel() != 0){
			e.setCanceled(true);
			if(e.getDwheel() > 0)
				this.logic.nextPage();
			else
				this.logic.previousPage();
		}
	}

	@SubscribeEvent
	public void onMouseInput2(final MouseInputEvent e){
		if(this.logic == null || this.logic.getCurrentLayout() == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic(JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		if(Mouse.getEventDWheel() != 0){
			e.setCanceled(true);
			if(Mouse.getEventDWheel() > 0)
				this.logic.nextPage();
			else
				this.logic.previousPage();
		}
	}

	@SubscribeEvent//not fired when in GUI
	public void onKeyPress(final KeyInputEvent e){
		if(!Keyboard.getEventKeyState())
			return;
		if(this.logic == null || this.logic.getCurrentLayout() == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic(JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		final int key = Keyboard.getEventKey();
		if(KeyBindings.NEXT_CATEGORY.getKeyCode() == key){
			if(this.logic.hasMultipleCategories())
				this.logic.nextRecipeCategory();
		}else if(KeyBindings.SWITCH_USES_RECIPES.getKeyCode() == key)
			this.logic.nextMode();
		else if(this.mc.currentScreen == null && mezz.jei.config.KeyBindings.showRecipe.getKeyCode() == key)
			try {
				this.getRecipesGui();
				if(this.lastStack != null){
					this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
					this.getRecipesGui().show(this.logic.getFocus());
				}
			} catch (final Exception e1) {
				LogHelper.error("An error occurred when opening the recipes gui!");
				e1.printStackTrace();
			}
		/*else if(this.mc.thePlayer != null && this.mc.thePlayer.openContainer != null && this.error == null && KeyBindings.FILL_RECIPE.getKeyCode() == key)
			RecipeTransferUtil.transferRecipe(this.mc.thePlayer.openContainer, this.layout, this.mc.thePlayer, GuiScreen.isShiftKeyDown());*/
		//TODO Removed transfer functionality
	}

	@SubscribeEvent //fired while in GUI, but isPressed returns false
	public void onKeyPress2(final KeyboardInputEvent.Pre e){
		this.onKeyPress(new KeyInputEvent());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderTooltip(final ItemTooltipEvent e){
		if(!Config.renderInTooltips || e.getItemStack() == null || !Keyboard.isKeyDown(KeyBindings.DISPLAY_1.getKeyCode()))
			return;
		if(this.mc.currentScreen == this.getRecipesGui())
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic(JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final float scale = Config.scaleInTooltip;
		final int mouseX = Math.round(Mouse.getEventX() / resolution.getScaleFactor());
		final int mouseY = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();
		int x = mouseX-8;
		int y = mouseY;
		this.checkLastItem(e.getItemStack());
		if(this.logic.getCurrentLayout() != null){
			final int width = this.logic.getSelectedRecipeCategory().getBackground().getWidth();
			final int height = this.logic.getSelectedRecipeCategory().getBackground().getHeight();
			/*if(this.layout.getRecipeWrapper() instanceof SmeltingRecipe)
				width += 8;*/ //TODO broken now
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			x -= width*scale;
			if(Config.xPosInTooltip >= 0)
				x = Config.xPosInTooltip;
			if(Config.yPosInTooltip >= 0)
				y = Config.yPosInTooltip;
			x += Config.xPaddingInTooltip;
			y += Config.yPaddingInTooltip;
			if(y < 12)
				y = 12;
			else if(y + height*scale + 12*scale > resolution.getScaledHeight())
				y = Math.round(resolution.getScaledHeight()-(height*scale+12*scale));
			if(x < 12)
				x = 12;
			else if(x + width*scale + 12*scale > resolution.getScaledWidth())
				x = Math.round(resolution.getScaledWidth()-(width*scale+12*scale));
			RenderHelper.outlineGuiArea(x, y, 500, width, height, scale);
			RenderHelper.fillGuiArea(x, y, 500, width, height, scale);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.scale(scale, scale, 1F);
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			final float xDiff = x/scale/*-this.layout.getPosX()*/;
			final float yDiff = y/scale/*-this.layout.getPosY()*/;
			GlStateManager.translate(xDiff, yDiff, 501F);
			//final JERScissorHook hook = JustEnoughTooltips.instance.getScissorHook();
			//hook.setScale(scale).setX(x).setY(y).setResolution(resolution).setHeight(height).setWidth(width).setApply(true);
			try{
				this.logic.getCurrentLayout().draw(this.mc, Math.round(mouseX-xDiff), Math.round(mouseY - yDiff));
				if(this.mc.thePlayer.openContainer != null && this.error != null && Keyboard.isKeyDown(KeyBindings.FILL_RECIPE.getKeyCode()))
					this.error.showError(this.mc, Math.round(x/scale+/*this.layout.getPosX()-x/scale*/-12),Math.round(y/scale/*+this.layout.getPosY()-y/scale*/-5), this.logic.getCurrentLayout(), x , y);
			}catch(final Exception e1){
				throw new RecipeDrawingException("An error ocurred while drawing a recipe with category: "+this.logic.getSelectedRecipeCategory().getTitle(), e1);
			}
			//hook.setApply(false);
			GlStateManager.popMatrix();
		}
		this.lastStack = e.getItemStack();
	}

	private void checkLastItem(final ItemStack toCheck){
		final boolean changedStacks = (this.lastStack == null && toCheck != null) || (this.lastStack != null && !toCheck.isItemEqual(this.lastStack));
		if(changedStacks)
			this.logic.newFocusItem(toCheck);
	}

	private Mode tempMode;

	private IRecipesGui getRecipesGui(){
		return JEIIntegrationModule.jeiRuntime == null ? null:JEIIntegrationModule.jeiRuntime.getRecipesGui();
	}

}
