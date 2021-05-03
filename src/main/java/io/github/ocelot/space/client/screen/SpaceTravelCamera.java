package io.github.ocelot.space.client.screen;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SpaceTravelCamera
{
    private final Vector3f lastPosition;
    private final Vector3f position;
    private final Vector3f positionDst;
    private final Vector3f lastRotation;
    private final Vector3f rotation;
    private final Vector3f rotationDst;
    private float lastZoom;
    private float zoom;
    private float zoomDst;

    public SpaceTravelCamera()
    {
        this.lastPosition = new Vector3f();
        this.position = new Vector3f();
        this.positionDst = new Vector3f();
        this.lastRotation = new Vector3f();
        this.rotation = new Vector3f();
        this.rotationDst = new Vector3f();
        this.lastZoom = 0;
        this.zoom = 0;
        this.zoomDst = 0;
    }

    public void tick()
    {
        this.lastPosition.set(this.position.x(), this.position.y(), this.position.z());
        this.lastRotation.set(this.rotation.x(), this.rotation.y(), this.rotation.z());
        this.lastZoom = this.zoom;
        this.position.add((this.positionDst.x() - this.position.x()) * 0.25F, (this.positionDst.y() - this.position.y()) * 0.25F, (this.positionDst.z() - this.position.z()) * 0.25F);
        this.rotation.add((this.rotationDst.x() - this.rotation.x()) * 0.25F, (this.rotationDst.y() - this.rotation.y()) * 0.25F, (this.rotationDst.z() - this.rotation.z()) * 0.25F);
        this.zoom += (this.zoomDst * 0.25F - this.zoom);
    }

    public void setPositionDst(float x, float y, float z)
    {
        this.positionDst.set(x, y, z);
    }

    public void setRotationDst(float x, float y, float z)
    {
        this.rotationDst.set(x, y, z);
    }

    public void setZoomDst(float zoom)
    {
        this.zoomDst = zoom;
    }

    public float getPosX(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastPosition.x(), this.position.x());
    }

    public float getPosY(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastPosition.y(), this.position.y());
    }

    public float getPosZ(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastPosition.z(), this.position.z());
    }

    public float getRotationX(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastRotation.x(), this.rotation.x());
    }

    public float getRotationY(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastRotation.y(), this.rotation.y());
    }

    public float getRotationZ(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastRotation.z(), this.rotation.z());
    }

    public float getZoom(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastZoom, this.zoom);
    }
}
