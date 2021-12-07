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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import io.material.catalog.feature.DemoActivity;
import java.util.List;
import java.util.concurrent.Executor;

/** An Activity which hosts the Adaptive feed demo flow. */
public class AdaptiveFeedDemoActivity extends DemoActivity {

  private View container;
  private DrawerLayout drawerLayout;
  private NavigationView modalNavDrawer;
  private BottomNavigationView bottomNav;
  private NavigationRailView navRail;
  private NavigationView navDrawer;
  private ExtendedFloatingActionButton navFab;
  private AdaptiveFeedDemoFragment feedFragment;

  @Nullable private WindowInfoTrackerCallbackAdapter windowInfoTracker;
  private final Consumer<WindowLayoutInfo> stateContainer = new StateContainer();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Executor executor = command -> handler.post(() -> handler.post(command));
  private Configuration configuration;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_feed_activity, viewGroup, false);
    container = view.findViewById(R.id.feed_activity_container);
    drawerLayout = view.findViewById(R.id.drawer_layout);
    modalNavDrawer = view.findViewById(R.id.modal_nav_drawer);
    windowInfoTracker =
        new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));
    configuration = getResources().getConfiguration();
    bottomNav = view.findViewById(R.id.bottom_nav);
    navRail = view.findViewById(R.id.nav_rail);
    navDrawer = view.findViewById(R.id.nav_drawer);
    navFab = view.findViewById(R.id.nav_fab);
    return view;
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    feedFragment = new AdaptiveFeedDemoFragment();

    // Update navigation views according to screen width size.
    int screenWidth = configuration.screenWidthDp;
    AdaptiveUtils.updateNavigationViewLayout(
        screenWidth,
        drawerLayout,
        modalNavDrawer,
        /* fab= */ null,
        bottomNav,
        navRail,
        navDrawer,
        navFab);

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, feedFragment)
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
      if (feedFragment == null) {
        return;
      }

      if (configuration.screenWidthDp < AdaptiveUtils.MEDIUM_SCREEN_WIDTH_SIZE) {
        feedFragment.setClosedLayout();
      } else {
        List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
        boolean isClosed = true;

        for (DisplayFeature displayFeature : displayFeatures) {
          if (displayFeature instanceof FoldingFeature) {
            FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
            if (foldingFeature.getState().equals(FoldingFeature.State.HALF_OPENED)
                || foldingFeature.getState().equals(FoldingFeature.State.FLAT)) {
              Orientation orientation = foldingFeature.getOrientation();
              if (orientation.equals(FoldingFeature.Orientation.VERTICAL)) {
                int foldPosition = foldingFeature.getBounds().left;
                int foldWidth = foldingFeature.getBounds().right - foldPosition;
                // Device is open and fold is vertical.
                feedFragment.setOpenLayout(foldPosition, foldWidth);
              } else {
                // Device is open and fold is horizontal.
                feedFragment.setOpenLayout(container.getWidth() / 2, 0);
              }
              isClosed = false;
            }
          }
        }
        if (isClosed) {
          if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Device is closed or not foldable and in portrait.
            feedFragment.setClosedLayout();
          } else {
            // Device is closed or not foldable and in landscape.
            feedFragment.setOpenLayout(container.getWidth() / 2, 0);
          }
        }
      }
    }
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
