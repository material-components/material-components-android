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
package io.material.catalog.bottomsheet;

import io.material.catalog.R;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/**
 * A fragment that displays the a BottomSheet demo with multiple scrollable content for the Catalog
 * app.
 */
public class BottomSheetMultipleScrollableContentDemoFragment extends DemoFragment {
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
    private BottomSheetViewPagerAdapter adapter;

    /**
     * A simple fragment containing scrollable content, used as pages within the {@link ViewPager}.
     */
    public static class ScrollableDemoFragment extends Fragment {
      @Nullable
      @Override
      public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cat_bottomsheet_viewpager_page_content, container, false);
      }

      @Override
      public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {}
    }

    private class BottomSheetViewPagerAdapter extends FragmentStatePagerAdapter {
      BottomSheetViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
      }

      @Override
      @NonNull
      public Fragment getItem(int i) {
        return new ScrollableDemoFragment();
      }

      @Override
      public int getCount() {
        return 5;
      }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      // Set up BottomSheetDialog
      adapter = new BottomSheetViewPagerAdapter(getChildFragmentManager());
      BottomSheetDialog bottomSheetDialog =
          new BottomSheetDialog(
              getContext(), R.style.ThemeOverlay_Catalog_BottomSheetDialog_MultiScrollable);
      new WindowPreferencesManager(requireContext())
          .applyEdgeToEdgePreference(bottomSheetDialog.getWindow());
      View content =
          LayoutInflater.from(getContext())
              .inflate(R.layout.cat_bottomsheet_viewpager_content, new FrameLayout(getContext()));
      bottomSheetDialog.setContentView(content);
      bottomSheetDialog.getBehavior().setPeekHeight(400);
      ViewPager viewPager = content.findViewById(R.id.cat_bottom_sheet_viewpager);
      viewPager.setAdapter(adapter);
      return bottomSheetDialog;
    }
  }
}
