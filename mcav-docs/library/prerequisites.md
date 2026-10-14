# Library Prerequisites

MCAV is a Java library that can be used in any project. To get started, add the MCAV core dependency to your project
with a build system that supports Maven repositories, such as Gradle or Maven.

```{note}
All of MCAV's modules require Java 25 or newer.
```

```kotlin
repositories {
    maven("https://repo.brandonli.me/snapshots")
}
```

```kotlin
dependencies {
    implementation("me.brandonli:mcav-common:1.0.0-SNAPSHOT")
}
```

## Supported Platforms

MCAV targets the following platforms. Everything MCAV downloads is stored in the cache folder of the current user;
optional native features can also need system libraries, a display, or an audio device.

| Platform       | VLC                                                               | yt-dlp        | FFmpeg and OpenCV |
|----------------|-------------------------------------------------------------------|---------------|-------------------|
| Windows x86-64 | Downloaded from VideoLAN when missing                             | Downloaded    | Bundled           |
| Windows ARM64  | Downloaded from VideoLAN when missing                             | Downloaded    | Not available     |
| macOS x86-64   | Downloaded from VideoLAN and mounted without administrator rights | Downloaded    | Bundled           |
| macOS ARM64    | Downloaded from VideoLAN and mounted without administrator rights | Downloaded    | Bundled           |
| Linux x86-64   | Used from the system, or downloaded as an AppImage                | Downloaded    | Bundled           |
| Linux ARM64    | Used from the system only (`apt install vlc`)                     | Downloaded    | Bundled           |
| FreeBSD        | Not available                                                     | Not available | Not available     |

When VLC is missing and the platform has no download, starting it fails with an `UnsupportedOperatingSystemException`
that names the platform. FFmpeg and OpenCV come from the JavaCPP builds MCAV depends on, which have none for Windows on
ARM64 or for FreeBSD; there, the features that need them are not available. 32-bit x86 is not listed, because Java 25,
which MCAV needs, has no port for it.

On Windows and macOS the download is VLC 3.0.24 from VideoLAN, checked against the SHA-256 hash MCAV ships. VideoLAN
publishes no Linux build, so on x86-64 Linux MCAV downloads an AppImage built from the VLC package of Arch Linux
(<https://github.com/ivan-hc/VLC-appimage>): one dated release, VLC 3.0.23 of 2026-10-01, pinned with the SHA-256 hash
MCAV ships, like the others. MCAV runs the AppImage to unpack it, so it runs only that reviewed build, never what the
project's weekly tag points to later; a newer VLC there needs a new pin in MCAV. A server that should run no build of a
third party installs VLC from its distribution (`apt install vlc`, `dnf install vlc`): MCAV then uses that VLC and
downloads nothing.

FFmpeg and OpenCV are bundled with the library for every platform marked so above. The OpenCV build differs per
platform: the Windows and macOS builds read video files themselves, the Linux build has no file backend and only
captures from cameras. `VideoPlayer.opencv()` therefore reads files with the bundled FFmpeg where OpenCV cannot, so a
file plays on every platform where they are bundled. OpenCV capture still depends on a working capture backend and
device. VLC and yt-dlp are optional: when one of them cannot be installed, only the features that need it are
unavailable, which you can check with [capabilities](instance.md#capabilities).

QEMU is never installed by MCAV. To use the [virtual machine module](vm.md), install QEMU yourself and make sure it is
on the `PATH`.

## Native Access Warnings

Java 24 and newer print a warning the first time a library loads native code. The warning is harmless. To hide it,
start the JVM with the following argument:

```bash
--enable-native-access=ALL-UNNAMED
```
