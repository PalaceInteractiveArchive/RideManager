package network.palace.ridemanager.utils;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.core.utils.MathUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.*;
import network.palace.ridemanager.handlers.builder.PathDataTimer;
import network.palace.ridemanager.handlers.builder.actions.*;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

/**
 * @author Marc
 * @since 8/10/17
 */
public class RideBuilderUtil {
    private HashMap<UUID, BuildSession> sessions = new HashMap<>();
    private List<UUID> inventory = new ArrayList<>();
    @Getter private PathDataTimer pathDataTimer;

    public RideBuilderUtil() {
        pathDataTimer = new PathDataTimer();
        Core.runTaskTimer(RideManager.getInstance(), pathDataTimer, 0L, 20L);
        Core.runTaskTimer(RideManager.getInstance(), () -> {
            for (BuildSession session : sessions.values()) {
                CPlayer player = Core.getPlayerManager().getPlayer(session.getUuid());
                if (player == null) continue;
                RideAction editAction = session.getEditAction();
                if (editAction == null) continue;
                ChatColor finished = ((FakeAction) editAction).areFieldsIncomplete() ? ChatColor.RED : ChatColor.DARK_GREEN;
                player.getActionBar().show(finished + "Editing: " + ChatColor.GREEN + editAction.toString());
            }
        }, 0L, 20L);
    }

    /**
     * Get build session for a player
     *
     * @param player the player
     * @return the BuildSession or null if there is none
     */
    public BuildSession getSession(CPlayer player) {
        return sessions.get(player.getUniqueId());
    }

    /**
     * Get build session for a player
     *
     * @param uuid the uuid of the player
     * @return the BuildSession or null if there is none
     */
    public BuildSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    /**
     * Create a new build session for a player
     *
     * @param player the player
     * @return the new BuildSession
     */
    public BuildSession newSession(CPlayer player) {
        BuildSession session = new BuildSession(player.getUniqueId());
        sessions.put(player.getUniqueId(), session);
        session.updateBossBar();
        return session;
    }

    /**
     * Load a build session for a player
     *
     * @param player the player
     * @param file   the ride's file
     * @return the BuildSession
     */
    public BuildSession loadSession(CPlayer player, File file) {
        BuildSession session = newSession(player);
        player.sendMessage(ChatColor.GREEN + "Session created, loading actions now...");
        session.load(file);
        return session;
    }

    /**
     * Remove a player's build session
     *
     * @param uuid the uuid of the player
     * @return the BuildSession that was removed, or null if there was none
     */
    public BuildSession removeSession(UUID uuid) {
        BuildSession session = sessions.remove(uuid);
        if (session == null) return null;
        List<ArmorStand> stands = new ArrayList<>(session.getStands().values());
        for (ArmorStand stand : stands) {
            stand.remove();
        }
        session.removeBossBar();
        return session;
    }

