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
package me.brandonli.mcav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import me.brandonli.mcav.utils.ThrowableUtils;
import me.brandonli.mcav.utils.os.OS;
import me.brandonli.mcav.utils.os.OSUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Tests {@link NativeHeapTrimmer} with trims the tests count, and once with the JDK's own command.
 */
final class NativeHeapTrimmerTest {

  private static final Duration SHORT = Duration.ofMillis(10);
  private static final long TIMEOUT_SECONDS = 10L;
  private static final long FRAME_BYTES = 1920L * 1080L * 3L;
  private static final int ARENA_THREADS = 8;
  private static final String RESIDENT_PREFIX = "VmRSS:";

  private static Optional<Thread> trimmingThread() {
    return Thread.getAllStackTraces()
      .keySet()
      .stream()
      .filter(thread -> thread.getName().equals(NativeHeapTrimmer.THREAD_NAME))
      .findFirst();
  }

  @Test
  void trimsAtEveryIntervalOnADaemonUntilClosed() throws InterruptedException {
    final CountDownLatch trims = new CountDownLatch(3);
    final NativeHeapTrimmer trimmer = new NativeHeapTrimmer(SHORT, trims::countDown, () -> {});
    trimmer.start();
    final boolean trimmed = trims.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    final boolean trimming = trimmer.isTrimming();
    final Thread thread = trimmingThread().orElseThrow();
    final boolean daemon = thread.isDaemon();
    trimmer.close();

    assertTrue(trimmed);
    assertTrue(trimming);
    assertTrue(daemon, "the trims never keep the JVM alive");
    assertFalse(trimmer.isTrimming());
    assertFalse(thread.isAlive(), "closing waits for the thread to end");
  }

  @Test
  void aFailedTrimIsLoggedAndTrimmingGoesOn() throws InterruptedException {
    final AtomicInteger attempts = new AtomicInteger();
    final CountDownLatch trims = new CountDownLatch(1);
    final NativeHeapTrimmer trimmer = new NativeHeapTrimmer(
      SHORT,
      () -> {
        if (attempts.incrementAndGet() == 1) {
          throw new IllegalStateException("the first trim fails on purpose");
        }
        trims.countDown();
      },
      () -> {}
    );
    final PrintStream standardError = System.err;
    final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
    final boolean trimmed;
    try {
      trimmer.start();
      trimmed = trims.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      trimmer.close();
    } finally {
      System.setErr(standardError);
    }
    final String log = captured.toString(StandardCharsets.UTF_8);

    assertTrue(trimmed, "the trim after the failed one ran");
    assertTrue(log.contains("Could not trim the native heap"), log);
    assertTrue(log.contains("the first trim fails on purpose"), log);
  }

  @Test
  void trimsOnlyOnLinuxInAJvmThatDoesNotTrimItselfAtAPositiveInterval() {
    final Runnable nothing = () -> {};
    final NativeHeapTrimmer trimming = NativeHeapTrimmer.create(OS.LINUX, 0L, 60L, nothing, nothing);
    trimming.start();
    final boolean trims = trimming.isTrimming();
    trimming.close();

    assertTrue(trims);
    assertNeverTrims(NativeHeapTrimmer.create(OS.WINDOWS, 0L, 60L, nothing, nothing), "off Linux");
    assertNeverTrims(NativeHeapTrimmer.create(OS.LINUX, 1L, 60L, nothing, nothing), "in a JVM that trims itself");
    assertNeverTrims(
      NativeHeapTrimmer.create(OS.LINUX, NativeHeapTrimmer.NO_JVM_INTERVAL, 60L, nothing, nothing),
      "in a JVM other than HotSpot"
    );
    assertNeverTrims(NativeHeapTrimmer.create(OS.LINUX, 0L, 0L, nothing, nothing), "turned off");
    assertNeverTrims(NativeHeapTrimmer.create(OS.LINUX, 0L, -1L, nothing, nothing), "turned off by a negative interval");
  }

