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
package com.google.android.material.testapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.navigation.NavigationView;

/** Expose hasSystemWindowInsets() for testing. */
public class NavigationTestView extends NavigationView {

  boolean hasSystemWindowInsets;

  public NavigationTestView(Context context) {
    this(context, null);
  }

  public NavigationTestView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NavigationTestView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onInsetsChanged(WindowInsetsCompat insets) {
    super.onInsetsChanged(insets);
    hasSystemWindowInsets = insets.hasSystemWindowInsets();
  }

  public boolean hasSystemWindowInsets() {
    return hasSystemWindowInsets;
  }
}
