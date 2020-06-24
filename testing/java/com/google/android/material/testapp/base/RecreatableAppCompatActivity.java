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

package com.google.android.material.testapp.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import java.util.concurrent.CountDownLatch;

/**
 * Activity that keeps track of resume / destroy lifecycle events, as well as of the last instance
 * of itself.
 */
public class RecreatableAppCompatActivity extends AppCompatActivity {
  // These must be cleared after each test using clearState()
  @SuppressLint("StaticFieldLeak") // Not an issue because this is test-only and gets cleared
  public static RecreatableAppCompatActivity activity;

  public static CountDownLatch resumedLatch;
  public static CountDownLatch destroyedLatch;

  public static void clearState() {
    activity = null;
    resumedLatch = null;
    destroyedLatch = null;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activity = this;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (resumedLatch != null) {
      resumedLatch.countDown();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (destroyedLatch != null) {
      destroyedLatch.countDown();
    }
  }
}
