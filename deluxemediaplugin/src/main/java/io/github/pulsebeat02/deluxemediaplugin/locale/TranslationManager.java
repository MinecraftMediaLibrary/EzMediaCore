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
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/locale/deluxemediaplugin_en.properties")) {
      final PropertyResourceBundle bundle = new PropertyResourceBundle(reader);
      this.registry.registerAll(DEFAULT_LOCALE, bundle, false);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    GlobalTranslator.translator().addSource(this.registry);
  }

  public @NotNull Component render(@NotNull final Component component) {
    return GlobalTranslator.render(component, DEFAULT_LOCALE);
  }
}
