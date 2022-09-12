package io.sandbox.sandbox_mobs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;

@Mixin(AttackGoal.class)
public class AttackGoalMixin {
  @Shadow private MobEntity mob;
  @Shadow private LivingEntity target;
  @Shadow private int cooldown;

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  public void tickOverride(CallbackInfo cb){
    this.mob.getLookControl().lookAt(this.target, 30.0f, 30.0f);
    double d = this.mob.getWidth() * 2.0f * (this.mob.getWidth() * 2.0f);
    double e = this.mob.squaredDistanceTo(this.target.getX(), this.target.getY(), this.target.getZ());
    double f = 0.8;
    if (e > d && e < 16.0) {
        f = 1.33;
    } else if (e < 225.0) {
        f = 0.6;
    }
    this.mob.getNavigation().startMovingTo(this.target, f);
    this.cooldown = Math.max(this.cooldown - 1, 0);
    if (e > d) {
        return;
    }
    if (this.cooldown > 0) {
        return;
    }
    System.out.println("Attack trigger: " + this.mob.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
    this.cooldown = (int)(20 / this.mob.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
    this.mob.tryAttack(this.target);
  }
}
