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

package io.material.catalog.textfield;

import io.material.catalog.R;

import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowInsetsCompat;

/** A fragment that displays the main text field demos for the Catalog app. */
public class TextFieldMainDemoFragment extends TextFieldDemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(getTextFieldContent(), viewGroup, false /* attachToRoot */);
  }

  @Override
  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_content;
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    requireActivity().getWindow().setDecorFitsSystemWindows(false);
    final View root = requireView().findViewById(R.id.content_root);
    root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
      @Override
      public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        Insets inset = insets.getInsets(WindowInsetsCompat.Type.ime());
//        view.setPadding(0, 0, 0, inset.bottom);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) root.getLayoutParams();
        params.bottomMargin = params.bottomMargin + inset.bottom/160;
        root.setLayoutParams(params);
        return insets;
      }
    });
  }
}
