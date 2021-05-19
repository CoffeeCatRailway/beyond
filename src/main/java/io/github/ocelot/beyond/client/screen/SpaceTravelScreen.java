package io.github.ocelot.beyond.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.client.screen.component.SolarSystemWidget;
import io.github.ocelot.beyond.common.MagicMath;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SUpdateSimulationBodiesMessage;
import io.github.ocelot.beyond.common.space.satellite.Satellite;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.system.NativeResource;

/**
 * @author Ocelot
 */
public class SpaceTravelScreen extends Screen
{
    private static final float ZOOM_SPEED = 0.1F;
    private final SolarSystemWidget solarSystemWidget;

    private byte zooming;
    private float zoomStart;
    private float zoomEnd;
    private float lastZoom;
    private float zoom;

    public SpaceTravelScreen(SOpenSpaceTravelScreenMessage msg)
    {
        super(new TranslatableComponent("screen." + Beyond.MOD_ID + ".space_travel"));
        this.addButton(this.solarSystemWidget = new SolarSystemWidget(this, 0, 0, this.width, this.height, msg));
        this.zoom(false);
    }

    private void zoom(boolean in)
    {
        this.zooming = (byte) (in ? 2 : 1);
        this.zoomStart = in ? -this.solarSystemWidget.getCamera().getZoom(1.0F) : 0;
        this.zoomEnd = in ? 0 : -this.solarSystemWidget.getCamera().getZoom(1.0F);
        this.lastZoom = 0;
        this.zoom = 0;
        this.solarSystemWidget.getCamera().setZoom(this.zoom);
    }

    private void repositionWidgets()
    {
        this.solarSystemWidget.setWidth(this.width);
        this.solarSystemWidget.setHeight(this.height);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height)
    {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = width;
        this.height = height;
        this.repositionWidgets();
    }

    @Override
    public void tick()
    {
        if (this.zooming != 0)
        {
            this.lastZoom = this.zoom;
            this.zoom += ZOOM_SPEED;
            if (this.zoom >= 1.0F)
            {
                if (this.zooming == 2)
                    this.onClose();
                this.solarSystemWidget.getCamera().setInputDisabled(false);
                this.zooming = 0;
            }
        }
        for (GuiEventListener listener : this.children)
            if (listener instanceof TickableWidget)
                ((TickableWidget) listener).tick();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        partialTicks = Minecraft.getInstance().getFrameTime();
        this.renderBackground(poseStack);
        if (this.zooming != 0)
            this.solarSystemWidget.getCamera().setZoom(Mth.lerp(MagicMath.ease(Mth.lerp(partialTicks, this.lastZoom, this.zoom)), this.zoomStart, this.zoomEnd));
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed()
    {
        for (GuiEventListener listener : this.children)
            if (listener instanceof NativeResource)
                ((NativeResource) listener).free();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        for (GuiEventListener iguieventlistener : this.children())
        {
            if (iguieventlistener.mouseClicked(mouseX, mouseY, mouseButton))
            {
                this.setFocused(iguieventlistener);
                this.setDragging(true);
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
    public boolean shouldCloseOnEsc()
    {
        return !this.solarSystemWidget.isTravelling();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    /**
     * Transitions from the gui to the physical world.
     */
    public void transition()
    {
        this.zoom(true);
    }

    /**
     * Teleports the player to the body they are supposed to be on.
     */
    public void notifyFailure(ResourceLocation body)
    {
        this.solarSystemWidget.notifyFailure(body);
    }

    /**
     * Receives updates about the server sided player travel.
     *
     * @param msg The update message
     */
    public void receivePlayerTravel(SPlayerTravelMessage msg)
    {
        PlayerRocketBody body = this.solarSystemWidget.getSimulation().getPlayer(msg.getPlayer());
        if (body != null)
        {
            body.travelTo(msg.getBody());
        }
    }

    /**
     * Receives updates about the server sided simulation.
     *
     * @param msg The update message
     */
    public void receiveSimulationUpdate(SUpdateSimulationBodiesMessage msg)
    {
        CelestialBodySimulation simulation = this.solarSystemWidget.getSimulation();
        for (int removed : msg.getRemoved())
            simulation.removeSatellite(removed);
        for (Satellite satellite : msg.getAddedSatellites())
            simulation.add(satellite.createBody(simulation));
    }
}
