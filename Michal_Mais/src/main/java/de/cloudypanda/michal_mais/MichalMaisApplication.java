package de.cloudypanda.michal_mais;

import de.cloudypanda.michal_mais.discord.CommandInteractionListener;
import de.cloudypanda.michal_mais.discord.SlashCommandInteractionListener;
import de.cloudypanda.michal_mais.repositories.ChannelConfigRepository;
import de.cloudypanda.michal_mais.repositories.UserRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class MichalMaisApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MichalMaisApplication.class);

    @Autowired
    private UserRepository _userRepository;

    @Autowired
    private ChannelConfigRepository _channelConfigRepository;

    public static void main(String[] args){

        SpringApplication.run(MichalMaisApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        String token = System.getenv("PROJECT_TOKEN");
        String guildId = System.getenv("PROJECT_GUILDID");

        if(token.isEmpty()) {
            logger.error("Cannot start Bot, ENV TOKEN is empty");
            return;
        }

        if(guildId.isEmpty()){
            logger.error("Cannot start Bot, ENV GUILDID is empty");
            return;
        }

        Long discordGuildId = Long.parseLong(guildId);

        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(
                        new CommandInteractionListener(_userRepository, _channelConfigRepository),
                        new SlashCommandInteractionListener(_channelConfigRepository, _userRepository))
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(discordGuildId);

        if(guild == null){
            logger.error("No Guild found, cancelling command adding");
            return;
        }

        //Create Commands for Guild Only
        guild.upsertCommand(Commands.slash("control", "Gibt das Kontrollfenster aus")).queue();
        guild.upsertCommand(Commands.slash("map", "Setup-Command für die Channel")
                        .addOptions(
                                new OptionData(OptionType.STRING, "button", "Auswahlbutton für einen Channel")
                                        .addChoice("Meeting Channel", "cabin_meeting")
                                        .addChoice("Streaming Room", "cabin_streaming")
                                        .addChoice("Cabin 1","cabin_1")
                                        .addChoice("Cabin 2","cabin_2")
                                        .addChoice("Cabin 3","cabin_3")
                                        .addChoice("Cabin 4","cabin_4")
                                        .addChoice("Cabin 5","cabin_5")
                        )
                        .addOption(OptionType.CHANNEL, "channel", "Channe welcher für den Button hinterlegt werden soll"))
                .queue();

        guild.upsertCommand(Commands.slash("assign", "Gibt das Auswahlfenster für Nutzer aus")).queue();

        guild.upsertCommand(Commands.slash("list", "Gibt eine aktuelle Liste der zugewiesenen Channels aus")).queue();

        guild.retrieveCommands().complete().forEach(x -> logger.info("Found Command:" + x.getName() +" . " + x.getId()));

        logger.info("Added commands to Guild: " + guild.getName());
    }
}
