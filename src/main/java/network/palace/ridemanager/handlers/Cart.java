package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Marc on 5/2/17.
 */
public class Cart {
    @Getter private final FileRide ride;
    private final LinkedHashMap<Integer, RideAction> actions;
    @Getter private final ModelMap map;
    @Getter @Setter private ArmorStand stand;
    @Getter private final ItemStack model;
    @Getter private double power = 0;
    @Getter @Setter private float yaw = 0;
    @Getter private int currentActionIndex = 0;
    @Getter @Setter private long spawnTime;
    private List<Seat> seats = new ArrayList<>();

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map) {
        this(ride, actions, model, map, 0.1);
    }

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map, double power) {
        this.ride = ride;
        this.actions = actions;
        this.map = map;
        for (RideAction a : this.actions.values()) {
            a.setCart(this);
        }
        setPower(power);
        this.model = model;
//        this.yaw = spawnAngle;
//        this.stand = loc.getWorld().spawn(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), yaw, 0), ArmorStand.class);
//        stand.setVisible(false);
//        stand.setGravity(false);
//        stand.setHelmet(model);
//        stand.setHeadPose(new EulerAngle(0, Math.toRadians(spawnAngle), 0));
    }

    public void move() {
        if (actions.isEmpty()) {
            despawn();
            return;
        }
        RideAction a = actions.get(currentActionIndex);
        if (a == null || a.getCart() == null || !a.getCart().equals(this)) {
            despawn();
            return;
        }
//        // Pause for 3 seconds before moving cart
//        if (getCurrentActionIndex() > 0 && System.currentTimeMillis() - getSpawnTime() < 3000 && getSpawnTime() != 0) {
//            Vector v = new Vector(0, MovementUtil.getYMin(), 0);
//            stand.setVelocity(v);
//            for (Seat s : getSeats()) {
//                s.getStand().setVelocity(v);
//            }
//            return;
//        }
        a.execute();
        if (a.isFinished()) {
            currentActionIndex++;
        }
    }

    public void teleport(Location loc) {
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        if (getRide().isAutoYaw()) setYaw(loc.getYaw());
        ride.teleport(stand, loc);
        for (Seat s : getSeats()) {
            s.move(loc, yaw);
        }
    }

    public void setPower(double p) {
        this.power = p > 1 ? 1 : (p < -1 ? -1 : p);
    }

    public Location getLocation() {
        Location loc = stand.getLocation();
        loc.setY(loc.getY() + MovementUtil.armorStandHeight);
        return loc;
    }

    public void spawn(Location loc) {
        setYaw(loc.getYaw());
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        ArmorStand stand = ride.lock(loc.getWorld().spawn(loc, ArmorStand.class));
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setHelmet(getModel());
        stand.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        setStand(stand);
        ModelMap map = getMap();
        for (Seat seat : map.getSeats()) {
            Seat copy = seat.copy();
            copy.spawn(loc, ride);
            addSeat(copy);
        }
    }

    public void despawn() {
        if (stand != null) {
            if (!stand.getPassengers().isEmpty()) {
                for (Entity e : stand.getPassengers()) {
                    getRide().getOnRide().remove(e.getUniqueId());
                    stand.removePassenger(e);
                    e.teleport(getRide().getExit());
                }
            }
            stand.remove();
            stand = null;
        }
        if (!seats.isEmpty()) {
            for (Seat s : getSeats()) {
                s.getPassengers().forEach(p -> getRide().getOnRide().remove(p.getUniqueId()));
                s.despawn(getRide().getExit());
            }
        }
    }

    public void addPassenger(CPlayer tp) {
        if (seats.size() > 0) {
            for (Seat s : getSeats()) {
                if (s.hasPassenger()) continue;
                getRide().getOnRide().add(tp.getUniqueId());
                s.addPassenger(tp);
                break;
            }
            return;
        }
        if (!stand.getPassengers().isEmpty()) return;
        stand.addPassenger(tp.getBukkitPlayer());
    }

    public void removePassenger(CPlayer tp) {
        stand.removePassenger(tp.getBukkitPlayer());
        for (Seat s : getSeats()) {
            s.removePassenger(tp);
        }
        tp.teleport(getRide().getExit());
    }

    public List<CPlayer> getPassengers() {
        List<CPlayer> list = new ArrayList<>();
        for (Entity e : stand.getPassengers()) {
            CPlayer p = Core.getPlayerManager().getPlayer(e.getUniqueId());
            if (p != null) {
                list.add(p);
            } else {
                stand.removePassenger(e);
            }
        }
        return list;
    }

    public void empty() {
        for (CPlayer p : getPassengers()) {
            removePassenger(p);
        }
    }

    public RideAction getPreviousAction() {
        if (currentActionIndex == 0) return null;
        return getPreviousAction(actions.get(currentActionIndex).getId());
    }

    public RideAction getPreviousAction(UUID id) {
        int i = -1;
        for (RideAction a : new ArrayList<>(actions.values())) {
            if (a.getId().equals(id)) {
                return actions.get(i);
            }
            i++;
        }
        return null;
    }

    public RideAction getNextAction(UUID id) {
        int i = 0;
        for (Map.Entry<Integer, RideAction> a : new HashMap<>(actions).entrySet()) {
            if (a.getValue().getId().equals(id)) {
                i = a.getKey();
                break;
            }
        }
        if (i < 0) {
            return null;
        }
        return actions.get(i + 1);
    }

    public boolean isFinished() {
        return stand == null && currentActionIndex >= actions.size();
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public void setVelocity(Vector v) {
        stand.setVelocity(v);
        getSeats().forEach(s -> s.getStand().setVelocity(v));
    }
}