  @Test
  void fixesTheThresholdOnceWhenTrimmingStarts() {
    final Runnable nothing = () -> {};
    final AtomicInteger fixes = new AtomicInteger();
    final Runnable fix = fixes::incrementAndGet;
    final NativeHeapTrimmer trimming = NativeHeapTrimmer.create(OS.LINUX, 0L, 60L, nothing, fix);
    final int created = fixes.get();
    trimming.start();
    final int started = fixes.get();
    trimming.close();
    for (final NativeHeapTrimmer never : List.of(
      NativeHeapTrimmer.create(OS.WINDOWS, 0L, 60L, nothing, fix),
      NativeHeapTrimmer.create(OS.LINUX, 1L, 60L, nothing, fix),
      NativeHeapTrimmer.create(OS.LINUX, NativeHeapTrimmer.NO_JVM_INTERVAL, 60L, nothing, fix),
      NativeHeapTrimmer.create(OS.LINUX, 0L, 0L, nothing, fix),
      NativeHeapTrimmer.create(OS.LINUX, 0L, -1L, nothing, fix)
    )) {
      never.start();
      never.close();
    }

    assertEquals(0, created, "creating the trimmer leaves the threshold alone");
    assertEquals(1, started);
    assertEquals(1, fixes.get(), "a trimmer that trims never leaves the threshold alone");
  }

  @Test
  void aFailedThresholdIsLoggedAndTrimmingStillStarts() {
    final NativeHeapTrimmer trimmer = new NativeHeapTrimmer(
      SHORT,
      () -> {},
      () -> {
        throw new IllegalStateException("the threshold fails on purpose");
      }
    );
    final PrintStream standardError = System.err;
    final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
    final boolean trimming;
    try {
      trimmer.start();
      trimming = trimmer.isTrimming();
      trimmer.close();
    } finally {
      System.setErr(standardError);
    }
    final String log = captured.toString(StandardCharsets.UTF_8);

    assertTrue(trimming);
    assertTrue(log.contains("Could not fix the trim threshold of the native heap"), log);
    assertTrue(log.contains("the threshold fails on purpose"), log);
  }

  @Test
  void glibcTakesTheThreshold() {
    final SymbolLookup library = Linker.nativeLinker().defaultLookup();
    final boolean glibc = OSUtils.getOS() == OS.LINUX && library.find("gnu_get_libc_version").isPresent();
    assertEquals(glibc, NativeHeapTrimmer.fixGlibcTrimThreshold());
  }

  @Test
  void aCLibraryOtherThanGlibcKeepsItsThreshold() {
    final SymbolLookup musl = name -> name.equals("mallopt") ? Optional.of(MemorySegment.NULL) : Optional.empty();
    assertFalse(NativeHeapTrimmer.fixGlibcTrimThreshold(musl), "musl has a mallopt that ignores every parameter");
    assertFalse(NativeHeapTrimmer.fixGlibcTrimThreshold(_ -> Optional.empty()));
  }

  @Test
  void aThresholdThatMalloptRefusesIsReported() {
    final MethodHandle refusing = MethodHandles.dropArguments(MethodHandles.constant(int.class, 0), 0, int.class, int.class);
    assertFalse(NativeHeapTrimmer.setTrimThreshold(refusing));
  }

  @Test
  void aThresholdThatGlibcRefusesIsReported() throws ReflectiveOperationException {
    final List<List<Integer>> calls = new ArrayList<>();
    final boolean taken;
    final boolean refused;
    try (final Arena arena = Arena.ofConfined()) {
      taken = NativeHeapTrimmer.fixGlibcTrimThreshold(glibcWithMallopt(arena, recordingMallopt(calls, 1)));
      refused = NativeHeapTrimmer.fixGlibcTrimThreshold(glibcWithMallopt(arena, recordingMallopt(calls, 0)));
    }
    final List<Integer> threshold = List.of(-1, NativeHeapTrimmer.TRIM_THRESHOLD_BYTES);

    assertTrue(taken);
    assertFalse(refused, "a threshold that glibc refuses is reported");
    assertEquals(List.of(threshold, threshold), calls, "mallopt takes M_TRIM_THRESHOLD and the threshold");
  }

