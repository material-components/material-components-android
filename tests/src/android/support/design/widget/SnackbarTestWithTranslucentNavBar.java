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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.support.design.test.R;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.filters.SdkSuppress;
import android.support.v4.view.WindowInsetsCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;

import org.junit.Before;
import org.junit.Test;

@SdkSuppress(minSdkVersion = 21)
public class SnackbarTestWithTranslucentNavBar
        extends BaseInstrumentationTestCase<SnackbarActivityWithTranslucentNavBar> {

    private static final String MESSAGE_TEXT = "Test Message";

    private CoordinatorLayout mCoordinatorLayout;

    public SnackbarTestWithTranslucentNavBar() {
        super(SnackbarActivityWithTranslucentNavBar.class);
    }

    @Before
    public void setup() {
        mCoordinatorLayout =
                (CoordinatorLayout) mActivityTestRule.getActivity().findViewById(R.id.col);
    }

    @Test
    @MediumTest
    public void testDrawsAboveNavigationBar() {
        // Show a simple Snackbar and wait for it to be shown
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT,
                Snackbar.LENGTH_SHORT);
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(snackbar);

        final WindowInsetsCompat colLastInsets = mCoordinatorLayout.getLastWindowInsets();
        assertNotNull(colLastInsets);

        // Check that the Snackbar view has padding set to display above the nav bar
        final View view = snackbar.getView();
        assertNotNull(view);
        assertEquals(colLastInsets.getSystemWindowInsetBottom(), view.getPaddingBottom());
    }
}
