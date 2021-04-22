package com.github.pulsebeat02.minecraftmedialibrary.test.dithering

import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.FilterLiteDither
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.StaticDitherInitialization
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.development.DynamicIntegerDithering
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities
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

@SuppressWarnings("deprecated")
class PulseDitherTest(image: File) : JFrame() {
    private fun ditherSierra(before: BufferedImage): BufferedImage {
        val buffer = VideoUtilities.getBuffer(before)
        FilterLiteDither().dither(buffer, before.width)
        return VideoUtilities.getBufferedImage(buffer, before.width, before.height)
    }

    private fun ditherPulse(before: BufferedImage): BufferedImage {
        val buffer = VideoUtilities.getBuffer(before)
        DynamicIntegerDithering()
            .dither(buffer, before.width)
        return VideoUtilities.getBufferedImage(buffer, before.width, before.height)
    }

    companion object {
        private const val serialVersionUID = 3726338370660165985L

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows: C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png
            // Mac: /Users/bli24/Desktop/platform1/6vv2qz15h7e51.png
            StaticDitherInitialization.init()
            DynamicIntegerDithering.init()
            FilterLiteDither.init()
            PulseDitherTest(File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"))
        }
    }

    init {
        val before = ImageIO.read(image)
        val beforeDither = JPanel()
        beforeDither.add(JLabel(ImageIcon(VideoUtilities.resizeImage(before, 500, 250))))
        beforeDither.add(JLabel("Before Dithering"))
        beforeDither.add(JLabel("| Width (before resize): " + before.width))
        beforeDither.add(JLabel("| Height (before resize): " + before.height))
        val pulseDithering = JPanel()
        val floydStart = System.currentTimeMillis()
        pulseDithering.add(
            JLabel(ImageIcon(VideoUtilities.resizeImage(ditherPulse(before), 500, 250)))
        )
        val floydEnd = System.currentTimeMillis()
        pulseDithering.add(JLabel("PulseBeat_02's Dithering"))
        pulseDithering.add(JLabel("| Time (Milliseconds): " + (floydEnd - floydStart)))
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
        container.add(pulseDithering)
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