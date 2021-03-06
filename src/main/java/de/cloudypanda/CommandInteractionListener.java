package de.cloudypanda;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;

public class CommandInteractionListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent ev){
        if(!ev.isFromGuild()) return;
        switch (ev.getName()) {
            case "control" -> executeControlCommand(ev);
            case "assign" -> executeAssignCommand(ev);
            case "setup" -> executeSetupCommand(ev);
            case "list" -> executeListCommand(ev);
        }
    }

    private void executeListCommand(SlashCommandInteractionEvent ev) {
        ev.deferReply().queue();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Cabin Zuweisungen");
        eb.setColor(Color.CYAN);

        F1Bot.channelMap.forEach((channelId, userList) -> {
            StringBuilder sb = new StringBuilder();

            userList.forEach(user -> {
                Member member = ev.getGuild().retrieveMemberById(user).complete();

                if(member == null){
                    Logger.debug("Cannot find user with id: " + user + " in guild " + ev.getGuild().getName());
                    return;
                }

                sb.append("<:small_green_diamond:923596714449989692> ")
                        .append(member.getEffectiveName())
                        .append("\n");
            });

            eb.addField(ev.getGuild().getVoiceChannelById(channelId).getName(), sb.toString(), false);
        });
        ev.getHook().sendMessageEmbeds(eb.build()).queue();
    }

    private void executeSetupCommand(SlashCommandInteractionEvent ev) {
        int id = ev.getOption("id").getAsInt();
        long channelID = ev.getOption("channel").getAsLong();

        if(ev.getGuild().getGuildChannelById(channelID).getType() != ChannelType.VOICE){
            ev.reply("Der Channel muss ein Voicechannel sein")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if(id > 5 || id < 0) {
            ev.reply("Id muss zwischen 0 und 5 sein; 5 ist der Meetingroom, 0-4 sind die Cabins")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if(id == 5){
            F1Bot.meetingChannelId = channelID;
            ev.reply("**" + ev.getGuild().getVoiceChannelById(channelID).getName() + "** ist nun der Meetingchannel" )
                    .setEphemeral(true)
                    .queue();
            Logger.info("Channel " + ev.getGuild().getVoiceChannelById(channelID).getName() + " set as meeting room");
            return;
        }

        F1Bot.setupMap.put(id, channelID);
        ev.reply("ID **" + id + "** wurde auf den Channel **" + ev.getGuild().getVoiceChannelById(channelID).getName() + "** gesetzt")
                .setEphemeral(true)
                .queue();
        Logger.info("Channel " + ev.getGuild().getVoiceChannelById(channelID).getName() + " set as channel for id " + id);

        F1Bot.SafeSetupConfigs();
    }

    public void executeAssignCommand(SlashCommandInteractionEvent ev){
        ev.reply("Zuweisungsfenster, durch anklicken der Buttons kannst du dir eine Cabin zuweisen")
                .addActionRow(
                        Button.primary("assign_broadcast", "Streaming"),
                        Button.primary("assign_1", "Channel 1"),
                        Button.primary("assign_2", "Channel 2"),
                        Button.primary("assign_3", "Channel 3"),
                        Button.primary("assign_4", "Channel 4")
                )
                .queue();
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

        if(event.getComponentId().equals("assign_broadcast")){
            addUserToMap(event, 0);
        }
        if(event.getComponentId().equals("assign_1")){
            addUserToMap(event, 1);
        }
        if(event.getComponentId().equals("assign_2")){
            addUserToMap(event, 2);
        }
        if(event.getComponentId().equals("assign_3")){
            addUserToMap(event, 3);
        }
        if(event.getComponentId().equals("assign_4")){
            addUserToMap(event, 4);
        }

        if(event.getComponentId().equals("move_cabins")){
            event.reply("Alle Nutzer werden nun in die Cabins gemovet!")
                    .setEphemeral(true)
                    .queue();

            F1Bot.channelMap.forEach((channelId, userList) -> {
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
                event.reply("Kann nicht in Meeting moven, da die ID daf??r nicht gesetzt wurde. Bitte setze diese mit /setup 0 [Meetingraum]").queue();
                return;
            }
            event.reply("Alle Benutzer werden nun in den Meetingraum gemovet!")
                    .setEphemeral(true)
                    .queue();
            F1Bot.channelMap.forEach((channelId, userList) -> {
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

    private void addUserToMap(ButtonInteractionEvent event, int mapId){
        long channelID = F1Bot.setupMap.get(mapId);
        //System.out.println("Starting event for " + event.getGuild().getVoiceChannelById(channelID).getName());

        if(channelID == 0){
            event.reply("No channel setup for id " + mapId)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if(!F1Bot.channelMap.containsKey(channelID)){
            Logger.debug("No key for vc " + event.getGuild().getVoiceChannelById(channelID).getName());
            F1Bot.channelMap.put(channelID, new ArrayList<>());
        }

        Logger.debug("Adding member to key: " + event.getGuild().getVoiceChannelById(channelID).getName());
        F1Bot.channelMap.forEach((channel, list) ->  list.remove(event.getMember().getIdLong()));
        F1Bot.channelMap.get(channelID).add(event.getMember().getIdLong());
        Logger.debug("Key now has " + F1Bot.channelMap.get(channelID).size() + " entries");
        event.reply( event.getMember().getEffectiveName() + " ist nun dem Channel " + event.getGuild().getVoiceChannelById(channelID).getName() + " zugewiesen")
                .setEphemeral(true)
                .queue();

        F1Bot.SafeChannelConfig();
    }
}
