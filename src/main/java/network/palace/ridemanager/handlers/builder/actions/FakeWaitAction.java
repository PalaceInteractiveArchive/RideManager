package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;

@Getter
@Setter
public class FakeWaitAction extends FakeAction {
    private long ticks;

    public FakeWaitAction() {
        super(true);
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.WaitAction(ticks);
    }

    @Override
    public String toString() {
        return "";
    }
}
