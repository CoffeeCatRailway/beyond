package io.github.ocelot.space.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.screen.component.SolarSystemWidget;
import io.github.ocelot.space.common.MagicMath;
import io.github.ocelot.space.common.init.SpaceMessages;
import io.github.ocelot.space.common.network.play.message.CPlanetTravelMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
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

    public SpaceTravelScreen()
    {
        super(new TranslationTextComponent("screen." + SpacePrototype.MOD_ID + ".space_travel"));
        this.addButton(this.solarSystemWidget = new SolarSystemWidget(this, 0, 0, this.width, this.height));
        this.zoom(false);
    }

    private void zoom(boolean in)
    {
        this.zooming = (byte) (in ? -1 : 1);
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
                if (this.zooming == -1)
                    this.onClose();
                this.solarSystemWidget.getCamera().setInputDisabled(false);
                this.zooming = 0;
            }
        }
        for (IGuiEventListener listener : this.children)
            if (listener instanceof IScreen)
                ((IScreen) listener).tick();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        partialTicks = Minecraft.getInstance().getFrameTime();
        this.renderBackground(poseStack);
        if (this.zooming != 0)
            this.solarSystemWidget.getCamera().setZoom(MathHelper.lerp(MagicMath.ease(MathHelper.lerp(partialTicks, this.lastZoom, this.zoom)), this.zoomStart, this.zoomEnd));
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed()
    {
        for (IGuiEventListener listener : this.children)
            if (listener instanceof NativeResource)
                ((NativeResource) listener).free();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        for (IGuiEventListener iguieventlistener : this.children())
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
    public boolean keyPressed(int keyCode, int scanCode, int mods)
    {
        if (keyCode == 256)
            SpaceMessages.PLAY.sendToServer(new CPlanetTravelMessage(null));
        return super.keyPressed(keyCode, scanCode, mods);
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
}
