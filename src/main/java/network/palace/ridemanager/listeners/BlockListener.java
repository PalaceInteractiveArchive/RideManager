package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.player.Rank;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author Marc
 * @since 8/17/17
 */
public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) return;
        if (player.getRank().getRankId() < Rank.DEVELOPER.getRankId()) return;
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.placeBlock(player, event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
