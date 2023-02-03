package io.github.pulsebeat02.deluxewebdisplays.locale;

import io.github.pulsebeat02.deluxewebdisplays.DeluxeWebDisplays;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

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
    try (final Reader reader = getResource("/locale/deluxewebdisplays_en.properties")) {
      return new PropertyResourceBundle(reader);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static @NotNull InputStreamReader getResource(@NotNull final String resource) {
    return new InputStreamReader(
        requireNonNull(DeluxeWebDisplays.class.getResourceAsStream(resource)));
  }

  public @NotNull Component render(@NotNull final Component component) {
    return GlobalTranslator.render(component, DEFAULT_LOCALE);
  }
}
