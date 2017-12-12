package network.palace.ridemanager.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marc on 3/12/17.
 */
public class ModelMap {
    private HashMap<Integer, Seat> seats = new HashMap<>();

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
