package io.github.ocelot.beyond.common.space;

import com.mojang.serialization.Codec;
import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.SOpenSpaceTravelScreenMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlanetTravelResponseMessage;
import io.github.ocelot.beyond.common.network.play.message.SPlayerTravelMessage;
import io.github.ocelot.beyond.common.network.play.message.SUpdateSimulationBodiesMessage;
import io.github.ocelot.beyond.common.rocket.LaunchContext;
import io.github.ocelot.beyond.common.space.planet.Planet;
import io.github.ocelot.beyond.common.space.planet.StaticSolarSystemDefinitions;
import io.github.ocelot.beyond.common.space.satellite.ArtificialSatellite;
import io.github.ocelot.beyond.common.space.satellite.PlayerRocket;
import io.github.ocelot.beyond.common.space.satellite.Satellite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger();

    private static SpaceManager spaceManager;
    private final MinecraftServer server;
    // TODO store solar system id and update dimension cache
    private final Map<ResourceLocation, ResourceLocation> validDestinations;
    private final Map<UUID, TransactionListener> transactions;
    private final Set<Satellite> satellites;

    // TODO remove player from simulation if they aren't in a rocket entity
    private SpaceManager(MinecraftServer server)
    {
        this.server = server;

        // TODO Load simulation from datapack

        this.validDestinations = StaticSolarSystemDefinitions.SOLAR_SYSTEM.get().entrySet().stream().filter(entry -> entry.getValue().getDimension().isPresent()).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getDimension().get()));
        this.transactions = new HashMap<>();
        this.satellites = ConcurrentHashMap.newKeySet();

        List<Satellite> satellites = this.getSaveData().satellites;
        if (satellites.isEmpty())
        {
            // TODO don't add default satellites
            LOGGER.debug("Adding default satellites");
            this.add(new ArtificialSatellite(new TextComponent("Earth Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "earth"), new ResourceLocation(Beyond.MOD_ID, "body/satellite")));
            this.add(new ArtificialSatellite(new TextComponent("Mars Satellite Test"), new ResourceLocation(Beyond.MOD_ID, "mars"), new ResourceLocation(Beyond.MOD_ID, "body/satellite")));
        }
        else
        {
            this.satellites.addAll(satellites);
        }

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLeave);
    }

    private void add(Satellite satellite)
    {
        this.satellites.add(satellite);
        this.getSaveData().update(this.satellites);
        if (satellite instanceof PlayerRocket)
            this.notifyAllPlayers(((PlayerRocket) satellite).getCommandingProfile().getId(), new SUpdateSimulationBodiesMessage(new Satellite[]{satellite}, new int[0]));
    }

    private void remove(Satellite satellite)
    {
        this.satellites.remove(satellite);
        this.getSaveData().update(this.satellites);
        if (satellite instanceof PlayerRocket)
        {
            UUID id = ((PlayerRocket) satellite).getCommandingProfile().getId();
            this.getTransaction(id).ifPresent(TransactionListener::cancel);
            this.notifyAllPlayers(id, new SUpdateSimulationBodiesMessage(new Satellite[0], new int[]{satellite.getId()}));
        }
    }

    private Stream<PlayerRocket> getPlayers()
    {
        return this.satellites.stream().filter(satellite -> satellite instanceof PlayerRocket).map(satellite -> (PlayerRocket) satellite);
    }

    private <MSG> void notifyAllPlayers(@Nullable UUID id, MSG msg)
    {
        this.getPlayers().flatMap(b -> Arrays.stream(b.getProfiles())).forEach(profile ->
        {
            ServerPlayer player = this.server.getPlayerList().getPlayer(profile.getId());
            if (player == null || (id != null && player.getUUID().equals(id)))
                return;
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
        });
    }

    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        this.getPlayer(event.getPlayer().getUUID()).ifPresent(this::remove);
    }

    private SpaceData getSaveData()
    {
        return this.server.overworld().getDataStorage().computeIfAbsent(SpaceData::new, SpaceData.DATA_NAME);
    }

    private Optional<TransactionListener> getTransaction(UUID id)
    {
        return Optional.ofNullable(this.transactions.get(id));
    }

    /**
     * Checks to see if the player with the specified id is currently in a transaction with space.
     *
     * @param id The id of the player to check
     * @return Whether or not that player is currently in a transaction with space
     */
    public boolean hasTransaction(UUID id)
    {
        return this.transactions.containsKey(id);
    }

    /**
     * Initiates a space transaction with the specified players.
     *
     * @param players  The players to go into space. The first player is assumed to be the commander
     * @param ctx      The launch context
     * @param listener The listener for transaction events
     * @return Whether or not a transaction could be made
     */
    public boolean beginTransaction(Player[] players, LaunchContext ctx, TransactionListener listener)
    {
        if (players.length == 0) // If there are no players, immediately cancel the transaction and return to the ground
            return false;

        for (Player player : players)
            this.getPlayer(player.getUUID()).ifPresent(this::remove); // If any players are already in space, cancel their old transaction

        Player commander = players[0]; // The first player controls the ship

        ResourceLocation playerDimension = commander.level.dimension().location();
        ResourceLocation playerPlanet = this.validDestinations.entrySet().stream().filter(entry -> entry.getValue().equals(playerDimension)).map(Map.Entry::getKey).findAny().orElse(Planet.EARTH);//this.simulation.getBodies().filter(b -> b.canTeleportTo() && b.getDimension().isPresent() && b.getDimension().get().equals(playerDimension)).map(SimulatedBody::getId).findAny().orElse(Planet.EARTH);
        this.add(new PlayerRocket(players, playerPlanet, ctx.getTemplate()));
        for (Player player : players)
            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SOpenSpaceTravelScreenMessage(this.satellites.toArray(new Satellite[0])));
        this.transactions.put(commander.getUUID(), listener);

        return true;
    }

    /**
     * Called when the specified player cancels the transaction and returns to the ground.
     *
     * @param player The player cancelling
     */
    public void cancelTransaction(Player player)
    {
        this.getTransaction(player.getUUID()).ifPresent(TransactionListener::cancel);
        this.transactions.remove(player.getUUID());
        this.getPlayer(player.getUUID()).ifPresent(rocket ->
        {
            for (int i = 1; i < rocket.getProfiles().length; i++)
            {
                Player passenger = player.level.getPlayerByUUID(rocket.getProfiles()[i].getId());
                if (passenger != null)
                    BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) passenger), new SPlanetTravelResponseMessage(SPlanetTravelResponseMessage.Status.ABORT));
            }
            this.remove(rocket);
        });
    }

    /**
     * Called when the specified player made a decision and started travelling to the specified body.
     *
     * @param player The player travelling
     * @param bodyId The id of the body to travel to
     */
    public void depart(Player player, ResourceLocation bodyId)
    {
        this.notifyAllPlayers(player.getUUID(), new SPlayerTravelMessage(player.getUUID(), bodyId));
    }

    /**
     * Called when the commanding player arrives at the body.
     *
     * @param player The player commanding the ship
     * @param level  The level to travel to
     */
    public void arrive(Player player, ServerLevel level)
    {
        this.getTransaction(player.getUUID()).ifPresent(listener -> listener.travelTo(level));
        this.transactions.remove(player.getUUID());
        this.getPlayer(player.getUUID()).ifPresent(this::remove);
    }

