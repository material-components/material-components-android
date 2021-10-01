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

import android.graphics.Rect;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.FoldingFeature.Orientation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import com.google.android.material.navigationrail.NavigationRailView;

/** Utility class for the Adaptive package. */
class AdaptiveUtils {

  static final int MEDIUM_SCREEN_WIDTH_SIZE = 600;
  static final int LARGE_SCREEN_WIDTH_SIZE = 1240;

  private AdaptiveUtils() {}

  /**
   * Updates the visibility of the main navigation view components according to screen size.
   *
   * <p>The small screen layout should have a bottom navigation and optionally a fab. The medium
   * layout should have a navigation rail with a fab, and the large layout should have a navigation
   * drawer with an extended fab.
   */
  static void updateNavigationViewLayout(
      int screenWidth,
      @NonNull DrawerLayout drawerLayout,
      @NonNull NavigationView modalNavDrawer,
      @Nullable FloatingActionButton fab,
      @NonNull BottomNavigationView bottomNav,
      @NonNull NavigationRailView navRail,
      @NonNull NavigationView navDrawer,
      @NonNull ExtendedFloatingActionButton navFab) {
    // Set navigation menu button to show a modal navigation drawer in medium screens.
    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    setNavRailButtonOnClickListener(
        drawerLayout, navRail.getHeaderView().findViewById(R.id.nav_button), modalNavDrawer);
    setModalDrawerButtonOnClickListener(
        drawerLayout,
        modalNavDrawer.getHeaderView(0).findViewById(R.id.nav_button),
        modalNavDrawer);
    modalNavDrawer.setNavigationItemSelectedListener(
        new OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            modalNavDrawer.setCheckedItem(item);
            drawerLayout.closeDrawer(modalNavDrawer);
            return true;
          }
        });

    if (screenWidth < AdaptiveUtils.MEDIUM_SCREEN_WIDTH_SIZE) {
      // Small screen
      if (fab != null) {
        fab.setVisibility(View.VISIBLE);
      }
      bottomNav.setVisibility(View.VISIBLE);
      navRail.setVisibility(View.GONE);
      navDrawer.setVisibility(View.GONE);
    } else if (screenWidth < AdaptiveUtils.LARGE_SCREEN_WIDTH_SIZE) {
      // Medium screen
      if (fab != null) {
        fab.setVisibility(View.GONE);
      }
      bottomNav.setVisibility(View.GONE);
      navRail.setVisibility(View.VISIBLE);
      navDrawer.setVisibility(View.GONE);
      navFab.shrink();
    } else {
      // Large screen
      if (fab != null) {
        fab.setVisibility(View.GONE);
      }
      bottomNav.setVisibility(View.GONE);
      navRail.setVisibility(View.GONE);
      navDrawer.setVisibility(View.VISIBLE);
      navFab.extend();
    }
  }

  /* Sets navigation rail's header button to open the modal navigation drawer. */
  private static void setNavRailButtonOnClickListener(
      @NonNull DrawerLayout drawerLayout,
      @NonNull View navButton,
      @NonNull NavigationView modalDrawer) {
    navButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            drawerLayout.openDrawer(modalDrawer);
          }
        });
  }

  /* Sets modal navigation drawer's header button to close the drawer. */
  private static void setModalDrawerButtonOnClickListener(
      @NonNull DrawerLayout drawerLayout,
      @NonNull View button,
      @NonNull NavigationView modalDrawer) {
    button.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            drawerLayout.closeDrawer(modalDrawer);
          }
        });
  }

  /* Returns the position of the fold relative to the view. */
  static int getFoldPosition(
      @NonNull View view,
      @NonNull FoldingFeature foldingFeature,
      @NonNull FoldingFeature.Orientation orientation) {
    Rect splitRect = getFeatureBoundsInWindow(foldingFeature, view);
    int position = 0;
    if (splitRect != null) {
      position =
          orientation.equals(Orientation.VERTICAL)
              ? view.getWidth() - splitRect.left
              : view.getHeight() - splitRect.top;
    }
    return position;
  }

  /**
   * Gets the bounds of the display feature translated to the View's coordinate space and current
   * position in the window. This will also include view padding in the calculations.
   */
  @Nullable
  private static Rect getFeatureBoundsInWindow(
      @NonNull DisplayFeature displayFeature, @NonNull View view) {
    // The location of the view in window to be in the same coordinate space as the feature.
    int[] viewLocationInWindow = new int[2];
    view.getLocationInWindow(viewLocationInWindow);

    // Intersect the feature rectangle in window with view rectangle to clip the bounds.
    Rect viewRect =
        new Rect(
            viewLocationInWindow[0],
            viewLocationInWindow[1],
            viewLocationInWindow[0] + view.getWidth(),
            viewLocationInWindow[1] + view.getHeight());
    // Include padding.
    viewRect.left += view.getPaddingLeft();
    viewRect.top += view.getPaddingTop();
    viewRect.right -= view.getPaddingRight();
    viewRect.bottom -= view.getPaddingBottom();

    Rect featureRectInView = new Rect(displayFeature.getBounds());
    boolean intersects = featureRectInView.intersect(viewRect);

    // Checks to see if the display feature overlaps with our view at all.
    if ((featureRectInView.width() == 0 && featureRectInView.height() == 0) || !intersects) {
      return null;
    }

    // Offset the feature coordinates to view coordinate space start point.
    featureRectInView.offset(-viewLocationInWindow[0], -viewLocationInWindow[1]);

    return featureRectInView;
  }
}
