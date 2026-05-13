package net.redct.client;

import net.fabricmc.api.ClientModInitializer;

import net.redct.client.config.ConfigManager;
import net.redct.client.gui.hud.HudManager;
import net.redct.client.module.ModuleManager;
import net.redct.client.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.redct.client.utils.Logger.log;

public class RedUtilsClient implements ClientModInitializer {

	public static final String MOD_ID = "red-utils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Red Utils");
		log("INFO","Initializing Red Utils");
		ModuleManager.init();
		//HudManager.init();
		ConfigManager.load();
		EventSubscriber.registerToEvents();
		ModCommands.register();
	}
}