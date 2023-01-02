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

import static java.util.Collections.max;
import static java.util.Collections.min;

import android.graphics.drawable.Drawable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility methods for {@link Toolbar}s.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ToolbarUtils {

  private static final Comparator<View> VIEW_TOP_COMPARATOR =
      new Comparator<View>() {
        @Override
        public int compare(View view1, View view2) {
          return view1.getTop() - view2.getTop();
        }
      };

  private ToolbarUtils() {
    // Private constructor to prevent unwanted construction.
  }

  @Nullable
  public static TextView getTitleTextView(@NonNull Toolbar toolbar) {
    List<TextView> textViews = getTextViewsWithText(toolbar, toolbar.getTitle());
    return textViews.isEmpty() ? null : min(textViews, VIEW_TOP_COMPARATOR);
  }

  @Nullable
  public static TextView getSubtitleTextView(@NonNull Toolbar toolbar) {
    List<TextView> textViews = getTextViewsWithText(toolbar, toolbar.getSubtitle());
    return textViews.isEmpty() ? null : max(textViews, VIEW_TOP_COMPARATOR);
  }

  private static List<TextView> getTextViewsWithText(@NonNull Toolbar toolbar, CharSequence text) {
    List<TextView> textViews = new ArrayList<>();
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      View child = toolbar.getChildAt(i);
      if (child instanceof TextView) {
        TextView textView = (TextView) child;
        if (TextUtils.equals(textView.getText(), text)) {
          textViews.add(textView);
        }
      }
    }
    return textViews;
  }

  @Nullable
  public static ImageView getLogoImageView(@NonNull Toolbar toolbar) {
    return getImageView(toolbar, toolbar.getLogo());
  }

  @Nullable
  private static ImageView getImageView(@NonNull Toolbar toolbar, @Nullable Drawable content) {
    if (content == null) {
      return null;
    }
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      View child = toolbar.getChildAt(i);
      if (child instanceof ImageView) {
        ImageView imageView = (ImageView) child;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null
            && drawable.getConstantState() != null
            && drawable.getConstantState().equals(content.getConstantState())) {
          return imageView;
        }
      }
    }
    return null;
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
    Drawable navigationIcon = toolbar.getNavigationIcon();
    if (navigationIcon == null) {
      return null;
    }
    for (int i = 0; i < toolbar.getChildCount(); i++) {
      View child = toolbar.getChildAt(i);
      if (child instanceof ImageButton) {
        ImageButton imageButton = (ImageButton) child;
        if (imageButton.getDrawable() == navigationIcon) {
          return imageButton;
        }
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