//    /**
//     * Inserts the specified player into the simulation.
//     *
//     * @param player The player to insert
//     * @return A new packet to sync the player with the simulation
//     */
//    @Deprecated // TODO remove
//    public SOpenSpaceTravelScreenMessage insertPlayer(Player player, LaunchContext ctx)
//    {
//        if (!this.getPlayer(player.getUUID()).isPresent())
//        {
//            ResourceLocation playerDimension = player.level.dimension().location();
//            ResourceLocation playerPlanet = this.validDestinations.entrySet().stream().filter(entry -> entry.getValue().equals(playerDimension)).map(Map.Entry::getKey).findAny().orElse(Planet.EARTH);//this.simulation.getBodies().filter(b -> b.canTeleportTo() && b.getDimension().isPresent() && b.getDimension().get().equals(playerDimension)).map(SimulatedBody::getId).findAny().orElse(Planet.EARTH);
//            this.add(new PlayerRocket(player, playerPlanet, ctx.getTemplate()));
//        }
//        return new SOpenSpaceTravelScreenMessage(this.satellites.toArray(new Satellite[0]));
//    }

    /**
     * Removes all players from the simulation with the specified id.
     *
     * @param id The id of the player to remove
     */
    @Deprecated // TODO remove
    public void removePlayer(UUID id)
    {
        PlayerRocket[] players = this.getPlayers().toArray(PlayerRocket[]::new);
        for (PlayerRocket rocket : players)
            if (rocket.getCommandingProfile().getId().equals(id))
                this.remove(rocket);
    }

