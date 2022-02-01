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

package com.google.android.material.bottomsheet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.BottomSheetBehaviorWithInsetsActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BottomSheetBehaviorWithInsetsTest {

  @Rule
  public final ActivityTestRule<BottomSheetBehaviorWithInsetsActivity> activityTestRule =
      new ActivityTestRule<>(BottomSheetBehaviorWithInsetsActivity.class);

  @Test
  @SmallTest
  public void testFitsSystemWindows() {
    BottomSheetBehaviorWithInsetsActivity activity = activityTestRule.getActivity();
    ViewCompat.setFitsSystemWindows(activity.mCoordinatorLayout, true);
    assertThat(activity.mBehavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
    ViewGroup bottomSheet = activity.mBottomSheet;
    assertThat(
        bottomSheet.getTop(),
        is(activity.mCoordinatorLayout.getHeight() - activity.mBehavior.getPeekHeight()));
    assertThat(activity.mBottomSheetContent.getTop(), is(0));
  }
}
