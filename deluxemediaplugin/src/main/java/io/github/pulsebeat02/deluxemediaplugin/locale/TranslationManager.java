/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.deluxemediaplugin.locale;

import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class TranslationManager {

  private static final java.util.Locale DEFAULT_LOCALE;

  static {
    DEFAULT_LOCALE = java.util.Locale.ENGLISH;
  }

  private final TranslationRegistry registry;

  public TranslationManager() {
    this.registry = TranslationRegistry.create(Key.key("deluxemediaplugin", "main"));
    this.registry.defaultLocale(DEFAULT_LOCALE);
    this.registerTranslations();
  }

  private void registerTranslations() {
    this.registerLocale();
    this.addGlobalRegistry();
  }

  private void addGlobalRegistry() {
    GlobalTranslator.translator().addSource(this.registry);
  }

  private void registerLocale() {
    final ResourceBundle bundle = this.getBundle();
    this.registry.registerAll(DEFAULT_LOCALE, bundle, false);
  }

  private @NotNull PropertyResourceBundle getBundle() {
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/locale/deluxemediaplugin_en.properties")) {
      return new PropertyResourceBundle(reader);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public @NotNull Component render(@NotNull final Component component) {
    return GlobalTranslator.render(component, DEFAULT_LOCALE);
  }
}
