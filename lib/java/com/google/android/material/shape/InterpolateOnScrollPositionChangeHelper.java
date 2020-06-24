/*
 * Copyright 2017 The Android Open Source Project
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

package com.google.android.material.shape;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.ScrollView;
import androidx.annotation.NonNull;

/** Helper class to handle shape interpolation when shaped views enter or exit the window. */
public class InterpolateOnScrollPositionChangeHelper {

  private View shapedView;
  private MaterialShapeDrawable materialShapeDrawable;
  private ScrollView containingScrollView;
  private final int[] scrollLocation = new int[2];
  private final int[] containerLocation = new int[2];
  private final ViewTreeObserver.OnScrollChangedListener scrollChangedListener =
      new OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
          updateInterpolationForScreenPosition();
        }
      };

  /**
   * Instantiate a scroll position helper.
   *
   * @param shapedView the {@link View} whose background is a {@link MaterialShapeDrawable} and
   *     which is scrolled in and out of view.
   * @param materialShapeDrawable the {@link MaterialShapeDrawable} which will be interpolated.
   * @param containingScrollView the {@link ScrollView} that contains shapedView.
   */
  public InterpolateOnScrollPositionChangeHelper(
      View shapedView,
      MaterialShapeDrawable materialShapeDrawable,
      ScrollView containingScrollView) {
    this.shapedView = shapedView;
    this.materialShapeDrawable = materialShapeDrawable;
    this.containingScrollView = containingScrollView;
  }

  /**
   * Set the {@link MaterialShapeDrawable} which will be interpolated.
   *
   * @param materialShapeDrawable the desired drawable.
   */
  public void setMaterialShapeDrawable(MaterialShapeDrawable materialShapeDrawable) {
    this.materialShapeDrawable = materialShapeDrawable;
  }

  /**
   * Set the {@link ScrollView} which contains the {@link View} being interpolated.
   *
   * @param containingScrollView
   */
  public void setContainingScrollView(ScrollView containingScrollView) {
    this.containingScrollView = containingScrollView;
  }

  /**
   * Start listening for scroll changes and interpolating based on position.
   *
   * @param viewTreeObserver {@link ViewTreeObserver belonging to the {@link View} being
   * interpolated.
   */
  public void startListeningForScrollChanges(@NonNull ViewTreeObserver viewTreeObserver) {
    viewTreeObserver.addOnScrollChangedListener(scrollChangedListener);
  }

  /**
   * Stop listening for scroll changes and interpolating based on position.
   *
   * @param viewTreeObserver {@link ViewTreeObserver belonging to the {@link View} being
   * interpolated.
   */
  public void stopListeningForScrollChanges(@NonNull ViewTreeObserver viewTreeObserver) {
    viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener);
  }

  /**
   * Updates the {@link MaterialShapeDrawable}'s interpolation based on the {@link View}'s position
   * in the containing {@link ScrollView}.
   */
  public void updateInterpolationForScreenPosition() {
    if (containingScrollView == null) {
      // No scroll view, no healing/growing.
      return;
    }
    if (containingScrollView.getChildCount() == 0) {
      // No container inside scroll view, no healing/growing.
      throw new IllegalStateException(
          "Scroll bar must contain a child to calculate interpolation.");
    }

    containingScrollView.getLocationInWindow(scrollLocation);
    containingScrollView.getChildAt(0).getLocationInWindow(containerLocation);
    int y = shapedView.getTop() - scrollLocation[1] + containerLocation[1];
    int viewHeight = shapedView.getHeight();
    int windowHeight = containingScrollView.getHeight();

    // Off the top of the screen.
    if (y < 0) {
      materialShapeDrawable.setInterpolation(
          Math.max(0f, Math.min(1f, 1f + (float) y / (float) viewHeight)));
      shapedView.invalidate();
    } else if (y + viewHeight > windowHeight) {
      int distanceOffScreen = y + viewHeight - windowHeight;
      materialShapeDrawable.setInterpolation(
          Math.max(0f, Math.min(1f, 1f - (float) distanceOffScreen / (float) viewHeight)));
      shapedView.invalidate();
    } else if (materialShapeDrawable.getInterpolation() != 1f) {
      materialShapeDrawable.setInterpolation(1f);
      shapedView.invalidate();
    }
  }
}
