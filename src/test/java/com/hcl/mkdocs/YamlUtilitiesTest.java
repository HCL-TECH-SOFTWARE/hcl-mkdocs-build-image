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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class YamlUtilitiesTest {

  static final Path TEST_RESOURCES_PATH = Paths.get("src", "test", "resources");

  String sampleYamlString() {
    final StringBuilder b = new StringBuilder();
    b.append("color: Red");
    b.append(System.lineSeparator());
    b.append("versions:");
    b.append(System.lineSeparator());
    b.append("- v1");
    b.append(System.lineSeparator());
    b.append("- v2");
    b.append(System.lineSeparator());
    return b.toString();
  }

  @Test
  void testGenerateYamlString() {
    final Map<String, Object> input = new HashMap<>();
    input.put("color", "Red");
    input.put("versions", Arrays.asList("v1", "v2"));


    final String expected = this.sampleYamlString();
    final String actual = YamlUtilities.generateYamlString(input);
    Assertions.assertEquals(expected, actual);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testParseYaml() throws IOException {
    final Path yamlFile = YamlUtilitiesTest.TEST_RESOURCES_PATH.resolve("config.yml");
    final Map<String, Object> result = YamlUtilities.parseYaml(yamlFile);

    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals("docs", result.get(PreprocessorConfig.SOURCE_PATH));
    Assertions.assertEquals("out", result.get(PreprocessorConfig.TARGET_PATH));

    final List<String> versions = (List<String>) result.get(PreprocessorConfig.DOC_VERSIONS);
    Assertions.assertEquals(3, versions.size());
    Assertions.assertTrue(versions.contains("v1"));
    Assertions.assertTrue(versions.contains("v2"));
    Assertions.assertTrue(versions.contains("v3"));
    Assertions.assertFalse(versions.contains("v4"));
  }

  @Test
  void testParseYamlFromEmptyMarkdown() throws IOException {
    final Path markdownFile =
        YamlUtilitiesTest.TEST_RESOURCES_PATH.resolve("testMarkdownFileEmpty.md");
    final Map<String, Object> result = YamlUtilities.parseYamlFromMarkdown(markdownFile);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testParseYamlFromMarkdown() throws IOException {
    final Path markdownFile = YamlUtilitiesTest.TEST_RESOURCES_PATH.resolve("testMarkdownFile.md");
    final Map<String, Object> result = YamlUtilities.parseYamlFromMarkdown(markdownFile);

    Assertions.assertEquals("Red", result.get("color"));
    final String actualType = result.get("versions").getClass().getName();
    Assertions.assertEquals("java.util.ArrayList", actualType);
  }


  @Test
  void testParseYamlFromString() {
    final String sample = this.sampleYamlString();
    final Map<String, Object> result = YamlUtilities.parseYamlFromString(sample);
    Assertions.assertEquals("Red", result.get("color"));
    final String actualType = result.get("versions").getClass().getName();
    Assertions.assertEquals("java.util.ArrayList", actualType);
  }

  @Test
  void testReplaceFrontMatter() throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintWriter pw = new PrintWriter(out);
    final Path markdownFile = YamlUtilitiesTest.TEST_RESOURCES_PATH.resolve("testMarkdownFile.md");
    final Path resultFile = YamlUtilitiesTest.TEST_RESOURCES_PATH.resolve("replacedResult.txt");
    final Map<String, Object> newYaml = YamlUtilities.parseYamlFromString(this.sampleYamlString());

    final String expected = Files.readString(resultFile);

    YamlUtilities.replaceFrontMatter(markdownFile, newYaml, pw);
    pw.flush();
    final String actual = out.toString();
    Assertions.assertEquals(expected, actual);
  }

}
