package io.github.ocelot.space.client.screen;

import io.github.ocelot.space.common.simulation.body.SimulatedBody;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

/**
 * <p>A custom camera implementation for the space simulation.</p>
 *
 * @author Ocelot
 */
public class SpaceTravelCamera implements IScreen, IGuiEventListener
{
    private final Vector3f lastAnchorPos;
    private final Vector3f anchorPos;
    private float lastPitch;
    private float pitch;
    private float lastYaw;
    private float yaw;
    private float lastZoom;
    private float zoom;
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
        this.focused = null;
        this.inputDisabled = false;
    }

    @Override
    public void tick()
    {
        this.lastAnchorPos.set(this.lastAnchorPos.x(), this.lastAnchorPos.y(), this.lastAnchorPos.z());
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        this.lastZoom = this.zoom;
    }

    private float getHorizontalDistance(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastZoom, this.zoom) * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastPitch, this.pitch));
    }

    private float getVerticalDistance(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastZoom, this.zoom) * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastPitch, this.pitch));
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
            this.pitch = MathHelper.clamp(this.pitch, -(float) Math.PI / 2F, (float) Math.PI / 2F);
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
        this.zoom += amount * 4.0F;
        if (this.zoom > 0)
            this.zoom = 0;
        return true;
    }

    public float getX(float partialTicks)
    {
        float anchorX = this.focused != null ? this.focused.getX(partialTicks) : MathHelper.lerp(partialTicks, this.lastAnchorPos.x(), this.anchorPos.x());
        return anchorX - this.getHorizontalDistance(partialTicks) * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getY(float partialTicks)
    {
        float anchorY = this.focused != null ? this.focused.getY(partialTicks) : MathHelper.lerp(partialTicks, this.lastAnchorPos.y(), this.anchorPos.y());
        return anchorY + this.getVerticalDistance(partialTicks);
    }

    public float getZ(float partialTicks)
    {
        float anchorZ = this.focused != null ? this.focused.getZ(partialTicks) : MathHelper.lerp(partialTicks, this.lastAnchorPos.z(), this.anchorPos.z());
        return anchorZ - this.getHorizontalDistance(partialTicks) * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getRotationX(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastPitch, this.pitch);
    }

    public float getRotationY(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastYaw, this.yaw);
    }

    public float getZoom(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastZoom, this.zoom);
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
