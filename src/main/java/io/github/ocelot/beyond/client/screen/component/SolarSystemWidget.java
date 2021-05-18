package io.github.ocelot.beyond.client.screen.component;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.client.BeyondRenderTypes;
import io.github.ocelot.beyond.client.MousePicker;
import io.github.ocelot.beyond.client.SpacePlanetSpriteManager;
import io.github.ocelot.beyond.client.render.SpaceStarsRenderer;
import io.github.ocelot.beyond.client.screen.SpaceTravelCamera;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.CPlanetTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.space.PlayerRocket;
import io.github.ocelot.beyond.common.space.planet.StaticSolarSystemDefinitions;
import io.github.ocelot.beyond.common.space.simulation.*;
import io.github.ocelot.beyond.common.util.CelestialBodyRayTraceResult;
import io.github.ocelot.sonar.client.render.BakedModelRenderer;
import io.github.ocelot.sonar.client.render.ShapeRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

/**
 * <p>A widget for rendering space in a GUI.</p>
 *
 * @author Ocelot
 */
public class SolarSystemWidget extends AbstractWidget implements ContainerEventHandler, TickableWidget, NativeResource
{
    private static final ModelPart CUBE = new ModelPart(32, 16, 0, 0);

    static
    {
        CUBE.addBox(0, 0, 0, 8, 8, 8, 4);
    }

    private final Screen parent;
    private final CelestialBodySimulation simulation;
    private final SpaceStarsRenderer starsRenderer;
    private final SpaceTravelCamera camera;
    private final PlayerRocketBody localRocket;
    private boolean travelling;

    private CelestialBodyRayTraceResult hoveredBody;
    private SimulatedBody selectedBody;
    private RenderTarget framebuffer;

    private final List<GuiEventListener> children;
    private final Button launchButton;
    private GuiEventListener focused;
    private boolean dragging;
    private boolean bubbleHovered;

    public SolarSystemWidget(@Nullable Screen parent, int x, int y, int width, int height, SOpenSpaceTravelScreenMessage msg)
    {
        super(x, y, width, height, TextComponent.EMPTY);
        this.parent = parent;
        this.simulation = new CelestialBodySimulation(StaticSolarSystemDefinitions.SOLAR_SYSTEM.get()); // TODO load solar system from server
        this.starsRenderer = new SpaceStarsRenderer();
        this.camera = new SpaceTravelCamera();
        this.camera.setZoom(30);
        this.camera.setPitch((float) (24F * Math.PI / 180F));

        // TODO get initial planet from server
        PlayerRocketBody p = null;
        for (PlayerRocket rocket : msg.getPlayers())
        {
            PlayerRocketBody body = new PlayerRocketBody(this.simulation, rocket);
            if (rocket.getProfile().getId().equals(Objects.requireNonNull(Minecraft.getInstance().player).getUUID()))
            {
                if (p != null)
                    throw new IllegalStateException("Duplicate local player was located in simulation.");
                p = body;
            }
            this.simulation.add(body);
        }
        if (p == null)
            throw new IllegalStateException("Local player was not located in simulation.");

        this.localRocket = p;
        this.localRocket.addListener(new PlayerRocketBody.PlayerTravelListener()
        {
            private void sendTravelPacket(ResourceLocation body, boolean arrive)
            {
                SimulatedBody simulatedBody = SolarSystemWidget.this.simulation.getBody(body);
                if (simulatedBody == null || !simulatedBody.canTeleportTo())
                    return;
                simulatedBody.getDimension().ifPresent(dimension -> BeyondMessages.PLAY.sendToServer(new CPlanetTravelMessage(body, arrive)));
            }

            @Override
            public void onDepart(PlayerRocketBody rocket, ResourceLocation body)
            {
                this.sendTravelPacket(body, false);
            }

            @Override
            public void onArrive(PlayerRocketBody rocket, ResourceLocation body)
            {
                this.sendTravelPacket(body, true);
            }
        });
        this.camera.setFocused(this.localRocket);

        this.children = new ArrayList<>();
        this.children.add(this.camera);
        this.launchButton = new Button(0, 0, 50, 20, new TranslatableComponent("gui." + Beyond.MOD_ID + ".launch"), button ->
        {
            if (this.selectedBody == null || !this.selectedBody.canTeleportTo())
                return;
            this.selectedBody.getDimension().ifPresent(dimension ->
            {
                System.out.println("Launched to " + dimension);
                PlayerRocketBody rocket = this.simulation.getPlayer(Objects.requireNonNull(Minecraft.getInstance().player).getUUID());
                if (rocket != null)
                {
                    this.travelling = true;
                    rocket.travelTo(this.selectedBody.getId());
                    this.selectedBody = null;
                }
            });
        }, (button, matrixStack, mouseX, mouseY) ->
        {
            if (this.selectedBody == null || !this.selectedBody.canTeleportTo())
                return;
            if (this.parent != null)
            {
                Optional<ResourceLocation> optionalDimension = this.selectedBody.getDimension();
                if (!optionalDimension.isPresent())
                {
                    this.parent.renderTooltip(matrixStack, new TranslatableComponent("gui." + Beyond.MOD_ID + ".cannot_launch"), mouseX, mouseY);
                }
                else if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.level.dimension().location().equals(optionalDimension.get()))
                {
                    this.parent.renderTooltip(matrixStack, new TranslatableComponent("gui." + Beyond.MOD_ID + ".already_there"), mouseX, mouseY);
                }
            }
        });
        this.launchButton.visible = false;
        this.children.add(this.launchButton);