    /**
     * Process a player moving for editing actions
     *
     * @param player the player
     * @param from   where the player moved from
     * @param to     where the player moved to
     */
    public void moveEvent(CPlayer player, Location from, Location to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ()) return;
        BuildSession session = getSession(player);
        if (session == null || !session.isEditingLocation()) return;
        RideAction a = session.getEditAction();
        switch (a.getActionType()) {
            case SPAWN:
            case STRAIGHT:
            case TURN:
            case INCLINE:
            case DECLINE:
            case TELEPORT:
            case LAUNCH:
            case STOP:
            case EXIT:
                break;
            default:
                return;
        }
        Vector diff;
        if (session.isChangeY()) {
            diff = new Vector(0, to.getY() - from.getY(), 0);
        } else if (session.isSneaking()) {
            diff = to.toVector().subtract(from.toVector());
            diff.setY(0);
        } else {
            return;
        }
        RideAction newAction = changeLocation(player, session, a, diff);
        if (newAction == null) return;
        player.playSound(to, Sound.BLOCK_COMPARATOR_CLICK, 1, 2);
        session.setEditAction(newAction);
    }

    /**
     * Set a player's sneaking value
     *
     * @param player the player
     * @param sneak  whether or not the player is sneaking
     */
    public void toggleShift(CPlayer player, boolean sneak) {
        BuildSession session = getSession(player);
        if (session == null) return;
        session.setSneaking(sneak);
    }

    /**
     * Edit an action's location
     *
     * @param player  the player
     * @param session the player's BuildSession
     * @param a       the action
     * @param v       the vector representing the change for the action
     * @return an updated instance of the action, or null if the action wasn't supported
     */
    public RideAction changeLocation(CPlayer player, BuildSession session, RideAction a, Vector v) {
        RideAction newAction = null;
        Location to = null;
        switch (a.getActionType()) {
            case EXIT: {
                FakeExitAction act = (FakeExitAction) a;
                newAction = new FakeExitAction(MathUtil.round(act.getTo().add(v), 4), act.getAutoYaw());
                to = ((FakeExitAction) newAction).getTo();
                break;
            }
            case SPAWN: {
                FakeSpawnAction act = (FakeSpawnAction) a;
                newAction = new FakeSpawnAction(MathUtil.round(act.getLoc().add(v), 4), act.getSpeed(), act.getYaw());
                to = ((FakeSpawnAction) newAction).getLoc();
                break;
            }
            case STRAIGHT: {
                FakeStraightAction act = (FakeStraightAction) a;
                newAction = new FakeStraightAction(MathUtil.round(act.getTo().add(v), 4), act.getAutoYaw());
                to = ((FakeStraightAction) newAction).getTo();
                break;
            }
            case TELEPORT: {
                FakeTeleportAction act = (FakeTeleportAction) a;
                newAction = new FakeTeleportAction(MathUtil.round(act.getTo().add(v), 4));
                to = ((FakeTeleportAction) newAction).getTo();
                break;
            }
            case TURN: {
                FakeTurnAction act = (FakeTurnAction) a;
                switch (session.getEditLocation()) {
                    case 0: {
                        newAction = new FakeTurnAction(MathUtil.round(act.getTo().clone().add(v), 4), act.getFrom(), act.getP0());
                        to = ((FakeTurnAction) newAction).getTo();
                        break;
                    }
                    case 1: {
                        Location from = act.getFrom();
                        float yaw = from.getYaw();
                        double rads = Math.toRadians(yaw);

                        Vector direction = new Vector(Math.cos(rads), 0, Math.sin(rads));

                        double length = direction.length() * Math.cos(v.angle(direction) - Math.toRadians(90));

                        double length_x = length * Math.cos(rads);
                        double length_z = length * Math.sin(rads);

                        Vector change = new Vector(-length_z, 0, length_x);

                        Vector facing = player.getLocation().getDirection().setY(0);

                        boolean backward = Math.abs(Math.toDegrees(v.angle(facing))) > 90;

                        if (backward) {
                            change.multiply(-1);
                        }

                        change.multiply(0.25);

                        newAction = new FakeTurnAction(act.getTo(), act.getFrom(), MathUtil.round(act.getP0().clone().add(change), 4));
                        to = ((FakeTurnAction) newAction).getP0();
                        break;
                    }
                }
                break;
            }
        }
        if (to != null) {
            player.getParticles().send(to, Particle.REDSTONE, 0, 0, 0, 1, 1);
        }
        return newAction;
    }

    /**
     * Convert a list of RideActions into FakeActions for ride building
     *
     * @param list the list of RideActions
     * @return a list of FakeActions
     * @implNote not every RideAction necessarily has a corresponding FakeAction
     */
    public List<RideAction> getFakeActions(LinkedList<RideAction> list) {
        List<RideAction> finalList = new ArrayList<>();
        for (RideAction a : list) {
            switch (a.getActionType()) {
                case EXIT: {
                    finalList.add(new FakeExitAction(((ExitAction) a).getTo(), ((ExitAction) a).getAutoYaw()));
                    break;
                }
                case SPAWN: {
                    finalList.add(new FakeSpawnAction(((SpawnAction) a).getLoc(), ((SpawnAction) a).getSpeed(), ((SpawnAction) a).getYaw()));
                    break;
                }
                case STRAIGHT: {
                    finalList.add(new FakeStraightAction(((StraightAction) a).getTo(), ((StraightAction) a).getAutoYaw()));
                    break;
                }
                case TELEPORT: {
                    finalList.add(new FakeTeleportAction(((TeleportAction) a).getTo()));
                    break;
                }
                case TURN: {
                    finalList.add(new FakeTurnAction(((TurnAction) a).getTo(), ((TurnAction) a).getP0()));
                    break;
                }
            }
        }
        return finalList;
    }

    /**
     * Get a list of existing BuildSessions
     *
     * @return a list of existing BuildSessions
     */
    public List<BuildSession> getSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Set a player's inventory for ride building
     *
     * @param uuid  the uuid of the player
     * @param value true if entering ride building, false if exiting
     */
    public void setInventory(UUID uuid, boolean value) {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (value) {
            if (inventory.contains(uuid)) return;
            inventory.add(uuid);
            if (player == null) return;
            int i = 0;
            PlayerInventory inv = player.getInventory();
            inv.setItem(0, ItemUtil.create(Material.COMPASS));
            inv.setItem(1, ItemUtil.create(Material.WOODEN_AXE));
            inv.setItem(2, ItemUtil.create(Material.LIME_TERRACOTTA, ChatColor.GREEN + "Create an Action"));
            inv.setItem(3, ItemUtil.create(Material.LIGHT_BLUE_TERRACOTTA, ChatColor.AQUA + "Create a Sensor"));
            inv.setItem(4, ItemUtil.create(Material.STONE_SHOVEL, ChatColor.GRAY + "Edit Action/Sensor"));
        } else {
            if (!inventory.contains(uuid)) return;
            inventory.remove(uuid);
            if (player == null) return;
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, ItemUtil.create(Material.AIR));
            }
            inv.setItem(0, ItemUtil.create(Material.COMPASS));
            inv.setItem(1, ItemUtil.create(Material.WOODEN_AXE));
        }
    }

    public interface ConfirmCallback {

        /**
         * Called when the confirm is executed.
         *
         * @param uuid the uuid
         */
        void done(UUID uuid);
    }
}
