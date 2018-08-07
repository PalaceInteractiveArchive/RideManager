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
public class RotateAction extends MoveAction {
    @Getter private final float angle;
    @Getter private final boolean rightTurn;
    @Getter private final long ticks;
    private float change = 0;
    private boolean finished = false;

    public RotateAction(float angle, boolean rightTurn, long ticks) {
        this.angle = angle;
        this.rightTurn = rightTurn;
        this.ticks = ticks;
    }

    @Override
    public void execute() {
        cart.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        float current = cart.getLocation().getYaw() % 360;
        if (rightTurn && current >= angle) {
            finished = true;
        } else if (!rightTurn && current <= angle) {
            finished = true;
        }
        if (ticks == 0) {
            finished = true;
        }
        if (change == 0) {
            if (current == angle) {
                finished = true;
                return;
            }
            if (rightTurn) {
                if (current > angle) {
                    change = (angle + 360) - current;
                } else {
                    change = angle - current;
                }
            } else {
                if (angle > current) {
                    change = angle - (current + 360);
                } else {
                    change = angle - current;
                }
            }
            change /= ticks;
        }
        if (finished) {
            Location to = cart.getLocation();
            to.setYaw(angle);
            cart.setYaw(angle);
            cart.teleport(to);
            return;
        }
        Location to = cart.getLocation();
        to.setYaw(to.getYaw() + change);
        cart.setYaw(to.getYaw());
        cart.teleport(to);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new RotateAction(angle, rightTurn, ticks);
    }

    @Override
    public String toString() {
        return "Rotate " + angle + " " + Boolean.toString(rightTurn) + " " + ticks;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ROTATE;
    }
}
