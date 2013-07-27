package com.hackhalo2.chat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;

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
	public Permission permission = null;

	//The ChatModifier
	private UnleashedChatModifier ucm = null;

	//The boolean flag to check to see if Factions is enabled
	public static boolean factionsEnabled = false;

	@Override
	public void onEnable() {
		//Setup the Logger
		log = this.getLogger();

		//Vault hook checks
		if(!this.setupChat() || !this.setupPermissions()) {
			log.severe("Errors in setting up Vault variables! Disabling...");
			this.getServer().getPluginManager().disablePlugin(this);
		}

		//Setup the ParserLib
		ParserLib.setup(this.permission, this.chat);
		
		//Setup and register the ChatModifier
		this.ucm = new UnleashedChatModifier(this);
		this.getServer().getPluginManager().registerEvents(this.ucm, this);

		//Factions Check
		Plugin factions = null;
		if((factions = this.getServer().getPluginManager().getPlugin("Factions")) != null) {
			if(this.ucm.toggleFactionsSupport) {
				log.info("Factions "+factions.getDescription().getVersion()+" found, enabling support...");
				factionsEnabled = true;
			} else {
				log.info("Factions "+factions.getDescription().getVersion()+" found, but support not enabled.");
			}
		}

		//Set up the commands
		if(this.ucm.toggleControlMe) {
			if(this.getCommand("me").isRegistered() && this.getCommand("me").getPlugin() != this) {
				log.info("Command 'me' is registered to '"+this.getCommand("me").getPlugin().getName()+"', overriding...");
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
					log.info("Command 'me' is now registered to '"+this.getCommand("me").getPlugin().getName()+"', executor '"+this.getCommand("me").getExecutor().toString()+"'");
				} catch (Exception e) {
					log.warning("Unable to override plugin registration! Releasing control of 'me' Command...");
					this.ucm.skipMeCommand = true;
				}
			} else {
				this.getCommand("me").setExecutor(this.ucm);
			}
		}
		
		this.getCommand("ucm").setExecutor(this.ucm);

		log.info("Enabled Successfully!");
	}
	
	@Override
	public void onDisable() {
		this.ucm.saveConfig(); //Save the config
		ParserLib.cleanup(); //Clean up the ParserLib
		this.chat = null;
		this.permission = null;
		this.ucm = null;
		log.info("Plugin Disabled");
		log = null;
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