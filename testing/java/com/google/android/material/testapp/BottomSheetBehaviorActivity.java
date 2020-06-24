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

import android.content.Intent;
import android.widget.LinearLayout;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.testapp.base.BaseTestActivity;

public class BottomSheetBehaviorActivity extends BaseTestActivity {

  public static final String EXTRA_INITIAL_STATE = "initial_state";

  @VisibleForTesting public LinearLayout mBottomSheet;
  @VisibleForTesting public BottomSheetBehavior mBehavior;
  @VisibleForTesting public CoordinatorLayout mCoordinatorLayout;
  @VisibleForTesting public FloatingActionButton mFab;

  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.test_design_bottom_sheet_behavior;
  }

  @Override
  protected void onContentViewSet() {
    mCoordinatorLayout = findViewById(R.id.coordinator);
    mBottomSheet = findViewById(R.id.bottom_sheet);
    mBehavior = BottomSheetBehavior.from(mBottomSheet);
    mFab = findViewById(R.id.fab);
    Intent intent = getIntent();
    if (intent != null) {
      int initialState = intent.getIntExtra(EXTRA_INITIAL_STATE, -1);
      if (initialState != -1) {
        //noinspection ResourceType
        mBehavior.setState(initialState);
      }
    }
  }
}
