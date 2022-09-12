package io.sandbox.sandbox_mobs;

import io.sandbox.sandbox_mobs.entities.EntityLoaderClient;
import net.fabricmc.api.ClientModInitializer;

public class MainClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    EntityLoaderClient.init();
  }
}
