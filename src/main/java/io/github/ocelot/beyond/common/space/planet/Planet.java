package io.github.ocelot.beyond.common.space.planet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * <p>A single body in the solar system.</p>
 *
 * @author Ocelot
 */
public class Planet
{
    public static final Codec<Planet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Planet::getParent),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(Planet::getTexture),
            ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(Planet::getDimension),
            Codec.STRING.fieldOf("displayName").<ITextComponent>xmap(ITextComponent.Serializer::fromJson, ITextComponent.Serializer::toJson).forGetter(Planet::getDisplayName),
            Codec.BOOL.optionalFieldOf("shade", true).forGetter(Planet::isShade),
            Codec.FLOAT.optionalFieldOf("size", 1.0F).forGetter(Planet::getSize),
            Codec.FLOAT.optionalFieldOf("distanceFactor", 1.0F).forGetter(Planet::getDistanceFactor),
            PlanetAtmosphere.CODEC.optionalFieldOf("atmosphere").forGetter(Planet::getAtmosphere)
    ).apply(instance, (parent, texture, dimension, displayName, shade, scale, distanceFactor, atmosphere) -> new Planet(parent.orElse(null), texture, dimension.orElse(null), displayName, shade, scale, distanceFactor, atmosphere.orElse(null))));

    private final ResourceLocation parent;
    private final ResourceLocation texture;
    private final ResourceLocation dimension;
    private final ITextComponent displayName;
    private final boolean shade;
    private final float size;
    private final float distanceFactor;
    private final PlanetAtmosphere atmosphere;

    public Planet(@Nullable ResourceLocation parent, ResourceLocation texture, @Nullable ResourceLocation dimension, ITextComponent displayName, boolean shade, float size, float distanceFactor, PlanetAtmosphere atmosphere)
    {
        this.parent = parent;
        this.texture = texture;
        this.dimension = dimension;
        this.displayName = displayName;
        this.shade = shade;
        this.size = size;
        this.distanceFactor = distanceFactor;
        this.atmosphere = atmosphere;
    }

    /**
     * @return The parent of this body
     */
    public Optional<ResourceLocation> getParent()
    {
        return Optional.ofNullable(this.parent);
    }

    /**
     * @return The sprite to use for the cube
     */
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * @return The dimension to teleport to
     */
    public Optional<ResourceLocation> getDimension()
    {
        return Optional.ofNullable(this.dimension);
    }

    /**
     * @return The display name of this body
     */
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    /**
     * @return Whether or not this body should have lighting
     */
    public boolean isShade()
    {
        return shade;
    }

    /**
     * @return The scale of the body
     */
    public float getSize()
    {
        return size;
    }

    /**
     * @return The additional modifier to the distance from the parent
     */
    public float getDistanceFactor()
    {
        return distanceFactor;
    }

    /**
     * @return The atmosphere to show around this body
     */
    public Optional<PlanetAtmosphere> getAtmosphere()
    {
        return Optional.ofNullable(this.atmosphere);
    }

    /**
     * @return A new builder for a celestial body
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
        private ResourceLocation parent;
        private ResourceLocation texture;
        private ResourceLocation dimension;
        private ITextComponent displayName;
        private boolean shade;
        private float scale;
        private float distanceFactor;
        private PlanetAtmosphere atmosphere;

        private Builder()
        {
            this.parent = null;
            this.texture = null;
            this.dimension = null;
            this.displayName = null;
            this.shade = true;
            this.scale = 1.0F;
            this.distanceFactor = 1.0F;
            this.atmosphere = null;
        }

        /**
         * Sets the parent for this body.
         *
         * @param parent The body to orbit around or <code>null</code> to stay stationary
         */
        public Builder setParent(@Nullable ResourceLocation parent)
        {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the texture of this body.
         *
         * @param texture The texture to use
         */
        public Builder setTexture(ResourceLocation texture)
        {
            this.texture = texture;
            return this;
        }

        /**
         * Sets the dimension to teleport to.
         *
         * @param dimension The dimension to teleport to
         */
        public Builder setDimension(@Nullable ResourceLocation dimension)
        {
            this.dimension = dimension;
            return this;
        }

        /**
         * Sets the display name of this body.
         *
         * @param displayName The name to use
         */
        public Builder setDisplayName(ITextComponent displayName)
        {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets whether or not this body should have lighting.
         *
         * @param shade Whether or not shading should be applied to the body
         */
        public void setShade(boolean shade)
        {
            this.shade = shade;
        }

        /**
         * Sets the scale to draw the body at.
         *
         * @param scale The scale factor for the body
         */
        public Builder setScale(float scale)
        {
            this.scale = scale;
            return this;
        }

        /**
         * Sets the extra distance to go away from the parent body
         *
         * @param distanceFactor The additional modifier to the distance from the parent body
         */
        public Builder setDistanceFactor(float distanceFactor)
        {
            this.distanceFactor = distanceFactor;
            return this;
        }

        /**
         * Sets the atmosphere to render around the body.
         *
         * @param atmosphere The atmosphere to show around the body or <code>null</code> for none
         */
        public Builder setAtmosphere(PlanetAtmosphere atmosphere)
        {
            this.atmosphere = atmosphere;
            return this;
        }

        /**
         * @return A new body with the specified parameters
         */
        public Planet build()
        {
            Validate.notNull(this.texture);
            Validate.notNull(this.displayName);
            return new Planet(this.parent, this.texture, this.dimension, this.displayName, this.shade, this.scale, this.distanceFactor, this.atmosphere);
        }
    }
}
