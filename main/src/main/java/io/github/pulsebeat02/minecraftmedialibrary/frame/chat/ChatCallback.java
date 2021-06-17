/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.chat;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.Callback;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * The callback used to update chat for each frame when necessary.
 *
 * <p>It sends the necessary chat to the players to create a video.
 */
public final class ChatCallback extends Callback implements ChatCallbackPrototype {

  private final UUID[] viewers;
  private final Set<Player> players;
  private final String character;

  /**
   * Instantiates a new Item frame callback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param character the character to use
   * @param chatWidth the chat width
   * @param chatHeight the chat height
   * @param delay the delay
   */
  public ChatCallback(
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      @Nullable final String character,
      final int chatWidth,
      final int chatHeight,
      final int delay) {
    super(library, chatWidth, chatHeight, chatWidth, delay);
    this.viewers = viewers;
    players = Collections.newSetFromMap(new WeakHashMap<>());
    players.addAll(Arrays.stream(viewers).map(Bukkit::getPlayer).collect(Collectors.toList()));
    this.character = character == null ? "\u2588" : character;
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sends the necessary data into the chat.
   *
   * @param data to send
   */
  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - getLastUpdated() >= getDelay()) {
      setLastUpdated(time);
      final int height = getHeight();
      final int width = getWidth();
      for (int y = 0; y < height; ++y) {
        int before = -1;
        final StringBuilder msg = new StringBuilder();
        for (int x = 0; x < width; ++x) {
          final int rgb = data[width * y + x];
          if (before != rgb) {
            msg.append(ChatColor.of("#" + String.format("%08x", rgb).substring(2)));
          }
          msg.append(character);
          before = rgb;
        }
        for (final Player player : players) {
          player.sendMessage(msg.toString());
        }
      }
    }
  }

  @Override
  public UUID[] getViewers() {
    return viewers;
  }

  @Override
  public String getCharacter() {
    return character;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private String character;
    private int width;
    private int height;
    private int delay;

    private Builder() {}

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder viewers(final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets chat width.
     *
     * @param width the width
     * @return the width
     */
    public Builder chatWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets chat height.
     *
     * @param height the height
     * @return the height
     */
    public Builder chatHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder delay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Sets character.
     *
     * @param character the character
     * @return the character
     */
    public Builder character(final String character) {
      this.character = character;
      return this;
    }

    /**
     * Create item frame callback item frame callback.
     *
     * @param library the library
     * @return the item frame callback
     */
    public ChatCallbackPrototype build(final MediaLibrary library) {
      return new ChatCallback(library, viewers, character, width, height, delay);
    }
  }
}
