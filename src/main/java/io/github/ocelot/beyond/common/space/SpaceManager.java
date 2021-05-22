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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>The server-side tracking of players in and approaching space.</p>
 *
 * @author Ocelot
 */
public class SpaceManager
{
    private static SpaceManager spaceManager;
    private final MinecraftServer server;
    // TODO store solar system id and update dimension cache
    private final Map<ResourceLocation, ResourceLocation> validDestinations;
    private final Set<Satellite> satellites;

    // TODO remove player from simulation if they aren't in a rocket entity
    private SpaceManager(MinecraftServer server)
    {
        this.server = server;

        // TODO Load simulation from datapack

        this.validDestinations = StaticSolarSystemDefinitions.SOLAR_SYSTEM.get().entrySet().stream().filter(entry -> entry.getValue().getDimension().isPresent()).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getDimension().get()));
        this.satellites = ConcurrentHashMap.newKeySet();

        this.add(new ArtificialSatellite(new TextComponent("Earth Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "earth"), new ResourceLocation(Beyond.MOD_ID, "body/satellite")));
        this.add(new ArtificialSatellite(new TextComponent("Mars Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "mars"), new ResourceLocation(Beyond.MOD_ID, "body/satellite")));

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLeave);
    }

    private void add(Satellite satellite)
    {
        this.satellites.add(satellite);
        if (satellite instanceof PlayerRocket)
            this.notifyAllPlayers(((PlayerRocket) satellite).getProfile().getId(), new SUpdateSimulationBodiesMessage(new Satellite[]{satellite}, new int[0]));
    }

    private void remove(Satellite satellite)
    {
        this.satellites.remove(satellite);
        if (satellite instanceof PlayerRocket)
            this.notifyAllPlayers(((PlayerRocket) satellite).getProfile().getId(), new SUpdateSimulationBodiesMessage(new Satellite[0], new int[]{satellite.getId()}));
    }

    private Stream<PlayerRocket> getPlayers()
    {
        return this.satellites.stream().filter(satellite -> satellite instanceof PlayerRocket).map(satellite -> (PlayerRocket) satellite);
    }

    private <MSG> void notifyAllPlayers(@Nullable UUID id, MSG msg)
    {
        this.getPlayers().forEach(b ->
        {
            ServerPlayer player = this.server.getPlayerList().getPlayer(b.getProfile().getId());
            if (player == null || (id != null && player.getUUID().equals(id)))
                return;
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
        });
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        this.getPlayer(event.getPlayer().getUUID()).ifPresent(this::remove);
    }

    /**
     * Inserts the specified player into the simulation.
     *
     * @param player The player to insert
     * @return A new packet to sync the player with the simulation
     */
    public SOpenSpaceTravelScreenMessage insertPlayer(Player player)
    {
        if (!this.getPlayer(player.getUUID()).isPresent())
        {
            ResourceLocation playerDimension = player.level.dimension().location();
            ResourceLocation playerPlanet = this.validDestinations.entrySet().stream().filter(entry -> entry.getValue().equals(playerDimension)).map(Map.Entry::getKey).findAny().orElse(Planet.EARTH);//this.simulation.getBodies().filter(b -> b.canTeleportTo() && b.getDimension().isPresent() && b.getDimension().get().equals(playerDimension)).map(SimulatedBody::getId).findAny().orElse(Planet.EARTH);
            this.add(new PlayerRocket(player, playerPlanet));
        }
        return new SOpenSpaceTravelScreenMessage(this.satellites.toArray(new Satellite[0]));
    }

    /**
     * Removes all players from the simulation with the specified id.
     *
     * @param id The id of the player to remove
     */
    public void removePlayer(UUID id)
    {
        PlayerRocket[] players = this.getPlayers().toArray(PlayerRocket[]::new);
        for (PlayerRocket rocket : players)
        {
            if (rocket.getProfile().getId().equals(id))
                this.remove(rocket);
        }
    }

    /**
     * Relays the specified message to all players in the simulation except for the sender if not null.
     *
     * @param sender The person sending the message or <code>null</code> to send the message to all players
     * @param msg    The message to relay
     */
    public void relay(@Nullable ServerPlayer sender, SPlayerTravelMessage msg)
    {
        this.getPlayers().forEach(rocket ->
        {
            ServerPlayer player = this.server.getPlayerList().getPlayer(rocket.getProfile().getId());
            if (player == null || (sender != null && player.getUUID().equals(sender.getUUID())))
                return;
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
        });
    }

    /**
     * Fetches a player with the specified id from the simulation.
     *
     * @param playerId The player to fetch
     * @return The player in space or nothing if there is no player in space with that id
     */
    public Optional<PlayerRocket> getPlayer(UUID playerId)
    {
        return this.getPlayers().filter(rocket -> rocket.getProfile().getId().equals(playerId)).findFirst();
    }

    /**
     * Retrieves a dimension from the body id.
     *
     * @param bodyId The id of the body to get the dimension for
     * @return The dimension for that body
     */
    public Optional<ResourceLocation> getDimension(ResourceLocation bodyId)
    {
        return Optional.ofNullable(this.validDestinations.get(bodyId));
    }

    /**
     * @return The space manager for the server
     */
    public static SpaceManager get()
    {
        return spaceManager;
    }

    /**
     * Loads the space manager with the specified server.
     *
     * @param server The server instance
     */
    public static void load(MinecraftServer server)
    {
        spaceManager = new SpaceManager(server);
    }

    /**
     * Unloads the space manager for the server.
     */
    public static void unload()
    {
        spaceManager = null;
    }

    private static class SpaceData extends SavedData
    {
        private static final String DATA_NAME = Beyond.MOD_ID + "_Space";

        private final Set<Satellite> satellites;

        private SpaceData()
        {
            super(DATA_NAME);
            this.satellites = new HashSet<>();
        }

        @Override
        public void load(CompoundTag nbt)
        {
//            this.satellites.clear();
//            ListTag satellitesNbt = nbt.getList("Satellites", Constants.NBT.TAG_COMPOUND);
//            for (int i = 0; i < satellitesNbt.size(); i++)
//                this.satellites.add(Satellite.load(satellitesNbt.getCompound(i)));
        }

        @Override
        public CompoundTag save(CompoundTag nbt)
        {
            return null;
        }

        private void update(Set<Satellite> satellites)
        {

        }
    }
}
