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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

final class RuntimeDependenciesTest {

  private static final String NATIVE_BRIDGE_CLASS = "com/sun/jna/Native.class";
  private static final String NATIVE_PLATFORM_TYPES_CLASS = "com/sun/jna/platform/win32/WinDef.class";

  @Test
  void shouldHaveOneRuntimeDefinitionOfTheNativeBridge() throws IOException {
    assertSingleDefinition(NATIVE_BRIDGE_CLASS);
  }

  @Test
  void shouldHaveOneRuntimeDefinitionOfTheNativePlatformTypes() throws IOException {
    assertSingleDefinition(NATIVE_PLATFORM_TYPES_CLASS);
  }

  private static void assertSingleDefinition(final String name) throws IOException {
    final ClassLoader loader = RuntimeDependenciesTest.class.getClassLoader();
    final List<URL> definitions = Collections.list(loader.getResources(name));
    final int definitionCount = definitions.size();
    assertEquals(1, definitionCount, "one runtime definition of " + name + ": " + definitions);
  }
}
