/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.locale;

import java.nio.file.Path;

public interface Locale {

  NullComponent FINISHED_SYSTEM_DIAGNOSTIC = () -> "Finished System Diagnostic Analysis";
  NullComponent FINISHED_PACKET_HANDLE = () -> "Found valid PacketHandler implementation!";
  NullComponent FINISHED_LOOKUP_CACHE = () -> "Finished loading lookup cache!";
  NullComponent FINISHED_DEPENDENCY_LOADER = () -> "Finished initializing Dependency Loader!";
  NullComponent FINISHED_EVENT_REGISTRATION = () -> "Finished registering events!";
  NullComponent FINISHED_FOLDER_CREATION = () -> "Finished folder creation!";
  NullComponent FINISHED_DEPENDENCY_HANDLING = () -> "Finished dependency handling!";

  NullComponent FINISHED_NATIVE_VLC_LOG_REGISTRATION = () -> "Registered Native VLC log!";

  UniComponent<String> FINISHED_FFMPEG_PROCESS_CREATION = "Finished FFmpeg process constructor with arguments %s!"::formatted;

  NullComponent FINISHED_JCODEC_FRAME_GRABBER = () -> "Finished JCodec frame grabber object!";

  BiComponent<String, String> MEDIA_PLAYER_START = "Playing media on resource mrl %s with arguments %s"::formatted;
  BiComponent<String, String> MEDIA_PLAYER_RESUME = "Resuming media on resource mrl %s with arguments %s"::formatted;
  NullComponent MEDIA_PLAYER_RELEASE = () -> "Released media player!";
  NullComponent MEDIA_PLAYER_PAUSE = () -> "Paused media player!";

  NullComponent SERVER_SOFTWARE_TIP = () ->
      """

             As a reminder, the only server software supported by this library are Spigot and Paper.
             Tunity has been merged into Paper and will not be supported. Custom forks will also not
             be supported. If you can reproduce an issue on Spigot or Paper 1.19, I am welcome to
             accept and analyze it. Otherwise, I cannot do anything. This message is automatically
             sent to all servers.
          """;
  NullComponent PACKET_COMPRESSION_TIP = () ->
      """
             
             Setting the value "network-compression-threshold", to -1 in the server.properties
             file may lead to improved performance of video players for servers that aren't proxy
             servers.
          """;
  NullComponent WARN_SPOTIFY_AUTH = () ->
      """
              
              Spotify API Client ID and Client Secret not specified! You will not be able
              to use any Spotify related features.
          """;
  UniComponent<String> ERR_SERVER_UNSUPPORTED = "Current server implementation (%s) is not supported!"::formatted;
  UniComponent<String> FINISHED_DEPENDENCY_LOAD = "Finished %s dependency installation and load!"::formatted;

  BiComponent<String, Path> BINARY_PATHS = "%s path: %s"::formatted;
  TriComponent<String, String, Boolean> SERVER_INFO =
      """
                
          ===========================================
                       SERVER INFORMATION
          ===========================================
          NAME: %s
          VERSION: %s
          ONLINE MODE: %s
          """::formatted;

  QuadComponent<String, String, String, String> SYSTEM_INFO =
      """
                
          ===========================================
                       SYSTEM INFORMATION
          ===========================================
          OS: %s
          VERSION: %s
          DISTRO: %s
          CPU: %s
          """::formatted;

  HeptaComponent<String, String, Boolean, Path, Path, Path, Path> PLUGIN_INFO =
      """
                
          ===========================================
                       PLUGIN INFORMATION
          ===========================================
          NAME: %s
          DESCRIPTION: %s
          LIB DISABLED: %s
          LIB PATH: %s
          VLC PATH: %s
          IMAGE PATH: %s
          AUDIO PATH: %s
          """::formatted;

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
