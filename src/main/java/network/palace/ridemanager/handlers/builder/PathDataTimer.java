package network.palace.ridemanager.handlers.builder;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.actions.*;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PathDataTimer implements Runnable {
    @Override
    public void run() {
        for (BuildSession session : new ArrayList<>(RideManager.getRideBuilderUtil().getSessions())) {
            CPlayer player = Core.getPlayerManager().getPlayer(session.getUuid());
            if (!session.isPath() || player == null) continue;
            Location start = session.getSpawn();
            double speed = session.getSpeed();
            for (RideAction action : session.getActions()) {
                boolean finished = false;
                if (action instanceof FakeWaitAction) {
                    // do something
                } else if (action instanceof FakeStraightAction || action instanceof FakeExitAction) {
                    Location original = start.clone();
                    Location to = action instanceof FakeStraightAction ? ((FakeStraightAction) action).getTo() : ((FakeExitAction) action).getTo();

                    pathParticle(player, start, true);

                    while (!finished) {
                        float yaw = (float) Math.toDegrees(Math.atan2(original.getZ() - to.getZ(), original.getX() - to.getX())) + 90;
                        double distance = original.distance(to);
                        Vector resultant = to.clone().subtract(start).toVector().normalize();
                        Vector change = resultant.multiply(new Vector(speed, speed, speed));
                        Location next = start.clone().add(change);
                        if (next.distance(original) >= distance) {
                            Vector v = to.toVector().subtract(start.toVector());
                            if (v.getY() == 0) {
                                v.setY(MovementUtil.getYMin());
                            }
                            start = to;
                            finished = true;
                        } else {
                            Vector v = next.toVector().subtract(start.toVector());
                            if (v.getY() == 0) {
                                v.setY(MovementUtil.getYMin());
                            }
                            start = next;
                        }
                        pathParticle(player, start, false);
                    }
                } else if (action instanceof FakeTurnAction) {
                    Location original = start.clone();
                    FakeTurnAction ac = (FakeTurnAction) action;
                    Location origin = ac.getOrigin();
                    int angle = ac.getAngle();

                    boolean clockwise = angle > 0;
                    double radius = 0;
                    float originAngle = 0;
                    float targetAngle = 0;
//                        float yawDifference;
                    double originalY = 0;
                    double yDifference = 0;
                    double yChange = 0;

                    pathParticle(player, start, true);

                    while (!finished) {
                        if (radius == 0) {
                            if (angle > 180 || angle == 0) {
                                finished = true;
                                Bukkit.getLogger().severe("Cannot have a turn travel more than 180 degrees or equal 0!");
                                return;
                            }
                            radius = original.distance(origin);
                            originAngle = (float) Math.toDegrees(Math.atan2(origin.getX() - original.getX(), original.getZ() - origin.getZ()));
//                                yawDifference = cart.getYaw() - originAngle;
                            originalY = original.getY();
                            yDifference = 2 * (origin.getY() - original.getY());
                            yChange = MovementUtil.pythag((Math.abs(angle) * radius * Math.PI) / 180, yDifference);
                            targetAngle = originAngle + angle;
                        }
                        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / (speed * 1.66));
                        float dynamicAngle;
                        if ((clockwise && originAngle + angleChange > targetAngle) || (!clockwise && originAngle + angleChange < targetAngle)) {
                            dynamicAngle = targetAngle;
                            finished = true;
                        } else {
                            dynamicAngle = originAngle += angleChange;
                        }
                        Location rel = origin.clone();
                        Location current = start.clone();
                        rel.setY(current.getY());
                        Location target = Ride.getRelativeLocation(-dynamicAngle, radius, rel);
//                            target.setYaw(dynamicAngle + yawDifference);
//                            Bukkit.broadcastMessage(yDifference + " " + yChange + " " + cart.getPower() + " " + target.getRelativeY());
                        if (yDifference != 0) {
                            target.setY(target.getY() + (yChange / (20 / speed)));
                        }
//                            cart.setYaw(target.getYaw());
                        Vector v = target.toVector().subtract(start.clone().toVector());
                        if (v.getY() == 0) {
                            v.setY(MovementUtil.getYMin());
                        }
//                            cart.setVelocity(v);
//                            cart.teleport(target);
                        start = target;
                        pathParticle(player, start, false);
                    }
                } else if (action instanceof FakeSpeedAction) {
                    FakeSpeedAction ac = (FakeSpeedAction) action;
                    speed = ac.getSpeed();
                }
            }
        }
    }

    private void pathParticle(CPlayer player, Location loc, boolean action) {
        if (player.getLocation().distance(loc) > 15) return;
        if (action) {
            player.getParticles().send(loc, Particle.REDSTONE, 0, 0, 0.8f, 0, 1);
        } else {
            player.getParticles().send(loc, Particle.REDSTONE, 0, 1, 0, 0, 1);
        }
    }
}
