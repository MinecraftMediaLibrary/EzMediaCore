package com.github.pulsebeat02.minecraftmedialibrary.extractor;

import java.io.File;

public interface AbstractVideoExtractor {

    /**
     * Downloads the specified video given the url. If the video
     * is a Youtube URL and the video id could not be extracted,
     * it will throw an InvalidYoutubeURLException.
     *
     * @return File for the downloaded video
     */
    File downloadVideo();

    /**
     * Extracts audio from the video just downloaded. If the video
     * was not downloaded, it will prompt to call the downloadVideo()
     * method first and then extract it. By default, this will return
     * an ogg file.
     *
     * @return File for the extracted audio
     */
    File extractAudio();

    /**
     * This method is called at the beginning of video download. Useful
     * for preparation in advance of the video download.
     */
    void onVideoDownload();

    /**
     * This method is called at the beginning of audio extraction. Useful
     * for prparation in advance of audio extraction.
     */
    void onAudioExtraction();

}
