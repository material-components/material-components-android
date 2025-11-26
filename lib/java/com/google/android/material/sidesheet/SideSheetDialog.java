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

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.google.android.material.R;
import com.google.android.material.sidesheet.Sheet.StableSheetState;

/**
 * Base class for {@link android.app.Dialog}s styled as a side sheet.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/SideSheet.md">component
 * developer guidance</a> and <a href="https://material.io/components/side-sheets/overview">design
 * guidelines</a>.
 */
public class SideSheetDialog extends SheetDialog<SideSheetCallback> {

  private static final int SIDE_SHEET_DIALOG_THEME_ATTR = R.attr.sideSheetDialogTheme;
  private static final int SIDE_SHEET_DIALOG_DEFAULT_THEME_RES =
      R.style.Theme_Material3_Light_SideSheetDialog;

  @Nullable
  private SideSheetCallback defaultOnHideCallback;

  @Nullable
  private SideSheetCallback currentOnHideCallback;

  public SideSheetDialog(@NonNull Context context) {
    this(context, 0);
  }

  public SideSheetDialog(@NonNull Context context, @StyleRes int theme) {
    super(context, theme, SIDE_SHEET_DIALOG_THEME_ATTR, SIDE_SHEET_DIALOG_DEFAULT_THEME_RES);
  }

  @Override
  void setDefaultOnHideSheetAction(@NonNull Runnable action) {
    final Sheet<SideSheetCallback> behavior = getBehavior();

    if (currentOnHideCallback == defaultOnHideCallback) {
      if (currentOnHideCallback != null) {
        behavior.removeCallback(currentOnHideCallback);
      }

      defaultOnHideCallback = createDefaultOnHideCallback(action);
      currentOnHideCallback = defaultOnHideCallback;

      behavior.addCallback(currentOnHideCallback);
    } else {
      defaultOnHideCallback = createDefaultOnHideCallback(action);
    }
  }

  @Override
  void setOneShotOnHideSheetAction(@NonNull Runnable action) {
    final Sheet<SideSheetCallback> behavior = getBehavior();

    if (currentOnHideCallback != null) {
      behavior.removeCallback(currentOnHideCallback);
    }

    currentOnHideCallback = createOneTimeActionOnHideCallback(behavior, action);
    behavior.addCallback(currentOnHideCallback);
  }

  @NonNull
  private SideSheetCallback createDefaultOnHideCallback(@NonNull Runnable action) {
    return new SideSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View sheet, int newState) {
        if (newState == Sheet.STATE_HIDDEN) {
          action.run();
        }
      }

      @Override
      public void onSlide(@NonNull View sheet, float slideOffset) {
      }
    };
  }

  @NonNull
  private SideSheetCallback createOneTimeActionOnHideCallback(
      Sheet<SideSheetCallback> behavior, @NonNull Runnable action) {
    return new SideSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View sheet, int newState) {
        if (newState == Sheet.STATE_HIDDEN) {
          if (currentOnHideCallback != null) {
            behavior.removeCallback(currentOnHideCallback);
          }

          action.run();

          currentOnHideCallback = defaultOnHideCallback;
          if (currentOnHideCallback != null) {
            behavior.addCallback(currentOnHideCallback);
          }
        }
      }

      @Override
      public void onSlide(@NonNull View sheet, float slideOffset) {
      }
    };
  }

  @LayoutRes
  @Override
  int getLayoutResId() {
    return R.layout.m3_side_sheet_dialog;
  }

  @IdRes
  @Override
  int getDialogId() {
    return R.id.m3_side_sheet;
  }

  @NonNull
  @Override
  Sheet<SideSheetCallback> getBehaviorFromSheet(@NonNull FrameLayout sheet) {
    return SideSheetBehavior.from(sheet);
  }

  @StableSheetState
  @Override
  int getStateOnStart() {
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
    Sheet<SideSheetCallback> sheetBehavior = super.getBehavior();
    if (!(sheetBehavior instanceof SideSheetBehavior)) {
      throw new IllegalStateException("The view is not associated with SideSheetBehavior");
    }
    return (SideSheetBehavior<?>) sheetBehavior;
  }
}
