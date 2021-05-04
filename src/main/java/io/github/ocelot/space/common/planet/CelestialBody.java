package io.github.ocelot.space.common.planet;

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
public class CelestialBody
{
    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(CelestialBody::getParent),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(CelestialBody::getTexture),
            Codec.STRING.fieldOf("displayName").<ITextComponent>xmap(ITextComponent.Serializer::fromJson, ITextComponent.Serializer::toJson).forGetter(CelestialBody::getDisplayName),
            Codec.BOOL.optionalFieldOf("shade", true).forGetter(CelestialBody::isShade),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(CelestialBody::getScale)
    ).apply(instance, (parent, texture, displayName, shade, scale) -> new CelestialBody(parent.orElse(null), texture, displayName, shade, scale)));

    private final ResourceLocation parent;
    private final ResourceLocation texture;
    private final ITextComponent displayName;
    private final boolean shade;
    private final float scale;

    public CelestialBody(@Nullable ResourceLocation parent, ResourceLocation texture, ITextComponent displayName, boolean shade, float scale)
    {
        this.parent = parent;
        this.texture = texture;
        this.displayName = displayName;
        this.shade = shade;
        this.scale = scale;
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
    public float getScale()
    {
        return scale;
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
        private ITextComponent displayName;
        private boolean shade;
        private float scale;

        private Builder()
        {
            this.parent = null;
            this.texture = null;
            this.displayName = null;
            this.shade = true;
            this.scale = 1.0F;
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
         * @return A new body with the specified parameters
         */
        public CelestialBody build()
        {
            Validate.notNull(this.texture);
            Validate.notNull(this.displayName);
            return new CelestialBody(this.parent, this.texture, this.displayName, this.shade, this.scale);
        }
    }
}
