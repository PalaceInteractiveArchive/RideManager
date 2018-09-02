package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.ActionType;

@Getter
@Setter
public class FakeWaitAction extends FakeAction {
    private long ticks = -1;

    public FakeWaitAction() {
        super(true);
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.WaitAction(ticks);
    }

    @Override
    public String toString() {
        return "Wait " + ticks;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.WAIT;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return ticks == -1;
    }
}
