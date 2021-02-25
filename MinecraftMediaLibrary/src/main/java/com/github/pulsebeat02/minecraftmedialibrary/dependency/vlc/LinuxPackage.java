/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/24/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import org.jetbrains.annotations.NotNull;

public class LinuxPackage {

    private final CPUArchitecture arch;
    private final String url;
    private final String mirror;

    public LinuxPackage(@NotNull final String url, @NotNull final CPUArchitecture arch) {
        this.arch = arch;
        this.url = url;
        mirror =
                "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/linux/"
                        + url.substring(url.lastIndexOf("/") + 1);
    }

    public CPUArchitecture getArch() {
        return arch;
    }

    public String getUrl() {
        return url;
    }

    public String getMirror() {
        return mirror;
    }
}
