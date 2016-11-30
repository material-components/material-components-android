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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.support.v4.view.ViewCompat;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.ViewGroup;

import org.junit.Test;

public class BottomSheetBehaviorWithInsetsTest extends
        BaseInstrumentationTestCase<BottomSheetBehaviorWithInsetsActivity> {

    public BottomSheetBehaviorWithInsetsTest() {
        super(BottomSheetBehaviorWithInsetsActivity.class);
    }

    @Test
    @SmallTest
    public void testFitsSystemWindows() {
        BottomSheetBehaviorWithInsetsActivity activity = mActivityTestRule.getActivity();
        ViewCompat.setFitsSystemWindows(activity.mCoordinatorLayout, true);
        assertThat(activity.mBehavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        ViewGroup bottomSheet = activity.mBottomSheet;
        assertThat(bottomSheet.getTop(),
                is(activity.mCoordinatorLayout.getHeight() - activity.mBehavior.getPeekHeight()));
        assertThat(activity.mBottomSheetContent.getTop(), is(0));
    }

}
