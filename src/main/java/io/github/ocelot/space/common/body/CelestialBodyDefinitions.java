package io.github.ocelot.space.common.body;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO make data driven
public class CelestialBodyDefinitions
{
    public static final Supplier<Map<ResourceLocation, CelestialBody>> SOLAR_SYSTEM;

    static
    {
        {
            SOLAR_SYSTEM = () -> Util.make(new HashMap<>(), bodies ->
            {
                Random random = new Random();
                ResourceLocation sun = create(bodies, "sun", "Sun", "sun", builder ->
                {
                    builder.setDimension(DimensionType.NETHER_LOCATION.location());
                    builder.setScale(20.0F);
                    builder.setShade(false);
                });
                ResourceLocation mercury = create(bodies, "mercury", "Mercury", "mercury", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(3.0F);
                    builder.setDistanceFactor(0.25F);
                    builder.setParent(sun);
                });
                ResourceLocation venus = create(bodies, "venus", "Venus", "venus", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(3.0F);
                    builder.setDistanceFactor(0.5F);
                    builder.setParent(sun);
                });
                ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder ->
                {
                    builder.setDimension(DimensionType.OVERWORLD_LOCATION.location());
                    builder.setScale(4.0F);
                    builder.setDistanceFactor(0.8F);
                    builder.setParent(sun);
                    builder.setAtmosphere(CelestialBodyAtmosphere.builder().setTexture(new ResourceLocation(SpacePrototype.MOD_ID, "earth_clouds")).setDistance(0.0625F).setDensity(0.8F).build());
                });
                ResourceLocation moon = create(bodies, "moon", "Moon", "moon", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(2.0F);
                    builder.setParent(earth);
                });
                ResourceLocation mars = create(bodies, "mars", "Mars", "mars", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(3.5F);
                    builder.setDistanceFactor(1.2F);
                    builder.setParent(sun);
                });
                for (int i = 0; i < 1000; i++)
                {
                    ResourceLocation asteroid = create(bodies, "asteroid" + i, "Asteroid", "asteroid", builder ->
                    {
                        builder.setDimension(DimensionType.END_LOCATION.location());
                        builder.setScale(1.0F + random.nextFloat() - 0.5F);
                        builder.setDistanceFactor(2.0F + random.nextFloat());
                        builder.setParent(sun);
                    });
                }
                ResourceLocation jupiter = create(bodies, "jupiter", "Jupiter", "jupiter", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(12.0F);
                    builder.setDistanceFactor(5.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(CelestialBodyAtmosphere.builder().setTexture(new ResourceLocation(SpacePrototype.MOD_ID, "jupiter")).setDistance(1.0F).setDensity(0.6F).build());
                });
                ResourceLocation saturn = create(bodies, "saturn", "Saturn", "saturn", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(8.0F);
                    builder.setDistanceFactor(6.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(CelestialBodyAtmosphere.builder().setTexture(new ResourceLocation(SpacePrototype.MOD_ID, "saturn")).build());
                });
                for (int i = 0; i < 1000; i++)
                {
                    ResourceLocation ring = create(bodies, "saturn_ring_" + i, "Saturn Ring", "asteroid", builder ->
                    {
                        builder.setDimension(DimensionType.END_LOCATION.location());
                        builder.setScale(0.5F + random.nextFloat() / 2F - 0.25F);
                        builder.setDistanceFactor(0.5F + random.nextFloat() / 2F);
                        builder.setParent(saturn);
                    });
                }
                ResourceLocation uranus = create(bodies, "uranus", "Uranus", "uranus", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(6.0F);
                    builder.setDistanceFactor(7.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(CelestialBodyAtmosphere.builder().setTexture(new ResourceLocation(SpacePrototype.MOD_ID, "uranus")).setDistance(1.2F).setDensity(0.6F).build());
                });
                ResourceLocation neptune = create(bodies, "neptune", "Neptune", "neptune", builder ->
                {
                    builder.setDimension(DimensionType.END_LOCATION.location());
                    builder.setScale(6.0F);
                    builder.setDistanceFactor(8.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(CelestialBodyAtmosphere.builder().setTexture(new ResourceLocation(SpacePrototype.MOD_ID, "neptune")).setDistance(0.8F).setDensity(0.6F).build());
                });
            });
        }
    }

    private static ResourceLocation create(Map<ResourceLocation, CelestialBody> bodies, String name, String displayName, String texture, Consumer<CelestialBody.Builder> consumer)
    {
        ResourceLocation id = new ResourceLocation(SpacePrototype.MOD_ID, name);
        CelestialBody.Builder builder = CelestialBody.builder();
        builder.setTexture(new ResourceLocation(SpacePrototype.MOD_ID, texture));
        builder.setDisplayName(new StringTextComponent(displayName));
        consumer.accept(builder);
        bodies.put(id, builder.build());
        return id;
    }
}
