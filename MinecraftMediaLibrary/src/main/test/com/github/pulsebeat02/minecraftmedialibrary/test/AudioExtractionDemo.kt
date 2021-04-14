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

package com.github.pulsebeat02.minecraftmedialibrary.test

import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.FFmpegLogCallback
import org.bytedeco.javacv.Frame
import java.io.File


fun main() {
    val path = System.getProperty("user.dir") + "/media/"
    val videoPath = path + "test.mp4"
    val extractAudio = path + "test.ogg"
    try {
        FFmpegLogCallback.set()
        val extractAudioFile = File(extractAudio)
        if (extractAudioFile.exists()) {
            extractAudioFile.delete()
        }
        val recorder = FFmpegFrameRecorder(extractAudio, 2)
        recorder.setAudioOption("crf", "0")
        recorder.audioQuality = 0.0
        recorder.audioBitrate = 192000
        recorder.sampleRate = 44100
        recorder.audioChannels = 2
        recorder.audioCodec = avcodec.AV_CODEC_ID_VORBIS
        recorder.start()
        val grabber = FFmpegFrameGrabber.createDefault(videoPath)
        grabber.start()
        var f: Frame?
        while (grabber.grabSamples().also { f = it } != null) {
            recorder.record(f)
        }
        grabber.stop()
        recorder.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}