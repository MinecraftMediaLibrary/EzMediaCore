package io.github.pulsebeat02.ezmediacore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatTest {

  public static void main(final String[] args) {
    final Date date = new Date(10000);
    final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    System.out.println(formatter.format(date));
  }
}
