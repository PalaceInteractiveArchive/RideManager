package network.palace.ridemanager.threads;

import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.sensors.RideSensor;
import org.bukkit.Location;

import java.util.LinkedList;

/**
 * Created by Marc on 5/2/17.
 */
public interface RideCallback {

    /**
     * Called when the result is done.
     *
     * @param name    the name of the ride
     * @param actions the list of ride actions
     * @param sensors the list of ride sensors
     * @param spawn   the spawn location
     * @param speed   the speed the ride moves when it first spawns
     * @param setYaw  Whether or not yaw values should be set automatically on actions
     */
    void done(String name, LinkedList<RideAction> actions, LinkedList<RideSensor> sensors, Location spawn, double speed, boolean setYaw);
}
