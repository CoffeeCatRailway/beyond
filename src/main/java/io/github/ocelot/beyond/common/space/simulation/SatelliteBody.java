package io.github.ocelot.beyond.common.space.simulation;

import io.github.ocelot.beyond.common.space.satellite.Satellite;

public interface SatelliteBody<T extends Satellite>
{
    T getSatellite();
}
