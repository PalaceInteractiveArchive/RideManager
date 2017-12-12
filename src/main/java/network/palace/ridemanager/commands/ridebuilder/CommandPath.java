package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
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
public class CommandPath extends CoreCommand {

    public CommandPath() {
        super("path");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        session.setPath(!session.isPath());
        if (session.isPath()) {
            player.sendMessage(ChatColor.GREEN + "You have enabled the ride path!");
        } else {
            player.sendMessage(ChatColor.RED + "You have disabled the ride path!");
        }
    }
}
