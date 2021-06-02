package io.github.ocelot.beyond.common.blockentity;

import com.google.common.base.Stopwatch;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.util.BlockScanner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        BlockScanner.runScan(this.level, this.getBlockPos(), state -> !state.getBlock().is(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM.get()) && !state.isAir(), 512).thenAcceptAsync(result ->
        {
            LOGGER.debug("Completed scan in " + startTime);
            System.out.println("Rocket Controllers: " + result.getCount(BeyondBlocks.ROCKET_CONTROLLER.get()));
            System.out.println("Thrusters: " + result.getCount(BeyondBlocks.ROCKET_THRUSTER.get()));
        }, this.level.getServer());
    }
}
