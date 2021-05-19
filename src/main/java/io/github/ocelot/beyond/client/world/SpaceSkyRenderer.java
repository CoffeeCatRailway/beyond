package io.github.ocelot.beyond.client.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.client.render.SpaceStarsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ISkyRenderHandler;

import javax.annotation.Nullable;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

/**
 * @author Ocelot
 */
public class SpaceSkyRenderer implements ISkyRenderHandler
{
    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
    private static final SpaceStarsRenderer STARS = new SpaceStarsRenderer();

    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;

    private void drawSkyHemisphere(BufferBuilder builder, float y, boolean reverse)
    {
        builder.begin(7, DefaultVertexFormat.POSITION);

        for (int k = -384; k <= 384; k += 64)
        {
            for (int l = -384; l <= 384; l += 64)
            {
                float f = (float) k;
                float f1 = (float) (k + 64);
                if (reverse)
                {
                    f1 = (float) k;
                    f = (float) (k + 64);
                }

                builder.vertex(f, y, l).endVertex();
                builder.vertex(f1, y, l).endVertex();
                builder.vertex(f1, y, l + 64).endVertex();
                builder.vertex(f, y, l + 64).endVertex();
            }
        }
    }

    private void createDarkSky()
    {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.darkBuffer != null)
        {
            this.darkBuffer.close();
        }

        this.darkBuffer = new VertexBuffer(DefaultVertexFormat.POSITION);
        this.drawSkyHemisphere(bufferbuilder, -16.0F, true);
        bufferbuilder.end();
        this.darkBuffer.upload(bufferbuilder);
    }

    private void createLightSky()
    {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.skyBuffer != null)
        {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer(DefaultVertexFormat.POSITION);
        this.drawSkyHemisphere(bufferbuilder, 16.0F, false);
        bufferbuilder.end();
        this.skyBuffer.upload(bufferbuilder);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(int ticks, float partialTicks, PoseStack poseStack, ClientLevel level, Minecraft minecraft)
    {
        RenderSystem.disableTexture();
        Vec3 vector3d = level.getSkyColor(minecraft.gameRenderer.getMainCamera().getBlockPosition(), partialTicks);
        float f = (float) vector3d.x;
        float f1 = (float) vector3d.y;
        float f2 = (float) vector3d.z;
        FogRenderer.levelFogColor();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthMask(false);
        RenderSystem.enableFog();
        RenderSystem.color4f(f, f1, f2, 1.0F);

        if (this.skyBuffer == null)
            this.createLightSky();
        this.skyBuffer.bind();
        DefaultVertexFormat.POSITION.setupBufferState(0L);
        this.skyBuffer.draw(poseStack.last().pose(), GL_QUADS);
        VertexBuffer.unbind();
        DefaultVertexFormat.POSITION.clearBufferState();
        RenderSystem.disableFog();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float[] afloat = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
        if (afloat != null)
        {
            RenderSystem.disableTexture();
            RenderSystem.shadeModel(7425);
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            float f3 = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(f3));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            float f4 = afloat[0];
            float f5 = afloat[1];
            float f6 = afloat[2];
            Matrix4f matrix4f = poseStack.last().pose();
            builder.begin(6, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3]).endVertex();

            for (int j = 0; j <= 16; ++j)
            {
                float f7 = (float) j * ((float) Math.PI * 2F) / 16.0F;
                float f8 = Mth.sin(f7);
                float f9 = Mth.cos(f7);
                builder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
            }

            Tesselator.getInstance().end();
            poseStack.popPose();
            RenderSystem.shadeModel(7424);
        }

        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // TODO draw other planets

        float f12 = 30.0F;
        minecraft.textureManager.bind(SUN_LOCATION);
        Matrix4f matrix4f1 = poseStack.last().pose();
        builder.begin(GL_QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix4f1, -f12, 100.0F, -f12).uv(0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f1, f12, 100.0F, -f12).uv(1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f1, f12, 100.0F, f12).uv(1.0F, 1.0F).endVertex();
        builder.vertex(matrix4f1, -f12, 100.0F, f12).uv(0.0F, 1.0F).endVertex();
        Tesselator.getInstance().end();

        STARS.render(poseStack);

        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableFog();
        poseStack.popPose();
        RenderSystem.disableTexture();
        double d0 = Objects.requireNonNull(minecraft.player).getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight();
        if (d0 < 0.0D)
        {
            if (this.darkBuffer == null)
                this.createDarkSky();
            this.darkBuffer.bind();
            DefaultVertexFormat.POSITION.setupBufferState(0L);
            this.darkBuffer.draw(poseStack.last().pose(), GL_QUADS);
            VertexBuffer.unbind();
            DefaultVertexFormat.POSITION.clearBufferState();
            RenderSystem.enableTexture();
        }

        if (level.effects().hasGround())
        {
            RenderSystem.color3f(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
        }
        else
        {
            RenderSystem.color3f(f, f1, f2);
        }

        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.disableFog();
    }
}
