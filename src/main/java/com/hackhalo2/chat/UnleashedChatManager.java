package com.hackhalo2.chat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.massivecraft.factions.entity.UPlayer;

/**
 * @author t3hk0d3 (ChatManager)
 * @author rymate (bChatManager)
 * @author hackhalo2 (UnleashedChatManager)
 */
public class UnleashedChatManager extends JavaPlugin {

	//The Logger
	public static Logger log = null;
	
	//The Vault Variables
	private Chat chat = null;
	private Permission permission = null;

	//Internal ChatListener references
	private ChatListener listener = null;
	
	//The CommandExecutor
	private UnleashedCommandExecutor uce;

	//The config.yml file
	public YamlConfiguration config = null;
	
	//The boolean flag to check to see if Factions is enabled
	public boolean factionsEnabled = false;
	
	//The boolean flag to check to see if we are forcefully overriding the /me command
	private boolean forceMeCommand = false;

	@Override
	public void onEnable() {
		//Setup the Logger
		log = this.getLogger();
		
		//Initialize the Command Executor
		this.uce = new UnleashedCommandExecutor(this);

		//Setup the config
		this.setupConfig();

		//Chatlistener - can you hear me?
		this.listener = new ChatListener(this);
		this.getServer().getPluginManager().registerEvents(this.listener, this);

		//Vault hook checks
		if(!this.setupChat() || !this.setupPermissions()) {
			log.severe("Errors in setting up Vault variables! Disabling...");
			this.getServer().getPluginManager().disablePlugin(this);
		}
		
		//Factions Check
		Plugin factions = null;
		if((factions = this.getServer().getPluginManager().getPlugin("Factions")) != null) {
			if(this.config.getBoolean("toggles.factions-support")) {
				log.info("Factions "+factions.getDescription().getVersion()+" found, enabling support...");
				this.factionsEnabled = true;
			} else {
				log.info("Factions "+factions.getDescription().getVersion()+" found, but support not enabled.");
			}
		}
		
		//Set up the commands
		if(this.config.getBoolean("toggles.control-me")) {
			if(this.getCommand("me").isRegistered()) {
				log.info("Command 'me' is registered to "+this.getCommand("me").getPlugin().getName()+", overriding...");
				//Reflection stuffs!
				PluginCommand pc = this.getCommand("me");
				Class<?> clazz = pc.getClass();
				try {
					//XXX: This is a damn dirty hack, but I want control of /me
					Field executor = clazz.getDeclaredField("executor"); //The executor field
					Field owningPlugin = clazz.getDeclaredField("owningPlugin"); //The owningPlugin field
					
					//Set the private access to public
					executor.setAccessible(true);
					owningPlugin.setAccessible(true);
					
					//Set the fields to point to us
					executor.set(pc, this);
					owningPlugin.set(pc, this);
					
					//Reset the private access
					executor.setAccessible(false);
					owningPlugin.setAccessible(false);
					
					//Test to see if the Reflection worked
					log.info("Command 'me' is now registered to "+this.getCommand("me").getPlugin().getName()+" executor '"+this.getCommand("me").getExecutor().toString()+"'");
				} catch (Exception e) {
					log.warning("Unable to override plugin registration! Falling back to forceful override...");
					this.forceMeCommand = true;
				}
			} else {
				this.getCommand("me").setExecutor(this.uce);
			}
		}

		log.info("Enabled Successfully!");
	}

	private void setupConfig() {
		File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
		
		try {
			if (!configFile.exists()) {
				this.saveDefaultConfig();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, null, ex);
		}
		
		this.config = new YamlConfiguration();
		this.config = YamlConfiguration.loadConfiguration(configFile);
	}

	/*
	 * Code to setup the Chat variable in Vault. Allows me to hook to all the prefix plugins.
	 */
	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		
		if (chatProvider != null) {
			this.chat = chatProvider.getProvider();
		}
		
		log.info("Chat Provider is: "+chatProvider.getProvider().getName());

		return (this.chat != null);
	}

	/*
	 * Code to setup the Permission variable in Vault. Allows me to hook into all supported permissions plugins.
	 */
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permsProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		
		if(permsProvider != null) {
			this.permission = permsProvider.getProvider();
		}
		
		log.info("Permissions Provider is: "+permsProvider.getProvider().getName());

		return (this.permission != null);
	}

	//
	//  Begin methods from Functions.java
	//
	public String replacePlayerPlaceholders(Player player, String format) {
		return format.replace("%prefix", this.getPlayerPrefix(player))
				.replace("%suffix", this.getPlayerSuffix(player))
				.replace("%world", player.getWorld().getName())
				.replace("%player", player.getName())
				.replace("%displayname", player.getDisplayName())
				.replace("%group", this.chat.getPrimaryGroup(player))
				.replace("%faction", this.getPlayerFaction(player));
	}
	
	private String getPlayerFaction(Player player) {
		String factionTag = "";
		
		if(this.factionsEnabled) {
			 UPlayer uPlayer = UPlayer.get(player); //Get the Faction Player reference
			 factionTag = uPlayer.getFactionName()+""+uPlayer.getRole().getPrefix();
		}
		
		return factionTag;
	}

	private String getPlayerPrefix(Player player) {
		String prefix = this.chat.getPlayerPrefix(player);
		
		if(prefix == null || prefix.equals("") || prefix.isEmpty()) {
			String group = this.permission.getPrimaryGroup(player);
			prefix = this.chat.getGroupPrefix(player.getWorld().getName(), group);
			
			if(prefix == null || prefix.equals("") || prefix.isEmpty()) {
				prefix = "";
			}
		}

		return prefix;
	}

	private String getPlayerSuffix(Player player){
		String suffix = this.chat.getPlayerPrefix(player);
		
		if(suffix == null || suffix.equals("")  || suffix.isEmpty()){
			String group = permission.getPrimaryGroup(player);
			suffix = this.chat.getGroupPrefix(player.getWorld().getName(),group);
			
			if(suffix == null || suffix.equals("")  || suffix.isEmpty()){
				suffix = "";
			}
		}
		return suffix;
	}

	public String colorize(String string) {
		if (string == null) return "";
		else return string.replaceAll("&([a-z0-9])", "\u00A7$1");
	}

	public List<Player> getLocalRecipients(Player sender, String message, double range) {
		Location playerLocation = sender.getLocation();
		List<Player> recipients = new LinkedList<Player>();
		double squaredDistance = Math.pow(range, 2);
		
		for (Player recipient : getServer().getOnlinePlayers()) {
			
			// Recipient are not from same world or in range
			if (!recipient.getWorld().equals(sender.getWorld()) ||
					(playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance)) continue;
			
			recipients.add(recipient);
		}
		return recipients;
	}

	public List<Player> getSpies() {
		List<Player> recipients = new LinkedList<Player>();
		for (Player recipient : this.getServer().getOnlinePlayers()) {
			if (recipient.hasPermission("umc.spy")) {
				recipients.add(recipient);
			}
		}
		return recipients;
	}
}