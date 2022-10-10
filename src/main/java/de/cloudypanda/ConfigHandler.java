package de.cloudypanda;

import de.cloudypanda.model.ChannelEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler {

    public static List<ChannelEntry> Entries;
    public static long meetingChannelId;

    public ConfigHandler() {
        Entries = new ArrayList<>();
    }

    public void createConfigFile(){
        //ToDo: Create Config File
    }

    public void readConfig() {
        //ToDO: Read Config file
    }
}
