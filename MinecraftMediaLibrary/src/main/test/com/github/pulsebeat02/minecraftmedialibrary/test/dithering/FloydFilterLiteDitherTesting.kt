/*
 * ============================================================================
 *  Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 *  This file is part of MinecraftMediaLibrary
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *
 *  Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */
package com.github.pulsebeat02.minecraftmedialibrary.test.dithering

import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.FilterLiteDither
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.FloydImageDither
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.StaticDitherInitialization
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
                    System.exit(0)
                }
            })
        pack()
        isVisible = true
    }
}