package network.palace.ridemanager.handlers;

import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.ride.file.RideVehicle;
import network.palace.show.Show;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RideShow extends Show {
    private final RideVehicle vehicle;

    public RideShow(JavaPlugin plugin, File file, RideVehicle vehicle) {
        super(plugin, file);
        this.vehicle = vehicle;
    }

    @Override
    public List<UUID> getNearPlayers() {
        List<UUID> uuids = new ArrayList<>();
        for (CPlayer p : vehicle.getPassengers()) {
            uuids.add(p.getUniqueId());
        }
        return uuids;
    }
}
