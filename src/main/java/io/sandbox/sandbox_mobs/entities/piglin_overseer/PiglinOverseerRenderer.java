package io.sandbox.sandbox_mobs.entities.piglin_overseer;

import io.sandbox.sandbox_mobs.Main;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;

public class PiglinOverseerRenderer extends ExtendedGeoEntityRenderer<PiglinOverseerEntity> {

  public PiglinOverseerRenderer(Context ctx) {
    super(ctx, new PiglinOverseerModel());
  }
  
  public Identifier getTextureLocation(PiglinOverseerEntity instance) {
    return new Identifier(Main.MOD_ID, "textures/entity/piglin_overseer/piglin_overseer.png");
  }

  @Override
  protected boolean isArmorBone(GeoBone bone) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected Identifier getTextureForBone(String boneName, PiglinOverseerEntity currentEntity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ItemStack getHeldItemForBone(String boneName, PiglinOverseerEntity currentEntity) {
    switch (boneName) {
      case "hand_left":
        return currentEntity.isLeftHanded() ? mainHand : offHand;
      case "hand_right":
        return currentEntity.isLeftHanded() ? offHand : mainHand;
      default:
        break;
    }

    return null;
  }

  @Override
  protected Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
    switch (boneName) {
      case "hand_left":
        return Mode.THIRD_PERSON_RIGHT_HAND;
      case "hand_right":
        return Mode.THIRD_PERSON_RIGHT_HAND;
      default:
        return Mode.NONE;
    }
  }

  @Override
  protected BlockState getHeldBlockForBone(String boneName, PiglinOverseerEntity currentEntity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void preRenderItem(MatrixStack matrixStack, ItemStack item, String boneName, PiglinOverseerEntity currentEntity, IBone bone) {
    if (item.equals(mainHand)) {
      matrixStack.scale(2, 2, 2);
    }
  }

  @Override
  protected void preRenderBlock(MatrixStack matrixStack, BlockState block, String boneName,
      PiglinOverseerEntity currentEntity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void postRenderItem(MatrixStack matrixStack, ItemStack item, String boneName, PiglinOverseerEntity currentEntity,
      IBone bone) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void postRenderBlock(MatrixStack matrixStack, BlockState block, String boneName,
      PiglinOverseerEntity currentEntity) {
    // TODO Auto-generated method stub
    
  }

}
