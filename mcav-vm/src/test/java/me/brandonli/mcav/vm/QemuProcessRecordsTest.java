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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link QemuProcessRecords} with child JVMs in place of QEMU: a process a killed server left running is stopped
 * when the module starts again, and nothing else is.
 */
final class QemuProcessRecordsTest {

  private static final long GONE = Long.MAX_VALUE;

  @TempDir
  private Path directory;

  private final List<Process> children = new ArrayList<>();

  @AfterEach
  void stopChildren() throws InterruptedException {
    for (final Process child : this.children) {
      child.destroyForcibly();
      child.waitFor(10, TimeUnit.SECONDS);
    }
  }

  /**
   * Starts a JVM that sleeps for five minutes, standing in for QEMU.
   *
   * @param ignoresStop whether it ignores a request to end, so only a kill ends it
   * @return the process
   */
  private Process child(final boolean ignoresStop) throws IOException {
    final List<String> command = this.sleeper(ignoresStop);
    // JVM startup diagnostics are separate from the fixture's readiness protocol.
    final Process process = new ProcessBuilder(command).redirectError(Redirect.INHERIT).start();
    this.children.add(process);
    final byte[] ready = process.getInputStream().readNBytes(5);
    final String announcement = new String(ready, StandardCharsets.US_ASCII);
    assertEquals("ready", announcement);
    return process;
  }

  /**
   * Writes the program of a JVM that sleeps for five minutes, standing in for QEMU.
   *
   * @param ignoresStop whether it ignores a request to end, so only a kill ends it
   * @return the command that starts it
   */
  private List<String> sleeper(final boolean ignoresStop) throws IOException {
    final String hook = ignoresStop ? "Runtime.getRuntime().addShutdownHook(new Thread(Sleep::sleep));" : "";
    final String source =
      "class Sleep { static void sleep() { try { Thread.sleep(300_000L); } catch (InterruptedException exception) { } } " +
      "public static void main(String[] arguments) { " +
      hook +
      " System.out.println(\"ready\"); System.out.flush(); sleep(); } }";
    final Path file = this.directory.resolve("Sleep" + this.children.size() + ".java");
    Files.writeString(file, source);
    final String java = ProcessHandle.current().info().command().orElseThrow();
    return List.of(java, file.toString());
  }

  private QemuProcessRecords records(final ProcessHandle owner) {
    final Path folder = this.directory.resolve("records");
    return new QemuProcessRecords(folder, owner, ProcessHandle::of, Duration.ofMillis(500));
  }

  private static ProcessHandle goneServer() {
    final ProcessHandle owner = mock(ProcessHandle.class);
    final ProcessHandle.Info information = mock(ProcessHandle.Info.class);
    when(owner.pid()).thenReturn(GONE);
    when(owner.info()).thenReturn(information);
    when(information.startInstant()).thenReturn(Optional.of(Instant.parse("2026-10-05T00:00:00Z")));
    return owner;
  }

  private Path recordOf(final long processId) {
    return this.directory.resolve("records").resolve(processId + ".qemu");
  }

  @Test
  void shouldRecordAProcessTheLauncherStartsUntilItEnds() throws IOException, InterruptedException {
    final VmProcess.RecordingLauncher launcher = new VmProcess.RecordingLauncher(this.records(ProcessHandle.current()));
    final Process qemu = launcher.launch(this.sleeper(false));
    this.children.add(qemu);
    final boolean recorded = Files.isRegularFile(this.recordOf(qemu.pid()));
    qemu.destroyForcibly();
    qemu.waitFor(10, TimeUnit.SECONDS);
    launcher.ended(qemu);

    final boolean recordLeft = Files.exists(this.recordOf(qemu.pid()));
    assertTrue(recorded, "a started QEMU is recorded at once, before anything can kill this JVM");
    assertFalse(recordLeft, "an ended QEMU is forgotten");
  }

