/*
 * Copyright (C) 2017 The Android Open Source Project
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

import static org.junit.Assert.assertTrue;

import android.graphics.Rect;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.testapp.R;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class AppBarWithDodgingTest extends AppBarLayoutBaseTest {
  @Test
  public void testLeftDodge() throws Throwable {
    configureContent(R.layout.design_appbar_dodge_left, R.string.design_appbar_dodge_left);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final FloatingActionButton fab2 = mCoordinatorLayout.findViewById(R.id.fab2);

    final int[] fabOnScreenXY = new int[2];
    final int[] fab2OnScreenXY = new int[2];
    fab.getLocationOnScreen(fabOnScreenXY);
    fab2.getLocationOnScreen(fab2OnScreenXY);

    final Rect fabRect = new Rect();
    final Rect fab2Rect = new Rect();
    fab.getMeasuredContentRect(fabRect);
    fab2.getMeasuredContentRect(fab2Rect);

    // Our second FAB is configured to "dodge" the first one - to be displayed to the
    // right of it
    int firstRight = fabOnScreenXY[0] + fabRect.right;
    int secondLeft = fab2OnScreenXY[0] + fab2Rect.left;
    assertTrue(
        "Second button left edge at "
            + secondLeft
            + " should be dodging the first button right edge at "
            + firstRight,
        secondLeft >= firstRight);
  }

  @Test
  public void testRightDodge() throws Throwable {
    configureContent(R.layout.design_appbar_dodge_right, R.string.design_appbar_dodge_right);

    final FloatingActionButton fab = mCoordinatorLayout.findViewById(R.id.fab);
    final FloatingActionButton fab2 = mCoordinatorLayout.findViewById(R.id.fab2);

    final int[] fabOnScreenXY = new int[2];
    final int[] fab2OnScreenXY = new int[2];
    fab.getLocationOnScreen(fabOnScreenXY);
    fab2.getLocationOnScreen(fab2OnScreenXY);

    final Rect fabRect = new Rect();
    final Rect fab2Rect = new Rect();
    fab.getMeasuredContentRect(fabRect);
    fab2.getMeasuredContentRect(fab2Rect);

    // Our second FAB is configured to "dodge" the first one - to be displayed to the
    // left of it
    int firstLeft = fabOnScreenXY[0] + fabRect.left;
    int secondRight = fab2OnScreenXY[0] + fab2Rect.right;
    assertTrue(
        "Second button right edge at "
            + secondRight
            + " should be dodging the first button left edge at "
            + firstLeft,
        secondRight <= firstLeft);
  }
}
