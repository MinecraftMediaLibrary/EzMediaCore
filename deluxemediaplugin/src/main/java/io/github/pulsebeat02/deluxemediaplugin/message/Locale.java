package io.github.pulsebeat02.deluxemediaplugin.message;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface Locale {

  Component NEW_LINE = Component.newline();
  Component SPACE = Component.space();

  UniComponent<Sender, String> REQUIRED_ARGUMENT =
      argument ->
          text()
              .color(DARK_GRAY)
              .append(text("<"))
              .append(text(argument, GRAY))
              .append(text(">"))
              .build();

  UniComponent<Sender, String> OPTIONAL_ARGUMENT =
      argument ->
          text()
              .color(DARK_GRAY)
              .append(text("["))
              .append(text(argument, GRAY))
              .append(text("]"))
              .build();

  NullComponent<Sender>

      ENABLE_PLUGIN = () -> join(separator(text(" ")),
          text("Running DeluxeMediaPlugin", AQUA),
          text("[BETA]", GOLD),
          text("1.0.0", AQUA)),
      EMC_INIT = () -> text("Loading EzMediaCore instance... this may take some time!"),
      WELCOME = () -> text("""
        Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For identifier purposes, this
         is your purchase identification code: %%__NONCE__%% - Enjoy using the plugin, and ask for
         support at my Discord! (https://discord.gg/MgqRKvycMC)
        """),

      DISABLE_PLUGIN = () -> text("DeluxeMediaPlugin is shutting down!"),
      GOOD_EMC_SHUTDOWN = () -> text("Successfully shutdown MinecraftMediaLibrary instance!"),
      GOODBYE = () -> text("Good Bye! :("),

      FIN_EMC_INIT = () -> text("Finished loading MinecraftMediaLibrary instance!"),
      FIN_PERSISTENT_INIT = () -> text("Finished loading persistent data!"),
      FIN_COMMANDS_INIT = () -> text("Finished registering plugin commands!"),
      FIN_METRICS_INIT = () -> text("Finished loading Metrics data!"),
      FIN_PLUGIN_INIT = () -> text("Finished loading DeluxeMediaPlugin!"),

      ERR_EMC_INIT = () -> text("There was a severe issue while loading the EzMediaCore instance!", RED),
      ERR_PERSISTENT_INIT = () -> text("A severe issue occurred while reading data from configuration files!", RED),
      ERR_EMC_SHUTDOWN = () -> text("EzMediaCore instance is null... something fishy is going on.", RED)




  ;

  @FunctionalInterface
  interface NullComponent<S extends Sender> {
    Component build();

    default void send(@NotNull final S sender) {
      sender.sendMessage(format(this.build()));
    }
  }

  @FunctionalInterface
  interface UniComponent<S extends Sender, A0> {
    Component build(A0 arg0);

    default void send(@NotNull final S sender, final A0 arg0) {
      sender.sendMessage(format(this.build(arg0)));
    }
  }

  @FunctionalInterface
  interface BiComponent<S extends Sender, A0, A1> {
    Component build(A0 arg0, A1 arg1);

    default void send(@NotNull final S sender, @NotNull final A0 arg0, @NotNull final A1 arg1) {
      sender.sendMessage(format(this.build(arg0, arg1)));
    }
  }

  @FunctionalInterface
  interface TriComponent<S extends Sender, A0, A1, A2> {
    Component build(A0 arg0, A1 arg1, A2 arg2);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2)));
    }
  }

  @FunctionalInterface
  interface QuadComponent<S extends Sender, A0, A1, A2, A3> {
    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3)));
    }
  }

  @FunctionalInterface
  interface PentaComponent<S extends Sender, A0, A1, A2, A3, A4> {
    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4)));
    }
  }

  @FunctionalInterface
  interface HexaComponent<S extends Sender, A0, A1, A2, A3, A4, A5> {
    Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

    default void send(
        @NotNull final S sender,
        @NotNull final A0 arg0,
        @NotNull final A1 arg1,
        @NotNull final A2 arg2,
        @NotNull final A3 arg3,
        @NotNull final A4 arg4,
        @NotNull final A5 arg5) {
      sender.sendMessage(format(this.build(arg0, arg1, arg2, arg3, arg4, arg5)));
    }
  }
}
