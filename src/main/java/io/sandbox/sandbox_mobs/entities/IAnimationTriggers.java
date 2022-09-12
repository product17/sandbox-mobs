package io.sandbox.sandbox_mobs.entities;

public interface IAnimationTriggers {
  public int getMainAttackStartAnimationTicks();
  // public void setMainAttackStartAnimationTicks(int ticks);
  // public int getMainAttackUntilDamageTicks();
  public void setMainAttackTicksUntilDamage(int ticks);
  public void mainAttackStarted();
}
