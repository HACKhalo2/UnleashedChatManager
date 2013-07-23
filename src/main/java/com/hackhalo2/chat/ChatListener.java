package com.hackhalo2.chat;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author t3hk0d3 (ChatManager)
 * @author rymate (bChatManager)
 * @author hackhalo2 (UnleashedChatManager)
 */
public class ChatListener implements Listener {

    public String messageFormat = "%prefix %player: &f%message";
    public String localMessageFormat = "[LOCAL] %prefix %player: &f%message";
    public String personalMessageFormat = "[MSG] [%player -> %reciever] &f%message";
    public String opMessageFormat = "&c[OPS ONLY] %player: &f%message";
    
    public boolean rangedMode = false;
    public boolean specialFeatures = true;
    public double chatRange = 100d;
    
    private final UnleashedChatManager plugin;

    public ChatListener(UnleashedChatManager chatManager) {
        this.plugin = chatManager;
        
        this.messageFormat = this.plugin.getConfig().getString("formats.message-format", this.messageFormat);
        this.localMessageFormat = this.plugin.getConfig().getString("formats.local-message-format", this.localMessageFormat);
        this.personalMessageFormat = this.plugin.getConfig().getString("formats.personal-message-format", this.personalMessageFormat);
        this.rangedMode = this.plugin.getConfig().getBoolean("toggles.ranged-mode", this.rangedMode);
        this.specialFeatures = this.plugin.getConfig().getBoolean("toggles.special-features", this.specialFeatures);
        this.chatRange = this.plugin.getConfig().getDouble("other.chat-range", this.chatRange);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        String message = this.messageFormat;
        String chatMessage = event.getMessage();

        if (this.rangedMode) {
            message = this.localMessageFormat;
            event.getRecipients().clear();
            event.getRecipients().addAll(this.plugin.getLocalRecipients(player, message, this.chatRange));
            event.getRecipients().addAll(this.plugin.getSpies());
        }
        
        if (this.specialFeatures) {
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