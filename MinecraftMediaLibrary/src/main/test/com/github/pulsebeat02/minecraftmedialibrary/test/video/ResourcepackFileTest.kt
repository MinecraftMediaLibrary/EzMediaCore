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
package com.github.pulsebeat02.minecraftmedialibrary.test.video

import com.google.gson.JsonObject

fun main(args: Array<String>) {
    println(generateJSON())
}

private fun generateJSON(): String {
    val format = JsonObject()
    format.addProperty("format", 6)
    val description = JsonObject()
    description.addProperty("description", "Example Pack")
    val pack = JsonObject()
    pack.add("pack", format)
    pack.add("pack", description)
    return pack.toString()
}
