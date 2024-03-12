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

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DocVersionTest {
  static Stream<Arguments> providePath() {
    return Stream.of(
        Arguments.of(Path.of("some/dir/doc.v1.md"), "v1", "v1"),
        Arguments.of(Path.of("some/dir/doc.md"), "2", "v2"),
        Arguments.of(Path.of("some/dir/doc.v1.1.md"), "1.1", "v1.1"),
        Arguments.of(Path.of("some/dir/doc.v1.2.3.md"), "v3.4", "v1.2.3"),
        Arguments.of(Path.of("some/dir/doc.1.2.md"), "5.0.4", "v5.0.4"),
        Arguments.of(Path.of("some/dir/doc.1.md"), "v1.2.3", "v1.2.3"));
  }

  static Stream<Arguments> provideVersions() {
    return Stream.of(
        Arguments.of("v1", "v1"),
        Arguments.of("2", "v2"),
        Arguments.of("1.1", "v1.1"),
        Arguments.of("v3.4", "v3.4"),
        Arguments.of("5.0.5", "v5.0.5"),
        Arguments.of("v1.2.3", "v1.2.3"),
        Arguments.of("v1.2.3.4", "v1.2.3.4"),
        Arguments.of("1.2.3.5", "v1.2.3.5"));
  }

  @ParameterizedTest
  @MethodSource("providePath")
  void testFromPath(final Path source, final String fallback, final String expected) {
    final DocVersion actual = DocVersion.fromPath(source, fallback);
    Assertions.assertEquals(expected, actual.toString());
  }

  @ParameterizedTest
  @MethodSource("provideVersions")
  void testFromString(final String incoming, final String expected) {
    final DocVersion actual = DocVersion.fromString(incoming);
    Assertions.assertEquals(expected, actual.toString());
  }
}
