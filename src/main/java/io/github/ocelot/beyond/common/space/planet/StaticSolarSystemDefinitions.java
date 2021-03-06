package io.github.ocelot.beyond.common.space.planet;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondDimensions;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO make data driven
public class StaticSolarSystemDefinitions
{
    public static final Supplier<Map<ResourceLocation, Planet>> SOLAR_SYSTEM;

    static
    {
        {
            SOLAR_SYSTEM = () -> Util.make(new HashMap<>(), bodies ->
            {
                Random random = new Random();
                ResourceLocation sun = create(bodies, "sun", "Sun", "sun", builder ->
                {
                    builder.setDescription(new TextComponent("The sun is a star, a hot ball of glowing gases at the heart of our solar system. Its influence extends far beyond the orbits of distant Neptune and Pluto."));
                    builder.setDimension(DimensionType.NETHER_LOCATION.location());
                    builder.setScale(20.0F);
                    builder.setShade(false);
                });
                ResourceLocation mercury = create(bodies, "mercury", "Mercury", "mercury", builder ->
                {
                    builder.setDescription(new TextComponent("Mercury is one of the rocky planets. It has a solid surface that is covered with craters. It has no atmosphere, and it doesn't have any moons."));
                    builder.setScale(3.0F);
                    builder.setDistanceFactor(0.25F);
                    builder.setParent(sun);
                });
                ResourceLocation venus = create(bodies, "venus", "Venus", "venus", builder ->
                {
                    builder.setDescription(new TextComponent("Venus is the brightest object in the sky after the Sun and the Moon, and sometimes looks like a bright star in the morning or evening sky."));
                    builder.setScale(3.0F);
                    builder.setShade(false);
                    builder.setDistanceFactor(0.5F);
                    builder.setParent(sun);
                });
                ResourceLocation earth = create(bodies, "earth", "Earth", "earth", builder ->
                {
                    builder.setDescription(new TextComponent("Earth is the planet we live on, one of eight planets in our solar system and the only known place in the universe to support life."));
                    builder.setDimension(DimensionType.OVERWORLD_LOCATION.location());
                    builder.setScale(4.0F);
                    builder.setDistanceFactor(0.8F);
                    builder.setParent(sun);
                    builder.setAtmosphere(PlanetAtmosphere.builder().setTexture(new ResourceLocation(Beyond.MOD_ID, "earth_clouds")).setDistance(0.0625F).setDensity(0.8F).build());
                });
                ResourceLocation moon = create(bodies, "moon", "Moon", "moon", builder ->
                {
                    builder.setDescription(new TextComponent("The moon is Earth's only natural satellite. The moon is a cold, dry orb whose surface is studded with craters and strewn with rocks and dust."));
                    builder.setDimension(BeyondDimensions.MOON.location());
                    builder.setScale(2.0F);
                    builder.setParent(earth);
                });
                ResourceLocation mars = create(bodies, "mars", "Mars", "mars", builder ->
                {
                    builder.setDescription(new TextComponent("Mars is the fourth planet from the Sun and the next planet beyond Earth. It is, on average, more than 142 million miles from the Sun."));
                    builder.setScale(3.5F);
                    builder.setDistanceFactor(1.2F);
                    builder.setParent(sun);
                });
                for (int i = 0; i < 1000; i++)
                {
                    ResourceLocation asteroid = create(bodies, "asteroid" + i, "Asteroid", "asteroid", builder ->
                    {
                        builder.setScale(1.0F + random.nextFloat() - 0.5F);
                        builder.setDistanceFactor(2.0F + random.nextFloat());
                        builder.setParent(sun);
                    });
                }
                ResourceLocation jupiter = create(bodies, "jupiter", "Jupiter", "jupiter", builder ->
                {
                    builder.setDescription(new TextComponent("Jupiter is a gas giant with a mass two and a half times that of all the other planets in the Solar System combined, but less than one-thousandth the mass of the Sun."));
                    builder.setScale(12.0F);
                    builder.setDistanceFactor(5.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(PlanetAtmosphere.builder().setTexture(new ResourceLocation(Beyond.MOD_ID, "jupiter")).setDistance(1.0F).setDensity(0.6F).build());
                });
                ResourceLocation saturn = create(bodies, "saturn", "Saturn", "saturn", builder ->
                {
                    builder.setDescription(new TextComponent("Saturn is a gas giant with an average radius of about nine and a half times that of Earth."));
                    builder.setScale(8.0F);
                    builder.setDistanceFactor(6.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(PlanetAtmosphere.builder().setTexture(new ResourceLocation(Beyond.MOD_ID, "saturn")).build());
                });
                for (int i = 0; i < 1000; i++)
                {
                    ResourceLocation ring = create(bodies, "saturn_ring_" + i, "Saturn Ring", "asteroid", builder ->
                    {
                        builder.setScale(0.5F + random.nextFloat() / 2F - 0.25F);
                        builder.setDistanceFactor(0.5F + random.nextFloat() / 2F);
                        builder.setParent(saturn);
                    });
                }
                ResourceLocation uranus = create(bodies, "uranus", "Uranus", "uranus", builder ->
                {
                    builder.setDescription(new TextComponent("Uranus is an ice giant (instead of a gas giant). It is mostly made of flowing icy materials above a solid core. Uranus has a thick atmosphere made of methane, hydrogen, and helium. Uranus is the only planet that spins on its side."));
                    builder.setScale(6.0F);
                    builder.setDistanceFactor(7.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(PlanetAtmosphere.builder().setTexture(new ResourceLocation(Beyond.MOD_ID, "uranus")).setDistance(1.2F).setDensity(0.6F).build());
                });
                ResourceLocation neptune = create(bodies, "neptune", "Neptune", "neptune", builder ->
                {
                    builder.setDescription(new TextComponent("Neptune, like Uranus, is an ice giant. It's similar to a gas giant. It is made of a thick soup of water, ammonia, and methane flowing over a solid core about the size of Earth. Neptune has a thick, windy atmosphere."));
                    builder.setScale(6.0F);
                    builder.setDistanceFactor(8.0F);
                    builder.setParent(sun);
                    builder.setAtmosphere(PlanetAtmosphere.builder().setTexture(new ResourceLocation(Beyond.MOD_ID, "neptune")).setDistance(0.8F).setDensity(0.6F).build());
                });
            });
        }
    }

    private static ResourceLocation create(Map<ResourceLocation, Planet> bodies, String name, String displayName, String texture, Consumer<Planet.Builder> consumer)
    {
        ResourceLocation id = new ResourceLocation(Beyond.MOD_ID, name);
        Planet.Builder builder = Planet.builder();
        builder.setTexture(new ResourceLocation(Beyond.MOD_ID, texture));
        builder.setDisplayName(new TextComponent(displayName));
        consumer.accept(builder);
        bodies.put(id, builder.build());
        return id;
    }
}
