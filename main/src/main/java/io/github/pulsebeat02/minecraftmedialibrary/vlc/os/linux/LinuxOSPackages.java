/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux;

import com.google.common.collect.ListMultimap;
import org.jetbrains.annotations.NotNull;

/**
 * A class for handling the packages for each distribution. It specifies the correct packages for
 * the version of the distribution, and each CPU architecture of it. Used for better organization
 * for the packages.
 */
public class LinuxOSPackages {

  /* Here for all URLs just in case I lose any (in comments due to not being used anymore)

  // https://pkgs.org/download/vlc

    ARCH_LINUX(
        ImmutableMap.of(
            "LATEST",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.tku.edu.tw/Linux/ArchLinux-arm/aarch64/extra/vlc-3.0.12-1-aarch64.pkg.tar.xz",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://ftp.tku.edu.tw/Linux/ArchLinux-arm/armv7h/extra/vlc-3.0.12-1-armv7h.pkg.tar.xz",
                    ARMV7H) // ARMV7H
                ))),

    CENTOS(
        ImmutableMap.of(
            "8",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/aarch64/os/Packages/v/vlc-3.0.12.1-6.fc35.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64)), // x86_64
            "7",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64)), // x86_64
            "6",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64), // x86_64
                new LinuxPackage(
                    "http://ftp.riken.jp/Linux/rpmfusion/free/el/updates/6/x86_64/vlc-core-2.0.10-3.el6.i686.rpm",
                    I386) // i386
                ))),

    DEBIAN(
        ImmutableMap.of(
            "Sid",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_i386.deb",
                    I386) // i386
                ),
            "11",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-2_i386.deb",
                    I386) // i386
                ),
            "10",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-0+deb10u1_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-0+deb10u1_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-0+deb10u1_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.12-0+deb10u1_i386.deb",
                    I386) // i386
                ),
            "9",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.11-0+deb9u1_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.11-0+deb9u1_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.11-0+deb9u1_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://ftp.debian.org/debian/pool/main/v/vlc/vlc_3.0.11-0+deb9u1_i386.deb",
                    I386) // i386
                ))),

    FEDORA(
        ImmutableMap.of(
            "Rawhide",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/aarch64/os/Packages/v/vlc-3.0.12.1-6.fc35.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/armhfp/os/Packages/v/vlc-3.0.12.1-6.fc35.armv7hl.rpm",
                    ARMHFP), // ARMHFP
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64) // x86_64
                ),
            "33",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/aarch64/os/Packages/v/vlc-3.0.12.1-6.fc35.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/armhfp/os/Packages/v/vlc-3.0.12.1-6.fc35.armv7hl.rpm",
                    ARMHFP), // ARMHFP
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64) // x86_64
                ),
            "32",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/aarch64/os/Packages/v/vlc-3.0.12.1-6.fc35.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/armhfp/os/Packages/v/vlc-3.0.12.1-6.fc35.armv7hl.rpm",
                    ARMHFP), // ARMHFP
                new LinuxPackage(
                    "http://rpmfind.net/linux/rpmfusion/free/fedora/development/rawhide/Everything/x86_64/os/Packages/v/vlc-3.0.12.1-6.fc35.x86_64.rpm",
                    X86_64) // x86_64
                ))),

    FREEBSD(
        ImmutableMap.of(
            "13",
            ImmutableSet.of(
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AMD64), // AMD64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    I386) // i386
                ),
            "12",
            ImmutableSet.of(
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AMD64), // AMD64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    ARMV7), // ARMV7
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    I386) // i386
                ),
            "11",
            ImmutableSet.of(
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    AMD64), // AMD64
                new LinuxPackage(
                    "https://club.bisd.ro/package/freebsd/13/x86/64/annex/uv/All/vlc-3.0.11_9%2C4.txz",
                    I386) // i386
                ))),

    KAOS(
        ImmutableMap.of(
            "LATEST",
            ImmutableSet.of(
                new LinuxPackage(
                    "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/linux/vlc-1_3.0.12.1-2-x86_64.pkg.tar.zst",
                    X86_64) // x86_64
                ))),

    NETBSD(
        ImmutableMap.of(
            "9.1",
            ImmutableSet.of(
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/x86_64/.9.0-current-2021-02-02-15.06/All/vlc-3.0.12.tgz",
                    X86_64), // x86_64
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/earmv7hf/9.0/All/vlc-3.0.11.1.tgz",
                    EARNMV7HF), // EARMV7HF
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/i386/9.0_2020Q2/All/vlc-3.0.11.tgz",
                    I386) // i386
                ),
            "8.2",
            ImmutableSet.of(
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/x86_64/8.0_2020Q2/All/vlc-3.0.11.tgz",
                    X86_64), // x86_64
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/earmv7hf/8.0/All/vlc-3.0.11.1.tgz",
                    EARNMV7HF), // EARNMV7HF
                new LinuxPackage(
                    "ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/i386/8.0_2020Q4/All/vlc-3.0.11.1.tgz",
                    I386) // i386
                ))),

    OPENSUSE(
        ImmutableMap.of(
            "Tumbleweed",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://ftp.us2.freshrpms.net/linux/opensuse/tumbleweed/repo/oss/i586/vlc-3.0.12-1.3.i586.rpm",
                    I586), // i586
                new LinuxPackage(
                    "http://ftp.us2.freshrpms.net/linux/opensuse/tumbleweed/repo/oss/x86_64/vlc-3.0.12-1.3.x86_64.rpm",
                    X86_64), // x86_64
                new LinuxPackage(
                    "http://fr2.rpmfind.net/linux/opensuse/ports/aarch64/tumbleweed/repo/oss/aarch64/vlc-3.0.12-1.3.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://fr2.rpmfind.net/linux/opensuse/ports/armv7hl/tumbleweed/repo/oss/armv7hl/vlc-3.0.12-1.3.armv7hl.rpm",
                    ARMV7HL) // ARMC7HL
                ),
            "Leap 15.2",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://download.videolan.org/pub/videolan/vlc//SuSE/Leap_15.2/x86_64/vlc-3.0.12-lp152.342.1.x86_64.rpm",
                    X86_64), // x86_64
                new LinuxPackage(
                    "http://fr2.rpmfind.net/linux/opensuse/ports/aarch64/distribution/leap/15.2/repo/oss/aarch64/vlc-3.0.10-lp152.1.1.aarch64.rpm",
                    AARCH64), // AARCH64
                new LinuxPackage(
                    "http://fr2.rpmfind.net/linux/opensuse/ports/armv7hl/distribution/leap/15.2/repo/oss/armv7hl/vlc-3.0.10-lp152.1.1.armv7hl.rpm",
                    ARMV7HL) // ARMV7HL
                ))),

    SLACKWARE(
        ImmutableMap.of(
            "current",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg/current/vlc-3.0.12-i586-1alien.txz",
                    I586), // i586
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg64/current/vlc-3.0.12-x86_64-1alien.txz",
                    X86_64) // x86_64
                ),
            "14.2",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg/14.2/vlc-3.0.12-i586-1alien.txz",
                    I586), // i586
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg64/14.2/vlc-3.0.12-x86_64-1alien.txz",
                    X86_64) // x86_64
                ),
            "14.1",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg/14.1/vlc-2.2.6-i486-1alien.txz",
                    I486), // i486
                new LinuxPackage(
                    "http://www.slackware.com/~alien/slackbuilds/vlc/pkg64/14.1/vlc-2.2.6-x86_64-1alien.txz",
                    X86_64) // x86_64
                ))),

    SOLUS(
        ImmutableMap.of(
            "LATEST",
            ImmutableSet.of(
                new LinuxPackage(
                    "https://mirrors.rit.edu/solus/packages/shannon/v/vlc/vlc-3.0.12-123-1-x86_64.eopkg",
                    X86_64) // x86_64
                ))),

    UBUNTU(
        ImmutableMap.of(
            "20.10",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.11.1-2_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.11.1-2_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.11.1-2_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.11.1-2_i386.deb",
                    I386) // i386
                ),
            "20.04",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.9.2-1_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.9.2-1_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.9.2-1_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.9.2-1_i386.deb",
                    I386) // i386
                ),
            "18.04",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.1-3build1_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.1-3build1_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_3.0.1-3build1_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_3.0.1-3build1_i386.deb",
                    I386) // i386
                ),
            "16.04",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_2.2.2-5_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_2.2.2-5_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_2.2.2-5_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_2.2.2-5_i386.deb",
                    I386) // i386
                ),
            "14.04",
            ImmutableSet.of(
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_2.1.2-2build2_amd64.deb",
                    AMD64), // AMD64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_2.1.2-2build2_arm64.deb",
                    ARM64), // ARM64
                new LinuxPackage(
                    "http://ports.ubuntu.com/ubuntu-ports/pool/universe/v/vlc/vlc_2.1.2-2build2_armhf.deb",
                    ARMHF), // ARMHF
                new LinuxPackage(
                    "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/vlc_2.1.2-2build2_i386.deb",
                    I386) // i386
                )));
  */

  private final ListMultimap<String, LinuxPackage> links;

  /**
   * Creates new distribution per operating system.
   *
   * @param links URLs
   */
  public LinuxOSPackages(@NotNull final ListMultimap<String, LinuxPackage> links) {
    this.links = links;
  }

  /**
   * Gets links for each version.
   *
   * @return links for each system
   */
  public ListMultimap<String, LinuxPackage> getLinks() {
    return links;
  }
}
