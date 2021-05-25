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
package io.github.pulsebeat02.minecraftmedialibrary.dithering

import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.FilterLiteDither
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.FloydImageDither
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.StaticDitherInitialization
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.system.exitProcess

class FloydFilterLiteDitherTesting(image: File) : JFrame() {
    private fun ditherSierra(before: BufferedImage): BufferedImage {
        val buffer = VideoUtilities.getBuffer(before)
        FilterLiteDither().dither(buffer, before.width)
        return VideoUtilities.getBufferedImage(buffer, before.width, before.height)
    }

    private fun ditherFloyd(before: BufferedImage): BufferedImage {
        val buffer = VideoUtilities.getBuffer(before)
        FloydImageDither().dither(buffer, before.width)
        return VideoUtilities.getBufferedImage(buffer, before.width, before.height)
    }

    companion object {
        private const val serialVersionUID = 2515798191284509909L

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows: C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png
            // Mac: /Users/bli24/Desktop/platform1/6vv2qz15h7e51.png
            StaticDitherInitialization.init()
            FloydFilterLiteDitherTesting(File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"))
        }
    }

    init {
        val before = ImageIO.read(image)
        val beforeDither = JPanel()
        beforeDither.add(JLabel(ImageIcon(VideoUtilities.resizeImage(before, 500, 250))))
        beforeDither.add(JLabel("Before Dithering"))
        beforeDither.add(JLabel("| Width (before resize): " + before.width))
        beforeDither.add(JLabel("| Height (before resize): " + before.height))
        val floydDithering = JPanel()
        val floydStart = System.currentTimeMillis()
        floydDithering.add(
            JLabel(ImageIcon(VideoUtilities.resizeImage(ditherFloyd(before), 500, 250)))
        )
        val floydEnd = System.currentTimeMillis()
        floydDithering.add(JLabel("Floyd Steinberg Dithering"))
        floydDithering.add(JLabel("| Time (Milliseconds): " + (floydEnd - floydStart)))
        val sierraDithering = JPanel()
        val sierraStart = System.currentTimeMillis()
        sierraDithering.add(
            JLabel(ImageIcon(VideoUtilities.resizeImage(ditherSierra(before), 500, 250)))
        )
        val sierraEnd = System.currentTimeMillis()
        sierraDithering.add(JLabel("Sierra 2-4A Dithering"))
        sierraDithering.add(JLabel("| Time (Milliseconds): " + (sierraEnd - sierraStart)))
        val container = contentPane
        container.layout = GridLayout()
        container.add(beforeDither)
        container.add(floydDithering)
        container.add(sierraDithering)
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    exitProcess(0)
                }
            })
        pack()
        isVisible = true
    }
}