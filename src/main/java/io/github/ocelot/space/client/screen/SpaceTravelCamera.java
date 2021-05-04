package io.github.ocelot.space.client.screen;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SpaceTravelCamera implements IGuiEventListener
{
    private final Vector3f lastAnchorPos;
    private final Vector3f anchorPos;
    private float lastPitch;
    private float pitch;
    private float lastYaw;
    private float yaw;
    private float lastZoom;
    private float zoom;

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
    }

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
        return mouseButton == 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        this.zoom += amount * 4.0F;
        if (this.zoom > 0)
            this.zoom = 0;
        return true;
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

    public float getX(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastAnchorPos.x(), this.anchorPos.x()) - this.getHorizontalDistance(partialTicks) * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getY(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastAnchorPos.y(), this.anchorPos.y()) + this.getVerticalDistance(partialTicks);
    }

    public float getZ(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastAnchorPos.z(), this.anchorPos.z()) - this.getHorizontalDistance(partialTicks) * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    public float getRotationX(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastPitch, this.pitch);
    }

    public float getRotationY(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastYaw, this.yaw);
    }
}
