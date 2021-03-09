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
import io.github.glytching.junit.extension.system.SystemProperty
import org.junit.jupiter.api.Test
import java.io.File

class MacTest {

    @Test
    @SystemProperty(name = "os.name", value = "Linux")
    fun linuxTest() {
        Logger.setVerbose(true)
        val folder = File(File(System.getProperty("user.dir")).parent + "/vlc")
        if (!folder.exists()) {
            if (folder.mkdir()) {
                println("Made Folder")
            } else {
                println("Could NOY Make Folder")
            }
        }
        val fetcher = VLCNativeDependencyFetcher(folder.absolutePath)
        fetcher.downloadLibraries()
    }
}