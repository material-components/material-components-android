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

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.testapp.base.BaseTestActivity;

/** Activity with an AppBar that contains horizontally-scrolling content. */
public class AppBarHorizontalScrollingActivity extends BaseTestActivity {
  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.design_appbar_horizontal_scrolling;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }
}
