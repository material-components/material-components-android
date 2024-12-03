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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.appcompat.view.menu.MenuView;
import androidx.annotation.RestrictTo;

/**
 * Interface for views that represent the Navigation Bar menu items.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface NavigationBarMenuItemView extends MenuView.ItemView {
  /** Update the bar expanded state in item. */
  void setExpanded(boolean expanded);

  /** Whether or not the item's bar expanded state is expanded. */
  boolean isExpanded();

  /** Set whether or not to only show the item when expanded. */
  void setOnlyShowWhenExpanded(boolean onlyShowWhenExpanded);

  /** Whether or not to only show the item when expanded. */
  boolean isOnlyVisibleWhenExpanded();
}
