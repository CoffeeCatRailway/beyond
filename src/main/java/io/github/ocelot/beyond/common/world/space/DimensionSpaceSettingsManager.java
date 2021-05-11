package io.github.ocelot.beyond.common.world.space;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

/**
 * <p>Manages dimension specific space settings.</p>
 *
 * @author Ocelot
 */
public interface DimensionSpaceSettingsManager
{
    /**
     * Retrieves the settings for the specified dimension.
     *
     * @param dimensionLocation The dimension to get the settings for
     * @return The settings for that dimension
     */
    DimensionSpaceSettings getSettings(ResourceLocation dimensionLocation);

    /**
     * Retrieves the space settings manager for the specified side.
     *
     * @param side The side to get the manager for
     * @return The manager for that side
     */
    static DimensionSpaceSettingsManager get(LogicalSide side)
    {
        return side.isClient() ? ClientDimensionSpaceSettings.INSTANCE : DimensionSpaceSettingsLoader.INSTANCE;
    }
}
