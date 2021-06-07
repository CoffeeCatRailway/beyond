package io.github.ocelot.beyond.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.common.world.space.DimensionSpaceSettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

@OnlyIn(Dist.CLIENT)
public class SpaceHelmetLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
    private static final ItemStack GLASS = new ItemStack(Blocks.GLASS);

    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public SpaceHelmetLayer(RenderLayerParent<T, M> p_i50946_1_)
    {
        this(p_i50946_1_, 1.0F, 1.0F, 1.0F);
    }

    public SpaceHelmetLayer(RenderLayerParent<T, M> p_i232475_1_, float scaleX, float scaleY, float scaleZ)
    {
        super(p_i232475_1_);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity e, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_)
    {
        // TODO check if there is no oxygen
        if (!(e instanceof LivingEntity) || !(this.getParentModel() instanceof HeadedModel) || !DimensionSpaceSettingsManager.get(LogicalSide.CLIENT).getSettings(e.level.dimension().location()).requiresSpaceSuit((LivingEntity) e))
            return;

        LivingEntity entity = (LivingEntity) e;
        poseStack.pushPose();
        poseStack.scale(this.scaleX, this.scaleY, this.scaleZ);
        boolean flag = entity instanceof Villager || entity instanceof ZombieVillager;
        if (entity.isBaby() && !(entity instanceof Villager))
        {
            poseStack.translate(0.0D, 0.03125D, 0.0D);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }

        ((HeadedModel) this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0D, -0.25D, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        if (flag)
            poseStack.translate(0.0D, 0.1875D, 0.0D);

        Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, GLASS, ItemTransforms.TransformType.HEAD, false, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}