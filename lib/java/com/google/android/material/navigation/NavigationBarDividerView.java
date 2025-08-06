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
import android.graphics.drawable.Drawable;
import androidx.appcompat.view.menu.MenuItemImpl;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Provides a view that will be used to render subheader items inside a {@link
 * NavigationBarMenuView}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationBarDividerView extends FrameLayout implements NavigationBarMenuItemView {

  private boolean expanded;
  boolean onlyShowWhenExpanded;
  private boolean dividersEnabled;

  NavigationBarDividerView(@NonNull Context context) {
    super(context);
    LayoutInflater.from(context).inflate(R.layout.m3_navigation_menu_divider, this, true);
    updateVisibility();
  }

  @Override
  public void initialize(@NonNull MenuItemImpl menuItem, int i) {
    updateVisibility();
  }

  @Override
  @Nullable
  public MenuItemImpl getItemData() {
    return null;
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

  public void updateVisibility() {
    setVisibility(dividersEnabled && (expanded || !onlyShowWhenExpanded) ? VISIBLE : GONE);
  }

  public void setDividersEnabled(boolean dividersEnabled) {
    this.dividersEnabled = dividersEnabled;
    updateVisibility();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
