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

/**
 * Collects the QEMU command-line settings of a {@link VmConfiguration}. Options keep the order they were first set
 * in, and setting an option again replaces its value. Options QEMU accepts more than once, such as {@code -drive} or
 * {@code -device}, are added with {@link #repeatable(String, String)}. Built configurations keep their own settings.
 *
 * <p>Builders are not thread-safe; use one per thread.
 */
public interface VmConfigurationBuilder {
  /**
   * Sets the memory of the machine ({@code -m}).
   *
   * @param memoryMegabytes the strictly positive memory amount, passed to QEMU with its {@code M} suffix
   * @return this builder
   * @throws IllegalArgumentException if {@code memoryMegabytes} is zero or negative
   */
  VmConfigurationBuilder memory(final int memoryMegabytes);

  /**
   * Sets the memory of the machine ({@code -m}) with a unit.
   *
   * @param amount the strictly positive amount
   * @param unit   the non-null QEMU unit suffix, such as {@code M} or {@code G}; it is appended verbatim
   *               and is not validated here
   * @return this builder
   * @throws IllegalArgumentException if {@code amount} is zero or negative
   * @throws NullPointerException     if {@code unit} is null
   */
  VmConfigurationBuilder memory(final int amount, final String unit);

  /**
   * Sets the number of virtual processors ({@code -smp}).
   *
   * @param cores the strictly positive number of virtual processors
   * @return this builder
   * @throws IllegalArgumentException if {@code cores} is zero or negative
   */
  VmConfigurationBuilder cores(final int cores);

  /**
   * Attaches an ISO image as the CD-ROM drive ({@code -cdrom}).
   *
   * @param isoPath the path of the image
   * @return this builder
   * @throws NullPointerException if {@code isoPath} is null
   */
  VmConfigurationBuilder cdrom(final String isoPath);

  /**
   * Attaches a disk image as the first hard disk ({@code -hda}).
   *
   * @param hdaPath the path of the image
   * @return this builder
   * @throws NullPointerException if {@code hdaPath} is null
   */
  VmConfigurationBuilder hda(final String hdaPath);

  /**
   * Attaches a disk image as the second hard disk ({@code -hdb}).
   *
   * @param hdbPath the path of the image
   * @return this builder
   * @throws NullPointerException if {@code hdbPath} is null
   */
  VmConfigurationBuilder hdb(final String hdbPath);

  /**
   * Sets the boot order ({@code -boot}).
   *
   * @param bootOrder the boot order, such as {@code d} for the CD-ROM first
   * @return this builder
   * @throws NullPointerException if {@code bootOrder} is null
   */
  VmConfigurationBuilder boot(final String bootOrder);

  /**
   * Sets the network configuration ({@code -nic}).
   *
   * @param networkConfiguration the configuration, such as {@code user,model=virtio-net-pci}
   * @return this builder
   * @throws NullPointerException if {@code networkConfiguration} is null
   */
  VmConfigurationBuilder network(final String networkConfiguration);

  /**
   * Sets the processor model ({@code -cpu}).
   *
   * @param model the model, such as {@code host} or {@code max}
   * @return this builder
   * @throws NullPointerException if {@code model} is null
   */
  VmConfigurationBuilder cpu(final String model);

  /**
   * Sets the machine type ({@code -machine}).
   *
   * @param machineType the type, such as {@code q35} or {@code virt}
   * @return this builder
   * @throws NullPointerException if {@code machineType} is null
   */
  VmConfigurationBuilder machine(final String machineType);

  /**
   * Sets the accelerator ({@code -accel}). Without it the player picks the fastest accelerator available on the
   * host platform and retries with software emulation when startup fails with an accelerator-related diagnostic.
   *
   * @param accelerator the accelerator, such as {@code kvm}, {@code whpx}, {@code hvf}, or {@code tcg}
   * @return this builder
   * @throws NullPointerException if {@code accelerator} is null
   */
  VmConfigurationBuilder accelerator(final String accelerator);

  /**
   * Sets the graphics adapter ({@code -vga}).
   *
   * @param adapter the adapter, such as {@code std} or {@code virtio}
   * @return this builder
   * @throws NullPointerException if {@code adapter} is null
   */
  VmConfigurationBuilder vga(final String adapter);

  /**
   * Adds a drive ({@code -drive}). Drives can be added more than once.
   *
   * @param drive the drive specification, such as {@code file=disk.qcow2,format=qcow2}
   * @return this builder
   * @throws NullPointerException if {@code drive} is null
   */
  VmConfigurationBuilder drive(final String drive);

  /**
   * Adds a device ({@code -device}). Devices can be added more than once.
   *
   * @param device the device specification, such as {@code usb-tablet}
   * @return this builder
   * @throws NullPointerException if {@code device} is null
   */
  VmConfigurationBuilder device(final String device);

  /**
   * Sets an option that takes a value, replacing an earlier value of the same option.
   *
   * @param key   the non-null, nonblank option name without the leading dash
   * @param value the non-null value, passed verbatim as one process argument; empty values are allowed
   * @return this builder
   * @throws IllegalArgumentException if {@code key} is blank
   * @throws NullPointerException     if {@code key} or {@code value} is null
   */
  VmConfigurationBuilder option(final String key, final String value);

  /**
   * Adds an option that takes a value and may appear more than once.
   *
   * @param key   the non-null, nonblank option name without the leading dash
   * @param value the non-null value, passed verbatim as one process argument; empty values are allowed
   * @return this builder
   * @throws IllegalArgumentException if {@code key} is blank
   * @throws NullPointerException     if {@code key} or {@code value} is null
   */
  VmConfigurationBuilder repeatable(final String key, final String value);

  /**
   * Adds an option without a value, such as {@code enable-kvm}.
   *
   * @param flag the non-null, nonblank option name without the leading dash; duplicates are ignored
   * @return this builder
   * @throws IllegalArgumentException if {@code flag} is blank
   * @throws NullPointerException     if {@code flag} is null
   */
  VmConfigurationBuilder flag(final String flag);

  /**
   * Removes an option, every value of a repeatable option, or a flag.
   *
   * @param key the option name without the leading dash
   * @return this builder
   * @throws NullPointerException if {@code key} is null
   */
  VmConfigurationBuilder remove(final String key);

  /**
   * Returns the configuration built so far.
   *
   * @return the configuration
   */
  VmConfiguration build();
}
