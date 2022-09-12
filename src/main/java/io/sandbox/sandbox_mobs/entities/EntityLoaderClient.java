package io.sandbox.sandbox_mobs.entities;

import io.sandbox.sandbox_mobs.entities.piglin_overseer.PiglinOverseerRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class EntityLoaderClient {
  public static void init() {
    EntityRendererRegistry.register(EntityLoader.PIGLIN_OVERSEER, PiglinOverseerRenderer::new);
  }
}
