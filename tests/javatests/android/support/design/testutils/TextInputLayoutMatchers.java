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

import android.support.design.R;
import android.support.design.internal.CheckableImageButton;
import android.support.design.textfield.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextInputLayoutMatchers {

  /**
   * Returns a matcher that matches TextInputLayouts with non-empty content descriptions for the
   * password toggle.
   */
  public static Matcher<View> passwordToggleHasContentDescription() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText(
            "TextInputLayout has non-empty content description" + "for password toggle.");
      }

      @Override
      protected boolean matchesSafely(View view) {
        TextInputLayout item = (TextInputLayout) view;
        // Reach in and find the password toggle since we don't have a public API
        // to get a reference to it
        View passwordToggle = item.findViewById(R.id.text_input_password_toggle);
        return !TextUtils.isEmpty(item.getPasswordVisibilityToggleContentDescription())
            && !TextUtils.isEmpty(passwordToggle.getContentDescription());
      }
    };
  }

  /** Returns a matcher that matches TextInputLayouts with non-displayed password toggles */
  public static Matcher<View> doesNotShowPasswordToggle() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("TextInputLayout shows password toggle.");
      }

      @Override
      protected boolean matchesSafely(View item) {
        // Reach in and find the password toggle since we don't have a public API
        // to get a reference to it
        View passwordToggle = item.findViewById(R.id.text_input_password_toggle);
        return passwordToggle.getVisibility() != View.VISIBLE;
      }
    };
  }

  /** Returns a matcher that matches TextInputLayouts with non-displayed password toggles */
  public static Matcher<View> passwordToggleIsNotChecked() {
    return new TypeSafeMatcher<View>(TextInputLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("TextInputLayout has checked password toggle.");
      }

      @Override
      protected boolean matchesSafely(View item) {
        // Reach in and find the password toggle since we don't have a public API
        // to get a reference to it
        CheckableImageButton passwordToggle = item.findViewById(R.id.text_input_password_toggle);
        return !passwordToggle.isChecked();
      }
    };
  }
}
