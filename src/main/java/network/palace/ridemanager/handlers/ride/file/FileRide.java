package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.sensors.RideSensor;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

/**
 * Created by Marc on 5/2/17.
 */
public class FileRide extends Ride {
    @Getter private File rideFile;
    @Getter @Setter private long lastSpawn = System.currentTimeMillis();
    private Optional<RideVehicle> atStation = Optional.empty();
    private List<RideVehicle> inRide = new ArrayList<>();
    private LinkedList<RideAction> actions = new LinkedList<>();
    private LinkedList<RideSensor> sensors = new LinkedList<>();
    @Getter @Setter private Location spawn = null;
    @Getter @Setter private double speed = 0;
    @Getter @Setter private boolean autoYaw = true;
    private boolean loading = false;
    private int taskID = 0;
    @Getter private ModelMap modelMap;
    private final String modelMapFileName;

    public FileRide(String name, String displayName, int riders, double delay, Location exit, String fileName, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId) {
        super(name, displayName, riders, delay, exit, currencyType, currencyAmount, honorAmount, achievementId);
        this.modelMapFileName = fileName;
        this.rideFile = new File("plugins/RideManager/rides/" + fileName + ".ride");
    }

    /**
     * Called by the queue manager to load from file and spawn the carts
     *
     * @param delayInMillis The amount of delay left before spawning
     */
    public void loadFile(long delayInMillis) {
        if (loading) {
            return;
        }
        loading = true;
        Core.runTaskAsynchronously(new FileRideLoader(this, rideFile, (name, actionList, sensorList, spawn, speed, setYaw) -> {
            actions = actionList;
            sensors = sensorList;
            setSpawn(spawn);
            setSpeed(speed);
            setAutoYaw(setYaw);
            loading = false;
        }));
        if (delayInMillis < 0) return;
        taskID = Core.runTaskTimer(() -> {
            if (loading) return;
            spawn(delayInMillis);
            Core.cancelTask(taskID);
        }, 0L, 10L);
    }

    /**
     * Called periodically to move all carts to their next positions
     * <p>
     * Also update the cart in the station to keep it from falling
     */
    @Override
    public void move() {
        atStation.ifPresent(v -> v.setVelocity(new Vector(0, MovementUtil.getYMin(), 0)));
        for (RideVehicle v : new ArrayList<>(inRide)) {
            if (v.isFinished()) {
                inRide.remove(v);
                continue;
            }
            v.move();
        }
    }

    /**
     * Despawn all carts
     */
    @Override
    public void despawn() {
        atStation.ifPresent(RideVehicle::despawn);
        atStation = Optional.empty();
        for (RideVehicle v : new ArrayList<>(inRide)) {
            v.despawn();
            inRide.remove(v);
        }
    }

    @Override
    public boolean handleEject(CPlayer player, boolean async) {
        return true;
    }

