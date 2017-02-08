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

package android.support.design.widget;

import static org.junit.Assert.assertEquals;

import android.app.Instrumentation;
import android.support.design.testapp.CoordinatorLayoutActivity;
import android.support.design.testutils.CoordinatorLayoutUtils;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class CoordinatorLayoutSortTest {

  private static final int NUMBER_VIEWS_DEPENDENCY_SORT = 4;

  @Rule
  public final ActivityTestRule<CoordinatorLayoutActivity> activityTestRule =
      new ActivityTestRule<>(CoordinatorLayoutActivity.class);

  /** All 27 permutations of a quad-tuple containing unique values in the range 0-3 */
  public static Collection<Integer[]> data() {
    return Arrays.asList(
        new Integer[][] {
          {0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 2, 3, 1}, {0, 3, 1, 2}, {0, 3, 2, 1},
          {1, 0, 2, 3}, {1, 0, 3, 2}, {1, 2, 0, 3}, {1, 2, 3, 0}, {1, 3, 0, 2}, {1, 3, 2, 0},
          {2, 0, 1, 3}, {2, 0, 3, 1}, {2, 1, 0, 3}, {2, 1, 3, 0}, {2, 3, 0, 1}, {2, 3, 1, 0},
          {3, 0, 1, 2}, {3, 0, 2, 1}, {3, 1, 0, 2}, {3, 1, 2, 0}, {3, 2, 0, 1}, {3, 2, 1, 0}
        });
  }

  @Test
  public void testDependencySortingOrder() throws Throwable {
    for (Integer[] values : data()) {
      // Bazel doesn't support Parameterized android tests, so we emulate it (badly) here
      doTest(values[0], values[1], values[2], values[3]);
    }
  }

  public void doTest(int firstIndex, int secondIndex, int thirdIndex, int fourthIndex)
      throws Throwable {
    final CoordinatorLayout col = activityTestRule.getActivity().mCoordinatorLayout;

    // Let's create some views where each view depends on the previous view.
    // i.e C depends on B, B depends on A, A doesn't depend on anything.
    final List<View> views = new ArrayList<>();
    for (int i = 0; i < NUMBER_VIEWS_DEPENDENCY_SORT; i++) {
      // 65 == A in ASCII
      final String label = Character.toString((char) (65 + i));
      final View view =
          new View(col.getContext()) {
            @Override
            public String toString() {
              return label;
            }
          };

      // Create a Behavior which depends on the previously added view
      View dependency = i > 0 ? views.get(i - 1) : null;
      final CoordinatorLayout.Behavior<View> behavior =
          new CoordinatorLayoutUtils.DependentBehavior(dependency);

      // And set its LayoutParams to use the Behavior
      CoordinatorLayout.LayoutParams lp = col.generateDefaultLayoutParams();
      lp.setBehavior(behavior);
      view.setLayoutParams(lp);

      views.add(view);
    }

    // Now the add the views in the given order and assert that they still end up in
    // the expected order A, B, C, D
    final List<View> testOrder = new ArrayList<>();
    testOrder.add(views.get(firstIndex));
    testOrder.add(views.get(secondIndex));
    testOrder.add(views.get(thirdIndex));
    testOrder.add(views.get(fourthIndex));
    addViewsAndAssertOrdering(col, views, testOrder);
  }

  private void addViewsAndAssertOrdering(
      final CoordinatorLayout col, final List<View> expectedOrder, final List<View> addOrder)
      throws Throwable {
    final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    // Add the Views in the given order
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < addOrder.size(); i++) {
              col.addView(addOrder.get(i));
            }
          }
        });
    instrumentation.waitForIdleSync();

    // Now assert that the dependency sorted order is correct
    assertEquals(expectedOrder, col.getDependencySortedChildren());

    // Finally remove all of the views
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            col.removeAllViews();
          }
        });
    instrumentation.waitForIdleSync();
  }
}