  @Test
  void shouldStopTheQemuOfAServerThatIsGone() throws IOException {
    final Process qemu = this.child(false);
    this.records(goneServer()).add(qemu.toHandle());
    final boolean recorded = Files.exists(this.recordOf(qemu.pid()));
    assertTrue(recorded);

    final int stopped = this.records(ProcessHandle.current()).reap();

    final boolean ended = qemu.onExit().completeOnTimeout(null, 10, TimeUnit.SECONDS).join() != null;
    final boolean recordLeft = Files.exists(this.recordOf(qemu.pid()));
    assertEquals(1, stopped);
    assertTrue(ended, "the process ended");
    assertFalse(recordLeft, "its record is gone");
  }

  @Test
  void shouldKillAQemuThatIgnoresTheRequestToEnd() throws IOException {
    final Process qemu = this.child(true);
    this.records(goneServer()).add(qemu.toHandle());

    final int stopped = this.records(ProcessHandle.current()).reap();

    final boolean killed = qemu.onExit().completeOnTimeout(null, 10, TimeUnit.SECONDS).join() != null;
    assertEquals(1, stopped);
    assertTrue(killed, "the process was killed");
  }

  @Test
  void shouldLeaveTheQemuOfAServerThatStillRuns() throws IOException {
    final Process qemu = this.child(false);
    final QemuProcessRecords records = this.records(ProcessHandle.current());
    records.add(qemu.toHandle());

    final int stopped = records.reap();

    final boolean alive = qemu.isAlive();
    final boolean recorded = Files.exists(this.recordOf(qemu.pid()));
    assertEquals(0, stopped);
    assertTrue(alive, "this server releases it itself");
    assertTrue(recorded);
    records.remove(qemu.pid());
    final boolean recordLeft = Files.exists(this.recordOf(qemu.pid()));
    assertFalse(recordLeft, "a process that ended is forgotten");
  }

  @Test
  void shouldTreatAServerWhoseIdAnotherProcessReusedAsGone() throws IOException {
    final Process qemu = this.child(false);
    final Instant started = qemu.toHandle().info().startInstant().orElseThrow();
    final Path record = this.recordOf(qemu.pid());
    Files.createDirectories(record.getParent());
    final long reused = ProcessHandle.current().pid();
    Files.writeString(record, started + "\n" + reused + "\n2001-01-01T00:00:00Z\n");

    final int stopped = this.records(ProcessHandle.current()).reap();

    final boolean ended = qemu.onExit().completeOnTimeout(null, 10, TimeUnit.SECONDS).join() != null;
    assertEquals(1, stopped);
    assertTrue(ended, "the process ended");
  }

  @Test
  void shouldNeverTouchAProcessThatReusedTheId() throws IOException {
    final Process other = this.child(false);
    final Path record = this.recordOf(other.pid());
    Files.createDirectories(record.getParent());
    Files.writeString(record, "2001-01-01T00:00:00Z\n" + GONE + "\n2001-01-01T00:00:00Z\n");

    final int stopped = this.records(ProcessHandle.current()).reap();

    final boolean alive = other.isAlive();
    final boolean recordLeft = Files.exists(record);
    assertEquals(0, stopped);
    assertTrue(alive);
    assertFalse(recordLeft, "the record of the process that ended is deleted");
  }

  @Test
  void shouldDeleteRecordsItCannotReadAndKeepReapingTheOthers() throws IOException {
    final Path folder = this.directory.resolve("records");
    Files.createDirectories(folder);
    final Path garbage = folder.resolve("12.qemu");
    Files.writeString(garbage, "not a record");
    final Path unnamed = folder.resolve("abc.qemu");
    Files.writeString(unnamed, "2001-01-01T00:00:00Z\n1\n2001-01-01T00:00:00Z\n");
    final Path shortRecord = folder.resolve("13.qemu");
    Files.writeString(shortRecord, "2001-01-01T00:00:00Z\n");
    final Path unreadable = folder.resolve("14.qemu");
    Files.createDirectories(unreadable.resolve("inside"));
    final Process qemu = this.child(false);
    this.records(goneServer()).add(qemu.toHandle());
    final Path unrelated = folder.resolve("notes.txt");
    Files.writeString(unrelated, "kept");

    final int stopped = this.records(ProcessHandle.current()).reap();

    final boolean garbageLeft = Files.exists(garbage);
    final boolean unnamedLeft = Files.exists(unnamed);
    final boolean shortRecordLeft = Files.exists(shortRecord);
    final boolean unreadableKept = Files.isDirectory(unreadable);
    final boolean unrelatedKept = Files.exists(unrelated);
    assertEquals(1, stopped);
    assertFalse(garbageLeft);
    assertFalse(unnamedLeft);
    assertFalse(shortRecordLeft);
    assertTrue(unreadableKept);
    assertTrue(unrelatedKept, "only records are reaped");
  }

