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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.activity.BackEventCompat;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Interface for views that support back handling.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface MaterialBackHandler {

  /**
   * Call this method from {@link
   * android.window.OnBackAnimationCallback#onBackStarted(android.window.BackEvent)} or {@link
   * androidx.activity.OnBackPressedCallback#handleOnBackStarted(BackEventCompat)} so that the back
   * handler can initialize and start animating.
   *
   * <p>Note that this must be called prior to calling {@link #updateBackProgress(BackEventCompat)}.
   */
  void startBackProgress(@NonNull BackEventCompat backEvent);

  /**
   * Call this method from {@link
   * android.window.OnBackAnimationCallback#onBackProgressed(android.window.BackEvent)} or {@link
   * androidx.activity.OnBackPressedCallback#handleOnBackProgressed(BackEventCompat)} so that the
   * back handler can continue animating with a new progress value.
   */
  void updateBackProgress(@NonNull BackEventCompat backEvent);

  /**
   * Call this method from {@link android.window.OnBackAnimationCallback#onBackInvoked()} or {@link
   * androidx.activity.OnBackPressedCallback#handleOnBackPressed()} so that the back handler can
   * complete the back animation, or handle back without progress in certain cases.
   */
  void handleBackInvoked();

  /**
   * Call this method from {@link android.window.OnBackAnimationCallback#onBackCancelled()} or
   * {@link androidx.activity.OnBackPressedCallback#handleOnBackCancelled()} so that the back
   * handler can cancel the back animation.
   */
  void cancelBackProgress();
}
