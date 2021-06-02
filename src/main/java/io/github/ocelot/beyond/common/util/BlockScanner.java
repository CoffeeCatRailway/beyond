package io.github.ocelot.beyond.common.util;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * <p>Scans blocks in a level asynchronously.</p>
 *
 * @author Ocelot
 */
public class BlockScanner
{
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
            Map<Block, Set<BlockPos>> blockCounts = new HashMap<>();
            boolean limitReached = false;

            Deque<BlockPos> queue = new LinkedList<>();
            Set<BlockPos> closedSet = new HashSet<>();
            queue.add(pos.immutable());

            while (!queue.isEmpty())
            {
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

            return new Result(blockCounts, limitReached);
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
        private final boolean limitReached;

        private Result(Map<Block, Set<BlockPos>> blockCounts, boolean limitReached)
        {
            this.blockCounts = blockCounts;
            this.limitReached = limitReached;
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
    }
}
