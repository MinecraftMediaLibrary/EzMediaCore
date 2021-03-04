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

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.EnchancedNativeDiscovery;

public class VLCNativeIdentificationTest {

    public static void main(final String[] args) {
        final EnchancedNativeDiscovery enchancedNativeDiscovery = new EnchancedNativeDiscovery("");
        System.out.println(enchancedNativeDiscovery.discover());
    }

}
