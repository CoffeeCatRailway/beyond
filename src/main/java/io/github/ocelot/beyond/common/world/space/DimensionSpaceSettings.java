package io.github.ocelot.beyond.common.world.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

/**
 * <p>Additional parameters for space dimensions. All dimensions without custom properties will be defined by {@link #DEFAULT}.</p>
 *
 * @author Ocelot
 */
public class DimensionSpaceSettings
{
    /**
     * Space settings for all normal overworld dimensions.
     */
    public static final DimensionSpaceSettings DEFAULT = new DimensionSpaceSettings(true, 1.0F);

    public static final Codec<DimensionSpaceSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("oxygenAtmosphere", true).forGetter(DimensionSpaceSettings::isOxygenAtmosphere),
            Codec.DOUBLE.optionalFieldOf("gravityMultiplier", 1.0).forGetter(DimensionSpaceSettings::getGravityMultiplier)
    ).apply(instance, DimensionSpaceSettings::new));

    private final boolean oxygenAtmosphere; // TODO add support for different gasses?
    private final double gravityMultiplier;

    public DimensionSpaceSettings(boolean oxygenAtmosphere, double gravityMultiplier)
    {
        this.oxygenAtmosphere = oxygenAtmosphere;
        this.gravityMultiplier = gravityMultiplier;
    }

    /**
     * Checks whether or not the specified entity requires a space suit to survive.
     *
     * @param entity The entity to check
     * @return Whether or not the entity is fine
     */
    public boolean requiresSpaceSuit(LivingEntity entity)
    {
        return !oxygenAtmosphere; // TODO add additional checks
    }

    /**
     * @return Whether or not the atmosphere can be breathed
     */
    public boolean isOxygenAtmosphere()
    {
        return oxygenAtmosphere;
    }

    /**
     * @return The factor to multiply gravity by. 0.0 is no gravity and 1.0 is regular earth gravity. Higher values will mean less ability to jump
     */
    public double getGravityMultiplier()
    {
        return gravityMultiplier;
    }
}
