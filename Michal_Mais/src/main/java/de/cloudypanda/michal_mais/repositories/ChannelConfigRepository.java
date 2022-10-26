package de.cloudypanda.michal_mais.repositories;

import de.cloudypanda.michal_mais.models.ChannelConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChannelConfigRepository extends CrudRepository<ChannelConfig, Long> {
    ChannelConfig findChannelConfigByDiscordChannelId(Long channelId);
    ChannelConfig findChannelConfigByChannelName(String channelName);
    ChannelConfig findChannelConfigByChannelIdentifier(String channelIdentifier);
}
