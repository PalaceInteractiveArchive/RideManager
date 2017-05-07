package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marc on 5/2/17.
 */
public class FileRide extends Ride {
    @Getter private File rideFile;
    @Getter @Setter private long lastSpawn = System.currentTimeMillis();
    private List<Cart> carts = new ArrayList<>();
    private LinkedList<RideAction> actions = new LinkedList<>();
    @Getter @Setter private Location spawn = null;
    @Getter @Setter private int spawnAngle = 0;
    @Getter @Setter private double speed = 0;
    private boolean loading = false;
    private int taskID = 0;

    public FileRide(String name, String displayName, int riders, double delay, Location exit, String fileName) {
        super(name, displayName, riders, delay, exit);
        this.rideFile = new File("plugins/RideManager/rides/" + fileName + ".ride");
    }

    private void loadFile() {
        if (loading) {
            return;
        }
        loading = true;
        Bukkit.getScheduler().runTaskAsynchronously(RideManager.getInstance(), new FileRideLoader(this, rideFile, new RideCallback() {
            @Override
            public void done(LinkedList<RideAction> list, Location spawn, int spawnAngle, double speed) {
                actions = list;
                setSpawn(spawn);
                setSpawnAngle(spawnAngle);
                setSpeed(speed);
                loading = false;
            }
        }));
        taskID = Bukkit.getScheduler().runTaskTimer(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (!loading) {
                    spawn();
                    Bukkit.getScheduler().cancelTask(taskID);
                }
            }
        }, 0L, 10L).getTaskId();
    }

    @Override
    public void move() {
        long tick = MovementUtil.getTick();
        for (Cart c : carts) {
            c.move(tick);
        }
    }

    @Override
    public void despawn() {
        for (Cart c : carts) {
            c.despawn();
        }
    }

    @Override
    public void start() {
        loadFile();
//        if (spawn == null || actions.isEmpty()) {
//            loadFile();
//        } else {
//            spawn();
//        }
    }

    private void spawn() {
        lastSpawn = System.currentTimeMillis();
        LinkedList<RideAction> cartActions = new LinkedList<>();
        cartActions.addAll(actions);
        Cart c = new Cart(this, cartActions, spawn, new ItemStack(Material.STONE), spawnAngle, "");
        c.setPower(speed);
        carts.add(c);
    }
}
