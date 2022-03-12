package de.cloudypanda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;

public class F1Bot {
    private static Long guildID;
    public static HashMap<Long, List<Long>> channelMap;
    public static long meetingChannelId;
    public static HashMap<Integer, Long> setupMap;
    public static final Logger LOGGER = LoggerFactory.getLogger(F1Bot.class);

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = System.getenv("TOKEN");
        String guildId = System.getenv("GUILDID");
        if(token.isEmpty()) {
            F1Bot.LOGGER.error("Cannot start Bot, ENV TOKEN is empty");
            return;
        }

        if(guildId.isEmpty()){
            F1Bot.LOGGER.error("Cannot start Bot, ENV GUILDID is empty");
            return;
        }
        guildID = Long.parseLong(guildId);

        channelMap = new HashMap<>();
        setupMap = new HashMap<>();

        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandInteractionListener())
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(guildID);

        if(guild == null){
            F1Bot.LOGGER.error("No Guild found, cancelling command adding");
            return;
        }
        //Create Commands for Guild Only
        guild.upsertCommand(Commands.slash("control", "Prints the controlwindow")).queue();
        guild.upsertCommand(Commands.slash("setup", "Command to setup bot")
                .addOption(OptionType.INTEGER, "id", "id of the button", true)
                .addOption(OptionType.CHANNEL, "channel","Channel for the id", true)).queue();

        guild.upsertCommand(Commands.slash("assign", "Gives a panel for the users to assign to channels")).queue();

        guild.upsertCommand(Commands.slash("list", "Prints a list of all assigned users")).queue();

        guild.retrieveCommands().queue(commands -> commands.forEach(command -> F1Bot.LOGGER.debug("Found registered Command: " + command.getName())));

        F1Bot.LOGGER.info("Added commands to Guild: " + guild.getName());
    }
}
