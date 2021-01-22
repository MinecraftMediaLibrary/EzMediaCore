package com.github.pulsebeat02.extractor;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.pulsebeat02.utility.ExtractorUtilities;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.IOException;

public class YoutubeExtraction implements AbstractVideoExtractor {

    private final String url;
    private final String directory;
    private VideoDetails details;
    private File video;
    private File audio;

    public YoutubeExtraction(final String url, final String directory) {
        this.url = url;
        this.directory = directory;
    }

    @Override
    public File downloadVideo() {
        onVideoDownload();
        File videoFile = null;
        final YoutubeDownloader downloader = new YoutubeDownloader();
        final String ID = ExtractorUtilities.getVideoID(url);
        if (ID != null) {
            try {
                final YoutubeVideo video = downloader.getVideo(ID);
                details = video.details();
                videoFile = video.download(video.videoWithAudioFormats().get(0), new File(directory),
                        "video",
                        true);
            } catch (IOException | YoutubeException e) {
                e.printStackTrace();
            }
        }
        return videoFile;
    }

    @Override
    public File extractAudio() {
        onAudioExtraction();
        File sound = new File(directory + "/audio.ogg");
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libvorbis");
        audio.setBitRate(160000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);
        audio.setVolume(48);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("ogg");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(video), sound, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return sound;
    }

    @Override
    public void onVideoDownload() {
    }

    @Override
    public void onAudioExtraction() {
    }

    public String getDirectory() {
        return directory;
    }

    public VideoDetails getDetails() {
        return details;
    }

    public File getVideo() {
        return video;
    }

    public File getAudio() {
        return audio;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return details.author();
    }

    public String getVideoTitle() {
        return details.title();
    }

    public String getVideoDescription() {
        return details.description();
    }

    public String getVideoId() {
        return details.videoId();
    }

    public int getVideoRating() {
        return details.averageRating();
    }

    public long getViewerCount() {
        return details.viewCount();
    }

    public boolean isLive() {
        return details.isLive();
    }

    public boolean isLiveContent() {
        return details.isLiveContent();
    }

}
