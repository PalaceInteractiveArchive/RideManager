package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.ChatColor;

/**
 * @author Marc
 * @since 8/10/17
 */
@CommandMeta(description = "Exit the ride builder")
public class ExitCommand extends CoreCommand {

    public ExitCommand() {
        super("exit");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        if (session.hasConfirm()) {
            player.sendMessage(ChatColor.RED + "You must confirm this action, but there's another command you have to confirm first.");
            return;
        }
        player.sendMessage(ChatColor.RED + "Exiting will lose any progress since your last save. Are you sure?");
        session.setConfirm(player, uuid -> {
            player.sendMessage(ChatColor.GREEN + "Exiting session...");
            rideBuilderUtil.setInventory(player.getUniqueId(), false);
            rideBuilderUtil.removeSession(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You've successfully exited your build session!");
        });
    }
}
