/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.google.android.material.progressindicator;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.ContentResolver;
import android.os.Build.VERSION;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

/**
 * This is a utility class to get system animator duration scale from the system settings. It's used
 * as instances so that some requirements for testing can be met by mocking.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class AnimatorDurationScaleProvider {

  /** The emulated system animator duration scale setting for SDK_INT < 16. */
  private static float defaultSystemAnimatorDurationScale = 1f;

  /** Returns the animator duration scale from developer options setting. */
  public float getSystemAnimatorDurationScale(@NonNull ContentResolver contentResolver) {
    if (VERSION.SDK_INT >= 17) {
      return Global.getFloat(contentResolver, Global.ANIMATOR_DURATION_SCALE, 1f);
    }
    if (VERSION.SDK_INT == 16) {
      return System.getFloat(contentResolver, System.ANIMATOR_DURATION_SCALE, 1f);
    }
    return defaultSystemAnimatorDurationScale;
  }

  /**
   * Sets the default system animator duration scale for SDK < 16.
   *
   * @param scale New system animator duration scale.
   * @see android.provider.Settings.Global#ANIMATOR_DURATION_SCALE
   * @see android.provider.Settings.System#ANIMATOR_DURATION_SCALE
   */
  @VisibleForTesting
  public static void setDefaultSystemAnimatorDurationScale(float scale) {
    defaultSystemAnimatorDurationScale = scale;
  }
}