  @Test
  void theCLibraryOfThisJvmAnswersForTheThreshold() {
    final List<SymbolLookup> asked = new ArrayList<>();
    final boolean fixed;
    try (final MockedStatic<NativeHeapTrimmer> trimmer = mockStatic(NativeHeapTrimmer.class, CALLS_REAL_METHODS)) {
      trimmer
        .when(() -> NativeHeapTrimmer.fixGlibcTrimThreshold(any(SymbolLookup.class)))
        .thenAnswer(call -> {
          asked.add(call.getArgument(0));
          return false;
        });
      fixed = NativeHeapTrimmer.fixGlibcTrimThreshold();
    }
    final SymbolLookup ownLibrary = Linker.nativeLinker().defaultLookup();

    assertFalse(fixed, "a C library that keeps its threshold is reported");
    assertEquals(1, asked.size());
    assertEquals(ownLibrary.find("malloc"), asked.getFirst().find("malloc"), "the C library of this JVM is asked");
  }

  private static IntBinaryOperator recordingMallopt(final List<List<Integer>> calls, final int answer) {
    return (parameter, value) -> {
      calls.add(List.of(parameter, value));
      return answer;
    };
  }

  // a glibc whose mallopt is a native stub that calls the operator; the trimmer only looks gnu_get_libc_version up
  @SuppressWarnings("restricted")
  private static SymbolLookup glibcWithMallopt(final Arena arena, final IntBinaryOperator mallopt) throws ReflectiveOperationException {
    final MethodType type = MethodType.methodType(int.class, int.class, int.class);
    final MethodHandle operator = MethodHandles.lookup().findVirtual(IntBinaryOperator.class, "applyAsInt", type).bindTo(mallopt);
    final FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
    final MemorySegment stub = Linker.nativeLinker().upcallStub(operator, descriptor, arena);
    return name -> name.equals("gnu_get_libc_version") || name.equals("mallopt") ? Optional.of(stub) : Optional.empty();
  }

  @Test
  void aMalloptThatCannotBeCalledIsAnIllegalState() {
    final IllegalCallerException refused = new IllegalCallerException("native access is not enabled");
    final MethodHandle mallopt = malloptThatThrows(refused);
    final IllegalStateException failure = assertThrows(IllegalStateException.class, () -> NativeHeapTrimmer.setTrimThreshold(mallopt));
    assertEquals("Could not fix the trim threshold of the native heap", failure.getMessage());
    assertSame(refused, failure.getCause());
  }

  @Test
  void aVirtualMachineErrorOfMalloptGoesOnAsItIs() {
    final OutOfMemoryError exhausted = new OutOfMemoryError("no memory for the call");
    final MethodHandle mallopt = malloptThatThrows(exhausted);
    assertSame(exhausted, assertThrows(OutOfMemoryError.class, () -> NativeHeapTrimmer.setTrimThreshold(mallopt)));
  }

  private static MethodHandle malloptThatThrows(final Throwable failure) {
    final MethodHandle throwing = MethodHandles.throwException(int.class, failure.getClass());
    return MethodHandles.dropArguments(MethodHandles.insertArguments(throwing, 0, failure), 0, int.class, int.class);
  }

  private static void assertNeverTrims(final NativeHeapTrimmer trimmer, final String reason) {
    trimmer.start();
    final boolean trimming = trimmer.isTrimming();
    trimmer.close();
    assertFalse(trimming, reason);
  }

  @Test
  void closingWithoutTrimmingIsHarmless() {
    final NativeHeapTrimmer trimmer = new NativeHeapTrimmer(SHORT, () -> {}, () -> {});
    trimmer.close();
    assertFalse(trimmer.isTrimming());
  }

