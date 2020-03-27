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

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import com.google.android.material.transition.MaterialSharedAxis;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;

/** A fragment that displays the Shared Axis Transition demo for the Catalog app. */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class TransitionSharedAxisDemoFragment extends DemoFragment implements OnBackPressedHandler {

  private static final int LAYOUT_RES_ID_START = R.layout.cat_transition_shared_axis_start_fragment;
  private static final int LAYOUT_RES_ID_END = R.layout.cat_transition_shared_axis_end_fragment;
  private static final SparseIntArray BUTTON_AXIS_MAP = new SparseIntArray();

  static {
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_x, MaterialSharedAxis.X);
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_y, MaterialSharedAxis.Y);
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_z, MaterialSharedAxis.Z);
  }

  private Button backButton;
  private Button nextButton;
  private RadioGroup directionRadioGroup;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_shared_axis_fragment, viewGroup, false /* attachToRoot */);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    backButton = view.findViewById(R.id.back_button);
    nextButton = view.findViewById(R.id.next_button);
    directionRadioGroup = view.findViewById(R.id.radio_button_group_direction);

    replaceFragment(LAYOUT_RES_ID_START);

    backButton.setOnClickListener(v -> replaceFragment(LAYOUT_RES_ID_START));
    nextButton.setOnClickListener(v -> replaceFragment(LAYOUT_RES_ID_END));
  }

  @Override
  public boolean onBackPressed() {
    if (requireView().findViewById(R.id.end_root) != null) {
      replaceFragment(LAYOUT_RES_ID_START);
      return true;
    }
    return false;
  }

  private void replaceFragment(@LayoutRes int layoutResId) {
    boolean entering = layoutResId == LAYOUT_RES_ID_END;

    Fragment fragment = TransitionSimpleLayoutFragment.newInstance(layoutResId);
    // Set the transition as the Fragment's enter transition. This will be used when the fragment
    // is added to the container and re-used when the fragment is removed from the container.
    fragment.setEnterTransition(createTransition(entering));

    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();

    backButton.setEnabled(entering);
    nextButton.setEnabled(!entering);
  }

  private MaterialSharedAxis createTransition(boolean entering) {
    int axis = BUTTON_AXIS_MAP.get(directionRadioGroup.getCheckedRadioButtonId());

    MaterialSharedAxis transition = MaterialSharedAxis.create(axis, entering);

    // Add targets for this transition to explicitly run transitions only on these views. Without
    // targeting, a MaterialSharedAxis transition would be run for every view in the
    // Fragment's layout.
    transition.addTarget(R.id.start_root);
    transition.addTarget(R.id.end_root);

    return transition;
  }
}
