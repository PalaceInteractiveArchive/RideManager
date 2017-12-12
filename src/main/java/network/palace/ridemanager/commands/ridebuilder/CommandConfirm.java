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
public class CommandConfirm extends CoreCommand {

    public CommandConfirm() {
        super("confirm");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        session.confirm();
    }
}
