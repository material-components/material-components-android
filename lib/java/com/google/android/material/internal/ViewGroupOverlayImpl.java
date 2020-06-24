/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.internal;

import android.view.View;
import androidx.annotation.NonNull;

interface ViewGroupOverlayImpl extends ViewOverlayImpl {

  /**
   * Adds a View to the overlay. The bounds of the added view should be relative to the host view.
   * Any view added to the overlay should be removed when it is no longer needed or no longer
   * visible.
   *
   * <p>Views in the overlay are visual-only; they do not receive input events and do not
   * participate in focus traversal. Overlay views are intended to be transient, such as might be
   * needed by a temporary animation effect.
   *
   * <p>If the view has a parent, the view will be removed from that parent before being added to
   * the overlay. Also, if that parent is attached in the current view hierarchy, the view will be
   * repositioned such that it is in the same relative location inside the activity. For example, if
   * the view's current parent lies 100 pixels to the right and 200 pixels down from the origin of
   * the overlay's host view, then the view will be offset by (100, 200).
   *
   * @param view The View to be added to the overlay. The added view will be drawn when the overlay
   *     is drawn.
   * @see #remove(View)
   * @see android.view.ViewOverlay#add(android.graphics.drawable.Drawable)
   */
  void add(@NonNull View view);

  /**
   * Removes the specified View from the overlay.
   *
   * @param view The View to be removed from the overlay.
   * @see #add(View)
   * @see android.view.ViewOverlay#remove(android.graphics.drawable.Drawable)
   */
  void remove(@NonNull View view);
}
