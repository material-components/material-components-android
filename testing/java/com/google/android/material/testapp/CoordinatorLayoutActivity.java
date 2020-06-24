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

package com.google.android.material.testapp;

import android.widget.FrameLayout;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.testapp.base.BaseTestActivity;

public class CoordinatorLayoutActivity extends BaseTestActivity {

  @VisibleForTesting public FrameLayout mContainer;
  @VisibleForTesting public CoordinatorLayout mCoordinatorLayout;

  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.activity_coordinator_layout;
  }

  @Override
  protected void onContentViewSet() {
    mContainer = findViewById(R.id.container);
    mCoordinatorLayout = findViewById(R.id.coordinator);
  }
}
