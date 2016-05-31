package me.superckl.jet.integration;

import lombok.Getter;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@Getter
@JEIPlugin
public class JEIIntegrationModule implements IModPlugin{

	public static IJeiRuntime jeiRuntime = null;

	@Override
	public void register(final IModRegistry registry) {}

	@Override
	public void onRuntimeAvailable(final IJeiRuntime jeiRuntime) {
		JEIIntegrationModule.jeiRuntime = jeiRuntime;
	}

}
