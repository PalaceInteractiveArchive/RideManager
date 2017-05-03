package network.palace.ridemanager.threads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.ridemanager.handlers.FileRide;
import network.palace.ridemanager.handlers.actions.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.LinkedList;

/**
 * Created by Marc on 5/2/17.
 */
@AllArgsConstructor
public class FileRideLoader implements Runnable {
    @Getter private final FileRide ride;
    @Getter private final File file;
    @Getter private RideCallback callback;

    @Override
    public void run() {
        LinkedList<RideAction> list = new LinkedList<>();
        Location spawn = null;
        double spawnAngle = 0;
        try {
            String strLine = "";
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // Parse Lines
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() == 0 || strLine.startsWith("#"))
                    continue;
                String[] tokens = strLine.split("\\s+");
                double length = tokens.length;
                if (length < 2) {
                    System.out.println("Invalid Ride Line [" + strLine + "]");
                    continue;
                }
                switch (tokens[0]) {
                    case "Spawn": {
                        spawn = strToLoc(tokens[1]);
                        if (spawn == null) {
                            System.out.println("Invalid Spawn Location");
                            continue;
                        }
                        if (length > 2) {
                            spawnAngle = getDouble(tokens[2]);
                        }
                        break;
                    }
                    case "Speed": {
                        double speed = getDouble(tokens[1]);
                        double ticks = 0;
                        if (length > 2) {
                            ticks = getDouble(tokens[2]);
                        }
                        list.add(new SpeedAction(speed, ticks));
                        break;
                    }
                    case "Teleport": {
                        Location to = strToLoc(tokens[1]);
                        list.add(new TeleportAction(to));
                        break;
                    }
                    case "Move": {
                        Location to = strToLoc(tokens[1]);
                        list.add(new MoveAction(to));
                        break;
                    }
                    case "Turn": {
                        if (length < 4) {
                            System.out.println("Invalid parameters for Turn [" + strLine + "]");
                            continue;
                        }
                        Location to = strToLoc(tokens[1]);
                        Location origin = strToLoc(tokens[2]);
                        boolean positive = Boolean.valueOf(tokens[3]);
                        list.add(new TurnAction(to, origin, positive));
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        callback.done(list, spawn);
    }

    private int getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    private double getDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Not a number [" + s + "]");
            return 0;
        }
    }

    private Location strToLoc(String string) {
        Location l = null;
        if (string.length() == 0) {
            return null;
        }
        String[] tokens = string.split(",");
        try {
            l = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]),
                    Double.parseDouble(tokens[2]));
        } catch (Exception ignored) {
            System.out.println("Error parsing location [" + string + "]");
        }
        return l;
    }
}
