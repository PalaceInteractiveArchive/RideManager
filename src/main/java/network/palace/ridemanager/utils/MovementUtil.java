package network.palace.ridemanager.utils;

import network.palace.core.Core;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Marc on 1/15/17.
 */
public class MovementUtil {
    private List<Ride> rides = new ArrayList<>();
    private int taskid;

    public MovementUtil() {
        loadRides();
        taskid = Bukkit.getScheduler().runTaskTimer(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Ride ride : new ArrayList<>(rides)) {
                    ride.move();
                }
            }
        }, 0L, 1L).getTaskId();
    }

    public void loadRides() {
        despawnAll();
        rides.clear();
        File dir = new File("plugins/RideManager");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File("plugins/RideManager/rides.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Core.logMessage("RideManager", "New configuration file generated!");
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config == null) {
            return;
        }
        ConfigurationSection sec = config.getConfigurationSection("rides");
        if (sec != null) {
            Set<String> names = sec.getKeys(false);
            for (String s : names) {
                ConfigurationSection current = config.getConfigurationSection("rides." + s);
                RideType type = RideType.fromString(current.getString("type"));
                String displayName = ChatColor.translateAlternateColorCodes('&', current.getString("name"));
                int riders = current.getInt("riders");
                double delay = current.getDouble("delay");
                Location exit = RideManager.parseLocation(current.getConfigurationSection("exit"));
                Ride ride = null;
                switch (type) {
                    case SIGN: {
                        Location spawnSign = RideManager.parseLocation(current.getConfigurationSection("sign"));
                        ride = new SignRide(s, displayName, riders, delay, exit, spawnSign);
                        break;
                    }
                    case COASTER:
                        break;
                    case TEACUPS: {
                        Location center = RideManager.parseLocation(current.getConfigurationSection("center"));
                        ride = new TeacupsRide(s, displayName, delay, exit, center);
                        break;
                    }
                    case CAROUSEL: {
                        Location center = RideManager.parseLocation(current.getConfigurationSection("center"));
                        ride = new CarouselRide(s, displayName, delay, exit, center);
                        break;
                    }
                    case AERIAL_CAROUSEL: {
                        Location center = RideManager.parseLocation(current.getConfigurationSection("center"));
                        ride = new AerialCarouselRide(s, displayName, delay, exit, center, current.getDouble("aerialRadius"), current.getDouble("supportRadius"));
                        break;
                    }
                }
                if (ride != null) {
                    rides.add(ride);
                } else {
                    Core.logMessage("RideManager", ChatColor.RED + "Error loading ride " + ChatColor.GREEN + s);
                }
            }
        }
        Core.logMessage("RideManager", ChatColor.GREEN + "Loaded " + ChatColor.BOLD + rides.size() +
                ChatColor.GREEN + " rides!");
    }

    public List<Ride> getRides() {
        return new ArrayList<>(rides);
    }

    public void despawnAll() {
        for (Ride ride : getRides()) {
            ride.despawn();
        }
    }

    public void stop() {
        despawnAll();
        Bukkit.getScheduler().cancelTask(taskid);
    }

    public Ride getRide(String name) {
        for (Ride r : getRides()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }
}
