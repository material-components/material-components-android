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

import com.google.android.material.R;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import com.google.android.material.sidesheet.Sheet.StableSheetState;

/** Base class for {@link android.app.Dialog}s styled as a side sheet. */
public class SideSheetDialog extends SheetDialog {

  private static final int SIDE_SHEET_DIALOG_THEME_ATTR = R.attr.sideSheetDialogTheme;
  private static final int SIDE_SHEET_DIALOG_DEFAULT_THEME_RES =
      R.style.Theme_Material3_Light_SideSheetDialog;

  public SideSheetDialog(@NonNull Context context) {
    this(context, 0);
  }

  public SideSheetDialog(@NonNull Context context, @StyleRes int theme) {
    super(context, theme, SIDE_SHEET_DIALOG_THEME_ATTR, SIDE_SHEET_DIALOG_DEFAULT_THEME_RES);
    // We hide the title bar for any style configuration. Otherwise, there will be a gap
    // above the side sheet when it is expanded.
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  @LayoutRes
  @Override
  protected int getLayoutResId() {
    return R.layout.m3_side_sheet_dialog;
  }

  @IdRes
  @Override
  protected int getDialogId() {
    return R.id.m3_side_sheet;
  }

  @NonNull
  @Override
  protected Sheet getBehaviorFromSheet(@NonNull FrameLayout sheet) {
    return SideSheetBehavior.from(sheet);
  }

  @StableSheetState
  @Override
  protected int getStateOnStart() {
    return Sheet.STATE_EXPANDED;
  }

  /**
   * Returns the behavior associated with this {@link SideSheetDialog}. The behavior must always be
   * a {@link SideSheetBehavior}; otherwise, this method will throw an {@link
   * IllegalStateException}.
   */
  @NonNull
  @Override
  public SideSheetBehavior<? extends View> getBehavior() {
    Sheet sheetBehavior = super.getBehavior();
    if (!(sheetBehavior instanceof SideSheetBehavior)) {
      throw new IllegalStateException("The view is not associated with SideSheetBehavior");
    }
    return (SideSheetBehavior<?>) sheetBehavior;
  }
}
