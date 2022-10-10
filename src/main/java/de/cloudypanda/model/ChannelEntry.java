package de.cloudypanda.model;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ChannelEntry {
    public String ChannelId;
    public String ChannelName;
    public Long DiscordChannelId;
    public List<Long> EntryMembers;

    public ChannelEntry (String channelId, String channelName, Long discordChannelId) {
        EntryMembers = new ArrayList<>();
        this.ChannelId = channelId;
        this.ChannelName = channelName;
        this.DiscordChannelId = discordChannelId;
    }
}
