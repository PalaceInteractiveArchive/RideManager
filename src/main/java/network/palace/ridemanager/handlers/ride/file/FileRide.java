package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.CurrencyType;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import network.palace.ridemanager.utils.MovementUtil;
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
    private Optional<Cart> atStation = Optional.empty();
    private List<Cart> inRide = new ArrayList<>();
    private LinkedList<RideAction> actions = new LinkedList<>();
    @Getter @Setter private Location spawn = null;
    @Getter @Setter private double speed = 0;
    @Getter @Setter private boolean autoYaw = true;
    private boolean loading = false;
    private int taskID = 0;
    @Getter private final ModelMap modelMap;

    public FileRide(String name, String displayName, int riders, double delay, Location exit, String fileName) {
        super(name, displayName, riders, delay, exit, CurrencyType.BALANCE, 0);
        modelMap = RideManager.getMappingUtil().getMap(fileName);
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
        Core.runTaskAsynchronously(new FileRideLoader(this, rideFile, new RideCallback() {
            @Override
            public void done(String name, LinkedList<RideAction> list, Location spawn, double speed, boolean setYaw) {
                actions = list;
                setSpawn(spawn);
                setSpeed(speed);
                setAutoYaw(setYaw);
                loading = false;
            }
        }));
        if (delayInMillis < 0) return;
        taskID = Core.runTaskTimer(new Runnable() {
            @Override
            public void run() {
                if (loading) return;
                spawn(delayInMillis);
                Core.cancelTask(taskID);
            }
        }, 0L, 10L);
    }

    /**
     * Called periodically to move all carts to their next positions
     * <p>
     * Also update the cart in the station to keep it from falling
     */
    @Override
    public void move() {
        atStation.ifPresent(c -> c.setVelocity(new Vector(0, MovementUtil.getYMin(), 0)));
        for (Cart c : new ArrayList<>(inRide)) {
            if (c.isFinished()) {
                inRide.remove(c);
                continue;
            }
            c.move();
        }
    }

    /**
     * Despawn all carts
     */
    @Override
    public void despawn() {
        atStation.ifPresent(Cart::despawn);
        atStation = Optional.empty();
        for (Cart c : new ArrayList<>(inRide)) {
            c.despawn();
            inRide.remove(c);
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
        Cart atStation = this.atStation.get();
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
            if (s == null) break;
            s.addPassenger(tp);
            getOnRide().add(tp.getUniqueId());
            s = seats.get(sc++);
        }
        atStation.setSpawnTime(System.currentTimeMillis());
        inRide.add(atStation);
        atStation = null;
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
        if (!atStation.isPresent()) return false;
        UUID uuid = stand.getUniqueId();
        for (Seat seat : atStation.get().getSeats()) {
            if (!seat.getUniqueId().equals(uuid)) continue;
            if (seat.addPassenger(player)) {
                getOnRide().add(player.getUniqueId());
                return true;
            }
        }
        return true;
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
        ItemStack model = ItemUtil.create(Material.SHEARS, 1, (byte) 13);
        Cart c = new Cart(this, cartActions, model, modelMap);
        c.setPower(speed);
        c.spawn(spawn.clone());
        atStation = Optional.of(c);
    }

    public List<Cart> getCarts() {
        List<Cart> list = new ArrayList<>();
        atStation.ifPresent(list::add);
        list.addAll(inRide);
        return list;
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
        getCarts().forEach(c -> c.chunkLoaded(chunk));
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
        getCarts().forEach(c -> c.chunkUnloaded(chunk));
    }
}
