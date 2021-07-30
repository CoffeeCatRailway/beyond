package io.github.ocelot.beyond.common.entity;

import io.github.ocelot.beyond.common.blockentity.RocketControllerBlockEntity;
import io.github.ocelot.beyond.common.init.BeyondEntities;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.init.BeyondTriggers;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import io.github.ocelot.beyond.common.rocket.LaunchContext;
import io.github.ocelot.beyond.common.rocket.RocketComponent;
import io.github.ocelot.beyond.common.space.SpaceManager;
import io.github.ocelot.beyond.common.space.SpaceTeleporter;
import io.github.ocelot.beyond.event.ReloadRenderersEvent;
import io.github.ocelot.sonar.client.render.StructureTemplateRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ocelot
 */
public class RocketEntity extends Entity implements IEntityAdditionalSpawnData
{
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);

    private final Map<BlockPos, BlockState> components;
    private LaunchContext ctx;
    private EntityDimensions dimensions;
    private final Map<UUID, Vec3> players;

    @OnlyIn(Dist.CLIENT)
    private StructureTemplateRenderer templateRenderer;

    public RocketEntity(EntityType<? extends RocketEntity> entityType, Level level)
    {
        super(entityType, level);
        this.components = new HashMap<>();
        this.ctx = LaunchContext.DUMMY;
        this.players = new HashMap<>();
        this.noCulling = true;
        this.noPhysics = true;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public RocketEntity(Level level, LaunchContext ctx, Map<UUID, Vec3> players)
    {
        this(BeyondEntities.ROCKET.get(), level);
        this.ctx = ctx;
        this.recalculateDimensions();
        this.locateComponents();
        this.players.putAll(players);
    }

    private void recalculateDimensions()
    {
        Vec3i size = this.ctx.getTemplate().getSize();
        this.dimensions = EntityDimensions.fixed(Math.max(size.getX(), size.getZ()), size.getY());
        this.refreshDimensions();
    }

    private void locateComponents()
    {
        List<StructureTemplate.Palette> blockInfos = RocketControllerBlockEntity.getPalettes(this.ctx.getTemplate());
        for (StructureTemplate.Palette palette : blockInfos)
        {
            for (StructureTemplate.StructureBlockInfo info : palette.blocks())
            {
                if (info.state.getBlock() instanceof RocketComponent)
                    this.components.put(info.pos, info.state);
            }
        }
    }

    private Phase getPhase()
    {
        return Phase.values()[this.entityData.get(PHASE) % Phase.values().length];
    }

    private void setPhase(Phase phase)
    {
        this.entityData.set(PHASE, phase.ordinal());
    }

    @OnlyIn(Dist.CLIENT)
    private void freeRenderer()
    {
        if (this.templateRenderer != null)
        {
            this.templateRenderer.free();
            this.templateRenderer = null;
        }
    }

    @SubscribeEvent
    public void onReloadRenderers(ReloadRenderersEvent event)
    {
        this.freeRenderer();
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(PHASE, Phase.LAUNCHING.ordinal());
    }

    @Override
    public void tick()
    {
        super.tick();

        switch (this.getPhase())
        {
            case LAUNCHING:
            {
                if (this.getY() < this.level.getMaxBuildHeight())
                {
                    this.setDeltaMovement(0, Math.min(this.getDeltaMovement().y() + this.ctx.getLift() / 20.0F, 1.0F), 0);
                }
                else
                {
                    this.setDeltaMovement(Vec3.ZERO);
                    if (!this.level.isClientSide())
                    {
                        boolean success = SpaceManager.get().beginTransaction(this.getPassengers().stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).toArray(Player[]::new), this.ctx, new SpaceManager.TransactionListener()
                        {
                            @Override
                            public void cancel()
                            {
                                RocketEntity.this.setPhase(Phase.LANDING);
                            }

                            @Override
                            public void travelTo(ServerLevel level)
                            {
                                List<Entity> passengers = new ArrayList<>(RocketEntity.this.getPassengers());
                                RocketEntity.this.ejectPassengers();
                                RocketEntity newRocket = (RocketEntity) RocketEntity.this.changeDimension(level, new SpaceTeleporter());
                                if (newRocket == null)
                                {
                                    for (Entity passenger : passengers)
                                        if (passenger instanceof Player)
                                            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) passenger), new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.ABORT));
                                    this.cancel();
                                    return;
                                }

                                passengers.forEach(entity ->
                                {
                                    Entity e = entity.changeDimension(level, new SpaceTeleporter());
                                    if (e != null)
                                    {
                                        e.startRiding(newRocket, true);
                                        if (e instanceof Player)
                                            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e), new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.SUCCESS));
                                    }
                                    else if (entity instanceof Player)
                                    {
                                        BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) entity), new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.ABORT));
                                    }
                                });
                                newRocket.setPhase(Phase.LANDING);
                            }
                        });

                        if (!success)
                        {
                            RocketEntity.this.setPhase(Phase.LANDING);
                            return;
                        }

                        RocketEntity.this.setPhase(Phase.WAITING);
                    }
                }
                break;
            }
            case WAITING:
            {
                break;
            }
            case LANDING:
            {
                this.noPhysics = false;
                if (!this.verticalCollision)
                {
                    this.setDeltaMovement(0, -0.5F, 0);
                }
                else
                {
                    this.setDeltaMovement(Vec3.ZERO);

                    this.getPassengers().forEach(entity ->
                    {
                        if (entity instanceof ServerPlayer)
                            BeyondTriggers.LAND_ROCKET.trigger((ServerPlayer) entity);
                    });

                    this.ejectPassengers();
                    this.remove();

                    if (!this.level.isClientSide())
                    {
                        Vec3i size = this.ctx.getTemplate().getSize();
                        this.ctx.getTemplate().placeInWorld((ServerLevel) this.level, new BlockPos(this.getX() - size.getX() / 2.0, this.getY(), this.getZ() - size.getZ() / 2.0), new StructurePlaceSettings(), this.level.getRandom());
                    }
                }
                break;
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level.isClientSide() && this.getPhase() != Phase.WAITING)
        {
            for (Map.Entry<BlockPos, BlockState> entry : this.components.entrySet())
            {
                if (entry.getValue().getBlock() instanceof RocketComponent)
                {
                    BlockPos pos = entry.getKey();
                    Vec3i size = this.ctx.getTemplate().getSize();
                    ((RocketComponent) entry.getValue().getBlock()).addParticles(this.level, entry.getValue(), this.getX() - size.getX() / 2.0 + pos.getX(), this.getY() + pos.getY(), this.getZ() - size.getZ() / 2.0 + pos.getZ(), this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z());
                }
            }
        }