    @Override
    public void handleEject(CPlayer player, boolean async, boolean force) {
        if (!force) return;
        removeFromOnRide(player.getUniqueId());
        Cart cart = null;
        for (Cart c : getCarts()) {
            if (c.getPassengers().contains(player)) {
                cart = c;
                break;
            }
        }
        if (cart == null) return;
        Cart finalCart = cart;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                finalCart.removePassenger(player, false);
            }
        };
        if (async) {
            Core.runTask(task);
        } else {
            task.run();
        }
    }

    /**
     * Start a new cart for the list of players
     *
     * @param riders players to be teleported into the station
     */
    @Override
    public void start(List<CPlayer> riders) {
        if (!atStation.isPresent()) return;
        RideVehicle atStation = this.atStation.get();
        new RideStartEvent(this).call();
        for (CPlayer player : new ArrayList<>(riders)) {
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        List<Seat> seats = atStation.getSeats();
        int sc = 0;
        Seat s = seats.get(sc);
        for (CPlayer tp : riders) {
            while (s.hasPassenger()) {
                s = seats.get(sc++);
                if (s == null) break;
            }
            if (s == null) {
                tp.sendMessage(ChatColor.RED + "We ran out of seats, sorry!");
                tp.teleport(getExit());
                continue;
            }
            if (tp.getBukkitPlayer().isSneaking()) {
                tp.sendMessage(ChatColor.RED + "You cannot board a ride while sneaking!");
                tp.teleport(getExit());
                continue;
            }
            s.addPassenger(tp);
            addToOnRide(tp.getUniqueId());
            s = seats.get(sc++);
        }
        atStation.setSpawnTime(System.currentTimeMillis());
        inRide.add(atStation);
        this.atStation = Optional.empty();
    }

    /**
     * Called when a player clicks on an armor stand you can sit on
     *
     * @param player the player who clicked
     * @param stand  the stand they clicked
     * @return true if the stand belongs to this ride
     */
    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (!atStation.isPresent() || getOnRide().contains(player.getUniqueId())) return false;
        UUID uuid = stand.getUniqueId();
        for (Seat seat : atStation.get().getSeats()) {
            if (!seat.getUniqueId().equals(uuid) || seat.hasPassenger()) continue;
            if (seat.addPassenger(player)) {
                addToOnRide(player.getUniqueId());
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean sitDown(CPlayer player, int entityId) {
        if (!atStation.isPresent() || getOnRide().contains(player.getUniqueId())) return false;
        for (Seat seat : atStation.get().getSeats()) {
            if (seat.getEntityId() != entityId || seat.hasPassenger()) continue;
            addToOnRide(player.getUniqueId());
            Core.runTask(() -> seat.addPassenger(player));
            return true;
        }
        return false;
    }

    /**
     * Spawn a cart or load from file if no file has been loaded yet
     *
     * @param delayInMillis The value to pass through to the loadFile method
     */
    public void spawn(long delayInMillis) {
        if (spawn == null || actions.isEmpty()) {
            loadFile(delayInMillis);
            return;
        }
        lastSpawn = System.currentTimeMillis();
        LinkedHashMap<Integer, RideAction> cartActions = new LinkedHashMap<>();
        int i = 0;
        for (RideAction a : new ArrayList<>(actions)) {
            cartActions.put(i++, a.duplicate());
        }
        LinkedList<RideSensor> cartSensors = new LinkedList<>();
        for (RideSensor s : new ArrayList<>(sensors)) {
            cartSensors.add(s.duplicate());
        }
        if (modelMap == null) {
            modelMap = RideManager.getMappingUtil().getMap(modelMapFileName);
        }
        ItemStack model = modelMap.getItem();
        if (model == null) {
            model = ItemUtil.create(Material.SHEARS, 1, (byte) 11);
        }
        RideVehicle v = new RideVehicle(this, cartActions, cartSensors, model, modelMap, 1);
        v.setSpeed(speed);
        v.spawn(spawn.clone());
        atStation = Optional.of(v);
    }

    public List<Cart> getCarts() {
        List<Cart> list = new ArrayList<>();
        atStation.ifPresent(v -> list.addAll(v.getCarts()));
        inRide.forEach(v -> list.addAll(v.getCarts()));
        return list;
    }

    /**
     * Load all armor stands inside a chunk
     *
     * @param chunk the chunk that was loaded
     */
    @Override
    public void onChunkLoad(Chunk chunk) {
        getCarts().forEach(c -> c.chunkLoaded(chunk));
    }

    /**
     * Unload all armor stands inside a chunk
     *
     * @param chunk the chunk that was unloaded
     */
    @Override
    public void onChunkUnload(Chunk chunk) {
        getCarts().forEach(c -> c.chunkUnloaded(chunk));
    }

    @Override
    public boolean isRideStand(ArmorStand stand) {
        UUID uuid = stand.getUniqueId();
        for (Cart c : getCarts()) {
            if (!c.isSpawned()) continue;
            for (Seat s : c.getSeats()) {
                if (!s.isSpawned()) continue;
                if (uuid.equals(s.getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isRideStand(int id) {
        for (Cart c : getCarts()) {
            if (!c.isSpawned()) continue;
            for (Seat s : c.getSeats()) {
                if (!s.isSpawned()) continue;
                if (s.getEntityId() == id) {
                    return true;
                }
            }
        }
        return false;
    }
}
