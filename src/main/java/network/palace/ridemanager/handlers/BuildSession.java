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
import network.palace.ridemanager.handlers.builder.SensorType;
import network.palace.ridemanager.handlers.builder.actions.*;
import network.palace.ridemanager.handlers.ride.ModelMap;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.utils.MovementUtil;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.*;
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
    @Getter private final World world;
    @Getter @Setter private String name;
    @Getter @Setter private String fileName;
    private List<RideAction> actions = new ArrayList<>();
    private List<RideSensor> sensors = new ArrayList<>();
    @Getter @Setter private Location spawn;
    @Getter @Setter private double speed;
    @Getter private boolean loading = false;
    @Getter @Setter private double lockY = 0;
    @Getter @Setter private boolean showArmorStands = false;
    @Getter @Setter private boolean path = true;
    @Getter @Setter private boolean changeY;
    @Getter @Setter private boolean sneaking;
    @Getter @Setter private boolean displayVehicle = false;
    @Getter @Setter private UUID displayVehicleUUID = null;
    @Getter HashMap<Location, ArmorStand> stands = new HashMap<>();
    private RideBuilderUtil.ConfirmCallback confirm = null;
    @Getter @Setter private Location currentLocation = null;
    @Getter @Setter private List<RideAction> possibleActions = null;
    @Getter @Setter private RideAction editAction = null;
    @Getter @Setter private boolean editingLocation = false;
    @Getter @Setter private int editLocation = 0;

    /**
     * Load actions from a file save
     *
     * @param file the file
     */
    public void load(File file) {
        loading = true;
        fileName = file.getName();
        Core.runTaskAsynchronously(RideManager.getInstance(), new FileRideLoader(world, null, file, (name, actionList, sensorList, spawn, speed, setYaw) -> {
            setName(name);
            actions = RideManager.getRideBuilderUtil().getFakeActions(actionList);
//            sensors = RideManager.getRideBuilderUtil().getFakeSensors(sensorList);
            setSpawn(spawn);
            setSpeed(speed);
            loading = false;
            Core.getPlayerManager().getPlayer(uuid).sendMessage(ChatColor.GREEN + "Your Build Session has loaded!");
            RideManager.getRideBuilderUtil().setInventory(uuid, true);
            updateBossBar();
        }, true));
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
                if (editAction instanceof FakeAction) {
                    if (((FakeAction) editAction).areFieldsIncomplete()) {
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
        return true;
    }

    /**
     * Open an inventory containing the different action types
     *
     * @param player the player
     */
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

    /**
     * Open an inventory containing the different sensor types
     *
     * @param player the player
     */
    private void openSensorMenu(CPlayer player) {
        Inventory inv = Bukkit.createInventory(player.getBukkitPlayer(), 27, ChatColor.GREEN + "New Sensor");
        int i = 0;
        for (SensorType s : SensorType.values()) {
            inv.setItem(i++, s.getItem());
            if (i >= 27) {
                break;
            }
        }
        player.openInventory(inv);
    }

    /**
     * Process a click on a ride builder inventory
     *
     * @param event  the click event
     * @param player the player clicking
     * @param name   the name of the clicked inventory
     */
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
                    if (item.equals(actionItem)) {
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
                if (editAction != null) {
                    if (editAction instanceof FakeAction) {
                        if (((FakeAction) editAction).areFieldsIncomplete()) {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "There are some unfinished values in the previous action!");
                            return;
                        }
                        actions.add(editAction);
                        if (editAction instanceof FakeSpawnAction) {
                            spawn = ((FakeSpawnAction) editAction).getLoc();
                            speed = ((FakeSpawnAction) editAction).getSpeed();
                        }
                        editAction = null;
                    }
                }
                currentLocation.add(0.5, 0, 0.5);
                editAction = action.newAction(currentLocation, this);
                player.sendMessage(ChatColor.GREEN + "Created a new " + editAction.getActionType().getColoredName() + " action.");
                player.closeInventory();
                break;
            }
        }
    }

    /**
     * Process an interact event for the ride builder
     *
     * @param event  the interact event
     * @param player the player who called the event
     */
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
                    Location origin = ac.getTo();
                    float angle = 0;

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

    /**
     * Open an inventory containing a list of possible actions the player then has to choose from
     *
     * @param player          the player
     * @param possibleActions the list of possible actions
     */
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
            meta.setLore(Collections.singletonList(action.toString()));
            item.setItemMeta(meta);
            inv.setItem(i++, item);
        }
        this.possibleActions = possibleActions;
        player.openInventory(inv);
    }

    /**
     * Set the action a player is editing
     *
     * @param player the player
     * @param action the action being edited
     */
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
            if (((FakeAction) editAction).areFieldsIncomplete()) {
                player.sendMessage(ChatColor.RED + "There are some incomplete fields in the current " + editAction.getActionType() + " action.");
                return;
            } else {
                player.sendMessage(ChatColor.RED + "You are no longer editing this " + editAction.getActionType() + " action.");
                editingLocation = false;
                editLocation = 0;
                if (editAction instanceof FakeSpawnAction) {
                    spawn = ((FakeSpawnAction) editAction).getLoc();
                    speed = ((FakeSpawnAction) editAction).getSpeed();
                }
                actions.add(editAction);
                editAction = null;
                return;
            }
        }
        switch (editAction.getActionType()) {
            case TURN: {
                if (args.length < 1) {
                    editHelp(player, editAction);
                    break;
                }
                switch (args[0].toLowerCase()) {
                    case "to": {
                        if (editingLocation && editLocation != 0) {
                            editLocation = 0;
                            player.sendMessage(ChatColor.GREEN + "Now editing the 'to' location.");
                            break;
                        }

                        editingLocation = !editingLocation;
                        if (editingLocation) {
                            player.sendMessage(ChatColor.GREEN + "Toggled on location editing. Hold shift and move to edit.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Toggled off location editing.");
                        }
                        editLocation = 0;
                        break;
                    }
                    case "p0": {
                        if (editingLocation && editLocation != 1) {
                            editLocation = 1;
                            player.sendMessage(ChatColor.GREEN + "Now editing the 'p0' location.");
                            break;
                        }

                        editingLocation = !editingLocation;
                        if (editingLocation) {
                            player.sendMessage(ChatColor.GREEN + "Toggled on location editing. Hold shift and move to edit.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Toggled off location editing.");
                        }
                        editLocation = 1;
                        break;
                    }
                }
                break;
            }
            case WAIT: {
                if (args.length < 1) {
                    editHelp(player, editAction);
                    break;
                }
                long time;
                try {
                    time = Long.parseLong(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + args[0] + " is not a number!");
                    break;
                }
                ((FakeWaitAction) editAction).setTicks(time);
                player.sendMessage(ChatColor.GREEN + "Set delay to " + time + " ticks.");
                break;
            }
            case SPAWN: {
                if (args.length < 2) {
                    editHelp(player, editAction);
                    break;
                }
                switch (args[0].toLowerCase()) {
                    case "speed": {
                        double speed;
                        try {
                            speed = Double.parseDouble(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + args[1] + " is not a number!");
                            break;
                        }
                        ((FakeSpawnAction) editAction).setSpeed(speed);
                        player.sendMessage(ChatColor.GREEN + "Set spawn speed to " + speed + ".");
                        break;
                    }
                    case "yaw": {
                        float yaw;
                        try {
                            yaw = Float.parseFloat(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(args[1] + " is not a number!");
                            break;
                        }
                        ((FakeSpawnAction) editAction).setYaw(yaw);
                        player.sendMessage(ChatColor.GREEN + "Set spawn yaw to " + yaw + ".");
                        break;
                    }
                }
                break;
            }
            case STRAIGHT: {
                if (args.length < 1) {
                    editHelp(player, editAction);
                    break;
                }
                switch (args[0].toLowerCase()) {
                    case "to": {
                        editingLocation = !editingLocation;
                        if (editingLocation) {
                            player.sendMessage(ChatColor.GREEN + "Toggled on location editing. Hold shift and move to edit.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Toggled off location editing.");
                        }
                        editLocation = 0;
                        break;
                    }
                    case "autoyaw": {
                        if (args.length < 2) {
                            editHelp(player, editAction);
                            break;
                        }
                        boolean b = Boolean.parseBoolean(args[1]);
                        player.sendMessage(ChatColor.GREEN + "Set auto yaw value to " + (b ? "true" : ChatColor.RED + "false") + ".");
                        ((FakeStraightAction) editAction).setAutoYaw(Boolean.toString(b));
                        break;
                    }
                }
                break;
            }
            case EXIT: {
                if (args.length < 1) {
                    editHelp(player, editAction);
                    break;
                }
                switch (args[0].toLowerCase()) {
                    case "to": {
                        editingLocation = !editingLocation;
                        if (editingLocation) {
                            player.sendMessage(ChatColor.GREEN + "Toggled on location editing. Hold shift and move to edit.");
                        } else {
                            player.sendMessage(ChatColor.RED + "Toggled off location editing.");
                        }
                        editLocation = 0;
                        break;
                    }
                    case "autoyaw": {
                        if (args.length < 2) {
                            editHelp(player, editAction);
                            break;
                        }
                        boolean b = Boolean.parseBoolean(args[1]);
                        player.sendMessage(ChatColor.GREEN + "Set auto yaw value to " + (b ? "true" : ChatColor.RED + "false") + ".");
                        ((FakeExitAction) editAction).setAutoYaw(Boolean.toString(b));
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * Send help messages based on the type of action being edited
     *
     * @param player the player
     * @param action the action
     */
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
                player.sendMessage(ChatColor.YELLOW + "/rb a speed [speed]" + ChatColor.GREEN + " to edit the speed the vehicle starts moving at");
                player.sendMessage(ChatColor.YELLOW + "/rb a yaw [yaw]" + ChatColor.GREEN + " to edit the yaw the vehicle spawns at");
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
                player.sendMessage(ChatColor.YELLOW + "/rb a to" + ChatColor.GREEN + " to edit the 'to' location (hold shift and move)");
                player.sendMessage(ChatColor.YELLOW + "/rb a p0" + ChatColor.GREEN + " to edit the 'p0' location (hold shift and move)");
                break;
            }
        }
        player.sendMessage(ChatColor.GREEN + "Type " + ChatColor.YELLOW + "/rb a complete" + ChatColor.GREEN +
                " when you've finished editing.");
    }

    /**
     * Get a list of all actions in the BuildSession, excluding the action currently being edited
     *
     * @return an ArrayList of all actions
     */
    public List<RideAction> getActions() {
        return new ArrayList<>(actions);
    }

    /**
     * Save all data to the ride's file
     *
     * @throws IOException if there's an error writing to the file
     */
    public void save() throws IOException {
        File file = new File("plugins/RideManager/rides/" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
        bw.write("Name " + name);
        bw.newLine();
        for (RideAction a : getActions()) {
            try {
                bw.write(a.toString());
                bw.newLine();
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Error with " + a.getActionType() + " action: " + e.getMessage());
            }
        }
        if (editAction != null) {
            try {
                bw.write(editAction.toString());
                bw.newLine();
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Error with " + editAction.getActionType() + " action: " + e.getMessage());
            }
        }
        bw.close();
    }

    /**
     * Update the boss bar which contains a lot of ride builder info
     */
    public void updateBossBar() {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (player == null) return;
        double roundedLockY = (double) ((int) (getLockY() * 100)) / 100.0;
        ChatColor path = isPath() ? ChatColor.GREEN : ChatColor.RED;
        ChatColor locky = roundedLockY != 0 ? ChatColor.GREEN : ChatColor.RED;
        ChatColor edity = isChangeY() ? ChatColor.GREEN : ChatColor.RED;
        ChatColor stands = isShowArmorStands() ? ChatColor.GREEN : ChatColor.RED;
        ChatColor display = isDisplayVehicle() ? ChatColor.GREEN : ChatColor.RED;
        player.getBossBar().setEverything(ChatColor.AQUA + "Ride " + name + " " + path + "Path " + locky +
                        "LockY: " + roundedLockY + edity + " Edit Y" + stands + " Stands" + display + " Display",
                1, BarColor.BLUE, BarStyle.SOLID);
    }

    /**
     * Remove the player's boss bar
     */
    public void removeBossBar() {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (player == null) return;
        player.getBossBar().remove();
    }

    /**
     * Spawn or despawn a display vehicle at the station of the ride
     *
     * @return true if spawned, false if despawned
     */
    public boolean toggleDisplayVehicle() {
        displayVehicle = !displayVehicle;
        if (!displayVehicle && displayVehicleUUID != null) {
            Optional<ArmorStand> opt = spawn.getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(a -> a.getUniqueId().equals(displayVehicleUUID)).findFirst();
            opt.ifPresent(a -> {
                a.remove();
                displayVehicleUUID = null;
            });
        } else if (displayVehicle) {
            ArmorStand stand = spawn.getWorld().spawn(spawn.clone().add(0, -MovementUtil.armorStandHeight, 0), ArmorStand.class);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setArms(false);
            stand.setBasePlate(false);
            displayVehicleUUID = stand.getUniqueId();
            int index = fileName.indexOf(".ride");
            String mapName = "";
            if (index != -1) {
                mapName = fileName.substring(0, index);
            }
            ModelMap map = RideManager.getMappingUtil().getMap(mapName, spawn.getWorld());
            if (map != null && map.getItem() != null) {
                stand.setHelmet(map.getItem());
            } else {
                stand.remove();
                displayVehicle = false;
                displayVehicleUUID = null;
            }
        }
        return displayVehicle;
    }

    /**
     * Get the last location in the current ride path
     *
     * @return the last location, or the spawn location if there's an error getting the last location
     */
    public Location getLastLocation() {
        Location last = RideManager.getRideBuilderUtil().getPathDataTimer().runTimer(this, false);
        if (last == null) {
            return spawn;
        } else {
            return last;
        }
    }
}
