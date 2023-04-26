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

package com.google.android.material.sidesheet;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import com.google.android.material.motion.MaterialBackHandler;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for sheet constants and {@code IntDefs} to be shared between the different {@link
 * Sheet} implementations.
 */
interface Sheet<C extends SheetCallback> extends MaterialBackHandler {
  /** The sheet is dragging. */
  int STATE_DRAGGING = 1;

  /** The sheet is settling. */
  int STATE_SETTLING = 2;

  /** The sheet is expanded. */
  int STATE_EXPANDED = 3;

  /** The sheet is hidden. */
  int STATE_HIDDEN = 5;

  /**
   * States that a sheet can be in.
   *
   * <p>Note: {@link Sheet} implementations are not guaranteed to support all states.
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    STATE_EXPANDED,
    STATE_DRAGGING,
    STATE_SETTLING,
    STATE_HIDDEN,
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface SheetState {}

  /**
   * Stable states that can be set by the a sheet's {@code setState(int)} method. These includes all
   * the possible states a sheet can be in when it's settled.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_EXPANDED, STATE_HIDDEN})
  @Retention(RetentionPolicy.SOURCE)
  @interface StableSheetState {}

  /**
   * The sheet is based on the right edge of the screen; it slides from the right edge towards the
   * left.
   */
  int EDGE_RIGHT = 0;
  /**
   * The sheet is based on the left edge of the screen; it slides from the left edge towards the
   * right.
   */
  int EDGE_LEFT = 1;

  /**
   * The edge of the screen that a sheet slides out of.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({EDGE_RIGHT, EDGE_LEFT})
  @Retention(RetentionPolicy.SOURCE)
  @interface SheetEdge {}

  /**
   * Gets the current state of the sheet.
   *
   * @return One of {@link #STATE_EXPANDED}, {@link #STATE_DRAGGING}, or {@link #STATE_SETTLING}.
   */
  @SheetState
  int getState();

  /** Sets the current state of the sheet. Must be one of {@link StableSheetState}. */
  void setState(@StableSheetState int state);

  /**
   * Adds a callback to be notified of sheet events.
   *
   * @param callback The callback to notify when sheet events occur.
   */
  void addCallback(C callback);

  /**
   * Removes a callback to be notified of sheet events.
   *
   * @param callback The callback to remove
   */
  void removeCallback(C callback);
}
