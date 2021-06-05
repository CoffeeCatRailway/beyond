package io.github.ocelot.beyond.common.blockentity;

import com.google.common.base.Stopwatch;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.block.RocketControllerBlock;
import io.github.ocelot.beyond.common.entity.RocketEntity;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.rocket.LaunchContext;
import io.github.ocelot.beyond.common.rocket.RocketComponent;
import io.github.ocelot.beyond.common.rocket.RocketThruster;
import io.github.ocelot.beyond.common.util.BlockScanner;
import io.github.ocelot.sonar.common.tileentity.BaseTileEntity;
import io.github.ocelot.sonar.common.util.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Ocelot
 */
public class RocketControllerBlockEntity extends BaseTileEntity implements TickableBlockEntity
{
    private static final long LAUNCH_TIME = 5; // TODO config in seconds

    private static final Logger LOGGER = LogManager.getLogger();
    private CompletableFuture<BlockScanner.Result> runningScan;

    private final Map<BlockPos, RocketComponent> components;

    private Future<?> launchFuture;
    @OnlyIn(Dist.CLIENT)
    private boolean launching;

    public RocketControllerBlockEntity()
    {
        super(BeyondBlocks.ROCKET_CONTROLLER_BE.get());
        this.runningScan = CompletableFuture.completedFuture(null);
        this.components = new HashMap<>();
        this.launchFuture = null;
    }

    // TODO send errors using a screen block instead of chat
    private void sendError(@Nullable CommandSource source, MutableComponent error)
    {
        Objects.requireNonNull(this.level).setBlock(this.getBlockPos(), this.getBlockState().setValue(RocketControllerBlock.STATE, RocketControllerBlock.State.ERROR), Constants.BlockFlags.DEFAULT);
        LOGGER.warn(error.getString());
        if (source != null && source.acceptsFailure())
            source.sendMessage(error.withStyle(ChatFormatting.RED), Util.NIL_UUID);
    }

