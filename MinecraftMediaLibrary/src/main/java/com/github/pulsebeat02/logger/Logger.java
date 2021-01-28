package com.github.pulsebeat02.logger;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Logger {

    public static volatile BufferedWriter WRITER;
    public static boolean VERBOSE;

    static {
        try {
            File f = new File(System.getProperty("user.dir") + "\\mml.log");
            if (f.createNewFile()) {
                System.out.println("File Created (" + f.getName() + ")");
            } else {
                System.out.println("Log File Exists Already");
                FileChannel.open(Paths.get(f.getPath()), StandardOpenOption.WRITE).truncate(0).close();
            }
            WRITER = new BufferedWriter(new FileWriter(f));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void info(@NotNull final String info) {
        directPrint(System.currentTimeMillis() + ": [INFO] " + info + "\n");
    }

    public static void warn(@NotNull final String warning) {
        directPrint(System.currentTimeMillis() + ": [WARN] " + warning + "\n");
    }

    public static void error(@NotNull final String error) {
        directPrint(System.currentTimeMillis() + ": [ERROR] " + error + "\n");
    }

    public static void directPrint(@NotNull final String line) {
        if (VERBOSE) {
            try {
                WRITER.write(line);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void setVerbose(final boolean verbose) {
        VERBOSE = verbose;
    }

}
