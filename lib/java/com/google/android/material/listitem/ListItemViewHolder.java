/*
 * Copyright 2025 The Android Open Source Project
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
package com.google.android.material.listitem;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

/**
 * A flexible {@link ViewHolder} provides utility methods for binding a {@link ListItemLayout} based
 * on an item count and position. ListItemViewHolder expects a {@link ListItemLayout} as the root
 * itemView, or as a direct child of the itemView.
 */
public class ListItemViewHolder extends ViewHolder {

  @NonNull private final ListItemLayout listItemLayout;

  public ListItemViewHolder(@NonNull View itemView) {
    super(itemView);
    listItemLayout = findListItemLayout();
  }

  @NonNull
  private ListItemLayout findListItemLayout() {
    if (itemView instanceof ListItemLayout) {
      return (ListItemLayout) itemView;
    } else if (itemView instanceof ViewGroup) {
      int childCount = ((ViewGroup) itemView).getChildCount();
      for (int i = 0; i < childCount; i++) {
        View child = ((ViewGroup) itemView).getChildAt(i);
        if (child instanceof ListItemLayout) {
          return (ListItemLayout) child;
        }
      }
    }
    throw new IllegalStateException(
        "Didn't find ListItemLayout in root itemView or among itemView's children.");
  }

  /**
   * Binds the corresponding {@link ListItemLayout} to the adapter position by calling {@link
   * ListItemLayout#updateAppearance}.
   */
  public void bind() {
    int position = getBindingAdapterPosition();
    int itemCount = getBindingAdapter().getItemCount();
    bind(position, itemCount);
  }

  /**
   * Binds the corresponding {@link ListItemLayout} according given position and item count.
   * If there are several sections in the list, the position and item count given should be relative
   * to its section.
   */
  public void bind(int position, int itemCount) {
    if (position == RecyclerView.NO_POSITION || itemCount == 0) {
      return;
    }
    listItemLayout.updateAppearance(position, itemCount);
  }
}
