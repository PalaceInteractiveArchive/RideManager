package network.palace.ridemanager.handlers.actions.sensors;

import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.RideShow;
import network.palace.show.ShowPlugin;
import org.bukkit.Location;

import java.io.File;

public class ShowSensor extends RideSensor {
    private final File file;

    public ShowSensor(Location location, double radius, String file) {
        super(location, radius);
        this.file = new File("plugins/Show/shows/" + file + ".show");
    }

    @Override
    public void activate() {
        super.activate();
        if (file == null) return;
        ShowPlugin.startShow("name", new RideShow(RideManager.getInstance(), file, cart));
    }

    @Override
    public RideSensor duplicate() {
        return new ShowSensor(location, radius, file.getName().replace(".show", ""));
    }
}
