package network.palace.ridemanager.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getWhoClicked().getUniqueId());
        BuildSession session = RideManager.getRideBuilderUtil().getSession(player);
        if (session == null) {
            return;
        }
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) {
            return;
        }
        String name = ChatColor.stripColor(inv.getName());
        switch (name) {
            case "Choose Action":
            case "New Action":
                session.handleInventoryClick(event, player, name);
                event.setCancelled(true);
                break;
        }
    }
}
