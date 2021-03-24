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
package com.github.pulsebeat02.minecraftmedialibrary.test.video

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary
import com.github.pulsebeat02.minecraftmedialibrary.concurrent.AsyncVideoExtraction
import com.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import javax.imageio.ImageIO

class YoutubeResourcepackTest : JavaPlugin() {
    private var library: MinecraftMediaLibrary? = null
    override fun onEnable() {
        library = MinecraftMediaLibrary(this, dataFolder.path, true)
    }

    fun getResourcepackUrlYoutube(
        youtubeUrl: String, directory: String, port: Int
    ): String {
        val extraction: YoutubeExtraction = object : YoutubeExtraction(
            youtubeUrl, directory, ExtractionSetting.Builder().createExtractionSetting()
        ) {
            override fun onVideoDownload() {
                println("Video is Downloading!")
            }

            override fun onAudioExtraction() {
                println("Audio is being extracted from Video!")
            }
        }
        val executor = Executors.newCachedThreadPool()
        CompletableFuture.runAsync({ AsyncVideoExtraction(extraction).extractAudio() }, executor)
        CompletableFuture.runAsync(
            { AsyncVideoExtraction(extraction).downloadVideo() }, executor
        )
        val wrapper = ResourcepackWrapper.Builder()
            .setAudio(extraction.audio)
            .setDescription("Youtube Video: " + extraction.videoTitle)
            .setPath(directory)
            .setPackFormat(6)
            .createResourcepackHostingProvider(library)
        wrapper.buildResourcePack()
        val hosting = HttpDaemonProvider(directory, port)
        hosting.startServer()
        return hosting.generateUrl(Paths.get(directory))
    }

    @Throws(IOException::class)
    fun displayImage(map: Int, image: File) {
        val bi = ImageIO.read(image)
        val imageMap = MapImage.Builder()
            .setMap(map)
            .setWidth(bi.width)
            .setHeight(bi.height)
            .createImageMap(library)
        imageMap.drawImage()
    }
}