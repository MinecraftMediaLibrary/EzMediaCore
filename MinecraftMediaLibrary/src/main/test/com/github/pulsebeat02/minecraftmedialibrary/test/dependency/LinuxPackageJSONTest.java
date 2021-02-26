/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/26/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageManager;

public class LinuxPackageJSONTest {

    public static void main(final String[] args) {
        System.out.println(new LinuxPackageManager().getAllPackages());
    }
}
