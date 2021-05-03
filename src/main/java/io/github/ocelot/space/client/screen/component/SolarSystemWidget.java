package io.github.ocelot.space.client.screen.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ocelot.sonar.client.render.ShapeRenderer;
import io.github.ocelot.space.client.SpacePlanetSpriteManager;
import io.github.ocelot.space.client.screen.SpaceTravelCamera;
import io.github.ocelot.space.common.planet.CelestialBodyDefinitions;
import io.github.ocelot.space.common.planet.CelestialBodySimulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
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
    private SpaceTravelCamera camera;
    private Framebuffer framebuffer;
    private VertexBuffer skyVBO;
    private int ticks;

    public SolarSystemWidget(int x, int y, int width, int height)
    {
        super(x, y, width, height, new StringTextComponent(""));
        this.active = false;
        this.simulation = new CelestialBodySimulation(CelestialBodyDefinitions.SOLAR_SYSTEM);
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.skyVBO != null)
            this.skyVBO.close();

        this.skyVBO = new VertexBuffer(DefaultVertexFormats.POSITION);
        Random random = new Random(10842L);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
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
                    bufferbuilder.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
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
        poseStack.translate(MathHelper.lerp(partialTicks, body.getLastPosition().x(), body.getPosition().x()), MathHelper.lerp(partialTicks, body.getLastPosition().y(), body.getPosition().y()), MathHelper.lerp(partialTicks, body.getLastPosition().z(), body.getPosition().z()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, body.getLastRotation(), body.getRotation())));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.25F, -0.25F, -0.25F);
        CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(body.getBody().getTexture()).wrap(buffer.getBuffer(RenderType.entitySolid(SpacePlanetSpriteManager.ATLAS_LOCATION))), 15728880, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public void tick()
    {
        this.ticks++;
        this.simulation.tick();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        float time = this.ticks + partialTicks;
        if (this.framebuffer == null)
            this.framebuffer = new Framebuffer(this.width * minecraft.options.guiScale, this.height * minecraft.options.guiScale, true, Minecraft.ON_OSX);
        this.framebuffer.bindWrite(true);

        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(70, (float) this.width / (float) this.height, 0.3F, 400F));
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();

        MatrixStack matrixStack1 = new MatrixStack();

        GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager._clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);

//        matrixStack1.mulPose(Vector3f.ZN.rotationDegrees(0));
//        matrixStack1.mulPose(Vector3f.YN.rotationDegrees(mouseX));
//        matrixStack1.mulPose(Vector3f.XN.rotationDegrees(mouseY));

        matrixStack1.pushPose();
        matrixStack1.mulPose(Vector3f.YP.rotation(time / 1200F));
        matrixStack1.mulPose(Vector3f.XP.rotation(time / 1100F));
        if (this.skyVBO == null)
            this.generateSky();
        this.skyVBO.bind();
        DefaultVertexFormats.POSITION.setupBufferState(0L);
        this.skyVBO.draw(matrixStack1.last().pose(), 7);
        VertexBuffer.unbind();
        DefaultVertexFormats.POSITION.clearBufferState();
        matrixStack1.popPose();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.enableLighting();
        RenderHelper.setupLevel(matrixStack1.last().pose());

        matrixStack1.translate(-this.camera.getPosX(partialTicks), -this.camera.getPosY(partialTicks) - 4.0F, -this.camera.getPosZ(partialTicks) - 30.0F);
        matrixStack1.mulPose(Vector3f.XP.rotation(this.camera.getRotationX(partialTicks)));
        matrixStack1.mulPose(Vector3f.YP.rotation(this.camera.getRotationY(partialTicks)));
        matrixStack1.mulPose(Vector3f.ZP.rotation(this.camera.getRotationZ(partialTicks)));

        matrixStack1.pushPose();

        this.simulation.getBodies().forEach(body -> this.renderBody(matrixStack1, buffer, body, partialTicks));

//        matrixStack1.mulPose(Vector3f.YP.rotationDegrees(time));
//        matrixStack1.translate(-0.5F, -0.5F, -0.5F);
//        Minecraft.getInstance().getBlockRenderer().renderBlock(Blocks.CAKE.defaultBlockState(), matrixStack1, buffer, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

//        matrixStack1.pushPose();
//        matrixStack1.translate(0.5F, 0.5F, 0.5F);
//        matrixStack1.mulPose(Vector3f.YP.rotationDegrees(time * 3F));
//        matrixStack1.translate(-0.5F, -0.5F, -0.5F);
//        matrixStack1.translate(2, 0, 0);
//        matrixStack1.scale(0.25F, 0.25F, 0.25F);
//        Minecraft.getInstance().getBlockRenderer().renderBlock(Blocks.SPONGE.defaultBlockState(), matrixStack1, buffer, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
//        matrixStack1.popPose();

        matrixStack1.popPose();

        RenderSystem.disableLighting();
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
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)
    {
        return super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return true;
    }

    @Override
    public void free()
    {
        if (this.skyVBO != null)
        {
            this.skyVBO.close();
            this.skyVBO = null;
        }
        if (this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer = null;
        }
    }
}
