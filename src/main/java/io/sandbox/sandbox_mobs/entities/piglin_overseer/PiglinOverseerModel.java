package io.sandbox.sandbox_mobs.entities.piglin_overseer;

import io.sandbox.sandbox_mobs.Main;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class PiglinOverseerModel extends AnimatedGeoModel<PiglinOverseerEntity> {

  @Override
  public Identifier getAnimationResource(PiglinOverseerEntity animatable) {
    return new Identifier(Main.MOD_ID, "animations/piglin_overseer.animation.json");
  }

  @Override
  public Identifier getModelResource(PiglinOverseerEntity object) {
    return new Identifier(Main.MOD_ID, "geo/piglin_overseer.geo.json");
  }

  @Override
  public Identifier getTextureResource(PiglinOverseerEntity object) {
    return new Identifier(Main.MOD_ID, "textures/entity/piglin_overseer/piglin_overseer.png");
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void setLivingAnimations(PiglinOverseerEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
    super.setLivingAnimations(entity, uniqueID, customPredicate);
    IBone head = this.getAnimationProcessor().getBone("head");

    EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
    if (head != null) {
      head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
      head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
    }
  }
}
