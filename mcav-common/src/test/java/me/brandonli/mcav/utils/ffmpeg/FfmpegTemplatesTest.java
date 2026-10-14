/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.utils.ffmpeg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.brandonli.mcav.media.player.metadata.OriginalAudioMetadata;
import me.brandonli.mcav.media.source.file.FileSource;
import me.brandonli.mcav.testing.TestMedia;
import me.brandonli.mcav.testing.UtilityClassAssertions;
import me.brandonli.mcav.utils.MetadataUtils;
import me.brandonli.mcav.utils.runtime.CommandTask;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link FfmpegTemplates}.
 */
final class FfmpegTemplatesTest {

  private static final String INPUT_NAME = "in.mp4";
  private static final String MATROSKA_INPUT_NAME = "in.mkv";
  private static final String OUTPUT_NAME = "out.mp4";
  private static final String AUDIO_OUTPUT_NAME = "out.opus";
  private static final String OGG_OUTPUT_NAME = "audio.ogg";
  private static final String THUMBNAIL_NAME = "thumb.png";

  private static final String PLACEHOLDER_INPUT = "i";
  private static final String PLACEHOLDER_OUTPUT = "o";

  private static final String VIDEO_BITRATE = "1M";

  @TempDir
  private Path directory;

  @Test
  void shouldKeepOnlyTheAudioTrackWhenExtractingAudio() {
    final FfmpegCommand command = FfmpegTemplates.extractAudio(INPUT_NAME, "libopus", AUDIO_OUTPUT_NAME);
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of("-i", INPUT_NAME, "-vn", "-strict", "-2", "-c:a", "libopus", "-y", AUDIO_OUTPUT_NAME);
    assertEquals(expected, arguments);
  }

  @Test
  void shouldProduceAPlayableFileWhenExtractingOggVorbis() throws IOException {
    final Path video = TestMedia.video();
    final Path output = this.directory.resolve(OGG_OUTPUT_NAME);
    final String videoPath = video.toString();
    final String outputPath = output.toString();
    final FfmpegCommand command = FfmpegTemplates.extractOggVorbis(videoPath, outputPath);
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of("-i", videoPath, "-vn", "-strict", "-2", "-c:a", "vorbis", "-ac", "2", "-y", outputPath);
    final CommandTask task = command.createTask();
    final int exitCode = task.run();
    final String errorOutput = task.getErrorOutput();
    final long size = Files.size(output);
    assertEquals(expected, arguments);
    assertEquals(0, exitCode, errorOutput);

    final boolean holdsAudio = size > 1_000;
    assertTrue(holdsAudio, "the file holds encoded audio");

    final FileSource source = FileSource.path(output);
    final OriginalAudioMetadata metadata = MetadataUtils.parseAudioMetadata(source);
    final int channels = metadata.getAudioChannels();
    final String codec = metadata.getAudioCodec();
    final int sampleRate = metadata.getAudioSampleRate();
    assertEquals(2, channels, "the audio is stereo as requested");

    final boolean isVorbis = codec.contains("vorbis");
    assertTrue(isVorbis, codec);

    final boolean decodable = sampleRate > 0;
    assertTrue(decodable, "the file can be decoded");

    long decodedSamples = 0;
    try (final FFmpegFrameGrabber decoder = new FFmpegFrameGrabber(output.toFile())) {
      decoder.start();
      Frame frame = decoder.grabSamples();
      while (frame != null) {
        assertNotNull(frame.samples);
        assertEquals(2, frame.audioChannels);
        assertEquals(sampleRate, frame.sampleRate);
        for (final Buffer samples : frame.samples) {
          decodedSamples += samples.remaining();
        }
        frame = decoder.grabSamples();
      }
    }

    final long decodedFrames = decodedSamples / channels;
    final long expectedFrames = (long) TestMedia.VIDEO_SECONDS * sampleRate;
    // AAC and Vorbis may pad the source and destination by a codec block.
    final boolean withinACodecBlock = Math.abs(decodedFrames - expectedFrames) <= 2048;
    assertTrue(withinACodecBlock, "decoded sample frames: " + decodedFrames);
  }

