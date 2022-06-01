package io.github.pulsebeat02.ezmediacore.utility.future;

import java.util.function.BiFunction;

public final class Throwing {

  public static final BiFunction<Object, Throwable, Object> THROWING_FUTURE;

  static {
    THROWING_FUTURE = Throwing::handleThrowingResult;
  }

  private Throwing() {}

  private static Object handleThrowingResult(final Object result, final Throwable exception) {
    if (exception == null) {
      return result;
    }
    exception.printStackTrace();
    return result;
  }
}
