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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import me.brandonli.mcav.media.player.PlayerException;
import me.brandonli.mcav.media.player.pipeline.filter.audio.AudioFilter;
import me.brandonli.mcav.utils.os.Os;
import me.brandonli.mcav.utils.os.OsUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A QEMU process with a VNC display on the local machine.
 *
 * <p>The process is started with the accelerator the machine supports; if QEMU exits right away, which is what
 * happens when the accelerator cannot be used, it is started again with software emulation. Output is drained
 * on a background thread and its tail is kept for error messages.
 *
 * <p>The display and the sound of the machine belong to mcav: a configuration that sets {@code -vnc},
 * {@code -audio} or {@code -audiodev}, or routes the PC speaker itself, is refused. The display is shared by every
 * client, and on x86 PC and Q35 machines an Intel HD Audio card and the PC speaker play into an audio backend without
 * a host device, whose sound QEMU's VNC server hands to the audio connection of the player
 * ({@link VmAudioClient}). Other machines have no sound.
 *
 * <p>The VNC port must be free before QEMU starts, and QEMU must still run a moment after the port accepts
 * connections, so the player never connects to another server that listens on the port. The display asks for a random
 * password, which QEMU reads from a file of a folder only the server's user can open; the file is deleted once QEMU
 * started, and the password reaches the players of the display through {@link #getPassword()}.
 */
final class VmProcess {

  static final int FIRST_VNC_PORT = 5900;

  /**
   * The id of the audio backend of the machine, which the sound cards and the VNC display name.
   */
  private static final String AUDIO_ID = "mcav-audio";

  /**
   * The id of the secret object that holds the password of the display.
   */
  private static final String PASSWORD_ID = "mcav-vnc-password";

  private static final String SOUND_CARD_ID = "mcav-sound";

  // VNC's password authentication reads at most eight characters
  private static final int PASSWORD_LENGTH = 8;
  private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  private static final String PASSWORD_FILE = "password";
  private static final String SECRET_FOLDER_PREFIX = "mcav-vm-";
  private static final Path TEMPORARY_FOLDER = Path.of(System.getProperty("java.io.tmpdir"));
  private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
  private static final FileAttribute<Set<PosixFilePermission>> OWNER_ONLY = PosixFilePermissions.asFileAttribute(
    PosixFilePermissions.fromString("rwx------")
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(VmProcess.class);

  static final String LOCALHOST = "127.0.0.1";
  static final InetAddress LOOPBACK = InetAddress.ofLiteral(LOCALHOST);

  private static final String ARGUMENT_SEPARATOR = " ";

  private static final String PROPERTY_SEPARATOR = ",";
  private static final String ESCAPED_PROPERTY_SEPARATOR = ",,";
  private static final String PROPERTY_ASSIGNMENT = "=";
  private static final Splitter PROPERTY_SPLITTER = Splitter.on(PROPERTY_SEPARATOR);

  private static final List<String> MODULE_OPTIONS = List.of("vnc", "audio", "audiodev");
  private static final List<String> ACCELERATOR_OPTIONS = List.of("accel", "enable-kvm");
  private static final List<String> DISPLAY_OPTIONS = List.of("display", "nographic");
  private static final List<String> NETWORK_OPTIONS = List.of("nic", "netdev", "net", "nodefaults");
  private static final Set<String> MACHINE_OPTIONS = Set.of("machine", "M");

  private static final String DEVICE_OPTION = "-device";
  private static final String VNC_OPTION = "-vnc";

  private static final String RESTRICTED_NETWORK = "user,restrict=on";

  private static final String SPEAKER_PROPERTY = "pcspk-audiodev";
  private static final String TYPE_PROPERTY = "type=";

  private static final String PC_MACHINE = "pc";
  private static final String Q35_MACHINE = "q35";
  private static final String PC_VERSION_PREFIX = "pc-";
  private static final String Q35_VERSION_PREFIX = "pc-q35";

  private static final String ICH9_SOUND_CARD = "ich9-intel-hda";
  private static final String SOUND_CARD = "intel-hda";

  private static final String TABLET_DEVICE = "usb-tablet";

  private static final String SOFTWARE_ACCELERATOR = "tcg";
  private static final String KVM_ACCELERATOR = "kvm";
  private static final String WHPX_ACCELERATOR = "whpx";
  private static final String HVF_ACCELERATOR = "hvf";
  private static final Path KVM_DEVICE = Path.of("/dev/kvm");

  private static final List<String> ACCELERATOR_WORDS = List.of("accel", "hypervisor", "virtualization");

  private static final long START_TIMEOUT_MILLISECONDS = 60_000L;
  private static final long POLL_MILLISECONDS = 100L;
  private static final int CONNECT_TIMEOUT_MILLISECONDS = (int) POLL_MILLISECONDS * 5;
  private static final long STOP_TIMEOUT_SECONDS = 10L;
  private static final long OUTPUT_TIMEOUT_MILLISECONDS = 2_000L;

  private static final String OUTPUT_THREAD_NAME = "mcav-qemu-output";
  private static final int READ_BUFFER_CHARACTERS = 4096;
  private static final char LINE_FEED = '\n';
  private static final char CARRIAGE_RETURN = '\r';
  private static final int OUTPUT_TAIL_LINES = 40;
  private static final int OUTPUT_LINE_CHARACTERS = 4096;

  private final VmSettings settings;
  private final VmPlayer.Architecture architecture;
  private final Path executable;
  private final VmConfiguration configuration;
  private final Launcher launcher;
  private final Os os;
  private final Path kvmDevice;
  private final long startTimeoutNanoseconds;
  private final LongSupplier nanosecondClock;
  private final SecretFolder secretFolder;
  private final String password;
  private final Deque<String> outputTail;

  private volatile @Nullable Process process;
  private volatile @Nullable Thread drainThread;

  /**
   * Creates a QEMU process for the current operating system that has not started yet.
   *
   * @param settings      the display settings
   * @param architecture  the guest architecture, which decides whether the machine has sound
   * @param executable    the QEMU program
   * @param configuration the QEMU options
   * @return the process
   */
  static VmProcess create(
    final VmSettings settings,
    final VmPlayer.Architecture architecture,
    final Path executable,
    final VmConfiguration configuration
  ) {
    final Os currentOs = OsUtils.getOs();
    return new VmProcess(
      settings,
      architecture,
      executable,
      configuration,
      new RecordingLauncher(QemuProcessRecords.ofUser()),
      currentOs,
      KVM_DEVICE,
      START_TIMEOUT_MILLISECONDS,
      () -> createSecretFolder(TEMPORARY_FOLDER, POSIX)
    );
  }

  /**
   * Constructs a process with its environment replaced, so tests can run without QEMU.
   *
   * @param settings                 the display settings
   * @param architecture             the guest architecture
   * @param executable               the QEMU program
   * @param configuration            the QEMU options
   * @param launcher                 starts the process from its command line
   * @param os                       the operating system, which decides the accelerator
   * @param kvmDevice                the KVM device, used on Linux
   * @param startTimeoutMilliseconds how long the display may take to accept connections
   * @param secretFolder             creates the folder of the password file of a start
   */
  @VisibleForTesting
  VmProcess(
    final VmSettings settings,
    final VmPlayer.Architecture architecture,
    final Path executable,
    final VmConfiguration configuration,
    final Launcher launcher,
    final Os os,
    final Path kvmDevice,
    final long startTimeoutMilliseconds,
    final SecretFolder secretFolder
  ) {
    this(
      settings,
      architecture,
      executable,
      configuration,
      launcher,
      os,
      kvmDevice,
      startTimeoutMilliseconds,
      System::nanoTime,
      secretFolder
    );
  }

  /** Constructs a process with a monotonic clock for deterministic startup deadline checks. */
  @VisibleForTesting
  VmProcess(
    final VmSettings settings,
    final VmPlayer.Architecture architecture,
    final Path executable,
    final VmConfiguration configuration,
    final Launcher launcher,
    final Os os,
    final Path kvmDevice,
    final long startTimeoutMilliseconds,
    final LongSupplier nanosecondClock,
    final SecretFolder secretFolder
  ) {
    this.settings = settings;
    this.architecture = architecture;
    this.executable = executable;
    this.configuration = configuration;
    this.launcher = launcher;
    this.os = os;
    this.kvmDevice = kvmDevice;
    this.startTimeoutNanoseconds = TimeUnit.MILLISECONDS.toNanos(startTimeoutMilliseconds);
    this.nanosecondClock = nanosecondClock;
    this.secretFolder = secretFolder;
    this.password = createPassword();
    this.outputTail = new ArrayDeque<>();
  }

  private static String createPassword() {
    final StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
    for (int index = 0; index < PASSWORD_LENGTH; index++) {
      final int character = RANDOM.nextInt(PASSWORD_CHARACTERS.length());
      password.append(PASSWORD_CHARACTERS.charAt(character));
    }
    return password.toString();
  }

  /**
   * Creates an empty folder that only the server's user can open. A file system without POSIX permissions is Windows,
   * whose temporary folder of a user is private to that user already.
   *
   * @param parent the folder to create it in
   * @param posix  whether the file system has POSIX permissions
   * @return the folder
   * @throws IOException if it cannot be created
   */
  @VisibleForTesting
  static Path createSecretFolder(final Path parent, final boolean posix) throws IOException {
    if (posix) {
      return Files.createTempDirectory(parent, SECRET_FOLDER_PREFIX, OWNER_ONLY);
    }
    return Files.createTempDirectory(parent, SECRET_FOLDER_PREFIX);
  }

  /**
   * Gets the password of the display, which a VNC client of the machine must send.
   *
   * @return the password, eight letters and digits
   */
  String getPassword() {
    return this.password;
  }

  private static Process startProcess(final List<String> command) throws IOException {
    final ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectErrorStream(true);
    return builder.start();
  }

  /**
   * Starts QEMU and waits until its VNC display accepts connections.
   *
   * @throws PlayerException if the VNC port is taken, or QEMU cannot be started, exits, or never opens its display
   */
  void start() {
    checkModuleOptions(this.configuration);
    this.ensurePortIsFree();
    final Path folder = this.writePassword();
    try {
      this.launchMachine(folder.resolve(PASSWORD_FILE));
    } finally {
      deleteSecret(folder);
    }
  }

  private void launchMachine(final Path passwordFile) {
    final boolean acceleratorConfigured = ACCELERATOR_OPTIONS.stream().anyMatch(this.configuration::has);
    if (acceleratorConfigured) {
      this.launch(null, passwordFile);
      return;
    }

    final String accelerator = detectAccelerator(this.os, this.kvmDevice);
    if (accelerator.equals(SOFTWARE_ACCELERATOR)) {
      this.launch(SOFTWARE_ACCELERATOR, passwordFile);
      return;
    }

    this.launchWithSoftwareFallback(accelerator, passwordFile);
  }

  /**
   * Writes the password of the display into a new private folder.
   *
   * @return the folder
   * @throws PlayerException if the folder or the file cannot be written
   */
  private Path writePassword() {
    try {
      final Path folder = this.secretFolder.create();
      final Path file = folder.resolve(PASSWORD_FILE);
      // QEMU reads the whole file as the password, so it has no line break
      Files.writeString(file, this.password, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      return folder;
    } catch (final IOException exception) {
      final String reason = exception.getMessage();
      final String message = "Password of the display could not be written because of \"%s\"!".formatted(reason);
      throw new PlayerException(message, exception);
    }
  }

  private static void deleteSecret(final Path folder) {
    try {
      Files.deleteIfExists(folder.resolve(PASSWORD_FILE));
      Files.deleteIfExists(folder);
    } catch (final IOException exception) {
      LOGGER.warn("The password folder {} of the virtual machine could not be deleted.", folder, exception);
    }
  }

  /**
   * Starts QEMU with a hardware accelerator, and again with software emulation if QEMU blames the accelerator.
   *
   * @param accelerator  the hardware accelerator to try first
   * @param passwordFile the file that holds the password of the display
   */
  private void launchWithSoftwareFallback(final String accelerator, final Path passwordFile) {
    try {
      this.launch(accelerator, passwordFile);
    } catch (final PlayerException exception) {
      final String reason = exception.getMessage();
      final boolean acceleratorProblem = mentionsAccelerator(reason, accelerator);
      if (!acceleratorProblem) {
        throw exception;
      }

      LOGGER.warn("QEMU failed on the {} accelerator with \"{}\", retrying with software emulation.", accelerator, reason);
      this.launch(SOFTWARE_ACCELERATOR, passwordFile);
    }
  }

  /**
   * Refuses a configuration that sets the display or the sound of the machine, which mcav owns.
   *
   * @param configuration the QEMU options
   * @throws PlayerException if the configuration sets {@code -vnc}, {@code -audio}, {@code -audiodev}, or routes the
   *                         PC speaker
   */
  @VisibleForTesting
  static void checkModuleOptions(final VmConfiguration configuration) {
    for (final String option : MODULE_OPTIONS) {
      if (configuration.has(option)) {
        final String message = "Option -%s is set by MCAV itself and must be removed from the configuration!".formatted(option);
        throw new PlayerException(message);
      }
    }
    for (final String machine : machineValues(configuration)) {
      if (machine.contains(SPEAKER_PROPERTY)) {
        final String message = "PC speaker is routed by MCAV itself, so pcspk-audiodev must be removed from -machine!";
        throw new PlayerException(message);
      }
    }
  }

  /**
   * Checks whether the machine gets sound: x86 PC and Q35 machines do, others have no sound card mcav can add.
   *
   * @return true if the machine plays into the audio backend of mcav
   */
  boolean hasAudio() {
    return this.architecture == VmPlayer.Architecture.X86_64 && isPcMachine(machineType(this.configuration));
  }

  /**
   * Gets the machine type of a configuration as QEMU does: it merges every {@code -machine} and {@code -M} option, a
   * value names the type in its first part or in {@code type=}, and the type named last wins.
   *
   * @param configuration the QEMU options
   * @return the type in lower case, or empty for the default machine of QEMU
   */
  @VisibleForTesting
  static String machineType(final VmConfiguration configuration) {
    String type = "";
    for (final String machine : machineValues(configuration)) {
      boolean first = true;
      for (final String part : PROPERTY_SPLITTER.split(machine)) {
        if (part.startsWith(TYPE_PROPERTY)) {
          type = part.substring(TYPE_PROPERTY.length());
        } else if (first && !part.isEmpty() && !part.contains(PROPERTY_ASSIGNMENT)) {
          type = part;
        }
        first = false;
      }
    }
    return type.toLowerCase(Locale.ROOT);
  }

  /**
   * Gets the values of every {@code -machine} and {@code -M} option of a configuration, in the order QEMU reads them.
   *
   * @param configuration the QEMU options
   * @return the values
   */
  private static List<String> machineValues(final VmConfiguration configuration) {
    // in the order of the arguments QEMU gets: grouped by spelling, -M q35 -machine microvm would read as q35 where QEMU
    // runs microvm; and by option name, as a scan of every argument would read -name -M -cpu host as the machine -cpu
    return configuration.valuesOf(MACHINE_OPTIONS);
  }

  /**
   * Checks whether a machine type is an x86 PC or Q35 machine, which has a PC speaker and a PCI bus for a sound card.
   *
   * @param type the machine type in lower case, empty for the default machine, which is a PC
   * @return true for the default machine, {@code pc}, {@code q35} and their versions
   */
  @VisibleForTesting
  static boolean isPcMachine(final String type) {
    return type.isEmpty() || type.equals(PC_MACHINE) || type.equals(Q35_MACHINE) || type.startsWith(PC_VERSION_PREFIX);
  }

  private static boolean isQ35(final String type) {
    return type.equals(Q35_MACHINE) || type.startsWith(Q35_VERSION_PREFIX);
  }

  private void ensurePortIsFree() {
    final int port = this.settings.getPort();
    final InetSocketAddress address = new InetSocketAddress(LOOPBACK, port);
    try (final ServerSocket probe = new ServerSocket()) {
      probe.setReuseAddress(false);
      probe.bind(address);
    } catch (final IOException exception) {
      final String reason = exception.getMessage();
      final String message = "VNC port %s is already in use because of \"%s\"!".formatted(port, reason);
      throw new PlayerException(message, exception);
    }
  }

  /**
   * Checks whether a failure message of QEMU blames the accelerator.
   *
   * @param reason      the failure message
   * @param accelerator the accelerator that was used
   * @return true if the message mentions the accelerator or hardware virtualization
   */
  @VisibleForTesting
  static boolean mentionsAccelerator(final @Nullable String reason, final String accelerator) {
    final String message = Objects.requireNonNullElse(reason, "");
    final String lower = message.toLowerCase(Locale.ROOT);
    final boolean namesAccelerator = lower.contains(accelerator);
    final boolean blamesVirtualization = ACCELERATOR_WORDS.stream().anyMatch(lower::contains);
    return namesAccelerator || blamesVirtualization;
  }

  private void launch(final @Nullable String accelerator, final Path passwordFile) {
    final List<String> command = this.buildCommand(accelerator, passwordFile);
    final String rendered = String.join(ARGUMENT_SEPARATOR, command);
    LOGGER.debug("QEMU is started with the command line {}.", rendered);

    final Process started;
    try {
      started = this.launcher.launch(command);
    } catch (final IOException exception) {
      final String reason = exception.getMessage();
      final String message = "QEMU could not be started because of \"%s\"!".formatted(reason);
      throw new PlayerException(message, exception);
    }
    this.process = started;

    this.startDraining(started);
    this.waitForDisplay(started);
  }

  /**
   * Starts the background thread that reads the output of a started QEMU process, forgetting the output of an
   * earlier attempt.
   *
   * @param started the started process
   */
  private void startDraining(final Process started) {
    synchronized (this.outputTail) {
      this.outputTail.clear();
    }

    final InputStream output = started.getInputStream();
    final Thread drain = new Thread(() -> this.drain(output), OUTPUT_THREAD_NAME);
    drain.setDaemon(true);
    this.drainThread = drain;
    drain.start();
  }

  /**
   * Builds the command line of QEMU.
   *
   * @param accelerator  the accelerator to add, or null to add none
   * @param passwordFile the file QEMU reads the password of the display from
   * @return the program followed by its arguments
   */
  @VisibleForTesting
  List<String> buildCommand(final @Nullable String accelerator, final Path passwordFile) {
    final List<String> command = new ArrayList<>();
    final String program = this.executable.toString();
    command.add(program);
    final List<String> arguments = this.configuration.getArguments();
    command.addAll(arguments);
    if (this.hasAudio()) {
      routeSpeaker(command);
    }
    if (accelerator != null) {
      command.add("-accel");
      command.add(accelerator);
    }
    this.addDefaultOptions(command, passwordFile);
    return command;
  }

  /**
   * Routes the PC speaker into the audio backend of mcav with a {@code -machine} option of its own, which QEMU merges
   * with those of the configuration.
   *
   * @param command the command line so far, with the options of the configuration
   */
  private static void routeSpeaker(final List<String> command) {
    command.add("-machine");
    command.add(SPEAKER_PROPERTY + PROPERTY_ASSIGNMENT + AUDIO_ID);
  }

  /**
   * Adds the options MCAV relies on: a standard VGA card, no window on the host and a network that reaches neither
   * the host nor the internet unless the configuration sets them itself, the sound of the machine, a VNC display on
   * the configured port that every client shares and that asks for the password, and a USB tablet. The tablet reports absolute coordinates, so VNC
   * pointer positions map straight onto the screen.
   */
  private void addDefaultOptions(final List<String> command, final Path passwordFile) {
    this.addUnlessConfigured(command, "vga", "-vga", "std");
    final boolean hasDisplay = DISPLAY_OPTIONS.stream().anyMatch(this.configuration::has);
    if (!hasDisplay) {
      command.add("-display");
      command.add("none");
    }
    // QEMU's own default network lets the guest reach every service of the host's loopback address (10.0.2.2 inside
    // the guest), so a machine that chose no network gets one that reaches neither the host nor the internet
    final boolean hasNetwork = NETWORK_OPTIONS.stream().anyMatch(this.configuration::has);
    if (!hasNetwork) {
      command.add("-nic");
      command.add(RESTRICTED_NETWORK);
    }
    final int port = this.settings.getPort();
    final int display = port - FIRST_VNC_PORT;
    // QEMU splits its option values at commas, so a comma of the path is written twice
    final String secretFile = passwordFile.toString().replace(PROPERTY_SEPARATOR, ESCAPED_PROPERTY_SEPARATOR);
    final String secret = "secret,id=%s,file=%s".formatted(PASSWORD_ID, secretFile);
    command.add("-object");
    command.add(secret);
    final String vnc = "%s:%s,share=force-shared,password-secret=%s".formatted(LOCALHOST, display, PASSWORD_ID);
    if (this.hasAudio()) {
      this.addAudio(command);
      final String vncWithAudio = "%s,audiodev=%s".formatted(vnc, AUDIO_ID);
      command.add(VNC_OPTION);
      command.add(vncWithAudio);
    } else {
      command.add(VNC_OPTION);
      command.add(vnc);
    }
    this.addUsbTablet(command);
  }

  /**
   * Adds the audio backend, which plays into no host device at the rate of the audio pipeline, and an Intel HD Audio
   * card with an output, the ICH9 variant on Q35 machines.
   *
   * @param command the command line so far
   */
  private void addAudio(final List<String> command) {
    final String backend = "none,id=%s,out.frequency=%s".formatted(AUDIO_ID, AudioFilter.SAMPLE_RATE);
    command.add("-audiodev");
    command.add(backend);
    final String card = isQ35(machineType(this.configuration)) ? ICH9_SOUND_CARD : SOUND_CARD;
    final String soundCard = "%s,id=%s".formatted(card, SOUND_CARD_ID);
    command.add(DEVICE_OPTION);
    command.add(soundCard);
    final String output = "hda-output,bus=%s.0,audiodev=%s".formatted(SOUND_CARD_ID, AUDIO_ID);
    command.add(DEVICE_OPTION);
    command.add(output);
  }

  /**
   * Adds the USB controller and the tablet. The older {@code -usbdevice} option brings both; {@code usb=} in the
   * machine type or the {@code usb} flag chooses the controller; {@code usb=off} means no USB, so no tablet either;
   * and a tablet the configuration adds itself replaces the default one.
   */
  private void addUsbTablet(final List<String> command) {
    final boolean legacyTablet = this.configuration.has("usbdevice");
    boolean controllerChosen = this.configuration.has("usb");
    boolean usbOff = false;
    for (final String machine : machineValues(this.configuration)) {
      for (final String part : PROPERTY_SPLITTER.split(machine)) {
        if (part.startsWith("usb=")) {
          controllerChosen = true;
          usbOff = part.equals("usb=off");
        }
      }
    }
    final List<String> devices = this.configuration.getAll("device");
    final boolean tabletAdded = hasTablet(devices);

    if (!legacyTablet && !controllerChosen) {
      command.add("-usb");
    }
    if (!legacyTablet && !usbOff && !tabletAdded) {
      command.add(DEVICE_OPTION);
      command.add(TABLET_DEVICE);
    }
  }

  private static boolean hasTablet(final List<String> devices) {
    for (final String device : devices) {
      final boolean tablet = device.equals(TABLET_DEVICE) || device.startsWith(TABLET_DEVICE + PROPERTY_SEPARATOR);
      if (tablet) {
        return true;
      }
    }
    return false;
  }

  private void addUnlessConfigured(final List<String> command, final String option, final String... defaults) {
    final boolean configured = this.configuration.has(option);
    if (!configured) {
      final List<String> defaultArguments = List.of(defaults);
      command.addAll(defaultArguments);
    }
  }

  /**
   * Picks the fastest accelerator of an operating system.
   *
   * @param os        the operating system
   * @param kvmDevice the KVM device, which must be writable to use KVM on Linux
   * @return the accelerator name
   */
  @VisibleForTesting
  static String detectAccelerator(final Os os, final Path kvmDevice) {
    return switch (os) {
      case LINUX -> Files.isWritable(kvmDevice) ? KVM_ACCELERATOR : SOFTWARE_ACCELERATOR;
      case WINDOWS -> WHPX_ACCELERATOR;
      case MAC -> HVF_ACCELERATOR;
      case FREEBSD, OTHER -> SOFTWARE_ACCELERATOR;
    };
  }

  private void drain(final InputStream stream) {
    final StringBuilder line = new StringBuilder();
    try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      final char[] characters = new char[READ_BUFFER_CHARACTERS];
      int count;
      while ((count = reader.read(characters)) >= 0) {
        this.collectLines(characters, count, line);
      }
    } catch (final IOException exception) {
      // the process is gone, nothing more to read
    }

    final String last = line.toString();
    this.remember(last);
  }

  /**
   * Adds characters to the line being read, remembering every line a line break completes.
   *
   * @param characters the characters that were read
   * @param count      how many of the characters are valid
   * @param line       the incomplete line, which keeps what follows the last line break
   */
  private void collectLines(final char[] characters, final int count, final StringBuilder line) {
    for (int index = 0; index < count; index++) {
      final char character = characters[index];
      if (character == LINE_FEED) {
        trimLine(line);
        final String complete = line.toString();
        this.remember(complete);
        line.setLength(0);
      } else if (character != CARRIAGE_RETURN) {
        line.append(character);
      }
    }
    trimLine(line);
  }

  /**
   * Retains only the end of a long diagnostic line, including when QEMU never prints a line break.
   * The reader supplies at most 4096 characters at once, so incomplete lines also have bounded storage.
   *
   * @param line the line being collected
   */
  private static void trimLine(final StringBuilder line) {
    final int excess = line.length() - OUTPUT_LINE_CHARACTERS;
    if (excess > 0) {
      line.delete(0, excess);
    }
  }

  private void remember(final String line) {
    if (line.isBlank()) {
      return;
    }
    LOGGER.debug("QEMU printed \"{}\".", line);
    synchronized (this.outputTail) {
      this.outputTail.addLast(line);
      while (this.outputTail.size() > OUTPUT_TAIL_LINES) {
        this.outputTail.removeFirst();
      }
    }
  }

  private String getOutputTail() {
    final String separator = System.lineSeparator();
    synchronized (this.outputTail) {
      return String.join(separator, this.outputTail);
    }
  }

  private void waitForDisplay(final Process started) {
    final int port = this.settings.getPort();
    final InetSocketAddress address = new InetSocketAddress(LOOPBACK, port);
    final long start = this.nanosecondClock.getAsLong();

    while (this.nanosecondClock.getAsLong() - start < this.startTimeoutNanoseconds) {
      this.checkAlive(started);
      final boolean reachable = isReachable(address);
      if (reachable) {
        // QEMU exits at once when it cannot open its display, so a moment later it must still run, or the
        // listener belongs to another program
        this.waitBeforeNextAttempt(started);
        this.checkAlive(started);
        return;
      }
      this.waitBeforeNextAttempt(started);
    }

    final String output = this.getOutputTail();
    this.shutdown();
    final String message = "QEMU display on port %s did not become reachable, and QEMU printed \"%s\"!".formatted(port, output);
    throw new PlayerException(message);
  }

  private void checkAlive(final Process started) {
    final boolean alive = started.isAlive();
    if (alive) {
      return;
    }

    final int exitCode = started.exitValue();
    this.awaitOutput();
    final String output = this.getOutputTail();
    this.process = null;
    final String message = "QEMU exited with code %s, printing \"%s\"!".formatted(exitCode, output);
    throw new PlayerException(message);
  }

  private void awaitOutput() {
    final Thread drain = Objects.requireNonNull(this.drainThread, "Output of QEMU must be drained while QEMU runs!");
    final long timeoutNanoseconds = TimeUnit.MILLISECONDS.toNanos(OUTPUT_TIMEOUT_MILLISECONDS);
    final long deadline = System.nanoTime() + timeoutNanoseconds;
    boolean interrupted = Thread.interrupted();
    try {
      while (drain.isAlive()) {
        final long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          drain.interrupt();
          LOGGER.warn("QEMU output reader did not stop within {} ms.", OUTPUT_TIMEOUT_MILLISECONDS);
          return;
        }
        try {
          TimeUnit.NANOSECONDS.timedJoin(drain, remaining);
        } catch (final InterruptedException exception) {
          interrupted = true;
        }
      }
    } finally {
      if (interrupted) {
        final Thread current = Thread.currentThread();
        current.interrupt();
      }
    }
  }

  /**
   * Waits a moment before the display is tried again, returning early if QEMU exits in the meantime.
   */
  private void waitBeforeNextAttempt(final Process started) {
    try {
      started.waitFor(POLL_MILLISECONDS, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException exception) {
      final Thread current = Thread.currentThread();
      current.interrupt();
      this.shutdown();
      final String message = "Waiting for QEMU was interrupted!";
      throw new PlayerException(message, exception);
    }
  }

  private static boolean isReachable(final InetSocketAddress address) {
    try (final Socket socket = new Socket()) {
      socket.connect(address, CONNECT_TIMEOUT_MILLISECONDS);
      return true;
    } catch (final IOException exception) {
      return false;
    }
  }

  /**
   * Checks whether QEMU is still running.
   *
   * @return true if the process is alive
   */
  boolean isAlive() {
    final Process current = this.process;
    return current != null && current.isAlive();
  }

  /**
   * Stops QEMU, forcibly if it does not exit in time, and waits for its final output.
   * Graceful and forced termination each have a bounded wait. Interrupts request forced termination immediately
   * and are restored after cleanup. A process that survives the forced wait remains visible to liveness checks.
   */
  void shutdown() {
    final Process current = this.process;
    if (current == null) {
      return;
    }

    boolean interrupted = Thread.interrupted();
    try {
      current.destroy();
      boolean exited = false;
      if (!interrupted) {
        try {
          exited = current.waitFor(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException exception) {
          interrupted = true;
        }
      }
      if (!exited) {
        current.destroyForcibly();
        awaitForcedExit(current);
      }
      this.awaitOutput();
      final boolean alive = current.isAlive();
      if (!alive) {
        this.process = null;
        this.launcher.ended(current);
      } else {
        LOGGER.warn("QEMU is still alive after its forced termination.");
      }
    } finally {
      if (interrupted) {
        final Thread thread = Thread.currentThread();
        thread.interrupt();
      }
    }
  }

  /**
   * Waits for asynchronous forced termination without letting interrupts abandon the child process.
   *
   * @param current the process whose termination was requested
   */
  private static void awaitForcedExit(final Process current) {
    final long timeoutNanoseconds = TimeUnit.SECONDS.toNanos(STOP_TIMEOUT_SECONDS);
    final long deadline = System.nanoTime() + timeoutNanoseconds;
    boolean interrupted = Thread.interrupted();
    try {
      while (current.isAlive()) {
        final long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          return;
        }
        try {
          current.waitFor(remaining, TimeUnit.NANOSECONDS);
        } catch (final InterruptedException exception) {
          interrupted = true;
        }
      }
    } finally {
      if (interrupted) {
        final Thread thread = Thread.currentThread();
        thread.interrupt();
      }
    }
  }

  /**
   * Creates the folder of the password file of one start.
   */
  @FunctionalInterface
  interface SecretFolder {
    /**
     * Creates an empty folder that only the server's user can open.
     *
     * @return the folder
     * @throws IOException if it cannot be created
     */
    Path create() throws IOException;
  }

  /**
   * Starts QEMU and records it, so that a later start of the module stops it if this JVM is killed before it could.
   */
  @VisibleForTesting
  static final class RecordingLauncher implements Launcher {

    private final QemuProcessRecords records;

    RecordingLauncher(final QemuProcessRecords records) {
      this.records = records;
    }

    @Override
    public Process launch(final List<String> command) throws IOException {
      final Process started = startProcess(command);
      final ProcessHandle handle = started.toHandle();
      this.records.add(handle);
      return started;
    }

    @Override
    public void ended(final Process process) {
      final long processId = process.pid();
      this.records.remove(processId);
    }
  }

  /**
   * Starts a process from its command line.
   */
  @FunctionalInterface
  interface Launcher {
    /**
     * Starts a process with its error output merged into its standard output.
     *
     * @param command the program followed by its arguments
     * @return the started process
     * @throws IOException if the process cannot be started
     */
    Process launch(List<String> command) throws IOException;

    /**
     * Learns that a process this launcher started has ended.
     *
     * @param process the process
     */
    default void ended(final Process process) {}
  }
}
