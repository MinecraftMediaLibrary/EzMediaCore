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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link VmConfiguration}.
 */
final class VmConfigurationTest {

  private static final String MEMORY_OPTION = "m";
  private static final String CDROM_OPTION = "cdrom";
  private static final String DEVICE_OPTION = "device";
  private static final String DRIVE_OPTION = "drive";
  private static final String SNAPSHOT_FLAG = "snapshot";

  private static final String MEMORY_ARGUMENT = "-m";
  private static final String CORES_ARGUMENT = "-smp";
  private static final String DRIVE_ARGUMENT = "-drive";

  private static final String ARGUMENT_SEPARATOR = " ";

  private static final String GIGABYTES = "G";

  private static final String CDROM_IMAGE = "alpine.iso";
  private static final String FIRST_DISK = "disk.qcow2";
  private static final String SECOND_DISK = "data.qcow2";

  private static final String TABLET_DEVICE = "usb-tablet";
  private static final String NETWORK_DEVICE = "virtio-net-pci";
  private static final String DISK_DRIVE = "file=disk.qcow2";

  @Test
  void shouldStartEmpty() {
    final VmConfiguration configuration = VmConfiguration.builder();
    final List<String> arguments = configuration.getArguments();
    final String[] array = configuration.buildArguments();
    final String text = configuration.toString();
    final boolean argumentsIsEmpty = arguments.isEmpty();
    assertTrue(argumentsIsEmpty);
    assertEquals(0, array.length);
    assertEquals("", text);
  }

  @Test
  void shouldCreateANewConfigurationEveryTime() {
    final VmConfiguration first = VmConfiguration.builder();
    final VmConfiguration second = VmConfiguration.builder();
    first.memory(256);
    final boolean secondHasMemory = second.has(MEMORY_OPTION);
    assertNotSame(first, second);
    assertFalse(secondHasMemory);
  }

  @Test
  void shouldRenderEveryTypedOptionAsAQemuArgument() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.memory(2048);
    configuration.cores(4);
    configuration.cdrom(CDROM_IMAGE);
    configuration.hda(FIRST_DISK);
    configuration.hdb(SECOND_DISK);
    configuration.boot("d");
    configuration.network("user,model=virtio-net-pci");
    configuration.cpu("max");
    configuration.machine("q35");
    configuration.accelerator("tcg");
    configuration.vga("virtio");

    final List<String> arguments = configuration.getArguments();

