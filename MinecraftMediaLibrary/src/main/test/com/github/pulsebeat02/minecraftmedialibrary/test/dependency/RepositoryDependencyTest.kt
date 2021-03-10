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
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger

fun main(args: Array<String>) {
    Logger.setVerbose(true)
    val jave = JaveDependencyInstallation("")
    jave.install()
    jave.load()
    val management = DependencyManagement("")
    management.install()
    management.relocate()
    management.load()
    object : CipherFactory {
        override fun createCipher(s: String): Cipher? {
            return null
        }

        override fun addInitialFunctionPattern(i: Int, s: String) {}
        override fun addFunctionEquivalent(s: String, cipherFunction: CipherFunction) {}
    }
}
