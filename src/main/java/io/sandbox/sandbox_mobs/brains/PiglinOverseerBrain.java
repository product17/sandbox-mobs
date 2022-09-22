package io.sandbox.sandbox_mobs.brains;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import io.sandbox.sandbox_mobs.entities.piglin_overseer.PiglinOverseerEntity;
import io.sandbox.sandbox_mobs.tasks.MainMeleeAttackTask;
import io.sandbox.sandbox_mobs.tasks.SpecialAttackTask;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.FindEntityTask;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.entity.ai.brain.task.ForgetAngryAtTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GoToIfNearbyTask;
import net.minecraft.entity.ai.brain.task.GoToNearbyPositionTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameRules;

public class PiglinOverseerBrain {
  public static Brain<?> create(PiglinOverseerEntity piglinBrute, Brain<PiglinOverseerEntity> brain) {
    PiglinOverseerBrain.addCoreActivities(piglinBrute, brain);
    PiglinOverseerBrain.addIdleActivities(piglinBrute, brain);
    PiglinOverseerBrain.addFightActivities(piglinBrute, brain);
    brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
    brain.setDefaultActivity(Activity.IDLE);
    brain.resetPossibleActivities();
    return brain;
  }

  public static void setCurrentPosAsHome(PiglinOverseerEntity piglinBrute) {
    GlobalPos globalPos = GlobalPos.create(piglinBrute.world.getRegistryKey(), piglinBrute.getBlockPos());
    piglinBrute.getBrain().remember(MemoryModuleType.HOME, globalPos);
  }

  private static void addCoreActivities(PiglinOverseerEntity piglinBrute, Brain<PiglinOverseerEntity> brain) {
    brain.setTaskList(Activity.CORE, 0, (ImmutableList<? extends Task<? super PiglinOverseerEntity>>) ImmutableList
        .of(new LookAroundTask(45, 90), new WanderAroundTask(), new OpenDoorsTask(), new ForgetAngryAtTargetTask()));
  }

  private static void addIdleActivities(PiglinOverseerEntity piglinBrute, Brain<PiglinOverseerEntity> brain) {
    brain.setTaskList(Activity.IDLE, 10,
        ImmutableList.of(new UpdateAttackTargetTask<PiglinOverseerEntity>(PiglinOverseerBrain::getTarget),
            PiglinOverseerBrain.getFollowTasks(), PiglinOverseerBrain.getIdleTasks(),
            new FindInteractionTargetTask(EntityType.PLAYER, 4)));
  }

  private static void addFightActivities(PiglinOverseerEntity piglinBrute, Brain<PiglinOverseerEntity> brain) {
    brain.setTaskList(Activity.FIGHT, 10,
        (ImmutableList<? extends Task<? super PiglinOverseerEntity>>) ImmutableList.of(
            new ForgetAttackTargetTask(entity -> !PiglinOverseerBrain.isTarget(piglinBrute, entity)),
            new RangedApproachTask(1.0f),
            new MainMeleeAttackTask(40),
            new SpecialAttackTask(12)
        ),
        MemoryModuleType.ATTACK_TARGET);
  }

  private static RandomTask<PiglinOverseerEntity> getFollowTasks() {
    return new RandomTask(ImmutableList.of(Pair.of(new FollowMobTask(EntityType.PLAYER, 8.0f), 1),
        Pair.of(new FollowMobTask(EntityType.PIGLIN, 8.0f), 1),
        Pair.of(new FollowMobTask(EntityType.PIGLIN_BRUTE, 8.0f), 1), Pair.of(new FollowMobTask(8.0f), 1),
        Pair.of(new WaitTask(30, 60), 1)));
  }

