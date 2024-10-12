/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoDownloader;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.YoutubeVideoDownloader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;



public class YoutubeOGGAudioExtractor extends FFmpegCommandExecutor
    implements YoutubeAudioExtractor {

  private final YoutubeVideoDownloader downloader;
  private final OGGAudioExtractor extractor;
  private final AtomicBoolean cancelled;

  YoutubeOGGAudioExtractor(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final String url,
       final Path output) {
    super(core);
    final Path path = core.getVideoPath().resolve("%s.mp4".formatted(UUID.randomUUID()));
    this.downloader = YoutubeVideoDownloader.ofYoutubeVideoDownloader(url, path);
    this.extractor = OGGAudioExtractor.ofFFmpegAudioExtractor(core, configuration, path, output);
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract("_, _, _, _ -> new")
  public static  YoutubeOGGAudioExtractor ofYoutubeVideoAudioExtractor(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final String url,
       final Path output) {
    return new YoutubeOGGAudioExtractor(core, configuration, url, output);
  }

  @Contract("_, _, _, _ -> new")
  public static  YoutubeOGGAudioExtractor ofYoutubeVideoAudioExtractor(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final String url,
       final String fileName) {
    return ofYoutubeVideoAudioExtractor(
        core, configuration, url, core.getAudioPath().resolve(fileName));
  }

  @Override
  public void executeWithLogging( final Consumer<String> logger) throws IOException {
    this.onStartAudioExtraction();
    this.downloader.downloadVideo(
        this.downloader.getVideo().getVideoFormats().get(0).getQuality(), true);
    this.extractor.executeWithLogging(logger);
    this.onFinishAudioExtraction();
  }

  @Override
  public void close() throws InterruptedException {
    this.onDownloadCancellation();
    this.cancelled.set(true);
    this.downloader.cancelDownload();
    this.extractor.close();
  }

  @Override
  public void onStartAudioExtraction() {}

  @Override
  public void onFinishAudioExtraction() {}

  @Override
  public void onDownloadCancellation() {}

  @Override
  public  VideoDownloader getDownloader() {
    return this.downloader;
  }

  @Override
  public  AudioExtractor getExtractor() {
    return this.extractor;
  }
}
