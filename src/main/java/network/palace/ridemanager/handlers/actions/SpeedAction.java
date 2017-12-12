package network.palace.ridemanager.handlers.actions;

import lombok.Getter;

/**
 * Created by Marc on 5/2/17.
 */
public class SpeedAction extends RideAction {
    @Getter private final double speed;
    @Getter private double ticks;
    private double change = 0;
    private boolean finished = false;

    public SpeedAction(double speed, double ticks) {
        super(false);
        this.speed = speed;
        this.ticks = Math.abs(ticks);
    }

    @Override
    public void execute() {
        if (change == 0 && this.ticks != 0) {
            change = (speed - cart.getPower()) / ticks;
        }
        if (ticks > 0) {
            ticks -= 1;
            cart.setPower(cart.getPower() + change);
        } else {
            cart.setPower(speed);
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new SpeedAction(speed, ticks);
    }

    @Override
    public String toString() {
        return "";
    }
}
