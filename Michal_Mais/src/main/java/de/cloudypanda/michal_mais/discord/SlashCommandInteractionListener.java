package de.cloudypanda.michal_mais.discord;

import de.cloudypanda.michal_mais.models.ChannelConfig;
import de.cloudypanda.michal_mais.models.DiscordUser;
import de.cloudypanda.michal_mais.repositories.ChannelConfigRepository;
import de.cloudypanda.michal_mais.repositories.UserRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SlashCommandInteractionListener extends ListenerAdapter {

    private final ChannelConfigRepository _channelConfigRepository;
    private final UserRepository _userRepository;

    @Autowired
    public SlashCommandInteractionListener(ChannelConfigRepository channelConfigRepository, UserRepository userRepository) {
        _channelConfigRepository = channelConfigRepository;
        _userRepository = userRepository;
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent ev){
        if(!ev.isFromGuild()) return;
        switch (ev.getName()) {
            case "control" -> executeControlCommand(ev);
            case "assign" -> executeAssignCommand(ev);
            case "map" -> executeMapCommand(ev);
            case "list" -> executeListCommand(ev);
        }
    }

    private void executeControlCommand(SlashCommandInteractionEvent ev){
        ev.reply("Kontrollfenster, durch Klick der Buttons kannst du alle Registrierten Benutzer moven.")
                .addActionRow(
                        Button.primary("move_cabins", "In die Cabins moven."),
                        Button.secondary("move_meeting", "In den Meetingroom moven.")
                )
                .setEphemeral(true)
                .queue();
    }



    private void executeAssignCommand(SlashCommandInteractionEvent ev){
        SelectMenu menu = SelectMenu.create("choose-channel")
                .addOption("Streaming Room", "cabin_streaming")
                .addOption("Cabin 1","cabin_1")
                .addOption("Cabin 2","cabin_2")
                .addOption("Cabin 3","cabin_3")
                .addOption("Cabin 4","cabin_4")
                .addOption("Cabin 5","cabin_5")
                .build();

        ev.reply("Bitte wähle eine Kabine aus:")
                .setEphemeral(false)
                .addActionRow(menu)
                .queue();
    }

    private void executeMapCommand(SlashCommandInteractionEvent ev) {
        String buttonId = ev.getOption("button").getAsString();
        Channel selectedChannel = ev.getOption("channel").getAsChannel();

        if(selectedChannel.getType() != ChannelType.VOICE){
            ev.reply("\uD83D\uDEAB Der Channel muss ein Sprachchannel sein.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        ChannelConfig alreadyUsedChannel = _channelConfigRepository.findChannelConfigByDiscordChannelId(selectedChannel.getIdLong());

        if(alreadyUsedChannel != null){
            ev.reply("\uD83D\uDEAB Dieser Channel wird schon durch das Mapping von *" + alreadyUsedChannel.getChannelIdentifier() + "* benutzt.").queue();
            return;
        }

        ChannelConfig existing = _channelConfigRepository.findChannelConfigByChannelIdentifier(buttonId);

        if(existing == null){
            _channelConfigRepository.save(new ChannelConfig(selectedChannel.getIdLong(), selectedChannel.getName(), buttonId));
            ev.reply("☑️ Der Channel " + selectedChannel.getAsMention() + " ist nun für den Button *" + buttonId + "* hinterlegt.")
                    .queue();
            return;
        }

        existing.setChannelName(selectedChannel.getName());
        existing.setDiscordChannelId(selectedChannel.getIdLong());

        _channelConfigRepository.save(existing);

        ev.reply("\uD83D\uDD04 Der Channel " + selectedChannel.getAsMention() + " ist nun für den Button *" + buttonId + "* hinterlegt.")
                .queue();
    }

    private void executeListCommand(SlashCommandInteractionEvent ev) {
        ev.deferReply().queue();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Cabin Zuweisungen");
        eb.setColor(Color.CYAN);

        List<DiscordUser> users = new ArrayList<>();
        _userRepository.findAll().forEach(users::add);

        Map<Long, List<DiscordUser>> partitionedUsers = users.stream().collect(Collectors.groupingBy(x -> x.getAssignedChannelId()));

        partitionedUsers.forEach((channel, userList) -> {
            String channelName = ev.getGuild().getGuildChannelById(channel).getName();

            StringBuilder names = new StringBuilder();

            userList.forEach(user ->
                    names.append(user.getName())
                    .append(" \n"));
            eb.addField(channelName, names.toString(), false);
        });

        ev.getInteraction().getHook().sendMessageEmbeds(eb.build()).queue();
    }
}
