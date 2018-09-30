package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.sensors.RideSensor;
import network.palace.ridemanager.handlers.ride.ModelMap;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class RideVehicle {
    @Getter private final FileRide ride;
    private final LinkedHashMap<Integer, RideAction> cartActions;
    private final LinkedList<RideSensor> sensors;
    private final ItemStack model;
    private final ModelMap modelMap;
    private final int cartCount;
    @Getter private double speed = 0;
    @Getter private List<Cart> carts = new ArrayList<>();
    @Getter @Setter private long spawnTime;

    public RideVehicle(FileRide ride, LinkedHashMap<Integer, RideAction> cartActions, LinkedList<RideSensor> sensors, ItemStack model, ModelMap modelMap) {
        this(ride, cartActions, sensors, model, modelMap, 0.1);
    }

    public RideVehicle(FileRide ride, LinkedHashMap<Integer, RideAction> cartActions, LinkedList<RideSensor> sensors, ItemStack model, ModelMap modelMap, double speed) {
        this.ride = ride;
        this.cartActions = cartActions;
        this.sensors = sensors;
        this.model = model;
        this.modelMap = modelMap;
        this.cartCount = modelMap.getCartCount();
        setSpeed(speed);
        this.sensors.forEach(s -> s.setVehicle(this));
    }

    public void move() {
        for (Cart c : new ArrayList<>(carts)) {
            if (c.isFinished()) {
                carts.remove(c);
                continue;
            }
            c.move();
        }
        final Location currentLocation = carts.get(0).getLocation();
        for (RideSensor sensor : sensors) {
            if (!sensor.isActivated() && sensor.isInRadius(currentLocation)) {
                sensor.activate();
            }
        }
    }

    public boolean isFinished() {
        for (Cart c : carts) {
            if (c.isFinished()) return true;
        }
        return false;
    }

    public void setSpeed(double p) {
        this.speed = p > 1 ? 1 : (p < 0 ? 0 : p);
    }

    public List<CPlayer> getPassengers() {
        List<CPlayer> list = new ArrayList<>();
        for (Cart c : carts) {
            list.addAll(c.getPassengers());
        }
        return list;
    }

    public void setVelocity(Vector v) {
        carts.forEach(c -> c.setVelocity(v));
    }

    public void despawn() {
        carts.forEach(Cart::despawn);
    }

    public List<Seat> getSeats() {
        List<Seat> list = new ArrayList<>();
        carts.forEach(c -> list.addAll(c.getSeats()));
        return list;
    }

    public void spawn(Location loc) {
        float yaw = loc.getYaw();
        double rad = Math.toRadians(yaw < 0 ? yaw + 360 : yaw);
        double x = Math.sin(rad);
        double z = Math.cos(rad);
        Vector change = new Vector(x, 0, z).multiply(modelMap.getLength() + modelMap.getSpaceBetweenCarts());

        for (int i = 0; i < cartCount; i++) {
            LinkedHashMap<Integer, RideAction> cartActions = new LinkedHashMap<>();
            int n = 0;
            for (RideAction a : new ArrayList<>(this.cartActions.values())) {
                cartActions.put(n++, a.duplicate());
            }
            Cart c = new Cart(this, cartActions, model, modelMap);
            c.setSpeed(speed);
            c.spawn(loc.clone());
            loc.add(change);
            carts.add(c);
        }
    }

    public boolean isAutoYaw() {
        return ride.isAutoYaw();
    }
}
