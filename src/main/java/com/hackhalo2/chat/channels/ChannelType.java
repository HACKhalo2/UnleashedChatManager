package com.hackhalo2.chat.channels;

public enum ChannelType {
	GLOBAL("", "global", false), //The Global channel. All messages go to this one as a default
	NEUTRAL("[N]", "neutral", false), //The Neutral channel. Player sees all messages except ones in rivaling factions
	ALLY("[A]", "ally", true), //The Ally channel. The Player only sees chat from allies and faction members
	FACTION("[F]", "faction", true); //The Faction channel. The Player only sees messages from other faction members
	
	private String channelTag; //The prefix added to chat
	private String name; //The name of the channel
	private boolean isPrivate; //Flag to see if chat should broadcast to other channels
	
	private ChannelType(final String channelTag, final String name, final boolean isPrivate) {
		this.channelTag = channelTag;
		this.name = name;
		this.isPrivate = isPrivate;
	}
	
	public String getChannelTag() {
		return this.channelTag;
	}
	
	public String getChannelName() {
		return this.name;
	}
	
	public boolean isPrivate() {
		return this.isPrivate;
	}

}
