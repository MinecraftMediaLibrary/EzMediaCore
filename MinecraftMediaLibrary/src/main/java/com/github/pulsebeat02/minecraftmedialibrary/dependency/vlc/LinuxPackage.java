package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public enum LinuxPackage {

    // TODO: Add packages based from https://pkgs.org/download/vlc

    ARCH_LINUX(ImmutableMap.of(
            "LATEST", ImmutableSet.of(
                    "vlc-3.0.12-1-aarch64.pkg.tar.xz", // AARCH64
                    "vlc-3.0.12-1-armv7h.pkg.tar.xz",  // ARMV7H
                    "vlc-3.0.12-1-x86_64.pkg.tar.zst") // Extra
    )),

    // NUX Only Supported
    CENTOS(ImmutableMap.of(
            "8", ImmutableSet.of(
                    "vlc-3.0.12-1.el8.aarch64.rpm",    // AARCH64
                    "vlc-3.0.10-2.el8.x86_64.rpm"),    // x86_64
            "7", ImmutableSet.of(
                    "vlc-2.2.8-1.el7.nux.x86_64.rpm",  // x86_64
                    "vlc-2.2.5.1-2.el7.nux.x86_64.rpm",
                    "vlc-2.2.4-1.el7.nux.x86_64.rpm",
                    "vlc-2.2.2-6.el7.nux.x86_64.rpm",
                    "vlc-2.2.2-5.el7.nux.x86_64.rpm",
                    "vlc-2.2.2-4.el7.nux.x86_64.rpm",
                    "vlc-2.2.2-3.el7.nux.x86_64.rpm",
                    "vlc-2.2.1-6.el7.nux.x86_64.rpm",
                    "vlc-2.1.6-2.el7.nux.x86_64.rpm",
                    "vlc-2.1.5-3.el7.nux.x86_64.rpm",
                    "vlc-2.1.5-2.el7.nux.x86_64.rpm",
                    "vlc-2.1.4-6.el7.nux.x86_64.rpm",
                    "vlc-2.1.4-5.el7.nux.x86_64.rpm")
    )),

    DEBIAN(ImmutableMap.of(
            "Sid", ImmutableSet.of(
                    "vlc_3.0.12-2_amd64.deb", // AMD64
                    "vlc_3.0.12-2_arm64.deb", // ARM64
                    "vlc_3.0.12-2_armhf.deb") // ARMHF
    )),
    FEDORA(),
    FREEBSD(),
    KAOS(),
    NETBSD(),
    OPENSUSE(),
    SLACKWARE(),
    SOLUS(),
    UBUNTU()

    private final Map<String, Set<String>> links;

    LinuxPackage(@NotNull final Map<String, Set<String>> links) {
        this.links = links;
    }

    public Map<String, Set<String>> getLinks() {
        return links;
    }

    public static String getBaseUrl() {
        return "http://archive.ubuntu.com/ubuntu/pool/universe/v/vlc/";
    }

}
