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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class MkDocsPreprocessorTest {

  static final Path TEST_RESOURCES_PATH = Paths.get("src", "test");

  @BeforeAll
  static void testProcessFiles() throws IOException {
    final List<String> versions = Arrays.asList("v1", "v2", "v3");
    final Path source = MkDocsPreprocessorTest.TEST_RESOURCES_PATH.resolve("e2e");
    final Path target = Path.of("target", "e2e");
    final MkDocsPreprocessor mdp =
        new MkDocsPreprocessor(source, target, versions, true, true, WatchMode.NONE);
    mdp.processFiles();
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/e2eExpectedFiles.csv")
  void checkForExpecetFiles(final String pathString) {
    final Path p = Path.of(pathString, "");
    System.out.println(p.toAbsolutePath());
    Assertions.assertTrue(p.toFile().exists());
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/e2eFilesThatShouldNotExist.csv")
  void ensureNoRougeVersion(final String shouldNotExistString) {
    final Path p = Path.of(shouldNotExistString, "");
    System.out.println(p.toAbsolutePath());
    Assertions.assertFalse(p.toFile().exists());
  }
}
