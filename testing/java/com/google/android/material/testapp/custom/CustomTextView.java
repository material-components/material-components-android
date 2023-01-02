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
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * Custom extension of AppCompat's text view for testing a runtime-specified behavior.
 */
public class CustomTextView extends AppCompatTextView
    implements CoordinatorLayout.AttachedBehavior {
  public CustomTextView(Context context) {
    super(context);
  }

  public CustomTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<TextView> getBehavior() {
      return new TestFloatingBehavior();
  }
}
