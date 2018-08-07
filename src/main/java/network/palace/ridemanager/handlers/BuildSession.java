package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.sensors.RideSensor;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.handlers.builder.actions.FakeAction;
import network.palace.ridemanager.handlers.builder.actions.FakeExitAction;
import network.palace.ridemanager.handlers.builder.actions.FakeStraightAction;
import network.palace.ridemanager.handlers.builder.actions.FakeTurnAction;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.utils.MovementUtil;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class BuildSession {
    @Getter private final UUID uuid;
    @Getter @Setter private String name;
    @Getter @Setter private String fileName;
    private List<RideAction> actions = new ArrayList<>();
    private List<RideSensor> sensors = new ArrayList<>();
    @Getter @Setter private Location spawn;
    @Getter @Setter private double speed;
    @Getter private boolean loading = false;
    @Getter @Setter private double lockY = 0;
    @Getter @Setter private RideAction currentAction = null;
    @Getter @Setter private boolean showArmorStands = false;
    @Getter @Setter private boolean path;
    @Getter @Setter private boolean changeY;
    @Getter @Setter private boolean sneaking;
    @Getter HashMap<Location, ArmorStand> stands = new HashMap<>();
    private RideBuilderUtil.ConfirmCallback confirm = null;
    @Getter @Setter private Location currentLocation = null;
    @Getter @Setter private List<RideAction> possibleActions = null;
    @Getter @Setter private RideAction editAction = null;

    /**
     * Load actions from a file save
     *
     * @param file the file
     */
    public void load(File file) {
        loading = true;
        fileName = file.getName();
        Core.runTaskAsynchronously(new FileRideLoader(null, file, (name, actionList, sensorList, spawn, speed, setYaw) -> {
            setName(name);
            actions = RideManager.getRideBuilderUtil().getFakeActions(actionList);
//            sensors = RideManager.getRideBuilderUtil().getFakeSensors(sensorList);
            setSpawn(spawn);
            setSpeed(speed);
            loading = false;
            Core.getPlayerManager().getPlayer(uuid).sendMessage(ChatColor.GREEN + "Your Build Session has loaded!");
            RideManager.getRideBuilderUtil().setInventory(uuid, true);
            updateBossBar();
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

    public void setConfirm(CPlayer player, RideBuilderUtil.ConfirmCallback confirm) {
        this.confirm = confirm;
        player.getBossBar().setEverything(ChatColor.RED + "Confirm Action!", 1, BarColor.RED, BarStyle.SOLID);
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
    @SuppressWarnings("deprecation")
    public boolean placeBlock(CPlayer player, Block block) {
        if (!block.getType().equals(Material.STAINED_CLAY)) return false;
        switch (block.getData()) {
            // Action
            case 5: {
                if (currentAction instanceof FakeAction) {
                    if (((FakeAction) currentAction).areFieldsIncomplete()) {
                        player.sendMessage(ChatColor.RED + "There are some unfinished values in the previous action!");
                        return true;
                    }
                }
                currentLocation = block.getLocation();
                if (lockY != 0) {
                    currentLocation.setY(lockY);
                }
                openActionMenu(player);
                break;
            }
            // Sensor
            case 3: {
                break;
            }
        }
        /*
        RideBuilderUtil.BlockAction a = RideBuilderUtil.BlockAction.fromBlock(block);
        if (a == null) return false;

        if (currentAction != null) {
            if (!a.getClazz().equals(currentAction.getClass())) {
                actions.add(currentAction);
//            } else {
                // Modify currentAction value
            }
        }

        currentAction = a.newAction();
        String msg = ChatColor.GREEN + "You created a " + ChatColor.YELLOW;
        switch (a) {
            case SPAWN:
                msg += "Spawn";
                ((FakeSpawnAction) currentAction).setLocation(block.getLocation());
                break;
            case STRAIGHT:
                msg += "Straight";
                ((FakeStraightAction) currentAction).setTo(block.getLocation());
                break;
            case TURN:
                msg += "Turn";
                break;
            case ROTATE:
                msg += "Rotate";
                break;
            case WAIT:
                msg += "Wait";
                break;
            case INCLINE:
                msg += "Incline";
                break;
            case DECLINE:
                msg += "Decline";
                break;
            case TELEPORT:
                msg += "Teleport";
                break;
            case EXIT:
                msg += "Exit";
                break;
        }
        msg += " action!";
        player.sendMessage(msg);*/
        return true;
    }

    private void openActionMenu(CPlayer player) {
        Inventory inv = Bukkit.createInventory(player.getBukkitPlayer(), 27, ChatColor.GREEN + "New Action");
        int i = 0;
        for (ActionType a : ActionType.values()) {
            inv.setItem(i++, a.getItem());
            if (i >= 27) {
                break;
            }
        }
        player.openInventory(inv);
    }

    private void openSensorMenu(CPlayer player) {
    }

    @SuppressWarnings("deprecation")
    public void handleInventoryClick(InventoryClickEvent event, CPlayer player, String name) {
        ItemStack item = event.getCurrentItem();
        switch (name) {
            case "Choose Action": {
                player.closeInventory();
                for (RideAction action : possibleActions) {
                    ActionType a = ActionType.fromAction(action.getClass());
                    if (a == null) continue;
                    ItemStack actionItem = a.getItem();
                    if (actionItem == null) continue;
                    if (item.getType().equals(actionItem.getType()) && item.getData().getData() == actionItem.getData().getData()) {
                        editAction(player, action);
                        this.possibleActions = null;
                        return;
                    }
                }
                this.possibleActions = null;
                player.sendMessage(ChatColor.RED + "There was an error editing that action!");
                break;
            }
            case "New Action": {
                ActionType action = ActionType.fromItem(item);
                if (action == null) return;
                if (currentLocation == null) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Saved location was lost, place again!");
                    return;
                }
                if (currentAction instanceof FakeAction) {
                    if (((FakeAction) currentAction).areFieldsIncomplete()) {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + "There are some unfinished values in the previous action!");
                        return;
                    }
                    actions.add(currentAction);
                }
                currentAction = action.newAction(currentLocation);
                break;
            }
        }
    }

    public void handleInteract(PlayerInteractEvent event, CPlayer player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.getType().equals(Material.STONE_SPADE)) return;
        event.setCancelled(true);
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
            case PHYSICAL:
                return;
        }
        Block b = event.getClickedBlock();
        Location loc = b.getLocation();
        List<RideAction> possibleActions = new ArrayList<>();
        Location start = getSpawn();
        for (RideAction action : actions) {
            boolean finished = false;
            switch (action.getActionType()) {
                case WAIT:
                case SPAWN: {
                    if (start.distance(loc) <= 1.5) {
                        possibleActions.add(action);
                    }
                    break;
                }
                case STRAIGHT:
                case EXIT: {
                    Location original = start.clone();
                    Location to = action.getActionType().equals(ActionType.STRAIGHT) ? ((FakeStraightAction) action).getTo() : ((FakeExitAction) action).getTo();

                    if (start.distance(loc) <= 1.5) {
                        possibleActions.add(action);
                    }

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
                    }
                    break;
                }
                case TURN: {
                    if (start.distance(loc) <= 1.5) {
                        possibleActions.add(action);
                    }

                    Location original = start.clone();
                    FakeTurnAction ac = (FakeTurnAction) action;
                    Location origin = ac.getOrigin();
                    int angle = ac.getAngle();

                    boolean clockwise = angle > 0;
                    double radius = 0;
                    float originAngle = 0;
                    float targetAngle = 0;
                    double originalY = 0;
                    double yDifference = 0;
                    double yChange = 0;

                    while (!finished) {
                        if (radius == 0) {
                            if (angle > 180 || angle == 0) {
                                finished = true;
                                Bukkit.getLogger().severe("Cannot have a turn travel more than 180 degrees or equal 0!");
                                return;
                            }
                            radius = original.distance(origin);
                            originAngle = (float) Math.toDegrees(Math.atan2(origin.getX() - original.getX(), original.getZ() - origin.getZ()));
                            originalY = original.getY();
                            yDifference = 2 * (origin.getY() - original.getY());
                            yChange = MovementUtil.pythag((Math.abs(angle) * radius * Math.PI) / 180, yDifference);
                            targetAngle = originAngle + angle;
                        }
                        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360.0 / angle))) / (speed * 1.66));
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
                        if (yDifference != 0) {
                            target.setY(target.getY() + (yChange / (20 / speed)));
                        }
                        Vector v = target.toVector().subtract(start.clone().toVector());
                        if (v.getY() == 0) {
                            v.setY(MovementUtil.getYMin());
                        }
                        start = target;
                    }
                    break;
                }
            }
        }
        if (possibleActions.isEmpty()) return;
        if (possibleActions.size() == 1) {
            editAction(player, possibleActions.get(0));
            return;
        }
        chooseAction(player, possibleActions);
    }

    private void chooseAction(CPlayer player, List<RideAction> possibleActions) {
        int size = possibleActions.size();
        if (size == 1) {
            editAction(player, possibleActions.get(0));
            return;
        }
        Inventory inv = Bukkit.createInventory(player.getBukkitPlayer(),
                size > 9 ? (size > 18 ? (size > 27 ? (size > 36 ? (size > 45 ? 54 : 45) : 36) : 27) : 18) : 9,
                ChatColor.GREEN + "Choose Action");
        int i = 0;
        for (RideAction action : possibleActions) {
            ActionType type = ActionType.fromAction(action.getClass());
            if (type == null) continue;
            ItemStack item = type.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(action.toString()));
            item.setItemMeta(meta);
            inv.setItem(i++, item);
        }
        this.possibleActions = possibleActions;
        player.openInventory(inv);
    }

    private void editAction(CPlayer player, RideAction action) {
        this.editAction = action;
        editHelp(player, editAction);
    }

    public void editAction(CPlayer player, String[] args) {
        if (editAction == null) {
            player.sendMessage(ChatColor.RED + "You don't have an action to edit!");
            return;
        }
        if (args.length == 0) {
            editHelp(player, editAction);
            return;
        }
        if (args[0].equalsIgnoreCase("complete")) {
            player.sendMessage(ChatColor.RED + "You are no longer editing this " + editAction.getActionType() + " action.");

        }
    }

    private void editHelp(CPlayer player, RideAction action) {
        if (action.getActionType() == null) return;
        switch (action.getActionType()) {
            case WAIT: {
                player.sendMessage(ChatColor.GREEN + "To edit a Wait action, type:");
                player.sendMessage(ChatColor.YELLOW + "/rb a [time in ticks, 20 ticks per second]");
                break;
            }
            case SPAWN: {
                player.sendMessage(ChatColor.GREEN + "To edit a Spawn action, hold shift and move the action.");
                break;
            }
            case STRAIGHT: {
                player.sendMessage(ChatColor.GREEN + "To edit a Straight action, type:");
                player.sendMessage(ChatColor.YELLOW + "/rb a to" + ChatColor.GREEN + " to edit the 'to' location (hold shift and move)");
                player.sendMessage(ChatColor.YELLOW + "/rb a autoyaw [true/false]" + ChatColor.GREEN + " to edit the autoyaw setting");
                break;
            }
            case EXIT: {
                player.sendMessage(ChatColor.GREEN + "To edit an End action, type:");
                player.sendMessage(ChatColor.YELLOW + "/rb a to" + ChatColor.GREEN + " to edit the 'to' location (hold shift and move)");
                player.sendMessage(ChatColor.YELLOW + "/rb a autoyaw [true/false]" + ChatColor.GREEN + " to edit the autoyaw setting");
                break;
            }
            case TURN: {
                player.sendMessage(ChatColor.GREEN + "To edit a Turn action, type:");
                player.sendMessage(ChatColor.YELLOW + "/rb a origin" + ChatColor.GREEN + " to edit the 'origin' location (hold shift and move)");
                player.sendMessage(ChatColor.YELLOW + "/rb a angle [angle]" + ChatColor.GREEN + " to edit the angle (max 180, min -180)");
                break;
            }
        }
        player.sendMessage(ChatColor.GREEN + "Type " + ChatColor.YELLOW + "/rb a complete" + ChatColor.GREEN +
                " when you've finished editing!");
    }

    public List<RideAction> getActions() {
        return new ArrayList<>(actions);
    }

    public void save() throws IOException {
        File file = new File("plugins/RideManager/rides/" + fileName + ".ride");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
        bw.write("Name " + name);
        bw.newLine();
        for (RideAction a : getActions()) {
            bw.write(a.toString());
            bw.newLine();
        }
        if (currentAction != null) {
            bw.write(currentAction.toString());
            bw.newLine();
        }
        bw.close();
    }

    public void updateBossBar() {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (player == null) return;
        double roundedLockY = (double) ((int) (getLockY() * 100)) / 100.0;
        ChatColor path = isPath() ? ChatColor.GREEN : ChatColor.RED;
        ChatColor locky = roundedLockY != 0 ? ChatColor.GREEN : ChatColor.RED;
        ChatColor edity = isChangeY() ? ChatColor.GREEN : ChatColor.RED;
        ChatColor stands = isShowArmorStands() ? ChatColor.GREEN : ChatColor.RED;
        player.getBossBar().setEverything(ChatColor.AQUA + "Ride " + name + " " + path + "Path " + locky +
                "LockY: " + roundedLockY + edity + " Edit Y" + stands + " Stands", 1, BarColor.BLUE, BarStyle.SOLID);
    }

    public void removeBossBar() {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (player == null) return;
        player.getBossBar().remove();
    }
}
