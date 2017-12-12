package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.player.Rank;
import network.palace.ridemanager.RideManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerMove implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null || player.getRank().getRankId() < Rank.MOD.getRankId()) return;
        RideManager.getRideBuilderUtil().moveEvent(player, event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null || player.getRank().getRankId() < Rank.MOD.getRankId()) return;
        RideManager.getRideBuilderUtil().toggleShift(player, event.isSneaking());
    }
}
