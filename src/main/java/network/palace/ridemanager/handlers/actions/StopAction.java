package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class StopAction extends MoveAction {
    private long time;
    private Vector change;
    private double powerChange = 0;
    private boolean finished = false;

    public StopAction(long time) {
        this.time = time;
    }

    @Override
    public void execute() {
        if (powerChange == 0) {
            Location lastLocation = cart.getLastLocation();
            Location currentLocation = cart.getLocation();
            change = currentLocation.toVector().subtract(lastLocation.toVector()).normalize();
            powerChange = cart.getPower() / time;
        }
        cart.setPower(cart.getPower() - powerChange);
        Location original = cart.getLocation();

        double power = cart.getPower();
        Vector change = this.change.clone().multiply(new Vector(power, power, power));
        Location next = cart.getLocation().add(change);

        float yaw = cart.getYaw();
        next.setYaw(yaw);

        Vector v = next.toVector().subtract(original.toVector());
        if (v.getY() == 0) {
            v.setY(MovementUtil.getYMin());
        }
        cart.setVelocity(v);
        cart.teleport(next);
        cart.setYaw(yaw);

        if (cart.getPower() <= 0) {
            cart.setPower(0);
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new StopAction(time);
    }

    @Override
    public String toString() {
        return "Stop " + time;
    }
}
