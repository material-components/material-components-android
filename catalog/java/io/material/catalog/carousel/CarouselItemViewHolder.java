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

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;

/** An {@link RecyclerView.ViewHolder} that displays an item inside a Carousel. */
class CarouselItemViewHolder extends RecyclerView.ViewHolder {

  private final ImageView imageView;
  private final CarouselItemListener listener;

  CarouselItemViewHolder(@NonNull View itemView, CarouselItemListener listener) {
    super(itemView);
    imageView = itemView.findViewById(R.id.carousel_image_view);
    this.listener = listener;
  }

  void bind(CarouselItem item) {
    Glide.with(imageView.getContext()).load(item.getDrawableRes()).centerCrop().into(imageView);
    imageView.setContentDescription(imageView.getResources().getString(item.getContentDescRes()));
    itemView.setOnClickListener(v -> listener.onItemClicked(item, getBindingAdapterPosition()));
  }
}
