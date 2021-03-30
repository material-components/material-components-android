/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.bottomnavigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationItemView extends NavigationBarItemView {
  public BottomNavigationItemView(@NonNull Context context) {
    super(context);
  }

  @Override
  @LayoutRes
  protected int getItemLayoutResId() {
    return R.layout.design_bottom_navigation_item;
  }

  @Override
  @DimenRes
  protected int getItemDefaultMarginResId() {
    return R.dimen.design_bottom_navigation_margin;
  }
}
