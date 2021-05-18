package io.github.ocelot.beyond.client.world;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * @author Ocelot
 */
public class SpaceRenderInfo extends DimensionSpecialEffects
{
    public SpaceRenderInfo(float cloudLevel, boolean hasGround, boolean forceBrightLightmap, boolean constantAmbientLight)
    {
        super(cloudLevel, hasGround, SkyType.NONE, forceBrightLightmap, constantAmbientLight);
        this.setSkyRenderHandler(new SpaceSkyRenderer());
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight)
    {
        return color;
    }

    @Override
    public boolean isFoggyAt(int camX, int camZ)
    {
        return false;
    }

    @Nullable
    public float[] getSunriseColor(float dayTime, float partialTicks)
    {
        return null;
    }
}
