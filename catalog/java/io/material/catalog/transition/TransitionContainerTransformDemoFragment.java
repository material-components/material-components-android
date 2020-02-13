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
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;

/** A fragment that displays the main Transition demo for the Catalog app. */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class TransitionContainerTransformDemoFragment extends DemoFragment
    implements OnBackPressedHandler {

  private static final String END_FRAGMENT_TAG = "END_FRAGMENT_TAG";

  private final ContainerTransformConfigurationHelper configurationHelper =
      getContainerTransformConfigurationHelper();

  private final Hold holdTransition = new Hold();

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_container_transform_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    getChildFragmentManager()
        .registerFragmentLifecycleCallbacks(
            new FragmentLifecycleCallbacks() {
              @Override
              public void onFragmentViewCreated(
                  @NonNull FragmentManager fragmentManager,
                  @NonNull Fragment fragment,
                  @NonNull View view,
                  @Nullable Bundle bundle) {
                addTransitionableTarget(view, R.id.start_fab);
                addTransitionableTarget(view, R.id.single_line_list_item);
                addTransitionableTarget(view, R.id.vertical_card_item);
                addTransitionableTarget(view, R.id.horizontal_card_item);
                addTransitionableTarget(view, R.id.grid_card_item);
                addTransitionableTarget(view, R.id.grid_tall_card_item);
              }
            },
            true);
    showStartFragment();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.configure_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.configure) {
      configurationHelper.showConfigurationChooser(
          requireContext(),
          dialog -> {
            // Apply the latest transition configurations to the end fragment that may already exist
            Fragment endFragment = getChildFragmentManager().findFragmentByTag(END_FRAGMENT_TAG);
            if (endFragment != null) {
              configureTransitions(endFragment);
            }
          });
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  private void addTransitionableTarget(View view, @IdRes int id) {
    View target = view.findViewById(id);
    if (target != null) {
      target.setOnClickListener(this::showEndFragment);
    }
  }

  private void showStartFragment() {
    TransitionSimpleLayoutFragment fragment =
        TransitionSimpleLayoutFragment.newInstance(
            R.layout.cat_transition_container_transform_start_fragment);
    fragment.setExitTransition(holdTransition);
    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack("ContainerTransformFragment::start")
        .commit();
  }

  private void showEndFragment(View sharedElement) {
    Fragment fragment =
        TransitionSimpleLayoutFragment.newInstance(
            R.layout.cat_transition_container_transform_end_fragment);
    configureTransitions(fragment);

    getChildFragmentManager()
        .beginTransaction()
        .addSharedElement(sharedElement, "shared_element_end_root")
        .replace(R.id.fragment_container, fragment, END_FRAGMENT_TAG)
        .addToBackStack("ContainerTransformFragment::end")
        .commit();
  }

  private void configureTransitions(Fragment fragment) {
    MaterialContainerTransform enterContainerTransform = buildContainerTransform(true);
    fragment.setSharedElementEnterTransition(enterContainerTransform);
    holdTransition.setDuration(enterContainerTransform.getDuration());

    MaterialContainerTransform returnContainerTransform = buildContainerTransform(false);
    fragment.setSharedElementReturnTransition(returnContainerTransform);
  }

  private MaterialContainerTransform buildContainerTransform(boolean entering) {
    MaterialContainerTransform transform = new MaterialContainerTransform(requireContext());
    transform.setDrawingViewId(entering ? R.id.end_root : R.id.start_root);
    configurationHelper.configure(transform, entering);
    return transform;
  }

  @Override
  public boolean onBackPressed() {
    if (getView().findViewById(R.id.end_root) != null) {
      getChildFragmentManager().popBackStack();
      return true;
    }
    return false;
  }

  protected ContainerTransformConfigurationHelper getContainerTransformConfigurationHelper() {
    return new ContainerTransformConfigurationHelper();
  }
}
