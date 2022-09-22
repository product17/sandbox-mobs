package io.sandbox.sandbox_mobs.entities;

import io.sandbox.sandbox_mobs.Main;
import io.sandbox.sandbox_mobs.entities.piglin_overseer.PiglinOverseerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntityLoader {
  public static final EntityType<PiglinOverseerEntity> PIGLIN_OVERSEER = Registry.register(
    Registry.ENTITY_TYPE,
    new Identifier(Main.MOD_ID, "piglin_overseer"),
    FabricEntityTypeBuilder.create(
      SpawnGroup.MONSTER,
      PiglinOverseerEntity::new
    ).dimensions(
      EntityDimensions.fixed(1f, 2.6f)
    ).build()
  );

  public static void init() {
    FabricDefaultAttributeRegistry.register(PIGLIN_OVERSEER, PiglinOverseerEntity.createPiglinOverseerAttributes());
  }
}