//    /**
//     * Relays the specified message to all players in the simulation except for the sender if not null.
//     *
//     * @param sender The person sending the message or <code>null</code> to send the message to all players
//     * @param msg    The message to relay
//     */
//    @Deprecated // TODO remove
//    public void relay(@Nullable ServerPlayer sender, SPlayerTravelMessage msg)
//    {
//        this.getPlayers().forEach(rocket ->
//        {
//            ServerPlayer player = this.server.getPlayerList().getPlayer(rocket.getProfile().getId());
//            if (player == null || (sender != null && player.getUUID().equals(sender.getUUID())))
//                return;
//            BeyondMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> player), msg);
//        });
//    }

    /**
     * Fetches a player with the specified id from the simulation if they are the commander.
     *
     * @param playerId The player to fetch
     * @return The player in space or nothing if there is no player in space with that id
     */
    public Optional<PlayerRocket> getPlayer(UUID playerId)
    {
        return this.getPlayers().filter(rocket -> rocket.getCommandingProfile().getId().equals(playerId)).findFirst();
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

    /**
     * <p>Manages behavior for interactions between the simulation and the rocket entity.</p>
     *
     * @author Ocelot
     */
    public interface TransactionListener
    {
        /**
         * Cancels the current transaction and sends the player back to the surface of the planet.
         */
        void cancel();

        /**
         * Teleports the rocket to the new level and ends the transaction.
         *
         * @param level The level to move the rocket to
         */
        void travelTo(ServerLevel level);
    }

    private static class SpaceData extends SavedData
    {
        private static final String DATA_NAME = Beyond.MOD_ID + "_space";

        private final List<Satellite> satellites;

        private SpaceData()
        {
            super(DATA_NAME);
            this.satellites = new ArrayList<>();
        }

        @Override
        public void load(CompoundTag nbt)
        {
            this.satellites.clear();
            ListTag satellitesNbt = nbt.getList("Satellites", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < satellitesNbt.size(); i++)
            {
                try
                {
                    CompoundTag satelliteNbt = satellitesNbt.getCompound(i);
                    Satellite.Type type = Satellite.Type.values()[satelliteNbt.getByte("Type") % Satellite.Type.values().length];
                    if (!type.shouldSave())
                        continue;
                    this.satellites.add(type.getCodec().parse(NbtOps.INSTANCE, satelliteNbt.get("Data")).getOrThrow(false, LOGGER::error));
                }
                catch (Exception ignored)
                {
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public CompoundTag save(CompoundTag nbt)
        {
            ListTag satellitesNbt = new ListTag();
            for (Satellite satellite : this.satellites)
            {
                if (!satellite.getType().shouldSave())
                    continue;
                try
                {
                    CompoundTag satelliteNbt = new CompoundTag();
                    satelliteNbt.putByte("Type", (byte) satellite.getType().ordinal());
                    satelliteNbt.put("Data", ((Codec<Satellite>) satellite.getCodec()).encodeStart(NbtOps.INSTANCE, satellite).getOrThrow(false, LOGGER::error));
                    satellitesNbt.add(satelliteNbt);
                }
                catch (Exception ignored)
                {
                }
            }
            nbt.put("Satellites", satellitesNbt);
            return nbt;
        }

        /**
         * Updates the satellites to be written to disk.
         *
         * @param satellites The satellites to write
         */
        private void update(Set<Satellite> satellites)
        {
            this.satellites.clear();
            this.satellites.addAll(satellites);
            this.satellites.sort(Comparator.comparingInt(Satellite::getId));
            this.setDirty();
        }
    }
}
