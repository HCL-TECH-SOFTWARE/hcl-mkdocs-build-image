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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class MenuStructure {

  final Path parent;
  final PreprocessorConfig config;
  final Map<DocVersion, Path> options = new TreeMap<>();

  Path main = null;

  public MenuStructure(final Path parent, final PreprocessorConfig config) {
    this.parent = parent;
    this.config = config;
  }

  public void addMenu(final Path incoming) {
    if (PreprocessorConfig.PAGES.equals(incoming.getFileName().toString())) {
      this.main = incoming;
    } else {
      final DocVersion key =
          DocVersion.fromString(incoming.getFileName().toString().replace(PreprocessorConfig.PAGES, ""));
      this.options.put(key, incoming);
    }
  }

  Map<String, Object> checkThatLinkedPagesExist(final Path parentDir,
      final Map<String, Object> yaml) {
    final Object o = yaml.get("nav");
    if (!(o instanceof List)) {
      return yaml;
    }
    final List<?> nav = (List<?>) o;
    final Map<String, Object> result = new HashMap<>();
    final List<Object> newNav = new ArrayList<>();
    nav.forEach(e -> this.copyIfPageExists(parentDir, e, newNav));
    if (!newNav.isEmpty()) {
      result.put("nav", newNav);
    }
    yaml.entrySet().stream()
        .filter(e -> !e.getKey().equals("nav"))
        .forEach(e -> result.put(e.getKey(), e.getValue()));
    return result;
  }

  void copyIfPageExists(final Path parentDir, final Object e,
      final List<Object> newNav) {

    if (e instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> navEntry = (Map<String, Object>) e;
      MenuStructure.copyIfPageExistsMap(parentDir, navEntry, newNav);
    } else if (e instanceof String) {
      final Path pagePath = parentDir.resolve(String.valueOf(e));
      if (pagePath.toFile().exists()) {
        newNav.add(e);
      }
    } else {
      newNav.add(e);
    }
  }

  static void copyIfPageExistsMap(final Path parentDir, final Map<String, Object> navEntry,
      final List<Object> newNav) {
    final Map<String, Object> newNavEntry = new HashMap<>();
    navEntry.entrySet().forEach(entry -> {
      if (entry.getValue() instanceof String) {
        final Path pagePath = parentDir.resolve(String.valueOf(entry.getValue()));
        if (pagePath.toFile().exists()) {
          newNavEntry.put(entry.getKey(), entry.getValue());
        } else {
          System.err.printf("Page %s not found for %s%s%n", pagePath, parentDir,
              PreprocessorConfig.PAGES);
        }
      } else {
        newNavEntry.put(entry.getKey(), entry.getValue());
      }
    });
    if (!newNavEntry.isEmpty()) {
      newNav.add(newNavEntry);
    }
  }

  Path getSource(final DocVersion v) {
    Path result = null;
    if (this.options.containsKey(v)) {
      result = this.options.get(v);
    } else {
      final Set<Entry<DocVersion, Path>> candidates = this.options.entrySet();
      for (final Entry<DocVersion, Path> candidate : candidates) {
        if (candidate.getKey().compareTo(v) < 0) {
          result = candidate.getValue();
        }
      }
    }
    if (result == null) {
      result = this.main;
    }

    return result;
  }

  Path getTarget(final Path actual, final DocVersion v) {
    final Path pagePath = actual.getParent().resolve(PreprocessorConfig.PAGES);
    final Path source = this.config.rootForMarkdownSource();
    final Path target = this.config.rootForMarkdownTarget().resolve(v.toString());
    return PathUtilities.mapSourceTreeToTarget(source, target, pagePath);
  }

  public void inspectAndSave(final Path source, final Path destination) {
    final Path parentDir = destination.getParent();
    try {
      final Map<String, Object> yaml = YamlUtilities.parseYaml(source);
      final Map<String, Object> newYaml = this.checkThatLinkedPagesExist(parentDir, yaml);
      if (!newYaml.isEmpty()) {
        YamlUtilities.saveYaml(destination, newYaml);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public void renderOutput() {
    for (final DocVersion v : this.config.versions) {
      final Path source = this.getSource(v);
      if (source != null) {
        final Path actual = this.getTarget(source, v);
        try {
          System.out.printf("Copy %s to %s%n", source, actual);
          Files.createDirectories(actual.getParent());
          // Ensuring .pages doesn't point to pages not copied in a version
          this.inspectAndSave(source, actual);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      } else {
        System.err.printf("No .pages file for %s%n", v.toString());
      }
    }
  }

}
