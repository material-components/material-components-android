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

package io.material.catalog.bottomnav;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.core.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment that displays a bottom nav that uses {@link
 * android.graphics.drawable.AnimatedStateListDrawable} for icons.
 */
public class BottomNavigationAnimatedDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_bottom_navs_animated, viewGroup, false /* attachToRoot */);
    return view;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = super.onCreateView(layoutInflater, viewGroup, bundle);
    CoordinatorLayout coordinatorLayout = view.findViewById(R.id.cat_demo_fragment_container);
    // For unknown reasons, setting this in the xml is cleared out but setting it here takes effect.
    CoordinatorLayout.LayoutParams lp =
        (LayoutParams) coordinatorLayout.getChildAt(0).getLayoutParams();
    lp.gravity = Gravity.BOTTOM;
    ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout, (v, insets) -> insets);
    return view;
  }
}
