/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.transition;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link Visibility} {@link android.transition.Transition} that does nothing to the target views,
 * which can be useful for preserving a scene on screen during a Fragment transition.
 */
@RequiresApi(VERSION_CODES.KITKAT)
public class Hold extends Visibility {

  @NonNull
  @Override
  public Animator onAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return ValueAnimator.ofFloat(0);
  }

  @NonNull
  @Override
  public Animator onDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return ValueAnimator.ofFloat(0);
  }
}
