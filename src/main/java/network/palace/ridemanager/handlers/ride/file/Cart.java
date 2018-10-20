package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.MathUtil;
import network.palace.ridemanager.events.RideEndEvent;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Marc on 5/2/17.
 */
public class Cart {
    @Getter private final RideVehicle vehicle;
    private final LinkedHashMap<Integer, RideAction> actions;
    @Getter private final ModelMap map;
    @Getter private final ItemStack model;
    @Getter @Setter private float yaw = 0;
    @Getter @Setter private float pitch = 0;
    @Getter private int currentActionIndex = 0;
    @Getter @Setter private EulerAngle headPose;
    @Getter @Setter private Optional<ArmorStand> base = Optional.empty();
    @Getter private boolean finished = false;
    @Getter private Vector velocity = new Vector();
    @Getter private boolean spawned = false;

    private Location lastLocation;

    // Position variables.
    @Getter private World world;
    @Getter private double x, y, z;
    private int chunkX;
    private int chunkZ;

    // The entities that represent seats.
    private final List<Seat> seats;

    @Getter private long creationTimestamp;

    public Cart(RideVehicle vehicle, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map) {
        this(vehicle, actions, model, map, 0.1);
    }

    public Cart(RideVehicle vehicle, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map, double speed) {
        this.vehicle = vehicle;
        this.actions = actions;
        this.map = map;
        this.seats = new ArrayList<>();
        creationTimestamp = System.currentTimeMillis();
        this.actions.values().forEach(a -> a.setCart(this));
        this.model = model;
    }

    public Chunk getChunk() {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public boolean isInChunk(Chunk chunk) {
        return chunk.getX() == chunkX && chunk.getZ() == chunkZ;
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch).add(0, MovementUtil.armorStandHeight, 0);
    }

    public Location getLastLocation() {
        return lastLocation.clone().add(0, MovementUtil.armorStandHeight, 0);
    }

    private void updateLocation(World world, double x, double y, double z, float yaw, float pitch) {
        lastLocation = new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        chunkX = MathUtil.floor(x) >> 4;
        chunkZ = MathUtil.floor(z) >> 4;
    }

    private void updateLocation(Location loc) {
        updateLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public List<Seat> getSeatsUnsafe() {
        return seats;
    }

    public Seat getSeat(int index) {
        return seats.get(index);
    }

    public void clearSeats() {
        for (Seat seat : seats) {
            seat.despawn(null);
        }
        seats.clear();
    }

    public int size() {
        return seats.size();
    }

    public void teleport(Location loc) {
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        if (getVehicle().isAutoYaw()) {
            setYaw(loc.getYaw());
        }
        teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    private void teleport(World world, double x, double y, double z) {
        updateLocation(world, x, y, z, yaw, pitch);
        Location loc = new Location(world, x, y, z, yaw, pitch);

        seats.forEach(s -> s.move(loc));

        base.ifPresent(base -> Ride.teleport(base, loc));
    }

    public void move() {
        if (actions.isEmpty()) {
            despawn();
            return;
        }
        RideAction a = actions.get(currentActionIndex);
        if (a == null || a.getCart() == null || !a.getCart().equals(this)) {
            finished = true;
            List<UUID> passengers = new ArrayList<>();
            getSeats().forEach(s -> s.getPassengers().forEach(p -> passengers.add(p.getUniqueId())));
            getRide().rewardCurrency(passengers.toArray(new UUID[]{}));
            new RideEndEvent(getRide(), passengers.toArray(new UUID[]{})).call();
            despawn();
            return;
        }
        a.execute();
        if (a.isFinished()) {
            currentActionIndex++;
        }
    }

    public void setSpeed(double p) {
        vehicle.setSpeed(p);
    }

    public void spawn(Location loc) {
        setYaw(loc.getYaw());
        setPitch(loc.getPitch());
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        updateLocation(loc);
        spawned = true;

        ModelMap map = getMap();
        for (Seat seat : map.getSeats()) {
            addSeat(seat.copy());
        }
        getSeats().forEach(s -> s.spawn(loc));

        if (!getChunk().isLoaded()) return;

        chunkLoaded(getChunk());
    }

    public void despawn() {
        despawn(getRide().getExit());
    }

    public void despawn(Location exit) {
        spawned = false;
        getSeats().forEach(s -> {
            s.getPassengers().forEach(p -> getRide().removeFromOnRide(p.getUniqueId()));
            s.despawn(exit);
        });
        chunkUnloaded(null);
    }

    public void chunkLoaded(Chunk c) {
        if (!spawned || base.isPresent() || !c.equals(getChunk())) return;

        Location loc = getLocation();

        ArmorStand stand = Ride.lock(loc.getWorld().spawn(loc.add(0, -MovementUtil.armorStandHeight, 0), ArmorStand.class));
        stand.setVisible(false);
        stand.teleport(loc);
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setHelmet(getModel());
        stand.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        base = Optional.of(stand);

        getSeats().forEach(Seat::chunkLoaded);
    }

    public void chunkUnloaded(Chunk c) {
        if (c != null && !c.equals(getChunk())) return;
        base.ifPresent(Entity::remove);
        base = Optional.empty();
        getSeats().forEach(Seat::chunkUnloaded);
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public void addPassenger(CPlayer tp) {
        if (seats.size() > 0) {
            getSeats().forEach(s -> {
                if (s.hasPassenger()) return;
                getRide().addToOnRide(tp.getUniqueId());
                s.addPassenger(tp);
            });
        }
    }

    public void removePassenger(CPlayer tp) {
        removePassenger(tp, true);
    }

    public void removePassenger(CPlayer tp, boolean teleport) {
        getRide().removeFromOnRide(tp.getUniqueId());
        getSeats().forEach(s -> s.removePassenger(tp));
        if (teleport) {
            tp.teleport(getRide().getExit());
        }
    }

    public List<CPlayer> getPassengers() {
        List<CPlayer> list = new ArrayList<>();
        getSeats().forEach(s -> {
            if (!s.hasPassenger()) return;
            list.addAll(s.getPassengers());
        });
        return list;
    }

    public void empty() {
        for (Seat s : getSeats()) {
            s.despawn(getRide().getExit());
        }
    }

    public RideAction getPreviousAction() {
        if (currentActionIndex == 0) return null;
        return getPreviousAction(actions.get(currentActionIndex).getId());
    }

    public RideAction getPreviousAction(UUID id) {
        int i = -1;
        for (RideAction a : new ArrayList<>(actions.values())) {
            if (a.getId().equals(id)) {
                return actions.get(i);
            }
            i++;
        }
        return null;
    }

    public RideAction getNextAction(UUID id) {
        int i = 0;
        for (Map.Entry<Integer, RideAction> a : new HashMap<>(actions).entrySet()) {
            if (a.getValue().getId().equals(id)) {
                i = a.getKey();
                break;
            }
        }
        if (i < 0) {
            return null;
        }
        return actions.get(i + 1);
    }

    public void setVelocity(Vector v) {
        this.velocity = v;
        base.ifPresent(base -> base.setVelocity(v));
        getSeats().forEach(s -> s.setVelocity(v));
    }

    public double getSpeed() {
        return vehicle.getSpeed();
    }

    public FileRide getRide() {
        return vehicle.getRide();
    }
}
