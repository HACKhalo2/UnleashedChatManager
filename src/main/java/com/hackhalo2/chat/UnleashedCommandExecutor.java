package com.hackhalo2.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnleashedCommandExecutor implements CommandExecutor {
	
	private final UnleashedChatManager plugin;
	
	public UnleashedCommandExecutor(UnleashedChatManager plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/*if ((command.getName().equals("me")) && (config.getBoolean("toggles.control-me", true))) {
		String meFormat = config.getString("formats.me-format", "* %player %message");
		Double chatRange = config.getDouble("other.chat-range", 100);
		boolean rangedMode = config.getBoolean("toggles.ranged-mode", false);
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Ya need to type something after it :P");
			return false;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You are not an in-game player!");
			return true;
		}
		Player player = (Player) sender;
		int i;
		StringBuilder me = new StringBuilder();
		for (i = 0; i < args.length; i++) {
			me.append(args[i]);
			me.append(" ");
		}
		String meMessage = me.toString();
		String message = meFormat;
		message = colorize(message);

		if (sender.hasPermission("bchatmanager.chat.color")) {
			meMessage = colorize(meMessage);
		}

		message = message.replace("%message", meMessage).replace("%displayname", "%1$s");
		message = this.replacePlayerPlaceholders(player, message);

		if (rangedMode) {
			List<Player> pl = this.getLocalRecipients(player, message, chatRange);
			for (int j = 0; j < pl.size(); j++) {
				pl.get(j).sendMessage(message);
			}
			sender.sendMessage(message);
			System.out.println(message);
		} else {
			this.getServer().broadcastMessage(message);
		}
		return true;
	}

	if ((command.getName().equals("ucm"))) {
		if (!(sender instanceof Player) || sender.hasPermission("bchatmanager.reload")) {
			getServer().getPluginManager().disablePlugin(this);
			getServer().getPluginManager().enablePlugin(this);
			sender.sendMessage(ChatColor.AQUA + "[bChatManager] Plugin reloaded!");
			return true;
		}

		if (sender.hasPermission("bchatmanager.reload")) {
			sender.sendMessage(ChatColor.AQUA + "[bChatManager] Wtf, you can't do this!");
			return true;
		}
	}*/
		return false;
	}

}
