package net.redct.client.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logger {

    private static final String FILE_NAME = "redutils_debug.log";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static PrintWriter writer;

    static {
        try {
            // Saves next to the minecraft log in .minecraft/logs/
            Path path = Paths.get("logs", FILE_NAME);
            writer = new PrintWriter(new FileWriter(path.toFile(), false)); // false = overwrite on launch
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String tag, String message) {
        if (writer == null) return;
        String line = String.format("[%s] [%s] %s", LocalTime.now().format(TIME_FORMAT), tag, message);
        writer.println(line);
        writer.flush();
    }

    public static void log(String tag, String format, Object... args) {
        log(tag, String.format(format, args));
    }

}