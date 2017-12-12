package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import network.palace.ridemanager.utils.MovementUtil;
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
    private Cart atStation = null;
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

    @Override
    public void move() {
        if (atStation != null) atStation.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        for (Cart c : new ArrayList<>(inRide)) {
            if (c.isFinished()) {
                inRide.remove(c);
                continue;
            }
            c.move();
        }
    }

    @Override
    public void despawn() {
        for (Cart c : new ArrayList<>(inRide)) {
            c.despawn();
            inRide.remove(c);
        }
    }

    @Override
    public void start(List<CPlayer> riders) {
        if (atStation == null) return;
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

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (atStation == null) return false;
        UUID uuid = stand.getUniqueId();
        for (Seat seat : atStation.getSeats()) {
            ArmorStand seatStand = seat.getStand();
            if (seatStand != null && seatStand.getUniqueId().equals(uuid) && !seat.hasPassenger()) {
                getOnRide().add(player.getUniqueId());
                seat.addPassenger(player);
                return true;
            }
        }
        return true;
    }

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
        Cart c = new Cart(this, cartActions, new ItemStack(Material.STONE), modelMap);
        c.setPower(speed);
        c.spawn(spawn.clone());
        atStation = c;
    }
}
