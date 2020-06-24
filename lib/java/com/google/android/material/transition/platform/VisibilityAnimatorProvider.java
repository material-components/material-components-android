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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import android.animation.Animator;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An interface which is able to provide an Animator to be supplied to a {@link
 * android.transition.Visibility} transition when a target view is appearing or disappearing.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public interface VisibilityAnimatorProvider {

  /**
   * Should return an Animator that animates in the appearing target {@code view}.
   *
   * @param sceneRoot The root of the transition hierarchy, which can be useful for checking
   *     configurations such as RTL
   * @param view The view that is appearing
   */
  @Nullable
  Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view);

  /**
   * Should return an Animator that animates out the disappearing target {@code view}.
   *
   * @param sceneRoot The root of the transition hierarchy, which can be useful for checking
   *     configurations such as RTL
   * @param view The view that is disappearing
   */
  @Nullable
  Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view);
}
