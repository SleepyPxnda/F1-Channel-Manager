package de.cloudypanda.michal_mais.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DiscordUser {

    @Id
    @GeneratedValue
    private Long id;
    private String name;

    private Long discordId;
    private Long assignedChannelId;

    public DiscordUser(String name, Long discordId, Long assignedChannelId) {
        this.name = name;
        this.discordId = discordId;
        this.assignedChannelId = assignedChannelId;
    }
}
