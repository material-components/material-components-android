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
import android.view.View;
import android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;

public class TestFloatingBehavior extends CoordinatorLayout.Behavior<TextView> {
  // Default constructor is needed to instantiate a Behavior object when it is attached
  // to custom view class as class-level annotation
  public TestFloatingBehavior() {}

  // This constructor is needed to instantiate a Behavior object when it is attached to a
  // view via layout_behavior XML attribute
  public TestFloatingBehavior(Context context, AttributeSet attrs) {}

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency) {
    return dependency instanceof Snackbar.SnackbarLayout;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
    child.setTranslationY(Math.min(0, dependency.getTranslationY() - dependency.getHeight()));
    return true;
  }
}
