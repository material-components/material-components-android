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

interface SheetCallback {

  /**
   * Called when the sheet changes its state.
   *
   * @param sheet The sheet view.
   * @param newState The new state.
   */
  void onStateChanged(@NonNull View sheet, @SheetState int newState);

  /**
   * Called when the sheet is being dragged.
   *
   * @param sheet The sheet view.
   * @param slideOffset The new offset of this sheet.
   */
  void onSlide(@NonNull View sheet, float slideOffset);
}
