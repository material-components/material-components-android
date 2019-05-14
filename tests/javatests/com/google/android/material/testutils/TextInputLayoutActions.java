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

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.testapp.R;
import com.google.android.material.textfield.TextInputLayout;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
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

  /** Sets the transformation method. */
  public static ViewAction setTransformationMethod(
      final TransformationMethod transformationMethod) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the transformation method";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout item = (TextInputLayout) view;
        item.getEditText().setTransformationMethod(transformationMethod);
      }
    };
  }

  public static ViewAction setEndIconOnClickListener(final OnClickListener onClickListener) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set end icon OnClickListener";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setEndIconOnClickListener(onClickListener);
      }
    };
  }

  public static ViewAction setEndIconMode(final int endIconMode) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set end icon mode";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setEndIconMode(endIconMode);
      }
    };
  }

  public static ViewAction setCustomEndIconContent() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set custom end icon content";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setEndIconDrawable(new ColorDrawable(Color.BLUE));
        layout.setEndIconContentDescription(R.string.textinput_custom_end_icon);
      }
    };
  }

  public static ViewAction setStartIcon(final Drawable startIconDrawable) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconDrawable(startIconDrawable);
      }
    };
  }

  public static ViewAction setStartIconContentDescription(final CharSequence contentDesc) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set a content description for the start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconContentDescription(contentDesc);
      }
    };
  }

  public static ViewAction setStartIconTintList(final ColorStateList tintList) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set a tint list for the start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconTintList(tintList);
      }
    };
  }

  public static ViewAction setStartIconOnClickListener(final OnClickListener onClickListener) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set a click listener for the start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconOnClickListener(onClickListener);
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

  /** Clicks end or start icon. */
  public static ViewAction clickIcon(final boolean isEndIcon) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Clicks the end or start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout item = (TextInputLayout) view;
        // Reach in and find the icon view since we don't have a public API to get a reference to it
        CheckableImageButton iconView =
            item.findViewById(isEndIcon ? R.id.text_input_end_icon : R.id.text_input_start_icon);
        iconView.performClick();
      }
    };
  }

  /** Skips any animations on the layout. */
  public static ViewAction skipAnimations() {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Skips any animations.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        view.jumpDrawablesToCurrentState();
      }
    };
  }

  /** Shows the dropdown popup. */
  public static ViewAction showDropdown() {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Shows the dropdown popup.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout item = (TextInputLayout) view;
        ((AutoCompleteTextView) item.getEditText()).showDropDown();
        view.jumpDrawablesToCurrentState();
        CheckableImageButton iconView = item.findViewById(R.id.text_input_end_icon);
        iconView.setChecked(true);
      }
    };
  }
}
