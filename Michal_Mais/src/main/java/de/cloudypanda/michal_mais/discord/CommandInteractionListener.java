package de.cloudypanda.michal_mais.discord;

import de.cloudypanda.michal_mais.models.ChannelConfig;
import de.cloudypanda.michal_mais.models.DiscordUser;
import de.cloudypanda.michal_mais.repositories.ChannelConfigRepository;
import de.cloudypanda.michal_mais.repositories.UserRepository;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandInteractionListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommandInteractionListener.class);

    private final UserRepository _userRepository;
    private final ChannelConfigRepository _channelConfigRepository;

    @Autowired
    public CommandInteractionListener(UserRepository userRepository, ChannelConfigRepository channelConfigRepository){
        _userRepository = userRepository;
        _channelConfigRepository = channelConfigRepository;
    }
    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent ev) {
        if (ev.getComponentId().equals("choose-channel")) {
            Member interactionMember = ev.getMember();
            SelectOption channelSelection = ev.getSelectedOptions().get(0);


            DiscordUser existing = _userRepository.findDiscordUserByDiscordId(interactionMember.getIdLong());
            ChannelConfig channelConfig = _channelConfigRepository.findChannelConfigByChannelIdentifier(channelSelection.getValue());

            if(channelConfig == null){
                logger.error(interactionMember.getEffectiveName() + " tried to sign up for channel which does not exists: " + channelSelection.getValue());
                ev.reply("\uD83D\uDEAB Für deine Auswahl *" + channelSelection.getLabel() + " wurde bisher kein Channel konfiguriert. \uD83D\uDEAB")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if(existing == null){
                _userRepository.save(new DiscordUser(interactionMember.getEffectiveName(), interactionMember.getIdLong(), channelConfig.getDiscordChannelId()));
                ev.reply("☑️ Du hast dich dem Channel " + channelConfig.getChannelName() + " zugewiesen.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            existing.setAssignedChannelId(channelConfig.getDiscordChannelId());
            _userRepository.save(existing);

            ev.reply("\uD83D\uDD04 Du bist nun dem Channel " + channelConfig.getChannelName() + " zugewiesen.")
                    .setEphemeral(true)
                    .queue();
        }
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferReply().queue();

        if (event.getComponentId().equals("move_cabins")) {
            event.getInteraction().getHook().sendMessage("Alle Nutzer werden nun in die Cabins gemovet!")
                    .setEphemeral(true)
                    .queue();

            _userRepository.findAll().forEach(user -> {
                Member member = event.getGuild().getMemberById(user.getDiscordId());

                if(member == null) {
                    return;
                }

                if(member.getVoiceState().inAudioChannel()){
                    if(event.getGuild().getVoiceChannelById(user.getAssignedChannelId()) == null){
                        logger.error("Cannot move user " + user.getName() + " to channelid " + user.getAssignedChannelId());
                        return;
                    }
                    //ToDo: Print list of successfull moved members and of failures
                    event.getGuild().moveVoiceMember(member, event.getGuild().getVoiceChannelById(user.getAssignedChannelId())).queue();
                }

            });
        }

        if (event.getComponentId().equals("move_meeting")) {
            ChannelConfig meetingConfig = _channelConfigRepository.findChannelConfigByChannelIdentifier("cabin_meeting");

            if(meetingConfig == null){
                event.getInteraction().getHook().sendMessage("\uD83D\uDEAB Es wurde bisher kein Meeting Room konfiguriert. Bitte mache dies mit dem /map Command. \uD83D\uDEAB")
                        .queue();
            }

            event.getInteraction().getHook().sendMessage("Alle Nutzer werden nun in den Meeting Room " +
                    event.getGuild().getVoiceChannelById(meetingConfig.getDiscordChannelId()).getAsMention() + " gemovet!")
                    .setEphemeral(true)
                    .queue();

            _userRepository.findAll().forEach(user -> {
                Member member = event.getGuild().getMemberById(user.getDiscordId());

                if(member == null) {
                    return;
                }

                if(member.getVoiceState().inAudioChannel()){
                    if(event.getGuild().getVoiceChannelById(user.getAssignedChannelId()) == null){
                        logger.error("Cannot move user " + user.getName() + " to channelid " + user.getAssignedChannelId());
                        return;
                    }
                    //ToDo: Print list of successfull moved members and of failures
                    event.getGuild().moveVoiceMember(member, event.getGuild().getVoiceChannelById(meetingConfig.getDiscordChannelId())).queue();
                }
            });
        }
    }
}
