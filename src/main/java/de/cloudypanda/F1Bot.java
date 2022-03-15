package de.cloudypanda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class F1Bot {
    private static Long guildID;
    public static HashMap<Long, List<Long>> channelMap;
    public static long meetingChannelId;
    public static HashMap<Integer, Long> setupMap;

    public static void main(String[] args) throws LoginException, InterruptedException {
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
        guildID = Long.parseLong(guildId);

        channelMap = new HashMap<>();
        setupMap = new HashMap<>();

        LoadConfig();

        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandInteractionListener())
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(guildID);

        if(guild == null){
            Logger.error("No Guild found, cancelling command adding");
            return;
        }
        //Create Commands for Guild Only
        guild.upsertCommand(Commands.slash("control", "Prints the controlwindow")).queue();
        guild.upsertCommand(Commands.slash("setup", "Command to setup bot")
                .addOption(OptionType.INTEGER, "id", "id of the button", true)
                .addOption(OptionType.CHANNEL, "channel","Channel for the id", true)).queue();

        guild.upsertCommand(Commands.slash("assign", "Gives a panel for the users to assign to channels")).queue();

        guild.upsertCommand(Commands.slash("list", "Prints a list of all assigned users")).queue();

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
            channelMap = mapper.readValue(channelFile, new TypeReference<HashMap<Long, List<Long>>>() {});

        } catch (IOException e) {
            Logger.error(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current Channel Map State: \n" );
        channelMap.forEach((id, list) -> {
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

            setupMap = mapper.readValue(setupFile, new TypeReference<HashMap<Integer, Long>>() {});
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }

        StringBuilder sb2 = new StringBuilder();
        sb2.append("Current Setup Map State: \n");
        setupMap.forEach((id, channel) ->sb2.append("ID:" + id + ", ChannelID:" + channel));
        Logger.info(sb2.toString());
    }

    public static void SafeChannelConfig() {
        try {
            // Jackson Mapper
            ObjectMapper mapper = new ObjectMapper();

            // Java object to JSON file
            mapper.writeValue(new File("./config/channelConfig.json"), channelMap);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

    public static void SafeSetupConfigs(){
        try {
            // Jackson Mapper
            ObjectMapper mapper = new ObjectMapper();

            // Java object to JSON file
            mapper.writeValue(new File("./config/setupConfig.json"), setupMap);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

}
