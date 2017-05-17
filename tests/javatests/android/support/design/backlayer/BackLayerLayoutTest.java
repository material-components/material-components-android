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

package android.support.design.backlayer;

import static junit.framework.Assert.assertEquals;

import android.content.res.Resources;
import android.support.design.testapp.backlayer.BackLayerLayoutActivity;
import android.support.design.testapp.backlayer.R;
import android.support.design.widget.CoordinatorLayout;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.NestedScrollView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BackLayerLayoutTest {

  BackLayerLayoutActivity activity;
  Resources resources;
  NestedScrollView contentLayer;
  BackLayerLayout backLayer;
  CoordinatorLayout coordinatorLayout;

  @Rule
  public final ActivityTestRule<BackLayerLayoutActivity> activityTestRule =
      new ActivityTestRule<>(BackLayerLayoutActivity.class);

  @Before
  public void setUp() throws Exception {
    activity = activityTestRule.getActivity();
    resources = activity.getResources();

    coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.coordinator_layout);
    backLayer = (BackLayerLayout) activity.findViewById(R.id.backLayer);
    contentLayer = (NestedScrollView) activity.findViewById(R.id.contentLayer);
  }

  // TODO: Add tests for Bottom, left, right (/start, end)
  @Test
  @SmallTest
  @UiThreadTest
  public void testTopBackLayerLaidOutCorrectly() {
    int width = coordinatorLayout.getWidth();
    int height = coordinatorLayout.getHeight();
    // BackLayer + ContentLayer should cover the entire screen, size wise.
    assertEquals(width, backLayer.getWidth());
    assertEquals(width, contentLayer.getWidth());
    assertEquals(height, contentLayer.getHeight() + backLayer.getHeight());
    // Check the positions
    assertEquals(0, (int) backLayer.getX());
    assertEquals(0, (int) backLayer.getY());
    assertEquals(0, (int) contentLayer.getX());
    assertEquals(backLayer.getHeight(), (int) contentLayer.getY());
  }
}
