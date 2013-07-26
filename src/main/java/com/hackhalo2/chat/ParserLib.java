package com.hackhalo2.chat;

import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.UPlayer;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class ParserLib {
	
	private static Permission perms; //The Vault Permissions Registration
	private static Chat chat; //The Vault Chat Registration

	private ParserLib() { }
	
	
	public static String replacePlayerPlaceholders(Player player, String format) {
		return format.replace("%prefix", getPlayerPrefix(player))
				.replace("%suffix", getPlayerSuffix(player))
				.replace("%world", player.getWorld().getName())
				.replace("%player", player.getName())
				.replace("%displayname", player.getDisplayName())
				.replace("%group", chat.getPrimaryGroup(player))
				.replace("%faction", getPlayerFaction(player));
	}

	private static String getPlayerFaction(Player player) {
		String factionTag = "";

		if(UnleashedChatManager.factionsEnabled) {
			UPlayer uPlayer = UPlayer.get(player); //Get the Faction Player reference
			factionTag = uPlayer.getFactionName()+""+uPlayer.getRole().getPrefix();
		}

		return factionTag;
	}

	private static String getPlayerPrefix(Player player) {
		String prefix = chat.getPlayerPrefix(player);

		if(prefix == null || prefix.equals("") || prefix.isEmpty()) {
			String group = perms.getPrimaryGroup(player);
			prefix = chat.getGroupPrefix(player.getWorld().getName(), group);

			if(prefix == null || prefix.equals("") || prefix.isEmpty()) {
				prefix = "";
			}
		}

		return prefix;
	}

	private static String getPlayerSuffix(Player player){
		String suffix = chat.getPlayerPrefix(player);

		if(suffix == null || suffix.equals("")  || suffix.isEmpty()){
			String group = perms.getPrimaryGroup(player);
			suffix = chat.getGroupPrefix(player.getWorld().getName(),group);

			if(suffix == null || suffix.equals("")  || suffix.isEmpty()){
				suffix = "";
			}
		}
		return suffix;
	}
	
	protected static void setup(final Permission permsObject, final Chat chatObject) {
		perms = permsObject;
		chat = chatObject;
	}
	
	protected static void cleanup() {
		perms = null;
		chat = null;
	}

}
