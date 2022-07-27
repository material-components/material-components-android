/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.animation;

import android.view.View;
import androidx.annotation.NonNull;

/**
 * Represents a {@link View} that provides animation callbacks. Implement this interface when
 * another class needs to start your {@link View}'s animation with a callback.
 */
public interface AnimatableView {

  void startAnimation(@NonNull Listener listener);

  void stopAnimation();

  /** Represents a callback for an {@link AnimatableView}. */
  interface Listener {

    void onAnimationEnd();
  }
}
