/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/26/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency;

import com.github.kiulian.downloader.cipher.Cipher;
import com.github.kiulian.downloader.cipher.CipherFactory;
import com.github.kiulian.downloader.cipher.CipherFunction;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;

public class RepositoryDependencyTest {

  public static void main(final String[] args) {
    Logger.setVerbose(true);
    final JaveDependencyInstallation jave = new JaveDependencyInstallation("");
    jave.install();
    jave.load();
    final DependencyManagement management = new DependencyManagement("");
    management.install();
    management.relocate();
    management.load();
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
