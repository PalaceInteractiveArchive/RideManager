package network.palace.ridemanager.handlers.ride;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.currency.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.events.RideMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
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
    private HashMap<UUID, Long> onRide = new HashMap<>();
    @Getter private final CurrencyType currencyType;
    @Getter private final int currencyAmount;
    @Getter private final int honorAmount;
    @Getter private final int achievementId;

    /**
     * Main constructor
     *
     * @param name           The reference name of the ride, like "iasw"
     * @param displayName    The display name of the ride, like "it's a small world"
     * @param riders         The number of players allowed in each group
     * @param delay          The delay between groups
     * @param exit           The location of the exit of the ride
     * @param currencyType   The type of currency to reward
     * @param currencyAmount The amount of the currency to reward
     * @param honorAmount    The amount of honor to reward
     * @param achievementId  The achievement ID to reward players
     * @implNote If no achievement is desired, use -1
     */
    public Ride(String name, String displayName, int riders, double delay, Location exit, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId) {
        this.world = Bukkit.getWorlds().get(0);
        this.name = name;
        this.displayName = displayName;
        this.riders = riders;
        this.delay = delay;
        this.exit = exit;
        this.currencyType = currencyType;
        this.currencyAmount = currencyAmount;
        this.honorAmount = honorAmount;
        this.achievementId = achievementId;
    }

    public List<UUID> getOnRide() {
        return ImmutableList.copyOf(onRide.keySet());
    }

    public void addToOnRide(UUID uuid) {
        onRide.put(uuid, System.currentTimeMillis());
    }

    public long getTimeOnRide(UUID uuid) {
        return onRide.containsKey(uuid) ? onRide.get(uuid) : -1;
    }

    public long removeFromOnRide(UUID uuid) {
        if (!onRide.containsKey(uuid)) {
            return -1;
        } else {
            return onRide.remove(uuid);
        }
    }

    public void clearOnRide() {
        onRide.clear();
    }

    public abstract void start(List<CPlayer> riders);

    public abstract void move();

    public abstract void despawn();

    public boolean handleEject(CPlayer player, boolean async) {
        return false;
    }

    public abstract void handleEject(CPlayer player, boolean async, boolean force);

    public static void teleport(org.bukkit.entity.Entity entity, Location loc) {
        try {
            if (!entity.getPassengers().isEmpty()) new RideMoveEvent(entity, entity.getLocation(), loc).call();

            Method getHandle = entity.getClass().getMethod("getHandle");
            Object minecraftEntity = getHandle.invoke(entity);

            Method setLocation = minecraftEntity.getClass().getMethod("setLocation",
                    Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            Method setHeadRotation = minecraftEntity.getClass().getMethod("setHeadRotation", Float.TYPE);

            Object worldServer = minecraftEntity.getClass().getField("world").get(minecraftEntity);
            Method entityJoinedWorld = worldServer.getClass().getMethod("entityJoinedWorld", MinecraftReflection.getEntityClass(), Boolean.TYPE);

            setLocation.invoke(minecraftEntity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            setHeadRotation.invoke(minecraftEntity, loc.getYaw());
            entityJoinedWorld.invoke(worldServer, minecraftEntity, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static Location getRelativeLocation(double angle, double radius, Location center) {
        Vector change = getRelativeVector(angle, radius);
        return center.clone().add(change);
    }

    public static Vector getRelativeVector(double angle, double radius) {
        if (angle < 0) {
            angle += 360;
        }
        double rad = Math.toRadians(angle);
        double x = Math.sin(rad) * radius;
        double z = Math.cos(rad) * radius;
        return new Vector(x, 0, z);
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
        for (UUID uuid : uuids) {
            CPlayer tp;
            if ((tp = Core.getPlayerManager().getPlayer(uuid)) == null) continue;
            switch (currencyType) {
                case BALANCE:
                    tp.addBalance(currencyAmount, "Ride " + name + " " + Core.getInstanceName());
                    break;
                case TOKENS:
                    tp.addTokens(currencyAmount, "Ride " + name + " " + Core.getInstanceName());
                    break;
            }
            tp.giveHonor(honorAmount);
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

    public static ArmorStand lock(ArmorStand stand) {
        try {
            String lockField;
            switch (Core.getMinecraftVersion()) {
                case "v1_13_R1":
                case "v1_13_R2":
                    lockField = "bH";
                    break;
                case "v1_12_R1":
                    lockField = "bB";
                    break;
                default:
                    lockField = "bA";
                    break;
            }
            Field f = Class.forName("net.minecraft.server." + Core.getMinecraftVersion() + ".EntityArmorStand")
                    .getDeclaredField(lockField);
            if (f != null) {
                f.setAccessible(true);
                Object craftStand = Class.forName("org.bukkit.craftbukkit." + Core.getMinecraftVersion() +
                        ".entity.CraftArmorStand").cast(stand);
                Object handle = craftStand.getClass().getDeclaredMethod("getHandle").invoke(craftStand);
                f.set(handle, 2096896);
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return stand;
    }


    public abstract boolean sitDown(CPlayer player, ArmorStand stand);

    public abstract boolean sitDown(CPlayer player, int entityId);

    public abstract void onChunkLoad(Chunk chunk);

    public abstract void onChunkUnload(Chunk chunk);

    public abstract boolean isRideStand(ArmorStand stand);

    public abstract boolean isRideStand(int id);
}
