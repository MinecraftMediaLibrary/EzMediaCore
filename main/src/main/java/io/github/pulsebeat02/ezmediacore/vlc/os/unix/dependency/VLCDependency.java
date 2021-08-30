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
package io.github.pulsebeat02.ezmediacore.vlc.os.unix.dependency;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public enum VLCDependency {

  //  LIBA52(of("liba52", "https://liba52.sourceforge.io/files/a52dec-0.7.4.tar.gz", Set.of())),
//  LIBMAD(of("libmad",
//      "https://sourceforge.net/projects/mad/files/madplay/0.15.1b/madplay-0.15.1b.tar.gz",
//      Set.of())),
//  LIBMPEG2(of("libmpeg2", "https://libmpeg2.sourceforge.io/files/libmpeg2-0.5.1.tar.gz", Set.of())),
//  LIBAVCODEC(of("libavcodec", "https://ffmpeg.org/releases/ffmpeg-snapshot.tar.bz2", Set.of())),
//  LIBOGG(of("libogg", "https://downloads.xiph.org/releases/ogg/libogg-1.3.5.tar.gz", Set.of())),
//  LIBVORBIS(of("libvorbis", "https://downloads.xiph.org/releases/vorbis/libvorbis-1.3.7.tar.xz",
//      Set.of())),
//  LIBFLAC(of("libflac", "https://downloads.xiph.org/releases/flac/flac-1.3.2.tar.xz", Set.of())),
//  LIBSPEEX(
//      of("libspeex", "https://downloads.xiph.org/releases/speex/speex-1.2.0.tar.gz", Set.of())),
//  LIBTHEORA(of("libtheora", "https://downloads.xiph.org/releases/theora/libtheora-1.1.1.tar.bz2",
//      Set.of())),
//  LIBAFAAD2(of("libfaad2", "https://github.com/gypified/libfaad/archive/refs/heads/master.zip",
//      Set.of())),
//  LIBCDA(
//      of("libcda", "https://download.videolan.org/pub/videolan/libdca/0.0.6/libdca-0.0.6.tar.bz2",
//          Set.of())),
//
//  LIBSDL(of("libsdl", "http://www.libsdl.org/release/SDL2-2.0.14.tar.gz", Set.of())),
//  LIBDVDCSS(of("libdvdcss",
//      "https://github.com/MinecraftMediaLibrary/VLC-Compile-Dependencies/raw/main/libdvdcss-master.tar.gz",
//      Set.of())),
//  LIBDVDNAV(of("libdvdnav",
//      "https://download.videolan.org/pub/videolan/libdvdnav/6.1.1/libdvdnav-6.1.1.tar.bz2",
//      Set.of())),
//  LIBDVDREAD(of("libdvdread",
//      "https://download.videolan.org/pub/videolan/libdvdread/6.1.2/libdvdread-6.1.2.tar.bz2",
//      Set.of())),
//  LIBDVBPSI(
//      of("libdvbpsi", "https://download.videolan.org/pub/libdvbpsi/1.3.3/libdvbpsi-1.3.3.tar.bz2",
//          Set.of())),
//  LIBOPENSLP(of("libopenslp",
//      "http://sourceforge.net/projects/openslp/files/2.0.0/2.0.0%20Release/openslp-2.0.0.tar.gz",
//      Set.of())),
//  LIVEMEDIA(
//      of("liveMedia", "http://www.live555.com/liveMedia/public/live555-latest.tar.gz", Set.of())),
//  MATROSKA(
//      of("matroska", "https://github.com/Matroska-Org/libmatroska/archive/refs/heads/master.zip",
//          Set.of()));
  PLACEHOLDER_DEPENDENCY(of("", "", Set.of()));

  private final UnixDependency dependency;

  VLCDependency(@NotNull final UnixDependency dependency) {
    this.dependency = dependency;
  }

  private static UnixDependency of(@NotNull final String name, @NotNull final String url,
      @NotNull final Set<UnixDependency> dependencies) {
    return new UnixDependency(name, url, dependencies);
  }

  public UnixDependency getDependency() {
    return this.dependency;
  }
}
