package network.palace.ridemanager.handlers.builder;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.actions.FakeExitAction;
import network.palace.ridemanager.handlers.builder.actions.FakeSpawnAction;
import network.palace.ridemanager.handlers.builder.actions.FakeStraightAction;
import network.palace.ridemanager.handlers.builder.actions.FakeTurnAction;
import network.palace.ridemanager.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PathDataTimer implements Runnable {

    @Override
    public void run() {
        new ArrayList<>(RideManager.getRideBuilderUtil().getSessions()).forEach(session -> {
            if (session.isPath()) {
                runTimer(session, true);
            }
        });
    }

    public Location runTimer(BuildSession session, boolean particles) {
        CPlayer player = Core.getPlayerManager().getPlayer(session.getUuid());
        if (player == null) return null;
        Location start = session.getSpawn();
        double speed = session.getSpeed();
        List<RideAction> actions = session.getActions();
        RideAction editAction = session.getEditAction();
        if (editAction != null) {
            actions.add(editAction);
        }
        for (RideAction action : actions) {
            boolean finished = false;
            switch (action.getActionType()) {
                case WAIT: {
                    if (particles) pathParticle(player, ((FakeSpawnAction) action).getLoc(), 0, 0, 1);
                    break;
                }
                case SPAWN: {
                    if (particles) pathParticle(player, ((FakeSpawnAction) action).getLoc(), 1, 1, 0);
                    break;
                }
                case STRAIGHT:
                case EXIT: {
                    Location original = start.clone();
                    Location to = action.getActionType().equals(ActionType.STRAIGHT) ? ((FakeStraightAction) action).getTo() : ((FakeExitAction) action).getTo();

                    if (to.equals(original)) {
                        finished = true;
                    }

                    if (particles) pathParticle(player, start, true);

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
                            start.setYaw(yaw);
                            finished = true;
                        } else {
                            Vector v = next.toVector().subtract(start.toVector());
                            if (v.getY() == 0) {
                                v.setY(MovementUtil.getYMin());
                            }
                            start = next;
                            start.setYaw(yaw);
                        }
                        if (particles) pathParticle(player, start, false);
                    }
                    break;
                }
                case TURN: {
                    Location original = start.clone();
                    FakeTurnAction ac = (FakeTurnAction) action;

                    Location to = ac.getTo();

                    Location p0 = ac.getP0();
                    Location p1 = null;
                    Location p2 = null;

                    double yDifference = 0;
                    double t = 0;

                    if (particles) pathParticle(player, start, true);

                    if (particles) pathParticle(player, p0, true);

                    while (!finished) {
                        if (p1 == null) {
                            double p1_x = original.getX();
                            double p1_z = original.getZ();

                            double p2_x = to.getX();
                            double p2_z = to.getZ();

                            p1 = new Location(original.getWorld(), p1_x, original.getY(), p1_z);
                            p2 = new Location(original.getWorld(), p2_x, original.getY(), p2_z);

                            yDifference = to.getY() - original.getY();
                        }

                        if (t >= 1) {
                            t = 1;
                            Vector v = to.toVector().subtract(start.clone().toVector());
                            if (v.getY() == 0) {
                                v.setY(MovementUtil.getYMin());
                            }
                            to.setYaw(MathUtil.getBezierAngleAt(t, p0, p1, p2) - 90);
                            start = to;
                            finished = true;
                            if (particles) pathParticle(player, start, false);
                            break;
                        }

                        double x = (Math.pow((1 - t), 2) * p1.getX())
                                + (2 * t * (1 - t) * p0.getX())
                                + Math.pow(t, 2) * p2.getX();

                        double y;
                        if (yDifference != 0) {
                            y = original.getY() + (yDifference * t);
                        } else {
                            y = original.getY();
                        }

                        double z = (Math.pow((1 - t), 2) * p1.getZ())
                                + (2 * t * (1 - t) * p0.getZ())
                                + Math.pow(t, 2) * p2.getZ();

                        Location next = new Location(original.getWorld(), x, y, z, original.getYaw(), 0);

                        Vector v = next.toVector().subtract(start.clone().toVector());
                        if (v.getY() == 0) {
                            v.setY(MovementUtil.getYMin());
                        }

                        next.setYaw(MathUtil.getBezierAngleAt(t, p0, p1, p2) - 90);
                        start = next;
                        if (particles) pathParticle(player, start, false);

                        double v1_x = 2 * p1.getX() - 4 * p0.getX() + 2 * p2.getX();
                        double v1_z = 2 * p1.getZ() - 4 * p0.getZ() + 2 * p2.getZ();

                        double v2_x = 2 * p0.getX() - 2 * p1.getX();
                        double v2_z = 2 * p0.getZ() - 2 * p1.getZ();

                        Vector v1 = new Vector(v1_x, 0, v1_z);
                        Vector v2 = new Vector(v2_x, 0, v2_z);

                        double denominator = ((v1.multiply(t)).add(v2)).length();

                        if (denominator == 0) {
                            denominator = 1;
                        }

                        t += (speed / denominator);
                        if (t > 1) {
                            t = 1;
                        }
                    }
                    break;
                }
            }
        }
        return start;
    }

    private void pathParticle(CPlayer player, Location loc, boolean action) {
        float r = 0, g = 0, b = 0;
        if (action) {
            g = 0.8f;
        } else {
            r = 1;
        }
        pathParticle(player, loc, r, g, b);
    }

    private void pathParticle(CPlayer player, Location loc, float r, float g, float b) {
        if (player.getLocation().distance(loc) > 15) return;
        player.getParticles().send(loc, Particle.REDSTONE, 0, r, g, b, 1);
    }
}
