package io.github.ocelot.beyond.common.space;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SUpdateSimulationBodiesMessage;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.planet.StaticSolarSystemDefinitions;
import io.github.ocelot.beyond.common.space.satellite.ArtificialSatellite;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import io.github.ocelot.beyond.common.space.satellite.Satellite;
import io.github.ocelot.beyond.common.space.simulation.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
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
public class SpaceManager extends SavedData
{
    private static final String DATA_NAME = Beyond.MOD_ID + "_SpaceManager";

    private final CelestialBodySimulation simulation;

    // TODO remove player from simulation if they aren't in a rocket entity
    private SpaceManager()
    {
        super(DATA_NAME);

        // TODO use custom server side implementation of simulation
        // TODO Load simulation from datapack
        this.simulation = new CelestialBodySimulation(StaticSolarSystemDefinitions.SOLAR_SYSTEM.get());
        this.simulation.addListener(new CelestialBodySimulation.PlayerUpdateListener()
        {
            @Override
            public void onPlayersJoin(PlayerRocketBody... bodies)
            {
                SpaceManager.this.notifyAllPlayers(new SUpdateSimulationBodiesMessage(Arrays.stream(bodies).map(PlayerRocketBody::getSatellite).toArray(Satellite[]::new), new ResourceLocation[0]));
            }

            @Override
            public void onPlayersLeave(PlayerRocketBody... bodies)
            {
                SpaceManager.this.notifyAllPlayers(new SUpdateSimulationBodiesMessage(new Satellite[0], Arrays.stream(bodies).map(SimulatedBody::getId).toArray(ResourceLocation[]::new)));
            }
        });

        ArtificialSatelliteBody earthSatellite = new ArtificialSatelliteBody(this.simulation, new ArtificialSatellite(new ResourceLocation(Beyond.MOD_ID, "earth_satellite_test"), new ResourceLocation(Beyond.MOD_ID, "body/satellite"), new TextComponent("Earth Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "earth")));
        this.simulation.add(earthSatellite);

        ArtificialSatelliteBody marsSatellite = new ArtificialSatelliteBody(this.simulation, new ArtificialSatellite(new ResourceLocation(Beyond.MOD_ID, "mars_satellite_test"), new ResourceLocation(Beyond.MOD_ID, "body/satellite"), new TextComponent("Mars Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "mars")));
        this.simulation.add(marsSatellite);

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
            ServerPlayer player = server.getPlayerList().getPlayer(b.getSatellite().getProfile().getId());
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

    private SOpenSpaceTravelScreenMessage createPacket(Stream<SatelliteBody<?>> bodies)
    {
        return new SOpenSpaceTravelScreenMessage(Stream.concat(this.simulation.getSatellites(), bodies).map(SatelliteBody::getSatellite).toArray(Satellite[]::new));
    }

    /**
     * Inserts the specified player into the simulation.
     *
     * @param player The player to insert
     * @return A new packet to sync the player with the simulation
     */
    public SOpenSpaceTravelScreenMessage insertPlayer(Player player)
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
        this.simulation.getPlayers().filter(body -> body.getSatellite().getProfile().getId().equals(id)).map(SimulatedBody::getId).forEach(this.simulation::remove);
    }

    /**
     * Relays the specified message to all players in the simulation except for the sender if not null.
     *
     * @param sender The person sending the message or <code>null</code> to send the message to all players
     * @param msg    The message to relay
     */
    public void relay(@Nullable ServerPlayer sender, SPlayerTravelMessage msg)
    {
        this.simulation.getPlayers().forEach(b ->
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null)
                return;
            ServerPlayer player = server.getPlayerList().getPlayer(b.getSatellite().getProfile().getId());
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
    public void load(CompoundTag nbt)
    {
    }

    @Override
    public CompoundTag save(CompoundTag nbt)
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
        ServerLevel world = server.getLevel(Level.OVERWORLD);
        return world != null ? world.getDataStorage().computeIfAbsent(SpaceManager::new, DATA_NAME) : null;
    }
}
