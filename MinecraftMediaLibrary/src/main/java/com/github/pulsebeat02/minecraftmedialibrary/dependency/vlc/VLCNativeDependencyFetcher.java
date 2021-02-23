/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.utility.OperatingSystemUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ZipFileUtilities;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class VLCNativeDependencyFetcher {

    public Plugin plugin;

    public VLCNativeDependencyFetcher(@NotNull final Plugin plugin) {
        this.plugin = plugin;
    }

    public void downloadLibraries() {
        boolean installed = new NativeDiscovery().discover();
        if (!installed) {
            String option = OperatingSystemUtilities.DOWNLOAD_OPTION;
            /*
            Must compile C libraries manually by using a library because they are using Linux
             */
            if (option.equalsIgnoreCase("COMPILE")) {

            } else {
                // Download Zip from hosted Github repo
                try (BufferedInputStream in = new BufferedInputStream(new URL(option).openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream("VLC.zip")) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File zip = new File("VLC.zip");
                String path = zip.getAbsolutePath();
                String dest = zip.getParent() + "/libs";
                // Extract to libs folder
                ZipFileUtilities.unzip(path, dest);
            }
        }
    }

}