  @Test
  void anInterruptedCloseKeepsTheInterruptOfItsCaller() throws InterruptedException {
    final CountDownLatch trimStarted = new CountDownLatch(1);
    final Semaphore trimRelease = new Semaphore(0);
    final NativeHeapTrimmer trimmer = new NativeHeapTrimmer(
      SHORT,
      () -> {
        trimStarted.countDown();
        trimRelease.acquireUninterruptibly();
      },
      () -> {}
    );
    final boolean started;
    final Thread thread;
    final boolean interrupted;
    final boolean aliveAfterClose;
    try {
      trimmer.start();
      started = trimStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      thread = trimmingThread().orElseThrow();
      final Thread currentThread = Thread.currentThread();
      currentThread.interrupt();
      trimmer.close();
      interrupted = Thread.interrupted();
      aliveAfterClose = thread.isAlive();
    } finally {
      trimRelease.release();
    }
    thread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));

    assertTrue(started);
    assertTrue(interrupted);
    assertTrue(aliveAfterClose, "an interrupted close stops waiting for the trimming thread");
    assertFalse(thread.isAlive(), "the trimming thread ends on its interrupt");
  }

  @Test
  void followsTheIntervalOfTheSystemProperty() {
    final boolean linux = OSUtils.getOS() == OS.LINUX;
    final String previous = System.getProperty(NativeHeapTrimmer.INTERVAL_PROPERTY);
    try {
      System.setProperty(NativeHeapTrimmer.INTERVAL_PROPERTY, "0");
      assertNeverTrims(NativeHeapTrimmer.forThisJvm(), "the property turns trimming off");
      System.clearProperty(NativeHeapTrimmer.INTERVAL_PROPERTY);
      final NativeHeapTrimmer trimmer = NativeHeapTrimmer.forThisJvm();
      trimmer.start();
      final boolean trimming = trimmer.isTrimming();
      trimmer.close();
      assertEquals(linux, trimming, "the JVM of the tests does not trim itself");
    } finally {
      if (previous == null) {
        System.clearProperty(NativeHeapTrimmer.INTERVAL_PROPERTY);
      } else {
        System.setProperty(NativeHeapTrimmer.INTERVAL_PROPERTY, previous);
      }
    }
  }

  @Test
  @SuppressWarnings("restricted")
  void theTopOfAThreadArenaGoesBackToTheSystemWhileTrimming() throws InterruptedException, IOException {
    final Linker linker = Linker.nativeLinker();
    final SymbolLookup library = linker.defaultLookup();
    assumeTrue(OSUtils.getOS() == OS.LINUX && library.find("gnu_get_libc_version").isPresent(), "glibc's thread arenas");
    final MethodHandle malloc = linker.downcallHandle(
      library.find("malloc").orElseThrow(),
      FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );
    final MethodHandle free = linker.downcallHandle(library.find("free").orElseThrow(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    final NativeHeapTrimmer trimmer = NativeHeapTrimmer.forThisJvm();
    final long before;
    final long after;
    trimmer.start();
    try {
      before = residentKib();
      final List<Thread> threads = new ArrayList<>();
      for (int index = 0; index < ARENA_THREADS; index++) {
        threads.add(Thread.ofPlatform().start(() -> freeFramesIntoTheArenaTop(malloc, free)));
      }
      for (final Thread thread : threads) {
        thread.join();
      }
      NativeHeapTrimmer.trimThisJvm();
      after = residentKib();
    } finally {
      trimmer.close();
    }
    final long freed = (ARENA_THREADS * FRAME_BYTES) / 1024;

    assertTrue(after - before < freed / 2, "kept " + (after - before) + " KiB of the " + freed + " KiB the threads freed");
  }

  // glibc maps the first frame on its own and raises its thresholds when it is freed; the second comes from the thread's
  // arena and joins its top when it is freed
  @SuppressWarnings("restricted")
  private static void freeFramesIntoTheArenaTop(final MethodHandle malloc, final MethodHandle free) {
    try {
      for (int frame = 0; frame < 2; frame++) {
        final MemorySegment block = ((MemorySegment) malloc.invokeExact(FRAME_BYTES)).reinterpret(FRAME_BYTES);
        block.fill((byte) 1);
        free.invokeExact(block);
      }
    } catch (final Throwable failure) {
      ThrowableUtils.throwIfFatal(failure);
      throw new IllegalStateException(failure);
    }
  }

  private static long residentKib() throws IOException {
    for (final String line : Files.readAllLines(Path.of("/proc/self/status"))) {
      if (line.startsWith(RESIDENT_PREFIX)) {
        return Long.parseLong(line.substring(RESIDENT_PREFIX.length()).replace("kB", "").strip());
      }
    }
    throw new IllegalStateException("no resident size in /proc/self/status");
  }

  @Test
  void readsTheIntervalOfTheJvmsOwnTrims() {
    assertEquals(0L, NativeHeapTrimmer.readJvmIntervalMillis(), "the JVM of the tests does not trim itself");
  }

  @Test
  void aJvmThatTrimsItselfIsLeftToItsOwnTrims() {
    final HotSpotDiagnosticMXBean trimmingItself = mock(HotSpotDiagnosticMXBean.class);
    final VMOption option = new VMOption("TrimNativeHeapInterval", "5000", true, VMOption.Origin.VM_CREATION);
    when(trimmingItself.getVMOption("TrimNativeHeapInterval")).thenReturn(option);
    final long interval;
    final NativeHeapTrimmer trimmer;
    try (final MockedStatic<ManagementFactory> factory = mockStatic(ManagementFactory.class, CALLS_REAL_METHODS)) {
      factory.when(() -> ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class)).thenReturn(trimmingItself);
      interval = NativeHeapTrimmer.readJvmIntervalMillis();
      trimmer = NativeHeapTrimmer.forThisJvm();
    }

    assertEquals(5000L, interval);
    assertNeverTrims(trimmer, "a JVM started with -XX:TrimNativeHeapInterval is left to its own trims");
  }

  @Test
  void aJvmOtherThanHotSpotIsNotTrimmed() {
    final HotSpotDiagnosticMXBean withoutTheOption = mock(HotSpotDiagnosticMXBean.class);
    final IllegalArgumentException noOption = new IllegalArgumentException("VM option \"TrimNativeHeapInterval\" does not exist");
    when(withoutTheOption.getVMOption(any())).thenThrow(noOption);
    final IllegalArgumentException noBean = new IllegalArgumentException("not a platform management interface");
    try (final MockedStatic<ManagementFactory> factory = mockStatic(ManagementFactory.class, CALLS_REAL_METHODS)) {
      factory.when(() -> ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class)).thenReturn(null);
      assertNeverTrims(NativeHeapTrimmer.forThisJvm(), "a JVM without HotSpot's diagnostic bean");
      factory.when(() -> ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class)).thenReturn(withoutTheOption);
      assertNeverTrims(NativeHeapTrimmer.forThisJvm(), "a JVM without the option of HotSpot's own trims");
      factory.when(() -> ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class)).thenThrow(noBean);
      assertNeverTrims(NativeHeapTrimmer.forThisJvm(), "a JVM that does not know HotSpot's diagnostic bean");
    }
  }

  @Test
  void trimsWithTheCommandOfTheJdk() {
    final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    final String answer = NativeHeapTrimmer.trimWithJdk(server);
    final String ownAnswer = NativeHeapTrimmer.trimThisJvm();
    final boolean linux = OSUtils.getOS() == OS.LINUX;
    final String expected = linux ? "Trim native heap" : "Not available";
    assertTrue(answer.startsWith(expected), answer);
    assertTrue(ownAnswer.startsWith(expected), ownAnswer);
  }

  @Test
  void aCommandThatCannotRunFailsWithItsCause() throws Exception {
    final MBeanServer server = mock(MBeanServer.class);
    final InstanceNotFoundException missing = new InstanceNotFoundException("no diagnostic commands");
    when(server.invoke(any(ObjectName.class), any(), any(), any())).thenThrow(missing);

    final IllegalStateException failure = assertThrows(IllegalStateException.class, () -> NativeHeapTrimmer.trimWithJdk(server));
    assertInstanceOf(InstanceNotFoundException.class, failure.getCause());
  }
}
