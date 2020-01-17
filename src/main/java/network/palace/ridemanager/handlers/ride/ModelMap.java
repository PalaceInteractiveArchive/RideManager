package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.ride.file.Seat;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marc on 3/12/17.
 */
public class ModelMap {
    private HashMap<Integer, Seat> seats = new HashMap<>();
    @Getter @Setter private double length;
    @Getter @Setter private double spaceBetweenCarts;
    @Getter @Setter private int cartCount;
    @Getter @Setter private ItemStack item = null;

    public HashMap<Integer, Seat> getMap() {
        return new HashMap<>(seats);
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats.values());
    }

    public Seat getSeat(int pos) {
        return seats.get(pos);
    }

    public void addSeat(int pos, Seat seat) {
        seats.put(pos, seat);
    }

    public ModelMap copy(World world) {
        ModelMap newMap = new ModelMap();

        newMap.setLength(length);
        newMap.setSpaceBetweenCarts(spaceBetweenCarts);
        newMap.setCartCount(cartCount);
        newMap.setItem(item);

        for (Map.Entry<Integer, Seat> entry : seats.entrySet()) {
            Seat s = entry.getValue().copy();
            s.setWorld(world);
            newMap.addSeat(entry.getKey(), s);
        }

        return newMap;
    }
}
