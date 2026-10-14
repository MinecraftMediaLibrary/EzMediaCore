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
package me.brandonli.mcav.media.player.multimedia.cv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import me.brandonli.mcav.media.Polling;
import me.brandonli.mcav.media.player.attachable.AudioAttachableCallback;
import me.brandonli.mcav.media.player.attachable.DimensionAttachableCallback;
import me.brandonli.mcav.media.player.attachable.VideoAttachableCallback;
import me.brandonli.mcav.media.player.pipeline.step.AudioPipelineStep;
import me.brandonli.mcav.media.player.pipeline.step.VideoPipelineStep;
import org.bytedeco.javacv.FrameGrabber;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/** Verifies natural worker termination when decoder acquisition, cleanup or failure reporting throws. */
final class PlaybackSessionFailureTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private static final String RECOVERABLE_FAILURES_SOURCE = "recoverableFailures";

  private static final int FRAME_WIDTH = 4;
  private static final int FRAME_HEIGHT = 2;
  private static final long FRAME_INTERVAL_MICROSECONDS = 33_333L;

  private static Stream<Throwable> recoverableFailures() {
    return Stream.of(
      new IllegalStateException("decoder failed!"),
      new UnsatisfiedLinkError("native decoder missing!"),
      new AssertionError("decoder assertion!")
    );
  }

  private static FrameGrabber fail(final Throwable failure) {
    if (failure instanceof final Error error) {
      throw error;
    }
    throw (RuntimeException) failure;
  }

  private static PlaybackSession session(
    final PlaybackSession.GrabberFactory video,
    final PlaybackSession.@Nullable GrabberFactory audio,
    final BiConsumer<String, Throwable> handler
  ) {
    final VideoAttachableCallback pictures = VideoAttachableCallback.create();
    final AudioAttachableCallback sound = AudioAttachableCallback.create();
    final DimensionAttachableCallback dimensions = DimensionAttachableCallback.create();
    return new PlaybackSession(video, audio, pictures, sound, dimensions, handler, 0L, false, 0L, Long.MAX_VALUE, System::nanoTime);
  }

  @Test
  void shouldCloseAnOpenedSessionCancelledBeforeThreadsStart() throws Exception {
    final FrameGrabber grabber = Mockito.mock(FrameGrabber.class);
    final PlaybackSession session = session(() -> grabber, null, (_, _) -> {});
    session.open();
    session.stop();
    session.stop();
    Mockito.verify(grabber).close();
    final boolean active = session.isActive();
    assertFalse(active);
  }

  @Test
  void shouldNotAcquireTheGrabberWhenCancellingAnUnopenedSession() {
    final PlaybackSession session = session(
      () -> {
        throw new AssertionError("cancelled acquisition!");
      },
      null,
      (_, _) -> {}
    );
    session.stop();
    final boolean active = session.isActive();
    assertFalse(active);
  }

  private static void awaitNaturalEnd(final PlaybackSession session) throws InterruptedException {
    Polling.awaitCondition("all session workers exited naturally", TIMEOUT, () -> !session.isActive());
  }

  private static void captureUncaught(final AtomicReference<Throwable> failure) {
    final Thread worker = Thread.currentThread();
    worker.setUncaughtExceptionHandler((_, thrown) -> failure.set(thrown));
  }

  @ParameterizedTest
  @MethodSource(RECOVERABLE_FAILURES_SOURCE)
  void shouldNotStrandTheAudioRendererWhenSeparateAudioFactoriesFail(final Throwable failure) throws Exception {
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    final ScriptedFrameGrabber video = ScriptedFrameGrabber.of();
    final PlaybackSession playback = session(() -> video, () -> fail(failure), (_, thrown) -> reported.set(thrown));
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable observed = reported.get();
      final boolean closed = video.isClosed();
      assertSame(failure, observed);
      assertTrue(closed);
    } finally {
      playback.stop();
    }
  }

  @Test
  void shouldSignalEndFromFailedAudioFactoriesEvenWhenTheirHandlerInterruptsAndThrows() throws Exception {
    final IllegalStateException opening = new IllegalStateException("audio open failed!");
    final AssertionError reporting = new AssertionError("handler failed!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    final AtomicBoolean keptInterrupt = new AtomicBoolean();
    final ScriptedFrameGrabber video = ScriptedFrameGrabber.of();
    final PlaybackSession.GrabberFactory audio = () -> {
      final Thread worker = Thread.currentThread();
      worker.setUncaughtExceptionHandler((thread, failure) -> {
        keptInterrupt.set(thread.isInterrupted());
        uncaught.set(failure);
      });
      throw opening;
    };
    final PlaybackSession playback = session(() -> video, audio, (_, failure) -> {
      reported.set(failure);
      final Thread worker = Thread.currentThread();
      worker.interrupt();
      throw reporting;
    });
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable observed = reported.get();
      final Throwable escaped = uncaught.get();
      final boolean interrupted = keptInterrupt.get();
      assertSame(opening, observed);
      assertSame(reporting, escaped);
      assertTrue(interrupted);
    } finally {
      playback.stop();
    }
  }

  @ParameterizedTest
  @MethodSource(RECOVERABLE_FAILURES_SOURCE)
  void shouldSignalBothEndsFromFailedDecodersEvenWhenTheirHandlerInterruptsAndThrows(final Throwable failure) throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final AssertionError reporting = new AssertionError("handler failed!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    doAnswer(_ -> {
      captureUncaught(uncaught);
      return fail(failure);
    })
      .when(video)
      .grab();
    final PlaybackSession playback = session(() -> video, null, (_, thrown) -> {
      reported.set(thrown);
      final Thread worker = Thread.currentThread();
      worker.interrupt();
      throw reporting;
    });
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable observed = reported.get();
      final Throwable escaped = uncaught.get();
      final boolean closed = video.isClosed();
      assertSame(failure, observed);
      assertSame(reporting, escaped);
      assertTrue(closed);
    } finally {
      playback.stop();
    }
  }

  @ParameterizedTest
  @MethodSource(RECOVERABLE_FAILURES_SOURCE)
  void shouldNotStrandEitherRendererOnRecoverableGrabberCloseFailures(final Throwable failure) throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    doAnswer(invocation -> {
      invocation.callRealMethod();
      throw failure;
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, thrown) -> reported.set(thrown));
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final boolean closed = video.isClosed();
      final Throwable observed = reported.get();
      assertTrue(closed);
      assertNull(observed, "discarded grabber close failures retain the existing quiet-close contract");
      verify(video).close();
    } finally {
      playback.stop();
    }
  }

  @ParameterizedTest
  @MethodSource(RECOVERABLE_FAILURES_SOURCE)
  void shouldCloseTheAcquiredGrabberAndKeepTheOriginalFailureWhenTheLengthLookupFails(final Throwable failure) throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final IllegalStateException cleanup = new IllegalStateException("close failed too!");
    when(video.getLengthInTime()).thenThrow(failure);
    doAnswer(invocation -> {
      invocation.callRealMethod();
      throw cleanup;
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    final Class<? extends Throwable> expectedType = failure.getClass();
    final Throwable thrown = assertThrows(expectedType, playback::open);
    final Throwable[] suppressed = thrown.getSuppressed();
    final boolean closed = video.isClosed();
    final boolean active = playback.isActive();
    assertSame(failure, thrown);
    assertTrue(closed);
    assertFalse(active);
    assertEquals(1, suppressed.length);
    final Throwable suppressedCleanup = suppressed[0];
    assertSame(cleanup, suppressedCleanup);
    assertThrows(IllegalStateException.class, playback::startThreads);
    verify(video).close();
  }

  @Test
  void shouldCloseTheAcquiredGrabberWhenTheLengthLookupFailsAndCleanupSucceeds() throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final IllegalStateException failure = new IllegalStateException("metadata failed!");
    when(video.getLengthInTime()).thenThrow(failure);
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    final IllegalStateException thrown = assertThrows(IllegalStateException.class, playback::open);
    final boolean closed = video.isClosed();
    assertSame(failure, thrown);
    assertTrue(closed);
    verify(video).close();
  }

  @Test
  void shouldKeepTheLengthLookupFailureWhenCloseThrowsTheSameInstance() throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final AssertionError failure = new AssertionError("metadata and close failed!");
    when(video.getLengthInTime()).thenThrow(failure);
    doAnswer(invocation -> {
      invocation.callRealMethod();
      throw failure;
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    final AssertionError thrown = assertThrows(AssertionError.class, playback::open);
    final Throwable[] suppressed = thrown.getSuppressed();
    assertSame(failure, thrown);
    assertEquals(0, suppressed.length);
    verify(video).close();
  }

  @Test
  void shouldSupersedeARecoverableOpenFailureWithAFatalCleanup() throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final IllegalStateException opening = new IllegalStateException("metadata failed!");
    final OutOfMemoryError cleanup = new OutOfMemoryError("close exhausted memory!");
    when(video.getLengthInTime()).thenThrow(opening);
    doAnswer(invocation -> {
      invocation.callRealMethod();
      throw cleanup;
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    final OutOfMemoryError thrown = assertThrows(OutOfMemoryError.class, playback::open);
    assertSame(cleanup, thrown);
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void shouldLetFatalPipelineErrorsEscapeWithoutCallingTheReporter(final boolean audioFailure) throws Exception {
    final Object frame = audioFailure ? ScriptedFrameGrabber.audio(0L) : ScriptedFrameGrabber.video(0L);
    final ScriptedFrameGrabber grabber = ScriptedFrameGrabber.of(frame);
    final VideoAttachableCallback pictures = VideoAttachableCallback.create();
    final AudioAttachableCallback sound = AudioAttachableCallback.create();
    final DimensionAttachableCallback dimensions = DimensionAttachableCallback.create();
    final OutOfMemoryError failure = new OutOfMemoryError("pipeline exhausted memory!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    if (audioFailure) {
      final AudioPipelineStep pipeline = AudioPipelineStep.of((_, _) -> {
        captureUncaught(uncaught);
        throw failure;
      });
      sound.attach(pipeline);
    } else {
      final VideoPipelineStep pipeline = VideoPipelineStep.of((_, _) -> {
        captureUncaught(uncaught);
        throw failure;
      });
      pictures.attach(pipeline);
    }
    final PlaybackSession playback = new PlaybackSession(
      () -> grabber,
      null,
      pictures,
      sound,
      dimensions,
      (_, thrown) -> reported.set(thrown),
      0L,
      false,
      0L,
      Long.MAX_VALUE,
      System::nanoTime
    );
    try {
      playback.start();
      Polling.awaitCondition("fatal renderer failure escapes", TIMEOUT, () -> uncaught.get() != null);
      final Throwable escaped = uncaught.get();
      final Throwable observed = reported.get();
      assertSame(failure, escaped);
      assertNull(observed);
    } finally {
      playback.stop();
    }
  }

  @Test
  void shouldPropagateFatalLengthLookupFailuresWithoutCallingForeignCleanup() throws Exception {
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    final OutOfMemoryError failure = new OutOfMemoryError("metadata exhausted memory!");
    when(video.getLengthInTime()).thenThrow(failure);
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    try {
      final OutOfMemoryError thrown = assertThrows(OutOfMemoryError.class, playback::open);
      assertSame(failure, thrown);
      verify(video, never()).close();
    } finally {
      video.close();
    }
  }

  @Test
  void shouldLetFatalAudioFactoryFailuresEscapeWithoutBeingReported() throws Exception {
    final OutOfMemoryError failure = new OutOfMemoryError("decoder out of memory!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    final ScriptedFrameGrabber video = ScriptedFrameGrabber.of();
    final PlaybackSession.GrabberFactory audio = () -> {
      captureUncaught(uncaught);
      throw failure;
    };
    final PlaybackSession playback = session(() -> video, audio, (_, thrown) -> reported.set(thrown));
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable escaped = uncaught.get();
      final Throwable observed = reported.get();
      assertSame(failure, escaped);
      assertNull(observed);
    } finally {
      playback.stop();
    }
  }

  @Test
  void shouldLetFatalDecodeFailuresEscapeWithoutBeingReported() throws Exception {
    final OutOfMemoryError failure = new OutOfMemoryError("decode exhausted memory!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    doAnswer(_ -> {
      captureUncaught(uncaught);
      throw failure;
    })
      .when(video)
      .grab();
    final PlaybackSession playback = session(() -> video, null, (_, thrown) -> reported.set(thrown));
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable escaped = uncaught.get();
      final Throwable observed = reported.get();
      final boolean closed = video.isClosed();
      assertSame(failure, escaped);
      assertNull(observed);
      assertTrue(closed);
    } finally {
      playback.stop();
    }
  }

  @Test
  void shouldLetFatalCloseFailuresEscapeWithoutBeingReportedAndStillSignalBothEnds() throws Exception {
    final OutOfMemoryError failure = new OutOfMemoryError("close out of memory!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    final ScriptedFrameGrabber real = ScriptedFrameGrabber.of();
    final ScriptedFrameGrabber video = spy(real);
    doAnswer(invocation -> {
      captureUncaught(uncaught);
      invocation.callRealMethod();
      throw failure;
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, thrown) -> reported.set(thrown));
    try {
      playback.start();
      awaitNaturalEnd(playback);
      final Throwable escaped = uncaught.get();
      final Throwable observed = reported.get();
      assertSame(failure, escaped);
      assertNull(observed);
    } finally {
      playback.stop();
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void shouldStopTheProducerAndEscapeWhenRendererHandlersFail(final boolean audioFailure) throws Exception {
    final List<Object> frames = new ArrayList<>();
    for (int index = 0; index < 30; index++) {
      frames.add(ScriptedFrameGrabber.video(index * FRAME_INTERVAL_MICROSECONDS));
      frames.add(ScriptedFrameGrabber.audio(index * FRAME_INTERVAL_MICROSECONDS));
    }
    final ScriptedFrameGrabber video = new ScriptedFrameGrabber(FRAME_WIDTH, FRAME_HEIGHT, false, frames);
    final VideoAttachableCallback pictures = VideoAttachableCallback.create();
    final AudioAttachableCallback sound = AudioAttachableCallback.create();
    final DimensionAttachableCallback dimensions = DimensionAttachableCallback.create();
    final IllegalStateException filterFailure = new IllegalStateException("filter failed!");
    final AssertionError handlerFailure = new AssertionError("handler failed!");
    final AtomicReference<Throwable> uncaught = new AtomicReference<>();
    final AtomicReference<Throwable> reported = new AtomicReference<>();
    if (audioFailure) {
      final AudioPipelineStep pipeline = AudioPipelineStep.of((_, _) -> {
        throw filterFailure;
      });
      sound.attach(pipeline);
    } else {
      final VideoPipelineStep pipeline = VideoPipelineStep.of((_, _) -> {
        throw filterFailure;
      });
      pictures.attach(pipeline);
    }
    final PlaybackSession playback = new PlaybackSession(
      () -> video,
      null,
      pictures,
      sound,
      dimensions,
      (_, failure) -> {
        reported.set(failure);
        captureUncaught(uncaught);
        throw handlerFailure;
      },
      0L,
      false,
      0L,
      Long.MAX_VALUE,
      System::nanoTime
    );
    try {
      playback.start();
      Polling.awaitCondition("failed renderer and producer terminate", TIMEOUT, () -> video.isClosed() && uncaught.get() != null);
      final Throwable observed = reported.get();
      final Throwable escaped = uncaught.get();
      final boolean active = playback.isActive();
      assertSame(filterFailure, observed);
      assertSame(handlerFailure, escaped);
      assertFalse(active);
    } finally {
      playback.stop();
    }
  }

  @Test
  void shouldStillTerminateADecoderWaitingToSignalEndToAFullQueueWhenStopping() throws Exception {
    final List<Object> frames = new ArrayList<>();
    for (int index = 0; index < 5; index++) {
      final Object frame = ScriptedFrameGrabber.video(index * FRAME_INTERVAL_MICROSECONDS);
      frames.add(frame);
    }
    final ScriptedFrameGrabber real = new ScriptedFrameGrabber(FRAME_WIDTH, FRAME_HEIGHT, false, frames);
    final ScriptedFrameGrabber video = spy(real);
    final AtomicReference<Thread> decoder = new AtomicReference<>();
    doAnswer(invocation -> {
      final Thread worker = Thread.currentThread();
      decoder.set(worker);
      return invocation.callRealMethod();
    })
      .when(video)
      .close();
    final PlaybackSession playback = session(() -> video, null, (_, _) -> {});
    playback.pause();
    try {
      playback.start();
      Polling.awaitCondition("decoder waits to signal end to a full paused queue", TIMEOUT, () -> {
        final Thread worker = decoder.get();
        final boolean closed = video.isClosed();
        return closed && worker != null && worker.getState() == Thread.State.TIMED_WAITING;
      });
      final long before = System.nanoTime();
      playback.stop();
      final long elapsed = System.nanoTime() - before;
      final boolean active = playback.isActive();
      assertFalse(active);
      final boolean stoppedQuickly = elapsed < 1_000_000_000L;
      assertTrue(stoppedQuickly, "terminal marker wait must end when stop takes ownership");
    } finally {
      playback.stop();
    }
  }
}
