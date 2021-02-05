[![CircleCI](https://img.shields.io/circleci/build/github/PulseBeat02/MinecraftMediaLibrary?style=for-the-badge)](https://app.circleci.com/pipelines/github/PulseBeat02/MinecraftMediaLibrary)

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/PulseBeat02/MinecraftMediaLibrary)

**You can use Gitpod for an online IDE experience**

---

**MinecraftMediaLibrary** is a library written along the Spigot API and net.minecraft.server classes to provide helpful and
useful classes for other plugins to take advantage of. One of the most important features perhaps is its ability to
play **videos** on a Minecraft Spigot server. It uses
a [very optimized dithering method](https://github.com/PulseBeat02/MinecraftMediaLibrary/blob/b47e10869cbcea03889670765aa3ef66d6ba171a/MinecraftMediaLibrary/src/main/java/com/github/pulsebeat02/minecraftmedialibrary/video/dither/FloydImageDither.java#L177) (
credits to **BananaPuncher714** and **Jetp250** for helping out with the Floyd Steinberg Dithering) along side with 
**VLCJ Integration** if necessary to parse the video even quicker. As a result, frames can reach up to **35** times per
second at times with very good quality on maps if necessary. As a reference, a *smooth* animation is one which is 24
frames only.

The plugin takes advantages of maps to handle its video playback. Currently, it uses maps with ids **0 to 25** to
display the video. However, it is likely in the near future I will add an implementation which allows you to change
this.

Using the library is very easy. In fact, it allows for easy serving of resourcepacks as well (if you are going to play
videos). Downloading a video's audio from Youtube and putting it into a modpack is as simple as the following steps:

1) Create a `MinecraftMediaLibrary` instance to use.
2) Create a `YoutubeExtraction` given a url and directory to download the audio/video files at. Call the
   methods `YoutubeExtraction#downloadVideo()` and `YoutubeExtraction#extractAudio()` to extract the media.
   ```java
    YoutubeExtraction extraction = new YoutubeExtraction(youtubeUrl, directory);
    extraction.downloadVideo();
    extraction.extractAudio();
   ```
3) Create a `ResourcepackWrapper` to specify specific details you want to use in your resourcepack. This could include
   the audio file, description, pack format, pack icon, etc. A build
   class (`ResourcePackWrapper.ResourcepackWrapperBuilder`) has been provided to help create this with ease. Call
   the `ResourcePackWrapper#buildResourcePack()` method to build the resourcepack.
   ```java
    ResourcepackWrapper wrapper = new ResourcepackWrapper.Builder()
        .setAudio(extraction.getAudio())
        .setDescription("Title of Youtube Video: " + extraction.getVideoTitle())
        .setPath(directory)
        .setPackFormat(6)
        .createResourcepackHostingProvider(this);
   wrapper.buildResourcePack();
   ```
4) Create a `HttpDaemonProvider` for hosting or another provider class to host your resourcepack. Give it a directory (
   for the parent directory of the HTTP Server), and a port for the server to run at. Then
   call `HttpDaemonProvider#startServer()` to start the HTTP daemon.
   ```java
    HttpDaemonProvider hosting = new HttpDaemonProvider(directory, port);
    hosting.startServer();
   ```
5) That's it! If you want to get the url of the file, you should call the
   `HttpDaemonProvider#generateUrl(Path path)` to generate a link for the specific file. You can use this link to send
   to players or just for general hosting. A full method can be found here:
   ```java
   public String getResourcepackUrlYoutube(final String youtubeUrl, final String directory, final int port) {

        YoutubeExtraction extraction = new YoutubeExtraction(youtubeUrl, directory);
        extraction.downloadVideo();
        extraction.extractAudio();

        ResourcepackWrapper wrapper = new ResourcepackWrapper.Builder()
                .setAudio(extraction.getAudio())
                .setDescription("Title of Youtube Video: " + extraction.getVideoTitle())
                .setPath(directory)
                .setPackFormat(6)
                .createResourcepackHostingProvider(this);
        wrapper.buildResourcePack();

        HttpDaemonProvider hosting = new HttpDaemonProvider(directory, port);
        hosting.startServer();

        return hosting.generateUrl(Paths.get(directory));

    }
   ```

Note: It is **very** recommended you put all these methods in async. By default, these methods are not wrapped with
async for better user end design, so use a class such as `Thread`, `Runnable`,
`ExecutorService`, etc if you want to run it with async.

More features are coming soon. I am working on Java Docs and I hope it will be more useful as time goes on!
