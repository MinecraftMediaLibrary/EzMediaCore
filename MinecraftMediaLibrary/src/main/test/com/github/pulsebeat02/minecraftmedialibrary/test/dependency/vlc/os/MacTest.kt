/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/4/2021
 * ============================================================================
 */
package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc.os

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import java.io.File

fun main(args: Array<String>) {
    MacTest().macTest();
}

class MacTest {

    fun macTest() {
        Logger.setVerbose(true)
        val folder = File("/Applications/")
        if (!folder.exists()) {
            if (folder.mkdir()) {
                println("Made Folder")
            } else {
                println("Could NOT Make Folder")
            }
        }
        println(folder.absolutePath)
        VLCNativeDependencyFetcher("/Applications/").downloadLibraries()
    }
}