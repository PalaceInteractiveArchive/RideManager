package network.palace.ridemanager.handlers.actions.sensors;

import network.palace.core.Core;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.RideShow;
import network.palace.show.ShowPlugin;
import org.bukkit.Location;

import java.io.File;
import java.util.Random;

public class ShowSensor extends RideSensor {
    private final File file;
    private final int delay;
    private static Random randomGenerator = new Random();
    private static int showNumber = 0;

    public ShowSensor(Location location, double radius, String file, int delay) {
        super(location, radius);
        this.file = new File("plugins/Show/shows/" + location.getWorld().getName() + "/" + file + ".show");
        this.delay = delay;
    }

    @Override
    public void activate() {
        super.activate();
        if (file == null || !file.exists()) return;
        Core.runTaskLater(RideManager.getInstance(), () -> {
            try {
                ShowPlugin.startShow(getRandomString(), new RideShow(file, vehicle));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20 * delay);
    }

    @Override
    public RideSensor duplicate() {
        return new ShowSensor(location, radius, file.getName().replace(".show", ""), delay);
    }

    private String getRandomString() {
        String s;
        int random = (int) (randomGenerator.nextDouble() * 999999) + showNumber++;
        if (vehicle == null) {
            s = random + "";
        } else {
            s = vehicle.getRide().getName().replaceAll(" ", "_") + "_" + random;
        }
        return s;
    }
}
