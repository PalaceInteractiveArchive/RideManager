package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import network.palace.ridemanager.handlers.Cart;

import java.util.UUID;

/**
 * Created by Marc on 5/2/17.
 */
@RequiredArgsConstructor
public abstract class RideAction {
    @Getter UUID id = UUID.randomUUID();
    @Getter @Setter public Cart cart = null;
    @Getter public final boolean movementAction;
    @Getter protected boolean executed = false;

    public abstract void execute();

    public abstract boolean isFinished();
}
