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
@CommandMeta(description = "Toggle editing y-value of ride")
public class YCommand extends CoreCommand {

    public YCommand() {
        super("y");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        session.setChangeY(!session.isChangeY());
        if (session.isChangeY()) {
            player.sendMessage(ChatColor.GREEN + "You're now editing the y position!");
        } else {
            player.sendMessage(ChatColor.RED + "You're no longer editing the y position!");
        }
        session.updateBossBar();
    }
}
