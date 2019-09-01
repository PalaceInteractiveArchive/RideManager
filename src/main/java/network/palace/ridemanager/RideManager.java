package network.palace.ridemanager;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.plugin.Plugin;
import network.palace.core.plugin.PluginInfo;
import network.palace.ridemanager.commands.Commandtest;
import network.palace.ridemanager.commands.RideBuilderCommand;
import network.palace.ridemanager.commands.RideMapResetCommand;
import network.palace.ridemanager.events.RideManagerStatusEvent;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.listeners.*;
import network.palace.ridemanager.utils.MappingUtil;
import network.palace.ridemanager.utils.MovementUtil;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Marc on 1/15/17.
 */
@PluginInfo(name = "RideManager", version = "1.2.0", depend = {"Core", "ProtocolLib"}, canReload = false)
public class RideManager extends Plugin {
    @Getter private static RideManager instance;
    @Getter private static MovementUtil movementUtil;
    @Getter private static MappingUtil mappingUtil;
    @Getter private static RideBuilderUtil rideBuilderUtil;
    @Getter private static String minecraftVersion = "";

    @Override
    protected void onPluginEnable() throws Exception {
        instance = this;

        minecraftVersion = Bukkit.getBukkitVersion();
        minecraftVersion = minecraftVersion.replace("-SNAPSHOT", "").replace("R0.", "R").replace(".", "_").replaceAll("_[0-9]-R", "_R").replace("-", "_");
        try {
            Class.forName("net.minecraft.server.v" + minecraftVersion + ".MinecraftServer");
        } catch (ClassNotFoundException e) {
            Core.logMessage(ChatColor.RED + "RideManager", ChatColor.RED + "RideManager disabled! This version doesn't support MC v" + minecraftVersion);
            onDisable();
            return;
        }

        mappingUtil = new MappingUtil();
        movementUtil = new MovementUtil();
        rideBuilderUtil = new RideBuilderUtil();

        registerCommand(new Commandtest());
        registerCommand(new RideBuilderCommand());
        registerCommand(new RideMapResetCommand());
        registerListener(new BlockListener());
        registerListener(new ChunkListener());
        registerListener(new InventoryClick());
        registerListener(new PacketListener());
        registerListener(new PlayerInteract());
        registerListener(new PlayerLeaveRide());
        registerListener(new PlayerMove());

        new RideManagerStatusEvent(RideManagerStatusEvent.Status.STARTING).call();
    }

    @Override
    protected void onPluginDisable() throws Exception {
        new RideManagerStatusEvent(RideManagerStatusEvent.Status.STOPPING).call();
        movementUtil.stop();
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    /**
     * Get the current ride a player is on
     *
     * @param player the player
     * @return the Ride the player is on, or null if they're not on a ride
     */
    public Ride getCurrentRide(CPlayer player) {
        for (Ride r : movementUtil.getRides()) {
            if (r.getOnRide().contains(player.getUniqueId())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Load a location from a section of a config file
     *
     * @param section the ConfigurationSection with location information
     * @return a Location with the x,y,z,yaw,pitch values from the config
     */
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
