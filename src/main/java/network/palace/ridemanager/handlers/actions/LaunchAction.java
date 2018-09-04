package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LaunchAction extends MoveAction {
    private final long time;
    private final double speed;
    private final Location target;
    private Vector change;
    private double powerChange = 0;
    private boolean finished = false;

    public LaunchAction(long time, double speed, Location target) {
        this.time = time;
        this.speed = speed;
        this.target = target;
    }

    @Override
    public void execute() {
        if (powerChange == 0) {
            Location current = cart.getLocation();

            change = target.clone().subtract(current).toVector().normalize();

            powerChange = (speed - cart.getSpeed()) / time;
        }
        if (!finished) {
            double currentPower = cart.getSpeed();
            boolean up = speed > currentPower;
            if (up) {
                if (currentPower + powerChange > speed) {
                    cart.setSpeed(speed);
                } else {
                    cart.setSpeed(cart.getSpeed() + powerChange);
                }
            } else {
                if (currentPower + powerChange < speed) {
                    cart.setSpeed(speed);
                } else {
                    cart.setSpeed(cart.getSpeed() + powerChange);
                }
            }
        }
        Location original = cart.getLocation();

        double power = cart.getSpeed();
        Vector change = this.change.clone().multiply(new Vector(power, power, power));
        Location next = original.clone().add(change);

        float yaw = cart.getYaw();
        next.setYaw(yaw);

        Vector v = next.toVector().subtract(original.toVector());
        if (v.getY() == 0) {
            v.setY(MovementUtil.getYMin());
        }
        cart.setVelocity(v);
        cart.teleport(next);
        cart.setYaw(yaw);

        if (cart.getSpeed() == speed) {
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new LaunchAction(time, speed, target);
    }

    @Override
    public String toString() {
        return "Launch " + time + " " + speed + " " + target.getX() + "," + target.getY() + "," + target.getZ();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.LAUNCH;
    }
}
