package io.sandbox.sandbox_mobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.sandbox.sandbox_mobs.entities.EntityLoader;
import io.sandbox.sandbox_mobs.items.ItemLoader;
import net.fabricmc.api.ModInitializer;
import software.bernie.geckolib3.GeckoLib;

public class Main implements ModInitializer {
  static public String MOD_ID = "sandbox_mobs";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
    EntityLoader.init();
    ItemLoader.init();
    GeckoLib.initialize();
    // FabricDefaultAttributeRegistry.register(EntityLoader.PIGLIN_OVERSEER, PiglinOverseer.setAttributes());

		LOGGER.info("Loaded: " + MOD_ID);
	}
}
