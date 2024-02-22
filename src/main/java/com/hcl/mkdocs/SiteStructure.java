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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SiteStructure {

  static String getLatest(final Set<DocVersion> versions) {
    return Collections.max(versions).toString();
  }

  final PreprocessorConfig config;
  final Map<Path, PageStructure> pages = new HashMap<>();

  final Map<Path, MenuStructure> menus = new HashMap<>();

  public SiteStructure(final PreprocessorConfig config) {
    this.config = config;
  }

  public void addMenu(final Path incoming) {
    final Path parent = incoming.getParent();
    final MenuStructure ms = this.menus.containsKey(parent)
        ? this.menus.get(parent)
        : new MenuStructure(parent, this.config);
    ms.addMenu(incoming);
    this.menus.put(parent, ms);
  }

  public void addPage(final Path incoming) {
    final DocVersion version = DocVersion.fromPath(incoming);
    final Path versionFree = this.getVersionFree(incoming, version);
    final PageStructure ps = this.pages.containsKey(versionFree)
        ? this.pages.get(versionFree)
        : new PageStructure(versionFree, this.config);
    ps.addPath(version, incoming);
    this.pages.put(versionFree, ps);
  }

  Path getVersionFree(final Path incoming, final DocVersion version) {
    final String replace = String.format(".%s.", version);
    if (!incoming.toString().contains(replace)) {
      return incoming;
    }
    return Path.of(incoming.toString().replace(replace, "."));
  }

  /**
   * Outputs the [projectDir]/docs/ .pages
   * replaces current/ with lastVersion/
   */
  void renderLandingMenu() {
    // TODO: check for existence of the pages mentioned in .pages
    final Path landingSourceCandidate = this.config.source
        .resolve(PreprocessorConfig.DOCS_PATH)
        .resolve(PreprocessorConfig.PAGES);

    final Path landingTargetCandidate = this.config.target
        .resolve(PreprocessorConfig.DOCS_PATH)
        .resolve(PreprocessorConfig.PAGES);

    if (landingSourceCandidate.toFile().exists()) {
      try {
        final String landingContentSource =
            Files.readString(landingSourceCandidate, StandardCharsets.UTF_8);
        final String current = String.format("%s/", PreprocessorConfig.DOC_CURRENT_PATH);
        final String latest = String.format("%s/", SiteStructure.getLatest(this.config.versions));
        final String landingContentTarget = landingContentSource.replace(current, latest);
        Files.writeString(landingTargetCandidate, landingContentTarget, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Outputs the [projectDir]/docs/index.md
   * replaces [current/ with [lastVersion/
   */
  void renderLandingPage() {

    final Path landingSourceCandidate = this.config.source
        .resolve(PreprocessorConfig.DOCS_PATH)
        .resolve(PreprocessorConfig.INDEX);

    final Path landingTargetCandidate = this.config.target
        .resolve(PreprocessorConfig.DOCS_PATH)
        .resolve(PreprocessorConfig.INDEX);

    if (landingSourceCandidate.toFile().exists()) {
      try {
        final String landingContentSource =
            Files.readString(landingSourceCandidate, StandardCharsets.UTF_8);
        final String current = String.format("(%s/", PreprocessorConfig.DOC_CURRENT_PATH);
        final String latest = String.format("(%s/", SiteStructure.getLatest(this.config.versions));
        final String landingContentTarget = landingContentSource.replace(current, latest);
        Files.writeString(landingTargetCandidate, landingContentTarget, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void renderOutput() {
    this.pages.values().forEach(PageStructure::renderOutput);
    // Menu stucture (.pages files) needs to render after pages
    // to eliminate non exisiting files in version
    this.menus.values().forEach(MenuStructure::renderOutput);
    // Finally the landing page index.md and if used landing .pages
    this.renderLandingPage();
    this.renderLandingMenu();
  }


}
