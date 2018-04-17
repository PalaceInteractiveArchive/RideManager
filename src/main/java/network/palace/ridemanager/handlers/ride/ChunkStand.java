package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ChunkStand {
    private List<Optional<ArmorStand>> stands = new ArrayList<>();
    @Getter private boolean spawned = false;
    @Getter private World world;
    @Getter private double x, y, z;
    private int chunkX, chunkZ;
    @Getter @Setter private float yaw = 0;

    public ChunkStand(Location loc) {
        updateLocation(loc);
        spawn(loc);
    }

    private void updateLocation(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        chunkX = MathUtil.floor(x) >> 4;
        chunkZ = MathUtil.floor(z) >> 4;
    }

    private void updateLocation(Location loc) {
        updateLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    public Location getLocation() {
        return new Location(world, x, y, z).add(0, MovementUtil.armorStandHeight, 0);
    }

    public Chunk getChunk() {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public void spawn(Location loc) {
        setYaw(loc.getYaw());
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        updateLocation(loc);
        spawned = true;

        if (!getChunk().isLoaded()) return;

        chunkLoaded(getChunk());
    }

    public void chunkLoaded(Chunk c) {
        if (!spawned || stands.isEmpty() || present(stands) || !c.equals(getChunk()) || !c.isLoaded()) return;

        Location loc = getLocation();

        for (Optional<ArmorStand> opt : stands) {
            ArmorStand stand = Ride.lock(loc.getWorld().spawn(loc, ArmorStand.class));
            opt = Optional.of(stand);
        }

        Bukkit.broadcastMessage("Spawned!");
    }

    public void teleport(Location loc) {
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        setYaw(loc.getYaw());
        teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    private void teleport(World world, double x, double y, double z) {
        updateLocation(world, x, y, z);
        Location loc = new Location(world, x, y, z);
        loc.setYaw(getYaw());

        stands.forEach(s -> {
            if (!s.isPresent()) return;
            Ride.teleport(s.get(), loc);
        });
    }

    private boolean present(List<Optional<ArmorStand>> stands) {
        for (Optional<ArmorStand> opt : stands) {
            if (!opt.isPresent()) return false;
        }
        return true;
    }
}
