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
import com.google.android.material.behavior.HideViewOnScrollBehavior.ViewEdge;

/** A delegate for {@link HideViewOnScrollBehavior} to handle logic specific to the View's edge. */
abstract class HideViewOnScrollDelegate {
  /**
   * Returns the edge of the screen from which the view should slide in and out. Must be a {@link
   * com.google.android.material.behavior.HideViewOnScrollBehavior.ViewEdge} value.
   */
  @ViewEdge
  abstract int getViewEdge();

  /**
   * Returns the size of the View. This is based on the height value for the bottom variation, and
   * the width value for right and left variations.
   */
  abstract <V extends View> int getSize(@NonNull V child, @NonNull MarginLayoutParams paramsCompat);

  /**
   * Sets the additional offset to add when hiding the view. The offset will be added on the Y axis
   * for the bottom variation, and on the X axis for the right and left variations.
   */
  abstract <V extends View> void setAdditionalHiddenOffset(
      @NonNull V child, int size, int additionalHiddenOffset);

  /** Returns the amount by which the View should be translated. */
  abstract int getTargetTranslation();

  /**
   * Sets the View's translation along the respective axis by the desired target translation amount.
   */
  abstract <V extends View> void setViewTranslation(@NonNull V child, int targetTranslation);

  /**
   * Returns an {@link ViewPropertyAnimator} that translates along the respective axis.
   *
   * @param child the View to animate
   * @param targetTranslation the amount by which to translate the View
   */
  abstract <V extends View> ViewPropertyAnimator getViewTranslationAnimator(
      @NonNull V child, int targetTranslation);
}
