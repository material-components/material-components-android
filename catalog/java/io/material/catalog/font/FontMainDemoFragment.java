/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.font;

import io.material.catalog.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the Font Typographic Styles demo for the Catalog app. */
public class FontMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_font_styles_fragment, viewGroup, false /* attachToRoot */);

    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    recyclerView.setAdapter(new FontStyleAdapter(getContext()));
    ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (view1, windowInsetsCompat) -> {
      recyclerView.setClipToPadding(windowInsetsCompat.getSystemWindowInsetBottom() == 0);
      recyclerView.setPadding(
          recyclerView.getPaddingLeft(),
          recyclerView.getPaddingTop(),
          recyclerView.getPaddingRight(),
          windowInsetsCompat.getSystemWindowInsetBottom());

      return windowInsetsCompat;
    });

    return view;
  }

  /**
   * Returns an array resource containing a list of theme attributes that each reference a text
   * appearance style. Each style will be displayed in a list.
   */
  @ArrayRes
  protected int getFontStyles() {
    return R.array.cat_font_styles_array;
  }

  @ArrayRes
  protected int getFontStyleNames() {
    return R.array.cat_font_style_names_array;
  }

  protected String convertFontFamilyToDescription(String fontFamily) {
    if (fontFamily == null) {
      return "Regular";
    }
    switch (fontFamily.toLowerCase()) {
      case "sans-serif-light":
        return "Light";
      case "sans-serif":
        return "Regular";
      case "sans-serif-medium":
        return "Medium";
      default:
        return fontFamily;
    }
  }

  private class FontStyleAdapter extends Adapter<FontStyleViewHolder> {

    private final List<Integer> styles = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    private final List<String> attributeNames = new ArrayList<>();

    public FontStyleAdapter(Context context) {
      TypedArray stylesArray = getResources().obtainTypedArray(getFontStyles());
      TypedArray namesArray = getResources().obtainTypedArray(getFontStyleNames());

      TypedValue value = new TypedValue();
      for (int i = 0; i < stylesArray.length(); i++) {
        // 1. Get the attribute from the array: ?attr/textAppearanceHeadline1
        stylesArray.getValue(i, value);
        int attribute = value.data;

        // 2. Get the style from the attribute: @style/TextAppearance.MaterialComponents.Headline1
        TypedArray a = context.obtainStyledAttributes(new int[] {attribute});
        int style = a.getResourceId(0, 0);
        a.recycle();

        styles.add(style);
        names.add(namesArray.getString(i));
        attributeNames.add(context.getResources().getResourceEntryName(attribute));
      }
      stylesArray.recycle();
      namesArray.recycle();
    }

    @NonNull
    @Override
    public FontStyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new FontStyleViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FontStyleViewHolder viewHolder, int i) {
      viewHolder.bind(styles.get(i), names.get(i), attributeNames.get(i));
    }

    @Override
    public int getItemCount() {
      return styles.size();
    }
  }

  private class FontStyleViewHolder extends ViewHolder {
    private final TextView nameView;
    private final TextView descriptionView;
    private final ImageView infoView;

    private String attributeName;

    public FontStyleViewHolder(ViewGroup parent) {
      super(
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_font_styles_item, parent, false));

      nameView = itemView.findViewById(R.id.name);
      descriptionView = itemView.findViewById(R.id.description);
      infoView = itemView.findViewById(R.id.info);

      infoView.setOnClickListener(
          view ->
              Snackbar.make(
                      view,
                      view.getContext().getString(R.string.cat_font_style_message, attributeName),
                      Snackbar.LENGTH_LONG)
                  .show());
    }

    public void bind(@StyleRes int style, String name, String attributeName) {
      this.attributeName = attributeName;

      nameView.setText(name);
      descriptionView.setText(createDescription(name, style));

      TextViewCompat.setTextAppearance(nameView, style);
    }

    @SuppressWarnings("RestrictTo")
    private String createDescription(String name, @StyleRes int style) {
      TextAppearance textAppearance = new TextAppearance(itemView.getContext(), style);
      return name
          + " - "
          + convertFontFamilyToDescription(textAppearance.fontFamily)
          + " "
          + pxToSp(textAppearance.textSize)
          + "sp";
    }

    private int pxToSp(float px) {
      return (int) (px / itemView.getResources().getDisplayMetrics().scaledDensity);
    }
  }
}
