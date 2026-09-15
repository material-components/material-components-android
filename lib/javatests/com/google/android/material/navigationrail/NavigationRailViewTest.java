/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.navigationrail;

import com.google.android.material.test.R;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = O)
public final class NavigationRailViewTest {

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_Material3_Light_NoActionBar);
  }

  @Test
  public void testSaveHierarchyState_restoresExpandedState() {
    NavigationRailView navigationRailView = new NavigationRailView(context);
    navigationRailView.setId(1);
    navigationRailView.expand();
    assertThat(navigationRailView.isExpanded()).isTrue();

    SparseArray<Parcelable> state = new SparseArray<>();
    navigationRailView.saveHierarchyState(state);

    NavigationRailView restoredView = new NavigationRailView(context);
    restoredView.setId(1);
    restoredView.restoreHierarchyState(state);

    assertThat(restoredView.isExpanded()).isTrue();
  }

  @Test
  public void testSaveHierarchyState_restoresCollapsedState() {
    NavigationRailView navigationRailView = new NavigationRailView(context);
    navigationRailView.setId(1);
    navigationRailView.collapse();
    assertThat(navigationRailView.isExpanded()).isFalse();

    SparseArray<Parcelable> state = new SparseArray<>();
    navigationRailView.saveHierarchyState(state);

    NavigationRailView restoredView = new NavigationRailView(context);
    restoredView.setId(1);
    // Expand the restored view before restoring state to ensure the restored state overwrites it.
    restoredView.expand();
    restoredView.restoreHierarchyState(state);

    assertThat(restoredView.isExpanded()).isFalse();
  }

  @Test
  public void testSaveHierarchyState_restoresMenuSelection() {
    NavigationRailView navigationRailView = new NavigationRailView(context);
    navigationRailView.setId(1);
    setupTestMenu(navigationRailView);
    navigationRailView.setSelectedItemId(20);

    SparseArray<Parcelable> state = new SparseArray<>();
    navigationRailView.saveHierarchyState(state);

    NavigationRailView restoredView = new NavigationRailView(context);
    restoredView.setId(1);
    setupTestMenu(restoredView);
    restoredView.restoreHierarchyState(state);

    assertThat(restoredView.getSelectedItemId()).isEqualTo(20);
  }

  @Test
  public void testRestoreHierarchyState_fromRailState_updatesBottomNavigationView() {
    NavigationRailView navigationRailView = new NavigationRailView(context);
    navigationRailView.setId(1);
    setupTestMenu(navigationRailView);
    navigationRailView.setSelectedItemId(20);
    navigationRailView.expand();

    SparseArray<Parcelable> state = new SparseArray<>();
    navigationRailView.saveHierarchyState(state);

    BottomNavigationView bottomNavigationView = new BottomNavigationView(context);
    bottomNavigationView.setId(1);
    setupTestMenu(bottomNavigationView);
    bottomNavigationView.restoreHierarchyState(state);

    assertThat(bottomNavigationView.getSelectedItemId()).isEqualTo(20);
  }

  @Test
  public void testRestoreHierarchyState_fromBottomNavigationState_updatesRailView() {
    BottomNavigationView bottomNavigationView = new BottomNavigationView(context);
    bottomNavigationView.setId(1);
    setupTestMenu(bottomNavigationView);
    bottomNavigationView.setSelectedItemId(20);

    SparseArray<Parcelable> state = new SparseArray<>();
    bottomNavigationView.saveHierarchyState(state);

    NavigationRailView restoredRailView = new NavigationRailView(context);
    restoredRailView.setId(1);
    setupTestMenu(restoredRailView);
    restoredRailView.restoreHierarchyState(state);

    assertThat(restoredRailView.getSelectedItemId()).isEqualTo(20);
  }

  @Test
  public void testSaveHierarchyState_restoresScrollPosition() {
    NavigationRailView railView = createScrollingRailView();
    measureAndLayout(railView);
    railView.getScrollView().scrollTo(0, 150);

    SparseArray<Parcelable> state = new SparseArray<>();
    railView.saveHierarchyState(state);

    NavigationRailView restoredView = createScrollingRailView();
    restoredView.restoreHierarchyState(state);
    measureAndLayout(restoredView);

    assertThat(restoredView.getScrollView().getScrollY()).isEqualTo(150);
  }

  private NavigationRailView createScrollingRailView() {
    NavigationRailView view =
        new NavigationRailView(
            context, null, 0, R.style.Widget_Material3Expressive_NavigationRailView);
    view.setId(100);
    for (int i = 0; i < 20; i++) {
      view.getMenu().add(Menu.NONE, i, i, "Item " + i);
    }
    return view;
  }

  private static void measureAndLayout(View view) {
    view.measure(
        MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(300, MeasureSpec.EXACTLY));
    view.layout(0, 0, 200, 300);
  }

  private void setupTestMenu(NavigationBarView view) {
    view.getMenu().add(Menu.NONE, 10, Menu.NONE, "Item 1").setCheckable(true);
    view.getMenu().add(Menu.NONE, 20, Menu.NONE, "Item 2").setCheckable(true);
  }
}
