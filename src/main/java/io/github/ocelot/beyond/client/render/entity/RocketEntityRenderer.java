package io.github.ocelot.beyond.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.ocelot.beyond.common.entity.RocketEntity;
import io.github.ocelot.sonar.client.render.StructureTemplateRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * @author Ocelot
 */
public class RocketEntityRenderer<T extends RocketEntity> extends EntityRenderer<T>
{
    public RocketEntityRenderer(EntityRenderDispatcher entityRenderer)
    {
        super(entityRenderer);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight)
    {
        StructureTemplateRenderer renderer = entity.getRenderer();
        Vec3i size = renderer.getWorldSize();
        renderer.render(matrixStack, size.getX() / 2F, 0, size.getZ() / 2F);
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, combinedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T rocket)
    {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
