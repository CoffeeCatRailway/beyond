package io.github.ocelot.beyond.common.util;

import com.google.common.base.Suppliers;
import io.github.ocelot.beyond.Beyond;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>Scans blocks in a level asynchronously.</p>
 *
 * @author Ocelot
 */
@Mod.EventBusSubscriber(modid = Beyond.MOD_ID)
public class BlockScanner
{
    private static final long MAX_SCAN_TIME = 10000; // TODO make this a config option

    /**
     * Runs a scan in the specified level starting at the specified position.
     *
     * @param level       The level to check blocks from
     * @param pos         The position to flood from
     * @param filter      The block filter to apply
     * @param maxDistance The maximum distance to go from the starting pos before failing
     * @return A future of the resulting data
     */
    public static CompletableFuture<Result> runScan(BlockGetter level, BlockPos pos, Predicate<BlockState> filter, int maxDistance)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            long startTime = System.currentTimeMillis();
            Map<Block, Set<BlockPos>> blockCounts = new HashMap<>();
            boolean limitReached = false;

            Deque<BlockPos> queue = new LinkedList<>();
            Set<BlockPos> closedSet = new HashSet<>();
            queue.add(pos.immutable());

            while (!queue.isEmpty())
            {
                if (System.currentTimeMillis() - startTime > MAX_SCAN_TIME)
                {
                    return new Result(Collections.emptyMap(), false, true);
                }
                BlockPos offsetPos = queue.remove();
                closedSet.add(offsetPos);
                BlockState state = level.getBlockState(offsetPos);
                if (filter.test(state))
                {
                    blockCounts.computeIfAbsent(state.getBlock(), key -> new HashSet<>()).add(offsetPos);
                    for (Direction direction : Direction.values())
                    {
                        BlockPos p = offsetPos.relative(direction);
                        if (queue.contains(p) || closedSet.contains(p))
                            continue;
                        if (pos.distManhattan(p) > maxDistance)
                        {
                            limitReached = true;
                            continue;
                        }

                        queue.add(p);
                    }
                }
            }

            return new Result(blockCounts, limitReached, false);
        }, Util.backgroundExecutor());
    }

    /**
     * <p>Data resulting from a block scan.</p>
     *
     * @author Ocelot
     */
    public static class Result
    {
        private final Map<Block, Set<BlockPos>> blockCounts;
        private final Supplier<Set<BlockPos>> blockPositions;
        private final boolean limitReached;
        private final boolean timedOut;

        private Result(Map<Block, Set<BlockPos>> blockCounts, boolean limitReached, boolean timedOut)
        {
            this.blockCounts = blockCounts;
            this.blockPositions = Suppliers.memoize(() -> this.blockCounts.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

            this.limitReached = limitReached;
            this.timedOut = timedOut;
        }

        /**
         * @return All unique blocks detected in the scan
         */
        public Set<Block> getBlocks()
        {
            return this.blockCounts.keySet();
        }

        /**
         * Fetches all block positions for the specified block.
         *
         * @param block The block to get positions for
         * @return All positions the specified block is present
         */
        public Set<BlockPos> getBlockPositions(Block block)
        {
            return this.blockCounts.getOrDefault(block, Collections.emptySet());
        }

        /**
         * @return All positions for all blocks
         */
        public Set<BlockPos> getBlockPositions()
        {
            return this.blockPositions.get();
        }

        /**
         * Checks the amount of a single block.
         *
         * @param block The block to get the count of
         * @return The amount of the specified block in the result
         */
        public int getCount(Block block)
        {
            return this.blockCounts.getOrDefault(block, Collections.emptySet()).size();
        }

        /**
         * @return Whether or not the limit was reached and the scan was capped
         */
        public boolean isLimitReached()
        {
            return limitReached;
        }

        /**
         * @return Whether or not it took too much time to scan the area
         */
        public boolean isTimedOut()
        {
            return timedOut;
        }

        /**
         * @return Whether or not the scan was successful
         */
        public boolean isSuccess()
        {
            return !this.limitReached && !this.timedOut;
        }
    }
}
