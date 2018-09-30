package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.PlayerLeaveRideEvent;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Created by Marc on 1/29/17.
 */
public class PlayerLeaveRide implements Listener {

    @EventHandler
    public void onPlayerLeaveRide(PlayerLeaveRideEvent event) {
        CPlayer player = event.getPlayer();
        Ride ride = event.getRide();
        long timeOnRide = ride.getTimeOnRide(player.getUniqueId());
        if (timeOnRide == -1 || System.currentTimeMillis() - timeOnRide < 500) {
            event.setCancelled(true);
            return;
        }
        if (ride.handleEject(player, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) {
            ejectUUID(event.getPlayer().getUniqueId());
            return;
        }
        ejectPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) {
            ejectUUID(event.getPlayer().getUniqueId());
            return;
        }
        ejectPlayer(player);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) {
            ejectUUID(event.getPlayer().getUniqueId());
            return;
        }
        Ride ride = RideManager.getMovementUtil().getRide(player);
        if (ride == null) return;
        long timeOnRide = ride.getTimeOnRide(player.getUniqueId());
        if (timeOnRide == -1 || System.currentTimeMillis() - timeOnRide < 500) {
            return;
        }
        ejectPlayer(player);
    }

    /**
     * Eject a UUID from all rides
     *
     * @param uuid the uuid
     */
    private void ejectUUID(UUID uuid) {
        RideManager.getMovementUtil().ejectUUID(uuid);
    }

    /**
     * Eject a player from the ride they're currently on
     *
     * @param player the player
     * @implNote If they're not on a ride, nothing will happen
     */
    private void ejectPlayer(CPlayer player) {
        ejectPlayer(player, RideManager.getMovementUtil().getRide(player));
    }

    /**
     * Eject a player from a specific ride
     *
     * @param player the player
     * @param ride   the ride
     * @implNote If the ride is null, or if the player is not on the provided ride, nothing will happen
     */
    private void ejectPlayer(CPlayer player, Ride ride) {
        if (ride == null) return;
        ride.handleEject(player, true, true);
    }
}
