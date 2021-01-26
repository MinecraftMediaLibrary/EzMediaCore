package com.github.pulsebeat02;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class Logger {

    public static boolean VERBOSE;
    public static volatile FileChannel WRITER;

    static {
        try {
            File f = new File(System.getProperty("user.dir") + "\\mml.log");
            System.out.println(f.getAbsolutePath());
            if (f.createNewFile()) {
                System.out.println("File Created (" + f.getName() + ")");
            } else {
                System.out.println("Log File Exists Already");
            }
            WRITER = new RandomAccessFile(f, "rw").getChannel();
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
            byte[] bytes = line.getBytes();
            try {
                WRITER.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length).put(bytes);
                WRITER.force(false);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void setVerbose(final boolean verbose) {
        VERBOSE = verbose;
    }

}
