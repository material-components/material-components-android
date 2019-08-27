/*
 * Copyright (C) 2017 The Android Open Source Project
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

import androidx.appcompat.widget.Toolbar;
import com.google.android.material.testapp.base.BaseTestActivity;

/** Activity for testing collapse state restoration in AppBar. */
public class AppBarLayoutCollapsePinActivity extends BaseTestActivity {

  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.design_appbar_toolbar_collapse_pin_restore_test;
  }

  @Override
  protected void onContentViewSet() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }
}
