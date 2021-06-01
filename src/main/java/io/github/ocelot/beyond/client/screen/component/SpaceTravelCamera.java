package io.github.ocelot.beyond.client.screen.component;

import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * <p>A custom camera implementation for the space simulation.</p>
 *
 * @author Ocelot
 */
public class SpaceTravelCamera implements TickableWidget, GuiEventListener
{
    private static final float ZOOM_TRANSITION_SPEED = 0.35F;

    private final Vector3f lastAnchorPos;
    private final Vector3f anchorPos;
    private float lastPitch;
    private float pitch;
    private float lastYaw;
    private float yaw;
    private float lastZoom;
    private float zoom;
    private float zoomSpeed;
    private SimulatedBody focused;
    private boolean inputDisabled;

    public SpaceTravelCamera()
    {
        this.lastAnchorPos = new Vector3f();
        this.anchorPos = new Vector3f();
        this.lastPitch = 0;
        this.pitch = 0;
        this.lastYaw = 0;
        this.yaw = 0;
        this.lastZoom = 0;
        this.zoom = 0;
        this.zoomSpeed = 0;
        this.focused = null;
        this.inputDisabled = false;
    }

    @Override
    public void tick()
    {
        this.lastAnchorPos.set(this.anchorPos.x(), this.anchorPos.y(), this.anchorPos.z());
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        this.lastZoom = this.zoom;

        if (!this.inputDisabled)
        {
            this.zoom += this.zoomSpeed;
            if (this.zoom > 0)
            {
                this.zoom = 0;
                this.zoomSpeed = 0;
            }

            if (this.zoomSpeed > 0)
            {
                this.zoomSpeed *= ZOOM_TRANSITION_SPEED;
                if (this.zoomSpeed < 0)
                    this.zoomSpeed = 0;
            }
            if (this.zoomSpeed < 0)
            {
                this.zoomSpeed *= ZOOM_TRANSITION_SPEED;
                if (this.zoomSpeed > 0)
                    this.zoomSpeed = 0;
            }
        }
    }

    private double getDistance(float partialTicks)
    {
        return -Math.pow(1.02, -Mth.lerp(partialTicks, this.lastZoom, this.zoom));
    }

    private float getHorizontalDistance(float partialTicks)
    {
        return (float) (this.getDistance(partialTicks) * Math.cos(Mth.lerp(partialTicks, this.lastPitch, this.pitch)));
    }

    private float getVerticalDistance(float partialTicks)
    {
        return (float) (this.getDistance(partialTicks) * Math.sin(Mth.lerp(partialTicks, this.lastPitch, this.pitch)));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)
    {
        if (this.inputDisabled)
            return false;
        if (mouseButton == 1)
        {
            this.yaw -= dx / 180F;
            this.pitch -= dy / 180F;
            this.pitch = Mth.clamp(this.pitch, -(float) Math.PI / 2F, (float) Math.PI / 2F);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        return !this.inputDisabled && mouseButton == 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        if (this.inputDisabled)
            return false;
        this.zoomSpeed += amount * 4.0F;
        return true;
    }

    public float getX(float partialTicks)
    {
        float anchorX = this.focused != null ? this.focused.getX(partialTicks) : Mth.lerp(partialTicks, this.lastAnchorPos.x(), this.anchorPos.x());
        return anchorX - this.getHorizontalDistance(partialTicks) * Mth.sin(Mth.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getY(float partialTicks)
    {
        float anchorY = this.focused != null ? this.focused.getY(partialTicks) : Mth.lerp(partialTicks, this.lastAnchorPos.y(), this.anchorPos.y());
        return anchorY + this.getVerticalDistance(partialTicks);
    }

    public float getZ(float partialTicks)
    {
        float anchorZ = this.focused != null ? this.focused.getZ(partialTicks) : Mth.lerp(partialTicks, this.lastAnchorPos.z(), this.anchorPos.z());
        return anchorZ - this.getHorizontalDistance(partialTicks) * Mth.cos(Mth.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getRotationX(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastPitch, this.pitch);
    }

    public float getRotationY(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastYaw, this.yaw);
    }

    public float getZoom(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.lastZoom, this.zoom);
    }

    public void setPosition(float x, float y, float z)
    {
        this.anchorPos.set(x, y, z);
    }

    public void setPitch(float pitch)
    {
        this.lastPitch = -pitch;
        this.pitch = -pitch;
    }

    public void setYaw(float yaw)
    {
        this.lastYaw = -yaw;
        this.yaw = -yaw;
    }

    public void setZoom(float zoom)
    {
        this.lastZoom = -zoom;
        this.zoom = -zoom;
        this.zoomSpeed = 0;
    }

    /**
     * Sets the body the camera should follow or <code>null</code> to switch back to the local anchor.
     *
     * @param focused The body to focus onto
     */
    // TODO add animation
    public void setFocused(@Nullable SimulatedBody focused)
    {
        this.focused = focused;
    }

    /**
     * Sets whether or not input should be disabled for the camera.
     *
     * @param inputDisabled Whether or not to disable input
     */
    public void setInputDisabled(boolean inputDisabled)
    {
        this.inputDisabled = inputDisabled;
    }
}
