/*
 * ============================================================================
 *  Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 *  This file is part of MinecraftMediaLibrary
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *
 *  Written by Brandon Li <brandonli2006ma@gmail.com>, 2/12/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.kiulian.downloader.cipher.Cipher;
import com.github.kiulian.downloader.cipher.CipherFactory;
import com.github.kiulian.downloader.cipher.CipherFunction;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;

public class MavenDependencyTest {

  public static void main(final String[] args) {
    Logger.setVerbose(true);
    new DependencyManagement().initialize();
    new JaveDependencyInstallation().initialize();
    // to test for whether the class loaded
    new CipherFactory() {
      @Override
      public Cipher createCipher(final String s) {
        return null;
      }

      @Override
      public void addInitialFunctionPattern(final int i, final String s) {}

      @Override
      public void addFunctionEquivalent(final String s, final CipherFunction cipherFunction) {}
    };
  }
}
