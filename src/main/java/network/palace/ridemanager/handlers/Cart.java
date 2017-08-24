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

import java.util.*;

/**
 * Created by Marc on 5/2/17.
 */
public class Cart {
    @Getter private final FileRide ride;
    private final LinkedHashMap<Integer, RideAction> actions;
    @Getter @Setter private ArmorStand stand;
    @Getter private final ItemStack model;
    @Getter private double power = 0;
    @Getter @Setter private float yaw = 0;
    private int currentActionIndex = 0;

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, String modelName) {
        this(ride, actions, model, modelName, 0.1);
    }

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, String modelName, double power) {
        this.ride = ride;
        this.actions = actions;
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
        a.execute();
        if (a.isFinished()) {
            currentActionIndex++;
        }
    }

    public void teleport(Location loc) {
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        ride.teleport(stand, loc);
    }

    public void setPower(double p) {
        this.power = p > 1 ? 1 : (p < -1 ? -1 : p);
    }

    public Location getLocation() {
        Location loc = stand.getLocation();
        loc.setY(loc.getY() + MovementUtil.armorStandHeight);
        return loc;
    }

    public void despawn() {
        if (stand == null) return;
        if (!stand.getPassengers().isEmpty()) {
            for (Entity e : stand.getPassengers()) {
                stand.removePassenger(e);
                e.teleport(getRide().getExit());
            }
        }
        stand.remove();
        stand = null;
    }

    public void addPassenger(CPlayer tp) {
        stand.addPassenger(tp.getBukkitPlayer());
    }

    public void removePassenger(CPlayer tp) {
        stand.removePassenger(tp.getBukkitPlayer());
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
}
