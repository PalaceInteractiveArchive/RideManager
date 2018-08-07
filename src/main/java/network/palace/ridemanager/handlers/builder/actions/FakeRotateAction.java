package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.ActionType;

@Getter
@Setter
public class FakeRotateAction extends FakeAction {
    private int angle;
    private boolean rightTurn;
    private long ticks = -1;

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

    @Override
    public ActionType getActionType() {
        return ActionType.ROTATE;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return angle == Integer.MAX_VALUE || ticks == -1;
    }
}
