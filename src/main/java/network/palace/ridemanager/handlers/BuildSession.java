package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.threads.FileRideLoader;
import network.palace.ridemanager.threads.RideCallback;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;

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
    @Getter private List<RideAction> actions = new ArrayList<>();
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

    /**
     * Load actions from a file save
     *
     * @param file the file
     */
    public void load(File file) {
        loading = true;
        fileName = file.getName();
        Bukkit.getScheduler().runTaskAsynchronously(RideManager.getInstance(), new FileRideLoader(null, file, new RideCallback() {
            @Override
            public void done(String name, LinkedList<RideAction> list, Location spawn, double speed, boolean setYaw) {
                setName(name);
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

    public void setConfirm(CPlayer player, RideBuilderUtil.ConfirmCallback confirm) {
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
    public boolean placeBlock(CPlayer player, Block block) {
        RideBuilderUtil.BlockAction a = RideBuilderUtil.BlockAction.fromBlock(block);
        if (a == null) {
            return false;
        }
        if (currentAction != null) {
            //TODO do something
        }
        currentAction = a.newAction();
        String msg = ChatColor.GREEN + "You created a " + ChatColor.YELLOW;
        switch (a) {
            case SPAWN:
                msg += "Spawn";
                break;
            case STRAIGHT:
                msg += "Straight";
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
        player.sendMessage(msg);
        return true;
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
        bw.close();
    }
}
