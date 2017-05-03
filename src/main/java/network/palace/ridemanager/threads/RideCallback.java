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
     * @param actions the list of ride actions
     * @param spawn   the spawn location
     */
    void done(LinkedList<RideAction> actions, Location spawn);
}