  @Test
  void shouldSetBothBitratesWhenCompressingVideo() {
    final FfmpegCommand command = FfmpegTemplates.compressVideo(INPUT_NAME, OUTPUT_NAME, VIDEO_BITRATE, "96k");
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of(
      "-i",
      INPUT_NAME,
      "-c:v",
      "libx264",
      "-b:v",
      VIDEO_BITRATE,
      "-c:a",
      "aac",
      "-b:a",
      "96k",
      "-y",
      OUTPUT_NAME
    );
    assertEquals(expected, arguments);
  }

  @Test
  void shouldSeekBeforeTheInputWhenExtractingAClip() {
    final FfmpegCommand command = FfmpegTemplates.extractClip(INPUT_NAME, OUTPUT_NAME, "00:00:10", "5");
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of(
      "-ss",
      "00:00:10",
      "-i",
      INPUT_NAME,
      "-t",
      "5",
      "-c:v",
      "copy",
      "-c:a",
      "copy",
      "-y",
      OUTPUT_NAME
    );
    assertEquals(expected, arguments);
  }

  @Test
  void shouldWriteOneFrameWhenCreatingAThumbnail() {
    final FfmpegCommand command = FfmpegTemplates.createThumbnail(INPUT_NAME, THUMBNAIL_NAME, "3");
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of("-ss", "3", "-i", INPUT_NAME, "-frames:v", "1", "-y", THUMBNAIL_NAME);
    assertEquals(expected, arguments);
  }

  @Test
  void shouldCopyBothStreamsWhenRemuxingVideo() {
    final FfmpegCommand command = FfmpegTemplates.remuxVideo(MATROSKA_INPUT_NAME, OUTPUT_NAME);
    final List<String> arguments = command.getArguments();
    final List<String> expected = List.of("-i", MATROSKA_INPUT_NAME, "-c:v", "copy", "-c:a", "copy", "-y", OUTPUT_NAME);
    assertEquals(expected, arguments);
  }

  @Test
  void shouldRejectMissingArguments() {
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractAudio(null, "aac", PLACEHOLDER_OUTPUT));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractAudio(PLACEHOLDER_INPUT, null, PLACEHOLDER_OUTPUT));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractAudio(PLACEHOLDER_INPUT, "aac", null));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractOggVorbis(null, PLACEHOLDER_OUTPUT));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractOggVorbis(PLACEHOLDER_INPUT, null));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.compressVideo(null, PLACEHOLDER_OUTPUT, VIDEO_BITRATE, "1k"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.compressVideo(PLACEHOLDER_INPUT, null, VIDEO_BITRATE, "1k"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.compressVideo(PLACEHOLDER_INPUT, PLACEHOLDER_OUTPUT, null, "1k"));
    assertThrows(NullPointerException.class, () ->
      FfmpegTemplates.compressVideo(PLACEHOLDER_INPUT, PLACEHOLDER_OUTPUT, VIDEO_BITRATE, null)
    );
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractClip(null, PLACEHOLDER_OUTPUT, "0", "1"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractClip(PLACEHOLDER_INPUT, null, "0", "1"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractClip(PLACEHOLDER_INPUT, PLACEHOLDER_OUTPUT, null, "1"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.extractClip(PLACEHOLDER_INPUT, PLACEHOLDER_OUTPUT, "0", null));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.createThumbnail(null, PLACEHOLDER_OUTPUT, "0"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.createThumbnail(PLACEHOLDER_INPUT, null, "0"));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.createThumbnail(PLACEHOLDER_INPUT, PLACEHOLDER_OUTPUT, null));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.remuxVideo(null, PLACEHOLDER_OUTPUT));
    assertThrows(NullPointerException.class, () -> FfmpegTemplates.remuxVideo(PLACEHOLDER_INPUT, null));
  }

  @Test
  void shouldNotBeInstantiated() throws ReflectiveOperationException {
    UtilityClassAssertions.assertNotInstantiable(FfmpegTemplates.class);
  }
}
