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

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import com.google.android.material.R;
import com.google.android.material.textfield.TextInputLayout;
import android.view.View;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import org.hamcrest.Matcher;

public class TextInputLayoutActions {

  public static ViewAction setErrorEnabled(final boolean enabled) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Enables/disables the error";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setErrorEnabled(enabled);
      }
    };
  }

  public static ViewAction setError(final CharSequence errorText) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the error";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setError(errorText);
      }
    };
  }

  public static ViewAction setErrorTextAppearance(final int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the error text appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setErrorTextAppearance(resId);
      }
    };
  }

  public static ViewAction setHelperTextEnabled(final boolean enabled) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Enables/disables the helper";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setHelperTextEnabled(enabled);
      }
    };
  }

  public static ViewAction setHelperText(final CharSequence helperText) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the helper";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setHelperText(helperText);
      }
    };
  }

  public static ViewAction setHelperTextTextAppearance(final int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the helper text appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setHelperTextTextAppearance(resId);
      }
    };
  }

  public static ViewAction setTypeface(final Typeface typeface) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the typeface";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setTypeface(typeface);
      }
    };
  }

  public static ViewAction setPasswordVisibilityToggleEnabled(final boolean enabled) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the error";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setPasswordVisibilityToggleEnabled(enabled);
      }
    };
  }

  public static ViewAction setCounterEnabled(final boolean enabled) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the counter enabled";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setCounterEnabled(enabled);
      }
    };
  }

  public static ViewAction setCounterMaxLength(final int maxLength) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the counter max length";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setCounterMaxLength(maxLength);
      }
    };
  }

  public static ViewAction setBoxStrokeColor(@ColorInt final int strokeColor) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's stroke color";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxStrokeColor(strokeColor);
      }
    };
  }

  public static ViewAction setBoxBackgroundColor(@ColorInt final int backgroundColor) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's background color";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxBackgroundColor(backgroundColor);
      }
    };
  }

  public static ViewAction setBoxCornerRadii(
      final float topLeftCornerRadius,
      final float topRightCornerRadius,
      final float bottomRightCornerRadius,
      final float bottomLeftCornerRadius) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's corner radii";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxCornerRadii(
            topLeftCornerRadius,
            topRightCornerRadius,
            bottomRightCornerRadius,
            bottomLeftCornerRadius);
      }
    };
  }

  public static ViewAction setBoxCornerRadii(
      @DimenRes final int topLeftCornerRadiusId,
      @DimenRes final int topRightCornerRadiusId,
      @DimenRes final int bottomRightCornerRadiusId,
      @DimenRes final int bottomLeftCornerRadiusId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's corner radii";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxCornerRadiiResources(
            topLeftCornerRadiusId,
            topRightCornerRadiusId,
            bottomRightCornerRadiusId,
            bottomLeftCornerRadiusId);
      }
    };
  }

  public static ViewAction setHint(final String hint) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the hint/label text";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setHint(hint);
      }
    };
  }

  public static ViewAction setHintTextAppearance(final int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the hint/label text appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setHintTextAppearance(resId);
      }
    };
  }

  /** Toggles password. */
  public static ViewAction clickPasswordToggle() {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Clicks the password toggle";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout textInputLayout = (TextInputLayout) view;
        // Reach in and find the password toggle since we don't have a public API
        // to get a reference to it
        View passwordToggle = textInputLayout.findViewById(R.id.text_input_password_toggle);
        passwordToggle.performClick();
      }
    };
  }
}
