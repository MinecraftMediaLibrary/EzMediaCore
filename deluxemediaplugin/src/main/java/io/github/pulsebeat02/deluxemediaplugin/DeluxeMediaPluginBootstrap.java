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
package io.github.pulsebeat02.deluxemediaplugin;

import io.github.pulsebeat02.deluxemediaplugin.message.Sender;
import io.github.slimjar.app.builder.ApplicationBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeluxeMediaPluginBootstrap extends JavaPlugin {

  private DeluxeMediaPlugin plugin;

  @Override
  public void onLoad() {
    this.buildApplication();
    this.plugin = new DeluxeMediaPlugin(this);
    this.plugin.load();
  }

  private void buildApplication() {
    final Logger logger = this.getLogger();
    logger.info(InternalLocale.SLIMJAR_LOAD.build());
    try {
      ApplicationBuilder.appending("DeluxeMediaPlugin").build();
    } catch (final IOException
        | NoSuchAlgorithmException
        | URISyntaxException
        | ReflectiveOperationException e) {
      e.printStackTrace();
    }
    logger.info(InternalLocale.SLIMJAR_FINISH.build());
  }

  @Override
  public void onEnable() {
    this.plugin.enable();
  }

  @Override
  public void onDisable() {
    this.plugin.disable();
  }

  interface InternalLocale {

    NullComponent<Sender> SLIMJAR_LOAD =
        () -> "Loading DeluxeMediaPlugin dependencies... this may take a minute!";
    NullComponent<Sender> SLIMJAR_FINISH = () -> "Finished loading DeluxeMediaPlugin dependencies!";

    @FunctionalInterface
    interface NullComponent<S extends Sender> {

      String build();
    }
  }
}
