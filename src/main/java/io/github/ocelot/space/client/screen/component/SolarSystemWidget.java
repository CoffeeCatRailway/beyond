package io.github.ocelot.space.client.screen.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ocelot.sonar.client.render.BakedModelRenderer;
import io.github.ocelot.sonar.client.render.ShapeRenderer;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.MousePicker;
import io.github.ocelot.space.client.SpacePlanetSpriteManager;
import io.github.ocelot.space.client.screen.SpaceTravelCamera;
import io.github.ocelot.space.common.init.SpaceRenderTypes;
import io.github.ocelot.space.common.simulation.*;
import io.github.ocelot.space.common.simulation.body.CelestialBodyDefinitions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * <p>A widget for rendering space in a GUI.</p>
 *
 * @author Ocelot
 */
public class SolarSystemWidget extends Widget implements IScreen, NativeResource
{
    private static final ModelRenderer CUBE = new ModelRenderer(32, 16, 0, 0);

    static
    {
        CUBE.addBox(0, 0, 0, 8, 8, 8, 4);
    }

    private final Screen parent;
    private final CelestialBodySimulation simulation;
    private final SpaceTravelCamera camera;
    private CelestialBodySimulation.CelestialBodyRayTraceResult hoveredBody;
    private Framebuffer framebuffer;
    private VertexBuffer skyVBO;

    public SolarSystemWidget(@Nullable Screen parent, int x, int y, int width, int height)
    {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.parent = parent;
        this.simulation = new CelestialBodySimulation(CelestialBodyDefinitions.SOLAR_SYSTEM.get());
        this.camera = new SpaceTravelCamera();
        this.camera.setZoom(80);
        this.camera.setPitch((float) (24F * Math.PI / 180F));

        ArtificialSatellite earthSatellite = new ArtificialSatellite(this.simulation, new ResourceLocation(SpacePrototype.MOD_ID, "earth_satellite_test"));
        earthSatellite.setParent(new ResourceLocation(SpacePrototype.MOD_ID, "earth"));
        earthSatellite.setDistanceFromParent(5F);
        earthSatellite.setModel(new ResourceLocation(SpacePrototype.MOD_ID, "body/satellite"));
        earthSatellite.setDisplayName(new StringTextComponent("Earth Satellite Test"));
        this.simulation.add(earthSatellite);

        ArtificialSatellite marsSatellite = new ArtificialSatellite(this.simulation, new ResourceLocation(SpacePrototype.MOD_ID, "mars_satellite_test"));
        marsSatellite.setParent(new ResourceLocation(SpacePrototype.MOD_ID, "mars"));
        marsSatellite.setDistanceFromParent(5F);
        marsSatellite.setModel(new ResourceLocation(SpacePrototype.MOD_ID, "body/satellite"));
        marsSatellite.setDisplayName(new StringTextComponent("Mars Satellite Test"));
        this.simulation.add(marsSatellite);
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.skyVBO != null)
            this.skyVBO.close();

