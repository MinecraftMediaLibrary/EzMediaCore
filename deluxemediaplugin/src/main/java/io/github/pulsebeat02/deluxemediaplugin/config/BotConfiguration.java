/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.mutable.MutableBoolean;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import java.io.IOException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BotConfiguration extends ConfigurationProvider<MediaBot> {

  public BotConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/bot.yml");
  }

  @Override
  public void deserialize() throws IOException {
    this.saveConfig();
  }

  private @Nullable String checkConfigurationValue(
      @NotNull final String key,
      @NotNull final Component message,
      @NotNull final MutableBoolean mutableBoolean) {
    final String value = this.getFileConfiguration().getString(key);
    Nill.ifSo(value, () -> this.handleNull(message, mutableBoolean));
    return value;
  }

  private void handleNull(
      @NotNull final Component message, @NotNull final MutableBoolean mutableBoolean) {
    this.getPlugin().getConsoleAudience().sendMessage(message);
    mutableBoolean.set(true);
  }

  @Override
  public @Nullable MediaBot serialize() {
    final MutableBoolean invalid = MutableBoolean.ofFalse();
    final String token =
        this.checkConfigurationValue("token", Locale.ERR_BOT_TOKEN.build(), invalid);
    final String guild =
        this.checkConfigurationValue("guild-id", Locale.ERR_GUILD_TOKEN.build(), invalid);
    final String vc =
        this.checkConfigurationValue("voice-chat-id", Locale.ERR_VC_ID.build(), invalid);
    if (!invalid.getBoolean()) {
      return this.constructBot(token, guild, vc);
    }
    return null;
  }

  private @NotNull MediaBot constructBot(
      @NotNull final String token, @NotNull final String guild, @NotNull final String vc) {
    try {
      return new MediaBot(token, guild, vc);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }
}
