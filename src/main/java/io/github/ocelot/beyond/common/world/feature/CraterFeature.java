package io.github.ocelot.beyond.common.world.feature;

import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.common.MagicMath;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

/**
 * @author Ocelot
 * TODO make carver so craters can be larger
 */
public class CraterFeature extends Feature<BlockStateFeatureConfig>
{
    public CraterFeature(Codec<BlockStateFeatureConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos centerPos, BlockStateFeatureConfig config)
    {
        float radius = MagicMath.bias(random.nextFloat(), 0.8F) * 6.0F;
        if (radius <= 3)
            return false;

        float rimWidth = 1.2F;
        float rimSteepness = 0.4F;
        float floorHeight = -random.nextFloat() * 0.5F - 0.5F;
        float smoothness = 0.34F;
        int min = -MathHelper.floor(2 * radius);
        int max = MathHelper.ceil(2 * radius);

        BlockPos.Mutable pos = centerPos.mutable();
        for (int xp = min; xp < max; xp++)
        {
            for (int zp = min; zp < max; zp++)
            {
                float x = MathHelper.sqrt(xp * xp + zp * zp) / radius;

                float cavity = x * x - 1;
                float rimX = Math.min(x - 1 - rimWidth, 0);
                float rim = rimSteepness * rimX * rimX;

                float craterShape = MagicMath.smoothMin(cavity, floorHeight, -smoothness);
                craterShape = MagicMath.smoothMin(craterShape, rim, smoothness);
                float y = craterShape * radius;
                int blockY = world.getHeight(Heightmap.Type.WORLD_SURFACE, xp + centerPos.getX(), zp + centerPos.getZ());

                if ((int) y == 0 && x > 0.6F)
                    continue;

                int startY = MathHelper.floor(blockY + y);
                if (startY < blockY)
                {
                    for (int i = startY; i <= blockY + 4; i++)
                        this.setBlock(world, pos.set(xp + centerPos.getX(), i, zp + centerPos.getZ()), Blocks.AIR.defaultBlockState());
                }
                else
                {
                    BlockState state = world.getBlockState(pos.set(xp + centerPos.getX(), blockY - 1, zp + centerPos.getZ()));
                    if (state.isAir())
                        state = config.state;
                    for (int i = blockY; i < startY; i++)
                        this.setBlock(world, pos.set(xp + centerPos.getX(), i, zp + centerPos.getZ()), state);
                }
                this.setBlock(world, pos.set(xp + centerPos.getX(), blockY + y - 1, zp + centerPos.getZ()), config.state);
            }
        }
        return true;
    }
}