        ArtificialSatellite earthSatellite = new ArtificialSatellite(this.simulation, new ResourceLocation(Beyond.MOD_ID, "earth_satellite_test"));
        earthSatellite.setParent(new ResourceLocation(Beyond.MOD_ID, "earth"));
        earthSatellite.setDistanceFromParent(5F);
        earthSatellite.setModel(new ResourceLocation(Beyond.MOD_ID, "body/satellite"));
        earthSatellite.setDisplayName(new TextComponent("Earth Satellite Test"));
        this.simulation.add(earthSatellite);

        ArtificialSatellite marsSatellite = new ArtificialSatellite(this.simulation, new ResourceLocation(Beyond.MOD_ID, "mars_satellite_test"));
        marsSatellite.setParent(new ResourceLocation(Beyond.MOD_ID, "mars"));
        marsSatellite.setDistanceFromParent(5F);
        marsSatellite.setModel(new ResourceLocation(Beyond.MOD_ID, "body/satellite"));
        marsSatellite.setDisplayName(new TextComponent("Mars Satellite Test"));
        this.simulation.add(marsSatellite);
    }

    private void renderBody(PoseStack poseStack, MultiBufferSource.BufferSource buffer, SimulatedBody body, float partialTicks)
    {
        float scale = body.getSize();
        boolean hovered = this.hoveredBody != null && this.hoveredBody.getBody().equals(body) && !this.bubbleHovered;
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
                    CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(b.getTexture()).wrap(buffer.getBuffer(BeyondRenderTypes.planet(b.isShade(), false))), 15728880, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();

                    b.getAtmosphere().ifPresent(atmosphere ->
                    {
                        float distance = 1.0F + atmosphere.getDistance() / scale;
                        poseStack.scale(distance, distance, distance);
                        poseStack.pushPose();
                        poseStack.translate(-0.25F, -0.25F, -0.25F);
                        CUBE.render(poseStack, SpacePlanetSpriteManager.getSprite(atmosphere.getTexture()).wrap(buffer.getBuffer(BeyondRenderTypes.planet(b.isShade(), true))), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, atmosphere.getDensity());
                        poseStack.popPose();
                    });
                    if (hovered)
                    {
                        poseStack.translate(-0.25F, -0.25F, -0.25F);
                        CUBE.render(poseStack, buffer.getBuffer(BeyondRenderTypes.planetSelect()), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.5F);
                    }
                }
                break;
            case MODEL:
                if (body instanceof ModelSimulatedBody)
                {
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    ModelSimulatedBody b = (ModelSimulatedBody) body;
                    BakedModelRenderer.renderModel(b.getModel(), buffer.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS)), poseStack, 1.0F, 1.0F, 1.0F, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                }
                break;
            case PLAYER:
                // TODO player
                if (body instanceof PlayerRocketBody)
                {
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    PlayerRocketBody b = (PlayerRocketBody) body;
                    BakedModelRenderer.renderModel(Minecraft.getInstance().getModelManager().getMissingModel(), buffer.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS)), poseStack, 1.0F, 1.0F, 1.0F, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                }
                break;
        }

        poseStack.popPose();
    }

    private void renderBubble(PoseStack poseStack, int x, int y, int mouseX, int mouseY)
    {
        Font fontRenderer = Minecraft.getInstance().font;
        Component description = new TextComponent("The moon is Earth's only natural satellite. The moon is a cold, dry orb whose surface is studded with craters and strewn with rocks and dust.").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        List<FormattedCharSequence> descriptionLines = fontRenderer.split(description, 300);

        int descriptionHeight = descriptionLines.size() * fontRenderer.lineHeight;

        int padding = 8;
        int boxWidth = Math.max(fontRenderer.width(this.selectedBody.getDisplayName()) * 2, 300) + padding * 2;
        int boxHeight = descriptionHeight + 2 * fontRenderer.lineHeight + (this.selectedBody.canTeleportTo() ? 20 + padding : 0) + padding * 2;
        int length = boxHeight + 40;
        int width = 5;
        int sheering = 20;

        float lineRed = 38F / 255F;
        float lineGreen = 30F / 255F;
        float lineBlue = 36F / 255F;
        float lineAlpha = 0.8F;
        float boxRed = 38F / 255F;
        float boxGreen = 30F / 255F;
        float boxBlue = 36F / 255F;
        float boxAlpha = 0.7F;

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(GL_TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        // Line
        builder.vertex(matrix4f, 0, 0, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width, -length, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();
        builder.vertex(matrix4f, sheering, -length, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();
        builder.vertex(matrix4f, 0, 0, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();
        builder.vertex(matrix4f, 1, 0, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width, -length, 0).color(lineRed, lineGreen, lineBlue, lineAlpha).endVertex();

        // Box
        builder.vertex(matrix4f, sheering + width - ((float) boxHeight / (float) length) * (sheering + width - 1), boxHeight - length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width + boxWidth, boxHeight - length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width, -length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width + boxWidth, boxHeight - length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width + boxWidth, -length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();
        builder.vertex(matrix4f, sheering + width, -length, 0).color(boxRed, boxGreen, boxBlue, boxAlpha).endVertex();

        Tesselator.getInstance().end();

        poseStack.translate(width + sheering, -length, 0.0F);
        poseStack.pushPose();
        poseStack.translate(padding, padding, 0);
        poseStack.scale(2F, 2F, 2F);
        fontRenderer.drawShadow(poseStack, this.selectedBody.getDisplayName(), 0, 0, -1);
        poseStack.popPose();
        for (int i = 0; i < descriptionLines.size(); i++)
            fontRenderer.drawShadow(poseStack, descriptionLines.get(i), padding, padding + (i + 2) * fontRenderer.lineHeight, -1);
        poseStack.popPose();

        if (this.selectedBody.canTeleportTo())
        {
            this.launchButton.x = x + width + sheering + padding;
            this.launchButton.y = y + descriptionHeight + 2 * fontRenderer.lineHeight + padding * 2 - length;
            this.launchButton.visible = true;
            this.launchButton.active = !this.travelling && this.selectedBody.getDimension().isPresent() && (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.level.dimension().location().equals(this.selectedBody.getDimension().get()));
        }

        this.bubbleHovered = mouseX >= x + width + sheering && mouseX < x + width + sheering + boxWidth && mouseY >= y - length && mouseY < y - length + boxHeight;
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
        for (GuiEventListener listener : this.children)
            if (listener instanceof TickableWidget)
                ((TickableWidget) listener).tick();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.launchButton.visible = false;
        this.hoveredBody = null;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        if (this.framebuffer == null)
        {
            Window window = Minecraft.getInstance().getWindow();
            this.framebuffer = new RenderTarget((int) (this.width * window.getGuiScale()), (int) (this.height * window.getGuiScale()), true, Minecraft.ON_OSX);
        }
        this.framebuffer.bindWrite(true);

        RenderSystem.matrixMode(GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        Matrix4f projectionMatrix = Matrix4f.perspective(70, (float) this.width / (float) this.height, 0.3F, 10000.0F);
        RenderSystem.multMatrix(projectionMatrix);
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();

        PoseStack matrixStack = new PoseStack();

        GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager._clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        float cameraRotationX = this.camera.getRotationX(partialTicks);
        float cameraRotationY = this.camera.getRotationY(partialTicks);
        matrixStack.mulPose(Vector3f.XN.rotation(cameraRotationX));
        matrixStack.mulPose(Vector3f.YN.rotation(cameraRotationY));

        RenderSystem.depthMask(false);
        this.starsRenderer.render(matrixStack);
        RenderSystem.depthMask(true);

        RenderSystem.enableLighting();
        Lighting.setupLevel(matrixStack.last().pose());
        RenderSystem.disableLighting();

        float cameraX = this.camera.getX(partialTicks);
        float cameraY = this.camera.getY(partialTicks);
        float cameraZ = this.camera.getZ(partialTicks);
        matrixStack.translate(-cameraX, -cameraY, -cameraZ);

        Matrix4f viewMatrix = matrixStack.last().pose().copy();
        Vec3 ray = MousePicker.getRay(projectionMatrix, viewMatrix, (float) (mouseX - this.x) / (float) this.width * 2F - 1F, (float) (mouseY - this.y) / (float) this.height * 2F - 1F);
        Vec3 start = new Vec3(cameraX, cameraY, cameraZ);
        Vec3 end = start.add(ray.multiply(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        this.hoveredBody = this.simulation.clip(start, end, partialTicks).orElse(null);

        MultiBufferSource.BufferSource buffer = BeyondRenderTypes.planetBuffer();
        matrixStack.pushPose();
        this.simulation.getBodies().forEach(body -> this.renderBody(matrixStack, buffer, body, partialTicks));
        matrixStack.popPose();
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

        this.bubbleHovered = false;
        if (this.selectedBody != null)
        {
            Vector4f pos = new Vector4f(this.selectedBody.getX(partialTicks), this.selectedBody.getY(partialTicks), this.selectedBody.getZ(partialTicks), 1.0F);
            pos.transform(viewMatrix);
            pos.transform(projectionMatrix);

            if (Math.abs(pos.z() / pos.w()) <= 1)
            {
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();

                this.renderBubble(poseStack, (int) ((pos.x() / pos.w() + 1F) * this.width / 2F), (int) ((-pos.y() / pos.w() + 1F) * this.height / 2F), mouseX, mouseY);

                RenderSystem.enableDepthTest();
                RenderSystem.enableTexture();
            }
        }

        for (GuiEventListener listener : this.children)
            if (listener instanceof Widget)
                ((Widget) listener).render(poseStack, mouseX, mouseY, partialTicks);

        if (this.isHovered())
            this.renderToolTip(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY)
    {
        if (this.hoveredBody != null && !this.bubbleHovered)
        {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(this.hoveredBody.getBody().getDisplayName());
            if (Minecraft.getInstance().options.advancedItemTooltips)
                tooltip.add(new TextComponent(this.hoveredBody.getBody().getId().toString()).withStyle(ChatFormatting.DARK_GRAY));
            this.parent.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY)
    {
        return this.isMouseOver(mouseX, mouseY) ? ContainerEventHandler.super.getChildAt(mouseX, mouseY) : Optional.empty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (!this.isMouseOver(mouseX, mouseY))
            return false;
        for (GuiEventListener iguieventlistener : this.children())
        {
            if (iguieventlistener.mouseClicked(mouseX, mouseY, mouseButton))
            {
                this.setFocused(iguieventlistener);
                this.setDragging(true);
                return true;
            }
        }

        if (!this.bubbleHovered)
        {
            this.selectedBody = null;
            if (!this.travelling && this.hoveredBody != null && mouseButton == 0)
            {
                this.selectedBody = this.hoveredBody.getBody();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)
    {
        return this.getFocused() != null && this.isDragging() && this.getFocused().mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount) || this.camera.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return this.children;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused()
    {
        return focused;
    }

    @Override
    public boolean isDragging()
    {
        return dragging;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused)
    {
        this.focused = focused;
    }

    @Override
    public void setDragging(boolean dragging)
    {
        this.dragging = dragging;
    }

    @Override
    public void free()
    {
        this.starsRenderer.free();
        this.invalidateFramebuffer();

        for (GuiEventListener listener : this.children)
            if (listener instanceof NativeResource)
                ((NativeResource) listener).free();
        if (!this.travelling)
            BeyondMessages.PLAY.sendToServer(new CPlanetTravelMessage(null, false));
    }

    /**
     * Teleports the player to the body they are supposed to be on.
     */
    public void notifyFailure(ResourceLocation body)
    {
        this.travelling = false;
        this.localRocket.setParent(body);
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
    protected MutableComponent createNarrationMessage()
    {
        return TextComponent.EMPTY.copy();
    }

    /**
     * @return The current simulation for the system
     */
    public CelestialBodySimulation getSimulation()
    {
        return simulation;
    }

    /**
     * @return The camera viewing the scene
     */
    public SpaceTravelCamera getCamera()
    {
        return camera;
    }

    /**
     * @return The local client player rocket
     */
    public PlayerRocketBody getLocalRocket()
    {
        return localRocket;
    }

    /**
     * @return Whether or not the local client is travelling to another planet
     */
    public boolean isTravelling()
    {
        return travelling;
    }

    /**
     * @return The celestial body the mouse is currently over
     */
    public Optional<CelestialBodyRayTraceResult> getHoveredBody()
    {
        return Optional.ofNullable(this.hoveredBody);
    }
}
