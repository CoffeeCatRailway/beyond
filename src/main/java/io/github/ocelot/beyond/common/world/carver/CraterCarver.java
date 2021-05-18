package io.github.ocelot.beyond.common.world.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.MagicMath;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

/**
 * <p>Carves a crater out of the terrain.</p>
 *
 * @author Ocelot
 */
public class CraterCarver extends WorldCarver<CraterConfig>
{
    public CraterCarver(Codec<CraterConfig> codec)
    {
        super(codec, 256);
        this.replaceableBlocks = ImmutableSet.of(BeyondBlocks.MOON_ROCK.get());
        this.liquids = ImmutableSet.of();
    }

    @Override
    public boolean carve(ChunkAccess chunk, Function<BlockPos, Biome> posToBiome, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, CraterConfig config)
    {
        float radius = config.getRadius().getRandomValue(random);
        float rimWidth = config.getRimWidth().getRandomValue(random);
        float rimSteepness = config.getRimSteepness().getRandomValue(random);
        float floorHeight = config.getFloorHeight().getRandomValue(random);
        float smoothness = config.getSmoothness();

        BlockPos centerPos = new BlockPos(mainChunkX * 16 + random.nextInt(16), 0, mainChunkZ * 16 + random.nextInt(16)); // TODO make this randomized in the chunk
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int xp = 0; xp < 16; xp++)
        {
            for (int zp = 0; zp < 16; zp++)
            {
                pos.set(xp, 0, zp);

                float xd = (chunkX * 16 + 15 - xp) - centerPos.getX();
                float zd = (chunkZ * 16 + 15 - zp) - centerPos.getZ();
                float x = Mth.sqrt(xd * xd + zd * zd) / radius;

                float cavity = x * x - 1;
                float rimX = Math.min(x - 1 - rimWidth, 0);
                float rim = rimSteepness * rimX * rimX;

                float craterShape = MagicMath.smoothMin(cavity, floorHeight, -smoothness);
                craterShape = MagicMath.smoothMin(craterShape, rim, smoothness);
                float y = craterShape * radius;
                int blockY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xp, zp) + 1;

                if ((int) y == 0 && x > rimWidth)
                    continue;

                int startY = Mth.floor(blockY + y);
                if (startY < blockY)
                {
                    for (int i = startY; i <= blockY + 4; i++)
                        chunk.setBlockState(pos.set(pos.getX(), i, pos.getZ()), Blocks.AIR.defaultBlockState(), false);
                }
                else
                {
                    BlockState state = chunk.getBlockState(pos.set(pos.getX(), blockY - 1, pos.getZ()));
                    if (state.isAir())
                        state = config.getSurfaceBlock();
                    for (int i = blockY; i < startY; i++)
                        chunk.setBlockState(pos.set(pos.getX(), i, pos.getZ()), state, false);
                }
                chunk.setBlockState(pos.set(pos.getX(), blockY + y - 1, pos.getZ()), config.getSurfaceBlock(), false);
            }
        }

        return true;
    }

    @Override
    public boolean isStartChunk(Random random, int chunkX, int chunkZ, CraterConfig config)
    {
        return random.nextFloat() <= config.probability;
    }

    @Override
    protected boolean skip(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y)
    {
        return scaledRelativeY <= -0.7D || scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0D;
    }
}
