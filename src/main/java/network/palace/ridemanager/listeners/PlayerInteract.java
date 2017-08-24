package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.AerialCarouselRide;
import network.palace.ridemanager.handlers.Ride;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Marc on 3/12/17.
 */
public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        CPlayer cp = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (cp == null) return;
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Ride ride = RideManager.getMovementUtil().getRide(cp);
        if (ride == null || (ride != null && !(ride instanceof AerialCarouselRide))) {
            return;
        }
        if (cp.getInventory().getHeldItemSlot() != 4) {
            return;
        }
        AerialCarouselRide acr = (AerialCarouselRide) ride;
        if (!acr.isCanFly()) {
            return;
        }
        AerialCarouselRide.Vehicle v = acr.getVehicle(cp.getUniqueId());
        switch (v.getFlyingState()) {
            case HOVERING: {
                if (v.getLastFlyingState().equals(AerialCarouselRide.FlyingState.ASCENDING)) {
                    v.setFlyingState(AerialCarouselRide.FlyingState.DESCENDING);
                } else {
                    v.setFlyingState(AerialCarouselRide.FlyingState.ASCENDING);
                }
                cp.playSound(cp.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 0.6f);
                break;
            }
            case ASCENDING:
            case DESCENDING:
                v.setLastFlyingState(v.getFlyingState());
                v.setFlyingState(AerialCarouselRide.FlyingState.HOVERING);
                cp.playSound(cp.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 0.5f);
                break;
        }
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) return;
        Entity e = event.getRightClicked();
        if (!e.getType().equals(EntityType.ARMOR_STAND)) return;
        ArmorStand stand = (ArmorStand) e;
        if (RideManager.getMovementUtil().sitDown(player, stand)) {
            event.setCancelled(true);
        }
    }
}
