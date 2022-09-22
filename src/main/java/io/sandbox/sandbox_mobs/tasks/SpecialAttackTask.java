package io.sandbox.sandbox_mobs.tasks;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import io.sandbox.sandbox_mobs.entities.IAnimationTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SpecialAttackTask
    extends Task<MobEntity> {
  private final int shoutSize;

  public SpecialAttackTask(int shoutSize) {
    super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT));
    this.shoutSize = shoutSize;
  }

  @Override
  protected boolean shouldRun(ServerWorld serverWorld, MobEntity mobEntity) {
    LivingEntity livingEntity = this.getAttackTarget(mobEntity);
    return LookTargetUtil.isVisibleInMemory(mobEntity, livingEntity) &&
      ((IAnimationTriggers) mobEntity).getSpecialAttackProgress() > ((IAnimationTriggers) mobEntity).getSpecialAttackCooldown() &&
      (mobEntity.getMaxHealth() / mobEntity.getHealth()) >= 2;
  }

  @Override
  protected void run(ServerWorld serverWorld, MobEntity mobEntity, long l) {
    LivingEntity livingEntity = this.getAttackTarget(mobEntity);
    LookTargetUtil.lookAt(mobEntity, livingEntity);

    BlockPos pos = mobEntity.getBlockPos();
    Box roomBoxt = new Box(
      pos.getX() - shoutSize,
      pos.getY() - shoutSize,
      pos.getZ() - shoutSize,
      pos.getX() + shoutSize,
      pos.getY() + shoutSize,
      pos.getZ() + shoutSize
    );

    List<AbstractPiglinEntity> nearbyPiglins = serverWorld.getEntitiesByType(
      TypeFilter.instanceOf(AbstractPiglinEntity.class),
      roomBoxt,
      EntityPredicates.VALID_ENTITY
    );

    for (AbstractPiglinEntity piglin : nearbyPiglins) {
      piglin.setStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 10 * 20, 1), mobEntity);
    }

    // The main mob gets the buff for a bit longer
    mobEntity.setStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 15 * 20, 1), mobEntity);

    ((IAnimationTriggers) mobEntity).setSpecialAttackProgress(0);
  }

  private LivingEntity getAttackTarget(MobEntity entity) {
    return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
  }

}
