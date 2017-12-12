package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Seat {
    private final double x;
    private final double y;
    private final double z;
    private ArmorStand stand = null;

    public ArmorStand spawn(Location loc, FileRide ride) {
        Location newloc = getRelative(loc, loc.getYaw());
        stand = ride.lock(loc.getWorld().spawn(newloc, ArmorStand.class));
        stand.setHelmet(new ItemStack(Material.GOLD_BLOCK));
        stand.setBasePlate(true);
        return stand;
    }

    public void move(Location loc, float yaw) {
        if (stand == null) return;
        Ride.teleport(stand, getRelative(loc, yaw));
    }

    private Location getRelative(Location loc, float yaw) {
        double angle = (double) yaw;
        if (angle < 0) {
            angle += 360;
        }
        angle %= 360;
        double rad = Math.toRadians(angle);
        double x = (Math.cos(rad) * getX() - Math.sin(rad) * getZ()) + loc.getX();
        double z = (Math.sin(rad) * getX() + Math.cos(rad) * getZ()) + loc.getZ();
        return new Location(loc.getWorld(), x, loc.getY() + getY(), z, yaw, loc.getPitch());
    }

    public Seat copy() {
        return new Seat(x, y, z);
    }

    public void addPassenger(CPlayer tp) {
        if (stand != null) stand.addPassenger(tp.getBukkitPlayer());
    }

    public List<CPlayer> getPassengers() {
        if (stand == null) return new ArrayList<>();
        List<Entity> list = stand.getPassengers();
        if (list.isEmpty()) return new ArrayList<>();
        List<CPlayer> players = new ArrayList<>();
        for (Entity e : list) {
            if (!e.getType().equals(EntityType.PLAYER)) continue;
            players.add(Core.getPlayerManager().getPlayer(e.getUniqueId()));
        }
        return players;
    }

    public void removePassenger(CPlayer tp) {
        if (stand != null) stand.removePassenger(tp.getBukkitPlayer());
    }

    public boolean hasPassenger() {
        return stand != null && !stand.getPassengers().isEmpty();
    }

    public void despawn(Location exit) {
        if (stand == null) return;
        for (CPlayer p : getPassengers()) {
            removePassenger(p);
            p.teleport(exit);
        }
        stand.remove();
        stand = null;
    }
}
