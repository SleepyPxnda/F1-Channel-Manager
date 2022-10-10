package de.cloudypanda;

import de.cloudypanda.model.ChannelEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class CommandInteractionListener extends ListenerAdapter {
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

    private void executeListCommand(SlashCommandInteractionEvent ev) {
        ev.deferReply().queue();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Cabin Zuweisungen");
        eb.setColor(Color.CYAN);

        //ToDo: List all Users and thei're assigned channels
    }

    private void executeMapCommand(SlashCommandInteractionEvent ev) {
        String buttonId = ev.getOption("button").getAsString();
        Channel selectedChannel = ev.getOption("channel").getAsChannel();

        if(selectedChannel.getType() != ChannelType.VOICE){
            ev.reply("The channel has to be a voice-channel.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if(buttonId.equals("cabin_meeting")){
            ConfigHandler.meetingChannelId = selectedChannel.getIdLong();
            ev.reply("Successfully mapped " + selectedChannel.getAsMention() + " as the meeting channel")
                    .queue();
        }

        if(!ConfigHandler.Entries.stream().anyMatch(x -> Objects.equals(x.ChannelId, buttonId))){
            ConfigHandler.Entries.add(new ChannelEntry(buttonId, selectedChannel.getName() , selectedChannel.getIdLong()));
        }

        ev.reply("Successfully mapped " + selectedChannel.getAsMention() + " as the channel for " + buttonId)
                .queue();

        F1Bot.SafeSetupConfigs();
    }

    public void executeAssignCommand(SlashCommandInteractionEvent ev){


        SelectMenu menu = SelectMenu.create("choose-channel")
                .addOption("Streaming Room", "cabin_streaming")
                .addOption("Cabin 1","cabin_1")
                .addOption("Cabin 2","cabin_2")
                .addOption("Cabin 3","cabin_3")
                .addOption("Cabin 4","cabin_4")
                .addOption("Cabin 5","cabin_5")
                .build();

        ev.reply("Please pick your cabin below")
                .setEphemeral(false)
                .addActionRow(menu)
                .queue();
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent ev) {
        if (ev.getComponentId().equals("choose-channel")) {
            Member interactionMember = ev.getMember();
            String channelId = ev.getValues().get(0);
            ev.reply("You chose " + channelId)
                    .setEphemeral(true)
                    .queue();

            ConfigHandler.Entries
                    .forEach(x -> x.EntryMembers
                            .removeIf(y -> y.equals(interactionMember.getIdLong())));

            ConfigHandler.Entries
                    .stream()
                    .filter(x -> x.ChannelId.equals(channelId))
                    .findFirst()
                    .get()
                    .EntryMembers
                    .add(interactionMember.getIdLong());
        }
    }

    public void executeControlCommand(SlashCommandInteractionEvent ev){
        ev.reply("Kontrollfenster, durch klick der Buttons kannst du alle registrierten Benutzer moven")
                .addActionRow(
                        Button.primary("move_cabins", "In die Cabins moven"),
                        Button.secondary("move_meeting", "In den Meetingroom moven")
                )
                .setEphemeral(true)
                .queue();
    }



    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        //ToDo: Rework this to work with the selection menus

        if(event.getComponentId().equals("move_cabins")){
            event.reply("Alle Nutzer werden nun in die Cabins gemovet!")
                    .setEphemeral(true)
                    .queue();

            ConfigHandler.channelConfig.forEach((channelId, userList) -> {
                userList.forEach(user -> {
                    Member member = event.getGuild().getMemberById(user);

                    if(member == null){
                        return;
                    }

                    if(member.getVoiceState().inAudioChannel()){
                        Logger.debug("Moving user " + member.getEffectiveName() + " into channel " + event.getGuild().getVoiceChannelById(channelId).getName());
                        event.getGuild().moveVoiceMember(member, event.getGuild().getVoiceChannelById(channelId)).queue();
                    }
                });
            });
        }

        if(event.getComponentId().equals("move_meeting")){
            if(F1Bot.meetingChannelId == 0) {
                event.reply("Kann nicht in Meeting moven, da die ID dafür nicht gesetzt wurde. Bitte setze diese mit /setup 0 [Meetingraum]").queue();
                return;
            }
            event.reply("Alle Benutzer werden nun in den Meetingraum gemovet!")
                    .setEphemeral(true)
                    .queue();
            ConfigHandler.channelConfig.forEach((channelId, userList) -> {
                Logger.debug("Found " + userList.size() + " entries for channel " + channelId);
                userList.forEach(user -> {
                    Member member = event.getGuild().getMemberById(user);

                    if(member == null){
                        Logger.error("Cannot find user with id:" + user);
                        return;
                    }

                    if(!member.getVoiceState().inAudioChannel()){
                        Logger.debug("Skipping " + member.getEffectiveName() + " cause not in vc");
                        return;
                    }
                    Logger.debug("Moving user " + member.getEffectiveName() + " into meetingroom");
                    event.getGuild().moveVoiceMember(member, event.getGuild().getVoiceChannelById(F1Bot.meetingChannelId)).queue();
                });
            });
        }
    }
}
