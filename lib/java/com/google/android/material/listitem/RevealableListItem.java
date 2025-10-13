/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.listitem;

import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/** 
 * Interface for the part of a ListItem that is able to be revealed when swiped.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public interface RevealableListItem {

  /**
   * Sets the revealed width of RevealableListItem, in pixels.
   */
  void setRevealedWidth(@Px int revealedWidth);

  /**
   * Returns the intrinsic width of the RevealableListItem. This may be 0 if an intrinsic width
   * has not yet been measured.
   */
  @Px int getIntrinsicWidth();
}
