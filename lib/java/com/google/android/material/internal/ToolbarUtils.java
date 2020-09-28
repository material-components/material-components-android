/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.internal;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Utility methods for {@link Toolbar}s.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ToolbarUtils {

  private ToolbarUtils() {
    // Private constructor to prevent unwanted construction.
  }

  @Nullable
  public static View getSecondaryActionMenuItemView(@NonNull Toolbar toolbar) {
    ActionMenuView actionMenuView = getActionMenuView(toolbar);
    if (actionMenuView != null) {
      // Only return the first child of the ActionMenuView if there is more than one child
      if (actionMenuView.getChildCount() > 1) {
        return actionMenuView.getChildAt(0);
      }
    }
    return null;
  }

  @Nullable
  public static ActionMenuView getActionMenuView(@NonNull Toolbar toolbar) {
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      View child = toolbar.getChildAt(i);
      if (child instanceof ActionMenuView) {
        return (ActionMenuView) child;
      }
    }
    return null;
  }

  @Nullable
  public static ImageButton getNavigationIconButton(@NonNull Toolbar toolbar) {
    if (toolbar.getChildCount() > 0) {
      View child = toolbar.getChildAt(0);
      if (child instanceof ImageButton) {
        return (ImageButton) child;
      }
    }
    return null;
  }

  @SuppressWarnings("RestrictTo")
  @Nullable
  public static ActionMenuItemView getActionMenuItemView(
      @NonNull Toolbar toolbar, @IdRes int menuItemId) {
    ActionMenuView actionMenuView = getActionMenuView(toolbar);
    if (actionMenuView != null) {
      for (int i = 0; i < actionMenuView.getChildCount(); i++) {
        View child = actionMenuView.getChildAt(i);
        if (child instanceof ActionMenuItemView) {
          ActionMenuItemView actionMenuItemView = (ActionMenuItemView) child;
          if (actionMenuItemView.getItemData().getItemId() == menuItemId) {
            return actionMenuItemView;
          }
        }
      }
    }
    return null;
  }
}
