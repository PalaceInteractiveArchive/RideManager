package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/7/17.
 */
public class WaitAction extends MoveAction {
    private final long ticks;
    private long time = 0;

    public WaitAction(long ticks) {
        this.ticks = ticks;
    }

    @Override
    public void execute() {
        if (this.time == 0) {
            this.time = System.currentTimeMillis() + (ticks * 50);
        }
        cart.getStand().setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
    }

    @Override
    public boolean isFinished() {
        return System.currentTimeMillis() >= time;
    }

    @Override
    public RideAction duplicate() {
        return new WaitAction(ticks);
    }

    @Override
    public String toString() {
        return "Wait " + ticks;
    }
}
