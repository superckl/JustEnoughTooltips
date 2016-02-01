package me.superckl.jet.integration;

import jeresources.api.render.IScissorHook;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.superckl.jet.util.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;


@Accessors(chain = true)
@Setter
public class JERScissorHook implements IScissorHook{

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private int x, y, width, height;
	private ScaledResolution resolution;
	private float scale;
	private boolean apply;

	@Override
	public ScissorInfo transformScissor(final ScissorInfo scissorInfo) {
		if(!this.apply)
			return scissorInfo;
		LogHelper.info("Old Scissor: "+scissorInfo.x+":"+scissorInfo.y);
		scissorInfo.height *= this.scale;
		scissorInfo.width *= this.scale;
		final int guiLeft = Math.round((this.resolution.getScaledWidth() - this.width)/2F);
		final int guiTop = Math.round((this.resolution.getScaledHeight() - this.height)/2F);
		scissorInfo.x += (this.x - guiLeft)*this.resolution.getScaleFactor();
		scissorInfo.y -= (this.y - guiTop)*this.resolution.getScaleFactor();
		LogHelper.info("New Scissor: "+scissorInfo.x+":"+scissorInfo.y);
		return scissorInfo;
	}

}
