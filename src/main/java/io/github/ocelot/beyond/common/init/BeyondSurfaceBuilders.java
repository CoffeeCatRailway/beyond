package io.github.ocelot.beyond.common.init;

import com.google.common.base.Suppliers;
import io.github.ocelot.beyond.Beyond;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

/**
 * @author Ocelot
 */
public class BeyondSurfaceBuilders
{
    public static final Supplier<SurfaceBuilderConfig> CONFIG_MOON = Suppliers.memoize(() -> new SurfaceBuilderConfig(BeyondBlocks.MOON_ROCK.get().defaultBlockState(), BeyondBlocks.MOON_ROCK.get().defaultBlockState(), BeyondBlocks.MOON_ROCK.get().defaultBlockState()));
}