        this.skyVBO = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
        Random random = new Random(10842L);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < 3000; ++i)
        {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d18 = (double) ((j & 2) - 1) * d3;
                    double d19 = (double) ((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    float blue = 0.7F + random.nextFloat() * 0.3F;
                    bufferbuilder.vertex(d5 + d25, d6 + d23, d7 + d26).color(blue, blue, 1.0F, random.nextFloat()).endVertex();
                }
            }
        }
        bufferbuilder.end();
        this.skyVBO.upload(bufferbuilder);
    }

    private void renderBody(MatrixStack poseStack, IRenderTypeBuffer.Impl buffer, SimulatedBody body, float partialTicks)
    {
        float scale = body.getSize();
        boolean hovered = this.hoveredBody != null && this.hoveredBody.getBody().equals(body);
        poseStack.pushPose();
        poseStack.translate(body.getX(partialTicks), body.getY(partialTicks), body.getZ(partialTicks));
        poseStack.mulPose(Vector3f.ZP.rotation(body.getRotationZ(partialTicks)));
        poseStack.mulPose(Vector3f.YP.rotation(body.getRotationY(partialTicks)));
        poseStack.mulPose(Vector3f.XP.rotation(body.getRotationX(partialTicks)));
        poseStack.scale(scale, scale, scale);

        switch (body.getRenderType())
        {
            case CUBE:
                if (body instanceof NaturalSimulatedBody)
                {
                    poseStack.pushPose();
                    poseStack.translate(-0.25F, -0.25F, -0.25F);
                    NaturalSimulatedBody b = (NaturalSimulatedBody) body;
                    CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(b.getTexture()).wrap(buffer.getBuffer(SpaceRenderTypes.planet(b.isShade(), false))), 15728880, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();

                    b.getAtmosphere().ifPresent(atmosphere ->
                    {
                        poseStack.scale((1.0F + atmosphere.getDistance() / scale), (1.0F + atmosphere.getDistance() / scale), (1.0F + atmosphere.getDistance() / scale));
                        poseStack.pushPose();
                        poseStack.translate(-0.25F, -0.25F, -0.25F);
                        CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(atmosphere.getTexture()).wrap(buffer.getBuffer(SpaceRenderTypes.planet(b.isShade(), true))), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, atmosphere.getDensity());
                        poseStack.popPose();
                    });
                    if (hovered)
                    {
                        poseStack.translate(-0.25F, -0.25F, -0.25F);
                        CUBE.render(poseStack, buffer.getBuffer(SpaceRenderTypes.planetSelect()), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.5F);
                    }
                }
                break;
            case MODEL:
                if (body instanceof ModelSimulatedBody)
                {
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    ModelSimulatedBody b = (ModelSimulatedBody) body;
                    BakedModelRenderer.renderModel(b.getModel(), buffer.getBuffer(RenderType.entityCutout(PlayerContainer.BLOCK_ATLAS)), poseStack, 1.0F, 1.0F, 1.0F, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                }
                break;
            case PLAYER:
                // TODO player
                break;
        }

        poseStack.popPose();
    }

    private void invalidateFramebuffer()
    {
        if (this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer = null;
        }
    }

    @Override
    public void tick()
    {
        this.simulation.tick();
        this.camera.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.hoveredBody = null;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        if (this.framebuffer == null)
            this.framebuffer = new Framebuffer(this.width * minecraft.options.guiScale, this.height * minecraft.options.guiScale, true, Minecraft.ON_OSX);
        this.framebuffer.bindWrite(true);

        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        Matrix4f projectionMatrix = Matrix4f.perspective(70, (float) this.width / (float) this.height, 0.3F, 10000.0F);
        RenderSystem.multMatrix(projectionMatrix);
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();

        MatrixStack matrixStack1 = new MatrixStack();

        GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager._clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);

        float cameraRotationX = this.camera.getRotationX(partialTicks);
        float cameraRotationY = this.camera.getRotationY(partialTicks);
        matrixStack1.mulPose(Vector3f.XN.rotation(cameraRotationX));
        matrixStack1.mulPose(Vector3f.YN.rotation(cameraRotationY));

        if (this.skyVBO == null)
            this.generateSky();
        this.skyVBO.bind();
        DefaultVertexFormats.POSITION_COLOR.setupBufferState(0L);
        this.skyVBO.draw(matrixStack1.last().pose(), 7);
        VertexBuffer.unbind();
        DefaultVertexFormats.POSITION_COLOR.clearBufferState();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();

        RenderSystem.enableLighting();
        RenderHelper.setupLevel(matrixStack1.last().pose());
        RenderSystem.disableLighting();

        float cameraX = this.camera.getX(partialTicks);
        float cameraY = this.camera.getY(partialTicks);
        float cameraZ = this.camera.getZ(partialTicks);
        matrixStack1.translate(-cameraX, -cameraY, -cameraZ);

        Matrix4f viewMatrix = matrixStack1.last().pose().copy();
        Vector3d ray = MousePicker.getRay(projectionMatrix, viewMatrix, (float) (mouseX - this.x) / (float) this.width * 2F - 1F, (float) (mouseY - this.y) / (float) this.height * 2F - 1F);
        Vector3d start = new Vector3d(cameraX, cameraY, cameraZ);
        Vector3d end = start.add(ray.multiply(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        this.hoveredBody = this.simulation.clip(start, end, partialTicks).orElse(null);

        IRenderTypeBuffer.Impl buffer = SpaceRenderTypes.planetBuffer();
        matrixStack1.pushPose();
        this.simulation.getBodies().forEach(body -> this.renderBody(matrixStack1, buffer, body, partialTicks));
        matrixStack1.popPose();
        buffer.endBatch(SpaceRenderTypes.planet(true, false));
        buffer.endBatch(SpaceRenderTypes.planet(false, false));
        buffer.endBatch(RenderType.entityCutout(PlayerContainer.BLOCK_ATLAS));
        buffer.endBatch();

        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.popMatrix();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        poseStack.pushPose();
        poseStack.translate(0, 0, 10);
        this.framebuffer.bindRead();
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShapeRenderer.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        ShapeRenderer.drawRectWithTexture(poseStack, this.x, this.y, 0, 1, this.width, this.height, 1, -1, 1, 1);
        this.framebuffer.unbindRead();
        poseStack.popPose();

        if (this.isHovered())
            this.renderToolTip(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(MatrixStack poseStack, int mouseX, int mouseY)
    {
        if (this.hoveredBody != null)
        {
            List<ITextComponent> tooltip = new ArrayList<>();
            tooltip.add(this.hoveredBody.getBody().getDisplayName());
            if (Minecraft.getInstance().options.advancedItemTooltips)
                tooltip.add(new StringTextComponent(this.hoveredBody.getBody().getId().toString()).withStyle(TextFormatting.DARK_GRAY));
            this.parent.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)
    {
        return this.camera.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (!this.isHovered())
            return false;
        if (this.camera.mouseClicked(mouseX, mouseY, mouseButton))
            return true;
        if (this.hoveredBody != null && mouseButton == 0)
        {
            this.camera.setFocused(this.hoveredBody.getBody());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        if (!this.isHovered())
            return false;
        return this.camera.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void free()
    {
        if (this.skyVBO != null)
        {
            this.skyVBO.close();
            this.skyVBO = null;
        }
        this.invalidateFramebuffer();
    }

    @Override
    public void setWidth(int width)
    {
        super.setWidth(width);
        this.invalidateFramebuffer();
    }

    @Override
    public void setHeight(int height)
    {
        super.setHeight(height);
        this.invalidateFramebuffer();
    }

    @Override
    protected IFormattableTextComponent createNarrationMessage()
    {
        return StringTextComponent.EMPTY.copy();
    }
}
