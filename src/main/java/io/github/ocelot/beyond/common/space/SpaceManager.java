package io.github.ocelot.beyond.common.space;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SUpdateSimulationBodiesMessage;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.planet.StaticSolarSystemDefinitions;
import io.github.ocelot.beyond.common.space.simulation.CelestialBodySimulation;
import io.github.ocelot.beyond.common.space.simulation.PlayerRocketBody;
import io.github.ocelot.beyond.common.space.simulation.SimulatedBody;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * <p>The server-side tracking of players in and approaching space.</p>
 *
 * @author Ocelot
 */
public class SpaceManager extends WorldSavedData
{
    private static final String DATA_NAME = Beyond.MOD_ID + "_SpaceManager";

    private final CelestialBodySimulation simulation;

    // TODO remove player from simulation if they aren't in a rocket entity
    private SpaceManager()
    {
        super(DATA_NAME);
        // TODO Load simulation from datapack
        this.simulation = new CelestialBodySimulation(StaticSolarSystemDefinitions.SOLAR_SYSTEM.get());
        this.simulation.addListener(new CelestialBodySimulation.PlayerUpdateListener()
        {
            @Override
            public void onPlayersJoin(PlayerRocketBody... bodies)
            {
                SpaceManager.this.notifyAllPlayers(new SUpdateSimulationBodiesMessage(Arrays.stream(bodies).map(PlayerRocketBody::getRocket).toArray(PlayerRocket[]::new), new ResourceLocation[0]));
            }

            @Override
            public void onPlayersLeave(PlayerRocketBody... bodies)
            {
                SpaceManager.this.notifyAllPlayers(new SUpdateSimulationBodiesMessage(new PlayerRocket[0], Arrays.stream(bodies).map(SimulatedBody::getId).toArray(ResourceLocation[]::new)));
            }
        });
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLeave);
    }

    private <MSG> void notifyAllPlayers(MSG msg)
    {
        this.simulation.getPlayers().forEach(b ->
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null)
                return;
            ServerPlayerEntity player = server.getPlayerList().getPlayer(b.getRocket().getProfile().getId());
            if (player == null)
                return;
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
        });
    }

    private void onTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
            return;
        this.simulation.tick();
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        PlayerRocketBody body = this.simulation.getPlayer(event.getPlayer().getUUID());
        if (body != null)
            this.simulation.remove(body.getId());
    }

    private SOpenSpaceTravelScreenMessage createPacket(Stream<PlayerRocketBody> bodies)
    {
        return new SOpenSpaceTravelScreenMessage(Stream.concat(this.simulation.getPlayers(), bodies).map(PlayerRocketBody::getRocket).toArray(PlayerRocket[]::new));
    }

    /**
     * Inserts the specified player into the simulation.
     *
     * @param player The player to insert
     * @return A new packet to sync the player with the simulation
     */
    public SOpenSpaceTravelScreenMessage insertPlayer(PlayerEntity player)
    {
        PlayerRocketBody body = this.simulation.getPlayer(player.getUUID());
        if (body != null)
            return this.createPacket(Stream.empty());

        ResourceLocation playerDimension = player.level.dimension().location();
        ResourceLocation playerPlanet = this.simulation.getBodies().filter(b -> b.canTeleportTo() && b.getDimension().isPresent() && b.getDimension().get().equals(playerDimension)).map(SimulatedBody::getId).findAny().orElse(Planet.EARTH);
        body = new PlayerRocketBody(this.simulation, new PlayerRocket(player, playerPlanet));
        this.simulation.add(body);
        return this.createPacket(Stream.of(body));
    }

    /**
     * Removes all players from the simulation with the specified id.
     *
     * @param id The id of the player to remove
     */
    public void removePlayer(UUID id)
    {
        this.simulation.getPlayers().filter(body -> body.getRocket().getProfile().getId().equals(id)).map(SimulatedBody::getId).forEach(this.simulation::remove);
    }

    /**
     * Relays the specified message to all players in the simulation except for the sender if not null.
     *
     * @param sender The person sending the message or <code>null</code> to send the message to all players
     * @param msg    The message to relay
     */
    public void relay(@Nullable ServerPlayerEntity sender, SPlayerTravelMessage msg)
    {
        this.simulation.getPlayers().forEach(b ->
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null)
                return;
            ServerPlayerEntity player = server.getPlayerList().getPlayer(b.getRocket().getProfile().getId());
            if (player == null || (sender != null && player.getUUID().equals(sender.getUUID())))
                return;
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
        });
    }

    /**
     * @return The server sided simulation
     */
    public CelestialBodySimulation getSimulation()
    {
        return simulation;
    }

    @Override
    public void load(CompoundNBT nbt)
    {
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt)
    {
        return nbt;
    }

    /**
     * Fetches the space manager for the specified server world.
     *
     * @param server The server instance
     * @return The space manager for the server
     */
    @Nullable
    public static SpaceManager get(MinecraftServer server)
    {
        ServerWorld world = server.getLevel(World.OVERWORLD);
        return world != null ? world.getDataStorage().computeIfAbsent(SpaceManager::new, DATA_NAME) : null;
    }
}
