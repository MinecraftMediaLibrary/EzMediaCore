/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/10/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.resourcepack

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun main() {
    println("===============================")
    val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
    val full = JsonObject()
    val pack = JsonObject()
    pack.addProperty("pack-format", 8)
    pack.addProperty("description", "description")
    full.add("pack", pack)
    System.out.println(gson.toJson(full))
    println("===============================")
    val category = JsonObject()
    val type = JsonObject()
    val sounds = JsonArray()
    sounds.add("audio")
    category.add("sounds", sounds)
    type.add("minecraftmedialibrary", category)
    println(gson.toJson(type))
    println("===============================")
}

