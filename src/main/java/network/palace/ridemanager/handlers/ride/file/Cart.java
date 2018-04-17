package network.palace.ridemanager.handlers.ride.file;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Marc on 5/2/17.
 */
public class Cart {
    @Getter private final FileRide ride;
    private final LinkedHashMap<Integer, RideAction> actions;
    @Getter private final ModelMap map;
    @Getter private final ItemStack model;
    @Getter private double power = 0;
    @Getter @Setter private float yaw = 0;
    @Getter private int currentActionIndex = 0;
    @Getter @Setter private long spawnTime;
    @Getter @Setter private EulerAngle headPose;
    @Getter @Setter private Optional<ArmorStand> base = Optional.empty();
    @Getter private boolean spawned = false;
    @Getter private boolean finished = false;
    @Getter private Vector velocity = new Vector();

    // Position variables.
    @Getter private World world;
    @Getter private double x, y, z;
    private int chunkX, chunkZ;

    // The entities that represent seats.
    private final List<Seat> seats;

    @Getter private long creationTimestamp;
    private boolean deleted;

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map) {
        this(ride, actions, model, map, 0.1);
    }

    public Cart(FileRide ride, LinkedHashMap<Integer, RideAction> actions, ItemStack model, ModelMap map, double power) {
        this.ride = ride;
        this.actions = actions;
        this.map = map;
        this.seats = new ArrayList<>();
        creationTimestamp = System.currentTimeMillis();
        this.actions.values().forEach(a -> a.setCart(this));
        setPower(power);
        this.model = model;
    }

    public Chunk getChunk() {
        return world.getChunkAt(chunkX, chunkZ);
    }

    public boolean isInChunk(Chunk chunk) {
        return chunk.getX() == chunkX && chunk.getZ() == chunkZ;
    }

    public Location getLocation() {
        return new Location(world, x, y, z).add(0, MovementUtil.armorStandHeight, 0);
    }

    private void updateLocation(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        chunkX = MathUtil.floor(x) >> 4;
        chunkZ = MathUtil.floor(z) >> 4;
    }

    private void updateLocation(Location loc) {
        updateLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
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
        if (getRide().isAutoYaw()) setYaw(loc.getYaw());
        teleport(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    private void teleport(World world, double x, double y, double z) {
        updateLocation(world, x, y, z);
        Location loc = new Location(world, x, y, z);
        loc.setYaw(getYaw());

        seats.forEach(s -> s.move(loc));

        base.ifPresent(base -> ride.teleport(base, loc));
    }

    public void move() {
        if (actions.isEmpty()) {
            despawn();
            return;
        }
        RideAction a = actions.get(currentActionIndex);
        if (a == null || a.getCart() == null || !a.getCart().equals(this)) {
            despawn();
            finished = true;
            return;
        }
        a.execute();
        if (a.isFinished()) {
            currentActionIndex++;
        }
    }

    public void setPower(double p) {
        this.power = p > 1 ? 1 : (p < -1 ? -1 : p);
    }

    public void spawn(Location loc) {
        setYaw(loc.getYaw());
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
        despawn(null);
    }

    public void despawn(Location exit) {
        spawned = false;
        getSeats().forEach(s -> s.despawn(exit));
        chunkUnloaded(null);
    }

    public void chunkLoaded(Chunk c) {
        if (!spawned || base.isPresent() || !c.equals(getChunk()) || !c.isLoaded()) return;

        Location loc = getLocation();

        ArmorStand stand = ride.lock(loc.getWorld().spawn(loc.add(0, -MovementUtil.armorStandHeight, 0), ArmorStand.class));
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setHelmet(getModel());
        stand.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        base = Optional.of(stand);
        Bukkit.broadcastMessage("Spawned!");

        getSeats().forEach(Seat::chunkLoaded);
    }

    public void chunkUnloaded(Chunk c) {
        if (c != null && (!c.equals(getChunk()) || !c.isLoaded())) return;
        base.ifPresent(b -> {
            b.remove();
            Bukkit.broadcastMessage("Despawned!");
        });
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
                getRide().getOnRide().add(tp.getUniqueId());
                s.addPassenger(tp);
            });
        }
    }

    public void removePassenger(CPlayer tp) {
        getSeats().forEach(s -> s.removePassenger(tp));
        tp.teleport(getRide().getExit());
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
        getPassengers().forEach(this::removePassenger);
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
}