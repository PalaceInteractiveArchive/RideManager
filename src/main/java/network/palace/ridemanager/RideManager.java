package network.palace.ridemanager;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.plugin.Plugin;
import network.palace.core.plugin.PluginInfo;
import network.palace.ridemanager.commands.CommandRideBuilder;
import network.palace.ridemanager.commands.Commandtest;
import network.palace.ridemanager.handlers.Ride;
import network.palace.ridemanager.listeners.*;
import network.palace.ridemanager.utils.MappingUtil;
import network.palace.ridemanager.utils.MovementUtil;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Marc on 1/15/17.
 */
@PluginInfo(name = "RideManager", version = "1.0", depend = {"Core", "ProtocolLib"}, canReload = true)
public class RideManager extends Plugin {
    @Getter private static RideManager instance;
    @Getter private static MovementUtil movementUtil;
    @Getter private static MappingUtil mappingUtil;
    @Getter private static RideBuilderUtil rideBuilderUtil;

    @Override
    protected void onPluginEnable() throws Exception {
        instance = this;
        String mcv = Bukkit.getBukkitVersion();
        mcv = mcv.replace("-SNAPSHOT", "").replace("R0.", "R").replace(".", "_").replaceAll("_[0-9]-R", "_R").replace("-", "_");
        try {
            Class.forName("net.minecraft.server.v" + mcv + ".MinecraftServer");
        } catch (ClassNotFoundException e) {
            Core.logMessage(ChatColor.RED + "RideManager", ChatColor.RED + "RideManager disabled! This version doesn't support MC v" + mcv);
            onDisable();
            return;
        }
        mappingUtil = new MappingUtil();
        movementUtil = new MovementUtil();
        rideBuilderUtil = new RideBuilderUtil();
        registerCommand(new Commandtest());
        registerCommand(new CommandRideBuilder());
        registerListener(new BlockListener());
        registerListener(new ChunkListener());
        registerListener(new PacketListener());
        registerListener(new PlayerInteract());
        registerListener(new PlayerLeaveRide());
    }

    @Override
    protected void onPluginDisable() throws Exception {
        movementUtil.stop();
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public Ride getCurrentRide(CPlayer player) {
        for (Ride r : movementUtil.getRides()) {
            if (r.getOnRide().contains(player.getUniqueId())) {
                return r;
            }
        }
        return null;
    }

    public static BlockFace getBlockFace(String s) {
        switch (s.toLowerCase()) {
            case "n":
                return BlockFace.NORTH;
            case "nnw":
                return BlockFace.NORTH_NORTH_WEST;
            case "nw":
                return BlockFace.NORTH_WEST;
            case "wnw":
                return BlockFace.WEST_NORTH_WEST;
            case "w":
                return BlockFace.WEST;
            case "wsw":
                return BlockFace.WEST_SOUTH_WEST;
            case "sw":
                return BlockFace.SOUTH_WEST;
            case "ssw":
                return BlockFace.SOUTH_SOUTH_WEST;
            case "s":
                return BlockFace.SOUTH;
            case "sse":
                return BlockFace.SOUTH_SOUTH_EAST;
            case "se":
                return BlockFace.SOUTH_EAST;
            case "ese":
                return BlockFace.EAST_SOUTH_EAST;
            case "e":
                return BlockFace.EAST;
            case "ene":
                return BlockFace.EAST_NORTH_EAST;
            case "ne":
                return BlockFace.NORTH_EAST;
            case "nne":
                return BlockFace.NORTH_NORTH_EAST;
        }
        return BlockFace.NORTH;
    }

    public static float getYaw(BlockFace dir) {
        switch (dir) {
            case NORTH:
                return 180;
            case EAST:
                return -90;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case NORTH_EAST:
                break;
            case NORTH_WEST:
                break;
            case SOUTH_EAST:
                break;
            case SOUTH_WEST:
                break;
            case WEST_NORTH_WEST:
                break;
            case NORTH_NORTH_WEST:
                break;
            case NORTH_NORTH_EAST:
                break;
            case EAST_NORTH_EAST:
                break;
            case EAST_SOUTH_EAST:
                break;
            case SOUTH_SOUTH_EAST:
                break;
            case SOUTH_SOUTH_WEST:
                break;
            case WEST_SOUTH_WEST:
                break;
        }
        return 0;
    }

    public static Location parseLocation(ConfigurationSection section) {
        World world = Bukkit.getWorlds().get(0);
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
