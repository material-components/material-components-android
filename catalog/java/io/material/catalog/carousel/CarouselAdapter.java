/*
 * Copyright 2022 The Android Open Source Project
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

import io.material.catalog.R;

import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

/** An adapter that displays {@link CarouselItem}s for a Carousel. */
class CarouselAdapter extends ListAdapter<CarouselItem, CarouselItemViewHolder> {

  private static final DiffUtil.ItemCallback<CarouselItem> DIFF_CALLBACK =
      new DiffUtil.ItemCallback<CarouselItem>() {
        @Override
        public boolean areItemsTheSame(
            @NonNull CarouselItem oldItem, @NonNull CarouselItem newItem) {
          // User properties may have changed if reloaded from the DB, but ID is fixed
          return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(
            @NonNull CarouselItem oldItem, @NonNull CarouselItem newItem) {
          return false;
        }
      };

  private final CarouselItemListener listener;
  @LayoutRes private final int itemLayoutRes;

  CarouselAdapter(CarouselItemListener listener) {
    this(listener, R.layout.cat_carousel_item);
  }

  CarouselAdapter(CarouselItemListener listener, @LayoutRes int itemLayoutRes) {
    super(DIFF_CALLBACK);
    this.listener = listener;
    this.itemLayoutRes = itemLayoutRes;
  }

  @NonNull
  @Override
  public CarouselItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
    return new CarouselItemViewHolder(
        LayoutInflater.from(viewGroup.getContext())
            .inflate(itemLayoutRes, viewGroup, false), listener);
  }

  @Override
  public void onBindViewHolder(@NonNull CarouselItemViewHolder carouselItemViewHolder, int pos) {
    carouselItemViewHolder.bind(getItem(pos));
    carouselItemViewHolder.itemView.setOnHoverListener(
        (v, event) -> {
          switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
              v.setAlpha(0.5f);
              break;
            case MotionEvent.ACTION_HOVER_EXIT:
              v.setAlpha(1f);
              break;
            default: // fall out
          }
          return false;
        });
  }

}
