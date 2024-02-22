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
import java.nio.file.Paths;

public class PathUtilities {

  public static Path mapSourceTreeToTarget(final Path sourceRoot,
      final Path targetRoot, final Path actual) {
    final String rest = actual.toString().substring(sourceRoot.toString().length() + 1);
    return targetRoot.resolve(rest);
  }

  public static Path patchPath(final Path base, final Path raw, final String add) {
    final String solve = raw.toString().substring(base.toString().length());
    return Paths.get(base.toString(), add, solve);

  }

  public static Path stripPath(final Path raw, final String goAway) {
    final String solve = raw.toString().replace("/" + goAway + "/", "/");
    return Path.of(solve);
  }

  private PathUtilities() {
    // Static only
  }
}
