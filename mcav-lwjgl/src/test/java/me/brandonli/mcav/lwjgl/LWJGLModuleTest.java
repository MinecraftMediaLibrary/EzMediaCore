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
package me.brandonli.mcav.lwjgl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.util.List;
import me.brandonli.mcav.module.MCAVModule;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

/**
 * Tests {@link LWJGLModule}.
 */
final class LWJGLModuleTest {

  @Test
  void shouldBeNamedLwjgl() {
    final LWJGLModule module = new LWJGLModule();
    final String name = module.getModuleName();
    assertEquals("lwjgl", name);
  }

  @Test
  void shouldStartAndStopWithoutPreparingAnything() {
    final MCAVModule module = new LWJGLModule();
    try (final MockedConstruction<GLTextureFilter> filters = Mockito.mockConstruction(GLTextureFilter.class)) {
      module.start();
      final List<GLTextureFilter> afterFirstStart = filters.constructed();
      assertEquals(List.of(), afterFirstStart);
      module.stop();
      final List<GLTextureFilter> afterFirstStop = filters.constructed();
      assertEquals(List.of(), afterFirstStop);
      module.start();
      final List<GLTextureFilter> afterSecondStart = filters.constructed();
      assertEquals(List.of(), afterSecondStart);
      module.stop();
      final List<GLTextureFilter> afterSecondStop = filters.constructed();
      assertEquals(List.of(), afterSecondStop);
    }
    final String name = module.getModuleName();
    assertEquals("lwjgl", name);
  }

  @Test
  void shouldBeCreatableByTheModuleLoader() throws ReflectiveOperationException {
    final Constructor<LWJGLModule> constructor = LWJGLModule.class.getConstructor();
    final LWJGLModule module = constructor.newInstance();
    final String name = module.getModuleName();
    assertEquals("lwjgl", name);
  }
}
