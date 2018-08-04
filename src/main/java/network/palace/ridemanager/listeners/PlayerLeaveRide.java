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
        if (ride.handleEject(player)) {
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
        ejectPlayer(player);
    }

    private void ejectUUID(UUID uuid) {
        RideManager.getMovementUtil().ejectUUID(uuid);
    }

    private void ejectPlayer(CPlayer player) {
        Ride ride = RideManager.getMovementUtil().getRide(player);
        if (ride == null) return;
        ride.handleEject(player, true);
    }
}
