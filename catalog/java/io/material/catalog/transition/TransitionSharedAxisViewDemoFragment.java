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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;
import com.google.android.material.transition.MaterialSharedAxis;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;

/** A fragment that displays the Shared Axis Transition (View) demo for the Catalog app. */
public class TransitionSharedAxisViewDemoFragment extends DemoFragment
    implements OnBackPressedHandler {

  private ViewGroup container;
  private View startView;
  private View endView;

  private SharedAxisHelper sharedAxisHelper;

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_shared_axis_view_fragment, viewGroup, false /* attachToRoot */);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    sharedAxisHelper = new SharedAxisHelper(view.findViewById(R.id.controls_layout));
    container = view.findViewById(R.id.container);
    startView = view.findViewById(R.id.start_view);
    endView = view.findViewById(R.id.end_view);

    sharedAxisHelper.setBackButtonOnClickListener(v -> switchView());
    sharedAxisHelper.setNextButtonOnClickListener(v -> switchView());
  }

  @Override
  public boolean onBackPressed() {
    if (!isStartViewShowing()) {
      switchView();
      return true;
    }
    return false;
  }

  private void switchView() {
    boolean startViewShowing = isStartViewShowing();

    // Change the visibility of the start and end views, animating using a shared axis transition.
    MaterialSharedAxis sharedAxis = createTransition(startViewShowing);
    TransitionManager.beginDelayedTransition(container, sharedAxis);
    startView.setVisibility(startViewShowing ? View.GONE : View.VISIBLE);
    endView.setVisibility(startViewShowing ? View.VISIBLE : View.GONE);

    sharedAxisHelper.updateButtonsEnabled(!startViewShowing);
  }

  private MaterialSharedAxis createTransition(boolean entering) {
    MaterialSharedAxis transition =
        new MaterialSharedAxis(sharedAxisHelper.getSelectedAxis(), entering);

    // Add targets for this transition to explicitly run transitions only on these views. Without
    // targeting, a MaterialSharedAxis transition would be run for every view in the
    // the ViewGroup's layout.
    transition.addTarget(startView);
    transition.addTarget(endView);

    return transition;
  }

  private boolean isStartViewShowing() {
    return startView.getVisibility() == View.VISIBLE;
  }
}
