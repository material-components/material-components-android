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

package com.google.android.material.snackbar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.SnackbarWithTranslucentNavBarActivity;
import com.google.android.material.testutils.SnackbarUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SdkSuppress(minSdkVersion = 21)
@RunWith(AndroidJUnit4.class)
public class SnackbarWithTranslucentNavBarTest {

  @Rule
  public final ActivityTestRule<SnackbarWithTranslucentNavBarActivity> activityTestRule =
      new ActivityTestRule<>(SnackbarWithTranslucentNavBarActivity.class);

  private static final String MESSAGE_TEXT = "Test Message";

  private CoordinatorLayout coordinatorLayout;

  @Before
  public void setup() {
    coordinatorLayout = activityTestRule.getActivity().findViewById(R.id.col);
  }

  @Test
  @MediumTest
  public void testDrawsAboveNavigationBar() {
    // Show a simple Snackbar and wait for it to be shown
    final Snackbar snackbar = Snackbar.make(coordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT);
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    final WindowInsetsCompat colLastInsets = coordinatorLayout.getLastWindowInsets();
    assertNotNull(colLastInsets);

    // Check that the Snackbar view has padding set to display above the nav bar
    final View view = snackbar.getView();
    assertNotNull(view);
    assertEquals(colLastInsets.getSystemWindowInsetBottom(), view.getPaddingBottom());
  }
}
