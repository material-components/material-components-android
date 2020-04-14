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

import android.graphics.drawable.Drawable;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;

/** An interface to control the main animator used in indeterminate progress indicators. */
interface IndeterminateAnimatorControl {

  /** Starts the main animator. */
  void startMainAnimator();

  /** Cancels the main animator immediately. */
  void cancelMainAnimatorImmediately();

  /** Requests to cancel the main animator after the current cycle finishes. */
  void requestCancelMainAnimatorAfterCurrentCycle();

  /**
   * Resets all main animators controlled properties for ending (or a refresh start). This should be
   * called after the drawable is hidden or before the drawable becomes visible.
   */
  void resetMainAnimatorPropertiesForEnd();

  /**
   * Resets all main animator controlled properties for the next cycle in the animation. This should
   * be called between the main animator cycles.
   */
  void resetMainAnimatorPropertiesForNextCycle();

  /**
   * Register an {@link AnimationCallback} to main animator for the process after main animator ends
   * with a complete cycle.
   *
   * @param callback Callback to execute at the end of current main animator cycle. Note: only
   *     {@link AnimationCallback#onAnimationEnd(Drawable)} will be executed, other events will be
   *     ignored.
   */
  void registerMainAnimatorCompleteEndCallback(AnimationCallback callback);
}
