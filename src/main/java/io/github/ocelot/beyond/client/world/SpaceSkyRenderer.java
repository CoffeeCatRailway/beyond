package io.github.ocelot.beyond.client.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.client.BeyondRenderTypes;
import io.github.ocelot.beyond.client.render.SpaceStarsRenderer;
import io.github.ocelot.beyond.client.screen.component.SolarSystemWidget;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.planet.StaticSolarSystemDefinitions;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

/**
 * @author Ocelot
 */
public class SpaceSkyRenderer implements ISkyRenderHandler
{
    private static final SpaceStarsRenderer STARS = new SpaceStarsRenderer();

    @Nullable
    private SimulationInfo simulationInfo;
    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;

    public SpaceSkyRenderer()
    {
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
    }

    private void clientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().level != null && this.simulationInfo != null && this.simulationInfo.isValid(Minecraft.getInstance().level))
            this.simulationInfo.simulation.tick();
    }

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
    private void renderSolarSystem(float partialTicks, PoseStack matrixStack, ClientLevel level)
    {
        if (this.simulationInfo == null)
            this.simulationInfo = new SimulationInfo(level);
        if (!this.simulationInfo.isValid(level))
        {
            this.simulationInfo.free();
            this.simulationInfo = new SimulationInfo(level);
        }

        SimulatedBody currentBody = this.simulationInfo.getBody();
        if (currentBody == null)
            return;

        Window window = Minecraft.getInstance().getWindow();
        CelestialBodySimulation simulation = this.simulationInfo.simulation;
        MultiBufferSource.BufferSource buffer = BeyondRenderTypes.planetBuffer();

        RenderSystem.depthMask(true);
        RenderTarget simulationTarget = this.simulationInfo.getSimulationTarget();
        simulationTarget.setClearColor(0,0,0,0);
        simulationTarget.clear(Minecraft.ON_OSX);
        simulationTarget.bindWrite(false);
        Lighting.setupFor3DItems();

        matrixStack.pushPose();
        matrixStack.translate(-currentBody.getX(partialTicks), 0, -currentBody.getZ(partialTicks));

        simulation.getBodies().forEach(body -> SolarSystemWidget.renderBody(matrixStack, buffer, body, partialTicks, false));
        matrixStack.popPose();

        buffer.endBatch();

        RenderSystem.depthMask(false);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GlStateManager._matrixMode(5889);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._ortho(0.0, window.getWidth(), window.getHeight(), 0.0, 1000.0, 3000.0);
        GlStateManager._matrixMode(5888);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._translatef(0.0F, 0.0F, -2000.0F);
        GlStateManager._enableTexture();
        GlStateManager._disableLighting();
        GlStateManager._disableAlphaTest();

        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        simulationTarget.bindRead();
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0, window.getHeight(), 0.0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(window.getWidth(), window.getHeight(), 0.0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(window.getWidth(), 0.0, 0.0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(0.0, 0.0, 0.0).uv(0.0F, 1.0F).endVertex();
        tesselator.end();
        simulationTarget.unbindRead();

        GlStateManager._matrixMode(5889);
        GlStateManager._popMatrix();
        GlStateManager._matrixMode(5888);
        GlStateManager._popMatrix();
        RenderSystem.disableBlend();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(int ticks, float partialTicks, PoseStack matrixStack, ClientLevel level, Minecraft minecraft)
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
        this.skyBuffer.draw(matrixStack.last().pose(), GL_QUADS);
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
            matrixStack.pushPose();
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            float f3 = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f3));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            float f4 = afloat[0];
            float f5 = afloat[1];
            float f6 = afloat[2];
            Matrix4f matrix4f = matrixStack.last().pose();
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
            matrixStack.popPose();
            RenderSystem.shadeModel(7424);
        }

        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YN.rotationDegrees(90.0F));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

//        float f12 = 30.0F;
//        minecraft.textureManager.bind(SUN_LOCATION);
//        Matrix4f matrix4f1 = matrixStack.last().pose();
//        builder.begin(GL_QUADS, DefaultVertexFormat.POSITION_TEX);
//        builder.vertex(matrix4f1, -f12, 100.0F, -f12).uv(0.0F, 0.0F).endVertex();
//        builder.vertex(matrix4f1, f12, 100.0F, -f12).uv(1.0F, 0.0F).endVertex();
//        builder.vertex(matrix4f1, f12, 100.0F, f12).uv(1.0F, 1.0F).endVertex();
//        builder.vertex(matrix4f1, -f12, 100.0F, f12).uv(0.0F, 1.0F).endVertex();
//        Tesselator.getInstance().end();

        STARS.render(matrixStack);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        this.renderSolarSystem(partialTicks, matrixStack, level);
        matrixStack.popPose();

        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableFog();
        RenderSystem.disableTexture();
        double d0 = Objects.requireNonNull(minecraft.player).getEyePosition(partialTicks).y - 93; // hardcoded horizon to 93 in space
        if (d0 < 0.0D)
        {
            if (this.darkBuffer == null)
                this.createDarkSky();
            this.darkBuffer.bind();
            DefaultVertexFormat.POSITION.setupBufferState(0L);
            this.darkBuffer.draw(matrixStack.last().pose(), GL_QUADS);
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

    private static class SimulationInfo implements NativeResource
    {
        private final ClientLevel level;
        private final CelestialBodySimulation simulation;
        private RenderTarget simulationTarget;

        private SimulationInfo(ClientLevel level)
        {
            this.level = level;
            this.simulation = getSolarSystem(level.dimension());
        }

        @Nullable
        private static CelestialBodySimulation getSolarSystem(ResourceKey<Level> dimension)
        {
            // TODO get real solar system once they are data-driven
            Map<ResourceLocation, Planet> solarSystem = StaticSolarSystemDefinitions.SOLAR_SYSTEM.get();
            return solarSystem.values().stream().anyMatch(planet -> planet.getDimension().isPresent() && planet.getDimension().get().equals(dimension.location())) ? new CelestialBodySimulation(solarSystem) : null;
        }

        public boolean isValid(ClientLevel level)
        {
            return this.level == level;
        }

        @Nullable
        public CelestialBodySimulation getSimulation()
        {
            return simulation;
        }

        @Nullable
        public SimulatedBody getBody()
        {
            return this.simulation != null ? this.simulation.getBodies().filter(body -> body.getDimension().isPresent() && body.getDimension().get().equals(level.dimension().location())).findFirst().orElse(null) : null;
        }

        public RenderTarget getSimulationTarget()
        {
            RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
            if (this.simulationTarget == null)
            {
                this.simulationTarget = new RenderTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
                this.simulationTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            }
            else if (this.simulationTarget.width != mainTarget.width || this.simulationTarget.height != mainTarget.height)
            {
                this.simulationTarget.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
            }
            return this.simulationTarget;
        }

        @Override
        public void free()
        {
            if (this.simulationTarget != null)
            {
                this.simulationTarget.destroyBuffers();
                this.simulationTarget = null;
            }
        }
    }
}
