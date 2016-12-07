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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.AppCompatTextView;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.widget.FrameLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class BottomSheetDialogTest extends
        BaseInstrumentationTestCase<BottomSheetDialogActivity> {

    private BottomSheetDialog mDialog;

    public BottomSheetDialogTest() {
        super(BottomSheetDialogActivity.class);
    }

    @Test
    @MediumTest
    public void testBasicDialogSetup() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                showDialog();
                // Confirms that the dialog is shown
                assertThat(mDialog.isShowing(), is(true));
                FrameLayout bottomSheet = (FrameLayout) mDialog
                        .findViewById(R.id.design_bottom_sheet);
                assertThat(bottomSheet, is(notNullValue()));
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                assertThat(behavior.isHideable(), is(true));
                assertThat(behavior, is(notNullValue()));
                // Modal bottom sheets have auto peek height by default.
                assertThat(behavior.getPeekHeight(), is(BottomSheetBehavior.PEEK_HEIGHT_AUTO));
            }
        });
        // Click outside the bottom sheet
        Espresso.onView(ViewMatchers.withId(R.id.touch_outside))
                .perform(ViewActions.click());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // Confirm that the dialog is no longer shown
                assertThat(mDialog.isShowing(), is(false));
            }
        });
    }

    @Test
    @MediumTest
    public void testShortDialog() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });
        // This ensures that the views are laid out before assertions below
        Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet))
                .perform(setTallPeekHeight())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                FrameLayout bottomSheet = (FrameLayout) mDialog
                        .findViewById(R.id.design_bottom_sheet);
                CoordinatorLayout coordinator = (CoordinatorLayout) bottomSheet.getParent();
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                assertThat(bottomSheet, is(notNullValue()));
                assertThat(coordinator, is(notNullValue()));
                assertThat(behavior, is(notNullValue()));
                // This bottom sheet is shorter than the peek height
                assertThat(bottomSheet.getHeight(), is(lessThan(behavior.getPeekHeight())));
                // Confirm that the bottom sheet is bottom-aligned
                assertThat(bottomSheet.getTop(),
                        is(coordinator.getHeight() - bottomSheet.getHeight()));
            }
        });
    }

    @Test
    @MediumTest
    public void testNonCancelableDialog() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                showDialog();
                mDialog.setCancelable(false);
            }
        });
        // Click outside the bottom sheet
        Espresso.onView(ViewMatchers.withId(R.id.touch_outside))
                .perform(ViewActions.click());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                FrameLayout bottomSheet = (FrameLayout) mDialog
                        .findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                assertThat(behavior.isHideable(), is(false));
                assertThat(mDialog.isShowing(), is(true));
                mDialog.cancel();
                assertThat(mDialog.isShowing(), is(false));
            }
        });
    }

    @Test
    @MediumTest
    public void testHideBottomSheet() {
        final AtomicBoolean canceled = new AtomicBoolean(false);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                showDialog();
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        canceled.set(true);
                    }
                });
            }
        });
        Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet))
                .perform(setState(BottomSheetBehavior.STATE_HIDDEN));
        // The dialog should be canceled
        long start = System.currentTimeMillis();
        while (!canceled.get()) {
            SystemClock.sleep(31);
            if (System.currentTimeMillis() - start > 3000) {
                fail("Timed out while waiting for the dialog to be canceled.");
            }
        }
    }

    private void showDialog() {
        Context context = mActivityTestRule.getActivity();
        mDialog = new BottomSheetDialog(context);
        AppCompatTextView text = new AppCompatTextView(context);
        StringBuilder builder = new StringBuilder();
        builder.append("It is fine today. ");
        text.setText(builder);
        mDialog.setContentView(text);
        mDialog.show();
    }

    private static ViewAction setTallPeekHeight() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isDisplayed();
            }

            @Override
            public String getDescription() {
                return "set tall peek height";
            }

            @Override
            public void perform(UiController uiController, View view) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(view);
                behavior.setPeekHeight(view.getHeight() + 100);
            }
        };
    }

    private static ViewAction setState(@BottomSheetBehavior.State final int state) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isBottomSheet();
            }

            @Override
            public String getDescription() {
                return "set state to " + state;
            }

            @Override
            public void perform(UiController uiController, View view) {
                BottomSheetBehavior.from(view).setState(state);
            }
        };
    }

    private static Matcher<View> isBottomSheet() {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                return BottomSheetBehavior.from(view) != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is a bottom sheet");
            }
        };
    }

}

