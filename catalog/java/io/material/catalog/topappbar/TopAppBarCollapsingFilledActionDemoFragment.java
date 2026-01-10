/*
 * Copyright 2024 The Android Open Source Project
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;

/**
 * A fragment that displays a medium Collapsing Toolbar Top App Bar with a filled action button demo
 * for the Catalog app.
 */
public class TopAppBarCollapsingFilledActionDemoFragment extends DemoFragment {

  @Override
  @Nullable
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_topappbar_collapsing_filled_action_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    Button actionButton = view.findViewById(R.id.action_button);
    actionButton.setOnClickListener(
        v -> Snackbar.make(v, "Action button is clicked.", Snackbar.LENGTH_SHORT).show());

    DemoUtils.setupClickableContentText(view);

    return view;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
