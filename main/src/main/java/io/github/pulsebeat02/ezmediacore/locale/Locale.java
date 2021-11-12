package io.github.pulsebeat02.ezmediacore.locale;

import io.github.pulsebeat02.ezmediacore.dependency.DependencyInfo;
import java.nio.file.Path;

public interface Locale {

  NullComponent ERR_EXCEPTION_CMD = () -> "An exception occurred while executing the command!";
  BiComponent<String, Path> BINARY_PATHS = "%s path: %s"::formatted;
  BiComponent<String, DependencyInfo> DEP_CHECKS = "Checking %s Central for dependency %s"::formatted;
  TriComponent<String, Integer, Path> HTTP_INFO =
      """
      ========================================
                     HTTP Server
      ========================================
      IP: %s
      PORT: %s
      PATH: %s
      """::formatted;
  TriComponent<String, String, Boolean> SERVER_INFO =
      """
      ===========================================
                   SERVER INFORMATION
      ===========================================
      NAME: %s
      VERSION: %s
      ONLINE MODE: %s
      """::formatted;
  QuadComponent<String, String, String, String> SYSTEM_INFO =
      """
      ===========================================
                   SYSTEM INFORMATION
      ===========================================
      OS: %s
      VERSION: %s
      DISTRO: %s
      CPU: %s
      """::formatted;
  HeptaComponent<String, String, Boolean, Path, Path, Path, Path> PLUGIN_INFO =
      """
      ===========================================
                   PLUGIN INFORMATION
      ===========================================
      NAME: %s
      DESCRIPTION: %s
      LIB DISABLED: %s
      LIB PATH: %s
      VLC PATH: %s
      IMAGE PATH: %s
      AUDIO PATH: %s
      """::formatted;

  @FunctionalInterface
  interface NullComponent {

    String build();
  }

  @FunctionalInterface
  interface UniComponent<A0> {
    String build(A0 arg0);
  }

  @FunctionalInterface
  interface BiComponent<A0, A1> {

    String build(A0 arg0, A1 arg1);
  }

  @FunctionalInterface
  interface TriComponent<A0, A1, A2> {

    String build(A0 arg0, A1 arg1, A2 arg2);
  }

  @FunctionalInterface
  interface QuadComponent<A0, A1, A2, A3> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);
  }

  @FunctionalInterface
  interface PentaComponent<A0, A1, A2, A3, A4> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
  }

  @FunctionalInterface
  interface HexaComponent<A0, A1, A2, A3, A4, A5> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
  }

  @FunctionalInterface
  interface HeptaComponent<A0, A1, A2, A3, A4, A5, A6> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);
  }
}
