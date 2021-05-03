package io.github.ocelot.space.client.screen;

import net.minecraft.util.math.vector.Vector3f;

public class SpaceTravelCamera
{
    private final Vector3f lastPosition;
    private final Vector3f position;
    private final Vector3f positionDst;
    private final Vector3f lastRotation;
    private final Vector3f rotation;
    private final Vector3f rotationDst;
    private final Vector3f lastZoom;
    private final Vector3f zoom;
    private final Vector3f zoomDst;

    public SpaceTravelCamera()
    {
        this.lastPosition = new Vector3f();
        this.position = new Vector3f();
        this.positionDst = new Vector3f();
        this.lastRotation = new Vector3f();
        this.rotation = new Vector3f();
        this.rotationDst = new Vector3f();
        this.lastZoom = new Vector3f(1, 1, 1);
        this.zoom = new Vector3f(1, 1, 1);
        this.zoomDst = new Vector3f(1, 1, 1);
    }

    public void tick()
    {
        this.lastPosition.set(this.position.x(), this.position.y(), this.position.z());
        this.lastRotation.set(this.rotation.x(), this.rotation.y(), this.rotation.z());
        this.lastZoom.set(this.zoom.x(), this.zoom.y(), this.zoom.z());
        this.position.set(this.position.x() + this.positionDst.x() * 0.25F, this.position.y() + this.positionDst.y() * 0.25F, this.position.z() + this.positionDst.z() * 0.25F);
        this.rotation.set(this.rotation.x() + this.rotationDst.x() * 0.25F, this.rotation.y() + this.rotationDst.y() * 0.25F, this.rotation.z() + this.rotationDst.z() * 0.25F);
        this.zoom.set(this.zoom.x() + this.zoomDst.x() * 0.25F, this.zoom.y() + this.zoomDst.y() * 0.25F, this.zoom.z() + this.zoomDst.z() * 0.25F);
    }

    public void setPositionDst(float x, float y, float z)
    {
        this.positionDst.set(x, y, z);
    }

    public void setRotationDst(float x, float y, float z)
    {
        this.rotationDst.set(x, y, z);
    }

    public void setZoomDst(float x, float y, float z)
    {
        this.zoomDst.set(x, y, z);
    }
}
