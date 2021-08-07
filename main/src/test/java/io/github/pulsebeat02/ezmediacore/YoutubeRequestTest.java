package io.github.pulsebeat02.ezmediacore;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.nio.file.Path;

public class YoutubeRequestTest {

  // https://www.youtube.com/watch?v=09R8_2nJtjg

  public static void main(final String[] args) {

    final Path path = Path.of(System.getProperty("user.dir")).resolve("test");

    final YoutubeDownloader downloader = new YoutubeDownloader();
    final RequestVideoInfo request = new RequestVideoInfo(
        MediaExtractionUtils.getYoutubeID("https://www.youtube.com/watch?v=09R8_2nJtjg")
            .orElseThrow());
    final Response<VideoInfo> response = downloader.getVideoInfo(request);
    final VideoInfo video = response.data();

    System.out.println("d");

//    new YoutubeDownloader().downloadVideoFile(
//        new RequestVideoFileDownload(
//
//        )
//    )
  }

}
