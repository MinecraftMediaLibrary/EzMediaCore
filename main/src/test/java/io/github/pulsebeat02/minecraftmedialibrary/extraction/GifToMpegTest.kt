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

package io.github.pulsebeat02.minecraftmedialibrary.extraction

import org.apache.commons.io.FilenameUtils
import ws.schild.jave.Encoder
import ws.schild.jave.EncoderException
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import java.io.File

fun main() {
    val image = File("/Users/bli24/Desktop/test.gif")
    val name: String = image.name
    val encoder = Encoder()
    val audio = AudioAttributes()
    audio.setVolume(0)
    val attrs = EncodingAttributes()
    attrs.setInputFormat(FilenameUtils.getExtension(name))
    attrs.setOutputFormat("mp4")
    attrs.setAudioAttributes(audio)
    val output = File("/Users/bli24/Desktop/", FilenameUtils.getBaseName(name) + ".mp4")
    try {
        encoder.encode(MultimediaObject(image), output, attrs)
    } catch (e: EncoderException) {
        e.printStackTrace()
    }
}