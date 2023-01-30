/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.android.material.carousel;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.NonNull;

/** A class that handles accessibility for CarouselLayoutManager. */
final class CarouselAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {

  public CarouselAccessibilityDelegate(@NonNull RecyclerView recyclerView) {
    super(recyclerView);
  }

  @Override
  public boolean onRequestSendAccessibilityEvent(
      @NonNull ViewGroup host, @NonNull View child, @NonNull AccessibilityEvent event) {
    switch (event.getEventType()) {
        // Allow every child in the carousel an opportunity to bring itself into the focal range
        // when focused by accessibility.
      case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
        RecyclerView rv = (RecyclerView) host;
        Rect rect = new Rect(0, 0, child.getWidth(), child.getHeight());
        rv.getLayoutManager()
            .requestChildRectangleOnScreen(
                (RecyclerView) host,
                child,
                rect,
                /* immediate= */ true,
                /* focusedChildVisible= */ false);
        return true;
      default:
        return super.onRequestSendAccessibilityEvent(host, child, event);
    }
  }
}
