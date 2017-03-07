package network.palace.ridemanager.listeners;

import network.palace.core.player.CPlayer;
import network.palace.ridemanager.events.PlayerLeaveRideEvent;
import network.palace.ridemanager.handlers.Ride;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Marc on 1/29/17.
 */
public class PlayerLeaveRide implements Listener {

    @EventHandler
    public void onPlayerLeaveRide(PlayerLeaveRideEvent event) {
        CPlayer player = event.getPlayer();
        Ride ride = event.getRide();
        if (ride.handleEject(player)) {
            event.setCancelled(false);
        }
    }
}
