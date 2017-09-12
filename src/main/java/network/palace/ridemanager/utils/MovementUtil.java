package network.palace.ridemanager.utils;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;

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
    public static final double armorStandHeight = 1.68888; //Meters
    @Getter private static long tick = 0;

    public MovementUtil() {
        loadRides();
        taskid = Bukkit.getScheduler().runTaskTimer(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Ride ride : new ArrayList<>(rides)) {
                    ride.move();
                }
                /*
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Vector v = p.getVelocity();
//                    p.sendMessage(v.toString());
                    p.setVelocity(new Vector(0.1, v.getY(), 0));
                }*/
                tick++;
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
                    case FILE:
                        ride = new FileRide(s, displayName, riders, delay, exit, current.getString("file"));
                        break;
                    case SIGN: {
                        Location spawnSign = RideManager.parseLocation(current.getConfigurationSection("sign"));
                        ride = new SignRide(s, displayName, riders, delay, exit, spawnSign, current.getString("model"));
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
                        ConfigurationSection support = current.getConfigurationSection("support");
                        ride = new AerialCarouselRide(s, displayName, delay, exit, center, current.getDouble("aerialRadius"),
                                support.getDouble("radius"), current.getBoolean("small"), support.getDouble("angle"),
                                support.getDouble("height"), support.getDouble("movein"));
                        break;
                    }
                    case ARMORSTAND:
                        String fileName = current.getString("file");
                        ride = new ArmorStandRide(s, displayName, riders, delay, exit, fileName);
                        break;
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

    public void addRide(Ride ride) {
        rides.add(ride);
    }

    public void removeRide(Ride ride) {
        rides.remove(ride);
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

    public Ride getRide(CPlayer cp) {
        for (Ride ride : getRides()) {
            if (ride.getOnRide().contains(cp.getUniqueId())) {
                return ride;
            }
        }
        return null;
    }

    public boolean sitDown(CPlayer player, ArmorStand stand) {
        for (Ride ride : getRides()) {
            if (ride.sitDown(player, stand)) {
            }
        }
        return false;
    }

    public static double getYMin() {
        return Double.MIN_VALUE;
    }

    public static double pythag(double a, double b) {
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public static double pythag2(double a, double c) {
        return Math.sqrt(Math.pow(c, 2) - Math.pow(a, 2));
    }

    public static double sin(double amplitude, double period, double x) {
        return amplitude * Math.sin(period * x);
    }
}
