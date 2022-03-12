package de.cloudypanda;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public class Logger {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void debug(String message){
        System.out.println(GetTimeStamp() + " | " + message);
    }

    public static void info(String message){
        System.out.println(ANSI_YELLOW + GetTimeStamp() + " | " + message + ANSI_RESET);
    }

    public static void error(String message){
        System.out.println(ANSI_RED + GetTimeStamp() + " | " + message + ANSI_RESET);
    }

    private static String GetTimeStamp() {
        return new Timestamp(new Date().getTime()).toString();
    }

}
