package io.sandbox.sandbox_mobs.items;

import io.sandbox.sandbox_mobs.Main;
import io.sandbox.sandbox_mobs.entities.EntityLoader;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemLoader {
  public static final Item PIGLIN_OVERSEER_EGG = new SpawnEggItem(
    EntityLoader.PIGLIN_OVERSEER,
    0x948e8d,
    0x3b3635,
    new FabricItemSettings().group(ItemGroup.MISC).maxCount(64)
  );


  public static void init() {
    Registry.register(
      Registry.ITEM,
      new Identifier(Main.MOD_ID, "piglin_overseer_spawn_egg"),
      PIGLIN_OVERSEER_EGG
    );
  }
}
