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
package me.brandonli.mcav.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;
import me.brandonli.mcav.media.player.PlayerException;
import me.brandonli.mcav.utils.os.Os;
import me.brandonli.mcav.vm.testing.FakeProcess;
import me.brandonli.mcav.vm.testing.FreePorts;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Tests {@link VmProcess} with {@link FakeProcess} instances in place of QEMU and a local socket in place of its
 * VNC display.
 */
final class VmProcessTest {

  private static final Path QEMU = Path.of("qemu-system-x86_64");
  private static final long SHORT_TIMEOUT_MILLISECONDS = 400L;

  @TempDir
  private Path directory;

  private final List<List<String>> commands = new CopyOnWriteArrayList<>();
  private final Deque<Object> outcomes = new ArrayDeque<>();
  private final List<ServerSocket> sockets = new CopyOnWriteArrayList<>();
  private int port;
  private ServerSocket display;
  private Path missingKvm;

  @BeforeEach
  void choosePort() {
    this.port = FreePorts.find();
    this.missingKvm = this.directory.resolve("kvm");
  }

  @AfterEach
  void closeSockets() throws IOException {
    for (final ServerSocket socket : this.sockets) {
      socket.close();
    }
  }

  private ServerSocket listen(final int listenPort) throws IOException {
    final InetAddress loopback = InetAddress.ofLiteral("127.0.0.1");
    final ServerSocket socket = new ServerSocket(listenPort, 50, loopback);
    this.sockets.add(socket);
    return socket;
  }

  private void openDisplay() throws IOException {
    if (this.display == null) {
      this.display = this.listen(this.port);
    }
  }

  private Path secretFolder() throws IOException {
    final Path folder = this.directory.resolve("secret");
    return Files.createDirectories(folder);
  }

  private Path passwordFile() {
    final Path folder = this.directory.resolve("secret");
    return folder.resolve("password");
  }

  private String secretObject() {
    final Path file = this.passwordFile();
    return "secret,id=mcav-vnc-password,file=" + file;
  }

  private VmSettings reachableSettings() {
    return new VmSettings(this.port, 64, 48, 10);
  }

  private static VmSettings unreachableSettings() {
    final int unusedPort = FreePorts.find();
    return new VmSettings(unusedPort, 64, 48, 10);
  }

