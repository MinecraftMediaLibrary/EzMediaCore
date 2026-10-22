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
package me.brandonli.mcav.voicechat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.function.Supplier;
import me.brandonli.mcav.media.player.metadata.OriginalAudioMetadata;
import me.brandonli.mcav.media.player.pipeline.filter.audio.AudioFilter;
import me.brandonli.mcav.utils.audio.MonoDownmixer;
import org.junit.jupiter.api.Tag;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@Tag("fuzz")
final class EntityChannelSvcFilterFuzzTest {

  private static final int FRAME_SAMPLES = 960;
  private static final short[] SILENCE = new short[FRAME_SAMPLES];

  private static final int MAXIMUM_QUEUED_FRAMES = 25;
  private static final int FED_SAMPLES_LIMIT = 2 * MAXIMUM_QUEUED_FRAMES * FRAME_SAMPLES;

  private static final int MAXIMUM_REPETITIONS = 8;
  private static final int MAXIMUM_CHUNK_BYTES = 2 * FRAME_SAMPLES * AudioFilter.FRAME_SIZE;
  private static final int MAXIMUM_FEED_SAMPLES = (MAXIMUM_REPETITIONS * MAXIMUM_CHUNK_BYTES) / AudioFilter.FRAME_SIZE;

  private static final String CODEC = "pcm_s16le";
  private static final int SAMPLE_RATE = 48_000;
  private static final int STEREO_CHANNELS = 2;
  private static final OriginalAudioMetadata METADATA = OriginalAudioMetadata.of(
    CODEC,
    OriginalAudioMetadata.UNKNOWN,
    SAMPLE_RATE,
    STEREO_CHANNELS,
    OriginalAudioMetadata.UNKNOWN
  );

  private static final String ENTITY = "alice";

  @FuzzTest(maxDuration = "30s")
  void shouldQueueTheNewestCompleteFramesOfAnyAudioInOrder(final FuzzedDataProvider data) {
    final VoicechatServerApi api = Mockito.mock(VoicechatServerApi.class, Mockito.RETURNS_MOCKS);
    final SvcModule module = new SvcModule();
    module.inject(api);
    try {
      this.checkQueuedFrames(api, data);
    } finally {
      module.stop();
    }
  }

  private void checkQueuedFrames(final VoicechatServerApi api, final FuzzedDataProvider data) {
    final SvcFilter filter = SvcFilter.svc(ENTITY);
    filter.start();
    final Supplier<short[]> speaker = this.findSpeaker(api);
    final short[] fed = this.feedChunks(filter, data);
    this.assertNewestFramesQueued(filter, speaker, fed);

    filter.release();
  }

  private Supplier<short[]> findSpeaker(final VoicechatServerApi api) {
    final ArgumentCaptor<Supplier<short[]>> speakerCaptor = ArgumentCaptor.captor();
    final VoicechatServerApi verifiedPlayerCreation = Mockito.verify(api);
    verifiedPlayerCreation.createAudioPlayer(
      ArgumentMatchers.any(AudioChannel.class),
      ArgumentMatchers.any(OpusEncoder.class),
      speakerCaptor.capture()
    );
    return speakerCaptor.getValue();
  }

  private short[] feedChunks(final SvcFilter filter, final FuzzedDataProvider data) {
    final ShortBuffer fed = ShortBuffer.allocate(FED_SAMPLES_LIMIT + MAXIMUM_FEED_SAMPLES);
    while (data.remainingBytes() > 0 && fed.position() < FED_SAMPLES_LIMIT) {
      final int repetitions = data.consumeInt(1, MAXIMUM_REPETITIONS);
      final int length = data.consumeInt(0, MAXIMUM_CHUNK_BYTES);
      final byte[] chunk = data.consumeBytes(length);
      this.feedRepeatedly(filter, chunk, repetitions, fed);
    }

    fed.flip();
    final short[] samples = new short[fed.remaining()];
    fed.get(samples);
    return samples;
  }

  private void feedRepeatedly(final SvcFilter filter, final byte[] chunk, final int repetitions, final ShortBuffer fed) {
    final ByteBuffer original = ByteBuffer.wrap(chunk);
    final short[] mono = MonoDownmixer.downmix(original);
    for (int repetition = 0; repetition < repetitions; repetition++) {
      final ByteBuffer samples = ByteBuffer.wrap(chunk);
      final boolean changed = filter.applyFilter(samples, METADATA);
      final int position = samples.position();
      assertFalse(changed);
      assertEquals(0, position);

      fed.put(mono);
    }
  }

  private void assertNewestFramesQueued(final SvcFilter filter, final Supplier<short[]> speaker, final short[] fed) {
    final int completeFrames = fed.length / FRAME_SAMPLES;
    final int expectedQueued = Math.min(completeFrames, MAXIMUM_QUEUED_FRAMES);
    final int queued = filter.getQueuedFrames();
    assertEquals(expectedQueued, queued);

    for (int frame = completeFrames - expectedQueued; frame < completeFrames; frame++) {
      final int start = frame * FRAME_SAMPLES;
      final short[] expected = Arrays.copyOfRange(fed, start, start + FRAME_SAMPLES);
      final short[] played = speaker.get();
      assertArrayEquals(expected, played);
    }

    final short[] afterQueue = speaker.get();
    assertArrayEquals(SILENCE, afterQueue);
  }
}
