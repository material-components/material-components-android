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

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import androidx.activity.BackEventCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import io.material.catalog.preferences.CatalogPreferencesHelper;
import java.util.List;

/** A fragment that displays the main Bottom App Bar demos for the Catalog app. */
public class BottomAppBarMainDemoFragment extends DemoFragment {

  private AccessibilityManager am;

  private final OnBackPressedCallback bottomDrawerOnBackPressedCallback =
      new OnBackPressedCallback(/* enabled= */ false) {
        @Override
        public void handleOnBackStarted(@NonNull BackEventCompat backEvent) {
          bottomDrawerBehavior.startBackProgress(backEvent);
        }

        @Override
        public void handleOnBackProgressed(@NonNull BackEventCompat backEvent) {
          bottomDrawerBehavior.updateBackProgress(backEvent);
        }

        @Override
        public void handleOnBackPressed() {
          bottomDrawerBehavior.handleBackInvoked();
        }

        @Override
        public void handleOnBackCancelled() {
          bottomDrawerBehavior.cancelBackProgress();
        }
      };

  protected BottomAppBar bar;
  protected View barNavView;
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
    menuInflater.inflate(R.menu.demo_primary_alternate, menu);
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
    LinearLayout content = view.findViewById(R.id.bottomappbar_content);
    bar = view.findViewById(R.id.bar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(bar);
    barNavView = bar.getChildAt(0);

    setUpBottomDrawer(view);

    fab = view.findViewById(R.id.fab);
    fab.setOnClickListener(v -> showSnackbar(fab.getContentDescription()));
    NavigationView navigationView = view.findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        item -> {
          showSnackbar(item.getTitle());
          return false;
        });

    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      am = getContext().getSystemService(AccessibilityManager.class);
      if (am != null && am.isTouchExplorationEnabled()) {
        bar.post(() -> content.setPadding(0, content.getPaddingTop(), 0, bar.getMeasuredHeight()));
      }
    }

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

    // Set up hide on scroll switch.
    MaterialSwitch barScrollSwitch = view.findViewById(R.id.bar_scroll_switch);
    barScrollSwitch.setChecked(bar.getHideOnScroll());
    barScrollSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (am != null && am.isTouchExplorationEnabled()) {
            bar.setHideOnScroll(false);
          } else {
            bar.setHideOnScroll(isChecked);
          }
        });
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
    bottomDrawerBehavior.setUpdateImportantForAccessibilityOnSiblings(true);
    bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    bottomDrawer.post(() -> updateBackHandlingEnabled(bottomDrawerBehavior.getState()));
    bottomDrawerBehavior.addBottomSheetCallback(
        new BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            updateBackHandlingEnabled(newState);

            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
              barNavView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(this, bottomDrawerOnBackPressedCallback);

    bar.setNavigationOnClickListener(
        v -> bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED));
  }

  private void updateBackHandlingEnabled(int state) {
    switch (state) {
      case BottomSheetBehavior.STATE_EXPANDED:
      case BottomSheetBehavior.STATE_HALF_EXPANDED:
      case BottomSheetBehavior.STATE_COLLAPSED:
        bottomDrawerOnBackPressedCallback.setEnabled(true);
        break;
      case BottomSheetBehavior.STATE_HIDDEN:
        bottomDrawerOnBackPressedCallback.setEnabled(false);
        break;
      case BottomSheetBehavior.STATE_DRAGGING:
      case BottomSheetBehavior.STATE_SETTLING:
      default:
        // Do nothing, only change callback enabled for "stable" states.
        break;
    }
  }

  private void showSnackbar(CharSequence text) {
    Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_SHORT)
        .setAnchorView(fab.getVisibility() == View.VISIBLE ? fab : bar)
        .show();
  }
}
