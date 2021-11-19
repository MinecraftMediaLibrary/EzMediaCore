/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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

package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;
import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.fetchResponseLines;

import com.sedmelluq.discord.lavaplayer.container.playlists.ExtendedM3uParser;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class M3uStreamSegmentUrlProvider {

  private static final long SEGMENT_WAIT_STEP_MS = 200;

  private SegmentInfo lastSegment;

  public M3uStreamSegmentUrlProvider(@NotNull final String url) {
  }

  abstract String fetchSegmentPlaylistUrl(@NotNull final HttpInterface httpInterface)
      ;

  private @Nullable String getNextSegmentUrl(@NotNull final HttpInterface httpInterface) {
    try {
      final String streamSegmentPlaylistUrl = this.fetchSegmentPlaylistUrl(httpInterface);
      if (streamSegmentPlaylistUrl == null) {
        return null;
      }
      final long startTime = System.currentTimeMillis();
      SegmentInfo nextSegment;
      while (true) {
        final List<SegmentInfo> segments =
            this.loadStreamSegmentsList(httpInterface, streamSegmentPlaylistUrl);
        nextSegment = this.chooseNextSegment(segments, this.lastSegment);
        if (nextSegment != null || !this.shouldWaitForSegment(startTime, segments)) {
          break;
        }
        //noinspection BusyWait
        Thread.sleep(SEGMENT_WAIT_STEP_MS);
      }
      if (nextSegment == null) {
        return null;
      }
      this.lastSegment = nextSegment;
      return URI.create(streamSegmentPlaylistUrl).resolve(this.lastSegment.url).toString();
    } catch (final IOException e) {
      throw new FriendlyException("Failed to get next part of the stream.", SUSPICIOUS, e);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public InputStream getNextSegmentStream(@NotNull final HttpInterface httpInterface) {
    final String url = this.getNextSegmentUrl(httpInterface);
    if (url == null) {
      return null;
    }
    CloseableHttpResponse response = null;
    boolean success = false;
    try {
      response = httpInterface.execute(new HttpGet(url));
      HttpClientTools.assertSuccessWithContent(response, "segment data URL");
      success = true;
      return response.getEntity().getContent();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null && !success) {
        ExceptionTools.closeWithWarnings(response);
      }
    }
  }

  private @NotNull List<ChannelStreamInfo> loadChannelStreamsList(
      @NotNull final String @NotNull [] lines) {
    ExtendedM3uParser.Line streamInfoLine = null;
    final List<ChannelStreamInfo> streams = new ArrayList<>();
    for (final String lineText : lines) {
      final ExtendedM3uParser.Line line = ExtendedM3uParser.parseLine(lineText);
      if (line.isData() && streamInfoLine != null) {
        streams.add(new ChannelStreamInfo("default", line.lineData));
        streamInfoLine = null;
      } else if (line.isDirective() && "EXT-X-STREAM-INF".equals(line.directiveName)) {
        streamInfoLine = line;
      }
    }

    return streams;
  }

  private @NotNull List<SegmentInfo> loadStreamSegmentsList(
      @NotNull final HttpInterface httpInterface, @NotNull final String streamSegmentPlaylistUrl)
      throws IOException {
    final List<SegmentInfo> segments = new ArrayList<>();
    ExtendedM3uParser.Line segmentInfo = null;
    for (final String lineText :
        fetchResponseLines(
            httpInterface, new HttpGet(streamSegmentPlaylistUrl), "stream segments list")) {
      final ExtendedM3uParser.Line line = ExtendedM3uParser.parseLine(lineText);
      if (line.isDirective() && "EXTINF".equals(line.directiveName)) {
        segmentInfo = line;
      }
      if (line.isData()) {
        if (segmentInfo != null && segmentInfo.extraData.contains(",")) {
          final String[] fields = segmentInfo.extraData.split(",", 2);
          segments.add(
              new SegmentInfo(line.lineData, this.parseSecondDuration(fields[0]), fields[1]));
        } else {
          segments.add(new SegmentInfo(line.lineData, null, null));
        }
      }
    }
    return segments;
  }

  private @Nullable Long parseSecondDuration(@NotNull final String value) {
    try {
      return (long) (Double.parseDouble(value) * 1000.0);
    } catch (final NumberFormatException ignored) {
      return null;
    }
  }

  private SegmentInfo chooseNextSegment(
      @NotNull final List<SegmentInfo> segments, @Nullable final SegmentInfo lastSegment) {
    SegmentInfo selected = null;
    for (int i = segments.size() - 1; i >= 0; i--) {
      final SegmentInfo current = segments.get(i);
      if (lastSegment != null && current.url.equals(lastSegment.url)) {
        break;
      }
      selected = current;
    }
    return selected;
  }

  private boolean shouldWaitForSegment(
      final long startTime, @NotNull final List<SegmentInfo> segments) {
    if (!segments.isEmpty()) {
      final SegmentInfo sampleSegment = segments.get(0);
      if (sampleSegment.duration != null) {
        return System.currentTimeMillis() - startTime < sampleSegment.duration;
      }
    }
    return false;
  }

  record ChannelStreamInfo(String quality, String url) {

  }

  record SegmentInfo(String url, Long duration, String name) {

  }
}
