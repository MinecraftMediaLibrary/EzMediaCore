/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/20/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import org.jetbrains.annotations.NotNull;

public enum DependencyResolution {

    MAVEN_DEPENDENCY("https://repo1.maven.org/maven2/"),
    JITPACK_DEPENDENCY("https://jitpack.io/");

    private final String baseUrl;

    DependencyResolution(@NotNull final String url) {
        baseUrl = url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

}
