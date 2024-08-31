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

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import io.material.catalog.feature.DemoActivity;

/** An Activity that displays the starting Shared Axis Transition demo for the Catalog app. */
public class TransitionSharedAxisStartDemoActivity extends DemoActivity {

  private SharedAxisHelper sharedAxisHelper;

  static final String SHARED_AXIS_KEY = "activity_shared_axis_axis";

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_shared_axis_start_activity, viewGroup, false /* attachToRoot */);
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    getWindow().setAllowReturnTransitionOverlap(true);

    super.onCreate(bundle);

    sharedAxisHelper = new SharedAxisHelper(findViewById(R.id.controls_layout));

    sharedAxisHelper.setNextButtonOnClickListener(v -> navigateToEndActivity());
  }

  @Override
  protected boolean shouldSetUpContainerTransform() {
    return false;
  }

  private void navigateToEndActivity() {
    int axis = sharedAxisHelper.getSelectedAxis();

    MaterialSharedAxis exitTransition = new MaterialSharedAxis(axis, /* forward= */ true);
    exitTransition.addTarget(R.id.start_activity);
    getWindow().setExitTransition(exitTransition);

    MaterialSharedAxis reenterTransition = new MaterialSharedAxis(axis, /* forward= */ false);
    reenterTransition.addTarget(R.id.start_activity);
    getWindow().setReenterTransition(reenterTransition);

    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
    Intent intent = new Intent(this, TransitionSharedAxisEndDemoActivity.class);
    intent.putExtra(SHARED_AXIS_KEY, axis);
    startActivity(intent, options.toBundle());
  }
}
