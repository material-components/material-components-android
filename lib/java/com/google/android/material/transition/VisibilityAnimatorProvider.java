/*
 * Copyright 2020 The Android Open Source Project
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

/**
 * An interface which is able to provide an Animator to be supplied to a {@link
 * android.transition.Visibility} transition when a target view is appearing or disappearing.
 */
public interface VisibilityAnimatorProvider {

  @Nullable
  Animator createAppear(
      @NonNull  ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues);

  @Nullable
  Animator createDisappear(
      @NonNull  ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues);
}
