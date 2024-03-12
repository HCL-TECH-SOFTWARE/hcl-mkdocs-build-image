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

/**
 * Enum to control the watch mode
 *
 * @author stwissel
 */
public enum WatchMode {

  /**
   * Run build first
   */
  BULID_AND_WATCH,

  /**
   * Just watch for changes
   */
  WATCH_ONLY,

  /**
   * DOn't start watch mode
   */
  NONE;

  /**
   * Get the watch mode from the incoming string
   * We only check the first character
   *
   * @param incoming String to check
   * @return WatchMode
   */
  public static WatchMode get(String incoming) {
    String incomingLower = incoming.toLowerCase();
    if (incomingLower.startsWith("w")) {
      return WATCH_ONLY;
    }
    if (incomingLower.startsWith("b")) {
      return BULID_AND_WATCH;
    }
    return NONE;
  }

}
