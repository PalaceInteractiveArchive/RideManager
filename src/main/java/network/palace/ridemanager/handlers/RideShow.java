package network.palace.ridemanager.handlers;

import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.ride.file.Cart;
import network.palace.show.Show;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RideShow extends Show {
    private final Cart cart;

    public RideShow(JavaPlugin plugin, File file, Cart cart) {
        super(plugin, file);
        this.cart = cart;
    }

    @Override
    public List<UUID> getNearPlayers() {
        List<UUID> uuids = new ArrayList<>();
        for (CPlayer p : cart.getPassengers()) {
            uuids.add(p.getUniqueId());
        }
        return uuids;
    }
}
