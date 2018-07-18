package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Seat {
    private Optional<ArmorStand> stand = Optional.empty();

    private final double relative_x;
    private final double relative_y;
    private final double relative_z;

    @Getter private World world;
    @Getter private double x, y, z;
    @Getter private float yaw;
    @Getter private float pitch;
    private int chunkX;
    private int chunkZ;

    @Getter private boolean spawned = false;
    @Getter private Vector velocity = new Vector();

    public Seat(double relative_x, double relative_y, double relative_z, World world) {
        this.relative_x = relative_x;
        this.relative_y = relative_y;
        this.relative_z = relative_z;
        this.world = world;
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double getRelativeX() {
        return relative_x;
    }

    public double getRelativeY() {
        return relative_y;
    }

    public double getRelativeZ() {
        return relative_z;
    }

    private void updateLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        chunkX = MathUtil.floor(x) >> 4;
        chunkZ = MathUtil.floor(z) >> 4;
    }

    public Chunk getChunk() {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public void spawn(Location loc) {
        spawned = true;
        Location rel = getRelative(loc, loc.getYaw());

        updateLocation(rel.getWorld(), rel.getX(), rel.getY(), rel.getZ(), rel.getYaw(), rel.getPitch());

        if (!getChunk().isLoaded()) return;

        chunkLoaded();
    }

    public void move(Location loc) {
        move(loc, loc.getYaw());
    }

    public void move(Location loc, float yaw) {
        Location rel = getRelative(loc, yaw);
        updateLocation(rel.getWorld(), rel.getX(), rel.getY(), rel.getZ(), rel.getYaw(), rel.getPitch());
        stand.ifPresent(s -> {
            Ride.teleport(s, rel);
            s.setVelocity(velocity);
        });
    }

    public Location getRelative(Location loc, float yaw) {
        double angle = (double) yaw;
        if (angle < 0) {
            angle += 360;
        }
        angle %= 360;
        double rad = Math.toRadians(angle);
        double x = (Math.cos(rad) * getRelativeX() - Math.sin(rad) * getRelativeZ()) + loc.getX();
        double z = (Math.sin(rad) * getRelativeX() + Math.cos(rad) * getRelativeZ()) + loc.getZ();
        return new Location(loc.getWorld(), x, loc.getY() + getRelativeY(), z, yaw, loc.getPitch());
    }

    public boolean addPassenger(CPlayer tp) {
        return stand.map(armorStand -> armorStand.addPassenger(tp.getBukkitPlayer())).orElse(false);
    }

    public List<CPlayer> getPassengers() {
        if (!stand.isPresent()) return new ArrayList<>();
        List<Entity> list = stand.get().getPassengers();
        List<CPlayer> players = new ArrayList<>();
        for (Entity e : list) {
            if (!e.getType().equals(EntityType.PLAYER)) continue;
            players.add(Core.getPlayerManager().getPlayer(e.getUniqueId()));
        }
        return players;
    }

    public void removePassenger(CPlayer tp) {
        stand.ifPresent(s -> s.removePassenger(tp.getBukkitPlayer()));
    }

    public boolean hasPassenger() {
        return stand.isPresent() && !stand.get().getPassengers().isEmpty();
    }

    public void despawn(Location exit) {
        if (!spawned) return;
        stand.ifPresent(s -> {
            for (CPlayer p : getPassengers()) {
                removePassenger(p);
                p.teleport(exit == null ? p.getLocation() : exit);
            }
            s.remove();
        });
        stand = Optional.empty();
        spawned = false;
    }

    public void chunkLoaded() {
        if (!spawned || stand.isPresent()) return;

        Location loc = getLocation();

        ArmorStand stand = Ride.lock(loc.getWorld().spawn(loc, ArmorStand.class));
        stand.setVisible(false);
        stand.teleport(loc);
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
//        stand.setHelmet(ItemUtil.create(Material.GOLD_BLOCK));
        stand.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        this.stand = Optional.of(stand);
    }

    public void chunkUnloaded() {
        stand.ifPresent(Entity::remove);
        stand = Optional.empty();
    }

    public void setVelocity(Vector v) {
        this.velocity = v;
        stand.ifPresent(s -> s.setVelocity(v));
    }

    public Seat copy() {
        return new Seat(relative_x, relative_y, relative_z, world);
    }

    public UUID getUniqueId() {
        if (stand.isPresent()) {
            return stand.get().getUniqueId();
        } else {
            return UUID.randomUUID();
        }
    }
}
