/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/3/2021
 * ============================================================================
 */
package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc

import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities
import java.io.File

fun main() {
    val prop = System.getProperty("user.dir")
    val f = File("$prop/extraction/test.deb")
    ArchiveUtilities.recursiveExtraction(
        f, f.parentFile
    )

}
