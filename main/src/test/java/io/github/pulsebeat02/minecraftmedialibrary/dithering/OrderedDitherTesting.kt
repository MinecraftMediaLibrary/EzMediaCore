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

import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.OrderedDithering
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.OrderedDithering.DitherType
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess

class OrderedDitherTesting(image: File) : JFrame() {
    companion object {
        private const val serialVersionUID = -47246908626887986L

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows: C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png
            // Mac: /Users/bli24/Desktop/platform1/6vv2qz15h7e51.png
            OrderedDitherTesting(File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"))
            // new OrderedDitherTesting(new File("C:\\Users\\Brandon
            // Li\\Desktop\\0923e57e357298233d20cb38cccc8e7c.png"));
        }
    }

    init {
        val before = ImageIO.read(image)
        val c = contentPane
        c.layout = BoxLayout(c, BoxLayout.Y_AXIS)
        val beforeDither = JPanel()
        beforeDither.add(JLabel(ImageIcon(VideoUtilities.resizeImage(before, 500, 250))))
        beforeDither.add(JLabel("Before Dithering"))
        beforeDither.add(JLabel("| Width (before resize): " + before.width))
        beforeDither.add(JLabel("| Height (before resize): " + before.height))
        c.add(beforeDither)
        for (type in DitherType.values()) {
            val buffer = VideoUtilities.getBuffer(before)
            val start = System.currentTimeMillis()
            OrderedDithering(DitherType.TWO_BY_TWO).dither(buffer, before.width)
            val end = System.currentTimeMillis()
            val after = VideoUtilities.getBufferedImage(buffer, before.width, before.height)
            val panel = JPanel()
            panel.add(JLabel(ImageIcon(VideoUtilities.resizeImage(after, 300, 250))))
            panel.add(JLabel("Bayer Matrix (Ordered) Dithering (" + type.getName() + ")"))
            panel.add(JLabel("| Time (Milliseconds): " + (end - start)))
            panel.isVisible = true
            c.add(panel)
        }
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