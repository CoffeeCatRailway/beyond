package io.github.ocelot.space.common.simulation;

import net.minecraft.client.renderer.model.IBakedModel;

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
    IBakedModel getModel();
}
