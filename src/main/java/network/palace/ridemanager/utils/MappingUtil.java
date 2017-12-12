package network.palace.ridemanager.utils;

import network.palace.ridemanager.handlers.ModelMap;
import network.palace.ridemanager.handlers.Seat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

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
        ConfigurationSection sec = file.getConfigurationSection("seats");
        ModelMap map = new ModelMap();
        for (String s : sec.getKeys(false)) {
            ConfigurationSection seat = sec.getConfigurationSection(s);
            map.addSeat(Integer.parseInt(s), new Seat(seat.getDouble("x"), seat.getDouble("y"), seat.getDouble("z")));
        }
        maps.put(name, map);
        return map;
    }

    public void reset() {
        maps.clear();
    }
}
