package me.superckl.jet.handler;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.superckl.jet.Config;
import me.superckl.jet.KeyBindings;
import me.superckl.jet.integration.JEIIntegrationModule;
import me.superckl.jet.util.LogHelper;
import me.superckl.jet.util.RecipeDrawingException;
import me.superckl.jet.util.RenderHelper;
import mezz.jei.RecipeRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.IRecipeGuiLogic;
import mezz.jei.gui.RecipeGuiLogic;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
	private IRecipeGuiLogic logic;
	private Mode mode = Mode.OUTPUT;
	private ItemStack lastStack;
	private RecipeLayout layout;
	private boolean needsReset;
	private IRecipeTransferError error;

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(!Config.renderInGame || this.mc.currentScreen != null || e.getType() != ElementType.CROSSHAIRS || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
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
		this.checkLastItem(toCheck, x, y);
		if(this.layout != null){
			int width = this.logic.getRecipeCategory().getBackground().getWidth();
			final int height = this.logic.getRecipeCategory().getBackground().getHeight();
			if(this.layout.getRecipeWrapper() instanceof SmeltingRecipe)
				width += 8;
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

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			GlStateManager.scale(scale, scale, 1F);
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			GlStateManager.translate(x/scale-this.layout.getPosX(), y/scale-this.layout.getPosY(), 501F);
			//final JERScissorHook hook = JustEnoughTooltips.instance.getScissorHook();
			//hook.setScale(scale).setX(x).setY(y).setResolution(e.getResolution()).setHeight(height).setWidth(width).setApply(true);
			try{
				//Fake mouse parameters, middle of recipe layout
				this.layout.draw(this.mc, Math.round(this.layout.getPosX()+width/2), Math.round(this.layout.getPosY()+height/2));
			}catch(final Exception e1){
				throw new RecipeDrawingException("An error ocurred while drawing a recipe with category: "+this.layout.getRecipeCategory().getTitle(), e1);
			}
			//hook.setApply(false);
			GlStateManager.popMatrix();
		}
		this.lastStack = toCheck;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.layout == null || !KeyBindings.DISPLAY_1.isKeyDown())
			return;
		if(this.logic == null)
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		if(e.getDwheel() != 0){
			e.setCanceled(true);
			if(e.getDwheel() > 0){
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
		if(this.logic == null)
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
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
		if(this.logic == null)
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		final int key = Keyboard.getEventKey();
		if(KeyBindings.NEXT_CATEGORY.getKeyCode() == key){
			if(this.logic.hasMultipleCategories()){
				this.logic.nextRecipeCategory();
				this.needsReset = true;
			}
		}else if(KeyBindings.SWITCH_USES_RECIPES.getKeyCode() == key){
			this.mode = this.mode == Mode.OUTPUT ? Mode.INPUT:Mode.OUTPUT;
			this.needsReset = true;
		}else if(this.mc.currentScreen == null && mezz.jei.config.KeyBindings.showRecipe.getKeyCode() == key)
			try {
				this.getRecipesGui();
				if(this.lastStack != null){
					this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
					if(this.mode == Mode.OUTPUT)
						this.getRecipesGui().showRecipes(this.lastStack);
					else
						this.getRecipesGui().showUses(this.lastStack);
				}
			} catch (final Exception e1) {
				LogHelper.error("An error occurred when opening the recipes gui!");
				e1.printStackTrace();
			}
		else if(this.mc.thePlayer != null && this.mc.thePlayer.openContainer != null && this.error == null && KeyBindings.FILL_RECIPE.getKeyCode() == key)
			RecipeTransferUtil.transferRecipe(this.mc.thePlayer.openContainer, this.layout, this.mc.thePlayer, GuiScreen.isShiftKeyDown());
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
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final float scale = Config.scaleInTooltip;
		final int mouseX = Math.round(Mouse.getEventX() / resolution.getScaleFactor());
		final int mouseY = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();
		int x = mouseX-8;
		int y = mouseY;
		this.checkLastItem(e.getItemStack(), x, y);
		if(this.layout != null){
			int width = this.logic.getRecipeCategory().getBackground().getWidth();
			final int height = this.logic.getRecipeCategory().getBackground().getHeight();
			if(this.layout.getRecipeWrapper() instanceof SmeltingRecipe)
				width += 8;
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

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			GlStateManager.scale(scale, scale, 1F);
			//Translate to move the draw to the right spot. The x and y passed on creation of the layouts may not be accurate (resizing, position overrides, etc.)
			final float xDiff = x/scale-this.layout.getPosX();
			final float yDiff = y/scale-this.layout.getPosY();
			GlStateManager.translate(xDiff, yDiff, 501F);
			//final JERScissorHook hook = JustEnoughTooltips.instance.getScissorHook();
			//hook.setScale(scale).setX(x).setY(y).setResolution(resolution).setHeight(height).setWidth(width).setApply(true);
			try{
				this.layout.draw(this.mc, Math.round(mouseX-xDiff), Math.round(mouseY - yDiff));
				if(this.mc.thePlayer.openContainer != null && this.error != null && Keyboard.isKeyDown(KeyBindings.FILL_RECIPE.getKeyCode()))
					this.error.showError(this.mc, Math.round(x/scale+this.layout.getPosX()-x/scale-12),Math.round(y/scale+this.layout.getPosY()-y/scale-5), this.layout, x , y);
			}catch(final Exception e1){
				throw new RecipeDrawingException("An error ocurred while drawing a recipe with category: "+this.layout.getRecipeCategory().getTitle(), e1);
			}
			//hook.setApply(false);
			GlStateManager.popMatrix();
		}
		this.lastStack = e.getItemStack();
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
		if(this.logic == null)
			this.logic = new RecipeGuiLogic((RecipeRegistry) JEIIntegrationModule.jeiRuntime.getRecipeRegistry());
		this.needsReset = false;
		final IFocus<ItemStack> focus = JEIIntegrationModule.jeiRuntime.getRecipeRegistry().createFocus(this.mode, toCheck);
		if(!this.logic.setFocus(focus)){
			this.layout = null;
			this.error = null;
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
		if(this.layout != null && this.mc.thePlayer.openContainer != null)
			this.error = RecipeTransferUtil.getTransferRecipeError(this.mc.thePlayer.openContainer, this.layout, this.mc.thePlayer);
		else if(this.layout == null)
			this.error = null;
	}

	private IRecipesGui getRecipesGui(){
		return JEIIntegrationModule.jeiRuntime == null ? null:JEIIntegrationModule.jeiRuntime.getRecipesGui();
	}

}
