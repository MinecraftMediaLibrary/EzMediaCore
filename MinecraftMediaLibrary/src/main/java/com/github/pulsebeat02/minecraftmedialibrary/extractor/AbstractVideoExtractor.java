/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

import java.io.File;

public interface AbstractVideoExtractor {

  /**
   * Downloads the specified video given the url. If the video is a Youtube URL and the video id
   * could not be extracted, it will throw an InvalidYoutubeURLException.
   *
   * @return the downloaded video
   */
  File downloadVideo();

  /**
   * Extracts audio from the video just downloaded. If the video was not downloaded, it will prompt
   * to call the downloadVideo() method first and then extract it. By default, this will return an
   * ogg file.
   *
   * @return the extracted audio
   */
  File extractAudio();

  /**
   * This method is called at the beginning of video download. Useful for preparation in advance of
   * the video download.
   */
  void onVideoDownload();

  /**
   * This method is called at the beginning of audio extraction. Useful for preparation in advance
   * of audio extraction.
   */
  void onAudioExtraction();
}
