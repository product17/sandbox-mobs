package io.sandbox.sandbox_mobs.entities;

public interface IAnimationTriggers {
  public int getMainAttackProgress();
  public int getMainAttackCooldown();
  public void setMainAttackProgress(int tick);

  public boolean getSpecialAttackActive();
  public int getSpecialAttackCooldown();
  public int getSpecialAttackProgress();
  public void setSpecialAttackActive(Boolean isActive);
  public void setSpecialAttackProgress(int tick);
}
