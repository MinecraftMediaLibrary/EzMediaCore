package io.github.pulsebeat02.epicmedialib.image;

public interface GifImage extends MapImage {

  void stopDrawing();

  void onStopDrawing();

  int getCurrentFrame();

  int getFrameCount();
}
