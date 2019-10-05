package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor
public class ChunkStand {
    @Getter private Optional<ArmorStand> stand = Optional.empty();
    @Getter private boolean spawned = false;
    @Getter private boolean gravity = true;
    @Getter private World world;
    @Getter private double x, y, z;
    private int chunkX, chunkZ;
    @Getter @Setter private float yaw = 0;
    @Getter @Setter private float pitch = 0;
    private ItemStack helmet = null;
    @Getter private Vector velocity = new Vector(0, MovementUtil.getYMin(), 0);
    @Getter private EulerAngle headPose = null;

    public ChunkStand(Location loc) {
        this(loc, true);
    }

    public ChunkStand(Location loc, boolean gravity) {
        this(loc, gravity, null);
    }

    public ChunkStand(Location loc, boolean gravity, EulerAngle headPose) {
        this.gravity = gravity;
        this.headPose = headPose;
        updateLocation(loc);
    }

    private void updateLocation(World world, double x, double y, double z) {
        updateLocation(world, x, y, z, 0, 0);
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

    private void updateLocation(Location loc) {
        updateLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch).add(0, MovementUtil.armorStandHeight, 0);
    }

    public Chunk getChunk() {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public void spawn() {
        spawn(getLocation());
    }

    public void spawn(Location loc) {
        setYaw(loc.getYaw());
        setPitch(loc.getPitch());
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        updateLocation(loc);
        spawned = true;

        if (!getChunk().isLoaded()) return;

        chunkLoaded(getChunk());
    }

    public void despawn() {
        spawned = false;

        if (!getChunk().isLoaded()) return;

        chunkUnloaded(getChunk());
    }

    public void chunkLoaded(Chunk c) {
        if (!spawned || stand.isPresent() || !c.equals(getChunk())) return;

        Location loc = getLocation().clone().add(0, -MovementUtil.armorStandHeight, 0);

        ArmorStand stand = Ride.lock(loc.getWorld().spawn(loc, ArmorStand.class));
        stand.setVisible(false);
        stand.setHelmet(helmet);
        stand.setGravity(gravity);
        stand.setVelocity(velocity);
        if (headPose != null) stand.setHeadPose(headPose);
        this.stand = Optional.of(stand);
    }

    public void chunkUnloaded(Chunk c) {
        if (!c.equals(getChunk())) return;

        stand.ifPresent(Entity::remove);
        stand = Optional.empty();
    }

    public void teleport(Location loc) {
        teleport(loc, false);
    }

    public void teleport(Location loc, boolean adjust) {
        if (adjust) {
            loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        }
        teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    private void teleport(World world, double x, double y, double z) {
        teleport(world, x, y, z, getYaw(), getPitch());
    }

    private void teleport(World world, double x, double y, double z, float yaw, float pitch) {
        updateLocation(world, x, y, z, yaw, pitch);
        Location loc = new Location(world, x, y, z, yaw, pitch);
        stand.ifPresent(armorStand -> {
            Ride.teleport(armorStand, loc);
            armorStand.setVelocity(velocity);
        });
    }

    public void setHeadPose(EulerAngle headPose) {
        this.headPose = headPose;
        stand.ifPresent(armorStand -> armorStand.setHeadPose(headPose));
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
        stand.ifPresent(armorStand -> armorStand.setGravity(gravity));
    }

    public void setHelmet(ItemStack item) {
        this.helmet = item;
        stand.ifPresent(armorStand -> armorStand.setHelmet(item));
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
        stand.ifPresent(armorStand -> armorStand.setVelocity(velocity));
    }

    public boolean addPassenger(CPlayer player) {
        return player.getBukkitPlayer() != null && stand.isPresent() && stand.get().getPassengers().isEmpty() && stand.get().addPassenger(player.getBukkitPlayer());
    }

    public UUID getPassenger() {
        return stand.filter(armorStand -> !armorStand.getPassengers().isEmpty())
                .map(armorStand -> armorStand.getPassengers().get(0).getUniqueId()).orElse(null);
    }

    public UUID getUniqueId() {
        return stand.map(Entity::getUniqueId).orElseGet(UUID::randomUUID);
    }

    public int getEntityId() {
        return stand.map(Entity::getEntityId).orElse(-1);
    }
}
