package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.slimjar.relocation.RelocationRule;
import org.jetbrains.annotations.NotNull;

public enum RelocationPaths {
  VLCJ("uk{}co{}caprica{}vlcj", "io{}github{}pulsebeat02{}ezmediacore{}lib{}vlcj"),
  VLCJ_NATIVES(
      "uk{}co{}caprica{}vlcj{}binding", "io{}github{}pulsebeat02{}ezmediacore{}lib{}vlcj{}binding"),
  VLCJ_NATIVE_STREAMS(
      "uk{}co{}caprica{}nativestreams",
      "io{}github{}pulsebeat02{}ezmediacore{}lib{}vlcj{}nativestreams"),
  YOUTUBE_DOWNLOADER(
      "com{}github{}kiulian{}downloader", "io{}github{}pulsebeat02{}ezmediacore{}lib{}youtube"),
  JAVE_CORE("ws{}schild{}jave", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jave"),
  COMMONS_COMPRESSION(
      "org{}apache{}commons{}compress", "io{}github{}pulsebeat02{}ezmediacore{}lib{}compress"),
  COMPRESSION(
      "org{}rauschig{}jarchivelib", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jarchivelib"),
  XZ("org{}tukaani.xz", "io{}github{}pulsebeat02{}ezmediacore{}lib{}xz"),
  FAST_JSON("com{}alibaba{}fastjson", "io{}github{}pulsebeat02{}ezmediacore{}lib{}fastjson"),
  SPOTIFY("com{}wrapper{}spotify", "io{}github{}pulsebeat02{}ezmediacore{}lib{}spotify"),
  JAFFREE("com{}github{}kokorin", "io{}github{}pulsebeat02{}ezmediacore{}lib{}kokorin"),
  JCODEC("org{}jcodec", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jcodec"),
  CAFFEINE(
      "com{}github{}benmanes{}caffeine", "io{}github{}pulsebeat02{}ezmediacore{}lib{}caffeine");

  private final RelocationRule relocation;

  RelocationPaths(@NotNull final String before, @NotNull final String after) {
    this.relocation =
        new RelocationRule(before.replaceAll("\\{}", "."), after.replaceAll("\\{}", "."));
  }

  public RelocationRule getRelocation() {
    return this.relocation;
  }
}
