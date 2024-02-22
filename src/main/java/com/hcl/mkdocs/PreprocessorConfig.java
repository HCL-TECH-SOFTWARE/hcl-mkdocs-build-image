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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds all configuration settings and constants for
 * running the Preprocessor in generate ansd watch mode
 */
public class PreprocessorConfig {


  /**
   * Directory with mkdocs sources including
   * main index.md and .pages file
   */
  public static final String DOCS_PATH = "docs";
  /**
   * Path inside the docs directory that contains source
   * versions to be copied to target v1 v2 v3
   * e.g myproject/docs/current
   */
  public static final String DOC_CURRENT_PATH = "current";
  
  public static final String PAGES = ".pages";
  public static final String INDEX = "index.md";

  /**
   * Constants for YAML names. mkDocs wants
   * them all lower_case snake_case
   */
  public static final String DOC_MAX_VERSION = "max_version";
  public static final String DOC_THIS_VERSION = "this_version";
  public static final String DOC_VERSIONS = "all_versions";
  public static final String GENERATE_REDIRECTS = "generate_redirects";

  public static final String SOURCE_PATH = "source";
  public static final String TARGET_PATH = "target";


  public final Path source;
  public final Path target;
  public final boolean watchMode;
  public final Set<DocVersion> versions = new TreeSet<>();
  /**
   * List of directories to copy on a source level
   */
  public final List<String> extraDirs = Arrays.asList("theme_overrides");
  final boolean generateRedirects;

  public PreprocessorConfig(final Path configFile, final boolean watchMode) throws IOException {
    final Map<String, Object> yamlConfig = YamlUtilities.parseYaml(configFile);
    this.source = Path.of(String.valueOf(yamlConfig.get(PreprocessorConfig.SOURCE_PATH)));
    this.target = Path.of(String.valueOf(yamlConfig.get(PreprocessorConfig.TARGET_PATH)));
    this.generateRedirects =
        Boolean.parseBoolean(String.valueOf(yamlConfig.get(PreprocessorConfig.GENERATE_REDIRECTS)));
    this.watchMode = watchMode;
    this.populateVersions(this.versions, yamlConfig, PreprocessorConfig.DOC_VERSIONS);
  }

  public PreprocessorConfig(final Path source, final Path target,
      final List<String> versionStrings, final boolean generateRedirects, final boolean watchMode) {
    this.source = source;
    this.target = target;
    this.generateRedirects = generateRedirects;
    versionStrings.forEach(s -> this.versions.add(DocVersion.fromString(s)));
    this.watchMode = watchMode;
  }

  void populateVersions(final Set<DocVersion> docSet, final Map<String, Object> sourceMap,
      final String key) {

    if (!sourceMap.containsKey(key)) {
      System.err.printf("No version array %s found in config.yml%n",
          PreprocessorConfig.DOC_VERSIONS);
      docSet.add(DocVersion.fromString("v1"));
      return;
    }

    final Object o = sourceMap.get(key);
    if (o instanceof List) {
      final List<?> list = (List<?>) o;
      list.forEach(l -> docSet.add(DocVersion.fromString(String.valueOf(l))));
    } else {
      System.err.printf("%s is not an Array in config.yml%n", PreprocessorConfig.DOC_VERSIONS);
      docSet.add(DocVersion.fromString("v1"));
    }

  }

  /**
   * Returns the root directory where transformation
   * starts e.g. myproject/src/docs/current
   *
   * @return Path
   */
  public Path rootForMarkdownSource() {
    return this.source.resolve(PreprocessorConfig.DOCS_PATH)
        .resolve(PreprocessorConfig.DOC_CURRENT_PATH);
  }

  /**
   * Returns the root directory where the
   * version directories reside e.g. myproject/target/docs
   *
   * @return Path
   */
  public Path rootForMarkdownTarget() {
    return this.target.resolve(PreprocessorConfig.DOCS_PATH);
  }


}
