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

import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.sidesheet.Sheet.SheetState;

/** Callback that monitors side sheet events. */
public abstract class SideSheetCallback implements SheetCallback {

  /**
   * Called when the sheet changes its state.
   *
   * @param sheet The sheet view.
   * @param newState The new state. This should be one of {@link SideSheetBehavior#STATE_DRAGGING},
   *     {@link SideSheetBehavior#STATE_SETTLING}, {@link SideSheetBehavior#STATE_EXPANDED} or
   *     {@link SideSheetBehavior#STATE_HIDDEN}.
   */
  @Override
  public abstract void onStateChanged(@NonNull View sheet, @SheetState int newState);

  /**
   * Called when the sheet is being dragged.
   *
   * @param sheet The sheet view.
   * @param slideOffset The new offset of this sheet within [0,1] range. Offset increases as this
   *     sheet is moving towards the outer edge. A value of 0 means that the sheet is hidden, and a
   *     value of 1 means that the sheet is fully expanded.
   */
  @Override
  public abstract void onSlide(@NonNull View sheet, float slideOffset);

  void onLayout(@NonNull View sheet) {}
}
