package network.palace.ridemanager.utils;

import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.handlers.ride.file.Seat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Marc on 3/12/17.
 */
public class MappingUtil {
    private HashMap<String, ModelMap> maps = new HashMap<>();

    /**
     * Get all loaded ModelMaps
     *
     * @return a HashMap of the name of the map mapped to the corresponding ModelMap object
     */
    public HashMap<String, ModelMap> getMaps() {
        return new HashMap<>(maps);
    }

    /**
     * Get a ModelMap by its name
     *
     * @param name the name
     * @return the ModelMap, or null if none was found
     * @implNote If no ModelMap is found, the plugin will attempt to load it from the file system. If this fails, null will be returned.
     */
    public ModelMap getMap(String name) {
        ModelMap map;
        if (maps.containsKey(name)) {
            map = maps.get(name);
        } else {
            map = loadMap(name);
        }
        return map;
    }

    /**
     * Read a ModelMap file from the file system
     *
     * @param name the name of the map *excluding* the extension .map
     * @return null if file doesn't exist, and a ModelMap object if the file does exist
     */
    private ModelMap loadMap(String name) {
        File f = new File("plugins/RideManager/maps/" + name + ".map");
        if (!f.exists()) {
            Bukkit.getLogger().severe("Could not find map file at plugins/RideManager/maps/" + name + ".map!");
            return null;
        }
        YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
        ModelMap map = new ModelMap();
        if (file.contains("item")) {
            ItemStack item = ItemUtil.create(Material.matchMaterial(file.getString("item")), 1);
            if (item != null)
                map.setItem(item);
        }
        if (file.contains("length")) {
            double length = file.getDouble("length");
            map.setLength(length);
        } else {
            map.setLength(1);
        }
        if (file.contains("space")) {
            double space = file.getDouble("space");
            map.setSpaceBetweenCarts(space);
        } else {
            map.setSpaceBetweenCarts(1);
        }
        if (file.contains("carts")) {
            int carts = file.getInt("carts");
            map.setCartCount(carts);
        } else {
            map.setCartCount(1);
        }
        ConfigurationSection sec = file.getConfigurationSection("seats");
        World w = Bukkit.getWorlds().get(0);
        for (String s : sec.getKeys(false)) {
            ConfigurationSection seat = sec.getConfigurationSection(s);
            map.addSeat(Integer.parseInt(s), new Seat(seat.getDouble("x"), seat.getDouble("y"), seat.getDouble("z"), w));
        }
        maps.put(name, map);
        return map;
    }

    /**
     * Clear all loaded ModelMaps
     */
    public void reset() {
        maps.clear();
    }
}