  @Test
  void shouldHaveNothingToReapWithoutAFolder() throws IOException {
    final int stoppedWithoutAFolder = this.records(ProcessHandle.current()).reap();
    assertEquals(0, stoppedWithoutAFolder);
    Files.writeString(this.directory.resolve("records"), "a file, not a folder");
    final int stoppedWithAFile = this.records(ProcessHandle.current()).reap();
    assertEquals(0, stoppedWithAFile);
  }

  @Test
  void shouldOnlyLogAFolderItCannotList() throws IOException {
    final Path folder = mock(Path.class);
    final FileSystem fileSystem = mock(FileSystem.class);
    final FileSystemProvider provider = mock(FileSystemProvider.class);
    final BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    final IOException unlistable = new IOException("Permission denied!");
    when(folder.getFileSystem()).thenReturn(fileSystem);
    when(fileSystem.provider()).thenReturn(provider);
    when(provider.readAttributesIfExists(folder, BasicFileAttributes.class)).thenReturn(attributes);
    when(attributes.isDirectory()).thenReturn(true);
    when(provider.newDirectoryStream(eq(folder), any())).thenThrow(unlistable);
    final QemuProcessRecords records = new QemuProcessRecords(folder, ProcessHandle.current(), ProcessHandle::of, Duration.ofMillis(500));
    final int stopped = records.reap();
    assertEquals(0, stopped);
  }

  @Test
  void shouldNotRecordAProcessWithoutAKnownStart() throws IOException {
    final ProcessHandle unknown = mock(ProcessHandle.class);
    final ProcessHandle.Info information = mock(ProcessHandle.Info.class);
    when(unknown.pid()).thenReturn(42L);
    when(unknown.info()).thenReturn(information);
    when(information.startInstant()).thenReturn(Optional.empty());
    this.records(ProcessHandle.current()).add(unknown);
    final boolean unknownRecorded = Files.exists(this.recordOf(42L));
    assertFalse(unknownRecorded, "it could not be told from a later process with its id");
    final Process qemu = this.child(false);
    this.records(unknown).add(qemu.toHandle());
    final boolean recorded = Files.exists(this.recordOf(qemu.pid()));
    assertFalse(recorded);
  }

  @Test
  void shouldOnlyLogARecordThatCannotBeWrittenOrDeleted() throws IOException {
    Files.writeString(this.directory.resolve("records"), "a file where the folder belongs");
    final Process qemu = this.child(false);
    this.records(goneServer()).add(qemu.toHandle());
    Files.delete(this.directory.resolve("records"));
    final Path record = this.recordOf(7L);
    Files.createDirectories(record.resolve("inside"));
    this.records(ProcessHandle.current()).remove(7L);
    final boolean kept = Files.isDirectory(record);
    assertTrue(kept, "a folder named like a record is not deleted");
  }

  @Test
  void shouldKeepTheRecordsOfAUserInTheirHome() throws IOException {
    final String previous = System.getProperty("user.home");
    System.setProperty("user.home", this.directory.toString());
    try {
      final QemuProcessRecords records = QemuProcessRecords.ofUser();
      final Process qemu = this.child(false);
      records.add(qemu.toHandle());
      final Path record = this.directory
        .resolve(".mcav")
        .resolve("vm")
        .resolve(qemu.pid() + ".qemu");
      final boolean recorded = Files.exists(record);
      assertTrue(recorded);
      records.remove(qemu.pid());
      final boolean recordLeft = Files.exists(record);
      assertFalse(recordLeft);
    } finally {
      System.setProperty("user.home", previous);
    }
  }
}
