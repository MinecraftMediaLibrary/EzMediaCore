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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Mutable QEMU command-line settings for a {@link VmPlayer}.
 *
 * <p>This object is not thread-safe. Finish configuring it before starting the player, and do not modify it
 * during startup or while the player uses it. Values are passed as individual process arguments, without a
 * shell; paths need no shell quoting. Option values are otherwise left to QEMU to validate.
 *
 * <p>Options keep the order they were first set in, and setting an option again replaces its value. Options
 * QEMU accepts more than once, such as {@code -drive} or {@code -device}, are added with
 * {@link #repeatable(String, String)}.
 *
 * <p>The player supplies {@code -vnc} from {@link VmSettings}, reserves {@code -audio}, {@code -audiodev} and
 * {@code pcspk-audiodev} routing, and rejects configurations that set those options. It also supplies defaults
 * for {@code -vga std}, {@code -display none} and a USB tablet ({@code -usb -device usb-tablet}) unless overridden,
 * which reports absolute pointer positions so clicks land where they are sent. The tablet is left out when the
 * configuration adds its own with {@code device("usb-tablet")} or uses the older {@code -usbdevice} option. To choose
 * the USB controller yourself, set the {@code usb} flag or {@code usb=on} in the machine type, and to run without USB
 * at all, which also leaves out the tablet, set {@code usb=off}, as in {@code machine("q35,usb=off")}.
 *
 * <pre><code>
 *   final VmConfiguration configuration = VmConfiguration.builder();
 *   configuration.cdrom("alpine.iso");
 *   configuration.memory(2048);
 *   configuration.cores(2);
 * </code></pre>
 */
public final class VmConfiguration {

  private static final String MEMORY_NOT_POSITIVE_MESSAGE = "Memory %s must be positive!";

  private static final String DISK_PATH_NULL_MESSAGE = "Disk path must not be null!";

  private static final String KEY_NULL_MESSAGE = "Key must not be null!";
  private static final String KEY_BLANK_MESSAGE = "Key must not be blank!";
  private static final String VALUE_NULL_MESSAGE = "Value must not be null!";

  private final Map<String, String> options;
  private final List<String> repeatable;
  private final Set<String> flags;

  private VmConfiguration() {
    this.options = new LinkedHashMap<>();
    this.repeatable = new ArrayList<>();
    this.flags = new LinkedHashSet<>();
  }

  /**
   * Creates an empty configuration.
   *
   * @return the configuration
   */
  public static VmConfiguration builder() {
    return new VmConfiguration();
  }

  /**
   * Sets the memory of the machine ({@code -m}).
   *
   * @param memoryMegabytes the strictly positive memory amount, passed to QEMU with its {@code M} suffix
   * @return this configuration
   * @throws IllegalArgumentException if {@code memoryMegabytes} is zero or negative
   */
  public VmConfiguration memory(final int memoryMegabytes) {
    Preconditions.checkArgument(memoryMegabytes > 0, MEMORY_NOT_POSITIVE_MESSAGE, memoryMegabytes);
    return this.option("m", memoryMegabytes + "M");
  }

  /**
   * Sets the memory of the machine ({@code -m}) with a unit.
   *
   * @param amount the strictly positive amount
   * @param unit   the non-null QEMU unit suffix, such as {@code M} or {@code G}; it is appended verbatim
   *               and is not validated here
   * @return this configuration
   * @throws IllegalArgumentException if {@code amount} is zero or negative
   * @throws NullPointerException     if {@code unit} is null
   */
  public VmConfiguration memory(final int amount, final String unit) {
    Preconditions.checkArgument(amount > 0, MEMORY_NOT_POSITIVE_MESSAGE, amount);
    Preconditions.checkNotNull(unit, "Unit must not be null!");
    return this.option("m", amount + unit);
  }

  /**
   * Sets the number of virtual processors ({@code -smp}).
   *
   * @param cores the strictly positive number of virtual processors
   * @return this configuration
   * @throws IllegalArgumentException if {@code cores} is zero or negative
   */
  public VmConfiguration cores(final int cores) {
    Preconditions.checkArgument(cores > 0, "Cores %s must be positive!", cores);
    final String value = String.valueOf(cores);
    return this.option("smp", value);
  }

  /**
   * Attaches an ISO image as the CD-ROM drive ({@code -cdrom}).
   *
   * @param isoPath the path of the image
   * @return this configuration
   * @throws NullPointerException if {@code isoPath} is null
   */
  public VmConfiguration cdrom(final String isoPath) {
    Preconditions.checkNotNull(isoPath, "ISO path must not be null!");
    return this.option("cdrom", isoPath);
  }

  /**
   * Attaches a disk image as the first hard disk ({@code -hda}).
   *
   * @param hdaPath the path of the image
   * @return this configuration
   * @throws NullPointerException if {@code hdaPath} is null
   */
  public VmConfiguration hda(final String hdaPath) {
    Preconditions.checkNotNull(hdaPath, DISK_PATH_NULL_MESSAGE);
    return this.option("hda", hdaPath);
  }

  /**
   * Attaches a disk image as the second hard disk ({@code -hdb}).
   *
   * @param hdbPath the path of the image
   * @return this configuration
   * @throws NullPointerException if {@code hdbPath} is null
   */
  public VmConfiguration hdb(final String hdbPath) {
    Preconditions.checkNotNull(hdbPath, DISK_PATH_NULL_MESSAGE);
    return this.option("hdb", hdbPath);
  }

  /**
   * Sets the boot order ({@code -boot}).
   *
   * @param bootOrder the boot order, such as {@code d} for the CD-ROM first
   * @return this configuration
   * @throws NullPointerException if {@code bootOrder} is null
   */
  public VmConfiguration boot(final String bootOrder) {
    Preconditions.checkNotNull(bootOrder, "Boot order must not be null!");
    return this.option("boot", bootOrder);
  }

  /**
   * Sets the network configuration ({@code -nic}).
   *
   * @param networkConfiguration the configuration, such as {@code user,model=virtio-net-pci}
   * @return this configuration
   * @throws NullPointerException if {@code networkConfiguration} is null
   */
  public VmConfiguration network(final String networkConfiguration) {
    Preconditions.checkNotNull(networkConfiguration, "Network configuration must not be null!");
    return this.option("nic", networkConfiguration);
  }

  /**
   * Sets the processor model ({@code -cpu}).
   *
   * @param model the model, such as {@code host} or {@code max}
   * @return this configuration
   * @throws NullPointerException if {@code model} is null
   */
  public VmConfiguration cpu(final String model) {
    Preconditions.checkNotNull(model, "CPU model must not be null!");
    return this.option("cpu", model);
  }

  /**
   * Sets the machine type ({@code -machine}).
   *
   * @param machineType the type, such as {@code q35} or {@code virt}
   * @return this configuration
   * @throws NullPointerException if {@code machineType} is null
   */
  public VmConfiguration machine(final String machineType) {
    Preconditions.checkNotNull(machineType, "Machine type must not be null!");
    return this.option("machine", machineType);
  }

  /**
   * Sets the accelerator ({@code -accel}). Without it the player picks the fastest accelerator available on the
   * host platform and retries with software emulation when startup fails with an accelerator-related diagnostic.
   *
   * @param accelerator the accelerator, such as {@code kvm}, {@code whpx}, {@code hvf}, or {@code tcg}
   * @return this configuration
   * @throws NullPointerException if {@code accelerator} is null
   */
  public VmConfiguration accelerator(final String accelerator) {
    Preconditions.checkNotNull(accelerator, "Accelerator must not be null!");
    return this.option("accel", accelerator);
  }

  /**
   * Sets the graphics adapter ({@code -vga}).
   *
   * @param adapter the adapter, such as {@code std} or {@code virtio}
   * @return this configuration
   * @throws NullPointerException if {@code adapter} is null
   */
  public VmConfiguration vga(final String adapter) {
    Preconditions.checkNotNull(adapter, "Adapter must not be null!");
    return this.option("vga", adapter);
  }

  /**
   * Adds a drive ({@code -drive}). Drives can be added more than once.
   *
   * @param drive the drive specification, such as {@code file=disk.qcow2,format=qcow2}
   * @return this configuration
   * @throws NullPointerException if {@code drive} is null
   */
  public VmConfiguration drive(final String drive) {
    Preconditions.checkNotNull(drive, "Drive must not be null!");
    return this.repeatable("drive", drive);
  }

  /**
   * Adds a device ({@code -device}). Devices can be added more than once.
   *
   * @param device the device specification, such as {@code usb-tablet}
   * @return this configuration
   * @throws NullPointerException if {@code device} is null
   */
  public VmConfiguration device(final String device) {
    Preconditions.checkNotNull(device, "Device must not be null!");
    return this.repeatable("device", device);
  }

  /**
   * Sets an option that takes a value, replacing an earlier value of the same option.
   *
   * @param key   the non-null, nonblank option name without the leading dash
   * @param value the non-null value, passed verbatim as one process argument; empty values are allowed
   * @return this configuration
   * @throws IllegalArgumentException if {@code key} is blank
   * @throws NullPointerException     if {@code key} or {@code value} is null
   */
  public VmConfiguration option(final String key, final String value) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    Preconditions.checkNotNull(value, VALUE_NULL_MESSAGE);
    Preconditions.checkArgument(!key.isBlank(), KEY_BLANK_MESSAGE);
    this.options.put(key, value);
    return this;
  }

  /**
   * Adds an option that takes a value and may appear more than once.
   *
   * @param key   the non-null, nonblank option name without the leading dash
   * @param value the non-null value, passed verbatim as one process argument; empty values are allowed
   * @return this configuration
   * @throws IllegalArgumentException if {@code key} is blank
   * @throws NullPointerException     if {@code key} or {@code value} is null
   */
  public VmConfiguration repeatable(final String key, final String value) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    Preconditions.checkNotNull(value, VALUE_NULL_MESSAGE);
    Preconditions.checkArgument(!key.isBlank(), KEY_BLANK_MESSAGE);
    this.repeatable.add("-" + key);
    this.repeatable.add(value);
    return this;
  }

  /**
   * Adds an option without a value, such as {@code enable-kvm}.
   *
   * @param flag the non-null, nonblank option name without the leading dash; duplicates are ignored
   * @return this configuration
   * @throws IllegalArgumentException if {@code flag} is blank
   * @throws NullPointerException     if {@code flag} is null
   */
  public VmConfiguration flag(final String flag) {
    Preconditions.checkNotNull(flag, "Flag must not be null!");
    Preconditions.checkArgument(!flag.isBlank(), "Flag must not be blank!");
    this.flags.add(flag);
    return this;
  }

  /**
   * Removes an option, every value of a repeatable option, or a flag.
   *
   * @param key the option name without the leading dash
   * @return this configuration
   * @throws NullPointerException if {@code key} is null
   */
  public VmConfiguration remove(final String key) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    this.options.remove(key);
    this.flags.remove(key);

    final String argument = "-" + key;
    final List<String> kept = new ArrayList<>();
    for (int index = 0; index < this.repeatable.size(); index += 2) {
      final String name = this.repeatable.get(index);
      final String value = this.repeatable.get(index + 1);
      if (!name.equals(argument)) {
        kept.add(name);
        kept.add(value);
      }
    }

    this.repeatable.clear();
    this.repeatable.addAll(kept);
    return this;
  }

  /**
   * Returns whether an option, a repeatable option, or a flag is set.
   *
   * @param key the option name without the leading dash
   * @return true if set
   * @throws NullPointerException if {@code key} is null
   */
  public boolean has(final String key) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    final List<String> values = this.getAll(key);
    return this.options.containsKey(key) || this.flags.contains(key) || !values.isEmpty();
  }

  /**
   * Returns the values of a repeatable option, such as every {@code -device}.
   *
   * @param key the option name without the leading dash
   * @return an unmodifiable snapshot in insertion order, empty if no repeatable values exist; singleton
   *         options and flags are not included
   * @throws NullPointerException if {@code key} is null
   */
  public List<String> getAll(final String key) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    final String argument = "-" + key;
    final List<String> values = new ArrayList<>();
    for (int index = 0; index < this.repeatable.size(); index += 2) {
      final String name = this.repeatable.get(index);
      if (name.equals(argument)) {
        final String value = this.repeatable.get(index + 1);
        values.add(value);
      }
    }
    return List.copyOf(values);
  }

  /**
   * Returns the value of an option. Repeatable options are read with {@link #getAll(String)}.
   *
   * @param key the option name without the leading dash
   * @return the value, or null if the option is not set
   * @throws NullPointerException if {@code key} is null
   */
  public @Nullable String get(final String key) {
    Preconditions.checkNotNull(key, KEY_NULL_MESSAGE);
    return this.options.get(key);
  }

  /**
   * Gets the values of options in the order of {@link #getArguments()}, whether an option was set once or repeated.
   * Only option names are matched, so the value of another option that looks like one of them is left out.
   *
   * @param keys the option names without the leading dash
   * @return the values
   */
  List<String> valuesOf(final Set<String> keys) {
    final List<String> values = new ArrayList<>();
    for (final Map.Entry<String, String> entry : this.options.entrySet()) {
      if (keys.contains(entry.getKey())) {
        values.add(entry.getValue());
      }
    }
    for (int index = 0; index < this.repeatable.size(); index += 2) {
      final String name = this.repeatable.get(index);
      if (keys.contains(name.substring(1))) {
        values.add(this.repeatable.get(index + 1));
      }
    }
    return values;
  }

  /**
   * Returns the command-line arguments in order: options, repeatable options, then flags.
   *
   * @return an unmodifiable snapshot; later configuration changes do not affect the returned list
   */
  public List<String> getArguments() {
    final List<String> arguments = new ArrayList<>();
    for (final Map.Entry<String, String> entry : this.options.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      arguments.add("-" + key);
      arguments.add(value);
    }

    arguments.addAll(this.repeatable);

    for (final String flag : this.flags) {
      arguments.add("-" + flag);
    }
    return List.copyOf(arguments);
  }

  /**
   * Returns the command-line arguments as an array.
   *
   * @return a new array snapshot; changes to the array do not affect this configuration
   */
  public String[] buildArguments() {
    final List<String> arguments = this.getArguments();
    return arguments.toArray(new String[0]);
  }

  /**
   * Joins the current argument tokens with spaces for diagnostics.
   *
   * @return the argument list without the executable; tokens are not shell-quoted, so this is not a
   *         command intended for shell execution
   */
  @Override
  public String toString() {
    final List<String> arguments = this.getArguments();
    return String.join(" ", arguments);
  }
}
