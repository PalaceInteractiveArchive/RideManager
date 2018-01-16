package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;

@Getter
@Setter
public class FakeRotateAction extends FakeAction {
    private int angle;
    private boolean rightTurn;
    private long ticks;

    public FakeRotateAction() {
        super(true);
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.RotateAction(angle, rightTurn, ticks);
    }

    @Override
    public String toString() {
        return "";
    }
}
