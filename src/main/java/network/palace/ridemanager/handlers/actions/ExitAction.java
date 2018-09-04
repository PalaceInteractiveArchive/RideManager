package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 8/10/17
 */
public class ExitAction extends MoveAction {
    @Getter private final Location to;
    @Getter private String autoYaw;
    private boolean finished = false;

    public ExitAction(Location to, String autoYaw) {
        this.to = to;
        this.autoYaw = autoYaw;
        this.finalLocation = to;
    }

    @Override
    public void execute() {
        Location original = cart.getLocation();
        float yaw = (float) Math.toDegrees(Math.atan2(original.getZ() - to.getZ(), original.getX() - to.getX())) + 90;
        double distance = original.distance(to);
        Vector resultant = to.clone().subtract(original).toVector().normalize();
        double power = cart.getSpeed();
        Vector change = resultant.multiply(new Vector(power, power, power));
        Location next = cart.getLocation().add(change);
        if (!autoYaw.isEmpty()) {
            if (autoYaw.equalsIgnoreCase("true")) {
                next.setYaw(yaw);
            } else if (autoYaw.equalsIgnoreCase("false")) {
                yaw = original.getYaw();
                next.setYaw(original.getYaw());
            }
        } else {
            if (getCart().getRide().isAutoYaw()) {
                next.setYaw(yaw);
            } else {
                yaw = original.getYaw();
                next.setYaw(original.getYaw());
            }
        }
        if (next.distance(original) >= distance) {
            if (getCart().getRide().isAutoYaw()) {
                to.setYaw(yaw);
            } else {
                to.setYaw(original.getYaw());
            }
            cart.teleport(to);
            finished = true;
            cart.empty();
        } else {
            Vector v = next.toVector().subtract(original.toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.setVelocity(v);
            cart.teleport(next);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new ExitAction(to.clone(), autoYaw);
    }

    @Override
    public String toString() {
        return "Exit " + to.getX() + "," + to.getY() + "," + to.getZ() + (autoYaw.isEmpty() ? "" : " " + autoYaw);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.EXIT;
    }
}
