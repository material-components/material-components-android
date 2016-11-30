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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.v4.view.MotionEventCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;

@MediumTest
public class BottomSheetBehaviorTouchTest extends
        BaseInstrumentationTestCase<CoordinatorLayoutActivity> {

    private static final int PEEK_HEIGHT = 100;

    private FrameLayout mBottomSheet;

    private BottomSheetBehavior<FrameLayout> mBehavior;

    private boolean mDown;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (MotionEventCompat.getActionMasked(event)) {
                case MotionEvent.ACTION_DOWN:
                    mDown = true;
                    break;
            }
            return true;
        }

    };

    public BottomSheetBehaviorTouchTest() {
        super(CoordinatorLayoutActivity.class);
    }

    @Before
    public void setUpBottomSheet() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
                activity.mContainer.setOnTouchListener(mOnTouchListener);
                mBottomSheet = new FrameLayout(activity);
                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.MATCH_PARENT,
                        CoordinatorLayout.LayoutParams.MATCH_PARENT);
                mBehavior = new BottomSheetBehavior<>();
                mBehavior.setPeekHeight(PEEK_HEIGHT);
                params.setBehavior(mBehavior);
                activity.mCoordinatorLayout.addView(mBottomSheet, params);
            }
        });
    }

    @Test
    public void testSetUp() {
        assertThat(mBottomSheet, is(notNullValue()));
        assertThat(mBehavior, is(sameInstance(BottomSheetBehavior.from(mBottomSheet))));
    }

    @Test
    public void testTouchCoordinatorLayout() {
        final CoordinatorLayoutActivity activity = mActivityTestRule.getActivity();
        mDown = false;
        Espresso.onView(sameInstance((View) activity.mCoordinatorLayout))
                .perform(ViewActions.click())  // Click outside the bottom sheet
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException e) {
                        assertThat(e, is(nullValue()));
                        assertThat(view, is(notNullValue()));
                        // Check that the touch event fell through to the container
                        assertThat(mDown, is(true));
                    }
                });
    }

}
