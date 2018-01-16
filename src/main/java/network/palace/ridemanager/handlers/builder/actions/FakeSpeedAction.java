package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.SpeedAction;

@Getter
@Setter
public class FakeSpeedAction extends FakeAction {
    @Getter private final double speed;
    @Getter private double ticks;
    private double change = 0;
    private boolean finished = false;

    public FakeSpeedAction() {
        this(0, 0);
    }

    public FakeSpeedAction(double speed, double ticks) {
        super(false);
        this.speed = speed;
        this.ticks = Math.abs(ticks);
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
