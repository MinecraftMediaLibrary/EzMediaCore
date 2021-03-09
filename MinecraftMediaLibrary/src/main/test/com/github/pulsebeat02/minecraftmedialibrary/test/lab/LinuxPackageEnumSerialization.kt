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
package com.github.pulsebeat02.minecraftmedialibrary.test.lab

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

fun main(args: Array<String>) {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val token = object : TypeToken<Map<String?, Map<String?, Set<LinuxPackage?>?>?>?>() {}.type
}
