/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

import java.io.File;

/**
 * A useful interface that is used to specify custom video extraction classes. Used within the
 * library as well.
 */
public interface VideoExtractorBase {

  /**
   * Downloads the specified video given the url. If the video is a Youtube URL and the video id
   * could not be extracted, it will throw an InvalidYoutubeURLException.
   *
   * @return the downloaded video
   */
  File downloadVideo();

  /**
   * Extracts audio from the video just downloaded. If the video was not downloaded, it will prompt
   * to call the downloadVideo() method first and then extract it. By default, this will return an
   * ogg file.
   *
   * @return the extracted audio
   */
  File extractAudio();

  /**
   * This method is called at the beginning of video download. Useful for preparation in advance of
   * the video download.
   */
  void onVideoDownload();

  /**
   * This method is called at the beginning of audio extraction. Useful for preparation in advance
   * of audio extraction.
   */
  void onAudioExtraction();
}