  private static RandomTask<PiglinOverseerEntity> getIdleTasks() {
    return new RandomTask(ImmutableList.of(Pair.of(new StrollTask(0.6f), 2),
        Pair.of(FindEntityTask.create(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2),
        Pair.of(FindEntityTask.create(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), 2),
        Pair.of(new GoToNearbyPositionTask(MemoryModuleType.HOME, 0.6f, 2, 100), 2),
        Pair.of(new GoToIfNearbyTask(MemoryModuleType.HOME, 0.6f, 5), 2), Pair.of(new WaitTask(30, 60), 1)));
  }

  public static void tick(PiglinOverseerEntity piglinOverseer) {
    Brain<PiglinOverseerEntity> brain = piglinOverseer.getBrain();
    Activity activity = brain.getFirstPossibleNonCoreActivity().orElse(null);
    brain.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    Activity activity2 = brain.getFirstPossibleNonCoreActivity().orElse(null);
    if (activity != activity2) {
      PiglinOverseerBrain.playSoundIfAngry(piglinOverseer);
    }
    piglinOverseer.setAttacking(brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
  }

  private static boolean isTarget(AbstractPiglinEntity piglin, Object entity) {
    return PiglinOverseerBrain.getTarget(piglin).filter(target -> target == entity).isPresent();
  }

  private static Optional<? extends LivingEntity> getTarget(AbstractPiglinEntity piglin) {
    Optional<LivingEntity> optional = LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
    if (optional.isPresent() && Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, optional.get())) {
      return optional;
    }
    Optional<? extends LivingEntity> optional2 = PiglinOverseerBrain.getTargetIfInRange(piglin,
        MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    if (optional2.isPresent()) {
      return optional2;
    }
    return piglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
  }

  private static Optional<? extends LivingEntity> getTargetIfInRange(AbstractPiglinEntity piglin,
      MemoryModuleType<? extends LivingEntity> targetMemoryModule) {
    return piglin.getBrain().getOptionalMemory(targetMemoryModule).filter(target -> target.isInRange(piglin, 12.0));
  }

  /**
   * @param piglinBrute
   * @param target
   */
  public static void tryRevenge(AbstractPiglinEntity piglin, LivingEntity target) {
    if (target instanceof AbstractPiglinEntity) {
      return;
    }
    if (LookTargetUtil.isNewTargetTooFar(piglin, target, 4.0)) {
      return;
    }
    if (target.getType() == EntityType.PLAYER && piglin.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
      PiglinOverseerBrain.becomeAngryWithPlayer(piglin, target);
      PiglinOverseerBrain.angerNearbyPiglins(piglin);
    } else {
      PiglinOverseerBrain.becomeAngryWith(piglin, target);
      PiglinOverseerBrain.angerAtCloserTargets(piglin, target);
    }
  }

  protected static void angerAtCloserTargets(AbstractPiglinEntity piglin, LivingEntity target) {
    PiglinOverseerBrain.getNearbyPiglins(piglin).forEach(nearbyPiglin -> {
      PiglinOverseerBrain.angerAtIfCloser(nearbyPiglin, target);
    });
  }

  private static void angerAtIfCloser(AbstractPiglinEntity piglin, LivingEntity target) {
    Optional<LivingEntity> optional = PiglinOverseerBrain.getAngryAt(piglin);
    LivingEntity livingEntity = LookTargetUtil.getCloserEntity((LivingEntity) piglin, optional, target);
    if (optional.isPresent() && optional.get() == livingEntity) {
      return;
    }
    PiglinOverseerBrain.becomeAngryWith(piglin, livingEntity);
  }

  private static Optional<LivingEntity> getAngryAt(AbstractPiglinEntity piglin) {
    return LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
  }

  protected static void angerNearbyPiglins(AbstractPiglinEntity piglin) {
    PiglinOverseerBrain.getNearbyPiglins(piglin)
        .forEach(nearbyPiglin -> PiglinBrain.getNearestDetectedPlayer(nearbyPiglin)
            .ifPresent(player -> PiglinOverseerBrain.becomeAngryWith(nearbyPiglin, player)));
  }

  private static List<AbstractPiglinEntity> getNearbyPiglins(AbstractPiglinEntity piglin) {
    return piglin.getBrain().getOptionalMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
  }

  private static void becomeAngryWithPlayer(AbstractPiglinEntity piglin, LivingEntity player) {
    Optional<PlayerEntity> optional = PiglinBrain.getNearestDetectedPlayer(piglin);
    if (optional.isPresent()) {
      PiglinOverseerBrain.becomeAngryWith(piglin, optional.get());
    } else {
      PiglinOverseerBrain.becomeAngryWith(piglin, player);
    }
  }

  protected static void becomeAngryWith(AbstractPiglinEntity piglin, LivingEntity target) {
    // if (!Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)) {
    // return;
    // }
    piglin.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    piglin.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
    if (target.getType() == EntityType.PLAYER && piglin.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
      piglin.getBrain().remember(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
    }
  }

  protected static void setTarget(PiglinOverseerEntity piglinOverseer, LivingEntity target) {
    piglinOverseer.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    piglinOverseer.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
  }

  public static void playSoundRandomly(PiglinOverseerEntity piglinOverseer) {
    if ((double) piglinOverseer.world.random.nextFloat() < 0.0125) {
      PiglinOverseerBrain.playSoundIfAngry(piglinOverseer);
    }
  }

  public static void playSoundIfAngry(PiglinOverseerEntity piglinOverseer) {
    piglinOverseer.getBrain().getFirstPossibleNonCoreActivity().ifPresent(activity -> {
      if (activity == Activity.FIGHT) {
        piglinOverseer.playAngrySound();
      }
    });
  }
}
