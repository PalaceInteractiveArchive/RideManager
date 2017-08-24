package network.palace.ridemanager.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.Cart;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

/**
 * @author Marc
 * @since 8/10/17
 */
public class RideBuilderUtil {
    private HashMap<UUID, BuildSession> sessions = new HashMap<>();

    public RideBuilderUtil() {
    }

    public BuildSession getSession(CPlayer player) {
        return sessions.get(player.getUniqueId());
    }

    public BuildSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public BuildSession newSession(CPlayer player) {
        BuildSession session = new BuildSession(player.getUniqueId());
        sessions.put(player.getUniqueId(), session);
        return session;
    }

    public BuildSession loadSession(CPlayer player, File file) {
        BuildSession session = newSession(player);
        player.sendMessage(ChatColor.GREEN + "Session created, loading actions now...");
        session.load(file);
        return session;
    }

    public BuildSession removeSession(UUID uuid) {
        return sessions.remove(uuid);
    }

    @RequiredArgsConstructor
    public class BuildSession {
        @Getter private final UUID uuid;
        @Getter private List<RideAction> actions = new ArrayList<>();
        @Getter @Setter private Location spawn;
        @Getter @Setter private double speed;
        @Getter private boolean loading = false;
        @Getter @Setter private double lockY = 0;
        @Getter @Setter private RideAction currentAction = null;
        private ConfirmCallback confirm = null;

        /**
         * Load actions from a file save
         *
         * @param file the file
         */
        public void load(File file) {
            loading = true;
            Bukkit.getScheduler().runTaskAsynchronously(RideManager.getInstance(), new FileRideLoader(null, file, new RideCallback() {
                @Override
                public void done(LinkedList<RideAction> list, Location spawn, double speed) {
                    actions = list;
                    setSpawn(spawn);
                    setSpeed(speed);
                    loading = false;
                    Core.getPlayerManager().getPlayer(uuid).sendMessage(ChatColor.GREEN + "Your Build Session has loaded!");
                }
            }));
        }

        /**
         * If there is a defined confirm callback, execute it
         */
        public void confirm() {
            CPlayer player = Core.getPlayerManager().getPlayer(uuid);
            if (confirm == null) {
                player.sendMessage(ChatColor.RED + "You don't have anything to confirm!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Action confirmed!");
            confirm.done(uuid);
            confirm = null;
        }

        /**
         * If there is a defined confirm callback, deny it
         */
        public void deny() {
            CPlayer player = Core.getPlayerManager().getPlayer(uuid);
            if (confirm == null) {
                player.sendMessage(ChatColor.RED + "You don't have anything to deny!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Action denied!");
            confirm = null;
        }

        public void setConfirm(CPlayer player, ConfirmCallback confirm) {
            this.confirm = confirm;
            player.sendMessage(ChatColor.RED + "Type " + ChatColor.GREEN + "/rb confirm " + ChatColor.RED +
                    "to allow this action, or " + ChatColor.GREEN + "/rb deny " + ChatColor.RED + "to cancel.");
        }

        /**
         * Return whether or not the session has a confirm callback defined
         *
         * @return true if confirm equals null
         */
        public boolean hasConfirm() {
            return confirm != null;
        }

        /**
         * Called when a player places a block
         *
         * @param block the block they place
         * @return true if the block event should be cancelled
         */
        public boolean placeBlock(Block block) {
            Location loc = block.getLocation();
            BlockAction a = BlockAction.fromBlock(block);
            if (a == null) {
                return true;
            }
            if (currentAction == null) {
                currentAction = a.newAction();
            }
            return true;
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

    @AllArgsConstructor
    @Getter
    public enum BlockAction {
        SPAWN(Material.STAINED_CLAY, (byte) 5), STRAIGHT(Material.STAINED_CLAY, (byte) 4), TURN(Material.STAINED_CLAY, (byte) 14),
        ROTATE(Material.STAINED_CLAY, (byte) 1), WAIT(Material.STAINED_CLAY, (byte) 13), INCLINE(Material.STAINED_CLAY, (byte) 3),
        DECLINE(Material.STAINED_CLAY, (byte) 11), TELEPORT(Material.STAINED_CLAY, (byte) 9), EXIT(Material.STAINED_CLAY, (byte) 15);
        private final Material type;

        private final byte data;

        public ItemStack getItem() {
            return ItemUtil.create(type, 1, data);
        }

        public static BlockAction fromBlock(Block b) {
            for (BlockAction a : BlockAction.values()) {
                if (a.type.equals(b.getType()) && a.data == b.getData()) {
                    return a;
                }
            }
            return null;
        }

        public static ItemStack[] getItems() {
            ItemStack[] array = new ItemStack[]{};
            int i = 0;
            for (BlockAction a : BlockAction.values()) {
                if (a.equals(SPAWN)) continue;
                array[i] = a.getItem();
                i++;
            }
            return array;
        }

        public RideAction newAction() {
            switch (this) {
                case SPAWN:
                    return new SpawnAction();
                case STRAIGHT:
                    return new StraightAction();
                case TURN:
                    return new TurnAction();
                case ROTATE:
                    return new RotateAction();
                case WAIT:
                    return new WaitAction();
                case INCLINE:
                case DECLINE:
                case TELEPORT:
                    return new TeleportAction();
                case EXIT:
                    return new ExitAction();
            }
            return null;
        }
    }

    @Getter
    @Setter
    public static class SpawnAction extends FakeAction {
        private Location location;
        private double speed;
        private float yaw;

        public SpawnAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.SpawnAction(location, speed, yaw);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class StraightAction extends FakeAction {
        private Location to;

        public StraightAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.StraightAction(to);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class TurnAction extends FakeAction {
        private Location origin;
        private int angle;

        public TurnAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.TurnAction(origin, angle);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class RotateAction extends FakeAction {
        private int angle;
        private boolean rightTurn;
        private long ticks;

        public RotateAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.RotateAction(angle, rightTurn, ticks);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class WaitAction extends FakeAction {
        private long ticks;

        public WaitAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.WaitAction(ticks);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class TeleportAction extends FakeAction {
        private Location to;

        public TeleportAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.TeleportAction(to);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class ExitAction extends FakeAction {
        private Location to;

        public ExitAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.ExitAction(to);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private static abstract class FakeAction extends RideAction {

        public FakeAction(boolean movementAction) {
            super(movementAction);
        }

        @Override
        public void execute() {
        }

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public RideAction load(Cart cart) {
            this.cart = cart;
            return this;
        }
    }
}
