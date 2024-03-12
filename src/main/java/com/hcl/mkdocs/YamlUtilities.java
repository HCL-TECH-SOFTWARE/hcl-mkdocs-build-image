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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility class to parse YAML configuration.
 */
public class YamlUtilities {

  static final String YAML_DELIMITER = "---";

  /**
   * Generate YAML string from a map of values.
   *
   * @param values Map containing values for YAML.
   * @return YAML string.
   */
  public static String generateYamlString(final Map<String, Object> values) {
    final DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    final Yaml yaml = new Yaml(options);
    return yaml.dump(values);
  }

  static boolean handleFirstLine(final PrintWriter pw, final String line) {
    if (line.startsWith(YamlUtilities.YAML_DELIMITER)) {
      return true;
    }
    pw.println(line);
    return false;
  }

  /**
   * Parse a YAML file.
   *
   * @param yamlFile Path to the config.yml file.
   * @return Map representing the parsed configuration.
   * @throws IOException If an I/O error occurs.
   */
  public static Map<String, Object> parseYaml(final Path yamlFile) throws IOException {
    final String yamlContent = Files.readString(yamlFile);
    return YamlUtilities.parseYamlFromString(yamlContent);
  }

  /**
   * @param markdownFile
   * @return Map with YAML Values
   * @throws IOException
   */
  public static Map<String, Object> parseYamlFromMarkdown(final Path markdownFile)
      throws IOException {
    try (FileInputStream in = new FileInputStream(markdownFile.toFile());
        Scanner scanner = new Scanner(in)) {
      String localSeparator = YamlUtilities.YAML_DELIMITER;
      boolean start = true;
      boolean done = false;
      final StringBuilder b = new StringBuilder();
      while (scanner.hasNextLine() && !done) {
        final String line = scanner.nextLine();
        if (start) {
          if (line.startsWith(YamlUtilities.YAML_DELIMITER)) {
            localSeparator = line;
          } else {
            // No front-matter available
            return new HashMap<>();
          }
          start = false;
        } else {
          if (line.equals(localSeparator)) {
            done = true;
          } else {
            b.append(line);
            b.append(System.lineSeparator());
          }
        }
      }
      return YamlUtilities.parseYamlFromString(b.toString());
    }
  }

  /**
   * Parse YAML front-matter content.
   *
   * @param yamlContent YAML front-matter content.
   * @return Map representing the parsed front-matter.
   */
  public static Map<String, Object> parseYamlFromString(final String yamlContent) {
    final Yaml yaml = new Yaml();
    return yaml.load(yamlContent);
  }

  public static void patchYamlWithVersions(final Set<DocVersion> versions, final DocVersion version,
      final Map<String, Object> yaml) {
    final List<String> strVersions = versions.stream().map(DocVersion::toString).toList();
    yaml.put(PreprocessorConfig.DOC_THIS_VERSION, version.toString());
    yaml.put(PreprocessorConfig.DOC_VERSIONS, strVersions);
    boolean isLatest = version.toString().equals(strVersions.get(strVersions.size() - 1));
    yaml.put(PreprocessorConfig.DOC_ISLATEST, isLatest);
  }

  public static void replaceFrontMatter(final Path markdownFile, final Map<String, Object> newYaml,
      final PrintWriter pw) throws IOException {
    try (FileInputStream in = new FileInputStream(markdownFile.toFile());
        Scanner scanner = new Scanner(in)) {

      boolean start = true;
      boolean insideYaml = false;
      if (!newYaml.isEmpty()) {
        final String yamlReplacement = YamlUtilities.generateYamlString(newYaml);
        pw.println(YamlUtilities.YAML_DELIMITER);
        pw.println(yamlReplacement);
        pw.println(YamlUtilities.YAML_DELIMITER);
      }
      // Skip over old front - matter, output the rest
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (start) {
          insideYaml = YamlUtilities.handleFirstLine(pw, line);
          start = false;
        } else {
          if (insideYaml) {
            if (line.startsWith(YamlUtilities.YAML_DELIMITER)) {
              insideYaml = false;
            }
          } else {
            pw.println(line);
          }
        }
      }
    }
  }

  public static void saveYaml(final Path destination, final Map<String, Object> yaml) {
    final String toWrite = YamlUtilities.generateYamlString(yaml);
    try {
      Files.writeString(destination, toWrite, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private YamlUtilities() {
    // Only static methods here
  }
}
