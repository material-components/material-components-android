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

import static com.google.android.material.bottomsheet.BottomSheetBehavior.getDefaultBottomGradientProtection;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.internal.ViewUtils;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.windowpreferences.WindowPreferencesManager;
import java.util.Collections;

/**
 * A fragment that displays the a BottomSheet demo with vertical scrollable content for the Catalog
 * app.
 */
public class BottomSheetScrollableContentDemoFragment extends DemoFragment {
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);
    View button = view.findViewById(R.id.bottomsheet_button);
    button.setOnClickListener(v -> new BottomSheet().show(getParentFragmentManager(), ""));
    return view;
  }

  @LayoutRes
  protected int getDemoContent() {
    return R.layout.cat_bottomsheet_additional_demo_fragment;
  }

  /** A custom bottom sheet dialog fragment. */
  @SuppressWarnings("RestrictTo")
  public static class BottomSheet extends BottomSheetDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      // Set up BottomSheetDialog
      BottomSheetDialog bottomSheetDialog =
          new BottomSheetDialog(
              getContext(), R.style.ThemeOverlay_Catalog_BottomSheetDialog_Scrollable);
      new WindowPreferencesManager(requireContext())
          .applyEdgeToEdgePreference(bottomSheetDialog.getWindow());
      View content =
          LayoutInflater.from(getContext())
              .inflate(R.layout.cat_bottomsheet_scrollable_content, new FrameLayout(getContext()));
      bottomSheetDialog.setContentView(content);
      bottomSheetDialog.getBehavior().setPeekHeight(400);

      View bottomSheetContent = content.findViewById(R.id.bottom_drawer_2);
      ViewUtils.doOnApplyWindowInsets(
          bottomSheetContent,
          (v, insets, initialPadding) -> {
            // Add the inset in the inner NestedScrollView instead to make the edge-to-edge behavior
            // consistent - i.e., the extra padding will only show at the bottom of all content,
            // i.e.,
            // only when you can no longer scroll down to show more content.
            bottomSheetContent.setPaddingRelative(
                initialPadding.start,
                initialPadding.top,
                initialPadding.end,
                initialPadding.bottom
                    + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
          });
      bottomSheetDialog.setProtections(
          Collections.singletonList(
              getDefaultBottomGradientProtection(requireContext())));
      return bottomSheetDialog;
    }
  }
}
