/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.color;

/** A class defines the name for color role. */
final class ColorRoleNames {

  private final String accentName;
  private final String onAccentName;
  private final String accentContainerName;
  private final String onAccentContainerName;

  public ColorRoleNames(
      String accentName,
      String onAccentName,
      String accentContainerName,
      String onAccentContainerName) {
    this.accentName = accentName;
    this.onAccentName = onAccentName;
    this.accentContainerName = accentContainerName;
    this.onAccentContainerName = onAccentContainerName;
  }

  /** Returns the accent color name. */
  public String getAccentName() {
    return accentName;
  }

  /** Returns the on_accent color name. */
  public String getOnAccentName() {
    return onAccentName;
  }

  /** Returns the accent_container color name. */
  public String getAccentContainerName() {
    return accentContainerName;
  }

  /** Returns the on_accent_container color name. */
  public String getOnAccentContainerName() {
    return onAccentContainerName;
  }
}
