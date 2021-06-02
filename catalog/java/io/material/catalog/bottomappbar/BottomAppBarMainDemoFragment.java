/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.bottomappbar;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import io.material.catalog.feature.OnBackPressedHandler;
import io.material.catalog.preferences.CatalogPreferencesHelper;
import java.util.List;

/** A fragment that displays the main Bottom App Bar demos for the Catalog app. */
public class BottomAppBarMainDemoFragment extends DemoFragment implements OnBackPressedHandler {

  protected BottomAppBar bar;
  protected CoordinatorLayout coordinatorLayout;
  protected FloatingActionButton fab;

  @Nullable private CatalogPreferencesHelper catalogPreferencesHelper;
  private BottomSheetBehavior<View> bottomDrawerBehavior;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);

    // The preferences helper is used in an adhoc way with the toolbar since the BottomAppBar is
    // set as the action bar.
    catalogPreferencesHelper = new CatalogPreferencesHelper(getParentFragmentManager());
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.demo_primary, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    showSnackbar(menuItem.getTitle());
    return true;
  }

  @LayoutRes
  public int getBottomAppBarContent() {
    return R.layout.cat_bottomappbar_fragment;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getBottomAppBarContent(), viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    toolbar.setTitle(getDefaultDemoTitle());
    catalogPreferencesHelper.onCreateOptionsMenu(
        toolbar.getMenu(), getActivity().getMenuInflater());
    toolbar.setOnMenuItemClickListener(catalogPreferencesHelper::onOptionsItemSelected);
    toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

    coordinatorLayout = view.findViewById(R.id.coordinator_layout);
    bar = view.findViewById(R.id.bar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(bar);

    setUpBottomDrawer(view);

    fab = view.findViewById(R.id.fab);
    fab.setOnClickListener(v -> showSnackbar(fab.getContentDescription()));
    NavigationView navigationView = view.findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        item -> {
          showSnackbar(item.getTitle());
          return false;
        });

    setUpDemoControls(view);
    setUpBottomAppBarShapeAppearance();

    return view;
  }

  private void setUpDemoControls(@NonNull View view) {
    // Set up generic settings for toggle button groups.
    List<MaterialButtonToggleGroup> toggleButtonGroups =
        DemoUtils.findViewsWithType(view, MaterialButtonToggleGroup.class);

    for (MaterialButtonToggleGroup toggleGroup : toggleButtonGroups) {
      toggleGroup.setSingleSelection(true);
      toggleGroup.setSelectionRequired(true);
    }

    // Set up FAB visibility mode toggle buttons.
    MaterialButton showFabButton = view.findViewById(R.id.show_fab_button);
    MaterialButton hideFabButton = view.findViewById(R.id.hide_fab_button);

    if (fab.getVisibility() == View.VISIBLE) {
      showFabButton.setChecked(true);
    } else {
      hideFabButton.setChecked(true);
    }

    showFabButton.setOnClickListener(v -> fab.show());
    hideFabButton.setOnClickListener(v -> fab.hide());

    // Set up FAB alignment mode toggle buttons.
    MaterialButton centerButton = view.findViewById(R.id.fab_position_button_center);
    MaterialButton endButton = view.findViewById(R.id.fab_position_button_end);

    if (bar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_CENTER) {
      centerButton.setChecked(true);
    } else {
      endButton.setChecked(true);
    }

    centerButton.setOnClickListener(
        v -> {
          bar.setFabAlignmentModeAndReplaceMenu(
              BottomAppBar.FAB_ALIGNMENT_MODE_CENTER, R.menu.demo_primary);
        });
    endButton.setOnClickListener(
        v ->
            bar.setFabAlignmentModeAndReplaceMenu(
                BottomAppBar.FAB_ALIGNMENT_MODE_END, R.menu.demo_primary_alternate));

    // Set up FAB animation mode toggle buttons.
    MaterialButton slideButton = view.findViewById(R.id.fab_animation_mode_button_slide);
    MaterialButton scaleButton = view.findViewById(R.id.fab_animation_mode_button_scale);

    if (bar.getFabAnimationMode() == BottomAppBar.FAB_ANIMATION_MODE_SCALE) {
      scaleButton.setChecked(true);
    } else {
      slideButton.setChecked(true);
    }

    scaleButton.setOnClickListener(
        v -> bar.setFabAnimationMode(BottomAppBar.FAB_ANIMATION_MODE_SCALE));
    slideButton.setOnClickListener(
        v -> bar.setFabAnimationMode(BottomAppBar.FAB_ANIMATION_MODE_SLIDE));

    // Set up hide on scroll switch.
    SwitchMaterial barScrollSwitch = view.findViewById(R.id.bar_scroll_switch);
    barScrollSwitch.setChecked(bar.getHideOnScroll());
    barScrollSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> bar.setHideOnScroll(isChecked));
  }

  @Override
  public boolean onBackPressed() {
    if (bottomDrawerBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
      bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      return true;
    }
    return false;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  private void setUpBottomAppBarShapeAppearance() {
    ShapeAppearanceModel fabShapeAppearanceModel = fab.getShapeAppearanceModel();
    boolean cutCornersFab =
        fabShapeAppearanceModel.getBottomLeftCorner() instanceof CutCornerTreatment
            && fabShapeAppearanceModel.getBottomRightCorner() instanceof CutCornerTreatment;

    BottomAppBarTopEdgeTreatment topEdge =
        cutCornersFab
            ? new BottomAppBarCutCornersTopEdge(
                bar.getFabCradleMargin(),
                bar.getFabCradleRoundedCornerRadius(),
                bar.getCradleVerticalOffset())
            : new BottomAppBarTopEdgeTreatment(
                bar.getFabCradleMargin(),
                bar.getFabCradleRoundedCornerRadius(),
                bar.getCradleVerticalOffset());

    MaterialShapeDrawable babBackground = (MaterialShapeDrawable) bar.getBackground();
    babBackground.setShapeAppearanceModel(
        babBackground.getShapeAppearanceModel().toBuilder().setTopEdge(topEdge).build());
  }

  protected void setUpBottomDrawer(View view) {
    View bottomDrawer = coordinatorLayout.findViewById(R.id.bottom_drawer);
    bottomDrawerBehavior = BottomSheetBehavior.from(bottomDrawer);
    bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    bar.setNavigationOnClickListener(
        v -> bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED));
    bar.setNavigationIcon(R.drawable.ic_drawer_menu_24px);
    bar.replaceMenu(R.menu.demo_primary);
  }

  private void showSnackbar(CharSequence text) {
    Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_SHORT)
        .setAnchorView(fab.getVisibility() == View.VISIBLE ? fab : bar)
        .show();
  }
}
