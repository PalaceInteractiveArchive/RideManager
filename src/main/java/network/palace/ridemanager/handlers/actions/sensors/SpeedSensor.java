package network.palace.ridemanager.handlers.actions.sensors;

import network.palace.core.Core;
import org.bukkit.Location;

public class SpeedSensor extends RideSensor {
    private final double speed;
    private final long time;

    public SpeedSensor(Location loc, double radius, double speed, long time) {
        super(loc, radius);
        this.speed = speed;
        this.time = time;
    }

    @Override
    public void activate() {
        super.activate();
        if (time == 0) {
            vehicle.setSpeed(speed);
            return;
        }
        double current = vehicle.getSpeed();
        double change = (speed - current) / time;

        int taskID = Core.runTaskTimer(() -> vehicle.setSpeed(vehicle.getSpeed() + change), 0L, 1L);

        Core.runTaskLater(() -> Core.cancelTask(taskID), time);
    }

    @Override
    public RideSensor duplicate() {
        return new SpeedSensor(location, radius, speed, time);
    }
}
