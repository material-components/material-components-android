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

import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.AppBarWithScrollbarsActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppBarWithScrollbarsTest {
  @Rule
  public final ActivityTestRule<AppBarWithScrollbarsActivity> activityTestRule =
      new ActivityTestRule<>(AppBarWithScrollbarsActivity.class);

  @Test
  @SmallTest
  public void testInflationNoCrash() {
    // This is the implicit test for to check that AppBarLayout inflation doesn't crash
    // when its theme has attributes that would cause onCreateDrawableState to be called
    // during the super's constructor flow.
  }
}
