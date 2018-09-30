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

    public HashMap<String, ModelMap> getMaps() {
        return new HashMap<>(maps);
    }

    public ModelMap getMap(String name) {
        ModelMap map;
        if (maps.containsKey(name)) {
            map = maps.get(name);
        } else {
            map = loadMap(name);
        }
        return map;
    }

    private ModelMap loadMap(String name) {
        File f = new File("plugins/RideManager/maps/" + name + ".map");
        if (!f.exists()) {
            Bukkit.getLogger().severe("Could not find map file at plugins/RideManager/maps/" + name + ".map!");
            return null;
        }
        YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
        ModelMap map = new ModelMap();
        if (file.contains("item")) {
            String[] list = file.getString("item").split(":");
            int id = Integer.parseInt(list[0]);
            byte data = 0;
            if (list.length > 1) {
                data = Byte.parseByte(list[1]);
            }
            ItemStack item = ItemUtil.create(Material.getMaterial(id), 1, data);
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

    public void reset() {
        maps.clear();
    }
}