    private void cancelLaunch()
    {
        if (this.launchFuture != null)
        {
            this.launchFuture.cancel(false);
            this.launchFuture = null;
            Objects.requireNonNull(this.level).sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.DEFAULT);
        }
    }

    private void launch(LaunchContext ctx, BlockPos min, BlockPos max)
    {
        this.cancelLaunch();
        if (this.level == null)
            return;

        System.out.println("Launched!");

        Map<UUID, Vec3> playerPositions = new HashMap<>();
        for (Player player : this.level.getEntitiesOfClass(Player.class, new AABB(min, max.offset(1, 1, 1)).inflate(4), EntitySelector.NO_SPECTATORS))
        {
            // TODO check if player is above a block before launching
            playerPositions.put(player.getUUID(), player.position().subtract(min.getX() + (max.getX() - min.getX()) / 2.0 + 0.5, min.getY(), min.getZ() + (max.getZ() - min.getZ()) / 2.0 + 0.5));
        }

        // DEBUG
        RocketEntity rocket = new RocketEntity(this.level, ctx, playerPositions);
        rocket.setPos(min.getX() + (max.getX() - min.getX()) / 2.0 + 0.5, min.getY(), min.getZ() + (max.getZ() - min.getZ()) / 2.0 + 0.5);
        for (Map.Entry<UUID, Vec3> entry : playerPositions.entrySet())
        {
            Player player = this.level.getPlayerByUUID(entry.getKey());
            if (player != null)
                player.startRiding(rocket, true);
        }
        this.level.addFreshEntity(rocket);
    }

    public void attemptLaunch(@Nullable CommandSource source)
    {
        if (this.level == null || this.level.isClientSide())
            throw new IllegalStateException("Cannot scan client side or without level");

        if (this.launchFuture != null)
        {
            this.cancelLaunch();
            return;
        }

        if (!this.runningScan.isDone())
        {
            this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.scanning"));
            return;
        }

        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(RocketControllerBlock.STATE, RocketControllerBlock.State.SEARCHING), Constants.BlockFlags.DEFAULT);
        this.components.clear();

        Stopwatch startTime = Stopwatch.createStarted();
        this.runningScan = BlockScanner.runScan(this.level, this.getBlockPos(), state -> !state.getBlock().is(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM.get()) && !state.isAir(), 64);
        this.runningScan.thenAcceptAsync(result -> // TODO config for distance
        {
            LOGGER.info("Completed scan in " + startTime);
            if (!result.isSuccess())
            {
                this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.large"));
                return;
            }

            if (result.getCount(BeyondBlocks.ROCKET_CONTROLLER.get()) != 1)
            {
                this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.too_many_controllers", result.getCount(BeyondBlocks.ROCKET_CONTROLLER.get())));
                return;
            }

            BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();
            Set<BlockPos> positions = result.getBlockPositions();
            if (!positions.isEmpty())
            {
                BlockPos first = positions.iterator().next();
                min.set(first);
                max.set(first);
                for (BlockPos pos : result.getBlockPositions())
                {
                    if (pos.getX() < min.getX())
                        min.setX(pos.getX());
                    if (pos.getY() < min.getY())
                        min.setY(pos.getY());
                    if (pos.getZ() < min.getZ())
                        min.setZ(pos.getZ());
                    if (pos.getX() > max.getX())
                        max.setX(pos.getX());
                    if (pos.getY() > max.getY())
                        max.setY(pos.getY());
                    if (pos.getZ() > max.getZ())
                        max.setZ(pos.getZ());

                    BlockState state = this.level.getBlockState(pos);
                    if (state.getBlock() instanceof RocketComponent)
                        this.components.put(pos, (RocketComponent) state.getBlock());
                }
            }

            float thrust = 0.0F;
            for (Map.Entry<BlockPos, RocketComponent> entry : this.components.entrySet())
                if (entry.getValue() instanceof RocketThruster)
                    thrust += ((RocketThruster) entry.getValue()).getThrust(this.level, entry.getKey());

            float mass = Math.round(positions.size() / 512.0);
            if (thrust <= mass) // TODO calculate required thrust
            {
                this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.not_enough_thrust", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(mass + 1), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(thrust)));
                return;
            }

            StructureTemplate structure = new StructureTemplate();
            structure.fillFromWorld(this.level, min, max.subtract(min).offset(1, 1, 1), true, null);

            List<StructureTemplate.Palette> blockInfos = getPalettes(structure);
            for (StructureTemplate.Palette palette : blockInfos)
                palette.blocks().removeIf(block -> !positions.contains(block.pos.offset(min)));

            this.cancelLaunch();
            LaunchContext ctx = new LaunchContext(structure, (thrust - mass) / 16.0F);
            this.launchFuture = Scheduler.get(this.level).schedule(() -> this.launch(ctx, min.immutable(), max.immutable()), LAUNCH_TIME, TimeUnit.SECONDS);

            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(RocketControllerBlock.STATE, RocketControllerBlock.State.SUCCESS), Constants.BlockFlags.DEFAULT);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.DEFAULT);
        }, this.level.getServer()).exceptionally(e ->
        {
            LOGGER.error("Error scanning rocket", e);
            return null;
        });
    }

    @Override
    public void tick()
    {
        if (this.isLaunching() && this.level != null)
        {
            if (this.components.entrySet().removeIf(entry -> this.level.getBlockState(entry.getKey()).getBlock() != entry.getValue()) && !this.level.isClientSide())
                this.cancelLaunch();
            if (this.level.isClientSide())
            {
                this.components.forEach((pos, component) -> component.addParticles(this.level, this.level.getBlockState(pos), pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0));
            }
        }
    }

    @Override
    public CompoundTag writeSyncTag(CompoundTag nbt)
    {
        nbt.putBoolean("Launching", this.isLaunching());
        if (this.isLaunching())
        {
            ListTag componentsNbt = new ListTag();
            this.components.keySet().forEach(pos -> componentsNbt.add(LongTag.valueOf(pos.asLong())));
            nbt.put("Components", componentsNbt);
        }
        return nbt;
    }

    @Override
    public void readSyncTag(CompoundTag nbt)
    {
        this.launching = nbt.getBoolean("Launching");
        this.components.clear();
        if (this.launching && this.level != null)
        {
            ListTag componentsNbt = nbt.getList("Components", Constants.NBT.TAG_LONG);
            for (Tag tag : componentsNbt)
            {
                BlockPos pos = BlockPos.of(((LongTag) tag).getAsLong());
                Block block = this.level.getBlockState(pos).getBlock();
                if (block instanceof RocketComponent)
                    this.components.put(pos, (RocketComponent) block);
            }
        }
    }

    public boolean isLaunching()
    {
        if (this.level == null)
            return false;
        return this.level.isClientSide() ? this.launching : this.launchFuture != null && !this.launchFuture.isDone();
    }

    //    @Override
//    public CompoundTag save(CompoundTag nbt)
//    {
//        super.save(nbt);
//        if (this.runningScan.isDone() && this.template != null)
//            nbt.put("Template", this.template.save(new CompoundTag()));
//        return nbt;
//    }
//
//    @Override
//    public void load(BlockState state, CompoundTag nbt)
//    {
//        super.load(state, nbt);
//        if (this.runningScan.isDone())
//        {
//            this.components.clear();
//            if (nbt.contains("Template", Constants.NBT.TAG_COMPOUND))
//            {
//                if (this.template == null)
//                    this.template = new StructureTemplate();
//                this.template.load(nbt.getCompound("Template"));
//
//                List<StructureTemplate.Palette> blockInfos = this.getPalettes();
//                for (StructureTemplate.Palette palette : blockInfos)
//                {
//                    for (StructureTemplate.StructureBlockInfo info : palette.blocks())
//                    {
//                        if (info.state.getBlock() instanceof RocketComponent)
//                            this.components.put(info.pos, (RocketComponent) info.state.getBlock());
//                    }
//                }
//            }
//        }
//    }

    public static List<StructureTemplate.Palette> getPalettes(StructureTemplate structure)
    {
        List<StructureTemplate.Palette> blockInfos = ObfuscationReflectionHelper.getPrivateValue(StructureTemplate.class, structure, "field_204769_a");
        if (blockInfos == null)
            return Collections.emptyList();
        return blockInfos;
    }

    private List<StructureTemplate.StructureEntityInfo> getEntities(StructureTemplate structure)
    {
        List<StructureTemplate.StructureEntityInfo> entityInfos = ObfuscationReflectionHelper.getPrivateValue(StructureTemplate.class, structure, "field_186271_b");
        if (entityInfos == null)
            return Collections.emptyList();
        return entityInfos;
    }
}
