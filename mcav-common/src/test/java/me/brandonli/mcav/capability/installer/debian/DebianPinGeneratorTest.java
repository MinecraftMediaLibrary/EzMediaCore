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
package me.brandonli.mcav.capability.installer.debian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class DebianPinGeneratorTest {

  private static final String INDEX = """
  Package: libroot1
  Architecture: arm64
  Pre-Depends: libc6 (>= 2.36)
  Depends: libmiddle2 (>= 1.0), libalternative1 | libunused1, libvirtual, libroot-data
  Filename: pool/main/r/root/libroot1_1.0_arm64.deb
  SHA256: 1111
  Description: a root library
   whose description: spans lines

  Package: libmiddle2
  Architecture: arm64
  Depends: libleaf0:any, libroot1
  Filename: pool/main/m/middle/libmiddle2_2.0_arm64.deb
  SHA256: 2222

  Package: libleaf0
  Architecture: arm64
  Depends: libgcc-s1, libstdc++6
  Filename: pool/main/l/leaf/libleaf0_0.1_arm64.deb
  SHA256: 3333

  Package: libalternative1
  Architecture: arm64
  Filename: pool/main/a/alternative/libalternative1_1_arm64.deb
  SHA256: 4444

  Package: libunused1
  Architecture: arm64
  Filename: pool/main/u/unused/libunused1_1_arm64.deb
  SHA256: 5555

  Package: libprovider3
  Architecture: arm64
  Provides: libvirtual (= 3)
  Filename: pool/main/p/provider/libprovider3_3_arm64.deb
  SHA256: 6666

  Package: libroot-data
  Architecture: all
  Depends: libunused1
  Filename: pool/main/r/root/libroot-data_1.0_all.deb
  SHA256: 7777

  Package: libc6
  Architecture: arm64
  Filename: pool/main/g/glibc/libc6_2.36_arm64.deb
  SHA256: 8888
  """;

  private DebianPinGenerator generator;

  @BeforeEach
  void setUp() {
    this.generator = new DebianPinGenerator();
  }

  @Test
  void shouldListEveryPackageAfterItsDependencies() {
    final List<DebianPinGenerator.IndexEntry> closure = this.generator.resolveClosure(INDEX, List.of("libroot1"));

    final List<String> names = closure.stream().map(DebianPinGenerator.IndexEntry::name).toList();
    final List<String> expected = List.of("libleaf0", "libmiddle2", "libalternative1", "libprovider3", "libroot1");
    assertEquals(expected, names);
  }

  @Test
  void shouldKeepTheFileAndHashOfEachPackage() {
    final List<DebianPinGenerator.IndexEntry> closure = this.generator.resolveClosure(INDEX, List.of("libleaf0"));

    final DebianPinGenerator.IndexEntry leaf = closure.getFirst();
    assertEquals("pool/main/l/leaf/libleaf0_0.1_arm64.deb", leaf.filename());
    assertEquals("3333", leaf.sha256());
  }

  @Test
  void shouldRefuseADependencyMissingFromTheIndex() {
    final List<String> roots = List.of("libmissing1");

    final IllegalStateException failure = assertThrows(IllegalStateException.class, () -> this.generator.resolveClosure(INDEX, roots));
    final String message = failure.getMessage();
    assertEquals("Package libmissing1 is missing from the index!", message);
  }
}
