package com.github.pulsebeat02.utility;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtilities {

    public static File downloadImageFile(@NotNull final String url, @NotNull final String path) {
        String filePath = path + "/" + UUID.randomUUID() + ".png";
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(filePath);
    }

}
