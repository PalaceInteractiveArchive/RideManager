package network.palace.ridemanager.handlers.actions.sensors;

import network.palace.core.player.CPlayer;
import org.bukkit.Location;

public class TextSensor extends RideSensor {
    private final String text;

    public TextSensor(Location loc, double radius, String text) {
        super(loc, radius);
        this.text = text;
    }

    @Override
    public void activate() {
        super.activate();
        for (CPlayer player : cart.getPassengers()) {
            player.sendMessage(text);
        }
    }

    @Override
    public RideSensor duplicate() {
        return new TextSensor(location, radius, text);
    }
}
