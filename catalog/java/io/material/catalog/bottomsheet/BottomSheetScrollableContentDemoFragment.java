/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.bottomsheet;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/**
 * A fragment that displays the a BottomSheet demo with vertical scrollable content for the Catalog
 * app.
 */
public class BottomSheetScrollableContentDemoFragment extends DemoFragment {

  private WindowPreferencesManager windowPreferencesManager;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    windowPreferencesManager = new WindowPreferencesManager(getContext());
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);

    // Set up BottomSheetDialog
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
    windowPreferencesManager.applyEdgeToEdgePreference(bottomSheetDialog.getWindow());
    bottomSheetDialog.setContentView(R.layout.cat_bottomsheet_scrollable_content);
    View bottomSheetInternal = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
    BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(400);
    View button = view.findViewById(R.id.bottomsheet_button);
    button.setOnClickListener(v -> bottomSheetDialog.show());
    return view;
  }

  @LayoutRes
  protected int getDemoContent() {
    return R.layout.cat_bottomsheet_scrollable_content_fragment;
  }
}
