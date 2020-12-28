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

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;

/** A fragment that displays the main Transition demo for the Catalog app. */
public class TransitionContainerTransformDemoFragment extends DemoFragment
    implements OnBackPressedHandler {

  private static final String END_FRAGMENT_TAG = "END_FRAGMENT_TAG";

  private ContainerTransformConfigurationHelper configurationHelper;

  private final Hold holdTransition = new Hold();

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    configurationHelper = new ContainerTransformConfigurationHelper();
  }

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
      ViewCompat.setTransitionName(target, String.valueOf(id));
      target.setOnClickListener(this::showEndFragment);
    }
  }

  private void showStartFragment() {
    TransitionSimpleLayoutFragment fragment =
        TransitionSimpleLayoutFragment.newInstance(
            R.layout.cat_transition_container_transform_start_fragment,
            "shared_element_fab",
            R.id.start_fab);

    // Add root view as target for the Hold so that the entire view hierarchy is held in place as
    // one instead of each child view individually. Helps keep shadows during the transition.
    holdTransition.addTarget(R.id.start_root);
    fragment.setExitTransition(holdTransition);

    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack("ContainerTransformFragment::start")
        .commit();
  }

  private void showEndFragment(View sharedElement) {
    String transitionName = "shared_element_end_root";

    Fragment fragment =
        TransitionSimpleLayoutFragment.newInstance(
            R.layout.cat_transition_container_transform_end_fragment, transitionName);
    configureTransitions(fragment);

    getChildFragmentManager()
        .beginTransaction()
        .addSharedElement(sharedElement, transitionName)
        .replace(R.id.fragment_container, fragment, END_FRAGMENT_TAG)
        .addToBackStack("ContainerTransformFragment::end")
        .commit();
  }

  private void configureTransitions(Fragment fragment) {
    // For all 3 container layer colors, use colorSurface since this transform can be configured
    // using any fade mode and some of the start views don't have a background and the end view
    // doesn't have a background.
    int colorSurface = MaterialColors.getColor(requireView(), R.attr.colorSurface);

    MaterialContainerTransform enterContainerTransform = buildContainerTransform(true);
    enterContainerTransform.setAllContainerColors(colorSurface);
    fragment.setSharedElementEnterTransition(enterContainerTransform);
    holdTransition.setDuration(enterContainerTransform.getDuration());

    MaterialContainerTransform returnContainerTransform = buildContainerTransform(false);
    returnContainerTransform.setAllContainerColors(colorSurface);
    fragment.setSharedElementReturnTransition(returnContainerTransform);
  }

  private MaterialContainerTransform buildContainerTransform(boolean entering) {
    Context context = requireContext();
    MaterialContainerTransform transform = new MaterialContainerTransform(context, entering);
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
}
