package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.actions.RideAction;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * Created by Marc on 5/2/17.
 */
public class Cart {
    @Getter private final FileRide ride;
    private final LinkedList<RideAction> actions;
    private final ArmorStand stand;
    @Getter private final ItemStack model;
    @Getter private double power = 0;
    private int recursiveProtect = 0;
    private int recursiveNum = 0;
    private long currentTick = 0;
    @Getter @Setter private float yaw = 0;

    public Cart(FileRide ride, LinkedList<RideAction> actions, Location loc, ItemStack model, int spawnAngle, String modelName) {
        this(ride, actions, loc, model, spawnAngle, modelName, 0.1);
    }

    public Cart(FileRide ride, LinkedList<RideAction> actions, Location loc, ItemStack model, int spawnAngle, String modelName, double power) {
        this.ride = ride;
        this.actions = actions;
        for (RideAction a : actions) {
            a.setCart(this);
        }
        setPower(power);
        this.model = model;
        this.yaw = spawnAngle;
        this.stand = loc.getWorld().spawn(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), yaw, 0), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setHelmet(model);
//        stand.setHeadPose(new EulerAngle(0, Math.toRadians(spawnAngle), 0));
    }

    public void move(long tick) {
        if (actions.isEmpty()) {
            return;
        }
        if (actions.get(0).getCart() == null) {
            return;
        }
        if (tick != currentTick) {
            currentTick = tick;
            recursiveProtect = 0;
            recursiveNum = 0;
        }
        RideAction a = actions.get(recursiveNum);
        a.execute();
        if (a.isFinished()) {
            actions.remove(recursiveNum);
        }
        if (!a.isMovementAction()) {
            recursiveProtect += 1;
            recursiveNum += 1;
            if (recursiveProtect < 5 && (actions.size() - 1) >= recursiveNum) {
                move(tick);
            }
        }
    }

    public void teleport(Location loc) {
        ride.teleport(stand, loc);
    }

    private double difference(double x, double y) {
        return Math.abs(x - y);
    }

    private double round(double value, int precision) {
        BigDecimal bd = new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    public void setPower(double p) {
        this.power = p > 1 ? 1 : (p < -1 ? -1 : p);
    }

    private boolean withinDistance(Location loc, Location target, double distance) {
        return loc.getX() >= target.getX() - distance && loc.getX() <= target.getX() + distance &&
                loc.getY() >= target.getY() - distance && loc.getY() <= target.getY() + distance &&
                loc.getZ() >= target.getZ() - distance && loc.getZ() <= target.getZ() + distance;
    }

    public Location getLocation() {
        return stand.getLocation();
    }

    public void despawn() {
        stand.remove();
    }

    public void addPassenger(CPlayer tp) {
        stand.addPassenger(tp.getBukkitPlayer());
    }
}
