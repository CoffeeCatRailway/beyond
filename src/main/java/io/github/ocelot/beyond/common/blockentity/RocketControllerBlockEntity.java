package io.github.ocelot.beyond.common.blockentity;

import com.google.common.base.Stopwatch;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.util.BlockScanner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author Ocelot
 */
public class RocketControllerBlockEntity extends BlockEntity
{
    private static final Logger LOGGER = LogManager.getLogger();

    public RocketControllerBlockEntity()
    {
        super(BeyondBlocks.ROCKET_CONTROLLER_BE.get());
    }

    public void rescan()
    {
        if (this.level == null || this.level.isClientSide())
            return;

        Stopwatch startTime = Stopwatch.createStarted();
        BlockScanner.runScan(this.level, this.getBlockPos(), state -> !state.getBlock().is(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM.get()) && !state.isAir(), 48).thenAcceptAsync(result -> // TODO config for distance
        {
            LOGGER.info("Completed scan in " + startTime);
            if (result.isLimitReached())
                LOGGER.warn("Rocket at {} is too large", this.getBlockPos());
            System.out.println(result.getBlocks().stream().sorted(Comparator.comparing(result::getCount)).map(block -> result.getCount(block) + " " + block.getName().getString()).collect(Collectors.joining(", ")));
        }, this.level.getServer());
    }
}
