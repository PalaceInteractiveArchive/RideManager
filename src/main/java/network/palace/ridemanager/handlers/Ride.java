package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityArmorStand;
import network.palace.core.Core;
import network.palace.core.economy.EconomyManager;
import network.palace.core.player.CPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/22/17.
 */
public abstract class Ride {
    @Getter private World world;
    @Getter private String name;
    @Getter private String displayName;
    @Getter private int riders;
    @Getter private double delay;
    @Getter @Setter private Location exit;
    @Getter private List<UUID> onRide = new ArrayList<>();
    @Getter private final CurrencyType currencyType;
    @Getter private final int currencyAmount;

    public Ride(String name, String displayName, int riders, double delay, Location exit, CurrencyType currencyType, int currencyAmount) {
        this.world = Bukkit.getWorlds().get(0);
        this.name = name;
        this.displayName = displayName;
        this.riders = riders;
        this.delay = delay;
        this.exit = exit;
        this.currencyType = currencyType;
        this.currencyAmount = currencyAmount;
    }

    public abstract void move();

    public abstract void despawn();

    public abstract void start(List<CPlayer> riders);

    public boolean handleEject(CPlayer player) {
        return false;
    }

    public void teleport(org.bukkit.entity.Entity entity, Location loc) {
        Entity e = ((CraftEntity) entity).getHandle();
        e.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        e.h(loc.getYaw());
        e.world.entityJoinedWorld(e, false);
        /*
        Location cur = entity.getLocation();
        double y = loc.getY() - cur.getY();
//        entity.teleport(loc);
//        entity.setVelocity(new Vector(loc.getX() - cur.getX(), y != 0 ? y : Double.MIN_VALUE, loc.getZ() - cur.getZ()));
        Entity e = ((CraftEntity) entity).getHandle();
        if (position) {
            e.locX = loc.getX();
            e.locY = loc.getY();
            e.locZ = loc.getZ();
            e.positionChanged = true;
        }
        e.yaw = loc.getYaw();
        e.pitch = loc.getPitch();
        e.motX = loc.getX() - cur.getX();
        e.motY = y != 0 ? y : Double.MIN_VALUE;
        e.motZ = loc.getZ() - cur.getZ();
        e.velocityChanged = true;*/
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

    public void loadSurroundingChunks(Location loc) {
        Chunk c = loc.getChunk();
        World w = c.getWorld();
        for (int x = c.getX() - 2; x < c.getX() + 4; x++) {
            for (int z = c.getZ() - 2; z < c.getZ() + 4; z++) {
                Chunk at = w.getChunkAt(x, z);
                if (!at.isLoaded()) {
                    at.load();
                }
            }
        }
    }

    public void rewardCurrency(UUID[] uuids) {
        EconomyManager man = Core.getEconomy();
        if (man == null) return;
        for (UUID uuid : uuids) {
            if (Core.getPlayerManager().getPlayer(uuid) == null) continue;
            switch (currencyType) {
                case BALANCE:
                    man.addBalance(uuid, currencyAmount, name);
                    break;
                case TOKENS:
                    man.addTokens(uuid, currencyAmount, name);
                    break;
            }
        }
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

    public ArmorStand lock(ArmorStand stand) {
        try {
            Field f = EntityArmorStand.class.getDeclaredField("bA");
            if (f != null) {
                f.setAccessible(true);
                f.set(((CraftArmorStand) stand).getHandle(), 2096896);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return stand;
    }

    public abstract boolean sitDown(CPlayer player, ArmorStand stand);
}
