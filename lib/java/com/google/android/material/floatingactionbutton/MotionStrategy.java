/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.floatingactionbutton;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton.OnChangedCallback;
import java.util.List;

/**
 * A delegate to perform actions that are coupled with animations on {@link
 * ExtendedFloatingActionButton}.
 */
interface MotionStrategy {

  void performNow();

  MotionSpec getCurrentMotionSpec();

  @AnimatorRes int getDefaultMotionSpecResource();

  void setMotionSpec(@Nullable MotionSpec spec);

  @Nullable MotionSpec getMotionSpec();

  AnimatorSet createAnimator();

  void addAnimationListener(@NonNull AnimatorListener listener);

  void removeAnimationListener(@NonNull AnimatorListener listener);

  List<AnimatorListener> getListeners();

  void onAnimationStart(Animator animator);

  void onAnimationEnd();

  void onAnimationCancel();

  void onChange(@Nullable OnChangedCallback callback);

  boolean shouldCancel();
}


