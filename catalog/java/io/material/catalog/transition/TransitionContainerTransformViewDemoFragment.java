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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.transition.TransitionManager;
import com.google.android.material.transition.MaterialContainerTransform;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the View container transform transition demos for the Catalog app. */
public class TransitionContainerTransformViewDemoFragment extends DemoFragment {

  @Nullable private View startCard;
  private View startFab;

  private View contactCard;
  @Nullable private View endView;
  private View expandedCard;

  private FrameLayout root;

  private ContainerTransformConfigurationHelper configurationHelper;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    configurationHelper = new ContainerTransformConfigurationHelper();
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_transition_container_transform_view_fragment, viewGroup, false);
    root = view.findViewById(R.id.root);

    startFab = view.findViewById(R.id.start_fab);
    expandedCard = view.findViewById(R.id.expanded_card);
    contactCard = view.findViewById(R.id.contact_card);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    addTransitionableTarget(view, R.id.start_fab);
    addTransitionableTarget(view, R.id.single_line_list_item);
    addTransitionableTarget(view, R.id.vertical_card_item);
    addTransitionableTarget(view, R.id.horizontal_card_item);
    addTransitionableTarget(view, R.id.grid_card_item);
    addTransitionableTarget(view, R.id.grid_tall_card_item);
    addTransitionableTarget(view, R.id.expanded_card);
    addTransitionableTarget(view, R.id.contact_card);
  }

  private void addTransitionableTarget(@NonNull View view, @IdRes int id) {
    View target = view.findViewById(id);
    if (target != null) {
      ViewCompat.setTransitionName(target, String.valueOf(id));
      if (id == R.id.expanded_card || id == R.id.contact_card) {
        target.setOnClickListener(this::showStartView);
      } else {
        target.setOnClickListener(this::showEndView);
      }
    }
  }

  private void showEndView(View startView) {
    if (startView.getId() == R.id.start_fab) {
      this.endView = contactCard;
    } else {
      // Save the startView reference as the startCard that triggered the transition in order to
      // know which card to transition into during the return transition.
      this.startCard = startView;
      this.endView = expandedCard;
    }

    // Construct a container transform transition between two views.
    MaterialContainerTransform transition = buildContainerTransform(true);
    transition.setStartView(startView);
    transition.setEndView(endView);

    // Add a single target to stop the container transform from running on both the start
    // and end view.
    transition.addTarget(endView);

    // Trigger the container transform transition.
    TransitionManager.beginDelayedTransition(root, transition);
    startView.setVisibility(View.INVISIBLE);
    endView.setVisibility(View.VISIBLE);

    View finalEndView = this.endView;

    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(/* enabled= */ true) {
              @Override
              public void handleOnBackPressed() {
                showStartView(finalEndView);
                remove();
              }
            });
  }

  private void showStartView(View endView) {
    View startView = endView.getId() == R.id.contact_card ? startFab : startCard;

    // Construct a container transform transition between two views.
    MaterialContainerTransform transition = buildContainerTransform(false);
    transition.setStartView(endView);
    transition.setEndView(startView);

    // Add a single target to stop the container transform from running on both the start
    // and end view.
    transition.addTarget(startView);

    // Trigger the container transform transition.
    TransitionManager.beginDelayedTransition(root, transition);
    startView.setVisibility(View.VISIBLE);
    endView.setVisibility(View.INVISIBLE);
  }

  @NonNull
  private MaterialContainerTransform buildContainerTransform(boolean entering) {
    Context context = requireContext();
    MaterialContainerTransform transform = new MaterialContainerTransform(context, entering);
    transform.setScrimColor(Color.TRANSPARENT);
    transform.setDrawingViewId(root.getId());
    configurationHelper.configure(transform, entering);
    return transform;
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
          requireContext(), dialog -> buildContainerTransform(true));
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }
}
