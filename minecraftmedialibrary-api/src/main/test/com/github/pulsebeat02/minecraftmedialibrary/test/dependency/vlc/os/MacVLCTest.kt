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

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import com.github.pulsebeat02.minecraftmedialibrary.utility.VLCUtilities
import com.github.pulsebeat02.minecraftmedialibrary.vlc.VLCNativeDependencyFetcher
import java.io.File
import java.nio.file.Paths

fun main() {
    MacTest().loadVLC()
}

class MacTest {

    fun installVLC() {
        Logger.setVerbose(false)
        val folder = File(System.getProperty("user.dir"), "/vlc")
        if (!folder.exists()) {
            if (folder.mkdir()) {
                println("Made Folder")
            } else {
                println("Could NOT Make Folder")
            }
        }
        println(folder.absolutePath)
        VLCNativeDependencyFetcher(Paths.get(folder.absolutePath)).downloadLibraries()
    }

    fun loadVLC() {
        println(VLCUtilities.checkVLCExistence(File("/Applications/VLC.app")))
    }

}