package io.github.ocelot.beyond.common.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
import net.minecraftforge.common.world.StructureSpawnManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class PlanetChunkGenerator extends ChunkGenerator
{
    public static final Codec<PlanetChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource), Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)).apply(instance, instance.stable(PlanetChunkGenerator::new)));

    private static final float[] BEARD_KERNEL = Util.make(new float[13824], p_236094_0_ ->
    {
        for (int i = 0; i < 24; ++i)
        {
            for (int j = 0; j < 24; ++j)
            {
                for (int k = 0; k < 24; ++k)
                {
                    p_236094_0_[i * 24 * 24 + j * 24 + k] = (float) computeContribution(j - 12, k - 12, i - 12);
                }
            }
        }

    });
    private static final float[] BIOME_WEIGHTS = Util.make(new float[25], p_236092_0_ ->
    {
        for (int i = -2; i <= 2; ++i)
        {
            for (int j = -2; j <= 2; ++j)
            {
                float f = 10.0F / Mth.sqrt((float) (i * i + j * j) + 0.2F);
                p_236092_0_[i + 2 + (j + 2) * 5] = f;
            }
        }

    });
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final int chunkHeight;
    private final int chunkWidth;
    private final int chunkCountX;
    private final int chunkCountY;
    private final int chunkCountZ;
    protected final WorldgenRandom random;
    private final PerlinNoise minLimitPerlinNoise;
    private final PerlinNoise maxLimitPerlinNoise;
    private final PerlinNoise mainPerlinNoise;
    private final SurfaceNoise surfaceNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    private final Supplier<NoiseGeneratorSettings> settings;
    private final int height;

    public PlanetChunkGenerator(BiomeSource biomeSource, long p_i241976_3_, Supplier<NoiseGeneratorSettings> settings)
    {
        super(biomeSource, biomeSource, settings.get().structureSettings(), p_i241976_3_);
        this.seed = p_i241976_3_;
        NoiseGeneratorSettings dimensionsettings = settings.get();
        this.settings = settings;
        NoiseSettings noisesettings = dimensionsettings.noiseSettings();
        this.height = noisesettings.height();
        this.chunkHeight = noisesettings.noiseSizeVertical() * 4;
        this.chunkWidth = noisesettings.noiseSizeHorizontal() * 4;
        this.defaultBlock = dimensionsettings.getDefaultBlock();
        this.defaultFluid = dimensionsettings.getDefaultFluid();
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = noisesettings.height() / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
        this.random = new WorldgenRandom(p_i241976_3_);
        this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
        this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
        this.surfaceNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0));
        this.random.consumeCount(2620);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec()
    {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long p_230349_1_)
    {
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(p_230349_1_), p_230349_1_, this.settings);
    }

    private double sampleAndClampNoise(int p_222552_1_, int p_222552_2_, int p_222552_3_, double p_222552_4_, double p_222552_6_, double p_222552_8_, double p_222552_10_)
    {
        double d0 = 0.0;
        double d1 = 0.0;
        double d2 = 0.0;
        double d3 = 4.0;

        for (int i = 0; i < 16; ++i)
        {
            double d4 = PerlinNoise.wrap((double) p_222552_1_ * p_222552_4_ * d3);
            double d5 = PerlinNoise.wrap((double) p_222552_2_ * p_222552_6_ * d3);
            double d6 = PerlinNoise.wrap((double) p_222552_3_ * p_222552_4_ * d3);
            double d7 = p_222552_6_ * d3;
            ImprovedNoise improvednoisegenerator = this.minLimitPerlinNoise.getOctaveNoise(i);
            if (improvednoisegenerator != null)
            {
                d0 += improvednoisegenerator.noise(d4, d5, d6, d7, (double) p_222552_2_ * d7) / d3;
            }

            ImprovedNoise improvednoisegenerator1 = this.maxLimitPerlinNoise.getOctaveNoise(i);
            if (improvednoisegenerator1 != null)
            {
                d1 += improvednoisegenerator1.noise(d4, d5, d6, d7, (double) p_222552_2_ * d7) / d3;
            }

            if (i < 8)
            {
                ImprovedNoise improvednoisegenerator2 = this.mainPerlinNoise.getOctaveNoise(i);
                if (improvednoisegenerator2 != null)
                {
                    d2 += improvednoisegenerator2.noise(PerlinNoise.wrap((double) p_222552_1_ * p_222552_8_ * d3), PerlinNoise.wrap((double) p_222552_2_ * p_222552_10_ * d3), PerlinNoise.wrap((double) p_222552_3_ * p_222552_8_ * d3), p_222552_10_ * d3, (double) p_222552_2_ * p_222552_10_ * d3) / d3;
                }
            }

            d3 /= 2.0;
        }

        return Mth.clampedLerp(d0 / 512.0, d1 / 512.0, (d2 / 10.0 + 1.0) / 2.0);
    }

    private double[] makeAndFillNoiseColumn(int x, int z)
    {
        double[] noiseColumn = new double[this.chunkCountY + 1];
        this.fillNoiseColumn(noiseColumn, x, z);
        return noiseColumn;
    }

    private void fillNoiseColumn(double[] noiseColumn, int x, int z)
    {
        NoiseSettings noisesettings = this.settings.get().noiseSettings();
        double d0;
        double d1;
        float f = 0.0F;
        float f1 = 0.0F;
        float f2 = 0.0F;
        int j = this.getSeaLevel();
        float f3 = this.biomeSource.getNoiseBiome(x, j, z).getDepth();

        for (int k = -2; k <= 2; ++k)
        {
            for (int l = -2; l <= 2; ++l)
            {
                Biome biome = this.biomeSource.getNoiseBiome(x + k, j, z + l);
                float biomeDepth = biome.getDepth();
                float biomeScale = biome.getScale();

                float f8 = biomeDepth > f3 ? 0.5F : 1.0F;
                float f9 = f8 * BIOME_WEIGHTS[k + 2 + (l + 2) * 5] / (biomeDepth + 2.0F);
                f += biomeScale * f9;
                f1 += biomeDepth * f9;
                f2 += f9;
            }
        }

        float f10 = f1 / f2;
        float f11 = f / f2;
        double d16 = f10 * 0.5F - 0.125F;
        double d18 = f11 * 0.9F + 0.1F;
        d0 = d16 * 0.265625;
        d1 = 96.0 / d18;

        double d12 = 684.412 * noisesettings.noiseSamplingSettings().xzScale();
        double d13 = 684.412 * noisesettings.noiseSamplingSettings().yScale();
        double d14 = d12 / noisesettings.noiseSamplingSettings().xzFactor();
        double d15 = d13 / noisesettings.noiseSamplingSettings().yFactor();
        double d17 = noisesettings.topSlideSettings().target();
        double d19 = noisesettings.topSlideSettings().size();
        double d20 = noisesettings.topSlideSettings().offset();
        double d21 = noisesettings.bottomSlideSettings().target();
        double d2 = noisesettings.bottomSlideSettings().size();
        double d3 = noisesettings.bottomSlideSettings().offset();
        double d5 = noisesettings.densityFactor();
        double d6 = noisesettings.densityOffset();

        for (int i1 = 0; i1 <= this.chunkCountY; ++i1)
        {
            double d7 = this.sampleAndClampNoise(x, i1, z, d12, d13, d14, d15);
            double d8 = 1.0 - (double) i1 * 2.0 / (double) this.chunkCountY;
            double d9 = d8 * d5 + d6;
            double d10 = (d9 + d0) * d1;
            if (d10 > 0.0)
            {
                d7 += d10 * 4.0;
            }
            else
            {
                d7 += d10;
            }

            if (d19 > 0.0)
            {
                double d11 = ((double) (this.chunkCountY - i1) - d20) / d19;
                d7 = Mth.clampedLerp(d17, d7, d11);
            }

            if (d2 > 0.0)
            {
                double d22 = ((double) i1 - d3) / d2;
                d7 = Mth.clampedLerp(d21, d7, d22);
            }

            noiseColumn[i1] = d7;
        }

    }

    @Override
    public int getBaseHeight(int p_222529_1_, int p_222529_2_, Heightmap.Types p_222529_3_)
    {
        return this.iterateNoiseColumn(p_222529_1_, p_222529_2_, null, p_222529_3_.isOpaque());
    }

    @Override
    public BlockGetter getBaseColumn(int p_230348_1_, int p_230348_2_)
    {
        BlockState[] ablockstate = new BlockState[this.chunkCountY * this.chunkHeight];
        this.iterateNoiseColumn(p_230348_1_, p_230348_2_, ablockstate, null);
        return new NoiseColumn(ablockstate);
    }

    private int iterateNoiseColumn(int p_236087_1_, int p_236087_2_, @Nullable BlockState[] p_236087_3_, @Nullable Predicate<BlockState> p_236087_4_)
    {
        int i = Math.floorDiv(p_236087_1_, this.chunkWidth);
        int j = Math.floorDiv(p_236087_2_, this.chunkWidth);
        int k = Math.floorMod(p_236087_1_, this.chunkWidth);
        int l = Math.floorMod(p_236087_2_, this.chunkWidth);
        double d0 = (double) k / (double) this.chunkWidth;
        double d1 = (double) l / (double) this.chunkWidth;
        double[][] adouble = new double[][]{this.makeAndFillNoiseColumn(i, j), this.makeAndFillNoiseColumn(i, j + 1), this.makeAndFillNoiseColumn(i + 1, j), this.makeAndFillNoiseColumn(i + 1, j + 1)};

        for (int i1 = this.chunkCountY - 1; i1 >= 0; --i1)
        {
            double d2 = adouble[0][i1];
            double d3 = adouble[1][i1];
            double d4 = adouble[2][i1];
            double d5 = adouble[3][i1];
            double d6 = adouble[0][i1 + 1];
            double d7 = adouble[1][i1 + 1];
            double d8 = adouble[2][i1 + 1];
            double d9 = adouble[3][i1 + 1];

            for (int j1 = this.chunkHeight - 1; j1 >= 0; --j1)
            {
                double d10 = (double) j1 / (double) this.chunkHeight;
                double d11 = Mth.lerp3(d10, d0, d1, d2, d6, d4, d8, d3, d7, d5, d9);
                int k1 = i1 * this.chunkHeight + j1;
                BlockState blockstate = this.generateBaseState(d11, k1);
                if (p_236087_3_ != null)
                {
                    p_236087_3_[k1] = blockstate;
                }

                if (p_236087_4_ != null && p_236087_4_.test(blockstate))
                {
                    return k1 + 1;
                }
            }
        }

        return 0;
    }

    protected BlockState generateBaseState(double p_236086_1_, int p_236086_3_)
    {
        BlockState blockstate;
        if (p_236086_1_ > 0.0)
        {
            blockstate = this.defaultBlock;
        }
        else if (p_236086_3_ < this.getSeaLevel())
        {
            blockstate = this.defaultFluid;
        }
        else
        {
            blockstate = AIR;
        }

        return blockstate;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion p_225551_1_, ChunkAccess p_225551_2_)
    {
        ChunkPos chunkpos = p_225551_2_.getPos();
        int i = chunkpos.x;
        int j = chunkpos.z;
        WorldgenRandom sharedseedrandom = new WorldgenRandom();
        sharedseedrandom.setBaseChunkSeed(i, j);
        ChunkPos chunkpos1 = p_225551_2_.getPos();
        int k = chunkpos1.getMinBlockX();
        int l = chunkpos1.getMinBlockZ();
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int i1 = 0; i1 < 16; ++i1)
        {
            for (int j1 = 0; j1 < 16; ++j1)
            {
                int k1 = k + i1;
                int l1 = l + j1;
                int i2 = p_225551_2_.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i1, j1) + 1;
                double d1 = this.surfaceNoise.getSurfaceNoiseValue((double) k1 * 0.0625, (double) l1 * 0.0625, 0.0625, (double) i1 * 0.0625) * 15.0;
                p_225551_1_.getBiome(blockpos$mutable.set(k + i1, i2, l + j1)).buildSurfaceAt(sharedseedrandom, p_225551_2_, k1, l1, i2, d1, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), p_225551_1_.getSeed());
            }
        }

        this.setBedrock(p_225551_2_, sharedseedrandom);
    }

    private void setBedrock(ChunkAccess p_222555_1_, Random p_222555_2_)
    {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        int i = p_222555_1_.getPos().getMinBlockX();
        int j = p_222555_1_.getPos().getMinBlockZ();
        NoiseGeneratorSettings dimensionsettings = this.settings.get();
        int k = dimensionsettings.getBedrockFloorPosition();
        int l = this.height - 1 - dimensionsettings.getBedrockRoofPosition();
        boolean flag = l + 4 >= 0 && l < this.height;
        boolean flag1 = k + 4 >= 0 && k < this.height;
        if (flag || flag1)
        {
            for (BlockPos blockpos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15))
            {
                if (flag)
                {
                    for (int j1 = 0; j1 < 5; ++j1)
                    {
                        if (j1 <= p_222555_2_.nextInt(5))
                        {
                            p_222555_1_.setBlockState(blockpos$mutable.set(blockpos.getX(), l - j1, blockpos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }

                if (flag1)
                {
                    for (int k1 = 4; k1 >= 0; --k1)
                    {
                        if (k1 <= p_222555_2_.nextInt(5))
                        {
                            p_222555_1_.setBlockState(blockpos$mutable.set(blockpos.getX(), k + k1, blockpos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fillFromNoise(LevelAccessor p_230352_1_, StructureFeatureManager p_230352_2_, ChunkAccess p_230352_3_)
    {
        ObjectList<StructurePiece> objectlist = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> objectlist1 = new ObjectArrayList<>(32);
        ChunkPos chunkpos = p_230352_3_.getPos();
        int i = chunkpos.x;
        int j = chunkpos.z;
        int k = i << 4;
        int l = j << 4;

        for (StructureFeature<?> structure : StructureFeature.NOISE_AFFECTING_FEATURES)
        {
            p_230352_2_.startsForFeature(SectionPos.of(chunkpos, 0), structure).forEach(p_236089_5_ ->
            {
                for (StructurePiece structurepiece1 : p_236089_5_.getPieces())
                {
                    if (structurepiece1.isCloseToChunk(chunkpos, 12))
                    {
                        if (structurepiece1 instanceof PoolElementStructurePiece)
                        {
                            PoolElementStructurePiece abstractvillagepiece = (PoolElementStructurePiece) structurepiece1;
                            StructureTemplatePool.Projection jigsawpattern$placementbehaviour = abstractvillagepiece.getElement().getProjection();
                            if (jigsawpattern$placementbehaviour == StructureTemplatePool.Projection.RIGID)
                            {
                                objectlist.add(abstractvillagepiece);
                            }

                            for (JigsawJunction jigsawjunction1 : abstractvillagepiece.getJunctions())
                            {
                                int l5 = jigsawjunction1.getSourceX();
                                int i6 = jigsawjunction1.getSourceZ();
                                if (l5 > k - 12 && i6 > l - 12 && l5 < k + 15 + 12 && i6 < l + 15 + 12)
                                {
                                    objectlist1.add(jigsawjunction1);
                                }
                            }
                        }
                        else
                        {
                            objectlist.add(structurepiece1);
                        }
                    }
                }

            });
        }

        double[][][] adouble = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

        for (int i5 = 0; i5 < this.chunkCountZ + 1; ++i5)
        {
            adouble[0][i5] = new double[this.chunkCountY + 1];
            this.fillNoiseColumn(adouble[0][i5], i * this.chunkCountX, j * this.chunkCountZ + i5);
            adouble[1][i5] = new double[this.chunkCountY + 1];
        }

        ProtoChunk chunkprimer = (ProtoChunk) p_230352_3_;
        Heightmap heightmap = chunkprimer.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap1 = chunkprimer.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        ObjectListIterator<StructurePiece> objectlistiterator = objectlist.iterator();
        ObjectListIterator<JigsawJunction> objectlistiterator1 = objectlist1.iterator();

        for (int i1 = 0; i1 < this.chunkCountX; ++i1)
        {
            for (int j1 = 0; j1 < this.chunkCountZ + 1; ++j1)
            {
                this.fillNoiseColumn(adouble[1][j1], i * this.chunkCountX + i1 + 1, j * this.chunkCountZ + j1);
            }

            for (int j5 = 0; j5 < this.chunkCountZ; ++j5)
            {
                LevelChunkSection chunksection = chunkprimer.getOrCreateSection(15);
                chunksection.acquire();

                for (int k1 = this.chunkCountY - 1; k1 >= 0; --k1)
                {
                    double d0 = adouble[0][j5][k1];
                    double d1 = adouble[0][j5 + 1][k1];
                    double d2 = adouble[1][j5][k1];
                    double d3 = adouble[1][j5 + 1][k1];
                    double d4 = adouble[0][j5][k1 + 1];
                    double d5 = adouble[0][j5 + 1][k1 + 1];
                    double d6 = adouble[1][j5][k1 + 1];
                    double d7 = adouble[1][j5 + 1][k1 + 1];

                    for (int l1 = this.chunkHeight - 1; l1 >= 0; --l1)
                    {
                        int i2 = k1 * this.chunkHeight + l1;
                        int j2 = i2 & 15;
                        int k2 = i2 >> 4;
                        if (chunksection.bottomBlockY() >> 4 != k2)
                        {
                            chunksection.release();
                            chunksection = chunkprimer.getOrCreateSection(k2);
                            chunksection.acquire();
                        }

                        double d8 = (double) l1 / (double) this.chunkHeight;
                        double d9 = Mth.lerp(d8, d0, d4);
                        double d10 = Mth.lerp(d8, d2, d6);
                        double d11 = Mth.lerp(d8, d1, d5);
                        double d12 = Mth.lerp(d8, d3, d7);

                        for (int l2 = 0; l2 < this.chunkWidth; ++l2)
                        {
                            int i3 = k + i1 * this.chunkWidth + l2;
                            int j3 = i3 & 15;
                            double d13 = (double) l2 / (double) this.chunkWidth;
                            double d14 = Mth.lerp(d13, d9, d10);
                            double d15 = Mth.lerp(d13, d11, d12);

                            for (int k3 = 0; k3 < this.chunkWidth; ++k3)
                            {
                                int l3 = l + j5 * this.chunkWidth + k3;
                                int i4 = l3 & 15;
                                double d16 = (double) k3 / (double) this.chunkWidth;
                                double d17 = Mth.lerp(d16, d14, d15);
                                double d18 = Mth.clamp(d17 / 200.0, -1.0, 1.0);

                                int j4;
                                int k4;
                                int l4;
                                for (d18 = d18 / 2.0 - d18 * d18 * d18 / 24.0; objectlistiterator.hasNext(); d18 += getContribution(j4, k4, l4) * 0.8)
                                {
                                    StructurePiece structurepiece = objectlistiterator.next();
                                    BoundingBox mutableboundingbox = structurepiece.getBoundingBox();
                                    j4 = Math.max(0, Math.max(mutableboundingbox.x0 - i3, i3 - mutableboundingbox.x1));
                                    k4 = i2 - (mutableboundingbox.y0 + (structurepiece instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece) structurepiece).getGroundLevelDelta() : 0));
                                    l4 = Math.max(0, Math.max(mutableboundingbox.z0 - l3, l3 - mutableboundingbox.z1));
                                }

                                objectlistiterator.back(objectlist.size());

                                while (objectlistiterator1.hasNext())
                                {
                                    JigsawJunction jigsawjunction = objectlistiterator1.next();
                                    int k5 = i3 - jigsawjunction.getSourceX();
                                    j4 = i2 - jigsawjunction.getSourceGroundY();
                                    k4 = l3 - jigsawjunction.getSourceZ();
                                    d18 += getContribution(k5, j4, k4) * 0.4;
                                }

                                objectlistiterator1.back(objectlist1.size());
                                BlockState blockstate = this.generateBaseState(d18, i2);
                                if (blockstate != AIR)
                                {
                                    blockpos$mutable.set(i3, i2, l3);
                                    if (blockstate.getLightValue(chunkprimer, blockpos$mutable) != 0)
                                    {
                                        chunkprimer.addLight(blockpos$mutable);
                                    }

                                    chunksection.setBlockState(j3, j2, i4, blockstate, false);
                                    heightmap.update(j3, i2, i4, blockstate);
                                    heightmap1.update(j3, i2, i4, blockstate);
                                }
                            }
                        }
                    }
                }

                chunksection.release();
            }

            double[][] adouble1 = adouble[0];
            adouble[0] = adouble[1];
            adouble[1] = adouble1;
        }

    }

    private static double getContribution(int p_222556_0_, int p_222556_1_, int p_222556_2_)
    {
        int i = p_222556_0_ + 12;
        int j = p_222556_1_ + 12;
        int k = p_222556_2_ + 12;
        if (i >= 0 && i < 24)
        {
            if (j >= 0 && j < 24)
            {
                return k >= 0 && k < 24 ? (double) BEARD_KERNEL[k * 24 * 24 + i * 24 + j] : 0.0;
            }
            else
            {
                return 0.0;
            }
        }
        else
        {
            return 0.0;
        }
    }

    private static double computeContribution(int p_222554_0_, int p_222554_1_, int p_222554_2_)
    {
        double d0 = p_222554_0_ * p_222554_0_ + p_222554_2_ * p_222554_2_;
        double d1 = p_222554_1_ + 0.5;
        double d2 = d1 * d1;
        double d3 = Math.pow(Math.E, -(d2 / 16.0 + d0 / 16.0));
        double d4 = -d1 * Mth.fastInvSqrt(d2 / 2.0 + d0 / 2.0) / 2.0;
        return d4 * d3;
    }

    @Override
    public int getGenDepth()
    {
        return this.height;
    }

    @Override
    public int getSeaLevel()
    {
        return this.settings.get().seaLevel();
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_230353_1_, StructureFeatureManager p_230353_2_, MobCategory p_230353_3_, BlockPos p_230353_4_)
    {
        List<MobSpawnSettings.SpawnerData> spawns = StructureSpawnManager.getStructureSpawns(p_230353_2_, p_230353_3_, p_230353_4_);
        return spawns != null ? spawns : super.getMobsAt(p_230353_1_, p_230353_2_, p_230353_3_, p_230353_4_);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region)
    {
        int i = region.getCenterX();
        int j = region.getCenterZ();
        Biome biome = region.getBiome((new ChunkPos(i, j)).getWorldPosition());
        WorldgenRandom sharedseedrandom = new WorldgenRandom();
        sharedseedrandom.setDecorationSeed(region.getSeed(), i << 4, j << 4);
        NaturalSpawner.spawnMobsForChunkGeneration(region, biome, i, j, sharedseedrandom);
    }
}
