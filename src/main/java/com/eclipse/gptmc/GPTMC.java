package com.eclipse.gptmc;

import com.eclipse.gptmc.timers.GPTMCLightningTimer;
import com.eclipse.gptmc.timers.GPTMCTimer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.eclipse.gptmc.ModCommands.registerCommands;
import static com.eclipse.gptmc.ModEventListeners.registerEventListeners;

public class GPTMC implements ModInitializer {
	public static final String MOD_ID = "gpt-mc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		registerCommands();
		registerEventListeners();
		ConfigHandler.loadConfig();
		GPTMCTimer.register();
		GPTMCLightningTimer.register();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			GPTMC.server = server;
		});
	}
}