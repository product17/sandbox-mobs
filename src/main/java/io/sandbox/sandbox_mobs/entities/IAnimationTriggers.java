package io.sandbox.sandbox_mobs.entities;

public interface IAnimationTriggers {
  public boolean getMainAttackHasSwung();
  public void setMainAttackHasSwung(Boolean swung);
  public int getMainAttackProgress();
  public void setMainAttackProgress(int tick);
}
