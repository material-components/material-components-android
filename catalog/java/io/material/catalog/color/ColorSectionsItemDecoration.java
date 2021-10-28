/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.color;

import io.material.catalog.R;

import android.content.Context;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * Allows the application to add a special drawing and layout offset to specific item views from the
 * adapter's data set.
 */
public class ColorSectionsItemDecoration extends ItemDecoration {

  private final int space;
  private final ColorsAdapter adapter;

  public ColorSectionsItemDecoration(@NonNull Context context, @NonNull ColorsAdapter adapter) {
    this.space = context.getResources().getDimensionPixelSize(R.dimen.cat_colors_header_space);
    this.adapter = adapter;
  }

  @NonNull
  @Override
  public void getItemOffsets(
      @NonNull Rect rect,
      @NonNull View view,
      @NonNull RecyclerView recyclerView,
      @NonNull State state) {
    super.getItemOffsets(rect, view, recyclerView, state);

    // Add space above each header except the first.
    int position = recyclerView.getChildAdapterPosition(view);
    if (position != 0 && adapter.getItemViewType(position) == ColorsAdapter.VIEW_TYPE_HEADER) {
      rect.set(0, space, 0, 0);
    }
  }
}
