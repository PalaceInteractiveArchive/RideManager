package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.threads.FileRideLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Marc
 * @since 8/7/17
 */
public class ArmorStandRide extends Ride {
    private Location spawn;
    private File file;
    private List<Vehicle> vehicles = new ArrayList<>();
    private final List<Action> actions;

    public ArmorStandRide(String name, String displayName, int riders, double delay, Location exit, String fileName, CurrencyType currencyType, int currencyAmount) {
        super(name, displayName, riders, delay, exit, currencyType, currencyAmount, 0);
        this.file = new File("plugins/RideManager/armorstand/" + fileName + ".ride");
        List<Action> actions = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.startsWith("#")) continue;
                String[] args = line.split("\\s+");
                if (spawn == null) {
                    if (args[0].equalsIgnoreCase("Spawn")) {
                        spawn = FileRideLoader.strToLoc(args[1]);
                    } else {
                        throw new Exception("Ride file doesn't start with Spawn location");
                    }
                    continue;
                }
                if (args.length == 0) {
                    continue;
                }
                if (args[0].contains("Move")) {
                    Location to = FileRideLoader.strToLoc(args[1]);
                    long dur = Long.parseLong(args[2]);
                    MoveAction move = new MoveAction(dur, to);
                    actions.add(move);
                    continue;
                }
                if (args[0].contains("Wait")) {
                    long dur = Long.parseLong(args[1]);
                    WaitAction wait = new WaitAction(dur);
                    actions.add(wait);
                }
            }
        } catch (Exception e) {
            parseError(e.getMessage());
        }
        this.actions = actions;
    }

    @Override
    public boolean handleEject(CPlayer player) {
        for (Vehicle v : new ArrayList<>(vehicles)) {
            if (!v.getPassengers().contains(player.getUniqueId())) continue;
            v.removePassenger(player);
        }
        return getOnRide().remove(player.getUniqueId());
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        return true;
    }

    @Override
    public void move() {
        if (actions.isEmpty()) return;
        List<Vehicle> vehicles = new ArrayList<>(this.vehicles);
        for (Vehicle v : vehicles) {
            v.move();
            if (!v.isFinished()) continue;
            v.despawn();
            this.vehicles.remove(v);
        }
    }

    @Override
    public void despawn() {
        List<Vehicle> vehicles = new ArrayList<>(this.vehicles);
        for (Vehicle v : vehicles) {
            v.despawn();
            this.vehicles.remove(v);
        }
    }

    @Override
    public void start(List<CPlayer> riders) {
        Vehicle v = new Vehicle(spawn);
        v.spawn();
        new RideStartEvent(this).call();
        for (CPlayer player : riders) {
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        riders.stream().forEach(p -> {
            v.addPassenger(p);
            p.sendMessage(ChatColor.GREEN + "Ride starting!");
            getOnRide().add(p.getUniqueId());
        });
        vehicles.add(v);
    }

    private void parseError(String s) {
        Core.logMessage("ArmorStand Parser", "Error parsing file " + file.getName() + ": " + s);
    }

    private class Vehicle {
        private Location spawnLocation;
        @Getter private boolean spawned = false;
        @Getter private UUID entityUUID;
        private List<Action> localActions;
        private List<UUID> passengers = new ArrayList<>();

        public Vehicle(Location spawnLocation) {
            this.spawnLocation = spawnLocation;
            this.localActions = new ArrayList<>();
            for (Action a : actions) {
                localActions.add(a.duplicate());
            }
        }

        public void move() {
            if (!spawned || isFinished()) return;
            Optional<ArmorStand> opt = getStand();
            if (!opt.isPresent()) {
                Bukkit.broadcastMessage("Not present " + entityUUID);
                return;
            }
            ArmorStand stand = opt.get();
            for (Action a : new ArrayList<>(localActions)) {
                if (a.getDuration() < 0) {
                    localActions.remove(a);
                    continue;
                }
                a.run();
                if (a instanceof MoveAction) {
                    MoveAction act = (MoveAction) a;
                    if (!act.isChangeCalculated()) {
                        act.calculateChange(stand.getLocation());
                    }
                    Vector xyz = act.getChange();
                    Location to = stand.getLocation();
                    to.add(xyz);
                    teleport(stand, to);
                }
                stand.setVelocity(new Vector(0, 0.000001, 0));
                break;
            }
        }

        public boolean isFinished() {
            if (localActions == null) Bukkit.broadcastMessage("NULL");
            if (localActions.isEmpty()) Bukkit.broadcastMessage("EMPTY");
            return localActions == null || localActions.isEmpty();
        }

        public List<UUID> getPassengers() {
            return new ArrayList<>(passengers);
        }

        public void spawn() {
            if (spawned) return;
            ArmorStand stand = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class);
            stand.setVisible(false);
            stand.setBasePlate(false);
            stand.setArms(false);
            stand.setSmall(false);
            stand.setGravity(true);
            this.entityUUID = stand.getUniqueId();
            this.spawned = true;
        }

        public void despawn() {
            Bukkit.broadcastMessage("DESPAWNING");
            if (!spawned) return;
            Optional<ArmorStand> opt = getStand();
            if (!opt.isPresent()) return;
            ArmorStand stand = opt.get();
            if (!passengers.isEmpty()) {
                for (UUID uuid : getPassengers()) {
                    CPlayer p = Core.getPlayerManager().getPlayer(uuid);
                    if (p == null || !passengers.contains(p.getUniqueId())) {
                        passengers.remove(uuid);
                        continue;
                    }
                    removePassenger(p);
                    p.teleport(getExit());
                }
            }
            stand.remove();
        }

        public void addPassenger(CPlayer p) {
            Optional<ArmorStand> opt = getStand();
            if (!opt.isPresent()) return;
            ArmorStand stand = opt.get();
            p.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200000, 0, true));
            stand.addPassenger(p.getBukkitPlayer());
            passengers.add(p.getUniqueId());
        }

        public void removePassenger(CPlayer p) {
            if (!passengers.contains(p.getUniqueId())) return;
            Optional<ArmorStand> opt = getStand();
            if (!opt.isPresent()) return;
            ArmorStand stand = opt.get();
            stand.removePassenger(p.getBukkitPlayer());
            p.teleport(getExit());
            p.getBukkitPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        public Optional<ArmorStand> getStand() {
            return spawnLocation.getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(a -> a.getUniqueId()
                    .equals(entityUUID)).findFirst();
        }

    }

    private class Action {

        @Getter protected long duration = 0;

        public Action(long duration) {
            this.duration = duration;
        }

        public void run() {
            duration -= 1;
        }

        public Action duplicate() {
            return new Action(duration);
        }
    }

    private class MoveAction extends Action {
        @Getter protected Location to;
        @Getter protected Vector change;

        public MoveAction(long duration, Location to) {
            super(duration);
            this.to = to;
        }

        public boolean isChangeCalculated() {
            return this.change != null;
        }

        public void calculateChange(Location from) {
            double x = to.getX() - from.getX();
            double y = to.getY() - from.getY();
            double z = to.getZ() - from.getZ();
            x = (x / duration);
            y = (y / duration);
            z = (z / duration);
            this.change = new Vector(x, y, z);
        }

        public MoveAction duplicate() {
            return new MoveAction(duration, to);
        }
    }

    private class WaitAction extends Action {

        public WaitAction(long duration) {
            super(duration);
        }

        public WaitAction duplicate() {
            return new WaitAction(duration);
        }
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
    }
}
