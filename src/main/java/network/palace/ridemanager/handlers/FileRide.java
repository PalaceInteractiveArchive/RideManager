package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    @Getter @Setter private double speed = 0;
    private boolean loading = false;
    private int taskID = 0;
    @Getter private final ModelMap modelMap;

    public FileRide(String name, String displayName, int riders, double delay, Location exit, String fileName) {
        super(name, displayName, riders, delay, exit);
        modelMap = RideManager.getMappingUtil().getMap(fileName);
        this.rideFile = new File("plugins/RideManager/rides/" + fileName + ".ride");
    }

    private void loadFile() {
        if (loading) {
            return;
        }
        loading = true;
        Bukkit.getScheduler().runTaskAsynchronously(RideManager.getInstance(), new FileRideLoader(this, rideFile, new RideCallback() {
            @Override
            public void done(LinkedList<RideAction> list, Location spawn, double speed) {
                actions = list;
                setSpawn(spawn);
                setSpeed(speed);
                loading = false;
            }
        }));
        taskID = Bukkit.getScheduler().runTaskTimer(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (loading) return;
                spawn();
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 0L, 10L).getTaskId();
    }

    @Override
    public void move() {
        for (Cart c : new ArrayList<>(carts)) {
            if (c.isFinished()) {
                carts.remove(c);
                continue;
            }
            c.move();
        }
    }

    @Override
    public void despawn() {
        for (Cart c : new ArrayList<>(carts)) {
            c.despawn();
            carts.remove(c);
        }
    }

    @Override
    public void start(List<CPlayer> riders) {
        loadFile();
//        if (spawn == null || actions.isEmpty()) {
//            loadFile();
//        } else {
//            spawn();
//        }
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        return true;
    }

    private void spawn() {
        lastSpawn = System.currentTimeMillis();
        LinkedHashMap<Integer, RideAction> cartActions = new LinkedHashMap<>();
        int i = 0;
        for (RideAction a : new ArrayList<>(actions)) {
            cartActions.put(i++, a.duplicate());
        }
        Cart c = new Cart(this, cartActions, new ItemStack(Material.STONE), "");
        c.setPower(speed);
        carts.add(c);
    }
}
