package io.github.ocelot.beyond.client.world;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Ocelot
 */
public class SpaceRenderInfo extends DimensionRenderInfo
{
    public SpaceRenderInfo(float cloudLevel, boolean hasGround, boolean forceBrightLightmap, boolean constantAmbientLight)
    {
        super(cloudLevel, hasGround, FogType.NONE, forceBrightLightmap, constantAmbientLight);
        this.setSkyRenderHandler(new SpaceSkyRenderer());
    }

    @Override
    public Vector3d getBrightnessDependentFogColor(Vector3d color, float sunHeight)
    {
        return color;
    }

    @Override
    public boolean isFoggyAt(int camX, int camZ)
    {
        return false;
    }
}
