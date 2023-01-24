//package io.github.pulsebeat02.ezmediacore.callback.audio;
//
//import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
//import io.github.pulsebeat02.ezmediacore.callback.Viewers;
//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;
//
//public abstract sealed class AudioCallbackBuilder permits DiscordCallback.Builder, HttpCallback.Builder, NullCallback.Builder, ResourcepackCallback.Builder {
//
//  private Viewers viewers;
//
//  @Contract(value = " -> new", pure = true)
//  public static @NotNull DiscordCallback.Builder discord() {
//    return new DiscordCallback.Builder();
//  }
//
//  @Contract(value = " -> new", pure = true)
//  public static @NotNull HttpCallback.Builder http() {
//    return new HttpCallback.Builder();
//  }
//
//  @Contract(value = " -> new", pure = true)
//  public static @NotNull NullCallback.Builder nullCallback() {
//    return new NullCallback.Builder();
//  }
//
//  @Contract(value = " -> new", pure = true)
//  public static @NotNull ResourcepackCallback.Builder resourcepack() {
//    return new ResourcepackCallback.Builder();
//  }
//
//  @Contract("_ -> this")
//  public @NotNull AudioCallbackBuilder viewers(@NotNull final Viewers viewers) {
//    this.viewers = viewers;
//    return this;
//  }
//
//  public @NotNull Viewers getViewers() {
//    return this.viewers;
//  }
//
//  public abstract @NotNull AudioCallback build(@NotNull final MediaLibraryCore core);
//}
