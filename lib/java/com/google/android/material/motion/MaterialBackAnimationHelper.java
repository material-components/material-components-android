/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.motion;

import com.google.android.material.R;

import android.animation.TimeInterpolator;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.window.BackEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.animation.PathInterpolatorCompat;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Base helper class for views that support back handling, which assists with common animation
 * details and back event validation.
 */
abstract class MaterialBackAnimationHelper {

  @NonNull private final TimeInterpolator progressInterpolator;

  @NonNull protected final View view;

  @Nullable private BackEvent backEvent;

  public MaterialBackAnimationHelper(@NonNull View view) {
    this.view = view;

    progressInterpolator =
        MotionUtils.resolveThemeInterpolator(
            view.getContext(),
            R.attr.motionEasingStandardDecelerateInterpolator,
            PathInterpolatorCompat.create(0, 0, 0, 1));
  }

  protected float interpolateProgress(float progress) {
    return progressInterpolator.getInterpolation(progress);
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected void onStartBackProgress(@NonNull BackEvent backEvent) {
    this.backEvent = backEvent;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  protected void onUpdateBackProgress(@NonNull BackEvent backEvent) {
    if (this.backEvent == null) {
      throw new IllegalStateException("Must call startBackProgress() before updateBackProgress()");
    }
    this.backEvent = backEvent;
  }

  @Nullable
  public BackEvent onHandleBackInvoked() {
    BackEvent finalBackEvent = this.backEvent;
    this.backEvent = null;
    return finalBackEvent;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  @CanIgnoreReturnValue
  @NonNull
  protected BackEvent onCancelBackProgress() {
    if (this.backEvent == null) {
      throw new IllegalStateException(
          "Must call startBackProgress() and updateBackProgress() before cancelBackProgress()");
    }
    BackEvent finalBackEvent = this.backEvent;
    this.backEvent = null;
    return finalBackEvent;
  }
}
