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
import java.util.Map;

/**
 * Stores information about validity of
 * a page with first and last version
 * and it's source path
 */
public class PageVariation {

  public final Path source;
  public final String title;
  public final DocVersion minVersion;
  public final DocVersion maxVersion;


  public PageVariation(final Path source) {
    this.source = source;
    this.minVersion = DocVersion.fromPath(source);
    Map<String, Object> yaml = null;
    try {
      yaml = YamlUtilities.parseYamlFromMarkdown(source);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.maxVersion = this.getMaxVersion(yaml);
    this.title = this.getTitle(yaml);
  }

  /**
   * @param yamlPath -path to source file
   * @return last version this path is valid if not later file available
   */
  DocVersion getMaxVersion(final Map<String, Object> yaml) {
    if (yaml != null && yaml.containsKey(PreprocessorConfig.DOC_MAX_VERSION)) {
      final String candidate = String.valueOf(yaml.get(PreprocessorConfig.DOC_MAX_VERSION));
      return DocVersion.fromString(candidate);
    }
    return DocVersion.last();
  }

  private String getTitle(final Map<String, Object> yaml) {
    if (yaml != null && yaml.containsKey("title")) {
      return String.valueOf(yaml.get("title"));
    }
    return null;
  }
}
