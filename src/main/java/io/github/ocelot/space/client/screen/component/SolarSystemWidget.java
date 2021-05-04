package io.github.ocelot.space.client.screen.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ocelot.sonar.client.render.ShapeRenderer;
import io.github.ocelot.space.client.MousePicker;
import io.github.ocelot.space.client.SpacePlanetSpriteManager;
import io.github.ocelot.space.client.screen.SpaceTravelCamera;
import io.github.ocelot.space.common.init.SpaceRenderTypes;
import io.github.ocelot.space.common.planet.CelestialBodyDefinitions;
import io.github.ocelot.space.common.planet.CelestialBodySimulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.system.NativeResource;

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
        CUBE.addBox(0, 0, 0, 8, 8, 8, 0);
    }

    private final CelestialBodySimulation simulation;
    private final SpaceTravelCamera camera;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Framebuffer framebuffer;
    private VertexBuffer skyVBO;

    public SolarSystemWidget(int x, int y, int width, int height)
    {
        super(x, y, width, height, StringTextComponent.EMPTY);

        this.simulation = new CelestialBodySimulation(CelestialBodyDefinitions.SOLAR_SYSTEM);
        this.camera = new SpaceTravelCamera();
        this.camera.setZoom(80);
        this.camera.setPitch((float) (24F * Math.PI / 180F));
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.skyVBO != null)
            this.skyVBO.close();

        this.skyVBO = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
        Random random = new Random();
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

    private void renderBody(MatrixStack poseStack, IRenderTypeBuffer buffer, CelestialBodySimulation.SimulatedBody body, float partialTicks)
    {
        float scale = body.getBody().getScale() * 4;
        poseStack.pushPose();
        poseStack.translate(body.getX(partialTicks), body.getY(partialTicks), body.getZ(partialTicks));
        poseStack.mulPose(Vector3f.YP.rotation(body.getRotation(partialTicks)));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.25F, -0.25F, -0.25F);
        CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(body.getBody().getTexture()).wrap(buffer.getBuffer(body.getBody().isShade() ? SpaceRenderTypes.planetShade() : SpaceRenderTypes.planet())), 15728880, OverlayTexture.NO_OVERLAY);
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
        RenderSystem.multMatrix(this.projectionMatrix = Matrix4f.perspective(70, (float) this.width / (float) this.height, 0.3F, 10000.0F));
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

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.enableLighting();
        RenderHelper.setupLevel(matrixStack1.last().pose());
        RenderSystem.disableLighting();

        float cameraX = this.camera.getX(partialTicks);
        float cameraY = this.camera.getY(partialTicks);
        float cameraZ = this.camera.getZ(partialTicks);
        matrixStack1.translate(-cameraX, -cameraY, -cameraZ);

        this.viewMatrix = matrixStack1.last().pose().copy();

        matrixStack1.pushPose();
        this.simulation.getBodies().forEach(body -> this.renderBody(matrixStack1, buffer, body, partialTicks));
        matrixStack1.popPose();

        buffer.endBatch();

//        Vector3d ray = MousePicker.getRay(this.projectionMatrix, this.viewMatrix, (float) (mouseX - this.x) / (float) this.width * 2F - 1F, (float) (mouseY - this.y) / (float) this.height * 2F - 1F);
//        Vector3d start = new Vector3d(cameraX, cameraY, cameraZ);
//        Vector3d end = start.add(ray.multiply(1000, 1000, 1000));
//        Optional<CelestialBodySimulation.CelestialBodyRayTraceResult> resultOptional = this.simulation.clip(start, end, partialTicks);
//
//        resultOptional.ifPresent(result ->
//        {
//            Vector3d pos = result.getPos();
//            matrixStack1.pushPose();
//            matrixStack1.translate(pos.x() - 1, pos.y() - 1, pos.z() - 1);
//            matrixStack1.scale(2, 2, 2);
//            Minecraft.getInstance().getBlockRenderer().renderBlock(Blocks.DIAMOND_BLOCK.defaultBlockState(), matrixStack1, buffer, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
//            matrixStack1.popPose();
//            buffer.endBatch();
//        });

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
        if (mouseButton == 0)
        {
            Vector3d ray = MousePicker.getRay(this.projectionMatrix, this.viewMatrix, (float) (mouseX - this.x) / (float) this.width * 2F - 1F, (float) (mouseY - this.y) / (float) this.height * 2F - 1F);
            Vector3d start = new Vector3d(this.camera.getX(1.0F), this.camera.getY(1.0F), this.camera.getZ(1.0F));
            Vector3d end = start.add(ray.multiply(1000, 1000, 1000));

            return this.simulation.clip(start, end, 1.0F).map(result ->
            {
                this.camera.setFocused(result.getBody());
                return true;
            }).orElse(false);
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
}
