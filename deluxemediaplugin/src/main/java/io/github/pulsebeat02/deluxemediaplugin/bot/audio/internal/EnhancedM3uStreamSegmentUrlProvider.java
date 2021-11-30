package io.github.pulsebeat02.deluxemediaplugin.bot.audio.internal;

import static com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.fetchResponseLines;

import com.sedmelluq.discord.lavaplayer.container.playlists.ExtendedM3uParser;
import com.sedmelluq.discord.lavaplayer.container.playlists.ExtendedM3uParser.Line;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EnhancedM3uStreamSegmentUrlProvider {

  private static final long SEGMENT_WAIT_STEP_MS = 200;

  protected SegmentInfo lastSegment;

  private @NotNull String createSegmentUrl(
      @NotNull final String playlistUrl, @NotNull final String segmentName) {
    return URI.create(playlistUrl).resolve(segmentName).toString();
  }

  protected abstract @Nullable String getQualityFromM3uDirective(@NotNull final Line directiveLine);

  protected abstract @Nullable String fetchSegmentPlaylistUrl(
      @NotNull final HttpInterface httpInterface) throws IOException;

  protected abstract HttpUriRequest createSegmentGetRequest(@NotNull final String url);

  protected @Nullable String getNextSegmentUrl(@NotNull final HttpInterface httpInterface) {

    try {

      final String streamSegmentPlaylistUrl = this.fetchSegmentPlaylistUrl(httpInterface);
      if (streamSegmentPlaylistUrl == null) {
        return null;
      }

      final long startTime = Instant.now().toEpochMilli();

      SegmentInfo nextSegment;

      while (true) {
        final List<SegmentInfo> segments =
            this.loadStreamSegmentsList(httpInterface, streamSegmentPlaylistUrl);
        nextSegment = this.chooseNextSegment(segments, this.lastSegment);
        if (this.reachedEndSegments(startTime, nextSegment, segments)) {
          break;
        }
        this.sleep();
      }

      if (nextSegment == null) {
        return null;
      }

      this.lastSegment = nextSegment;

      return this.createSegmentUrl(streamSegmentPlaylistUrl, this.lastSegment.url);

    } catch (final IOException e) {
      throw new RuntimeException("Failed to get next part of the stream!");
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void sleep() throws InterruptedException {
    Thread.sleep(SEGMENT_WAIT_STEP_MS);
  }

  private boolean reachedEndSegments(
      final long startTime,
      @Nullable final SegmentInfo nextSegment,
      @NotNull final List<SegmentInfo> segments) {
    return nextSegment != null || !this.shouldWaitForSegment(startTime, segments);
  }

  public InputStream getNextSegmentStream(@NotNull final HttpInterface httpInterface) {

    final String url = this.getNextSegmentUrl(httpInterface);
    if (url == null) {
      return null;
    }

    CloseableHttpResponse response = null;
    boolean success = false;

    try {
      response = httpInterface.execute(this.createSegmentGetRequest(url));
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

  @NotNull
  private List<ChannelStreamInfo> loadChannelStreamsList(final String @NotNull [] lines) {
    final List<ChannelStreamInfo> streams = new ArrayList<>();
    for (final String lineText : lines) {
      this.handleM3u8StreamLine(streams, lineText);
    }
    return streams;
  }

  private void handleM3u8StreamLine(
      @NotNull final List<ChannelStreamInfo> streams, @NotNull final String lineText) {
    final Line line = ExtendedM3uParser.parseLine(lineText);
    Line streamInfoLine = null;
    if (this.isValidStreamData(line, streamInfoLine)) {
      this.addChannelStreamInfo(streamInfoLine, streams, line);
      streamInfoLine = null;
    } else if (this.isValidStreamInfo(line)) {
      streamInfoLine = line;
    }
  }

  private void addChannelStreamInfo(
      @NotNull final Line streamInfoLine,
      @NotNull final List<ChannelStreamInfo> streams,
      @NotNull final Line line) {
    final String quality = this.getQualityFromM3uDirective(streamInfoLine);
    if (quality != null) {
      streams.add(new ChannelStreamInfo(quality, line.lineData));
    }
  }

  private boolean isValidStreamInfo(@NotNull final Line line) {
    return line.isDirective() && "EXT-X-STREAM-INF".equals(line.directiveName);
  }

  private boolean isValidStreamData(@NotNull final Line line, @Nullable final Line streamInfoLine) {
    return line.isData() && streamInfoLine != null;
  }

  @NotNull
  private List<SegmentInfo> loadStreamSegmentsList(
      @NotNull final HttpInterface httpInterface, @NotNull final String streamSegmentPlaylistUrl)
      throws IOException {
    final List<SegmentInfo> segments = new ArrayList<>();
    final String[] lines = this.getM3u8Response(httpInterface, streamSegmentPlaylistUrl);
    for (final String lineText : lines) {
      this.handleM3u8Line(segments, lineText);
    }
    return segments;
  }

  @NotNull
  private Line handleM3u8Line(final List<SegmentInfo> segments, final String lineText) {
    final Line line = ExtendedM3uParser.parseLine(lineText);
    final Line segmentInfo = this.handleSegmentPortion(line);
    this.handleDataPortion(segments, line, segmentInfo);
    return segmentInfo;
  }

  private @Nullable Line handleSegmentPortion(@NotNull final Line line) {
    if (this.isSegmentInfo(line)) {
      return line;
    }
    return null;
  }

  private void handleDataPortion(
      @NotNull final List<SegmentInfo> segments,
      @NotNull final Line line,
      @NotNull final Line segmentInfo) {
    if (this.isDataInfo(line)) {
      if (this.isValidData(segmentInfo)) {
        this.addDetailedM3u8Segment(segments, segmentInfo, line);
      } else {
        this.addSimpleM3u8Segment(segments, line);
      }
    }
  }

  private @NotNull String[] getM3u8Response(
      @NotNull final HttpInterface httpInterface, @NotNull final String streamSegmentPlaylistUrl)
      throws IOException {
    return fetchResponseLines(
        httpInterface, new HttpGet(streamSegmentPlaylistUrl), "M3U Stream Segments");
  }

  private void addSimpleM3u8Segment(
      @NotNull final List<SegmentInfo> segments, @NotNull final Line line) {
    segments.add(new SegmentInfo(line.lineData, null, null));
  }

  private void addDetailedM3u8Segment(
      @NotNull final List<SegmentInfo> segments,
      @NotNull final Line segmentInfo,
      @NotNull final Line line) {
    final String[] fields = segmentInfo.extraData.split(",", 2);
    final String url = line.lineData;
    final String duration = fields[0];
    final String name = fields[1];
    segments.add(new SegmentInfo(url, this.parseSecondDuration(duration), name));
  }

  private boolean isValidData(@Nullable final ExtendedM3uParser.Line line) {
    return line != null && line.extraData.contains(",");
  }

  private boolean isDataInfo(@NotNull final ExtendedM3uParser.Line line) {
    return line.isData();
  }

  private boolean isSegmentInfo(@NotNull final ExtendedM3uParser.Line line) {
    return line.isDirective() && line.directiveName.equals("EXTINF");
  }

  private @Nullable Long parseSecondDuration(@NotNull final String value) {
    try {
      final double asDouble = Double.parseDouble(value);
      return (long) (asDouble * 1000.0);
    } catch (final NumberFormatException ignored) {
      return null;
    }
  }

  @Nullable
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
        return Instant.now().toEpochMilli() - startTime < sampleSegment.duration;
      }
    }
    return false;
  }

  protected static class ChannelStreamInfo {

    public final String quality;
    public final String url;

    private ChannelStreamInfo(@NotNull final String quality, @NotNull final String url) {
      this.quality = quality;
      this.url = url;
    }
  }

  protected static class SegmentInfo {

    public final String url;
    public final Long duration;
    public final String name;

    public SegmentInfo(
        @NotNull final String url, @Nullable final Long duration, @Nullable final String name) {
      this.url = url;
      this.duration = duration;
      this.name = name;
    }
  }
}
