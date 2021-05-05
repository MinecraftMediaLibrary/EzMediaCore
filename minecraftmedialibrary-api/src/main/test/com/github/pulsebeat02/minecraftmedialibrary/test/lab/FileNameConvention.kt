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

import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities
import org.apache.commons.io.FilenameUtils

fun main() {
    println(FilenameUtils.getExtension("gradlew.bat"))
    println(FilenameUtils.getExtension("test.tar.gz"))
    println(ArchiveUtilities.getFileName("x.tar"))
}
