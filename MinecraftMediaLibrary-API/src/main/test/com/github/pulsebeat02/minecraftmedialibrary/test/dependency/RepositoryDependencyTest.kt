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
package com.github.pulsebeat02.minecraftmedialibrary.test.dependency

import com.github.kiulian.downloader.cipher.Cipher
import com.github.kiulian.downloader.cipher.CipherFactory
import com.github.kiulian.downloader.cipher.CipherFunction
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement
import com.github.pulsebeat02.minecraftmedialibrary.dependency.FFmpegDependencyInstallation
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import java.nio.file.Paths

fun main() {
    Logger.setVerbose(true)
    val management = DependencyManagement(Paths.get(""))
    management.install()
    management.relocate()
    management.load()
    val ffmpeg =
        FFmpegDependencyInstallation(Paths.get(""))
    ffmpeg.install()
    object : CipherFactory {
        override fun createCipher(s: String): Cipher? {
            return null
        }

        override fun addInitialFunctionPattern(i: Int, s: String) {}
        override fun addFunctionEquivalent(s: String, cipherFunction: CipherFunction) {}
    }
}
