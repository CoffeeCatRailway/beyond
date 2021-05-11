package io.github.ocelot.beyond.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.beyond.common.init.BeyondDimensions;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettingsManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

@OnlyIn(Dist.CLIENT)
public class SpaceHelmetLayer<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T, M>
{
    private static final ItemStack GLASS = new ItemStack(Blocks.GLASS);

    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public SpaceHelmetLayer(IEntityRenderer<T, M> p_i50946_1_)
    {
        this(p_i50946_1_, 1.0F, 1.0F, 1.0F);
    }

    public SpaceHelmetLayer(IEntityRenderer<T, M> p_i232475_1_, float scaleX, float scaleY, float scaleZ)
    {
        super(p_i232475_1_);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    @Override
    public void render(MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight, Entity e, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_)
    {
        // TODO check if there is no oxygen
        if (!(e instanceof LivingEntity) || !(this.getParentModel() instanceof IHasHead) || !DimensionSpaceSettingsManager.get(LogicalSide.CLIENT).getSettings(e.level.dimension().location()).requiresSpaceSuit((LivingEntity) e))
            return;

        LivingEntity entity = (LivingEntity) e;
        poseStack.pushPose();
        poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean flag = entity instanceof VillagerEntity || entity instanceof ZombieVillagerEntity;
        if (entity.isBaby() && !(entity instanceof VillagerEntity))
        {
            poseStack.translate(0.0D, 0.03125D, 0.0D);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }

        ((IHasHead) this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0D, -0.25D, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        if (flag)
            poseStack.translate(0.0D, 0.1875D, 0.0D);

        Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, GLASS, ItemCameraTransforms.TransformType.HEAD, false, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}