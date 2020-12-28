/*
 * Copyright 2019 The Android Open Source Project
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
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialFade;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the Fade Transition demo for the Catalog app. */
public class TransitionFadeDemoFragment extends DemoFragment {

  private static final long DURATION_ENTER = 150;
  private static final long DURATION_RETURN = 84;

  private Button fadeButton;
  private FloatingActionButton fadeFab;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_transition_fade_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    fadeButton = view.findViewById(R.id.fade_button);
    fadeFab = view.findViewById(R.id.fade_fab);

    fadeButton.setOnClickListener(v -> toggleFabVisibilityWithTransition());
  }

  private void toggleFabVisibilityWithTransition() {
    ViewGroup sceneRoot = (ViewGroup) requireView();

    boolean entering = fadeFab.getVisibility() == View.GONE;
    MaterialFade materialFade = new MaterialFade();
    materialFade.setDuration(entering ? DURATION_ENTER : DURATION_RETURN);
    TransitionManager.beginDelayedTransition(sceneRoot, materialFade);
    fadeFab.setVisibility(entering ? View.VISIBLE : View.GONE);
    fadeButton.setText(
        entering
            ? R.string.cat_transition_fade_button_hide_fab
            : R.string.cat_transition_fade_button_show_fab);
  }
}
