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
package io.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import io.github.pulsebeat02.minecraftmedialibrary.vlc.VLCBinarySearcher
import io.github.pulsebeat02.minecraftmedialibrary.vlc.VLCNativeDependencyFetcher
import java.io.File
import java.nio.file.Paths

fun main() {
    MacTest().loadVLC()
}

class MacTest {

    fun installVLC() {
        Logger.setVerbose(false)
        val folder = File(System.getProperty("user.dir"), "/vlc")
        if (!folder.exists()) {
            if (folder.mkdir()) {
                println("Made Folder")
            } else {
                println("Could NOT Make Folder")
            }
        }
        println(folder.absolutePath)
        VLCNativeDependencyFetcher(Paths.get(folder.absolutePath)).downloadLibraries()
    }

    fun loadVLC() {
        println(VLCBinarySearcher(Paths.get("/Applications/VLC.app")).search())
    }

}