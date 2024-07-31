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
import android.provider.Settings.Global;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * This is a utility class to get system animator duration scale from the system settings. It's used
 * as instances so that some requirements for testing can be met by mocking.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class AnimatorDurationScaleProvider {

  /** Returns the animator duration scale from developer options setting. */
  public float getSystemAnimatorDurationScale(@NonNull ContentResolver contentResolver) {
    return Global.getFloat(contentResolver, Global.ANIMATOR_DURATION_SCALE, 1f);
  }
}
