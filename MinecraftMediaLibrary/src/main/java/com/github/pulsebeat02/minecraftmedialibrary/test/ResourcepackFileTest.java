/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.google.gson.JsonObject;

public class ResourcepackFileTest {

    public static void main(String[] args) {

        System.out.println(generateJSON());

    }

    private static String generateJSON() {

        JsonObject format = new JsonObject();
        format.addProperty("format", 6);

        JsonObject description = new JsonObject();
        description.addProperty("description", "Example Pack");

        JsonObject pack = new JsonObject();
        pack.add("pack", format);
        pack.add("pack", description);

        return pack.toString();

    }

}
