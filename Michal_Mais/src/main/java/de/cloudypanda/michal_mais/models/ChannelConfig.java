package de.cloudypanda.michal_mais.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChannelConfig {
    @Id
    @GeneratedValue
    private Long id;
    private Long discordChannelId;
    private String channelName;
    private String channelIdentifier;

    public ChannelConfig(Long discordChannelId, String channelName, String channelIdentifier) {
        this.discordChannelId = discordChannelId;
        this.channelName = channelName;
        this.channelIdentifier = channelIdentifier;
    }
}
