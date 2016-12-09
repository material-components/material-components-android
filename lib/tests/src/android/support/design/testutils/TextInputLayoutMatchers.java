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

package android.support.design.testutils;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextInputLayoutMatchers {

    /**
     * Returns a matcher that matches TextInputLayouts with non-empty content descriptions for
     * the password toggle.
     */
    public static Matcher hasPasswordToggleContentDescription() {
        return new TypeSafeMatcher<TextInputLayout>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("TextInputLayout has non-empty content description"
                        + "for password toggle.");
            }

            @Override
            protected boolean matchesSafely(TextInputLayout item) {
                return !TextUtils.isEmpty(item.getPasswordVisibilityToggleContentDescription());
            }
        };
    }

}
