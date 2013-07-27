package com.hackhalo2.chat;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
	
	//Internal toggles
	public boolean skipMeCommand = false;

	//Other things
	private double chatRange;

	//The YAML configuration
	private YamlConfiguration config = new YamlConfiguration();
	private final File configFile;
	private boolean modified = false;

	public UnleashedChatModifier(UnleashedChatManager plugin) {
		this.plugin = plugin; //Set the plugin reference

		//load the config
		this.configFile = new File(this.plugin.getDataFolder()+File.separator+"config.yml");
		this.loadConfig();
	}
	
	public boolean wasModified() {
		return this.modified;
	}

	private void loadConfig() {
		this.modified = false; //make sure to reset this before loading the config from disk
		UnleashedChatManager.log.info("Loading Yaml Configuration...");
		try {
			if (!this.configFile.exists()) {
				this.plugin.saveDefaultConfig();
			}
		} catch (Exception e) {
			UnleashedChatManager.log.log(Level.SEVERE, null, e);
		}

		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		
		/* Cache the yml options */
		//Strings
		this.meFormat = this.config.getString("formats.me-format", "* %player %message");
		this.messageFormat = this.config.getString("formats.message-format", "%prefix %player: &f%message");
		this.localMessageFormat = this.config.getString("formats.local-message-format", "[LOCAL] %prefix %player: &f%message");
		this.opMessageFormat = this.config.getString("formats.op-message-format", "[OP] %prefix %player: &f%message");
		this.personalMessageFormat = this.config.getString("formats.personal-message-format", "[MSG] [%player -> %reciever] &f%message");

		//Booleans
		this.toggleControlMe = this.config.getBoolean("toggles.control-me", true);
		this.toggleRangedMode = this.config.getBoolean("toggles.ranged-mode", false);
		this.toggleSpecialFeatures = this.config.getBoolean("toggles.special-features", true);
		this.toggleFactionsSupport = this.config.getBoolean("toggles.factions-support", false);
		this.toggleModAsOp = this.config.getBoolean("toggles.mod-as-op", true);

		//Other
		this.chatRange = this.config.getDouble("other.chat-range", 100D);
	}

	public void saveConfig() {
		UnleashedChatManager.log.info("Saving Yaml Configuration...");

		//Set the values for the yaml file
		try {
			
			//Info header
			this.config.set("", "#Valid Format Tags: %prefix %player %displayname %message %reciever %faction");
			
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
			this.config.set("toggles.ranged-mode", this.toggleRangedMode);
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
	
	private String colorize(String string) {
		String newString = string; //make a copy of the string so we can preserve the original
		if (newString == null) return "";
		else return newString.replaceAll("&([a-z0-9])", "\u00A7$1");
	}
	
	private String stripColors(String string) {
		String newString = string; //make a copy of the string so we can preserve the original
		if(newString == null) return "";
		else return newString.replaceAll("&([a-z0-9])", "");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Set up the most commonly used fields
		Player player;
		
		if(command.getName().equalsIgnoreCase("me") && (this.toggleControlMe) && !this.skipMeCommand) {
			if(!(sender instanceof Player)) {
				//Something other than a player used this command, don't do anything
				return true;
			}
			
			player = ((Player)sender); //get the player out of the CommandSender
			
			if(!this.plugin.permission.has(player, "ucm.command.me")) {
				player.sendMessage(ChatColor.RED+"You don't have the permission to perform this command");
				return true;
			}
			
			//Length check
			if(args.length < 1) {
				player.sendMessage(ChatColor.GRAY+"You need to type something after it :P (/me <emote>)");
				return true;
			}
			
			StringBuilder me = new StringBuilder();
			
			//Rebuild the String from the args
			for(int i = 0; i < args.length; i++) {
				me.append(args[i]);
				me.append(" ");
			}
			
			String meMessage = "";
			String message = this.colorize(this.meFormat);
			
			if(this.plugin.permission.has(player, "ucm.chat.color")) meMessage = this.colorize(me.toString());
			else meMessage = this.stripColors(me.toString());
			
			message = message.replace("%message", meMessage).replace("%displayname", "%1$s");
			message = ParserLib.replacePlayerPlaceholders(player, message);
			
			if (this.toggleRangedMode) {
				List<Player> pl = this.plugin.getLocalRecipients(player, message, this.chatRange);
				for (int j = 0; j < pl.size(); j++) {
					pl.get(j).sendMessage(message);
				}
				sender.sendMessage(message);
				UnleashedChatManager.log.info(message);
			} else {
				Bukkit.getServer().broadcastMessage(message);
			}
			return true;
		}
		
		if(command.getName().equalsIgnoreCase("ucm")) { //The main command
			if(args[0].equalsIgnoreCase("reload") && this.plugin.permission.has(sender, "ucm.command.reload")) { //ucm reload
				this.loadConfig();
				sender.sendMessage(ChatColor.AQUA+"UCM Config Reloaded");
				return true;
			}
			
		}
		
		return true; //We should never get here, but just in case we do...
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		UnleashedChatManager.log.log(Level.FINE, "OnPlayerChat called!");
		if (event.isCancelled()) return;
		UnleashedChatManager.log.log(Level.FINE, "OnPlayerChat was not cancelled");

		Player player = event.getPlayer();

		String message;
		String chatMessage = event.getMessage();

		if (this.toggleRangedMode) {
			message = this.localMessageFormat;
			event.getRecipients().clear();
			event.getRecipients().addAll(this.plugin.getLocalRecipients(player, message, this.chatRange));
			event.getRecipients().addAll(this.plugin.getSpies());
		} else message = this.messageFormat;

		if (this.toggleSpecialFeatures) {
			if (chatMessage.startsWith("@") && player.hasPermission("ucm.chat.atmessage")) {
				chatMessage = chatMessage.substring(1);
				String[] messageSplit = chatMessage.split(" ");
				Player reciever = this.plugin.getServer().getPlayer(messageSplit[0]);
				if (messageSplit[0].equalsIgnoreCase("ops") || messageSplit[0].equalsIgnoreCase("mods")) {
					message = this.personalMessageFormat;
					chatMessage = chatMessage.replaceFirst(messageSplit[0], "");
					chatMessage = chatMessage.replaceAll("%reciever", messageSplit[0]);

					List<Player> recipients = new LinkedList<Player>();
					event.getRecipients().clear();
					event.getRecipients().add(player);
					
					for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
						if (recipient.isOp() || this.plugin.permission.has(player, "ucm.chat.mod")) {
							recipients.add(recipient);
						}
					}

					event.getRecipients().addAll(recipients);
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
				}
			}
		}

		message = ParserLib.replacePlayerPlaceholders(player, message);
		message = this.colorize(message);

		if (this.plugin.permission.has(player, "ucm.chat.color")) chatMessage = this.colorize(chatMessage);
		else this.stripColors(chatMessage);
		
		UnleashedChatManager.log.log(Level.FINE, "Chat Format is : "+message);
		message = message.replace("%message", chatMessage);

		event.setFormat(message);
		event.setMessage(chatMessage);

	}
}
