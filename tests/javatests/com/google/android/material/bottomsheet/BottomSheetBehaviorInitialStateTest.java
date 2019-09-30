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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.BottomSheetBehaviorActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class BottomSheetBehaviorInitialStateTest {

  @Rule
  public final ActivityTestRule<BottomSheetBehaviorActivity> mActivityTestRule =
      new ActivityTestRule<>(BottomSheetBehaviorActivity.class, true, false);

  @Test
  public void testSetStateExpanded() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Intent intent = new Intent(context, BottomSheetBehaviorActivity.class);
    intent.putExtra(
        BottomSheetBehaviorActivity.EXTRA_INITIAL_STATE, BottomSheetBehavior.STATE_EXPANDED);
    mActivityTestRule.launchActivity(intent);
    BottomSheetBehaviorActivity activity = mActivityTestRule.getActivity();
    assertThat(activity.mBehavior.getState(), is(BottomSheetBehavior.STATE_EXPANDED));
    assertThat(activity.mBottomSheet.getTop(), is(0));
  }

  @Test
  public void testSetStateHidden() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Intent intent = new Intent(context, BottomSheetBehaviorActivity.class);
    intent.putExtra(
        BottomSheetBehaviorActivity.EXTRA_INITIAL_STATE, BottomSheetBehavior.STATE_HIDDEN);
    mActivityTestRule.launchActivity(intent);
    BottomSheetBehaviorActivity activity = mActivityTestRule.getActivity();
    assertThat(activity.mBehavior.getState(), is(BottomSheetBehavior.STATE_HIDDEN));
    assertThat(activity.mBottomSheet.getTop(), is(activity.mCoordinatorLayout.getHeight()));
  }
}
