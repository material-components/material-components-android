/*
 * Copyright 2020 The Android Open Source Project
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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.color.MaterialColors;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;

/** A fragment that displays a collapsing Top App Bar demo for the Catalog app. */
public class TopAppBarCollapsingMultilineDemoFragment extends DemoFragment {

  private SparseIntArray linesMap;
  @ColorInt private int colorPrimary;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);

    linesMap = new SparseIntArray();
    linesMap.put(R.id.maxLines1, 1);
    linesMap.put(R.id.maxLines2, 2);
    linesMap.put(R.id.maxLines3, 3);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_topappbar_collapsing_multiline_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    colorPrimary = MaterialColors.getColor(view, androidx.appcompat.R.attr.colorPrimary);

    DemoUtils.setupClickableContentText(view);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu_maxlines, menu);

    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);

    CollapsingToolbarLayout collapsingToolbarLayout =
        requireView().findViewById(R.id.collapsingtoolbarlayout);
    @SuppressWarnings("RestrictTo")
    int maxLines = collapsingToolbarLayout.getMaxLines();
    for (int i = 0; i < linesMap.size(); i++) {
      int value = linesMap.valueAt(i);
      int itemId = linesMap.keyAt(i);
      MenuItem item = menu.findItem(itemId);
      CharSequence title = getString(R.string.menu_max_lines, value);
      if (maxLines == value) {
        SpannableString spannable = new SpannableString(title);
        spannable.setSpan(new ForegroundColorSpan(colorPrimary), 0, title.length(), 0);
        title = spannable;
      }

      item.setTitle(title);
    }
  }

  @SuppressWarnings("RestrictTo")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    CollapsingToolbarLayout collapsingToolbarLayout =
        requireView().findViewById(R.id.collapsingtoolbarlayout);
    collapsingToolbarLayout.setMaxLines(linesMap.get(item.getItemId(), 1));
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
