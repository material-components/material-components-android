/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.google.android.material.bottomsheet;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.animation.AnimationUtils;
import java.util.List;

class InsetsAnimationCallback extends WindowInsetsAnimationCompat.Callback {

  private final View view;

  private int startY;
  private int startTranslationY;

  private final int[] tmpLocation = new int[2];

  public InsetsAnimationCallback(View view) {
    super(DISPATCH_MODE_STOP);
    this.view = view;
  }

  @Override
  public void onPrepare(@NonNull WindowInsetsAnimationCompat windowInsetsAnimationCompat) {
    view.getLocationOnScreen(tmpLocation);
    startY = tmpLocation[1];
  }

  @NonNull
  @Override
  public BoundsCompat onStart(
      @NonNull WindowInsetsAnimationCompat windowInsetsAnimationCompat,
      @NonNull BoundsCompat boundsCompat) {
    view.getLocationOnScreen(tmpLocation);
    int endY = tmpLocation[1];
    startTranslationY = startY - endY;

    // Move the view back to its original position before the insets were applied.
    view.setTranslationY(startTranslationY);

    return boundsCompat;
  }

  @NonNull
  @Override
  public WindowInsetsCompat onProgress(
      @NonNull WindowInsetsCompat insets,
      @NonNull List<WindowInsetsAnimationCompat> animationList) {
    for (WindowInsetsAnimationCompat animation : animationList) {
      if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
        // Move the view to match the animated position of the keyboard.
        float translationY =
            AnimationUtils.lerp(startTranslationY, 0, animation.getInterpolatedFraction());
        view.setTranslationY(translationY);
        break;
      }
    }
    return insets;
  }

  @Override
  public void onEnd(@NonNull WindowInsetsAnimationCompat windowInsetsAnimationCompat) {
    view.setTranslationY(0f);
  }
}
