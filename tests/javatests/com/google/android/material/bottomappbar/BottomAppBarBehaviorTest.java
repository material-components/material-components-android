/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.bottomappbar;

import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class BottomAppBarBehaviorTest {

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  BottomAppBar bar;

  @Before
  public void createBottomAppBar() {
    bar = new BottomAppBar(activityTestRule.getActivity());
  }

  @Test
  public void testMovedDown_elevationIncreasedWhileHidden() throws Throwable {
    bar.setElevation(10);
    bar.performHide();
    float originalYTranslation = bar.getTranslationY();

    bar.setElevation(30);

    assertTrue(
        "The bar should have bee moved further down to hide the larger shadow.",
        bar.getTranslationY() > originalYTranslation);
  }
}
