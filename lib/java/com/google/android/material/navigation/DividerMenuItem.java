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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An empty MenuItem that is used to represent a divider in the menu.
 */
class DividerMenuItem implements MenuItem {

  @Override
  public boolean collapseActionView() {
    return false;
  }

  @Override
  public boolean expandActionView() {
    return false;
  }

  @Nullable
  @Override
  public ActionProvider getActionProvider() {
    return null;
  }

  @Nullable
  @Override
  public View getActionView() {
    return null;
  }

  @Override
  public char getAlphabeticShortcut() {
    return 0;
  }

  @Override
  public int getGroupId() {
    return 0;
  }

  @Nullable
  @Override
  public Drawable getIcon() {
    return null;
  }

  @Nullable
  @Override
  public Intent getIntent() {
    return null;
  }

  @Override
  public int getItemId() {
    return 0;
  }

  @Nullable
  @Override
  public ContextMenuInfo getMenuInfo() {
    return null;
  }

  @Override
  public char getNumericShortcut() {
    return 0;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Nullable
  @Override
  public SubMenu getSubMenu() {
    return null;
  }

  @Nullable
  @Override
  public CharSequence getTitle() {
    return null;
  }

  @Nullable
  @Override
  public CharSequence getTitleCondensed() {
    return null;
  }

  @Override
  public boolean hasSubMenu() {
    return false;
  }

  @Override
  public boolean isActionViewExpanded() {
    return false;
  }

  @Override
  public boolean isCheckable() {
    return false;
  }

  @Override
  public boolean isChecked() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return false;
  }

  @NonNull
  @Override
  public MenuItem setActionProvider(@Nullable ActionProvider actionProvider) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setActionView(@Nullable View view) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setActionView(int resId) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setAlphabeticShortcut(char alphaChar) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setCheckable(boolean checkable) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setChecked(boolean checked) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setEnabled(boolean enabled) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setIcon(@Nullable Drawable icon) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setIcon(int iconRes) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setIntent(@Nullable Intent intent) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setNumericShortcut(char numericChar) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setOnActionExpandListener(@Nullable OnActionExpandListener listener) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setOnMenuItemClickListener(
      @Nullable OnMenuItemClickListener menuItemClickListener) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setShortcut(char numericChar, char alphaChar) {
    return null;
  }

  @Override
  public void setShowAsAction(int actionEnum) {

  }

  @NonNull
  @Override
  public MenuItem setShowAsActionFlags(int actionEnum) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setTitle(int title) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setTitle(@Nullable CharSequence title) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setTitleCondensed(@Nullable CharSequence title) {
    return null;
  }

  @NonNull
  @Override
  public MenuItem setVisible(boolean visible) {
    return null;
  }
}
