package com.hackhalo2.chat.channels;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hackhalo2.chat.UnleashedChatManager;

public class ChannelManager {
	
	private static HashMap<ChannelType, List<String>> channelMap; //The Channel Map
	
	private static YamlConfiguration cache = new YamlConfiguration(); //The disk copy of the ChannelMap
	private static File cacheFile;
	
	private ChannelManager() { }
	
	public static void setup() {
		UnleashedChatManager.log.info("Setting up the ChannelManager...");
		
		//Set up the YamlConfiguration
		cache = YamlConfiguration.loadConfiguration(cacheFile);
		
		//Load the arrays into the channel map
		for(ChannelType value : ChannelType.values()) {
			List<String> names = cache.getStringList("cache."+value.getChannelName());
			if(names == null) names = new ArrayList<String>(); //Null check, just in case
			channelMap.put(value, names);
		}
	}
	
	public static void setCacheFile(File file) {
		cacheFile = file;
		if(!cacheFile.exists()) {
			try {
				cacheFile.createNewFile();
			} catch (Exception e) {
				UnleashedChatManager.log.log(Level.SEVERE, "Unable to create cache file!", e);
			}
		}
	}

}
