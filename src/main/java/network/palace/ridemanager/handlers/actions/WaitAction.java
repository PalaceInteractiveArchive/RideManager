package network.palace.ridemanager.handlers.actions;

/**
 * Created by Marc on 5/7/17.
 */
public class WaitAction extends RideAction {
    private long time;

    public WaitAction(long delay) {
        super(true);
        this.time = System.currentTimeMillis() + (delay * 50);
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return System.currentTimeMillis() >= time;
    }
}
