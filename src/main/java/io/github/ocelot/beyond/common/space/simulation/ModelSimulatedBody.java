package io.github.ocelot.beyond.common.space.simulation;

import net.minecraft.client.resources.model.BakedModel;

/**
 * <p>Specifies a body defines a custom model.</p>
 *
 * @author Ocelot
 */
public interface ModelSimulatedBody
{
    /**
     * @return The model to use
     */
    BakedModel getModel();
}
