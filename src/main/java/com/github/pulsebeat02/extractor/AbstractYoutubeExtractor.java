package com.github.pulsebeat02.extractor;

import java.io.File;

public interface AbstractYoutubeExtractor {

    File downloadVideo();

    File extractAudio();

}
