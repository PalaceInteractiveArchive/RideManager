package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.AerialCarouselRide;
import network.palace.ridemanager.handlers.Ride;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Marc on 3/12/17.
 */
public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        CPlayer cp = Core.getPlayerManager().getPlayer(event.getPlayer());
        ItemStack hand = cp.getInventory().getItemInMainHand();
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Ride ride = RideManager.getMovementUtil().getRide(cp);
        if (!(ride instanceof AerialCarouselRide)) {
            return;
        }
        AerialCarouselRide acr = (AerialCarouselRide) ride;
        acr.click(cp);
    }
}
