package io.github.ocelot.space.common.simulation.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * <p>Defines a layer of texture to draw on top of certain bodies.</p>
 *
 * @author Ocelot
 */
public class CelestialBodyAtmosphere
{
    public static final Codec<CelestialBodyAtmosphere> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(CelestialBodyAtmosphere::getTexture),
            Codec.FLOAT.optionalFieldOf("density", 1.0F).forGetter(CelestialBodyAtmosphere::getDensity),
            Codec.FLOAT.optionalFieldOf("distance", 1.0F).forGetter(CelestialBodyAtmosphere::getDistance)
    ).apply(instance, CelestialBodyAtmosphere::new));

    private final ResourceLocation texture;
    private final float density;
    private final float distance;

    public CelestialBodyAtmosphere(ResourceLocation texture, float density, float distance)
    {
        this.texture = texture;
        this.density = density;
        this.distance = distance;
    }

    /**
     * @return The texture to use for this body
     */
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * @return The transparency of the atmosphere from 0.0 to 1.0
     */
    public float getDensity()
    {
        return density;
    }

    /**
     * @return The distance between the surface of the planet and the atmosphere
     */
    public float getDistance()
    {
        return distance;
    }

    /**
     * @return A new builder for constructing an atmosphere
     */
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * <p>Constructs new bodies.</p>
     *
     * @author Ocelot
     */
    public static class Builder
    {
        private ResourceLocation texture;
        private float density;
        private float distance;

        private Builder()
        {
            this.texture = null;
            this.density = 0.25F;
            this.distance = 0.25F;
        }

        /**
         * Sets the texture of this body.
         *
         * @param texture The texture to use
         */
        public CelestialBodyAtmosphere.Builder setTexture(ResourceLocation texture)
        {
            this.texture = texture;
            return this;
        }

        /**
         * Sets the density of the atmosphere.
         *
         * @param density The transparency of the layer from 0.0 to 1.0
         */
        public CelestialBodyAtmosphere.Builder setDensity(float density)
        {
            this.density = density;
            return this;
        }

        /**
         * Sets the distance the atmosphere should be from the body
         *
         * @param distance The distance from one side of the body to the atmosphere
         */
        public CelestialBodyAtmosphere.Builder setDistance(float distance)
        {
            this.distance = distance;
            return this;
        }

        /**
         * @return A new body with the specified parameters
         */
        public CelestialBodyAtmosphere build()
        {
            Validate.notNull(this.texture);
            return new CelestialBodyAtmosphere(this.texture, this.density, this.distance);
        }
    }
}
