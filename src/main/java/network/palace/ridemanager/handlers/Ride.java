package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_11_R1.Entity;
import network.palace.core.player.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/22/17.
 */
public abstract class Ride {
    @Getter private String name;
    @Getter private String displayName;
    @Getter private int riders;
    @Getter private double delay;
    @Getter @Setter private Location exit;
    @Getter private List<UUID> queue = new ArrayList<>();
    @Getter private List<UUID> onRide = new ArrayList<>();

    public Ride(String name, String displayName, int riders, double delay, Location exit) {
        this.name = name;
        this.displayName = displayName;
        this.riders = riders;
        this.delay = delay;
        this.exit = exit;
    }

    public abstract void move();

    public abstract void despawn();

    public abstract void start();

    public boolean handleEject(CPlayer player) {
        return false;
    }

    public void joinQueue(Player tp) {
        queue.add(tp.getUniqueId());
        tp.sendMessage(ChatColor.GREEN + "You joined the queue for " + displayName + "!");
    }

    public void teleport(org.bukkit.entity.Entity entity, Location loc) {
        Entity e = ((CraftEntity) entity).getHandle();
        Location cur = entity.getLocation();
        e.locX = loc.getX();
        e.locY = loc.getY();
        e.locZ = loc.getZ();
        e.yaw = loc.getYaw();
        e.pitch = loc.getPitch();
        e.motX = Math.abs(cur.getX() - loc.getX());
        e.motY = Math.abs(cur.getY() - loc.getY());
        e.motZ = Math.abs(cur.getZ() - loc.getZ());
        e.positionChanged = true;
        e.velocityChanged = true;
    }

    public Location getRelativeLocation(double angle, double radius, Location center) {
        if (angle < 0) {
            angle = 360 + angle;
        }
        double rad = Math.toRadians(angle);
        double x = Math.sin(rad) * radius;
        double z = Math.cos(rad) * radius;
        return center.clone().add(x, 0, z);
    }

    public double getAngleFromDirection(BlockFace direction) {
        switch (direction) {
            case NORTH:
                return 180;
            case EAST:
                return -90;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case NORTH_EAST:
                return -135;
            case NORTH_WEST:
                return 135;
            case SOUTH_EAST:
                return -45;
            case SOUTH_WEST:
                return 45;
            case WEST_NORTH_WEST:
                return 112.5;
            case NORTH_NORTH_WEST:
                return 157.5;
            case NORTH_NORTH_EAST:
                return -157.5;
            case EAST_NORTH_EAST:
                return -112.5;
            case EAST_SOUTH_EAST:
                return -67.5;
            case SOUTH_SOUTH_EAST:
                return -22.5;
            case SOUTH_SOUTH_WEST:
                return 22.5;
            case WEST_SOUTH_WEST:
                return 67.5;
        }
        return 0;
    }
}
