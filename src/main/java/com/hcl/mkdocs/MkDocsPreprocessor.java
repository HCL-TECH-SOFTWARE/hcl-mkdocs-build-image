/*
 * ==========================================================================
 * Copyright (C) 2023-2024 HCL America, Inc. ( https://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.mkdocs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * MkDocsPreprocessor class to handle MkDocs file preprocessing based on the configuration.
 */
public class MkDocsPreprocessor {

  /**
   * Main function to preprocess MkDocs files based on the provided configuration.
   *
   * @param args Command-line arguments. Expects one or two arguments - the path to the config file
   *        and optional "watch" to go on watch mode
   */
  public static void main(final String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: MkDocsPreprocessor <configFilePath> [watch|buildAndWatch]");
      System.exit(1);
    }

    final WatchMode watchMode = args.length < 2
        ? WatchMode.NONE
        : WatchMode.get(args[1]);

    final String configFileName = args[0];
    final Path configFilePath = Path.of(configFileName);

    if (!configFilePath.toFile().exists()) {
      System.err.printf("Can't find file %s%n", configFilePath.toAbsolutePath());
      System.exit(2);
    }

    try {
      final MkDocsPreprocessor p = new MkDocsPreprocessor(configFilePath, watchMode);
      p.processFiles();
    } catch (final Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }


  final SiteStructure siteStructure;
  final AtomicInteger filesCopied = new AtomicInteger();
  final PreprocessorConfig config;
  final Map<WatchKey, Path> watchedDirectories = new HashMap<>();

  /**
   * @param configFile Path to the config.yml file.
   * @param watchMode Watch file system for changes
   * @throws IOException If an I/O error occurs.
   */
  public MkDocsPreprocessor(final Path configFile, final WatchMode watchMode) throws IOException {


    // Get values from the configuration YAML File
    this.config = new PreprocessorConfig(configFile, watchMode);
    this.siteStructure = new SiteStructure(this.config);
  }

  /**
   * Constructor to manually prime the preprocessor
   * 
   * @param source Path unversioned documentation source
   * @param target Path target directory
   * @param versionStrings List of version strings to generate
   * @param generateRedirects boolean should versionless redirects be generated
   * @param generateLatest boolean should directory latest be generated
   * @param watchMode what WatchMode to use
   */
  public MkDocsPreprocessor(final Path source, final Path target,
      final List<String> versionStrings, final boolean generateRedirects,
      final boolean generateLatest, final WatchMode watchMode) {
    this.config =
        new PreprocessorConfig(source, target, versionStrings, generateRedirects, generateLatest,
            watchMode);
    this.siteStructure = new SiteStructure(this.config);
  }

