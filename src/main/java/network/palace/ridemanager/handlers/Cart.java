package network.palace.ridemanager.handlers;

import lombok.Getter;
import net.minecraft.server.v1_11_R1.EntityArmorStand;
import network.palace.core.player.CPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.math.BigDecimal;

/**
 * Created by Marc on 1/15/17.
 */
public class Cart {
    @Getter private final SignRide ride;
    private final ArmorStand stand;
    @Getter private final ItemStack model;
    @Getter private double power = 0;
    private double lastPower = 0.1;
    private Location lastSignCheck = null;
    private boolean rotating = false;
    private Location rotTarget = null;
    private Location rotOrigin = null;
    private double rotYaw = 0;
    private double rotRadius = 0;
    private int rotatingDegree = 0;
    private double targetDegree = 0;
    private double startingAngle = 0;
    private Vector lastMovement = new Vector(0, 0, 0);
    private boolean test = false;

    public Cart(SignRide ride, Location loc, ItemStack model, BlockFace direction) {
        this(ride, loc, model, direction, 0.1);
    }

    public Cart(SignRide ride, Location loc, ItemStack model, BlockFace direction, double power) {
        this.ride = ride;
        this.lastSignCheck = loc.clone();
        this.stand = loc.getWorld().spawn(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()), ArmorStand.class);
        setPower(power);
        stand.setVisible(false);
        stand.setGravity(false);
        this.model = model;
        stand.setHelmet(model);
        double headAngle = ride.getAngleFromDirection(direction);
        stand.setHeadPose(new EulerAngle(0, Math.toRadians(headAngle), 0));
        switch (direction) {
            case NORTH:
                lastMovement = new Vector(0, 0, -power);
                break;
            case NORTH_NORTH_EAST:
                lastMovement = new Vector(3 * power / 4, 0, -power / 2);
                break;
            case NORTH_EAST:
                lastMovement = new Vector(power / 2, 0, -power / 2);
                break;
            case EAST_NORTH_EAST:
                lastMovement = new Vector(power / 2, 0, 3 * -power / 4);
                break;
            case EAST:
                lastMovement = new Vector(power, 0, 0);
                break;
            case EAST_SOUTH_EAST:
                lastMovement = new Vector(power / 2, 0, 3 * power / 4);
                break;
            case SOUTH_EAST:
                lastMovement = new Vector(power / 2, 0, power / 2);
                break;
            case SOUTH_SOUTH_EAST:
                lastMovement = new Vector(3 * power / 4, 0, power / 2);
                break;
            case SOUTH:
                lastMovement = new Vector(0, 0, power);
                break;
            case SOUTH_SOUTH_WEST:
                lastMovement = new Vector(3 * -power / 4, 0, power / 2);
                break;
            case SOUTH_WEST:
                lastMovement = new Vector(-power / 2, 0, -power / 2);
                break;
            case WEST_SOUTH_WEST:
                lastMovement = new Vector(-power / 2, 0, 3 * power / 4);
                break;
            case WEST:
                lastMovement = new Vector(-power, 0, 0);
                break;
            case WEST_NORTH_WEST:
                lastMovement = new Vector(-power / 2, 0, 3 * -power / 4);
                break;
            case NORTH_WEST:
                lastMovement = new Vector(-power / 2, 0, -power / 2);
                break;
            case NORTH_NORTH_WEST:
                lastMovement = new Vector(3 * -power / 4, 0, -power / 2);
                break;
        }
    }

    public void move() {
        Location loc = getLocation();
        Location next = loc.clone();
        Vector movement = lastMovement.clone();
        if (power != lastPower) {
            //Update movement vector if power has changed
            if (power != 0) {
                if (lastPower != 0) {
                    double x = (lastMovement.getX() * power) / lastPower;
                    double y = (lastMovement.getY() * power) / lastPower;
                    double z = (lastMovement.getZ() * power) / lastPower;
                    movement = new Vector(x, y, z);
                    this.lastPower = this.power;
                }
            } else {
                movement = new Vector();
            }
        }
//        Bukkit.broadcastMessage(ChatColor.GREEN + "" + movement.getX() + " | " + movement.getZ());
        next.add(movement);
        boolean allowed = false;
        Location tloc = next.clone().add(lastMovement.getX(), 0, lastMovement.getZ());
        if (tloc.getBlockX() != next.getBlockX() || tloc.getBlockZ() != next.getBlockZ()) {
            allowed = true;
        } else if (lastMovement.getX() != 0) {
            if (lastMovement.getX() >= 0) {
                if (Math.abs(next.getX() - next.getBlockX()) >= 0.5) {
                    allowed = true;
                }
            } else {
                if (Math.abs(1 - (next.getX() - next.getBlockX())) >= 0.5) {
                    allowed = true;
                }
            }
        } else if (lastMovement.getZ() != 0) {
            if (lastMovement.getZ() >= 0) {
                if (Math.abs(next.getZ() - next.getBlockZ()) >= 0.5) {
                    allowed = true;
                }
            } else {
                if (Math.abs(1 - (next.getZ() - next.getBlockZ())) >= 0.5) {
                    allowed = true;
                }
            }
        }
        if ((lastSignCheck.getBlockX() != loc.getBlockX() || lastSignCheck.getBlockZ() != loc.getBlockZ()) && allowed) {
            lastSignCheck = loc.clone();
            World w = loc.getWorld();
            Sign s = null;
            Block b = w.getBlockAt(next.getBlockX(), ride.getYAxis(), next.getBlockZ());
            if (b.getType().name().toLowerCase().contains("sign")) {
                s = (Sign) b.getState();
            }
            if (s != null && s.getLine(0).equalsIgnoreCase("train")) {
                switch (s.getLine(1)) {
                    case "speed": {
                        double newPower = Double.parseDouble(s.getLine(2));
                        setPower(newPower);
                        break;
                    }
                    case "rotate": {
                        if (rotating) {
                            break;
                        }
                        rotating = true;
                        rotatingDegree = Integer.parseInt(s.getLine(2));
                        rotRadius = Double.parseDouble(s.getLine(3));
                        double rotDis = Math.abs((2 * Math.PI * rotRadius) / (360 / rotatingDegree));
                        rotYaw = rotatingDegree / (rotDis / power);
                        rotOrigin = getOrigin(rotatingDegree, rotRadius, loc);
                        Bukkit.broadcastMessage(ChatColor.GOLD + "x: " + rotOrigin.getX() + " | y: " + rotOrigin.getY() + " | z: " + rotOrigin.getZ());
                        startingAngle = Math.toDegrees(stand.getHeadPose().getY());
                        if (rotatingDegree >= 0) {
                            targetDegree = rotatingDegree + Math.toDegrees(stand.getHeadPose().getY()) + 270;
                            targetDegree %= 360;
                            double rad = Math.toRadians(targetDegree);
                            double x = Math.sin(rad) * -rotRadius;
                            double z = Math.cos(rad) * rotRadius;
                            rotTarget = rotOrigin.clone().add(x, 0, z);
                        } else {
                            targetDegree = rotatingDegree + Math.toDegrees(stand.getHeadPose().getY()) + 450;
                            targetDegree %= 360;
                            double rad = Math.toRadians(targetDegree);
                            double x = Math.sin(rad) * -rotRadius;
                            double z = Math.cos(rad) * rotRadius;
                            rotTarget = rotOrigin.clone().add(x, 0, z);
                        }
                        break;
                    }
                }
            }
        }
        boolean rotUpdateMove = false;
        if (rotating) {
            double headDegrees = Math.toDegrees(stand.getHeadPose().getY());
            if (rotatingDegree >= 0) {
                headDegrees += 270;
            } else {
                headDegrees += 450;
            }
//            Bukkit.broadcastMessage(headDegrees + " " + targetDegree + " " + rotYaw);
            if (rotatingDegree >= 0 ? (headDegrees + rotYaw >= targetDegree) : (headDegrees + rotYaw <= targetDegree)) {
                rotating = false;
            }
            next = loc;

            /**
             * Head Calculations
             */
            double head = Math.toRadians(rotYaw);
            if (!rotating) {
                stand.setHeadPose(stand.getHeadPose().setY(Math.toRadians((startingAngle + rotatingDegree) % 360)));
            } else {
                stand.setHeadPose(stand.getHeadPose().add(0, head, 0));
            }
            //Bukkit.broadcastMessage(String.valueOf(Math.toDegrees(stand.getHeadPose().getY())));

            /**
             * Movement Calculations
             */
            double deg = Math.toDegrees(stand.getHeadPose().getY()) + (rotatingDegree >= 0 ? 270 : 450);
            double rad = Math.toRadians(deg);
            double x = round(Math.sin(rad) * -rotRadius, 6);
            double z = round(Math.cos(rad) * rotRadius, 6);
            next = rotOrigin.clone().add(x, 0, z);
//            Bukkit.broadcastMessage(ChatColor.GREEN + "x: " + next.getX() + " | y: " + next.getY() + " | z: " + next.getZ());
            if (!rotating) {
                Bukkit.broadcastMessage(ChatColor.RED + "TEST");
                rotUpdateMove = true;
                double cx = lastMovement.getX();
                double cy = lastMovement.getY();
                double cz = lastMovement.getZ();
////                if (rotatingDegree >= 0) {
//                double dg = Math.toRadians((targetDegree + 90) % 360);
//                double nx = Math.cos(dg) * cx - Math.sin(dg) * cz;
//                double nz = Math.sin(dg) * cx + Math.cos(dg) * cz;
//
//                double rads = Math.toRadians(rotatingDegree);
//
//                double currentX = lastMovement.getX();
//                double currentZ = lastMovement.getZ();
//
//                double cosine = Math.cos(rad);
//                double sine = Math.sin(rad);
//
//                double xPrime2 = (cosine * currentX - sine * currentZ);
//                double zPrime2 = (sine * currentX + cosine * currentZ);
//
//                double u = rotOrigin.getX();
//                double v = rotOrigin.getY();
//                double w = rotOrigin.getZ();
//
//                double dr = Math.toRadians(rotatingDegree);
//
//                double xPrime = u * (u * cx + v * cy + w * cz) * (1d - Math.cos(dr)) + cx * Math.cos(dr) + (-w * cy + v * cz) * Math.sin(dr);
//                double yPrime = v * (u * cx + v * cy + w * cz) * (1d - Math.cos(dr)) + cy * Math.cos(dr) + (w * cx - u * cz) * Math.sin(dr);
//                double zPrime = w * (u * cx + v * cy + w * cz) * (1d - Math.cos(dr)) + cz * Math.cos(dr) + (-v * cx + u * cy) * Math.sin(dr);
//
//                /**
//                 90 degrees CW about y-axis: (x, y, z) -> (-z, y, x)
//                 90 degrees CCW about y-axis: (x, y, z) -> (z, y, -x)
//                 **/
//
//                double ratio = Math.abs(rotatingDegree) / 90D;

                /*if (!test) {
                    double radJ = Math.toRadians(rotatingDegree);

                    double currentXJ = lastMovement.getX();
                    double currentZJ = lastMovement.getZ();

                    double cosineJ = Math.cos(radJ);
                    double sineJ = Math.sin(radJ);

                    double testX = (cosineJ * currentXJ - sineJ * currentZJ);
                    double testZ = (sineJ * currentXJ + cosineJ * currentZJ);

                    lastMovement.setX(testX);
                    lastMovement.setZ(testZ);
                    test = true;
                }*/

//                    lastMovement.setZ(nz);
//                Bukkit.broadcastMessage(cx + " " + nx + " | " + cz + " " + nz + " | " + dg + " " + targetDegree);
//                Bukkit.broadcastMessage(cx + " " + testX + " | " + cz + " " + testZ);
                Vector v = rotate(rotatingDegree, 0, lastMovement.getX(), lastMovement.getY(), lastMovement.getZ());
                lastMovement.setX(round(v.getX(), 6));
                lastMovement.setY(round(v.getY(), 6));
                lastMovement.setZ(round(v.getZ(), 6));

                rotTarget = null;
                rotOrigin = null;
                rotYaw = 0;
                rotRadius = 0;
                rotatingDegree = 0;
                targetDegree = 0;
//                } else {
//                }
            }
        }
        if (Math.abs(next.getX() - loc.getX()) > 1) {
            if (next.getX() >= loc.getX()) {
                next.setX(loc.getX() + 1);
            } else {
                next.setX(loc.getX() - 1);
            }
        }
        if (Math.abs(next.getY() - loc.getY()) > 1) {
            if (next.getY() >= loc.getY()) {
                next.setY(loc.getY() + 1);
            } else {
                next.setY(loc.getY() - 1);
            }
        }
        if (Math.abs(next.getZ() - loc.getZ()) > 1) {
            if (next.getZ() >= loc.getZ()) {
                next.setZ(loc.getZ() + 1);
            } else {
                next.setZ(loc.getZ() - 1);
            }
        }
//        Bukkit.broadcastMessage(ChatColor.RED + "x: " + next.getX() + " | y: " + next.getY() + " | z: " + next.getZ());
        EntityArmorStand s = ((CraftArmorStand) stand).getHandle();
        s.locX = next.getX();
        s.locY = next.getY();
        s.locZ = next.getZ();
        s.positionChanged = true;
        if (movement.hashCode() != lastMovement.hashCode() && !rotUpdateMove) {
            this.lastMovement = movement;
        }
    }

    private double round(double value, int precision) {
        BigDecimal bd = new BigDecimal(value).setScale(precision, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    public static Vector rotate(float yaw, float pitch, double x, double y, double z) {
        // Conversions found by (a lot of) testing
        float angle;
        angle = yaw * 0.017453293F;
        double sinyaw = Math.sin(angle);
        double cosyaw = Math.cos(angle);

        angle = pitch * 0.017453293F;
        double sinpitch = Math.sin(angle);
        double cospitch = Math.cos(angle);

        Vector vector = new Vector();
        vector.setZ((x * sinyaw) - (y * cosyaw * sinpitch) - (z * cosyaw * cospitch));
        vector.setY((y * cospitch) - (z * sinpitch));
        vector.setX(-(x * cosyaw) - (y * sinyaw * sinpitch) - (z * sinyaw * cospitch));
        Bukkit.broadcastMessage(ChatColor.GOLD + new Vector(x, y, z).toString() + "\n" + ChatColor.GREEN + vector.toString() + "\n");
        return vector;
    }

    private Location getOrigin(int angle, double radius, Location loc) {
        double vx = lastMovement.getX();
        double vz = lastMovement.getZ();
        boolean xPos = vx >= 0;
        boolean zPos = vz >= 0;
        int mult = (xPos && !zPos || !xPos && zPos) ? -1 : 1;
        if (angle >= 0) {
            double tempAng = Math.atan(vx / vz);
            double x = Math.cos(tempAng) * radius;
            double z = Math.sin(tempAng) * radius;
            Bukkit.broadcastMessage(x + " " + z);
            return new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z, loc.getYaw(), loc.getPitch());
        } else {
            double tempAng = Math.atan(vx / vz);
            double x = Math.cos(tempAng) * radius;
            double z = Math.sin(tempAng) * radius;
            Bukkit.broadcastMessage(x + " " + z);
            return new Location(loc.getWorld(), loc.getX() - x, loc.getY(), loc.getZ() - z, loc.getYaw(), loc.getPitch());
        }
    }

    public void setPower(double p) {
        if (this.power != 0) {
            this.lastPower = this.power;
        }
        this.power = p > 1 ? 1 : (p < -1 ? -1 : p);
    }

    private boolean withinDistance(Location loc, Location target, double distance) {
        return loc.getX() >= target.getX() - distance && loc.getX() <= target.getX() + distance &&
                loc.getY() >= target.getY() - distance && loc.getY() <= target.getY() + distance &&
                loc.getZ() >= target.getZ() - distance && loc.getZ() <= target.getZ() + distance;
    }

    public Location getLocation() {
        return stand.getLocation();
    }

    public void despawn() {
        stand.remove();
    }

    public void addPassenger(CPlayer tp) {
        stand.addPassenger(tp.getBukkitPlayer());
    }
}
