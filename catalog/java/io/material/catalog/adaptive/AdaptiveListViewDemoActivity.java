/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.ReactiveGuide;
import androidx.core.util.Consumer;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.FoldingFeature.Orientation;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import io.material.catalog.feature.DemoActivity;
import java.util.List;
import java.util.concurrent.Executor;

/** An Activity which hosts the Adaptive list view demo flow. */
public class AdaptiveListViewDemoActivity extends DemoActivity {

  private DrawerLayout drawerLayout;
  private NavigationView modalNavDrawer;
  private View detailViewContainer;
  private ReactiveGuide guideline;
  private BottomNavigationView bottomNav;
  private FloatingActionButton fab;
  private NavigationRailView navRail;
  private NavigationView navDrawer;
  private ExtendedFloatingActionButton navFab;

  @Nullable private WindowInfoTrackerCallbackAdapter windowInfoTracker;
  private final Consumer<WindowLayoutInfo> stateContainer = new StateContainer();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Executor executor = command -> handler.post(() -> handler.post(command));

  private ConstraintLayout constraintLayout;
  private Configuration configuration;
  private FragmentManager fragmentManager;
  private AdaptiveListViewDemoFragment listViewFragment;
  private AdaptiveListViewDetailDemoFragment detailViewFragment;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_list_view_activity, viewGroup, false);
    windowInfoTracker =
        new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));
    drawerLayout = view.findViewById(R.id.drawer_layout);
    constraintLayout = view.findViewById(R.id.list_view_activity_constraint_layout);
    modalNavDrawer = view.findViewById(R.id.modal_nav_drawer);
    detailViewContainer = view.findViewById(R.id.list_view_detail_fragment_container);
    guideline = view.findViewById(R.id.guideline);
    bottomNav = view.findViewById(R.id.bottom_nav);
    fab = view.findViewById(R.id.fab);
    navRail = view.findViewById(R.id.nav_rail);
    navDrawer = view.findViewById(R.id.nav_drawer);
    navFab = view.findViewById(R.id.nav_fab);
    return view;
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    configuration = getResources().getConfiguration();
    fragmentManager = getSupportFragmentManager();
    listViewFragment = new AdaptiveListViewDemoFragment();
    detailViewFragment = new AdaptiveListViewDetailDemoFragment();

    // Update navigation views according to screen width size.
    int screenWidth = configuration.screenWidthDp;
    AdaptiveUtils.updateNavigationViewLayout(
        screenWidth, drawerLayout, modalNavDrawer, fab, bottomNav, navRail, navDrawer, navFab);

    // Clear backstack to prevent unexpected behaviors when pressing back button.
    int backStrackEntryCount = fragmentManager.getBackStackEntryCount();
    for (int entry = 0; entry < backStrackEntryCount; entry++) {
      fragmentManager.popBackStack();
    }
  }

  private void updatePortraitLayout() {
    int listViewFragmentId = R.id.list_view_fragment_container;
    guideline.setGuidelineEnd(0);
    detailViewContainer.setVisibility(View.GONE);
    listViewFragment.setDetailViewContainerId(listViewFragmentId);
    fragmentManager.beginTransaction().replace(listViewFragmentId, listViewFragment).commit();
  }

  private void updateLandscapeLayout(int guidelinePosition, int foldWidth) {
    int listViewFragmentId = R.id.list_view_fragment_container;
    int detailViewFragmentId = R.id.list_view_detail_fragment_container;
    ConstraintSet landscapeLayout = new ConstraintSet();
    landscapeLayout.clone(constraintLayout);
    landscapeLayout.setMargin(detailViewFragmentId, ConstraintSet.START, foldWidth);
    landscapeLayout.applyTo(constraintLayout);
    guideline.setGuidelineEnd(guidelinePosition);
    listViewFragment.setDetailViewContainerId(detailViewFragmentId);
    fragmentManager
        .beginTransaction()
        .replace(listViewFragmentId, listViewFragment)
        .replace(detailViewFragmentId, detailViewFragment)
        .commit();
  }

  @Override
  public void onStart() {
    super.onStart();
    if (windowInfoTracker != null) {
      windowInfoTracker.addWindowLayoutInfoListener(this, executor, stateContainer);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (windowInfoTracker != null) {
      windowInfoTracker.removeWindowLayoutInfoListener(stateContainer);
    }
  }

  private class StateContainer implements Consumer<WindowLayoutInfo> {

    public StateContainer() {}

    @Override
    public void accept(WindowLayoutInfo windowLayoutInfo) {

      List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
      boolean hasVerticalFold = false;

      // Update layout according to orientation.
      if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        updatePortraitLayout();
      } else {
        for (DisplayFeature displayFeature : displayFeatures) {
          if (displayFeature instanceof FoldingFeature) {
            FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
            Orientation orientation = foldingFeature.getOrientation();
            if (orientation.equals(FoldingFeature.Orientation.VERTICAL)) {
              int foldPosition = foldingFeature.getBounds().left;
              int foldWidth = foldingFeature.getBounds().right - foldPosition;
              updateLandscapeLayout(foldPosition, foldWidth);
              hasVerticalFold = true;
            }
          }
        }
        if (!hasVerticalFold) {
          updateLandscapeLayout(constraintLayout.getWidth() / 2, 0);
        }
      }
    }
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
