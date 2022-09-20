package io.sandbox.sandbox_mobs.tasks;

import com.google.common.collect.ImmutableMap;

import io.sandbox.sandbox_mobs.entities.IAnimationTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class MainMeleeAttackTask
extends Task<MobEntity> {
    private final int interval;

    public MainMeleeAttackTask(int interval) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT));
      this.interval = interval;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, MobEntity mobEntity) {
        LivingEntity livingEntity = this.getAttackTarget(mobEntity);
        return !this.isHoldingUsableRangedWeapon(mobEntity) && LookTargetUtil.isVisibleInMemory(mobEntity, livingEntity) && mobEntity.isInAttackRange(livingEntity);
    }

    private boolean isHoldingUsableRangedWeapon(MobEntity entity) {
        return entity.isHolding(stack -> {
            Item item = stack.getItem();
            return item instanceof RangedWeaponItem && entity.canUseRangedWeapon((RangedWeaponItem)item);
        });
    }

    @Override
    protected void run(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mobEntity);
        LookTargetUtil.lookAt(mobEntity, livingEntity);
        mobEntity.swingHand(Hand.MAIN_HAND);

        // get the Entity's startAnimation wait until dealing damage
        // int mainAttackStartAnimationTicks = ((IAnimationTriggers)mobEntity).getMainAttackProgress();

        // Start the timer until damage
        ((IAnimationTriggers)mobEntity).setMainAttackProgress(0);
        ((IAnimationTriggers)mobEntity).setMainAttackHasSwung(false);

        System.out.println("Speed: " + mobEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        // System.out.println("Attack Speed: " + ((int)(20 / mobEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)) + mainAttackStartAnimationTicks));

        // Trigger the Cooldown
        mobEntity.getBrain().remember(
            MemoryModuleType.ATTACK_COOLING_DOWN,
            true,
            interval
        );
    }

    private LivingEntity getAttackTarget(MobEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}