  private VmProcess process(final VmSettings settings, final VmConfiguration configuration, final Os os, final Object... results) {
    for (final Object result : results) {
      this.outcomes.addLast(result);
    }
    final VmProcess.Launcher launcher = command -> {
      this.commands.add(command);
      final Object next = this.outcomes.removeFirst();
      if (next instanceof final IOException failure) {
        throw failure;
      }
      final Process started = (Process) next;
      final boolean alive = started.isAlive();
      if (alive) {
        this.openDisplay();
      }
      return started;
    };
    return new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      configuration,
      launcher,
      os,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      this::secretFolder
    );
  }

  private VmProcess reachable(final Os os, final Object... results) {
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    return this.process(settings, configuration, os, results);
  }

  private List<String> defaults() {
    final int display = this.port - VmProcess.FIRST_VNC_PORT;
    return List.of(
      "-vga",
      "std",
      "-display",
      "none",
      "-nic",
      "user,restrict=on",
      "-object",
      this.secretObject(),
      "-vnc",
      "127.0.0.1:" + display + ",share=force-shared,password-secret=mcav-vnc-password",
      "-usb",
      "-device",
      "usb-tablet"
    );
  }

  private List<String> command(final String... arguments) {
    final List<String> command = new ArrayList<>();
    final String program = QEMU.toString();
    command.add(program);
    final List<String> given = List.of(arguments);
    command.addAll(given);
    final List<String> defaultOptions = this.defaults();
    command.addAll(defaultOptions);
    return command;
  }

  private List<String> commandWithoutAccelerator(final VmSettings settings, final VmConfiguration configuration) {
    final VmProcess process = this.process(settings, configuration, Os.LINUX);
    return process.buildCommand(null, this.passwordFile());
  }

  /**
   * Builds the command expected on VNC display 1: the program, the configured options, the display defaults, and
   * the USB options the player adds.
   */
  private List<String> expectedOnDisplayOne(final List<String> options, final List<String> usbOptions) {
    final String program = QEMU.toString();
    final List<String> displayDefaults = List.of(
      "-vga",
      "std",
      "-display",
      "none",
      "-nic",
      "user,restrict=on",
      "-object",
      this.secretObject(),
      "-vnc",
      "127.0.0.1:1,share=force-shared,password-secret=mcav-vnc-password"
    );
    final List<String> expected = new ArrayList<>();
    expected.add(program);
    expected.addAll(options);
    expected.addAll(displayDefaults);
    expected.addAll(usbOptions);
    return expected;
  }

  @Test
  void shouldStartWithTheAcceleratorOfTheMachineAndWaitForTheDisplay() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.memory(64);
    final VmProcess process = this.process(settings, configuration, Os.WINDOWS, qemu);
    final boolean aliveBefore = process.isAlive();
    process.start();
    final boolean alive = process.isAlive();
    final List<String> expected = this.command("-m", "64M", "-accel", "whpx");
    assertFalse(aliveBefore);
    assertTrue(alive);
    final List<List<String>> expectedCommands = List.of(expected);
    assertEquals(expectedCommands, this.commands);
    process.shutdown();
    final boolean aliveAfter = process.isAlive();
    final int destroyCalls = qemu.getDestroyCalls();
    final int forcibleCalls = qemu.getForcibleDestroyCalls();
    assertFalse(aliveAfter);
    assertEquals(1, destroyCalls);
    assertEquals(0, forcibleCalls);
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "WHPX: No accelerator found, hr=80070057",
      "qemu-system-x86_64: invalid accel option",
      "failed to initialize the hypervisor",
      "Virtualization is disabled in the firmware",
    }
  )
  void shouldRetryWithSoftwareEmulationWhenTheAcceleratorFails(final String failure) {
    final FakeProcess failed = FakeProcess.exited(1, failure + "\n");
    final FakeProcess working = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.WINDOWS, failed, working);
    process.start();
    final boolean alive = process.isAlive();
    final List<String> first = this.command("-accel", "whpx");
    final List<String> second = this.command("-accel", "tcg");
    assertTrue(alive);
    final List<List<String>> expectedCommands = List.of(first, second);
    assertEquals(expectedCommands, this.commands);
  }

  @Test
  void shouldForgetTheOutputOfTheAttemptThatFailed() {
    final FakeProcess failed = FakeProcess.exited(1, "WHPX: No accelerator found\n");
    final FakeProcess failedAgain = FakeProcess.exited(2, "could not open disk image alpine.qcow2\n");
    final VmProcess process = this.reachable(Os.WINDOWS, failed, failedAgain);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    assertEquals("QEMU exited with code 2, printing \"could not open disk image alpine.qcow2\"!", message);
  }

  @Test
  void shouldFailWithoutRetryingWhenTheFailureIsUnrelatedToTheAccelerator() {
    final FakeProcess failed = FakeProcess.exited(1, "could not open disk image alpine.qcow2\n");
    final VmProcess process = this.reachable(Os.MAC, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final int launches = this.commands.size();
    final boolean alive = process.isAlive();
    assertEquals("QEMU exited with code 1, printing \"could not open disk image alpine.qcow2\"!", message);
    assertEquals(1, launches);
    assertFalse(alive);
  }

  @Test
  void shouldWaitForTheOutputThatExplainsTheExit() {
    final InputStream late = new DelayedStream("WHPX: No accelerator found\n", 300L);
    final FakeProcess failed = FakeProcess.running(late);
    failed.exit(1);
    final FakeProcess working = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.WINDOWS, failed, working);
    process.start();
    final int launches = this.commands.size();
    assertEquals(2, launches);
  }

  @Test
  void shouldUseSoftwareEmulationDirectlyWhenTheMachineHasNoAccelerator() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    final List<String> expected = this.command("-accel", "tcg");
    final List<List<String>> expectedCommands = List.of(expected);
    assertEquals(expectedCommands, this.commands);
  }

  @Test
  void shouldNotRetrySoftwareEmulation() {
    final FakeProcess failed = FakeProcess.exited(3, "tcg accel failed to start\n");
    final VmProcess process = this.reachable(Os.LINUX, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final int launches = this.commands.size();
    assertEquals("QEMU exited with code 3, printing \"tcg accel failed to start\"!", message);
    assertEquals(1, launches);
  }

  @ParameterizedTest
  @ValueSource(longs = { 0L, Long.MAX_VALUE - 100L })
  void shouldRejectAReachableDisplayAtTheStartupDeadline(final long origin) {
    final FakeProcess qemu = FakeProcess.running("");
    final long duration = TimeUnit.MILLISECONDS.toNanos(SHORT_TIMEOUT_MILLISECONDS);
    final Deque<Long> times = new ArrayDeque<>(List.of(origin, origin + duration));
    final LongSupplier clock = times::removeFirst;
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess.Launcher launcher = command -> {
      this.openDisplay();
      return qemu;
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      configuration,
      launcher,
      Os.FREEBSD,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      clock,
      this::secretFolder
    );
    assertThrows(PlayerException.class, process::start);
    final boolean alive = process.isAlive();
    assertFalse(alive, "an expired startup must terminate QEMU even if its display is reachable");
  }

  @Test
  void shouldAcceptAReachableDisplayBeforeTheStartupDeadline() {
    final FakeProcess qemu = FakeProcess.running("");
    final long duration = TimeUnit.MILLISECONDS.toNanos(SHORT_TIMEOUT_MILLISECONDS);
    final Deque<Long> times = new ArrayDeque<>(List.of(0L, duration - 1));
    final LongSupplier clock = times::removeFirst;
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess.Launcher launcher = command -> {
      this.openDisplay();
      return qemu;
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      configuration,
      launcher,
      Os.FREEBSD,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      clock,
      this::secretFolder
    );
    try {
      process.start();
      final boolean alive = process.isAlive();
      assertTrue(alive);
    } finally {
      process.shutdown();
    }
  }

  @Test
  void shouldKeepTheAcceleratorOfTheConfiguration() {
    final FakeProcess failed = FakeProcess.exited(1, "kvm accel is not available\n");
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.accelerator("kvm");
    final VmProcess process = this.process(settings, configuration, Os.WINDOWS, failed);
    assertThrows(PlayerException.class, process::start);
    final List<String> expected = this.command("-accel", "kvm");
    final List<List<String>> expectedCommands = List.of(expected);
    assertEquals(expectedCommands, this.commands);
  }

  @Test
  void shouldAddNoAcceleratorWhenKvmIsEnabledByAFlag() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.flag("enable-kvm");
    final VmProcess process = this.process(settings, configuration, Os.MAC, qemu);
    process.start();
    final List<String> expected = this.command("-enable-kvm");
    final List<List<String>> expectedCommands = List.of(expected);
    assertEquals(expectedCommands, this.commands);
  }

  @Test
  void shouldAskForAPasswordOnlyTheServerKnowsOnTheDisplay() throws IOException {
    final List<String> secrets = new CopyOnWriteArrayList<>();
    final VmSettings settings = this.reachableSettings();
    final VmProcess.Launcher launcher = command -> {
      this.commands.add(command);
      final int object = command.indexOf("-object");
      final boolean hasSecretObject = object > 0;
      assertTrue(hasSecretObject, () -> "a secret object in " + command);
      final String secret = command.get(object + 1);
      final String prefix = "secret,id=mcav-vnc-password,file=";
      final boolean prefixed = secret.startsWith(prefix);
      assertTrue(prefixed, secret);
      final Path file = Path.of(secret.substring(prefix.length()));
      secrets.add(Files.readString(file, StandardCharsets.US_ASCII));
      this.openDisplay();
      return FakeProcess.running("");
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      VmConfiguration.builder(),
      launcher,
      Os.LINUX,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      this::secretFolder
    );
    process.start();
    try {
      final List<String> command = this.commands.getFirst();
      final String vnc = command.get(command.indexOf("-vnc") + 1);
      final boolean protectedByPassword = vnc.endsWith(",password-secret=mcav-vnc-password");
      final int secretCount = secrets.size();
      assertTrue(protectedByPassword, vnc);
      assertEquals(1, secretCount);
      final String password = secrets.getFirst();
      final boolean wellFormed = password.matches("[A-Za-z0-9]{8}");
      assertTrue(wellFormed, password);
      final String secret = command.get(command.indexOf("-object") + 1);
      final Path file = Path.of(secret.substring(secret.indexOf("file=") + 5));
      final boolean leftOnDisk = Files.exists(file);
      assertFalse(leftOnDisk, "QEMU read the password, so it is not left on disk");
    } finally {
      process.shutdown();
    }
  }

  @Test
  void shouldGiveEveryProcessAPasswordOfItsOwn() {
    final VmProcess first = this.reachable(Os.LINUX);
    final VmProcess second = this.reachable(Os.LINUX);
    final String firstPassword = first.getPassword();
    final String secondPassword = second.getPassword();
    final boolean firstWellFormed = firstPassword.matches("[A-Za-z0-9]{8}");
    final boolean secondWellFormed = secondPassword.matches("[A-Za-z0-9]{8}");
    final boolean samePassword = firstPassword.equals(secondPassword);
    assertTrue(firstWellFormed, firstPassword);
    assertTrue(secondWellFormed, secondPassword);
    assertFalse(samePassword, "random passwords");
  }

  @Test
  void shouldOpenThePasswordFolderOnlyToTheServersUser() throws IOException {
    final Map<String, String> posixZip = Map.of("create", "true", "enablePosixFileAttributes", "true");
    try (final FileSystem zipped = FileSystems.newFileSystem(this.directory.resolve("folders.zip"), posixZip)) {
      // Windows' own file system has no POSIX permissions (VmProcess asks for them only where it has), so there the
      // POSIX branch makes its folder in the zip file system, which has them
      final boolean posixHere = this.directory.getFileSystem().supportedFileAttributeViews().contains("posix");
      final Path posix = VmProcess.createSecretFolder(posixHere ? this.directory : zipped.getPath("/"), true);
      final String permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(posix));
      assertEquals("rwx------", permissions);
      final Path windows = VmProcess.createSecretFolder(this.directory, false);
      final boolean windowsFolderMade = Files.isDirectory(windows);
      final boolean sameFolder = posix.equals(windows);
      assertTrue(windowsFolderMade);
      assertFalse(sameFolder, "a folder of its own for every start");
      final Path zippedFolder = VmProcess.createSecretFolder(zipped.getPath("/"), true);
      final String zippedPermissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(zippedFolder));
      assertEquals("rwx------", zippedPermissions);
    }
  }

  @Test
  void shouldTellTheLauncherWhenTheMachineItStartedHasEnded() {
    final FakeProcess qemu = FakeProcess.running("");
    final List<Process> ended = new ArrayList<>();
    final VmProcess.Launcher launcher = new VmProcess.Launcher() {
      @Override
      public Process launch(final List<String> command) throws IOException {
        VmProcessTest.this.openDisplay();
        return qemu;
      }

      @Override
      public void ended(final Process process) {
        ended.add(process);
      }
    };
    final VmProcess process = new VmProcess(
      this.reachableSettings(),
      VmPlayer.Architecture.AARCH64,
      QEMU,
      VmConfiguration.builder(),
      launcher,
      Os.WINDOWS,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      this::secretFolder
    );
    process.start();
    final List<Process> endedWhileRunning = List.copyOf(ended);
    process.shutdown();

    final List<Process> expectedEnded = List.of(qemu);
    assertEquals(List.of(), endedWhileRunning);
    assertEquals(expectedEnded, ended, "a launcher that records its processes forgets one once it ended");
  }

  @Test
  void shouldWriteACommaOfThePasswordPathTwice() throws IOException {
    final VmSettings settings = this.reachableSettings();
    final Path folder = this.directory.resolve("a,b");
    final VmProcess.Launcher launcher = command -> {
      this.commands.add(command);
      this.openDisplay();
      return FakeProcess.running("");
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      VmConfiguration.builder(),
      launcher,
      Os.LINUX,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      () -> Files.createDirectories(folder)
    );
    process.start();
    process.shutdown();
    final List<String> command = this.commands.getFirst();
    final String secret = command.get(command.indexOf("-object") + 1);
    final String expected = "secret,id=mcav-vnc-password,file=" + this.directory.resolve("a,,b").resolve("password");
    assertEquals(expected, secret);
  }

  @Test
  void shouldStopTheStartBeforeQemuWhenThePasswordCannotBeWritten() {
    final VmSettings settings = this.reachableSettings();
    final VmProcess.Launcher launcher = command -> {
      throw new AssertionError("QEMU must not start without the password of its display!");
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      VmConfiguration.builder(),
      launcher,
      Os.LINUX,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      () -> {
        throw new IOException("disk full!");
      }
    );
    final PlayerException failure = assertThrows(PlayerException.class, process::start);
    final String message = failure.getMessage();
    final boolean alive = process.isAlive();
    assertEquals("Password of the display could not be written because of \"disk full!\"!", message);
    assertFalse(alive);
  }

  @Test
  void shouldDeleteThePasswordOfAFailedStart() {
    final VmProcess process = this.reachable(Os.LINUX, new IOException("no QEMU!"));
    assertThrows(PlayerException.class, process::start);
    final Path folder = this.directory.resolve("secret");
    final boolean folderLeft = Files.exists(folder);
    assertFalse(folderLeft, "the password and its folder are gone");
  }

  @Test
  void shouldLeaveTheMachineRunningWhenThePasswordFolderCannotBeDeleted() throws IOException {
    final VmSettings settings = this.reachableSettings();
    final Path folder = this.directory.resolve("secret");
    final VmProcess.Launcher launcher = command -> {
      Files.writeString(folder.resolve("other"), "kept");
      this.openDisplay();
      return FakeProcess.running("");
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.AARCH64,
      QEMU,
      VmConfiguration.builder(),
      launcher,
      Os.LINUX,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      this::secretFolder
    );
    process.start();
    try {
      final boolean alive = process.isAlive();
      final boolean passwordLeft = Files.exists(folder.resolve("password"));
      final boolean otherKept = Files.exists(folder.resolve("other"));
      assertTrue(alive);
      assertFalse(passwordLeft, "the password itself is deleted");
      assertTrue(otherKept);
    } finally {
      process.shutdown();
    }
  }

  @Test
  void shouldGiveAMachineThatChoseNoNetworkOneThatReachesNeitherTheServerNorTheInternet() {
    final VmSettings settings = new VmSettings(5907, 64, 48, 10);
    final VmConfiguration configuration = VmConfiguration.builder();
    final List<String> command = this.commandWithoutAccelerator(settings, configuration);
    final int network = command.indexOf("-nic");
    final boolean hasNetwork = network > 0;
    assertTrue(hasNetwork, () -> "a -nic option in " + command);
    final String networkValue = command.get(network + 1);
    final int lastNetwork = command.lastIndexOf("-nic");
    assertEquals("user,restrict=on", networkValue);
    assertEquals(network, lastNetwork, "one network");
  }

  @ParameterizedTest
  @ValueSource(strings = { "nic", "netdev", "net" })
  void shouldKeepTheNetworkAMachineChose(final String option) {
    final VmSettings settings = new VmSettings(5907, 64, 48, 10);
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.option(option, "user,id=mine");
    final List<String> command = this.commandWithoutAccelerator(settings, configuration);
    final boolean restricted = command.contains("user,restrict=on");
    assertFalse(restricted, () -> "no network added to " + command);
  }

  @Test
  void shouldGiveAMachineWithoutDefaultDevicesNoNetwork() {
    final VmSettings settings = new VmSettings(5907, 64, 48, 10);
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.flag("nodefaults");
    final List<String> command = this.commandWithoutAccelerator(settings, configuration);
    final boolean network = command.contains("-nic");
    assertFalse(network, () -> "no network added to " + command);
  }

  @Test
  void shouldLeaveOutTheDefaultsTheConfigurationSets() {
    final VmSettings settings = new VmSettings(5907, 64, 48, 10);
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.vga("virtio");
    configuration.option("display", "gtk");
    configuration.option("usbdevice", "tablet");
    final VmProcess process = this.process(settings, configuration, Os.LINUX);
    final List<String> command = process.buildCommand(null, this.passwordFile());
    final String program = QEMU.toString();
    final List<String> expected = List.of(
      program,
      "-vga",
      "virtio",
      "-display",
      "gtk",
      "-usbdevice",
      "tablet",
      "-nic",
      "user,restrict=on",
      "-object",
      this.secretObject(),
      "-vnc",
      "127.0.0.1:7,share=force-shared,password-secret=mcav-vnc-password"
    );
    assertEquals(expected, command);
  }

  private List<String> x86Command(final VmConfiguration configuration) {
    final VmSettings settings = new VmSettings(5903, 64, 48, 10);
    final VmProcess.Launcher launcher = command -> {
      throw new IOException("not started!");
    };
    final VmProcess process = new VmProcess(
      settings,
      VmPlayer.Architecture.X86_64,
      QEMU,
      configuration,
      launcher,
      Os.LINUX,
      this.missingKvm,
      SHORT_TIMEOUT_MILLISECONDS,
      this::secretFolder
    );
    return process.buildCommand(null, this.passwordFile());
  }

  @Test
  void shouldGiveAPcMachineAnHdaCardThePcSpeakerAndTheSoundOnItsDisplay() {
    final List<String> command = this.x86Command(VmConfiguration.builder());
    final List<String> expected = List.of(
      QEMU.toString(),
      "-machine",
      "pcspk-audiodev=mcav-audio",
      "-vga",
      "std",
      "-display",
      "none",
      "-nic",
      "user,restrict=on",
      "-object",
      this.secretObject(),
      "-audiodev",
      "none,id=mcav-audio,out.frequency=48000",
      "-device",
      "intel-hda,id=mcav-sound",
      "-device",
      "hda-output,bus=mcav-sound.0,audiodev=mcav-audio",
      "-vnc",
      "127.0.0.1:3,share=force-shared,password-secret=mcav-vnc-password,audiodev=mcav-audio",
      "-usb",
      "-device",
      "usb-tablet"
    );
    assertEquals(expected, command);
  }

  @Test
  void shouldRouteTheSpeakerAsAMachineOptionOfItsOwnAndGiveQ35TheIch9Card() {
    final VmConfiguration q35 = VmConfiguration.builder();
    q35.machine("q35,accel=tcg");
    final List<String> command = this.x86Command(q35);
    final boolean keepsTheMachine = command.containsAll(List.of("-machine", "q35,accel=tcg"));
    assertTrue(keepsTheMachine, command::toString);
    final int speaker = command.indexOf("pcspk-audiodev=mcav-audio");
    final String speakerOption = command.get(speaker - 1);
    assertEquals("-machine", speakerOption, command::toString);
    final boolean ich9Card = command.contains("ich9-intel-hda,id=mcav-sound");
    assertTrue(ich9Card, command::toString);
    final VmConfiguration typed = VmConfiguration.builder();
    typed.machine("type=pc-q35-8.2,usb=on");
    final boolean typedIch9Card = this.x86Command(typed).contains("ich9-intel-hda,id=mcav-sound");
    assertTrue(typedIch9Card);
    final VmConfiguration i440fx = VmConfiguration.builder();
    i440fx.machine("pc-i440fx-8.2");
    final boolean i440fxCard = this.x86Command(i440fx).contains("intel-hda,id=mcav-sound");
    assertTrue(i440fxCard);
  }

  @Test
  void shouldGiveMachinesWithoutAPcSpeakerOrAPciBusNoSound() {
    final VmConfiguration microvm = VmConfiguration.builder();
    microvm.machine("microvm");
    final List<String> command = this.x86Command(microvm);
    final boolean audioDevice = command.contains("-audiodev");
    final boolean display = command.contains("127.0.0.1:3,share=force-shared,password-secret=mcav-vnc-password");
    assertFalse(audioDevice, command::toString);
    assertTrue(display, command::toString);
    final VmConfiguration twice = VmConfiguration.builder();
    twice.machine("type=pc,type=microvm,usb=off");
    final boolean twiceAudioDevice = this.x86Command(twice).contains("-audiodev");
    assertFalse(twiceAudioDevice);
    final List<String> arm = this.commandWithoutAccelerator(this.reachableSettings(), VmConfiguration.builder());
    final boolean armAudioDevice = arm.contains("-audiodev");
    assertFalse(armAudioDevice);
  }

  @Test
  void shouldGiveAMachineNamedWithMSoundLikeOneNamedWithMachine() {
    final VmConfiguration legacy = VmConfiguration.builder();
    legacy.option("M", "q35");
    final List<String> command = this.x86Command(legacy);
    final boolean ich9Card = command.contains("ich9-intel-hda,id=mcav-sound");
    assertTrue(ich9Card, command::toString);
    final boolean speakerRouted = command.contains("pcspk-audiodev=mcav-audio");
    assertTrue(speakerRouted, command::toString);
    final VmConfiguration routed = VmConfiguration.builder();
    routed.option("M", "pc,pcspk-audiodev=snd0");
    assertThrows(PlayerException.class, () -> VmProcess.checkModuleOptions(routed));
    final VmConfiguration repeated = VmConfiguration.builder();
    repeated.repeatable("machine", "pcspk-audiodev=snd0");
    assertThrows(PlayerException.class, () -> VmProcess.checkModuleOptions(repeated));
  }

  @Test
  void shouldNotConfuseTheSpeakerWithAValueThatLooksLikeTheMachineOption() {
    final VmConfiguration named = VmConfiguration.builder();
    named.option("name", "-machine");
    final List<String> command = this.x86Command(named);
    final boolean keepsTheName = command.containsAll(List.of("-name", "-machine"));
    final String speakerOption = command.get(command.indexOf("pcspk-audiodev=mcav-audio") - 1);
    assertTrue(keepsTheName, command::toString);
    assertEquals("-machine", speakerOption, command::toString);
    final VmConfiguration followed = VmConfiguration.builder();
    followed.machine("q35");
    followed.option("name", "-M");
    followed.cpu("host");
    final List<String> sound = this.x86Command(followed);
    final boolean ich9Card = sound.contains("ich9-intel-hda,id=mcav-sound");
    final boolean speakerRouted = sound.contains("pcspk-audiodev=mcav-audio");
    assertTrue(ich9Card, sound::toString);
    assertTrue(speakerRouted, sound::toString);
  }

  @Test
  void shouldReadTheMachineTypeFromTheMachineOption() {
    final VmConfiguration none = VmConfiguration.builder();
    final String noneType = VmProcess.machineType(none);
    assertEquals("", noneType);
    final VmConfiguration named = VmConfiguration.builder();
    named.machine("Q35,usb=on");
    final String namedType = VmProcess.machineType(named);
    assertEquals("q35", namedType);
    final VmConfiguration typed = VmConfiguration.builder();
    typed.machine("usb=on,type=PC");
    final String typedType = VmProcess.machineType(typed);
    assertEquals("pc", typedType);
    final VmConfiguration properties = VmConfiguration.builder();
    properties.machine("accel=kvm");
    final String propertiesType = VmProcess.machineType(properties);
    assertEquals("", propertiesType);
    final VmConfiguration last = VmConfiguration.builder();
    last.machine("pc,type=q35");
    final String lastType = VmProcess.machineType(last);
    assertEquals("q35", lastType);
    final VmConfiguration both = VmConfiguration.builder();
    both.machine("pc");
    both.option("M", "microvm");
    both.repeatable("machine", "usb=on");
    final String bothType = VmProcess.machineType(both);
    assertEquals("microvm", bothType);
    final VmConfiguration aliases = VmConfiguration.builder();
    aliases.option("M", "q35");
    aliases.machine("microvm");
    final String aliasesType = VmProcess.machineType(aliases);
    assertEquals("microvm", aliasesType);
    final VmConfiguration repeated = VmConfiguration.builder();
    repeated.repeatable("M", "microvm");
    repeated.repeatable("machine", "q35");
    final String repeatedType = VmProcess.machineType(repeated);
    assertEquals("q35", repeatedType);
    final VmConfiguration empty = VmConfiguration.builder();
    empty.machine(",usb=on");
    final String emptyType = VmProcess.machineType(empty);
    assertEquals("", emptyType);
    for (final String lookalike : List.of("-M", "-machine")) {
      final VmConfiguration valued = VmConfiguration.builder();
      valued.machine("q35");
      valued.option("name", lookalike);
      valued.cpu("host");
      valued.repeatable("device", lookalike);
      valued.repeatable("device", "e1000");
      final String valuedType = VmProcess.machineType(valued);
      assertEquals("q35", valuedType, valued::toString);
    }
    final boolean defaultIsPc = VmProcess.isPcMachine("");
    final boolean pcIsPc = VmProcess.isPcMachine("pc");
    final boolean q35IsPc = VmProcess.isPcMachine("q35");
    final boolean versionedQ35IsPc = VmProcess.isPcMachine("pc-q35-9.0");
    final boolean microvmIsPc = VmProcess.isPcMachine("microvm");
    final boolean isapcIsPc = VmProcess.isPcMachine("isapc");
    assertTrue(defaultIsPc);
    assertTrue(pcIsPc);
    assertTrue(q35IsPc);
    assertTrue(versionedQ35IsPc);
    assertFalse(microvmIsPc);
    assertFalse(isapcIsPc);
  }

  @Test
  void shouldRefuseAConfigurationThatSetsTheDisplayOrTheSoundOfTheMachine() {
    for (final String option : List.of("vnc", "audio", "audiodev")) {
      final VmConfiguration configuration = VmConfiguration.builder();
      configuration.option(option, "x");
      final PlayerException refused = assertThrows(PlayerException.class, () -> VmProcess.checkModuleOptions(configuration));
      final String expected = "Option -%s is set by MCAV itself and must be removed from the configuration!".formatted(option);
      final String message = refused.getMessage();
      assertEquals(expected, message);
    }
    final VmConfiguration speaker = VmConfiguration.builder();
    speaker.machine("pc,pcspk-audiodev=snd0");
    final PlayerException refused = assertThrows(PlayerException.class, () -> VmProcess.checkModuleOptions(speaker));
    final String message = refused.getMessage();
    assertEquals("PC speaker is routed by MCAV itself, so pcspk-audiodev must be removed from -machine!", message);
    final VmConfiguration fine = VmConfiguration.builder();
    fine.machine("q35");
    VmProcess.checkModuleOptions(fine);
    final VmSettings settings = this.reachableSettings();
    final VmConfiguration display = VmConfiguration.builder();
    display.option("vnc", ":7");
    final VmProcess process = this.process(settings, display, Os.LINUX);
    assertThrows(PlayerException.class, process::start);
    assertEquals(List.of(), this.commands, "a refused configuration never starts QEMU");
  }

  @Test
  void shouldAddNoDisplayWhenTheMachineRunsWithoutGraphics() {
    final VmSettings settings = new VmSettings(5901, 64, 48, 10);
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.flag("nographic");
    final VmProcess process = this.process(settings, configuration, Os.LINUX);
    final List<String> command = process.buildCommand("tcg", this.passwordFile());
    final String program = QEMU.toString();
    final List<String> expected = List.of(
      program,
      "-nographic",
      "-accel",
      "tcg",
      "-vga",
      "std",
      "-nic",
      "user,restrict=on",
      "-object",
      this.secretObject(),
      "-vnc",
      "127.0.0.1:1,share=force-shared,password-secret=mcav-vnc-password",
      "-usb",
      "-device",
      "usb-tablet"
    );
    assertEquals(expected, command);
  }

  @Test
  void shouldPickTheAcceleratorOfEveryOperatingSystem() throws IOException {
    final Path writableKvm = this.directory.resolve("writable-kvm");
    Files.writeString(writableKvm, "");
    final String windows = VmProcess.detectAccelerator(Os.WINDOWS, this.missingKvm);
    final String mac = VmProcess.detectAccelerator(Os.MAC, this.missingKvm);
    final String freeBsd = VmProcess.detectAccelerator(Os.FREEBSD, this.missingKvm);
    final String linuxWithoutKvm = VmProcess.detectAccelerator(Os.LINUX, this.missingKvm);
    final String linuxWithKvm = VmProcess.detectAccelerator(Os.LINUX, writableKvm);
    assertEquals("whpx", windows);
    assertEquals("hvf", mac);
    assertEquals("tcg", freeBsd);
    assertEquals("tcg", linuxWithoutKvm);
    assertEquals("kvm", linuxWithKvm);
  }

  @Test
  void shouldBlameTheAcceleratorOnlyForMessagesThatMentionIt() {
    final boolean named = VmProcess.mentionsAccelerator("HVF: error", "hvf");
    final boolean generic = VmProcess.mentionsAccelerator("no ACCEL", "hvf");
    final boolean hypervisor = VmProcess.mentionsAccelerator("Hypervisor.framework missing", "hvf");
    final boolean virtualization = VmProcess.mentionsAccelerator("enable VIRTUALIZATION", "hvf");
    final boolean unrelated = VmProcess.mentionsAccelerator("disk not found", "hvf");
    final boolean missing = VmProcess.mentionsAccelerator(null, "hvf");
    assertTrue(named);
    assertTrue(generic);
    assertTrue(hypervisor);
    assertTrue(virtualization);
    assertFalse(unrelated);
    assertFalse(missing);
  }

  @Test
  void shouldFailWhenQemuCannotBeLaunched() {
    final IOException failure = new IOException("CreateProcess error=2!");
    final VmProcess process = this.reachable(Os.FREEBSD, failure);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    final boolean alive = process.isAlive();
    assertEquals("QEMU could not be started because of \"CreateProcess error=2!\"!", message);
    assertSame(failure, cause);
    assertFalse(alive);
  }

  @Test
  void shouldFailToStartAProcessCreatedForAMissingProgram() {
    final VmSettings settings = this.reachableSettings();
    final Path missing = this.directory.resolve("missing-qemu");
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess process = VmProcess.create(settings, VmPlayer.Architecture.X86_64, missing, configuration);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final Throwable cause = exception.getCause();
    final boolean alive = process.isAlive();
    assertInstanceOf(IOException.class, cause);
    assertFalse(alive);
  }

  @Test
  void shouldReportTheLastFortyLinesOfOutput() {
    final StringBuilder output = new StringBuilder();
    for (int line = 1; line <= 45; line++) {
      final String number = String.format(Locale.ROOT, "%02d", line);
      output.append("line-");
      output.append(number);
      output.append("\r\n");
      output.append("   \n");
    }
    final String text = output.toString();
    final FakeProcess failed = FakeProcess.exited(2, text);
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final List<String> kept = new ArrayList<>();
    for (int line = 6; line <= 45; line++) {
      final String number = String.format(Locale.ROOT, "%02d", line);
      kept.add("line-" + number);
    }
    final String separator = System.lineSeparator();
    final String lines = String.join(separator, kept);
    final String expected = "QEMU exited with code 2, printing \"%s\"!".formatted(lines);
    assertEquals(expected, message);
  }

  @ParameterizedTest
  @ValueSource(strings = { "", "\n" })
  void shouldBoundLongDiagnosticLinesAndKeepTheirSuffix(final String ending) {
    final String discarded = "x".repeat(100_000);
    final String suffix = "z".repeat(4090) + "-tail!";
    final FakeProcess failed = FakeProcess.exited(1, discarded + suffix + ending);
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final String expected = "QEMU exited with code 1, printing \"%s\"!".formatted(suffix);
    assertEquals(expected, message);
  }

  @Test
  void shouldKeepTheLastLineWithoutALineBreak() {
    final FakeProcess failed = FakeProcess.exited(1, "first\nlast words");
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final String separator = System.lineSeparator();
    final String expected = "QEMU exited with code 1, printing \"first%slast words\"!".formatted(separator);
    assertEquals(expected, message);
  }

  @Test
  void shouldKeepTheOutputReadBeforeTheStreamFailed() {
    final InputStream broken = new FailingStream("first\nsecond");
    final FakeProcess failed = FakeProcess.running(broken);
    failed.exit(1);
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final String separator = System.lineSeparator();
    final String expected = "QEMU exited with code 1, printing \"first%ssecond\"!".formatted(separator);
    assertEquals(expected, message);
  }

  @Test
  void shouldDecodeCharactersWhoseBytesArriveSeparately() {
    final InputStream trickle = new OneByteStream("Gerät nicht gefunden: 磁盘\n");
    final FakeProcess failed = FakeProcess.running(trickle);
    failed.exit(1);
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    assertEquals("QEMU exited with code 1, printing \"Gerät nicht gefunden: 磁盘\"!", message);
  }

  @Test
  void shouldGiveUpWhenTheDisplayNeverOpens() throws IOException {
    final FakeProcess qemu = FakeProcess.running("VNC server running on 127.0.0.1:5999\n");
    final VmSettings settings = unreachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess process = this.process(settings, configuration, Os.FREEBSD, qemu);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final int port = settings.getPort();
    final boolean alive = process.isAlive();
    final int destroyCalls = qemu.getDestroyCalls();
    final boolean qemuAlive = qemu.isAlive();
    final String expected =
      "QEMU display on port %s did not become reachable, and QEMU printed \"VNC server running on 127.0.0.1:5999\"!".formatted(port);
    assertEquals(expected, message);
    assertFalse(alive);
    assertEquals(1, destroyCalls);
    assertFalse(qemuAlive);
  }

  @Test
  void shouldStopWaitingForTheDisplayWhenInterrupted() throws IOException {
    final FakeProcess qemu = FakeProcess.running("");
    qemu.interruptingWaits();
    final VmSettings settings = unreachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess process = this.process(settings, configuration, Os.FREEBSD, qemu);
    final PlayerException exception;
    try {
      exception = assertThrows(PlayerException.class, process::start);
    } finally {
      final boolean interrupted = Thread.interrupted();
      assertTrue(interrupted, "the interrupt must be kept for the caller");
    }
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    final boolean alive = qemu.isAlive();
    final int forcibleCalls = qemu.getForcibleDestroyCalls();
    assertEquals("Waiting for QEMU was interrupted!", message);
    assertInstanceOf(InterruptedException.class, cause);
    assertFalse(alive);
    assertEquals(1, forcibleCalls);
  }

  @Test
  void shouldReportTheExitWhenInterruptedWhileReadingTheLastOutput() {
    final InputStream late = new DelayedStream("too late\n", 1_000L);
    final FakeProcess failed = FakeProcess.running(late);
    failed.exit(4);
    final VmProcess process = this.reachable(Os.FREEBSD, failed);
    final Thread current = Thread.currentThread();
    current.interrupt();
    final PlayerException exception;
    try {
      exception = assertThrows(PlayerException.class, process::start);
    } finally {
      final boolean interrupted = Thread.interrupted();
      assertTrue(interrupted, "the interrupt must be kept for the caller");
    }
    final String message = exception.getMessage();
    final boolean messageStartsWith = message.startsWith("QEMU exited with code 4, printing ");
    assertTrue(messageStartsWith, message);
  }

  @Test
  void shouldForceProcessesThatIgnoreTheShutdownRequest() {
    final FakeProcess qemu = FakeProcess.running("");
    qemu.ignoringDestroy();
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    process.shutdown();
    final int destroyCalls = qemu.getDestroyCalls();
    final int forcibleCalls = qemu.getForcibleDestroyCalls();
    final boolean alive = qemu.isAlive();
    assertEquals(1, destroyCalls);
    assertEquals(1, forcibleCalls);
    assertFalse(alive);
  }

  @Test
  void shouldForceTheShutdownWhenInterrupted() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    qemu.interruptingWaits();
    process.shutdown();
    final boolean interrupted = Thread.interrupted();
    final int forcibleCalls = qemu.getForcibleDestroyCalls();
    assertTrue(interrupted);
    assertEquals(1, forcibleCalls);
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void shouldWaitForDelayedForcedTermination(final boolean interruptShutdown) {
    final FakeProcess qemu = FakeProcess.running("");
    qemu.ignoringDestroy();
    qemu.delayingForcedExit(50L);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    if (interruptShutdown) {
      qemu.interruptingFirstWait();
    }
    try {
      process.shutdown();
      final boolean alive = qemu.isAlive();
      final int forced = qemu.getForcibleDestroyCalls();
      assertFalse(alive, "destroyForcibly only requests termination; shutdown must wait for the child to exit");
      assertEquals(1, forced);
    } finally {
      final boolean interrupted = Thread.interrupted();
      assertEquals(interruptShutdown, interrupted, "cleanup preserves interruption for the caller");
    }
  }

  @Test
  void shouldBoundForcedWaitsAndPreserveInterruptsDuringForcedTermination() throws InterruptedException {
    final FakeProcess original = FakeProcess.running("");
    final FakeProcess qemu = Mockito.spy(original);
    qemu.ignoringDestroy();
    qemu.delayingForcedExit(100L);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    final AtomicBoolean interruptedOnce = new AtomicBoolean();
    Mockito.doAnswer(invocation -> {
      final int forced = qemu.getForcibleDestroyCalls();
      if (forced > 0) {
        final long timeout = invocation.getArgument(0);
        final TimeUnit unit = invocation.getArgument(1);
        final long nanoseconds = unit.toNanos(timeout);
        final long maximum = TimeUnit.SECONDS.toNanos(10);
        final boolean withinTheDeadline = nanoseconds > 0 && nanoseconds <= maximum;
        assertTrue(withinTheDeadline, "every forced wait must stay within the total ten-second deadline");
        if (interruptedOnce.compareAndSet(false, true)) {
          throw new InterruptedException("interrupt while forced exit is pending!");
        }
      }
      return invocation.callRealMethod();
    })
      .when(qemu)
      .waitFor(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class));
    try {
      process.shutdown();
      final boolean alive = qemu.isAlive();
      final boolean caught = interruptedOnce.get();
      assertFalse(alive);
      assertTrue(caught);
      final boolean interrupted = Thread.interrupted();
      assertTrue(interrupted, "the forced-exit phase must preserve its own interruption");
    } finally {
      Thread.interrupted();
      qemu.exit(137);
    }
  }

  @Test
  void shouldRetainAProcessThatRemainsAliveAfterTheForcedDeadline() throws InterruptedException {
    final FakeProcess original = FakeProcess.running("");
    final FakeProcess qemu = Mockito.spy(original);
    qemu.ignoringDestroy();
    Mockito.doReturn(qemu).when(qemu).destroyForcibly();
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    final Thread stopping = new Thread(process::shutdown, "stubborn-process-shutdown");
    stopping.setDaemon(true);
    try {
      stopping.start();
      stopping.join(12_000L);
      final boolean stillWaiting = stopping.isAlive();
      assertFalse(stillWaiting, "forced termination has a ten-second deadline even when the child refuses to exit");
      final boolean retained = process.isAlive();
      assertTrue(retained, "a live child remains owned after the deadline so shutdown can be retried");
    } finally {
      qemu.exit(137);
      stopping.interrupt();
      stopping.join(2_000L);
      process.shutdown();
    }
    final int attempts = qemu.getDestroyCalls();
    assertEquals(2, attempts, "the second cleanup still owns the process");
  }

  @Test
  void shouldBoundTheOutputJoinAndInterruptAnUnfinishedReader() throws InterruptedException {
    final CountDownLatch finish = new CountDownLatch(1);
    final BlockingStream output = new BlockingStream(finish);
    final FakeProcess qemu = FakeProcess.running(output);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    final Thread stopping = new Thread(process::shutdown, "bounded-output-shutdown");
    stopping.setDaemon(true);
    try {
      stopping.start();
      final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
      boolean joined = false;
      while (System.nanoTime() < deadline && stopping.isAlive()) {
        if (stopping.getState() == Thread.State.TIMED_WAITING) {
          joined = true;
          break;
        }
        Thread.sleep(1L);
      }
      assertTrue(joined, "shutdown must block in a timed join instead of spinning on reader liveness");
      stopping.join(4_000L);
      final boolean alive = stopping.isAlive();
      assertFalse(alive, "an unfinished reader cannot extend the two-second output deadline indefinitely");
      final boolean interrupted = output.interruptedRead.await(1, TimeUnit.SECONDS);
      assertTrue(interrupted, "a reader that outlives the deadline receives an interrupt");
    } finally {
      finish.countDown();
      stopping.interrupt();
      stopping.join(3_000L);
      process.shutdown();
    }
  }

  @Test
  void shouldPreserveAnInterruptReceivedDuringTheOutputJoinAndStillWaitForTheReader() throws InterruptedException {
    final CountDownLatch finish = new CountDownLatch(1);
    final BlockingStream output = new BlockingStream(finish);
    final FakeProcess qemu = FakeProcess.running(output);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    final AtomicBoolean restored = new AtomicBoolean();
    final AtomicReference<Throwable> failure = new AtomicReference<>();
    final CountDownLatch returned = new CountDownLatch(1);
    final Thread stopping = new Thread(() -> {
      try {
        process.shutdown();
        final Thread current = Thread.currentThread();
        restored.set(current.isInterrupted());
      } catch (final Throwable exception) {
        failure.set(exception);
      } finally {
        returned.countDown();
      }
    }, "interrupted-output-shutdown");
    stopping.setDaemon(true);
    try {
      stopping.start();
      awaitOutputJoin(stopping);
      stopping.interrupt();
      awaitOutputJoin(stopping);
      final long pendingReturns = returned.getCount();
      assertEquals(1, pendingReturns, "interruption must not abandon the live output reader");
      finish.countDown();
      stopping.join(3_000L);
      final boolean stoppingAlive = stopping.isAlive();
      final Throwable readerFailure = failure.get();
      final boolean interruptRestored = restored.get();
      final boolean qemuAlive = qemu.isAlive();
      assertFalse(stoppingAlive);
      assertNull(readerFailure);
      assertTrue(interruptRestored, "the caller receives its interrupt again after the reader finishes");
      assertFalse(qemuAlive);
    } finally {
      finish.countDown();
      stopping.interrupt();
      stopping.join(3_000L);
      process.shutdown();
    }
  }

  private static void awaitOutputJoin(final Thread stopping) throws InterruptedException {
    final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
    while (System.nanoTime() < deadline && stopping.isAlive()) {
      final Thread.State state = stopping.getState();
      if (state == Thread.State.TIMED_WAITING && !stopping.isInterrupted()) {
        final StackTraceElement[] stack = stopping.getStackTrace();
        for (final StackTraceElement frame : stack) {
          final String owner = frame.getClassName();
          final String method = frame.getMethodName();
          if (owner.equals(VmProcess.class.getName()) && method.equals("awaitOutput")) {
            return;
          }
        }
      }
      Thread.sleep(1L);
    }
    fail("Shutdown must be waiting inside the timed join of awaitOutput!");
  }

  @Test
  void shouldDrainFinalOutputBeforeReturningFromShutdown() {
    final DelayedStream output = new DelayedStream("final diagnostic\n", 500L);
    final FakeProcess qemu = FakeProcess.running(output);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    process.shutdown();
    final boolean alive = qemu.isAlive();
    final boolean outputClosed = output.closed;
    assertFalse(alive);
    assertTrue(outputClosed, "shutdown must wait for the output reader to consume and close the stream");
    assertEquals(output.content.length, output.position, "shutdown consumes the entire final diagnostic before closing it");
  }

  @Test
  void shouldShutDownOnlyOnce() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.shutdown();
    process.start();
    process.shutdown();
    process.shutdown();
    final int destroyCalls = qemu.getDestroyCalls();
    assertEquals(1, destroyCalls);
  }

  @Test
  void shouldFailFastWhenTheVncPortIsTaken() throws IOException {
    final ServerSocket other = this.listen(this.port);
    final FakeProcess qemu = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final int launches = this.commands.size();
    final boolean namesPort = message.startsWith("VNC port " + this.port + " is already in use");
    final boolean otherOpen = !other.isClosed();
    assertTrue(namesPort, message);
    assertEquals(0, launches);
    assertTrue(otherOpen);
  }

  @Test
  void shouldFailWhenQemuExitsRightAfterItsDisplayOpens() {
    final FakeProcess qemu = FakeProcess.running("Failed to start VNC server: address in use\n");
    qemu.exitingWhenWaitedOn(1);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    final PlayerException exception = assertThrows(PlayerException.class, process::start);
    final String message = exception.getMessage();
    final boolean alive = process.isAlive();
    assertEquals("QEMU exited with code 1, printing \"Failed to start VNC server: address in use\"!", message);
    assertFalse(alive);
  }

  @Test
  void shouldLeaveOutTheDefaultTabletWhenTheConfigurationAddsOne() {
    final VmSettings settings = new VmSettings(5901, 64, 48, 10);
    final VmConfiguration plain = VmConfiguration.builder();
    plain.device("usb-tablet");
    final VmConfiguration withBus = VmConfiguration.builder();
    withBus.device("usb-tablet,bus=usb-bus.0");
    final VmConfiguration otherDevice = VmConfiguration.builder();
    otherDevice.device("virtio-net-pci");

    final List<String> plainCommand = this.commandWithoutAccelerator(settings, plain);
    final List<String> withBusCommand = this.commandWithoutAccelerator(settings, withBus);
    final List<String> otherCommand = this.commandWithoutAccelerator(settings, otherDevice);

    final List<String> controllerOnly = List.of("-usb");
    final List<String> controllerAndTablet = List.of("-usb", "-device", "usb-tablet");
    final List<String> plainOptions = List.of("-device", "usb-tablet");
    final List<String> withBusOptions = List.of("-device", "usb-tablet,bus=usb-bus.0");
    final List<String> otherOptions = List.of("-device", "virtio-net-pci");
    final List<String> expectedPlain = this.expectedOnDisplayOne(plainOptions, controllerOnly);
    final List<String> expectedWithBus = this.expectedOnDisplayOne(withBusOptions, controllerOnly);
    final List<String> expectedOther = this.expectedOnDisplayOne(otherOptions, controllerAndTablet);
    assertEquals(expectedPlain, plainCommand);
    assertEquals(expectedWithBus, withBusCommand);
    assertEquals(expectedOther, otherCommand);
  }

  @Test
  void shouldHonorUsbOffThroughEitherMachineAliasAndItsLastOverride() {
    final VmSettings settings = new VmSettings(5901, 64, 48, 10);
    for (final String alias : List.of("M", "machine")) {
      final String other = alias.equals("M") ? "machine" : "M";
      final List<VmConfiguration> disabled = List.of(
        VmConfiguration.builder().option(alias, "q35,usb=off"),
        VmConfiguration.builder().option(other, "q35,usb=on").option(alias, "usb=off"),
        VmConfiguration.builder().repeatable(alias, "q35,usb=on").repeatable(alias, "usb=off"),
        VmConfiguration.builder().option(alias, "q35,usb=on,usb=off")
      );
      for (final VmConfiguration configuration : disabled) {
        final List<String> command = this.commandWithoutAccelerator(settings, configuration);
        final boolean usbBus = command.contains("-usb");
        final boolean usbTablet = command.contains("usb-tablet");
        assertFalse(usbBus, configuration::toString);
        assertFalse(usbTablet, configuration::toString);
      }
      final VmConfiguration enabled = VmConfiguration.builder().option(other, "q35,usb=off").option(alias, "usb=on");
      final List<String> enabledCommand = this.commandWithoutAccelerator(settings, enabled);
      final boolean enabledUsbBus = enabledCommand.contains("-usb");
      final boolean enabledUsbTablet = enabledCommand.contains("usb-tablet");
      assertFalse(enabledUsbBus, enabled::toString);
      assertTrue(enabledUsbTablet, enabled::toString);
    }
  }

  @Test
  void shouldLetTheConfigurationChooseOrTurnOffUsb() {
    final VmSettings settings = new VmSettings(5901, 64, 48, 10);
    final VmConfiguration usbOn = VmConfiguration.builder();
    usbOn.machine("q35,usb=on");
    final VmConfiguration usbOff = VmConfiguration.builder();
    usbOff.machine("q35,usb=off");
    final VmConfiguration usbFlag = VmConfiguration.builder();
    usbFlag.flag("usb");

    final List<String> onCommand = this.commandWithoutAccelerator(settings, usbOn);
    final List<String> offCommand = this.commandWithoutAccelerator(settings, usbOff);
    final List<String> flagCommand = this.commandWithoutAccelerator(settings, usbFlag);

    final List<String> tabletOnly = List.of("-device", "usb-tablet");
    final List<String> nothing = List.of();
    final List<String> onOptions = List.of("-machine", "q35,usb=on");
    final List<String> offOptions = List.of("-machine", "q35,usb=off");
    final List<String> flagOptions = List.of("-usb");
    final List<String> expectedOn = this.expectedOnDisplayOne(onOptions, tabletOnly);
    final List<String> expectedOff = this.expectedOnDisplayOne(offOptions, nothing);
    final List<String> expectedFlag = this.expectedOnDisplayOne(flagOptions, tabletOnly);
    assertEquals(expectedOn, onCommand);
    assertEquals(expectedOff, offCommand);
    assertEquals(expectedFlag, flagCommand);
  }

  @Test
  void shouldNoticeWhenQemuExitsOnItsOwn() {
    final FakeProcess qemu = FakeProcess.running("");
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    process.start();
    qemu.exit(0);
    final boolean alive = process.isAlive();
    assertFalse(alive);
  }

  @Test
  void shouldDrainTheOutputOnADaemonThreadSoItNeverKeepsTheJvmAlive() {
    final CountDownLatch finish = new CountDownLatch(1);
    final InputStream blocking = new BlockingStream(finish);
    final FakeProcess qemu = FakeProcess.running(blocking);
    final VmProcess process = this.reachable(Os.FREEBSD, qemu);
    try {
      process.start();
      final List<Thread> drains = awaitDrainThreads();
      for (final Thread thread : drains) {
        final boolean isDaemon = thread.isDaemon();
        final String name = thread.getName();
        assertTrue(isDaemon, name);
      }
    } finally {
      finish.countDown();
      process.shutdown();
    }
  }

  /**
   * Waits until the thread that reads the output of QEMU runs.
   *
   * @return the live drain threads
   */
  private static List<Thread> awaitDrainThreads() {
    final long timeoutNanoseconds = TimeUnit.SECONDS.toNanos(10);
    final long pauseNanoseconds = TimeUnit.MILLISECONDS.toNanos(5);
    final long deadline = System.nanoTime() + timeoutNanoseconds;
    while (true) {
      final List<Thread> found = drainThreads();
      final boolean empty = found.isEmpty();
      if (!empty) {
        return found;
      }
      final long now = System.nanoTime();
      if (now - deadline > 0) {
        fail("Output of QEMU is never drained!");
      }
      LockSupport.parkNanos(pauseNanoseconds);
    }
  }

  private static List<Thread> drainThreads() {
    final Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
    final Set<Thread> threads = stackTraces.keySet();
    final List<Thread> running = new ArrayList<>();
    for (final Thread thread : threads) {
      final String name = thread.getName();
      final boolean drain = name.equals("mcav-qemu-output");
      final boolean alive = thread.isAlive();
      if (drain && alive) {
        running.add(thread);
      }
    }
    return running;
  }

  @Test
  void shouldKeepTheInterruptOfTheThreadThatWaitedForTheDisplay() throws IOException {
    final FakeProcess qemu = FakeProcess.running("");
    qemu.interruptingFirstWait();
    final VmSettings settings = unreachableSettings();
    final VmConfiguration configuration = VmConfiguration.builder();
    final VmProcess process = this.process(settings, configuration, Os.FREEBSD, qemu);
    final PlayerException exception;
    try {
      exception = assertThrows(PlayerException.class, process::start);
    } finally {
      final boolean interrupted = Thread.interrupted();
      assertTrue(interrupted, "the interrupt of the waiting thread must reach the caller");
    }
    final String message = exception.getMessage();
    assertEquals("Waiting for QEMU was interrupted!", message);
  }

  /**
   * A stream that blocks until it is released, and then reports the end of the output.
   */
  private static final class BlockingStream extends InputStream {

    private final CountDownLatch finish;
    private final CountDownLatch interruptedRead = new CountDownLatch(1);

    BlockingStream(final CountDownLatch finish) {
      this.finish = finish;
    }

    @Override
    public int read() {
      this.awaitFinish();
      return -1;
    }

    @Override
    public int read(final byte @NonNull [] buffer, final int offset, final int length) {
      this.awaitFinish();
      return -1;
    }

    private void awaitFinish() {
      try {
        this.finish.await();
      } catch (final InterruptedException exception) {
        this.interruptedRead.countDown();
        final Thread current = Thread.currentThread();
        current.interrupt();
      }
    }
  }

  /**
   * A stream that holds its content back for a while, then returns it.
   */
  private static final class DelayedStream extends InputStream {

    private final byte[] content;
    private final long delayMilliseconds;
    private int position;
    private volatile boolean closed;

    DelayedStream(final String text, final long delayMilliseconds) {
      this.content = text.getBytes(StandardCharsets.UTF_8);
      this.delayMilliseconds = delayMilliseconds;
    }

    @Override
    public void close() {
      this.closed = true;
    }

    @Override
    public int read() {
      final byte[] single = new byte[1];
      final int count = this.read(single, 0, 1);
      return count < 0 ? -1 : single[0] & 0xFF;
    }

    @Override
    public int read(final byte @NonNull [] buffer, final int offset, final int length) {
      if (this.position == 0) {
        try {
          Thread.sleep(this.delayMilliseconds);
        } catch (final InterruptedException exception) {
          final Thread current = Thread.currentThread();
          current.interrupt();
        }
      }
      if (this.position >= this.content.length) {
        return -1;
      }
      final int count = Math.min(length, this.content.length - this.position);
      System.arraycopy(this.content, this.position, buffer, offset, count);
      this.position += count;
      return count;
    }
  }

  /**
   * A stream that returns its content one byte per read.
   */
  private static final class OneByteStream extends InputStream {

    private final byte[] content;
    private int position;

    OneByteStream(final String text) {
      this.content = text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int read() {
      if (this.position >= this.content.length) {
        return -1;
      }
      final int value = this.content[this.position] & 0xFF;
      this.position++;
      return value;
    }

    @Override
    public int read(final byte @NonNull [] buffer, final int offset, final int length) {
      final int value = this.read();
      if (value < 0) {
        return -1;
      }
      buffer[offset] = (byte) value;
      return 1;
    }
  }

  /**
   * A stream that returns its content and then fails.
   */
  private static final class FailingStream extends InputStream {

    private final byte[] content;
    private boolean delivered;

    FailingStream(final String text) {
      this.content = text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int read() throws IOException {
      throw new IOException("read single bytes is not supported!");
    }

    @Override
    public int read(final byte @NonNull [] buffer, final int offset, final int length) throws IOException {
      if (this.delivered) {
        throw new IOException("Stream closed!");
      }
      this.delivered = true;
      System.arraycopy(this.content, 0, buffer, offset, this.content.length);
      return this.content.length;
    }
  }
}
