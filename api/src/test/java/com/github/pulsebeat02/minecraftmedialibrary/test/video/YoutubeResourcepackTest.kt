/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/
package com.github.pulsebeat02.minecraftmedialibrary.test.video

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary
import com.github.pulsebeat02.minecraftmedialibrary.concurrent.AsyncVideoExtraction
import com.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction
import com.github.pulsebeat02.minecraftmedialibrary.image.basic.MinecraftStaticImage
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

    private var library: MediaLibrary? = null

    override fun onEnable() {
        library = MediaLibrary(this)
    }

    fun getResourcepackUrlYoutube(youtubeUrl: String, directory: String, port: Int): String {
        val extraction: YoutubeExtraction = object : YoutubeExtraction(
            youtubeUrl, directory, ExtractionSetting.builder().build()
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
        val wrapper = ResourcepackWrapper.builder()
            .setAudio(extraction.audio)
            .setDescription("Youtube Video: " + extraction.videoTitle)
            .setPath(directory)
            .setPackFormat(6)
            .build(library)
        wrapper.buildResourcePack()
        val hosting = HttpDaemonProvider(directory, port)
        hosting.startServer()
        return hosting.generateUrl(Paths.get(directory))
    }

    @Throws(IOException::class)
    fun displayImage(map: Int, image: File) {
        val bi = ImageIO.read(image)
        val imageMap = MinecraftStaticImage.builder()
            .setMap(map)
            .setWidth(bi.width)
            .setHeight(bi.height)
            .build(library)
        imageMap.drawImage()
    }
}