//        this.setDeltaMovement(this.getDeltaMovement().add(0, 1.0F / 20.0F, 0));
    }

    @Nullable
    @Override
    public Entity getControllingPassenger()
    {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    protected boolean canAddPassenger(Entity entity)
    {
        return this.players.containsKey(entity.getUUID());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity entity)
    {
        if (this.players.containsKey(entity.getUUID()))
            return this.players.get(entity.getUUID()).add(this.position());
        return this.players.getOrDefault(entity.getUUID(), super.getDismountLocationForPassenger(entity));
    }

    @Override
    public void positionRider(Entity passenger)
    {
        if (this.hasPassenger(passenger))
        {
            if (this.players.containsKey(passenger.getUUID()))
            {
                Vec3 pos = this.players.get(passenger.getUUID());
                passenger.setPos(this.getX() + pos.x(), this.getY() + pos.y(), this.getZ() + pos.z());
            }
            else
            {
                super.positionRider(passenger);
            }
        }
    }

    @Override
    public boolean shouldRiderSit()
    {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return dimensions;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        this.ctx = LaunchContext.CODEC.parse(NbtOps.INSTANCE, nbt.get("LaunchData")).result().orElse(LaunchContext.DUMMY);
        this.recalculateDimensions();
        this.locateComponents();

        this.players.clear();
        ListTag playersNbt = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < playersNbt.size(); i++)
        {
            CompoundTag playerNbt = playersNbt.getCompound(i);
            this.players.put(playerNbt.getUUID("UUID"), new Vec3(playerNbt.getDouble("X"), playerNbt.getDouble("Y"), playerNbt.getDouble("Z")));
        }

        this.entityData.set(PHASE, (int) nbt.getByte("Phase"));
        if (this.getPhase() == Phase.WAITING)
            this.setPhase(Phase.LANDING); // This will only occur if the commander is mid transaction and something goes horribly wrong when the server shuts down, so tell the rocket to land instead of stay in the air
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.put("LaunchData", LaunchContext.CODEC.encodeStart(NbtOps.INSTANCE, this.ctx).getOrThrow(false, LOGGER::error));

        ListTag playersNbt = new ListTag();
        this.players.forEach((id, pos) ->
        {
            CompoundTag playerNbt = new CompoundTag();
            playerNbt.putUUID("UUID", id);
            playerNbt.putDouble("X", pos.x());
            playerNbt.putDouble("Y", pos.y());
            playerNbt.putDouble("Z", pos.z());
            playersNbt.add(playerNbt);
        });
        nbt.put("Players", playersNbt);

        nbt.putByte("Phase", this.entityData.get(PHASE).byteValue());
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf)
    {
        try
        {
            buf.writeWithCodec(LaunchContext.CODEC, this.ctx);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        buf.writeVarInt(this.players.size());
        for (Map.Entry<UUID, Vec3> entry : this.players.entrySet())
        {
            buf.writeUUID(entry.getKey());
            buf.writeDouble(entry.getValue().x());
            buf.writeDouble(entry.getValue().y());
            buf.writeDouble(entry.getValue().z());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf)
    {
    }

    @Override
    public void onRemovedFromWorld()
    {
        super.onRemovedFromWorld();
        if (this.level.isClientSide())
            this.freeRenderer();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @OnlyIn(Dist.CLIENT)
    public StructureTemplateRenderer getRenderer()
    {
        if (this.templateRenderer == null)
        {
            Vec3i size = this.ctx.getTemplate().getSize();
            this.templateRenderer = new StructureTemplateRenderer(CompletableFuture.completedFuture(this.ctx.getTemplate()), ((ClientLevel) this.level).effects().constantAmbientLight(), level -> new LevelLightEngine(level, true, this.level.dimensionType().hasSkyLight()), (pos, colorResolver) -> colorResolver.getColor(this.level.getBiome(pos.offset(this.getX() - size.getX() / 2.0, this.getY(), this.getZ() - size.getZ() / 2.0)), this.getX() - size.getX() / 2.0 + pos.getX(), this.getZ() - size.getZ() / 2.0 + pos.getZ()));
        }
        return this.templateRenderer;
    }

    /**
     * <p>The phase of rocket entity movement.</p>
     *
     * @author Ocelot
     */
    public enum Phase
    {
        LAUNCHING, WAITING, LANDING
    }
}
