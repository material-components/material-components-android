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

package io.material.catalog.navigationrail;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment that displays a bottom nav that uses {@link
 * android.graphics.drawable.AnimatedStateListDrawable} for icons.
 */
public class NavigationRailAnimatedDemoFragment extends DemoFragment {

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View v = layoutInflater.inflate(
        R.layout.cat_navigation_rail_animated, viewGroup, false /* attachToRoot */);
    Configuration config = getResources().getConfiguration();
    View compactMessage = v.findViewById(R.id.cat_navigation_rail_compact_msg);
    if (config.smallestScreenWidthDp <= 600) { // 600dp and below is classified as compact
      compactMessage.setVisibility(View.VISIBLE);
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else {
      getActivity().setRequestedOrientation(config.orientation);
    }
    return v;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }
}
