package io.github.ocelot.beyond.common.blockentity;

import com.google.common.base.Stopwatch;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.block.RocketComponent;
import io.github.ocelot.beyond.common.block.RocketControllerBlock;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.util.BlockScanner;
import io.github.ocelot.sonar.common.tileentity.BaseTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ocelot
 */
public class RocketControllerBlockEntity extends BaseTileEntity
{
    private static final Logger LOGGER = LogManager.getLogger();
    private CompletableFuture<?> runningScan;

    private final Map<BlockPos, RocketComponent> components;
    private StructureTemplate template;

    public RocketControllerBlockEntity()
    {
        super(BeyondBlocks.ROCKET_CONTROLLER_BE.get());
        this.runningScan = CompletableFuture.completedFuture(null);
        this.components = new HashMap<>();
        this.template = null;
    }

    // TODO send errors using a screen block instead of chat
    private void sendError(@Nullable CommandSource source, MutableComponent error)
    {
        LOGGER.warn(error);
        if (source != null && source.acceptsFailure())
            source.sendMessage(error.withStyle(ChatFormatting.RED), Util.NIL_UUID);
    }

    public void rescan(@Nullable CommandSource source)
    {
        if (this.level == null || this.level.isClientSide())
            throw new IllegalStateException("Cannot scan client side or without level");

        if (!this.runningScan.isDone())
        {
            this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.scanning"));
            return;
        }

        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(RocketControllerBlock.LIT, false), Constants.BlockFlags.DEFAULT);
        this.components.clear();
        this.template = null;

        Stopwatch startTime = Stopwatch.createStarted();
        this.runningScan = BlockScanner.runScan(this.level, this.getBlockPos(), state -> !state.getBlock().is(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM.get()) && !state.isAir(), 64).thenAcceptAsync(result -> // TODO config for distance
        {
            LOGGER.info("Completed scan in " + startTime);
            if (!result.isSuccess())
            {
                this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.large"));
                return;
            }

            if (result.getCount(BeyondBlocks.ROCKET_CONTROLLER.get()) != 1)
            {
                this.sendError(source, new TranslatableComponent("block." + Beyond.MOD_ID + ".rocket_controller.too_many_controllers"));
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

                    Block block = this.level.getBlockState(pos).getBlock();
                    if (block instanceof RocketComponent)
                        this.components.put(pos, (RocketComponent) block);
                }
            }

            this.template = new StructureTemplate();
            this.template.fillFromWorld(this.level, min, max.subtract(min).offset(1, 1, 1), true, null);

            List<StructureTemplate.Palette> blockInfos = this.getPalettes();
            for (StructureTemplate.Palette palette : blockInfos)
                palette.blocks().removeIf(block -> !positions.contains(block.pos.offset(min)));

            // DEBUG
            if (!FMLLoader.isProduction())
                Objects.requireNonNull(this.level.getServer()).getStructureManager().getOrCreate(new ResourceLocation(Beyond.MOD_ID, "test")).load(this.template.save(new CompoundTag()));

            this.setChanged();
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(RocketControllerBlock.LIT, true), Constants.BlockFlags.DEFAULT);
        }, this.level.getServer()).exceptionally(e ->
        {
            LOGGER.error("Error scanning rocket", e);
            return null;
        });
    }

    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        super.save(nbt);
        if (this.runningScan.isDone() && this.template != null)
            nbt.put("Template", this.template.save(new CompoundTag()));
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundTag nbt)
    {
        super.load(state, nbt);
        if (this.runningScan.isDone())
        {
            this.components.clear();
            if (nbt.contains("Template", Constants.NBT.TAG_COMPOUND))
            {
                if (this.template == null)
                    this.template = new StructureTemplate();
                this.template.load(nbt.getCompound("Template"));

                List<StructureTemplate.Palette> blockInfos = this.getPalettes();
                for (StructureTemplate.Palette palette : blockInfos)
                {
                    for (StructureTemplate.StructureBlockInfo info : palette.blocks())
                    {
                        if (info.state.getBlock() instanceof RocketComponent)
                            this.components.put(info.pos, (RocketComponent) info.state.getBlock());
                    }
                }
            }
        }
    }

    private List<StructureTemplate.Palette> getPalettes()
    {
        List<StructureTemplate.Palette> blockInfos = ObfuscationReflectionHelper.getPrivateValue(StructureTemplate.class, this.template, "field_204769_a");
        if (blockInfos == null)
            return Collections.emptyList();
        return blockInfos;
    }
}
