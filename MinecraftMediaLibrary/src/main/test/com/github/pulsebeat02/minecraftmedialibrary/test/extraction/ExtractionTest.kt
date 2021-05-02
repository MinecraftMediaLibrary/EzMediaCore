/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/21/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.extraction

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

fun main() {
    Logger.info("Running Command Chain! (Setup)")
    val junest = File("/Users/bli24/Desktop/junest-7.3.7.zip")
    FileUtils.copyURLToFile(
        URL("https://github.com/MinecraftMediaLibrary/JuNest-Mirror/raw/main/junest-7.3.7.zip"), junest
    )
    ArchiveUtilities.decompressArchive(junest, junest.parentFile)
}