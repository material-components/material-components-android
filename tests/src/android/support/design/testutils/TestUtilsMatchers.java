/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.support.design.testutils;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class TestUtilsMatchers {
    /**
     * Returns a matcher that matches Views that are not narrower than specified width in pixels.
     */
    public static Matcher<View> isNotNarrowerThan(final int minWidth) {
        return new BoundedMatcher<View, View>(View.class) {
            private String failedCheckDescription;

            @Override
            public void describeTo(final Description description) {
                description.appendText(failedCheckDescription);
            }

            @Override
            public boolean matchesSafely(final View view) {
                final int viewWidth = view.getWidth();
                if (viewWidth < minWidth) {
                    failedCheckDescription =
                            "width " + viewWidth + " is less than minimum " + minWidth;
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Returns a matcher that matches Views that are not wider than specified width in pixels.
     */
    public static Matcher<View> isNotWiderThan(final int maxWidth) {
        return new BoundedMatcher<View, View>(View.class) {
            private String failedCheckDescription;

            @Override
            public void describeTo(final Description description) {
                description.appendText(failedCheckDescription);
            }

            @Override
            public boolean matchesSafely(final View view) {
                final int viewWidth = view.getWidth();
                if (viewWidth > maxWidth) {
                    failedCheckDescription =
                            "width " + viewWidth + " is more than maximum " + maxWidth;
                    return false;
                }
                return true;
            }
        };
    }
}
