package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.ChatColor;

@CommandMeta(description = "Toggle showing helpful armor stands for a ride")
public class StandCommand extends CoreCommand {

    public StandCommand() {
        super("stand");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        session.setShowArmorStands(!session.isShowArmorStands());
        if (session.isShowArmorStands()) {
            player.sendMessage(ChatColor.GREEN + "You can now see helpful armor stands!");
        } else {
            player.sendMessage(ChatColor.RED + "You can no longer see helpful armor stands!");
        }
        session.updateBossBar();
    }
}
