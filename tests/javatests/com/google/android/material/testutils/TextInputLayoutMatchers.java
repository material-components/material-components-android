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

package com.google.android.material.testutils;

import com.google.android.material.testapp.R;
import com.google.android.material.textfield.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextInputLayoutMatchers {

  /**
   * Returns a matcher that matches TextInputLayouts with non-empty content descriptions for the
   * end icon.
   */
  public static Matcher<View> endIconHasContentDescription() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText(
            "TextInputLayout has non-empty content description" + "for end icon.");
      }

      @Override
      protected boolean matchesSafely(View view) {
        TextInputLayout item = (TextInputLayout) view;
        // Reach in and find the end icon since we don't have a public API to get a reference to it
        View endIcon = item.findViewById(R.id.text_input_end_icon);
        return !TextUtils.isEmpty(item.getEndIconContentDescription())
            && !TextUtils.isEmpty(endIcon.getContentDescription());
      }
    };
  }

  /** Returns a matcher that matches TextInputLayouts with non-displayed end icons. */
  public static Matcher<View> doesNotShowEndIcon() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("TextInputLayout doesn't show end icon.");
      }

      @Override
      protected boolean matchesSafely(View item) {
        // Reach in and find the end icon since we don't have a public API
        // to get a reference to it
        View endIcon = item.findViewById(R.id.text_input_end_icon);
        return endIcon.getVisibility() != View.VISIBLE;
      }
    };
  }

  /** Returns a matcher that matches TextInputLayouts with displayed end icons. */
  public static Matcher<View> showsEndIcon() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("TextInputLayout shows end icon.");
      }

      @Override
      protected boolean matchesSafely(View item) {
        // Reach in and find the end icon since we don't have a public API
        // to get a reference to it
        View endIcon = item.findViewById(R.id.text_input_end_icon);
        return endIcon.getVisibility() == View.VISIBLE;
      }
    };
  }
}
