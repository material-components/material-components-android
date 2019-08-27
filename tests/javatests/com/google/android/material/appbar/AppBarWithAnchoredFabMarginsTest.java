/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.appbar;

import static org.junit.Assert.assertEquals;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.testapp.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class AppBarWithAnchoredFabMarginsTest extends AppBarLayoutBaseTest {
  private int fabMargin;

  @Before
  public void setup() {
    fabMargin =
        activityTestRule.getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin);
  }

  @Test
  public void testFabBottomMargin() throws Throwable {
    configureContent(
        R.layout.design_appbar_anchored_fab_margin_bottom,
        R.string.design_appbar_anchored_fab_margin_bottom);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final CoordinatorLayout.LayoutParams fabLp =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    assertEquals(mAppBar.getId(), fabLp.getAnchorId());

    final int[] appbarOnScreenXY = new int[2];
    final int[] fabOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    fab.getLocationOnScreen(fabOnScreenXY);

    // FAB is horizontally centered in the coordinate system of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[0] + mAppBar.getWidth() / 2, fabOnScreenXY[0] + fab.getWidth() / 2, 1);
    // Bottom margin is in the coordinate space of the parent (CoordinatorLayout) and not
    // the anchor. Since our FAB is far enough from the bottom edge of CoordinatorLayout,
    // we are expecting the vertical center of the FAB to be aligned with the bottom edge
    // of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[1] + mAppBar.getHeight(), fabOnScreenXY[1] + fab.getHeight() / 2, 1);
  }

  @Test
  public void testFabTopMargin() throws Throwable {
    configureContent(
        R.layout.design_appbar_anchored_fab_margin_top,
        R.string.design_appbar_anchored_fab_margin_top);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final CoordinatorLayout.LayoutParams fabLp =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    assertEquals(mAppBar.getId(), fabLp.getAnchorId());

    final int[] appbarOnScreenXY = new int[2];
    final int[] fabOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    fab.getLocationOnScreen(fabOnScreenXY);

    // FAB is horizontally centered in the coordinate system of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[0] + mAppBar.getWidth() / 2, fabOnScreenXY[0] + fab.getWidth() / 2, 1);
    // Top margin is in the coordinate space of the parent (CoordinatorLayout) and not
    // the anchor. Since our FAB is far enough from the bottom edge of CoordinatorLayout,
    // we are expecting the vertical center of the FAB to be aligned with the bottom edge
    // of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[1] + mAppBar.getHeight(), fabOnScreenXY[1] + fab.getHeight() / 2, 1);
  }

  @Test
  public void testFabLeftMargin() throws Throwable {
    configureContent(
        R.layout.design_appbar_anchored_fab_margin_left,
        R.string.design_appbar_anchored_fab_margin_left);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final CoordinatorLayout.LayoutParams fabLp =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    assertEquals(mAppBar.getId(), fabLp.getAnchorId());

    final int[] appbarOnScreenXY = new int[2];
    final int[] fabOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    fab.getLocationOnScreen(fabOnScreenXY);

    // FAB is left-aligned in the coordinate system of its anchor (app bar). In addition,
    // its left margin "pushes" it away in the coordinate system of the parent
    // (CoordinatorLayout)
    assertEquals(appbarOnScreenXY[0] + fabMargin, fabOnScreenXY[0], 1);
    // FAB's vertical center should be aligned with the bottom edge of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[1] + mAppBar.getHeight(), fabOnScreenXY[1] + fab.getHeight() / 2, 1);
  }

  @Test
  public void testFabRightMargin() throws Throwable {
    configureContent(
        R.layout.design_appbar_anchored_fab_margin_right,
        R.string.design_appbar_anchored_fab_margin_right);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final CoordinatorLayout.LayoutParams fabLp =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    assertEquals(mAppBar.getId(), fabLp.getAnchorId());

    final int[] appbarOnScreenXY = new int[2];
    final int[] fabOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    fab.getLocationOnScreen(fabOnScreenXY);

    // FAB is right-aligned in the coordinate system of its anchor (app bar). In addition,
    // its right margin "pushes" it away in the coordinate system of the parent
    // (CoordinatorLayout)
    assertEquals(
        appbarOnScreenXY[0] + mAppBar.getWidth() - fabMargin, fabOnScreenXY[0] + fab.getWidth(), 1);
    // FAB's vertical center should be aligned with the bottom edge of its anchor (app bar).
    assertEquals(
        appbarOnScreenXY[1] + mAppBar.getHeight(), fabOnScreenXY[1] + fab.getHeight() / 2, 1);
  }
}
