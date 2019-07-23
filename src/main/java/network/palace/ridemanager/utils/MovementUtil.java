package network.palace.ridemanager.utils;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/15/17.
 */
public class MovementUtil {
    private List<Ride> rides = new ArrayList<>();
    private int taskid;
    public static final double armorStandHeight = 1.68888; //Meters
    @Getter private static long tick = 0;

    public MovementUtil() {
        taskid = Core.runTaskTimer(RideManager.getInstance(), () -> {
            for (Ride ride : new ArrayList<>(rides)) {
                ride.move();
            }
            tick++;
        }, 0L, 1L);
    }

    /**
     * Start tracking new ride
     *
     * @param ride the ride
     */
    public void addRide(Ride ride) {
        rides.add(ride);
    }

    /**
     * Stop tracking a ride
     *
     * @param ride the ride
     */
    public void removeRide(Ride ride) {
        rides.remove(ride);
    }

    /**
     * Get a list of all currently tracked rides
     *
     * @return an ArrayList of Rides
     */
    public List<Ride> getRides() {
        return new ArrayList<>(rides);
    }

    /**
     * Despawn all rides
     */
    public void despawnAll() {
        for (Ride ride : getRides()) {
            ride.despawn();
        }
    }

    /**
     * Shutdown command; despawns all rides and cancels the movement task, which cannot be restarted
     */
    public void stop() {
        despawnAll();
        Bukkit.getScheduler().cancelTask(taskid);
    }

    /**
     * Get a ride by its short name
     *
     * @param name the name
     * @return the Ride, or null if one wasn't found
     */
    public Ride getRide(String name) {
        for (Ride r : getRides()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Get a ride by one of the players riding it
     *
     * @param cp the player
     * @return the Ride, or null if the player isn't on a ride
     */
    public Ride getRide(CPlayer cp) {
        for (Ride ride : getRides()) {
            if (ride.getOnRide().contains(cp.getUniqueId())) {
                return ride;
            }
        }
        return null;
    }

    /**
     * Sit a player down on a ride
     *
     * @param player the player
     * @param stand  the ArmorStand they are being seated on
     * @return true if they do sit down, false if not (seat is full, can't board right now, etc.)
     * @implNote Player can't board a ride while sneaking; this fixes a problem where they would immediately be ejected
     */
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (player.getBukkitPlayer().isSneaking()) {
            player.sendMessage(ChatColor.RED + "You cannot board a ride while sneaking!");
            return false;
        }
        for (Ride ride : getRides()) {
            if (ride.sitDown(player, stand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a player clickes on an entity
     *
     * @param player the player
     * @param id     the id of the entity they click
     * @return whether or not they sit on a ride vehicle
     * @implNote Player can't board a ride while sneaking; this fixes a problem where they would immediately be ejected
     */
    public boolean sitDown(CPlayer player, int id) {
        boolean rideStand = false;
        for (Ride ride : getRides()) {
            if (ride.isRideStand(id)) {
                rideStand = true;
                break;
            }
        }
        if (!rideStand) {
            return false;
        }
        if (player.getBukkitPlayer().isSneaking()) {
            player.sendMessage(ChatColor.RED + "You cannot board a ride while sneaking!");
            return false;
        }
        for (Ride ride : getRides()) {
            if (ride.sitDown(player, id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Eject a player from all rides by their UUID
     *
     * @param uuid the uuid
     */
    public void ejectUUID(UUID uuid) {
        for (Ride ride : getRides()) {
            ride.removeFromOnRide(uuid);
        }
    }

    /**
     * Get smallest y value, commonly used for the y-value of velocity vectors
     *
     * @return Double.MIN_VALUE
     */
    public static double getYMin() {
        return Double.MIN_VALUE;
    }

    public static double pythag(double a, double b) {
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public static double pythag2(double a, double c) {
        return Math.sqrt(Math.pow(c, 2) - Math.pow(a, 2));
    }

    public static double sin(double amplitude, double period, double x) {
        return amplitude * Math.sin(period * x);
    }
}
