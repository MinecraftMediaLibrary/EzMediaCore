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

package com.github.pulsebeat02.minecraftmedialibrary.test.lab;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class LinuxPackageEnumSerialization {

    /*
    Utility class to generate JSON for resource.
     */
    public static void main(final String[] args) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final Type token = new TypeToken<Map<String, Map<String, Set<LinuxPackage>>>>() {
        }.getType();
        //        System.out.println(
        //                gson.toJson(
        //                        Arrays.stream(LinuxOSPackages.values())
        //                                .collect(Collectors.toMap(Enum::name,
        // LinuxOSPackages::getLinks)),
        //                        token));
    }
}
