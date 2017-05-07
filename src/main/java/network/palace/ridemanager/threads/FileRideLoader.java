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
        int spawnAngle = 0;
        double speed = 0.1;
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
                            speed = getDouble(tokens[2]);
                        }
                        if (length > 3) {
                            spawnAngle = getInt(tokens[3]);
                        }
                        break;
                    }
                    case "Speed": {
                        double newSpeed = getDouble(tokens[1]);
                        double ticks = 0;
                        if (length > 2) {
                            ticks = getDouble(tokens[2]);
                        }
                        list.add(new SpeedAction(newSpeed, ticks));
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
                    case "Wait": {
                        long delay = Long.valueOf(tokens[1]);
                        list.add(new WaitAction(delay));
                        break;
                    }
                    case "Block": {
                        Location loc = strToLoc(tokens[1]);
                        String[] l;
                        if (tokens[2].contains(":")) {
                            l = tokens[2].split(":");
                        } else {
                            l = null;
                        }
                        try {
                            int id;
                            byte data;
                            if (l != null) {
                                id = Integer.parseInt(l[0]);
                                data = Byte.parseByte(l[1]);
                            } else {
                                id = Integer.parseInt(tokens[2]);
                                data = (byte) 0;
                            }
                            list.add(new BlockAction(loc, id, data));
                        } catch (Exception e) {
                            System.out.println("Invalid Block ID or Block data [" + strLine + "]");
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        callback.done(list, spawn, spawnAngle, speed);
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
