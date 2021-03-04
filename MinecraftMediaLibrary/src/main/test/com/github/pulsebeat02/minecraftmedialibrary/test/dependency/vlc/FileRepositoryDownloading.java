/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/3/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxOSPackages;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FileRepositoryDownloading {

    public static void main(final String[] args) {
        final LinuxPackageManager manager = new LinuxPackageManager("");
        for (final LinuxOSPackages dict : manager.getAllPackages().values()) {
            for (final LinuxPackage pkg : dict.getLinks().values()) {
                final String link = pkg.getUrl();
                final String fileName = link.substring(link.lastIndexOf("/") + 1);
                try {
                    FileUtils.copyURLToFile(new URL(link), new File("linux/" + fileName));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
