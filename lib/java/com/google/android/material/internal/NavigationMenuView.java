/*
 * Copyright (C) 2015 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import androidx.annotation.RestrictTo;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class NavigationMenuView extends RecyclerView implements MenuView {

  public NavigationMenuView(Context context) {
    this(context, null);
  }

  public NavigationMenuView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NavigationMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
  }

  @Override
  public void initialize(MenuBuilder menu) {}

  @Override
  public int getWindowAnimations() {
    return 0;
  }
}
