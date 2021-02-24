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

import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

public class EnchancedNativeDiscovery implements NativeDiscoveryStrategy {

    protected static final String PLUGIN_ENV_NAME = "VLC_PLUGIN_PATH";

    private static boolean found;

    private String discoveredPath;

    @Override
    public boolean supported() {
        return true;
    }

    @Override
    public String discover() {
        return null;
    }

    @Override
    public boolean onFound(final String s) {
        return false;
    }

    @Override
    public boolean onSetPluginPath(final String s) {
        return false;
    }

}
