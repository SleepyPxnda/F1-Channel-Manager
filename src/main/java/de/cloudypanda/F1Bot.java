package de.cloudypanda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class F1Bot {
    public static void main(String[] args) throws InterruptedException {
        String token = System.getenv("TOKEN");
        String guildId = System.getenv("GUILDID");


        if(token.isEmpty()) {
            Logger.error("Cannot start Bot, ENV TOKEN is empty");
            return;
        }

        if(guildId.isEmpty()){
            Logger.error("Cannot start Bot, ENV GUILDID is empty");
            return;
        }

        Long discordGuildId = Long.parseLong(guildId);

        LoadConfig();

        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandInteractionListener())
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(discordGuildId);

        if(guild == null){
            Logger.error("No Guild found, cancelling command adding");
            return;
        }

        //Create Commands for Guild Only
        guild.upsertCommand(Commands.slash("control", "Prints the control window")).queue();
        guild.upsertCommand(Commands.slash("map", "Command to setup bot")
                        .addOptions(
                                new OptionData(OptionType.STRING, "button", "button which will be mapped to the given channel")
                                        .addChoice("Meeting Channel", "cabin_meeting")
                                        .addChoice("Streaming Room", "cabin_streaming")
                                        .addChoice("Cabin 1","cabin_1")
                                        .addChoice("Cabin 2","cabin_2")
                                        .addChoice("Cabin 3","cabin_3")
                                        .addChoice("Cabin 4","cabin_4")
                                        .addChoice("Cabin 5","cabin_5")
                        )
                        .addOption(OptionType.CHANNEL, "channel", "channel which is mapped to the button"))
                        .queue();

        guild.upsertCommand(Commands.slash("assign", "Gives a panel for the users to assign to channels")).queue();

        guild.upsertCommand(Commands.slash("list", "Prints a new message with a user list, this list gets updated by the bot if anything changes")).queue();

        guild.retrieveCommands().queue(commands -> commands.forEach(command -> Logger.debug("Found registered Command: " + command.getName())));

        Logger.info("Added commands to Guild: " + guild.getName());
    }


    public static void LoadConfig() {
        // Jackson Mapper
        ObjectMapper mapper = new ObjectMapper();

        try {
            if(!Files.exists(Paths.get("./config/"))){
                Files.createDirectory(Paths.get("./config/"));
                Logger.info("Created config directory!");
            }
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }


        try {
            File channelFile = new File("./config/channelConfig.json");

            if(channelFile.createNewFile()){
                Logger.info("Created new file for channelconfig");
            } else {
                Logger.info("Found existing channelconfig");
            }

            // Java object to JSON file
            ConfigHandler.channelConfig = mapper.readValue(channelFile, new TypeReference<HashMap<Long, List<Long>>>() {});

        } catch (IOException e) {
            Logger.error(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current Channel Map State: \n" );
        ConfigHandler.channelConfig.forEach((id, list) -> {
            sb.append("ID: " + id);
            sb.append(" Users: ");
            list.forEach(user -> sb.append(user).append(" ,"));
            sb.append("\n");
        });
        Logger.info(sb.toString());

        try {
            File setupFile = new File("./config/setupConfig.json");

            if(setupFile.createNewFile()){
                Logger.info("Created new file for setupconfig");
            } else {
                Logger.info("Found existing setupconfig");
            }

            ConfigHandler.channelMapping = mapper.readValue(setupFile, new TypeReference<HashMap<Integer, Long>>() {});
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }

        StringBuilder sb2 = new StringBuilder();
        sb2.append("Current Setup Map State: \n");
        ConfigHandler.channelMapping.forEach((id, channel) ->sb2.append("ID:" + id + ", ChannelID:" + channel));
        Logger.info(sb2.toString());
    }

    public static void SafeChannelConfig() {
        try {
            // Jackson Mapper
            ObjectMapper mapper = new ObjectMapper();

            // Java object to JSON file
            mapper.writeValue(new File("./config/channelConfig.json"), ConfigHandler.channelMapping);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

    public static void SafeSetupConfigs(){
        try {
            // Jackson Mapper
            ObjectMapper mapper = new ObjectMapper();

            // Java object to JSON file
            mapper.writeValue(new File("./config/setupConfig.json"), ConfigHandler.channelConfig);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

}
