package com.github.pulsebeat02.minecraftmedialibrary.dependency;

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

    HTTPCLIENT(
            "org.apache.httpcomponents",
            "httpclient",
            "4.5.13"
    ),

    HTTPMINE(
            "org.apache.httpcomponents",
            "httpmime",
            "4.5.13"
    ),

//    JAVE(
//            "ws.schild",
//            "jave-all-deps",
//            "2.7.3"
//    ),

    JAVACV(
            "org.bytedeco",
            "javacv-platform",
            "1.5.4"
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
