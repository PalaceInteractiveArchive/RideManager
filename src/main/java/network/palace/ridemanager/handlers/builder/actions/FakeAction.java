package network.palace.ridemanager.handlers.builder.actions;

import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.ride.file.Cart;

public abstract class FakeAction extends RideAction {

    public FakeAction(boolean movementAction) {
        super(movementAction);
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    public abstract boolean areFieldsIncomplete();

    @Override
    public RideAction load(Cart cart) {
        this.cart = cart;
        return this;
    }
}
