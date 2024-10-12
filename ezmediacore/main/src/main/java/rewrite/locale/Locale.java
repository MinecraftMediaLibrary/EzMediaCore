/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rewrite.locale;

import java.nio.file.Path;

import static rewrite.locale.MessageLoader.key;

public interface Locale {
  NullComponent SYSTEM_DIAGNOSTIC = toNullComponent("ezmediacore.log.diagnostic");
  NullComponent PACKET_HANDLER = toNullComponent("ezmediacore.log.handle");
  NullComponent LOOKUP_CACHE = toNullComponent("ezmediacore.log.cache");
  NullComponent EVENT_REGISTRATION = toNullComponent("ezmediacore.log.event");
  NullComponent FILE_CREATION = toNullComponent("ezmediacore.log.folder");
  NullComponent DEPENDENCY_LOADING = toNullComponent("ezmediacore.log.dependency");
  NullComponent NATIVE_LOG_REGISTRATION = toNullComponent("ezmediacore.log.native");
  NullComponent PLAYER_RELEASE = toNullComponent("ezmediacore.log.release");
  NullComponent PLAYER_PAUSE = toNullComponent("ezmediacore.log.pause");
  NullComponent PLAYER_RESUME = toNullComponent("ezmediacore.log.resume");
  NullComponent SERVER_SOFTWARE = toNullComponent("ezmediacore.log.server.software");
  NullComponent SPOTIFY_AUTHENTICATION = toNullComponent("ezmediacore.log.spotify.auth");
  UniComponent<String> UNSUPPORTED_SERVER = key("ezmediacore.error.server")::formatted;
  UniComponent<String> DEPENDENCY_LOAD = key("ezmediacore.log.dependency.load")::formatted;
  BiComponent<String, String> PLAYER_START = key("ezmediacore.log.start")::formatted;
  BiComponent<String, Path> BINARY_PATHS = key("ezmediacore.log.binary")::formatted;
  TriComponent<String, String, Boolean> SERVER_INFO = key("ezmediacore.log.http")::formatted;
  QuadComponent<String, String, String, String> SYSTEM_INFO =
      key("ezmediacore.log.system")::formatted;
  HeptaComponent<String, String, Boolean, Path, Path, Path, Path> PLUGIN_INFO =
      key("ezmediacore.log.plugin")::formatted;

  @Contract(pure = true)
  static  NullComponent toNullComponent( final String key) {
    return () -> key(key);
  }

  @FunctionalInterface
  interface NullComponent {

    String build();
  }

  @FunctionalInterface
  interface UniComponent<A0> {

    String build(A0 arg0);
  }

  @FunctionalInterface
  interface BiComponent<A0, A1> {

    String build(A0 arg0, A1 arg1);
  }

  @FunctionalInterface
  interface TriComponent<A0, A1, A2> {

    String build(A0 arg0, A1 arg1, A2 arg2);
  }

  @FunctionalInterface
  interface QuadComponent<A0, A1, A2, A3> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);
  }

  @FunctionalInterface
  interface PentaComponent<A0, A1, A2, A3, A4> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
  }

  @FunctionalInterface
  interface HexaComponent<A0, A1, A2, A3, A4, A5> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
  }

  @FunctionalInterface
  interface HeptaComponent<A0, A1, A2, A3, A4, A5, A6> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);
  }
}