    final List<String> expected = this.typedArguments();
    assertEquals(expected, arguments);
  }

  private List<String> typedArguments() {
    return List.of(
      MEMORY_ARGUMENT,
      "2048M",
      CORES_ARGUMENT,
      "4",
      "-cdrom",
      CDROM_IMAGE,
      "-hda",
      FIRST_DISK,
      "-hdb",
      SECOND_DISK,
      "-boot",
      "d",
      "-nic",
      "user,model=virtio-net-pci",
      "-cpu",
      "max",
      "-machine",
      "q35",
      "-accel",
      "tcg",
      "-vga",
      "virtio"
    );
  }

  @Test
  void shouldSetMemoryWithAUnit() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.memory(2, GIGABYTES);
    final Optional<String> memory = configuration.find(MEMORY_OPTION);
    final Optional<String> expected = Optional.of("2G");
    assertEquals(expected, memory);
  }

  @Test
  void shouldReplaceOptionsButKeepTheirFirstPosition() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.memory(512);
    configuration.cores(1);
    configuration.memory(1024);
    final List<String> arguments = configuration.getArguments();
    final List<String> expectedArguments = List.of(MEMORY_ARGUMENT, "1024M", CORES_ARGUMENT, "1");
    assertEquals(expectedArguments, arguments);
  }

  @Test
  void shouldOrderOptionsBeforeRepeatableOptionsBeforeFlags() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.flag("enable-kvm");
    configuration.drive("file=a.qcow2,format=qcow2");
    configuration.device("virtio-rng-pci");
    configuration.drive("file=b.qcow2,format=qcow2");
    configuration.repeatable("object", "rng-random,id=rng0");
    configuration.option("name", "test");
    configuration.flag(SNAPSHOT_FLAG);
    configuration.flag("enable-kvm");

    final List<String> arguments = configuration.getArguments();
    final List<String> expected = this.orderedArguments();
    assertEquals(expected, arguments);

    final String[] array = configuration.buildArguments();
    final String[] expectedArray = expected.toArray(new String[0]);
    assertArrayEquals(expectedArray, array);

    final String text = configuration.toString();
    final String expectedText = String.join(ARGUMENT_SEPARATOR, expected);
    assertEquals(expectedText, text);
  }

  private List<String> orderedArguments() {
    return List.of(
      "-name",
      "test",
      DRIVE_ARGUMENT,
      "file=a.qcow2,format=qcow2",
      "-device",
      "virtio-rng-pci",
      DRIVE_ARGUMENT,
      "file=b.qcow2,format=qcow2",
      "-object",
      "rng-random,id=rng0",
      "-enable-kvm",
      "-snapshot"
    );
  }

  @Test
  void shouldAnswerWhichOptionsAndFlagsAreSet() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.cdrom(CDROM_IMAGE);
    configuration.flag(SNAPSHOT_FLAG);
    configuration.device(TABLET_DEVICE);
    final boolean hasCdrom = configuration.has(CDROM_OPTION);
    final boolean hasSnapshot = configuration.has(SNAPSHOT_FLAG);
    final boolean hasDevice = configuration.has(DEVICE_OPTION);
    final boolean hasMemory = configuration.has(MEMORY_OPTION);
    final Optional<String> cdrom = configuration.find(CDROM_OPTION);
    final Optional<String> snapshot = configuration.find(SNAPSHOT_FLAG);
    final Optional<String> memory = configuration.find(MEMORY_OPTION);
    final Optional<String> expectedCdrom = Optional.of(CDROM_IMAGE);
    final Optional<String> nothing = Optional.empty();
    assertTrue(hasCdrom);
    assertTrue(hasSnapshot);
    assertTrue(hasDevice);
    assertFalse(hasMemory);
    assertEquals(expectedCdrom, cdrom);
    assertEquals(nothing, snapshot);
    assertEquals(nothing, memory);
  }

  @Test
  void shouldListTheValuesOfRepeatableOptions() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.device(TABLET_DEVICE);
    configuration.drive(DISK_DRIVE);
    configuration.device(NETWORK_DEVICE);
    configuration.option(DEVICE_OPTION, "not-repeatable");
    final List<String> devices = configuration.getAll(DEVICE_OPTION);
    final List<String> drives = configuration.getAll(DRIVE_OPTION);
    final List<String> none = configuration.getAll("chardev");
    final Optional<String> single = configuration.find(DEVICE_OPTION);
    final Optional<String> expectedSingle = Optional.of("not-repeatable");
    final List<String> expectedDevices = List.of(TABLET_DEVICE, NETWORK_DEVICE);
    final List<String> expectedDrives = List.of(DISK_DRIVE);
    final boolean noneIsEmpty = none.isEmpty();
    assertEquals(expectedDevices, devices);
    assertEquals(expectedDrives, drives);
    assertTrue(noneIsEmpty);
    assertEquals(expectedSingle, single);
    assertThrows(UnsupportedOperationException.class, devices::clear);
    assertThrows(NullPointerException.class, () -> configuration.getAll(null));
  }

  @Test
  void shouldRemoveEveryValueOfARepeatableOption() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.device(TABLET_DEVICE);
    configuration.drive(DISK_DRIVE);
    configuration.device(NETWORK_DEVICE);
    configuration.remove(DEVICE_OPTION);
    final boolean hasDevice = configuration.has(DEVICE_OPTION);
    final boolean hasDrive = configuration.has(DRIVE_OPTION);
    final List<String> arguments = configuration.getArguments();
    final List<String> expectedArguments = List.of(DRIVE_ARGUMENT, DISK_DRIVE);
    assertFalse(hasDevice);
    assertTrue(hasDrive);
    assertEquals(expectedArguments, arguments);
  }

  @Test
  void shouldRemoveOptionsAndFlags() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.cdrom(CDROM_IMAGE);
    configuration.flag(SNAPSHOT_FLAG);
    configuration.cores(2);
    final VmConfiguration result = configuration.remove(CDROM_OPTION);
    configuration.remove(SNAPSHOT_FLAG);
    configuration.remove("not-set");
    final boolean hasCdrom = configuration.has(CDROM_OPTION);
    final boolean hasSnapshot = configuration.has(SNAPSHOT_FLAG);
    final List<String> arguments = configuration.getArguments();
    assertSame(configuration, result);
    assertFalse(hasCdrom);
    assertFalse(hasSnapshot);

    final List<String> expectedArguments = List.of(CORES_ARGUMENT, "2");
    assertEquals(expectedArguments, arguments);
  }

  @Test
  void shouldReturnItselfFromEverySetter() {
    final VmConfiguration configuration = VmConfiguration.builder();
    final List<UnaryOperator<VmConfiguration>> setters = List.of(
      candidate -> candidate.memory(1),
      candidate -> candidate.memory(1, GIGABYTES),
      candidate -> candidate.cores(1),
      candidate -> candidate.cdrom("a"),
      candidate -> candidate.hda("a"),
      candidate -> candidate.hdb("a"),
      candidate -> candidate.boot("a"),
      candidate -> candidate.network("a"),
      candidate -> candidate.cpu("a"),
      candidate -> candidate.machine("a"),
      candidate -> candidate.accelerator("a"),
      candidate -> candidate.vga("a"),
      candidate -> candidate.drive("a"),
      candidate -> candidate.device("a"),
      candidate -> candidate.option("a", "b"),
      candidate -> candidate.repeatable("a", "b"),
      candidate -> candidate.flag("a")
    );
    for (final UnaryOperator<VmConfiguration> setter : setters) {
      final VmConfiguration result = setter.apply(configuration);
      assertSame(configuration, result);
    }
  }

  @Test
  void shouldReturnArgumentsThatCannotBeModified() {
    final VmConfiguration configuration = VmConfiguration.builder();
    configuration.memory(64);
    final List<String> arguments = configuration.getArguments();
    assertThrows(UnsupportedOperationException.class, arguments::clear);

    final String[] array = configuration.buildArguments();
    array[0] = "-changed";
    final List<String> again = configuration.getArguments();
    final List<String> expectedAgain = List.of(MEMORY_ARGUMENT, "64M");
    assertEquals(expectedAgain, again);
  }

  @Test
  void shouldRejectInvalidTypedOptions() {
    final VmConfiguration configuration = VmConfiguration.builder();
    assertThrows(IllegalArgumentException.class, () -> configuration.memory(0));
    assertThrows(IllegalArgumentException.class, () -> configuration.memory(0, GIGABYTES));
    assertThrows(NullPointerException.class, () -> configuration.memory(1, null));
    assertThrows(IllegalArgumentException.class, () -> configuration.cores(0));
    assertThrows(NullPointerException.class, () -> configuration.cdrom(null));
    assertThrows(NullPointerException.class, () -> configuration.hda(null));
    assertThrows(NullPointerException.class, () -> configuration.hdb(null));
    assertThrows(NullPointerException.class, () -> configuration.boot(null));
    assertThrows(NullPointerException.class, () -> configuration.network(null));
    assertThrows(NullPointerException.class, () -> configuration.cpu(null));
    assertThrows(NullPointerException.class, () -> configuration.machine(null));
    assertThrows(NullPointerException.class, () -> configuration.accelerator(null));
    assertThrows(NullPointerException.class, () -> configuration.vga(null));
    assertThrows(NullPointerException.class, () -> configuration.drive(null));
    assertThrows(NullPointerException.class, () -> configuration.device(null));

    final List<String> arguments = configuration.getArguments();
    final boolean argumentsIsEmpty = arguments.isEmpty();
    assertTrue(argumentsIsEmpty);
  }

  @Test
  void shouldRejectInvalidGenericOptionsAndKeys() {
    final VmConfiguration configuration = VmConfiguration.builder();
    assertThrows(NullPointerException.class, () -> configuration.option(null, "value"));
    assertThrows(NullPointerException.class, () -> configuration.option("key", null));
    assertThrows(IllegalArgumentException.class, () -> configuration.option(" ", "value"));
    assertThrows(NullPointerException.class, () -> configuration.repeatable(null, "value"));
    assertThrows(NullPointerException.class, () -> configuration.repeatable("key", null));
    assertThrows(IllegalArgumentException.class, () -> configuration.repeatable("", "value"));
    assertThrows(NullPointerException.class, () -> configuration.flag(null));
    assertThrows(IllegalArgumentException.class, () -> configuration.flag(" "));
    assertThrows(NullPointerException.class, () -> configuration.remove(null));
    assertThrows(NullPointerException.class, () -> configuration.has(null));
    assertThrows(NullPointerException.class, () -> configuration.find(null));

    final List<String> arguments = configuration.getArguments();
    final boolean argumentsIsEmpty = arguments.isEmpty();
    assertTrue(argumentsIsEmpty);
  }
}
