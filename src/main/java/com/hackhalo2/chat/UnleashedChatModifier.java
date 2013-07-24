package com.hackhalo2.chat;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class UnleashedChatModifier implements CommandExecutor, Listener {

	private final UnleashedChatManager plugin;

	//General Chat message formats
	private String meFormat, messageFormat, localMessageFormat, opMessageFormat, personalMessageFormat;

	//Toggles
	public boolean toggleControlMe, toggleRangedMode, toggleSpecialFeatures, toggleFactionsSupport, toggleModAsOp;

	//Other things
	private double chatRange;

	//The YAML configuration
	private YamlConfiguration config = new YamlConfiguration();
	private final File configFile;

	public UnleashedChatModifier(UnleashedChatManager plugin) {
		this.plugin = plugin; //Set the plugin reference

		//load the config
		this.configFile = new File(this.plugin.getDataFolder()+File.separator+"config.yml");
		this.loadConfig();

		/* Cache the yml options */
		//Strings
		this.meFormat = this.config.getString("formats.me-format", "* %player %message");
		this.messageFormat = this.config.getString("formats.message-format", "%prefix %player: &f%message");
		this.localMessageFormat = this.config.getString("formats.local-message-format", "[LOCAL] %prefix %player: &f%message");
		this.opMessageFormat = this.config.getString("formats.op-message-format", "[OP] %prefix %player: &f%message");
		this.personalMessageFormat = this.config.getString("formats.personal-message-format", "[MSG] [%player -> %reciever] &f%message");

		//Booleans
		this.toggleControlMe = this.config.getBoolean("toggles.control-me", true);
		this.toggleRangedMode = this.config.getBoolean("toggles.range-mode", false);
		this.toggleSpecialFeatures = this.config.getBoolean("toggles.special-features", true);
		this.toggleFactionsSupport = this.config.getBoolean("toggles.factions-support", true);
		this.toggleModAsOp = this.config.getBoolean("toggles.mod-as-op", true);

		//Other
		this.chatRange = this.config.getDouble("other.chat-range", 100D);
	}

	private void loadConfig() {
		UnleashedChatManager.log.info("Loading Yaml Configuration...");
		try {
			if (!this.configFile.exists()) {
				this.plugin.saveDefaultConfig();
			}
		} catch (Exception e) {
			UnleashedChatManager.log.log(Level.SEVERE, null, e);
		}

		this.config = YamlConfiguration.loadConfiguration(this.configFile);
	}

	private void saveConfig() {
		UnleashedChatManager.log.info("Saving Yaml Configuration...");

		//Set the values for the yaml file
		try {
			//File Version
			this.config.set("version.major", 1);
			this.config.set("version.minor", 0);

			//Strings
			this.config.set("formats.me-format", this.meFormat);
			this.config.set("formats.message-format", this.messageFormat);
			this.config.set("formats.local-message-format", this.localMessageFormat);
			this.config.set("formats.op-message-format", this.opMessageFormat);
			this.config.set("formats.personal-message-format", this.personalMessageFormat);

			//Booleans
			this.config.set("toggles.control-me", this.toggleControlMe);
			this.config.set("toggles.range-mode", this.toggleRangedMode);
			this.config.set("toggles.special-features", this.toggleSpecialFeatures);
			this.config.set("toggles.factions-support", this.toggleFactionsSupport);
			this.config.set("toggles.mod-as-op", this.toggleModAsOp);

			//Other
			this.config.set("other.chat-range", this.chatRange);
			
			//Finally save the damned config
			this.config.save(this.configFile);
		} catch (Exception e) {
			UnleashedChatManager.log.log(Level.SEVERE, null, e);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("me") && (this.toggleControlMe)) {

		}
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();

		String message = this.messageFormat;
		String chatMessage = event.getMessage();

		if (this.toggleRangedMode) {
			message = this.localMessageFormat;
			event.getRecipients().clear();
			event.getRecipients().addAll(this.plugin.getLocalRecipients(player, message, this.chatRange));
			event.getRecipients().addAll(this.plugin.getSpies());
		}

		if (this.toggleSpecialFeatures) {
			if (chatMessage.startsWith("@") && player.hasPermission("bchatmanager.chat.message")) {
				chatMessage = chatMessage.substring(1);
				String[] messageSplit = chatMessage.split(" ");
				Player reciever = this.plugin.getServer().getPlayer(messageSplit[0]);
				if (messageSplit[0] == "ops") {
					chatMessage = chatMessage.replaceFirst(messageSplit[0], "");
					chatMessage = chatMessage.replaceAll("%reciever", messageSplit[0]);

					List<Player> recipients = new LinkedList<Player>();
					event.getRecipients().clear();
					event.getRecipients().add(player);

					for (Player recipient : this.plugin.getServer().getOnlinePlayers()) {
						if (recipient.isOp()) {
							recipients.add(recipient);
						}
					}

					event.getRecipients().addAll(recipients);
					message = this.personalMessageFormat;
				} else if (reciever == null) {
					player.sendMessage("This player isn't online or you just typed the @ symbol! Ignoring.");
					event.setCancelled(true);
				} else {
					chatMessage = chatMessage.replaceFirst(messageSplit[0], "");
					message = this.personalMessageFormat;
					message = message.replaceAll("%reciever", messageSplit[0]);
					event.getRecipients().clear();
					event.getRecipients().add(player);
					event.getRecipients().add(reciever);
					event.getRecipients().addAll(this.plugin.getSpies());
					message = this.personalMessageFormat;
				}
			}
		}

		message = this.plugin.replacePlayerPlaceholders(player, message);
		message = this.plugin.colorize(message);

		if (player.hasPermission("bchatmanager.color")) {
			chatMessage = this.plugin.colorize(chatMessage);
		}

		message = message.replace("%message", chatMessage);

		event.setFormat(message);
		event.setMessage(chatMessage);

	}
}
