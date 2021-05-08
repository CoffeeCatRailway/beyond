package io.github.ocelot.beyond.common.init;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.world.carver.MoonCaveCarver;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ICarverConfig;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class BeyondWorldCarvers
{
    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, Beyond.MOD_ID);

    public static final RegistryObject<WorldCarver<ProbabilityConfig>> MOON_CAVE = CARVERS.register("moon_cave", () -> new MoonCaveCarver(ProbabilityConfig.CODEC));
    public static final Supplier<ConfiguredCarver<ProbabilityConfig>> MOON = register("moon", () -> MOON_CAVE.get().configured(new ProbabilityConfig(0.14285715F)));

    private static <WC extends ICarverConfig> Supplier<ConfiguredCarver<WC>> register(String name, Supplier<ConfiguredCarver<WC>> carver)
    {
        return () -> WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_CARVER, name, carver.get());
    }
}
