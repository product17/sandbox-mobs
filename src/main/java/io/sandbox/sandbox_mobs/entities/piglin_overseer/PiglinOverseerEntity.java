package io.sandbox.sandbox_mobs.entities.piglin_overseer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;

import io.sandbox.sandbox_mobs.brains.PiglinOverseerBrain;
import io.sandbox.sandbox_mobs.entities.IAnimationTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class PiglinOverseerEntity extends AbstractPiglinEntity implements IAnimatable, IAnimationTriggers {
  private AnimationFactory factory = new AnimationFactory(this);
  private static final TrackedData<Integer> MAIN_ATTACK_PROGRESS = DataTracker.registerData(
    PiglinOverseerEntity.class,
		TrackedDataHandlerRegistry.INTEGER
  );
  private static final TrackedData<Boolean> SPECIAL_ATTACK_ACTIVE = DataTracker.registerData(
    PiglinOverseerEntity.class,
		TrackedDataHandlerRegistry.BOOLEAN
  );
  private static final TrackedData<Integer> SPECIAL_ATTACK_PROGRESS = DataTracker.registerData(
    PiglinOverseerEntity.class,
		TrackedDataHandlerRegistry.INTEGER
  );
  public int mainAttackCooldown = 30;
  public int mainAttackFullAnimation = 20;
  public boolean getMainAttackHasSwung = false;
  public int mainAttackTicksUntilDamage = 10;
  public static int specialAttackTotalTicks = 30;
  public static int specialAttackCooldown = 30 * 20; // 30 seconds

  protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinOverseerEntity>>> SENSOR_TYPES = ImmutableList.of(
    SensorType.NEAREST_LIVING_ENTITIES,
    SensorType.NEAREST_PLAYERS,
    SensorType.NEAREST_ITEMS,
    SensorType.HURT_BY,
    SensorType.PIGLIN_BRUTE_SPECIFIC_SENSOR
  );
  protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULE_TYPES = ImmutableList.of(
    MemoryModuleType.LOOK_TARGET,
    MemoryModuleType.DOORS_TO_CLOSE,
    MemoryModuleType.MOBS,
    MemoryModuleType.VISIBLE_MOBS,
    MemoryModuleType.NEAREST_VISIBLE_PLAYER,
    MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
    MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
    MemoryModuleType.NEARBY_ADULT_PIGLINS,
    MemoryModuleType.HURT_BY,
    MemoryModuleType.HURT_BY_ENTITY,
    MemoryModuleType.WALK_TARGET,
    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
    new MemoryModuleType[]{
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.ATTACK_COOLING_DOWN,
      MemoryModuleType.INTERACTION_TARGET,
      MemoryModuleType.PATH,
      MemoryModuleType.ANGRY_AT,
      MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
      MemoryModuleType.HOME
    }
  );

  public PiglinOverseerEntity(EntityType<? extends PiglinOverseerEntity> entityType, World world) {
    super(entityType, world);
  }

  public static DefaultAttributeContainer.Builder createPiglinOverseerAttributes() {
    return HostileEntity.createHostileAttributes()
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
        .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1)
        .add(EntityAttributes.GENERIC_ATTACK_SPEED, 0.8)
        .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.9)
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f);
  }

  @Override
  protected void initDataTracker() {
    super.initDataTracker();
    // initialize to animation at it's finished state
    this.dataTracker.startTracking(MAIN_ATTACK_PROGRESS, mainAttackFullAnimation + 1);
    this.dataTracker.startTracking(SPECIAL_ATTACK_ACTIVE, false);
    this.dataTracker.startTracking(SPECIAL_ATTACK_PROGRESS, 0);
  }

  @Override
  @Nullable
  public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      PiglinOverseerBrain.setCurrentPosAsHome(this);
      this.initEquipment(world.getRandom(), difficulty);
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
  }
  

  @Override
  protected Brain.Profile<PiglinOverseerEntity> createBrainProfile() {
    return Brain.createProfile(MEMORY_MODULE_TYPES, SENSOR_TYPES);
  }

  public Brain<PiglinOverseerEntity> getBrain() {
    return (Brain<PiglinOverseerEntity>) super.getBrain();
  }

  @Override
  protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
      return PiglinOverseerBrain.create(this, this.createBrainProfile().deserialize(dynamic));
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    this.world.getProfiler().push("piglinOverseerBrain");
    this.getBrain().tick((ServerWorld)this.world, this);
    this.world.getProfiler().pop();
    PiglinOverseerBrain.tick(this);
    PiglinOverseerBrain.playSoundRandomly(this);

    // Increment the SpecialAttack (used to compare against cooldown)
    this.setSpecialAttackProgress(this.getSpecialAttackProgress() + 1);

    // Main Attack Animation progress
    int mainAttackProgress = this.getMainAttackProgress();
    mainAttackProgress++;
    this.setMainAttackProgress(mainAttackProgress);
    if (mainAttackProgress < mainAttackCooldown) {
      if (mainAttackProgress < mainAttackFullAnimation) {
        if (mainAttackProgress >= this.mainAttackTicksUntilDamage && !this.getMainAttackHasSwung()) {
          var optMemory = this.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
          if (optMemory.isPresent()) {
            LivingEntity target = optMemory.get();
            if (target != null && this.isInAttackRange(target)) {
              this.tryAttack(target);
              this.setMainAttackHasSwung(true);
            }
          }
        }
      }
    } else {
      this.setMainAttackHasSwung(false);
    }
  }

  @Override
  public boolean damage(DamageSource source, float amount) {
    boolean bl = super.damage(source, amount);
    if (this.world.isClient) {
      return false;
    }

    if (bl && source.getAttacker() instanceof LivingEntity) {
      PiglinOverseerBrain.tryRevenge(this, (LivingEntity)source.getAttacker());
    }

    return bl;
  }

  @Override
  public boolean shouldZombify() {
    return false;
  }

  private <E extends IAnimatable> PlayState attackPredicate(AnimationEvent<E> event) {  
    if (this.getMainAttackProgress() < this.mainAttackFullAnimation) {
      event.getController().setAnimation(
        new AnimationBuilder().addAnimation("animation.piglin_overseer.attack", true));
      return PlayState.CONTINUE;
    }

    if (this.getSpecialAttackProgress() < specialAttackTotalTicks) {
      event.getController().setAnimation(
        new AnimationBuilder().addAnimation("animation.piglin_overseer.shout", true));
      return PlayState.CONTINUE;
    }

    return PlayState.STOP;
  }

  private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
    if (event.isMoving()) {
      event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.piglin_overseer.walk", true));
      return PlayState.CONTINUE;
    }

    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.piglin_overseer.idle", true));
    return PlayState.CONTINUE;
  }

  @Override
  public void registerControllers(AnimationData animationData) {
    animationData.addAnimationController(
      new AnimationController<PiglinOverseerEntity>(
        this, "controller", 0, this::predicate));
    animationData.addAnimationController(
      new AnimationController<PiglinOverseerEntity>(
        this, "attackController", 0, this::attackPredicate));
  }

  public int getMainAttackCooldown() {
    return mainAttackCooldown;
  }

  public int getMainAttackProgress () {
    return this.dataTracker.get(MAIN_ATTACK_PROGRESS);
  }

  public void setMainAttackProgress (int tick) {
    this.dataTracker.set(MAIN_ATTACK_PROGRESS, tick);
  }

  public boolean getMainAttackHasSwung () {
    return this.getMainAttackHasSwung;
  }

  public void setMainAttackHasSwung (Boolean hasSwung) {
    this.getMainAttackHasSwung = hasSwung;
  }

  @Override
  public AnimationFactory getFactory() {
    return factory;
  }

  @Override
  protected boolean canHunt() {
    return false;
  }

  @Override
  public PiglinActivity getActivity() {
    if (this.isAttacking() && this.isHoldingTool()) {
      return PiglinActivity.ATTACKING_WITH_MELEE_WEAPON;
    }
    return PiglinActivity.DEFAULT;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_DEATH;
  }

  @Override
  protected void playStepSound(BlockPos pos, BlockState state) {
    this.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_STEP, 0.15f, 1.0f);
  }

  public void playAngrySound() {
    this.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_ANGRY, 1.0f, this.getSoundPitch());
  }

  @Override
  protected void playZombificationSound() {
    this.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED, 1.0f, this.getSoundPitch());
  }

  public boolean getSpecialAttackActive() {
    return this.dataTracker.get(SPECIAL_ATTACK_ACTIVE);
  }

  public void setSpecialAttackActive(Boolean isActive) {
    this.dataTracker.set(SPECIAL_ATTACK_ACTIVE, isActive);
  }

  public int getSpecialAttackCooldown() {
    return specialAttackCooldown;
  }

  public int getSpecialAttackProgress() {
    return this.dataTracker.get(SPECIAL_ATTACK_PROGRESS);
  }

  public void setSpecialAttackProgress(int tick) {
    this.dataTracker.set(SPECIAL_ATTACK_PROGRESS, tick);
  }
}
