/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import org.jetbrains.annotations.NotNull;

public enum MavenDependency {
  VLCJ("uk.co.caprica", "vlcj", "4.6.0"),

  VLCJ_NATIVES("uk.co.caprica", "vlcj-natives", "4.5.0"),

  YOUTUBE_DOWNLOADER("com.github.sealedtx", "java-youtube-downloader", "2.4.6"),

  JAVE_CORE("ws.schild", "jave-core", "3.0.1"),

  JAVACV("org.bytedeco", "javacv-platform", "1.5.4");

  private final String group;
  private final String artifact;
  private final String version;

  MavenDependency(
      @NotNull final String group, @NotNull final String artifact, @NotNull final String version) {
    this.group = group;
    this.artifact = artifact;
    this.version = version;
  }

  public String getGroup() {
    return group;
  }

  public String getArtifact() {
    return artifact;
  }

  public String getVersion() {
    return version;
  }
}
