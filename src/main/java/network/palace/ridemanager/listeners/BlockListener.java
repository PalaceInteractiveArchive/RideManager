package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author Marc
 * @since 8/17/17
 */
public class BlockListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        RideBuilderUtil.BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.placeBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
