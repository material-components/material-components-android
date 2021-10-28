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

import android.content.Context;
import android.content.res.TypedArray;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import io.material.catalog.color.ColorPaletteDemoFragment.ColorHeaderViewHolder;
import io.material.catalog.color.ColorPaletteDemoFragment.ColorViewHolder;
import java.util.ArrayList;
import java.util.List;

/** Adapter class for the colors palette. */
public class ColorsAdapter extends Adapter<ViewHolder> {

  // The header indicator of a color.
  public static final int VIEW_TYPE_HEADER = 0;

  private static final int VIEW_TYPE_COLOR = 1;
  private final List<ColorAdapterItem> items = new ArrayList<>();
  private final Context context;

  public ColorsAdapter(@NonNull Context context, @NonNull @ArrayRes int colorItems) {
    this.context = context;
    TypedArray colorsArray = context.getResources().obtainTypedArray(colorItems);

    for (int i = 0; i < colorsArray.length(); i++) {
      List<ColorItem> colors = getColorsFromArrayResource(colorsArray.getResourceId(i, 0));
      items.add(new ColorHeaderItem(context, colors));
      items.addAll(colors);
    }

    colorsArray.recycle();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case VIEW_TYPE_HEADER:
        return new ColorHeaderViewHolder(parent);
      case VIEW_TYPE_COLOR:
        return new ColorViewHolder(parent);
      default:
        // The default case should never be reached.
        return null;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
    switch (getItemViewType(position)) {
      case VIEW_TYPE_HEADER:
        ColorHeaderItem header = (ColorHeaderItem) items.get(position);
        ((ColorHeaderViewHolder) viewHolder).bind(header);
        break;
      case VIEW_TYPE_COLOR:
        ColorItem item = (ColorItem) items.get(position);
        ((ColorViewHolder) viewHolder).bind(item);
        break;
      default: // fall out
    }
  }

  @Override
  public int getItemViewType(int position) {
    return (items.get(position) instanceof ColorHeaderItem) ? VIEW_TYPE_HEADER : VIEW_TYPE_COLOR;
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  /** Returns the list of {@link ColorAdapterItem}s in the adapter. */
  @NonNull
  public List<ColorAdapterItem> getItems() {
    return items;
  }

  private List<ColorItem> getColorsFromArrayResource(@ArrayRes int arrayRes) {
    List<ColorItem> colors = new ArrayList<>();
    TypedArray colorsArray = context.getResources().obtainTypedArray(arrayRes);

    for (int i = 0; i < colorsArray.length(); i++) {
      int color = colorsArray.getResourceId(i, 0);
      colors.add(new ColorItem(context, color));
    }
    colorsArray.recycle();
    return colors;
  }
}
