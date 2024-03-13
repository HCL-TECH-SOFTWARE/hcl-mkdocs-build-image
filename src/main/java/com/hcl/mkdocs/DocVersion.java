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

public class DocVersion implements Comparable<DocVersion> {

  public static final String PREFIX = "v";
  public static final String SEPARATOR = ".";
  public static final String SPLITTER = "\\.";

  /**
   * Convenience function to get v1
   *
   * @return DocVersion v1
   */
  public static DocVersion first() {
    return new DocVersion(1);
  }

  public static DocVersion fromPath(final Path path) {
    return DocVersion.fromPath(path, DocVersion.first().toString());
  }

  public static DocVersion fromPath(final Path path, final String fallback) {
    final String fileName = path.getFileName().toString();
    if (!fileName.contains(".v")) {
      return DocVersion.fromString(fallback);
    }
    final String fileNoextension = fileName.substring(0, fileName.lastIndexOf("."));
    final String[] split = fileNoextension.split(DocVersion.SPLITTER + DocVersion.PREFIX);
    final String candidate = split.length > 1 ? split[1] : fallback;
    return DocVersion.fromString(candidate);
  }

  public static DocVersion fromString(final String incoming) {
    final String[] actual =
        (incoming.startsWith(DocVersion.PREFIX) ? incoming.substring(1) : incoming)
            .split(DocVersion.SPLITTER);
    final int major = Integer.parseInt(actual[0]);
    final int minor = actual.length > 1 ? Integer.parseInt(actual[1]) : -1;
    final int patch = actual.length > 2 ? Integer.parseInt(actual[2]) : -1;
    final int subpatch = actual.length > 3 ? Integer.parseInt(actual[3]) : -1;
    return new DocVersion(major, minor, patch, subpatch);
  }

  /**
   * Convenience function to get vLast
   *
   * @return DocVersion last
   */
  public static DocVersion last() {
    return new DocVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
        Integer.MAX_VALUE);
  }

  public final int major;
  public final int minor;
  public final int patch;
  public final int subpatch;

  public DocVersion(final int major) {
    this.major = major;
    this.minor = -1;
    this.patch = -1;
    this.subpatch = -1;
  }

  public DocVersion(final int major, final int minor) {
    this.major = major;
    this.minor = minor;
    this.patch = -1;
    this.subpatch = -1;
  }

  public DocVersion(final int major, final int minor, final int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.subpatch = -1;
  }

  public DocVersion(final int major, final int minor, final int patch, final int subpatch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.subpatch = subpatch;
  }

  /**
   * Compare two DocVersions, comparison looks at each part of the
   * version number in order of major, minor, patch, subpatch.
   */
  @Override
  public int compareTo(final DocVersion incoming) {
    if (this.major == incoming.major && this.minor == incoming.minor
        && this.patch == incoming.patch) {
      return Integer.compare(this.subpatch, incoming.subpatch);
    }

    if (this.major == incoming.major && this.minor == incoming.minor) {
      return Integer.compare(this.patch, incoming.patch);
    }

    if (this.major == incoming.major) {
      return Integer.compare(this.minor, incoming.minor);
    }

    return Integer.compare(this.major, incoming.major);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof DocVersion) {
      return this.toString().equals(obj.toString());
    }
    return false;
  }

  @Override
  public int hashCode() {

    return this.major * 1000000 + this.minor * 10000 + this.patch * 100 + this.subpatch;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(DocVersion.PREFIX);
    builder.append(this.major);
    if (this.minor >= 0) {
      builder.append(DocVersion.SEPARATOR);
      builder.append(this.minor);
    }

    if (this.patch >= 0) {
      builder.append(DocVersion.SEPARATOR);
      builder.append(this.patch);
    }

    if (this.subpatch >= 0) {
      builder.append(DocVersion.SEPARATOR);
      builder.append(this.subpatch);
    }

    return builder.toString();
  }

}
