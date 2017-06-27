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

import static android.support.design.backlayer.BackLayerActions.collapse;
import static android.support.design.backlayer.BackLayerActions.expand;
import static android.support.design.backlayer.BackLayerActions.waitUntilIdle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.content.res.Resources;
import android.support.design.testapp.backlayer.BackLayerLayoutActivity;
import android.support.design.testapp.backlayer.R;
import android.support.design.widget.CoordinatorLayout;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.widget.Button;
import android.widget.ImageView;
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
  ImageView extraContent;
  Button expandButton;

  @Rule
  public final ActivityTestRule<BackLayerLayoutActivity> activityTestRule =
      new ActivityTestRule<>(BackLayerLayoutActivity.class);

  @Before
  public void setUp() throws Exception {
    activity = activityTestRule.getActivity();
    resources = activity.getResources();

    coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.coordinator_layout);
    backLayer = (BackLayerLayout) activity.findViewById(R.id.backLayer);
    expandButton = (Button) activity.findViewById(R.id.backLayerExpandButton);
    extraContent = (ImageView) activity.findViewById(R.id.backLayerExtraContent);

    contentLayer = (NestedScrollView) activity.findViewById(R.id.contentLayer);

    if (backLayer.isExpanded()) {
      onView(withId(R.id.backLayerExpandButton)).perform(click());
    }
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
    assertEquals(ViewCompat.getMinimumHeight(backLayer), (int) contentLayer.getY());
    assertEquals(height, contentLayer.getHeight() + (int) contentLayer.getY());
    // Check the positions
    assertEquals(0, (int) backLayer.getX());
    assertEquals(0, (int) backLayer.getY());
    assertEquals(0, (int) contentLayer.getX());
  }

  @Test
  @SmallTest
  public void testExpandingSlidesContentLayerOut() throws InterruptedException {
    int width = coordinatorLayout.getWidth();
    int height = coordinatorLayout.getHeight();
    assertFalse(backLayer.isExpanded());
    onView(withId(R.id.backLayer)).perform(expand());
    assertTrue(backLayer.isExpanded());
    // BackLayer + ContentLayer overflow the height of the coordinatorlayout.
    assertEquals(width, backLayer.getWidth());
    assertEquals(width, contentLayer.getWidth());
    assertThat(contentLayer.getHeight() + (int) contentLayer.getY(), greaterThan(height));
    // Check the positions
    assertEquals(0, (int) backLayer.getX());
    assertEquals(0, (int) backLayer.getY());
    assertEquals(0, (int) contentLayer.getX());
    assertThat(backLayer.getExpandedHeight(), greaterThan(ViewCompat.getMinimumHeight(backLayer)));
    assertEquals(backLayer.getExpandedHeight(), (int) contentLayer.getY());
    assertThat(
        contentLayer.getHeight() + (int) contentLayer.getY(),
        greaterThan(coordinatorLayout.getHeight()));
  }

  @Test
  @SmallTest
  public void testExpandAndCollapseBackLayer() throws InterruptedException {
    int width = coordinatorLayout.getWidth();
    int height = coordinatorLayout.getHeight();
    assertFalse(backLayer.isExpanded());
    onView(withId(R.id.backLayer)).perform(expand());
    assertTrue(backLayer.isExpanded());
    onView(withId(R.id.backLayer)).perform(collapse());
    assertFalse(backLayer.isExpanded());
    // BackLayer + ContentLayer should cover the entire screen, size wise.
    assertEquals(width, backLayer.getWidth());
    assertEquals(width, contentLayer.getWidth());
    assertEquals(ViewCompat.getMinimumHeight(backLayer), (int) contentLayer.getY());
    assertEquals(height, contentLayer.getHeight() + (int) contentLayer.getY());
    // Check the positions
    assertEquals(0, (int) backLayer.getX());
    assertEquals(0, (int) backLayer.getY());
    assertEquals(0, (int) contentLayer.getX());
  }

  @Test
  @SmallTest
  public void testBackLayerCollapsesOnContentLayerClick() throws InterruptedException {
    int width = coordinatorLayout.getWidth();
    int height = coordinatorLayout.getHeight();
    assertFalse(backLayer.isExpanded());
    onView(withId(R.id.backLayer)).perform(expand());
    assertTrue(backLayer.isExpanded());
    onView(withId(R.id.contentLayerHeaderText)).perform(click());
    onView(withId(R.id.backLayer)).perform(waitUntilIdle());
    assertFalse(backLayer.isExpanded());
    // BackLayer + ContentLayer should cover the entire screen, size wise.
    assertEquals(width, backLayer.getWidth());
    assertEquals(width, contentLayer.getWidth());
    assertEquals(ViewCompat.getMinimumHeight(backLayer), (int) contentLayer.getY());
    assertEquals(height, contentLayer.getHeight() + (int) contentLayer.getY());
    // Check the positions
    assertEquals(0, (int) backLayer.getX());
    assertEquals(0, (int) backLayer.getY());
    assertEquals(0, (int) contentLayer.getX());
  }
}
