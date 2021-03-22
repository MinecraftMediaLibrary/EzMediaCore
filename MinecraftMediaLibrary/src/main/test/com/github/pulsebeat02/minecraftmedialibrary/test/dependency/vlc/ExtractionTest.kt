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

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc

import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities
import java.io.File

fun main(args: Array<String>) {
    val f = File("/Users/bli24/Desktop/VLC")
    if (!f.isDirectory) {
        f.mkdir()
    }
    ArchiveUtilities.decompressArchive(File("/Users/bli24/Desktop/VLC.zip"), f)
}