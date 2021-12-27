package io.github.pulsebeat02.ezmediacore.utility.future;

import java.util.function.BiFunction;

public final class Throwing {

  public static final BiFunction<Object, Throwable, Object> THROWING_FUTURE;

  static {
    THROWING_FUTURE =
        (result, exception) -> {
          if (exception == null) {
            return result;
          }
          exception.printStackTrace();
          return result;
        };
  }

  private Throwing() {}
}
