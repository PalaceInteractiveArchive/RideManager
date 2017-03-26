package network.palace.ridemanager.handlers;

import lombok.Getter;

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

    public static class Seat {
        @Getter private final double x;
        @Getter private final double y;
        @Getter private final double z;

        public Seat(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
