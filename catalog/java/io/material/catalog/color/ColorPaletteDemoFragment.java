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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the Color Palette demo for the Catalog app. */
public abstract class ColorPaletteDemoFragment extends DemoFragment {

  private ColorsAdapter adapter;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getColorsLayoutResId(), viewGroup, false /* attachToRoot */);

    RecyclerView recyclerView = (RecyclerView) view;
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new ColorsAdapter(getContext(), getColorsArrayResId());
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new ColorSectionsItemDecoration(getContext(), adapter));

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_colors_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.copy_colors) {
      Context context = requireContext();
      ClipboardManager clipboard =
          (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText("Colors", generateColorsText(adapter));
      clipboard.setPrimaryClip(clip);
      Toast.makeText(context, "Copied colors to clipboard.", Toast.LENGTH_LONG).show();
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  @LayoutRes
  protected abstract int getColorsLayoutResId();

  /**
   * Returns an array of array resources containing a list of colors resource. Each group of colors
   * will be displayed in a list.
   */
  @ArrayRes
  protected abstract int getColorsArrayResId();

  /** View holder class for the header of the color palette. */
  static class ColorHeaderViewHolder extends ViewHolder {

    private final TextView header;

    ColorHeaderViewHolder(ViewGroup parent) {
      super(
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_colors_palette_header, parent, false));

      header = (TextView) itemView;
    }

    void bind(ColorHeaderItem headerItem) {
      header.setText(headerItem.getDisplayName());
      header.setBackgroundResource(headerItem.getBackgroundColorRes());
    }
  }

  /** View holder class for the items of the color palette. */
  static class ColorViewHolder extends ViewHolder {

    private final Context context;
    private final TextView nameView;
    private final TextView descriptionView;

    ColorViewHolder(ViewGroup parent) {
      super(
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_colors_palette_item, parent, false));

      nameView = itemView.findViewById(R.id.name);
      descriptionView = itemView.findViewById(R.id.description);
      context = itemView.getContext();
    }

    void bind(ColorItem colorItem) {
      int value = ContextCompat.getColor(context, colorItem.getColorRes());
      String colorResName = colorItem.getColorResName();
      String resQualifier =
          colorResName.startsWith(ColorHeaderItem.SYSTEM_PREFIX) ? "@android:color/" : "@color/";

      nameView.setText(
          context.getResources().getString(R.string.cat_color_res, resQualifier, colorResName));
      descriptionView.setText(String.format("#%06x", value & 0xFFFFFF));

      int textColor = getTextColor(colorItem);
      nameView.setTextColor(textColor);
      descriptionView.setTextColor(textColor);

      itemView.setBackgroundResource(colorItem.getColorRes());
    }

    @ColorInt
    private int getTextColor(ColorItem colorItem) {
      return ColorDemoUtils.getTextColor(colorItem.getColorValue());
    }
  }

  private String generateColorsText(@NonNull ColorsAdapter adapter) {
    StringBuilder colorsText = new StringBuilder();
    for (ColorAdapterItem item : adapter.getItems()) {
      if (item instanceof ColorHeaderItem) {
        if (colorsText.length() > 0) {
          colorsText.append("\n");
        }
        colorsText.append(((ColorHeaderItem) item).getDisplayName()).append("\n");
      } else if (item instanceof ColorItem) {
        ColorItem colorItem = (ColorItem) item;
        int value = ContextCompat.getColor(getContext(), colorItem.getColorRes());
        String colorResName = colorItem.getColorResName();
        String resQualifier =
            colorResName.startsWith(ColorHeaderItem.SYSTEM_PREFIX) ? "@android:color/" : "@color/";
        colorsText.append(String.format("#%06x", value & 0xFFFFFF)).append("\n");
        colorsText.append(resQualifier).append(colorResName).append("\n");
      }
    }
    return colorsText.toString();
  }
}
