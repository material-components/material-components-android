/*
 * Copyright 2018 The Android Open Source Project
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
package com.google.android.material.animation;

import android.view.View;

/**
 * Callback to be invoked when the view undergoes a translation or scale transformation. This is
 * particularly useful to react to animations which update the translation or scale of this view.
 * Such as the cutout of the {@link com.google.android.material.bottomappbar.BottomAppBar} reacting to
 * the {@link com.google.android.material.floatingactionbutton.FloatingActionButton}.
 */
public interface TransformationListener<T extends View> {
  /**
   * Called when the view has been translated.
   *
   * @param fab the view that was translated.
   */
  void onTranslationChanged(T fab);

  /**
   * Called when the view has been scaled.
   *
   * @param fab the view that was scaled.
   */
  void onScaleChanged(T fab);
}
