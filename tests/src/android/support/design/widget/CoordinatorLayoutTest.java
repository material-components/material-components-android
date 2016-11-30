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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.v4.view.WindowInsetsCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;

import org.junit.Test;

@MediumTest
public class CoordinatorLayoutTest extends BaseInstrumentationTestCase<CoordinatorLayoutActivity> {

    public CoordinatorLayoutTest() {
        super(CoordinatorLayoutActivity.class);
    }

    @Test
    @SdkSuppress(minSdkVersion = 21)
    public void testSetFitSystemWindows() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;
        final View view = new View(col.getContext());

        // Create a mock which calls the default impl of onApplyWindowInsets()
        final CoordinatorLayout.Behavior<View> mockBehavior =
                mock(CoordinatorLayout.Behavior.class);
        doCallRealMethod().when(mockBehavior)
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Assert that the CoL is currently not set to fitSystemWindows
        assertFalse(col.getFitsSystemWindows());

        // Now add a view with our mocked behavior to the CoordinatorLayout
        view.setFitsSystemWindows(true);
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final CoordinatorLayout.LayoutParams lp = col.generateDefaultLayoutParams();
                lp.setBehavior(mockBehavior);
                col.addView(view, lp);
            }
        });
        instrumentation.waitForIdleSync();

        // Now request some insets and wait for the pass to happen
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.requestApplyInsets();
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has not been called
        verify(mockBehavior, never())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Now enable fits system windows and wait for a pass to happen
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.setFitsSystemWindows(true);
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has been called with some insets
        verify(mockBehavior, atLeastOnce())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));
    }

}
