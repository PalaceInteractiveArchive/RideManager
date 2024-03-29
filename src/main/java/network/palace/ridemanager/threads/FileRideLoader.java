package network.palace.ridemanager.threads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import network.palace.ridemanager.handlers.actions.*;
import network.palace.ridemanager.handlers.actions.sensors.*;
import network.palace.ridemanager.handlers.builder.SensorType;
import network.palace.ridemanager.handlers.ride.file.FileRide;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.*;
import java.util.LinkedList;

/**
 * Created by Marc on 5/2/17.
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class FileRideLoader implements Runnable {
    @Getter private final World world;
    @Getter private final FileRide ride;
    @Getter private final File file;
    @Getter private final RideCallback callback;
    @Getter private boolean fakeRide = false;

    @Override
    public void run() {
        StringBuilder name = new StringBuilder();
        LinkedList<RideAction> actions = new LinkedList<>();
        LinkedList<RideSensor> sensors = new LinkedList<>();
        Location spawn = null;
        double speed = 0.1;
        boolean setYaw = true;
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // Parse Lines
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() == 0 || strLine.startsWith("#"))
                    continue;
                String[] tokens = strLine.split(" ");
                double length = tokens.length;
                if (length < 2) {
                    System.out.println("Invalid Ride Line [" + strLine + "]");
                    continue;
                }
                switch (tokens[0]) {
                    case "Name": {
                        for (int i = 1; i < tokens.length; i++) {
                            name.append(tokens[i]).append(" ");
                        }
                        break;
                    }
                    case "Sensor": {
                        Location loc = strToLoc(tokens[1]);
                        double radius = getDouble(tokens[2]);
                        SensorType type = SensorType.fromString(tokens[3].toLowerCase());
                        RideSensor sensor;
                        switch (type) {
                            case BLOCK:
                                Location blockLoc = strToLoc(tokens[4]);
                                Material btype = Material.valueOf(tokens[5]);
                                sensor = new BlockSensor(loc, radius, blockLoc, btype);
                                break;
                            case SHOW:
                                int delay = tokens.length > 5 ? getInt(tokens[5]) : 0;
                                sensor = new ShowSensor(loc, radius, tokens[4], delay);
                                break;
                            case SPEED:
                                sensor = new SpeedSensor(loc, radius, getDouble(tokens[4]), getLong(tokens[5]));
                                break;
                            case TEXT:
                                StringBuilder message = new StringBuilder();
                                for (int i = 4; i < tokens.length; i++) {
                                    message.append(tokens[i]);
                                }
                                sensor = new TextSensor(loc, radius, ChatColor.translateAlternateColorCodes('&', message.toString()));
                                break;
                            default:
                                continue;
                        }
                        if (sensor != null) sensors.add(sensor);
                        break;
                    }
                    case "Spawn": {
                        spawn = strToLoc(tokens[1]);
                        if (spawn == null) {
                            throw new Exception("Invalid Spawn Location");
                        }
                        speed = getDouble(tokens[2]);
                        float yaw = getFloat(tokens[3]);
                        spawn.setYaw(yaw);
                        if (tokens.length > 4) setYaw = getBoolean(tokens[4]);
                        if (fakeRide) {
                            SpawnAction a = new SpawnAction(spawn, speed, yaw);
                            actions.add(a);
                        }
                        break;
                    }
                    case "Straight": {
                        Location to = strToLoc(tokens[1]);
                        String autoYaw;
                        if (tokens.length > 2) {
                            autoYaw = tokens[2];
                        } else {
                            autoYaw = "";
                        }
                        StraightAction a = new StraightAction(to, autoYaw);
                        actions.add(a);
                        break;
                    }
                    case "Stop": {
                        long time = getLong(tokens[1]);
                        StopAction a = new StopAction(time);
                        actions.add(a);
                        break;
                    }
                    case "Launch": {
                        long time = getLong(tokens[1]);
                        double targetSpeed = getDouble(tokens[2]);
                        Location target = strToLoc(tokens[3]);
                        LaunchAction a = new LaunchAction(time, targetSpeed, target);
                        actions.add(a);
                        break;
                    }
                    case "Turn": {
                        Location to = strToLoc(tokens[1]);
                        Location p0 = strToLoc(tokens[2]);
                        TurnAction a = new TurnAction(to, p0);
                        actions.add(a);
                        break;
                    }
                    case "Rotate": {
                        long angle = getLong(tokens[1]);
                        boolean right = Boolean.parseBoolean(tokens[2]);
                        long ticks = getLong(tokens[3]);
                        RotateAction a = new RotateAction(angle, right, ticks);
                        actions.add(a);
                        break;
                    }
                    case "Wait": {
                        long delay = getLong(tokens[1]);
                        WaitAction a = new WaitAction(delay);
                        actions.add(a);
                        break;
                    }
                    case "Incline": {
                        Location to = strToLoc(tokens[1]);
                        int angle = getInt(tokens[2]);
                        InclineAction a = new InclineAction(to, angle);
                        actions.add(a);
                        break;
                    }
                    case "Decline": {
                        break;
                    }
                    case "Teleport": {
                        Location to = strToLoc(tokens[1]);
                        TeleportAction a = new TeleportAction(to);
                        actions.add(a);
                        break;
                    }
                    case "Exit": {
                        Location to = strToLoc(tokens[1]);
                        String autoYaw;
                        if (tokens.length > 2) {
                            autoYaw = tokens[2];
                        } else {
                            autoYaw = "";
                        }
                        ExitAction a = new ExitAction(to, autoYaw);
                        actions.add(a);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.done(name.toString().trim(), actions, sensors, spawn, speed, setYaw);
    }

    public static int getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    public static double getDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    public static float getFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    public static long getLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    public static boolean getBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    /**
     * Convert a string to a Location object
     *
     * @param string the string
     * @return the Location
     * @implNote string should follow the format x,y,z
     */
    private Location strToLoc(String string) {
        Location l = null;
        if (string.length() == 0) {
            return null;
        }
        String[] tokens = string.split(",");
        try {
            l = new Location(world, Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
        } catch (Exception ignored) {
            System.out.println("Error parsing location [" + string + "]");
        }
        return l;
    }
}
