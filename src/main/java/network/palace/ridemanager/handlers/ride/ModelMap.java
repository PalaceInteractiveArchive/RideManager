package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.ride.file.Seat;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
