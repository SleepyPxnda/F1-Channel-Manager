package de.cloudypanda.michal_mais.repositories;

import de.cloudypanda.michal_mais.models.DiscordUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<DiscordUser, Long> {
    DiscordUser findUserById(Long Id);
    List<DiscordUser> findUsersByAssignedChannelId(Long channelId);
    DiscordUser findDiscordUserByDiscordId(Long Id);
}
