package io.github.ocelot.beyond.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.Random;

/**
 * <p>Large crystals of blocks that form inside caves.</p>
 *
 * @author Ocelot
 */
public class CaveCrystalFeature extends Feature<CaveCrystalConfiguration>
{
    public CaveCrystalFeature(Codec<CaveCrystalConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, Random random, BlockPos pos, CaveCrystalConfiguration config)
    {
        if (pos.getY() >= config.getMaxY() || !config.getPlaceIn().contains(level.getBlockState(pos)))
            return false;

        for (Direction direction : Direction.values())
        {
            if (config.getPlaceOn().contains(level.getBlockState(pos.relative(direction))))
            {
                float length = config.getMinLength() + random.nextInt(config.getMaxLength() - config.getMinLength());

                BlockPos.MutableBlockPos crystalPos = new BlockPos.MutableBlockPos();
                for (int i = 0; i < length; i++)
                {
                    crystalPos.set(pos);
                    crystalPos.move(direction.getOpposite(), i);
                    if (!config.getPlaceIn().contains(level.getBlockState(crystalPos)))
                        return false;
                }
                for (int i = 0; i < length; i++)
                {
                    crystalPos.set(pos);
                    crystalPos.move(direction.getOpposite(), i);
                    level.setBlock(crystalPos, config.getState(), 3);
                }
                return true;
            }
        }
        return false;
    }
}
