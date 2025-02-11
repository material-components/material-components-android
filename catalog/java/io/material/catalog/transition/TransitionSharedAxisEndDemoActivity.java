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

package io.material.catalog.transition;

import io.material.catalog.R;

import static io.material.catalog.transition.TransitionSharedAxisStartDemoActivity.SHARED_AXIS_KEY;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import io.material.catalog.feature.DemoActivity;

/** An Activity that displays the ending Shared Axis Transition demo for the Catalog app. */
public class TransitionSharedAxisEndDemoActivity extends DemoActivity {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_shared_axis_end_activity, viewGroup, false /* attachToRoot */);
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    // Remove the transition name to avoid the super class, DemoActivity, from configuring the
    // window with its own transition setup.
    getWindow().setAllowEnterTransitionOverlap(true);

    int axis = getIntent().getIntExtra(SHARED_AXIS_KEY, MaterialSharedAxis.X);

    MaterialSharedAxis enterTransition = new MaterialSharedAxis(axis, /* forward= */ true);
    enterTransition.addTarget(R.id.end_activity);
    getWindow().setEnterTransition(enterTransition);

    MaterialSharedAxis returnTransition = new MaterialSharedAxis(axis, /* forward= */ false);
    returnTransition.addTarget(R.id.end_activity);
    getWindow().setReturnTransition(returnTransition);

    super.onCreate(bundle);

    SharedAxisHelper sharedAxisHelper = new SharedAxisHelper(findViewById(R.id.controls_layout));
    sharedAxisHelper.setAxisButtonGroupEnabled(false);

    sharedAxisHelper.updateButtonsEnabled(false);
    sharedAxisHelper.setSelectedAxis(axis);

    sharedAxisHelper.setBackButtonOnClickListener(v -> onBackPressed());
  }

  @Override
  protected boolean shouldSetUpContainerTransform() {
    return false;
  }
}
