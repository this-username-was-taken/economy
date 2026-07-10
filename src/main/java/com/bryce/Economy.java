package com.bryce;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Isometric Camera Mod Initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}