  /**
   * Copy a file to a destination, creating the directory structure as needed
   * 
   * @param incoming Path source file
   * @param whereTo Path target file
   */
  void copyToDestination(final Path incoming, final Path whereTo) {
    if (!incoming.toFile().isFile()) {
      // No processing of directories
      return;
    }
    final Path destination =
        PathUtilities.mapSourceTreeToTarget(this.config.source, whereTo, incoming);
    try {
      Files.createDirectories(destination.getParent());
      Files.copy(incoming, destination, StandardCopyOption.REPLACE_EXISTING);
      System.out.printf("COPY %s%n  TO %s%n%n", incoming.toAbsolutePath(),
          destination.toAbsolutePath());
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.filesCopied.incrementAndGet();
  }

  /**
   * Get all the non-md containing directories and copy them to the target
   */
  void getExtraDirs() {
    // handle extra dirs - theme_overrides

    this.config.extraDirs.forEach(dir -> {
      final Path themeover = this.config.source.resolve(dir);
      if (themeover.toFile().isDirectory()) {
        try (Stream<Path> allFiles = Files.walk(themeover)) {
          allFiles
              .filter(Files::isRegularFile)
              .forEach(p -> this.copyToDestination(p, this.config.target));
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Copies a single file to the target directories
   * after checking if processing for .md or .pages is required
   * Here happens the version expansion
   *
   * @param incoming Path File to be processed
   */
  void handleOnePath(final Path incoming) {
    // Determine if we are inside current
    final Path current = this.config.rootForMarkdownSource();
    if (incoming.startsWith(current)) {
      // Processing required
      this.handleVersions(incoming);
    } else {
      // 1:1 copy
      this.copyToDestination(incoming, this.config.target);
    }
  }

  void handleVersions(final Path incoming) {
    if (!incoming.toFile().isFile()) {
      // No processing of directories
      return;
    }
    if (incoming.getFileName().toString().endsWith(".md")) {
      // Handling of markdown
      this.siteStructure.addPage(incoming);
    } else if (incoming.getFileName().toString().endsWith(".pages")) {
      // Handling of pages files
      this.siteStructure.addMenu(incoming);
    } else {
      // 1:1 copies
      for (final DocVersion v : this.config.versions) {
        final Path whereto = this.config.rootForMarkdownTarget().resolve(v.toString());
        this.copyToDestination(incoming, whereto);
      }
    }
  }



  void makeDirectories() throws IOException {
    for (final DocVersion version : this.config.versions) {
      final Path versionDirectory = this.config.target.resolve(version.toString());
      Files.createDirectories(versionDirectory);
    }
  }

  /**
   * Process the MkDocs files based on the configuration provided
   *
   * @throws IOException If an I/O error occurs.
   */
  public int processFiles() throws IOException {

    // Check for mkdocs and docs directory
    final Path mkdocsyml = this.config.source.resolve("mkdocs.yml");
    if (!mkdocsyml.toFile().exists()) {
      System.err.printf("mkdocs.yml not found in %s%n", this.config.source.toAbsolutePath());
      return -1;
    }

    final Path docdir = this.config.source.resolve(PreprocessorConfig.DOCS_PATH);
    if (!docdir.toFile().exists() || !docdir.toFile().isDirectory()) {
      System.err.printf("%s directory not found in %s%n", PreprocessorConfig.DOCS_PATH,
          this.config.source.toAbsolutePath());
      return -1;
    }

    // Checking for WATCH mode being watch only and skip the buils step
    if (this.config.watchMode.equals(WatchMode.WATCH_ONLY)) {
      return setupWatchMode();
    }


    // Setup target structure
    // Copy mkdocs.yml configuration file
    final Path docdirTarget = this.config.target.resolve(PreprocessorConfig.DOCS_PATH);
    final Path mkdocsymlTarget = this.config.target.resolve("mkdocs.yml");
    Files.createDirectories(docdirTarget);
    Files.copy(mkdocsyml, mkdocsymlTarget, StandardCopyOption.REPLACE_EXISTING);

    // Copy extra directories like custom_theme
    this.getExtraDirs();

    // Iterate through the source directory
    try (Stream<Path> allFiles = Files.walk(docdir)) {
      allFiles
          .filter(Files::isRegularFile)
          .forEach(this::handleOnePath);
    }

    this.siteStructure.renderOutput();

    if (this.config.watchMode.equals(WatchMode.BULID_AND_WATCH)) {
      return this.setupWatchMode();
    }

    return this.filesCopied.get();
  }

  void startWatching(final WatchService watchService, final Path sourceDir) {

    try {
      Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
          WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_MODIFY);
          watchedDirectories.put(key, dir);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  int setupWatchMode() {
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      this.startWatching(watchService, this.config.source);
      this.watchLoop(watchService);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this.filesCopied.get();
  }

  void watchLoop(WatchService watchService) {

    boolean running = true;

    while (running) {
      try {
        WatchKey key = watchService.take();
        running = eventLoop(key);
        boolean valid = key.reset();
        if (!valid) {
          running = false;
        }
      } catch (InterruptedException e) {
        running = false;
      }
    }

  }

  boolean eventLoop(final WatchKey key) {
    boolean success = true;
    for (WatchEvent<?> event : key.pollEvents()) {
      success = this.handleWatchEvent(event, key);
      if (!success) {
        break;
      }
    }
    return success;
  }

  private boolean handleWatchEvent(final WatchEvent<?> event, final WatchKey key) {
    WatchEvent.Kind<?> kind = event.kind();
    if (kind == StandardWatchEventKinds.OVERFLOW) {
      System.err.println("Event Overflow happened, pls restart app");
      return false;
    }

    Object o = event.context();
    if (o instanceof Path) {
      Path filename = (Path) o;
      Path parent = watchedDirectories.get(key);
      if (parent == null) {
        System.err.printf("No parent found for %s%n", filename);
        return false;
      }
      Path fullPath = parent.resolve(filename).toAbsolutePath();

      if (kind == StandardWatchEventKinds.ENTRY_CREATE
          || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
        handleOnePath(fullPath);
      }
    } else {
      System.err.printf("Unknown event context %s%n", o.getClass().getName());
      return false;
    }
    return true;
  }



}
