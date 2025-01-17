/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.google.android.material.navigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import androidx.appcompat.view.menu.MenuItemImpl;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.widget.TextViewCompat;

/**
 * Provides a view that will be used to render subheader items inside a {@link
 * NavigationBarMenuView}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationBarSubheaderView extends FrameLayout
    implements NavigationBarMenuItemView {
  private final TextView subheaderLabel;
  private boolean expanded;
  boolean onlyShowWhenExpanded;
  @Nullable
  private MenuItemImpl itemData;
  @Nullable private ColorStateList textColor;

  NavigationBarSubheaderView(@NonNull Context context) {
    super(context);
    LayoutInflater.from(context)
        .inflate(R.layout.m3_navigation_menu_subheader, this, true);
    subheaderLabel = findViewById(R.id.navigation_menu_subheader_label);
  }

  @Override
  public void initialize(@NonNull MenuItemImpl menuItem, int i) {
    this.itemData = menuItem;
    menuItem.setCheckable(false);
    subheaderLabel.setText(menuItem.getTitle());
    updateVisibility();
  }

  public void setTextAppearance(@StyleRes int textAppearance) {
    TextViewCompat.setTextAppearance(subheaderLabel, textAppearance);
    // Set the text color if the user has set it, since it takes precedence
    // over a color set in the text appearance.
    if (textColor != null) {
      subheaderLabel.setTextColor(textColor);
    }
  }

  public void setTextColor(@Nullable ColorStateList color) {
    textColor = color;
    if (color != null) {
      subheaderLabel.setTextColor(color);
    }
  }

  @Override
  @Nullable
  public MenuItemImpl getItemData() {
    return itemData;
  }

  @Override
  public void setTitle(@Nullable CharSequence charSequence) {}

  @Override
  public void setEnabled(boolean enabled) {}

  @Override
  public void setCheckable(boolean checkable) {}

  @Override
  public void setChecked(boolean checked) {}

  @Override
  public void setShortcut(boolean showShortcut, char shortcutKey) {}

  @Override
  public void setIcon(@Nullable Drawable drawable) {}

  @Override
  public boolean prefersCondensedTitle() {
    return false;
  }

  @Override
  public boolean showsIcon() {
    return false;
  }

  @Override
  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
    updateVisibility();
  }

  @Override
  public boolean isExpanded() {
    return this.expanded;
  }

  @Override
  public void setOnlyShowWhenExpanded(boolean onlyShowWhenExpanded) {
    this.onlyShowWhenExpanded = onlyShowWhenExpanded;
    updateVisibility();
  }

  @Override
  public boolean isOnlyVisibleWhenExpanded() {
    return this.onlyShowWhenExpanded;
  }

  private void updateVisibility() {
    if (itemData != null) {
      setVisibility(itemData.isVisible() && (expanded || !onlyShowWhenExpanded) ? VISIBLE : GONE);
    }
  }
}
