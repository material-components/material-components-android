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

package io.material.catalog.carousel;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnSliderTouchListener;

/** Utilities for setting up carousel catalog demos. */
class CarouselDemoUtils {

  private CarouselDemoUtils() {}

  static RecyclerView.OnScrollListener createUpdateSliderOnScrollListener(
      Slider slider, CarouselAdapter adapter) {
    return new RecyclerView.OnScrollListener() {
      private boolean dragged = false;

      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
          dragged = true;
        } else if (dragged && newState == RecyclerView.SCROLL_STATE_IDLE) {
          if (recyclerView.computeHorizontalScrollRange() != 0) {
            slider.setValue(
                (adapter.getItemCount() - 1)
                        * Math.abs(recyclerView.computeHorizontalScrollOffset())
                        / recyclerView.computeHorizontalScrollRange()
                    + 1);
          }
          dragged = false;
        }
      }
    };
  }

  static OnSliderTouchListener createScrollToPositionSliderTouchListener(
      RecyclerView recyclerView) {
    return new OnSliderTouchListener() {
      @Override
      public void onStartTrackingTouch(@NonNull Slider slider) {}

      @Override
      public void onStopTrackingTouch(@NonNull Slider slider) {
        recyclerView.smoothScrollToPosition(((int) slider.getValue()) - 1);
      }
    };
  }
}
