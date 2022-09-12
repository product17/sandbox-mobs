package io.sandbox.sandbox_mobs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.sandbox.sandbox_mobs.entities.IAnimationTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

@Mixin(MeleeAttackTask.class)
public class MeleeAttackTaskMixin {
  @Inject(method = "run", at = @At("HEAD"), cancellable = true)
  protected void run(ServerWorld serverWorld, MobEntity mobEntity, long l, CallbackInfo cb) {
    if (mobEntity instanceof IAnimationTriggers) {
      System.out.println("Is IAnimationTriggers");
      LivingEntity livingEntity = mobEntity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
      LookTargetUtil.lookAt(mobEntity, livingEntity);
      mobEntity.swingHand(Hand.MAIN_HAND);

      // get the Entity's startAnimation wait until dealing damage
      int mainAttackStartAnimationTicks = ((IAnimationTriggers)mobEntity).getMainAttackStartAnimationTicks();

      // Start the timer until damage
      ((IAnimationTriggers)mobEntity).setMainAttackTicksUntilDamage(mainAttackStartAnimationTicks);

      // Trigger the Cooldown
      mobEntity.getBrain().remember(
        MemoryModuleType.ATTACK_COOLING_DOWN,
        true,
        (int)(20 / mobEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED)) + mainAttackStartAnimationTicks
      );

      // Cancel to prevent default behavior
      cb.cancel();
    } else {
      System.out.println("Not IAnimationTriggers");
    }
  }
}
