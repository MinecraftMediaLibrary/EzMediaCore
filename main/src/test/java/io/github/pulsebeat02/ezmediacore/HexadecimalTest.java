package io.github.pulsebeat02.ezmediacore;

import java.awt.Color;

public class HexadecimalTest {

  public static void main(final String[] args) {
    final int num = Color.RED.getRGB();
    long current = System.currentTimeMillis();
    System.out.println(current);
    System.out.println("%08x".formatted(num).substring(2));
    System.out.println(System.currentTimeMillis() - current);
    System.out.println();
    current = System.currentTimeMillis();
    System.out.println(Integer.toHexString(num).substring(2));
    System.out.println(System.currentTimeMillis() - current);
  }
}
