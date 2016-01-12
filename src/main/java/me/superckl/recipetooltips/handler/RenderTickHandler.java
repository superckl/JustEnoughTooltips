package me.superckl.recipetooltips.handler;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.superckl.recipetooltips.util.LogHelper;
import me.superckl.recipetooltips.util.RenderHelper;
import mezz.jei.gui.Focus;
import mezz.jei.gui.Focus.Mode;
import mezz.jei.gui.IRecipeGuiLogic;
import mezz.jei.gui.RecipeGuiLogic;
import mezz.jei.gui.RecipeLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderTickHandler {

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final IRecipeGuiLogic logic = new RecipeGuiLogic();
	private ItemStack lastStack;
	private RecipeLayout layout;
	private boolean needsReset;

	@SubscribeEvent
	public void onRenderTick(final RenderGameOverlayEvent.Pre e){
		if(this.mc.currentScreen != null || e.type != ElementType.CROSSHAIRS || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
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
		final int x = Math.round(e.resolution.getScaledWidth()/2);
		final int y = e.resolution.getScaledHeight()/2+13;
		this.checkLastItem(toCheck, x, y);
		if(this.layout != null){
			final int width = this.logic.getRecipeCategory().getBackground().getWidth();
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			RenderHelper.outlineGuiArea(x-(width)/2, y, 500, width, this.logic.getRecipeCategory().getBackground().getHeight(), scale);
			GlStateManager.popMatrix();

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			GlStateManager.translate(-width/2F, 0, 501F);
			this.layout.draw(this.mc, 0, 0);
			GlStateManager.popMatrix();
		}
		this.lastStack = toCheck;
	}

	@SubscribeEvent
	public void onMouseInput(final MouseEvent e){
		if(this.layout == null || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
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
		if(this.layout == null || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderTooltip(final ItemTooltipEvent e){
		if(e.itemStack == null || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			return;
		final ScaledResolution resolution = new ScaledResolution(this.mc);
		final float scale = 1F;
		final int x = Math.round(Mouse.getEventX() / resolution.getScaleFactor()-8);
		final int y = (this.mc.displayHeight - Mouse.getEventY()) / resolution.getScaleFactor();
		this.checkLastItem(e.itemStack, x, y);
		if(this.layout != null){
			final int width = this.logic.getRecipeCategory().getBackground().getWidth();
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			RenderHelper.outlineGuiArea(x-(width), y, 500, width, this.logic.getRecipeCategory().getBackground().getHeight(), scale);
			GlStateManager.popMatrix();

			this.layout.getRecipeTransferButton().enabled = false;
			this.layout.getRecipeTransferButton().visible = false;
			GlStateManager.pushMatrix();
			GlStateManager.translate((x-this.layout.getPosX())-width, (y-this.layout.getPosY()), 501F);
			this.layout.draw(this.mc, 0, 0);
			GlStateManager.popMatrix();
		}
		this.lastStack = e.itemStack;
	}

	private void checkLastItem(final ItemStack toCheck, final int x, final int y){
		if(this.needsReset || (this.lastStack != null && !toCheck.isItemEqual(this.lastStack)) || this.lastStack == null)
			this.resetGuiLogic(toCheck, x, y);
	}

	private void resetGuiLogic(final ItemStack toCheck, final int x, final int y){
		this.needsReset = false;
		final Focus focus = new Focus(toCheck);
		focus.setMode(Mode.OUTPUT);
		if(!this.logic.setFocus(focus)){
			this.layout = null;
			return;
		}
		this.logic.setRecipesPerPage(1);
		LogHelper.info(this.logic.hasMultiplePages());
		final List<RecipeLayout> layouts = this.logic.getRecipeWidgets(x, y, 0);
		this.layout = layouts.isEmpty() ? null:layouts.get(0);
	}

}
