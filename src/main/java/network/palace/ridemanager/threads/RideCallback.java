package network.palace.ridemanager.threads;

import network.palace.ridemanager.handlers.actions.RideAction;
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
     * @param spawn   the spawn location
     * @param speed   the speed the ride moves when it first spawns
     * @param setYaw  Whether or not yaw values should be set automatically on actions
     */
    void done(String name, LinkedList<RideAction> actions, Location spawn, double speed, boolean setYaw);
}
