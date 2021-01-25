package com.github.pulsebeat02.dependency;

import org.jetbrains.annotations.NotNull;

public enum MavenDependency {

    VLCJ(
            "uk.co.caprica",
            "vlcj",
            "4.6.0"
    ),

    YOUTUBE_DOWNLOADER(
            "com.github.sealedtx",
            "java-youtube-downloader",
            "2.4.6"
    ),

    JAVE(
            "ws.schild",
            "jave-all-deps",
            "2.7.3"
    );

    private final String group;
    private final String artifact;
    private final String version;

    MavenDependency(@NotNull final String group,
                    @NotNull final String artifact,
                    @NotNull final String version) {
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
