
/*
 * Copyright 2018 The Android Open Source Project
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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main BottomSheet demo for the Catalog app. */
public class BottomSheetMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);

    // Set up BottomSheetDialog
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
    bottomSheetDialog.setContentView(R.layout.cat_bottomsheet_content);
    View bottomSheetInternal = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
    BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(400);
    View button = view.findViewById(R.id.bottomsheet_button);
    button.setOnClickListener(v -> bottomSheetDialog.show());
    Switch fullScreenSwitch = view.findViewById(R.id.cat_fullscreen_switch);

    fullScreenSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          // Calculate window height for fullscreen use
          DisplayMetrics displayMetrics = new DisplayMetrics();
          ((Activity) getContext())
              .getWindowManager()
              .getDefaultDisplay()
              .getMetrics(displayMetrics);
          int windowHeight = displayMetrics.heightPixels;

          View bottomSheetChildView = view.findViewById(R.id.bottom_drawer);
          ViewGroup.LayoutParams params = bottomSheetChildView.getLayoutParams();
          BottomSheetBehavior<View> bottomSheetBehavior =
              BottomSheetBehavior.from(bottomSheetChildView);
          View modalBottomSheetChildView = bottomSheetDialog.findViewById(R.id.bottom_drawer_2);
          ViewGroup.LayoutParams layoutParams = modalBottomSheetChildView.getLayoutParams();
          BottomSheetBehavior<FrameLayout> modalBottomSheetBehavior =
              bottomSheetDialog.getBehavior();
          boolean fitToContents = true;
          float halfExpandedRatio = 0.5f;

          if (params != null && layoutParams != null) {
            if (isChecked) {
              params.height = windowHeight;
              layoutParams.height = windowHeight;
              fitToContents = false;
              halfExpandedRatio = 0.7f;

            } else {
              params.height = (windowHeight * 3 / 5);
              layoutParams.height = (windowHeight * 2 / 3);
            }
            bottomSheetChildView.setLayoutParams(params);
            modalBottomSheetChildView.setLayoutParams(layoutParams);
            bottomSheetBehavior.setFitToContents(fitToContents);
            modalBottomSheetBehavior.setFitToContents(fitToContents);
            bottomSheetBehavior.setHalfExpandedRatio(halfExpandedRatio);
            modalBottomSheetBehavior.setHalfExpandedRatio(halfExpandedRatio);
          }
        });

    TextView dialogText = bottomSheetInternal.findViewById(R.id.bottomsheet_state);
    BottomSheetBehavior.from(bottomSheetInternal)
        .setBottomSheetCallback(createBottomSheetCallback(dialogText));
    TextView bottomSheetText = view.findViewById(R.id.cat_persistent_bottomsheet_state);
    View bottomSheetPersistent = view.findViewById(R.id.bottom_drawer);
    BottomSheetBehavior.from(bottomSheetPersistent)
        .setBottomSheetCallback(createBottomSheetCallback(bottomSheetText));

    return view;
  }

  @LayoutRes
  protected int getDemoContent() {
    return R.layout.cat_bottomsheet_fragment;
  }

  private BottomSheetCallback createBottomSheetCallback(@NonNull TextView text) {
    // Set up BottomSheetCallback
    BottomSheetCallback bottomSheetCallback =
        new BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState) {
              case BottomSheetBehavior.STATE_DRAGGING:
                text.setText(R.string.cat_bottomsheet_state_dragging);
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                text.setText(R.string.cat_bottomsheet_state_expanded);
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                text.setText(R.string.cat_bottomsheet_state_collapsed);
                break;
              case BottomSheetBehavior.STATE_HALF_EXPANDED:
                BottomSheetBehavior<View> bottomSheetBehavior =
                    BottomSheetBehavior.from(bottomSheet);
                text.setText(
                    getString(
                        R.string.cat_bottomsheet_state_half_expanded,
                        bottomSheetBehavior.getHalfExpandedRatio()));
                break;
              default:
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        };
    return bottomSheetCallback;
  }
}
