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
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.OrderedDithering
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.OrderedDithering.DitherType
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.*

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
            OrderedDithering(DitherType.ModeTwo).dither(buffer, before.width)
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
                    System.exit(0)
                }
            })
        pack()
        isVisible = true
    }
}