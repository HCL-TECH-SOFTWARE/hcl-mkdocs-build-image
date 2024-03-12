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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class PageStructure {

  final Path origin;
  final Path destinationFileName;
  final PreprocessorConfig config;
  final Map<DocVersion, PageVariation> options = new TreeMap<>();


  public PageStructure(final Path origin, final PreprocessorConfig config) {
    this.origin = origin;
    this.config = config;
    this.destinationFileName = this.origin.getFileName();
  }

  public void addPath(final DocVersion version, final Path incoming) {
    this.options.put(version, new PageVariation(incoming));
  }

  void copyMarkdown(final Set<DocVersion> versions, final DocVersion version, final Path source,
      final Path target) {
    System.out.printf("%s%n from: %s%n   to: %s%n%n", version, source, target);
    try {
      Files.createDirectories(target.getParent());
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }
    try (FileOutputStream out = new FileOutputStream(target.toFile());
        PrintWriter pw = new PrintWriter(out)) {
      final Map<String, Object> yaml = YamlUtilities.parseYamlFromMarkdown(source);
      YamlUtilities.patchYamlWithVersions(versions, version, yaml);
      YamlUtilities.replaceFrontMatter(source, yaml, pw);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  String getMdRedirect(final Path actual) {
    final Path rawPath = Path.of(actual.toString()
        .substring(this.config.rootForMarkdownTarget().toString().length() + 1));
    final String prefix = "../".repeat(rawPath.getNameCount() - 2);
    return Paths.get(prefix, rawPath.toString()).toString();
  }

  void outputRedirect(final Path actual, final DocVersion v, final String title) {
    if (actual == null || v == null) {
      return;
    }
    final Path redirectFrom = PathUtilities.stripPath(actual, v.toString());

    try {
      Files.createDirectories(redirectFrom.getParent());
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }

    final String mdRedirect = this.getMdRedirect(actual);
    final String headerRedirect = mdRedirect.endsWith(".md")
        ? mdRedirect.replace("index.md", "")
        : mdRedirect.replace(".md", ".html");

    final File file = redirectFrom.toFile();
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.println("---");
      pw.println("template: versionredirect.html");
      pw.print("redirect: ");
      pw.println(headerRedirect);
      pw.println("search:");
      pw.println("  exclude: true");
      pw.println("---");
      pw.print("# ");
      pw.println(title);
      pw.println("");
      pw.println(String.format("[Latest version of %s](%s)", title, mdRedirect));
      pw.println("Click on the link if page doesn't load");
      pw.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  Map<DocVersion, PageVariation> preparePageList(final Set<DocVersion> versions,
      final Map<DocVersion, PageVariation> pages) {

    final Map<DocVersion, PageVariation> result = new TreeMap<>();

    // Part 1: Check if we have direct hits
    for (final PageVariation p : pages.values()) {
      if (versions.contains(p.minVersion)) {
        result.put(p.minVersion, p);
      } else {
        System.out.printf("Warning: %s found but not requested for rendering%n%s%n%n",
            p.minVersion, p.source);
      }
    }

    // Part 2: check if there is a suitable version
    for (final DocVersion v : versions) {
      if (!result.containsKey(v)) {
        // Need closest match, a little brute force here
        for (final PageVariation p : pages.values()) {
          if (p.minVersion.compareTo(v) <= 0 &&
              p.maxVersion.compareTo(v) >= 0) {
            result.put(v, p);
          }
        }
      }
    }

    return result;
  }

  public void renderOutput() {

    final Map<DocVersion, PageVariation> pages =
        this.preparePageList(this.config.versions, this.options);

    Path actual = null;
    Path from = null;
    DocVersion v = null;
    String title = this.destinationFileName.toString();

    for (final Entry<DocVersion, PageVariation> entry : pages.entrySet()) {
      final PageVariation p = entry.getValue();
      v = entry.getKey();
      title = p.title != null ? p.title : this.destinationFileName.toString();
      from = p.source;
      final Path toCandidate =
          PathUtilities.mapSourceTreeToTarget(this.config.rootForMarkdownSource(),
              this.config.rootForMarkdownTarget().resolve(v.toString()), from);
      actual = toCandidate.getParent().resolve(this.destinationFileName);
      this.copyMarkdown(pages.keySet(), v, from, actual);

    }

    if (this.config.generateLatest && actual != null && from != null) {
      Path latest = PathUtilities
          .mapSourceTreeToTarget(this.config.rootForMarkdownSource(),
              this.config.rootForMarkdownTarget().resolve("latest"), from)
          .getParent()
          .resolve(this.destinationFileName);
      this.copyMarkdown(pages.keySet(), v, from, latest);
    }

    if (this.config.generateRedirects) {
      this.outputRedirect(actual, v, title);
    }
  }

}
