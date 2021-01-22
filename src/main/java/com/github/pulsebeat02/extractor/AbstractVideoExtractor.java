package com.github.pulsebeat02.extractor;

import java.io.File;

public interface AbstractVideoExtractor {

    File downloadVideo();

    File extractAudio();

    void onVideoDownload();

    void onAudioExtraction();

}
