/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.material.behavior;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off of the
 * right of the screen when scrolling down, and show it when scrolling up.
 */
final class HideRightViewOnScrollDelegate extends HideViewOnScrollDelegate {

  HideRightViewOnScrollDelegate() {}

  @Override
  int getViewEdge() {
    return HideViewOnScrollBehavior.EDGE_RIGHT;
  }

  @Override
  <V extends View> int getSize(@NonNull V child, @NonNull MarginLayoutParams paramsCompat) {
    return child.getMeasuredWidth() + paramsCompat.rightMargin;
  }

  @Override
  <V extends View> void setAdditionalHiddenOffset(
      @NonNull V child, int size, int additionalHiddenOffset) {
    child.setTranslationX(size + additionalHiddenOffset);
  }

  @Override
  int getTargetTranslation() {
    return 0;
  }

  @Override
  <V extends View> void setViewTranslation(@NonNull V child, int targetTranslation) {
    child.setTranslationX(targetTranslation);
  }

  @Override
  <V extends View> ViewPropertyAnimator getViewTranslationAnimator(
      @NonNull V child, int targetTranslation) {
    return child.animate().translationX(targetTranslation);
  }
}
