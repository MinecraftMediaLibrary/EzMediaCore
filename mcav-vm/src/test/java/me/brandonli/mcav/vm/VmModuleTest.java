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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import me.brandonli.mcav.utils.os.Os;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link VmModule}.
 */
final class VmModuleTest {

  @TempDir
  private Path directory;

  private QemuProcessRecords records() {
    final Path folder = this.directory.resolve("records");
    return new QemuProcessRecords(folder, ProcessHandle.current(), ProcessHandle::of, Duration.ofSeconds(10));
  }

  private ExecutableFinder finderWithPath(final Path folder) {
    final String path = folder.toString();
    final Map<String, String> environment = Map.of("PATH", path);
    final Path macPaths = this.directory.resolve("no-paths-file");
    return new ExecutableFinder(Os.LINUX, environment, macPaths);
  }

  @Test
  void shouldFindQemuWhenStarted() throws IOException {
    final Path folder = this.directory.resolve("bin");
    Files.createDirectories(folder);
    final Path qemu = folder.resolve("qemu-system-x86_64");
    Files.writeString(qemu, "qemu");
    final File file = qemu.toFile();
    final boolean madeExecutable = file.setExecutable(true);
    assertTrue(madeExecutable);

    final ExecutableFinder finder = this.finderWithPath(folder);
    final VmModule module = new VmModule(finder, this.records());
    final boolean beforeStart = module.isQemuInstalled();
    module.start();
    final boolean qemuFoundAfterStart = module.isQemuInstalled();
    assertFalse(beforeStart);
    assertTrue(qemuFoundAfterStart);
  }

  @Test
  void shouldStopTheQemuOfAKilledServerWhenStarted() throws IOException, InterruptedException {
    final String java = ProcessHandle.current().info().command().orElseThrow();
    final Path sleeper = this.directory.resolve("Sleep.java");
    Files.writeString(sleeper, "class Sleep { public static void main(String[] a) throws Exception { Thread.sleep(300_000L); } }");
    final Process qemu = new ProcessBuilder(java, sleeper.toString()).start();
    try {
      final Instant started = qemu.toHandle().info().startInstant().orElseThrow();
      final Path record = this.directory.resolve("records").resolve(qemu.pid() + ".qemu");
      Files.createDirectories(record.getParent());
      Files.writeString(record, started + "\n" + Long.MAX_VALUE + "\n" + started + "\n");
      final Path empty = this.directory.resolve("empty");
      Files.createDirectories(empty);
      final VmModule module = new VmModule(this.finderWithPath(empty), this.records());

      module.start();

      final boolean stopped = qemu.waitFor(10, TimeUnit.SECONDS);
      final boolean recordExists = Files.exists(record);
      assertTrue(stopped, "the QEMU of the killed server is stopped");
      assertFalse(recordExists);
    } finally {
      qemu.destroyForcibly();
    }
  }

  @Test
  void shouldReportAMissingQemu() throws IOException {
    final Path empty = this.directory.resolve("empty");
    Files.createDirectories(empty);
    final ExecutableFinder finder = this.finderWithPath(empty);
    final VmModule module = new VmModule(finder, this.records());
    module.start();
    final boolean installed = module.isQemuInstalled();
    assertFalse(installed);
  }

  @Test
  void shouldLookForQemuOnThisMachineByDefault() {
    final VmModule module = new VmModule();
    module.start();
    final boolean installed = module.isQemuInstalled();
    final ExecutableFinder finder = new ExecutableFinder();
    final String command = VmPlayer.Architecture.X86_64.getCommand();
    final Optional<Path> qemu = finder.find(command);
    final boolean present = qemu.isPresent();
    assertEquals(present, installed);
    module.stop();
  }

  @Test
  void shouldBeNamedVm() {
    final VmModule module = new VmModule();
    final String name = module.getModuleName();
    assertEquals("vm", name);
  }

  @Test
  void shouldBeCreatableByTheModuleLoader() throws ReflectiveOperationException {
    final Constructor<VmModule> constructor = VmModule.class.getConstructor();
    final VmModule module = constructor.newInstance();
    final boolean installed = module.isQemuInstalled();
    assertFalse(installed, "nothing is looked up before the module starts");
  }
}
