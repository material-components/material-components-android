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
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AutoCompleteTextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.testapp.R;
import com.google.android.material.textfield.TextInputLayout;
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

  public static ViewAction setErrorContentDescription(final CharSequence errorContentDesc) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the error message's content description";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setErrorContentDescription(errorContentDesc);
      }
    };
  }

  public static ViewAction setErrorAccessibilityLiveRegion(final int accessibilityLiveRegion) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the error message's accessibility live region";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setErrorAccessibilityLiveRegion(accessibilityLiveRegion);
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

  public static ViewAction setPlaceholderText(final CharSequence placeholder) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the placeholder";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setPlaceholderText(placeholder);
      }
    };
  }

  public static ViewAction setPlaceholderTextAppearance(final int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the placeholder text appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setPlaceholderTextAppearance(resId);
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

  public static ViewAction setEndIconOnLongClickListener(
      final OnLongClickListener onLongClickListener) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set end icon OnLongClickListener";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setEndIconOnLongClickListener(onLongClickListener);
      }
    };
  }

  public static ViewAction setErrorIconOnClickListener(final OnClickListener onClickListener) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set error icon OnClickListener";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setErrorIconOnClickListener(onClickListener);
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

  public static ViewAction setStartIconTintMode(final PorterDuff.Mode tintMode) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set tint mode for the start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconTintMode(tintMode);
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

  public static ViewAction setStartIconOnLongClickListener(
      final OnLongClickListener onLongClickListener) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Set a long click listener for the start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconOnLongClickListener(onLongClickListener);
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

  public static ViewAction setBoxStrokeErrorColor(@ColorInt final ColorStateList strokeErrorColor) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's stroke error color";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxStrokeErrorColor(strokeErrorColor);
      }
    };
  }

  /**
   * Sets the text field's stroke width.
   *
   * @param strokeWidth the value to use for the text field box's stroke
   * @return the action of setting the box stroke width on a {@link TextInputLayout}
   */
  public static ViewAction setBoxStrokeWidth(final int strokeWidth) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's stroke width.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxStrokeWidth(strokeWidth);
      }
    };
  }

  /**
   * Sets the text field's focused stroke width.
   *
   * @param strokeWidthFocused the value to use for the text field box's stroke when focused
   * @return the action of setting the box's focused stroke width on a {@link TextInputLayout}
   */
  public static ViewAction setBoxStrokeWidthFocused(final int strokeWidthFocused) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's stroke width when focused.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxStrokeWidthFocused(strokeWidthFocused);
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

  /** Sets the {@link ShapeAppearanceModel} of the text field's box background. */
  public static ViewAction setShapeAppearanceModel(
      @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's shape appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setShapeAppearanceModel(shapeAppearanceModel);
      }
    };
  }

  /** Sets the corner family for all corners of the text field. */
  public static ViewAction setBoxCornerFamily(@CornerFamily final int cornerFamily) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets the box's corner family";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setBoxCornerFamily(cornerFamily);
      }
    };
  }

  public static ViewAction setBoxCornerRadii(
      final float topLeftCornerRadius,
      final float topRightCornerRadius,
      final float bottomLeftCornerRadius,
      final float bottomRightCornerRadius) {
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
            bottomLeftCornerRadius,
            bottomRightCornerRadius);
      }
    };
  }

  public static ViewAction setBoxCornerRadii(
      @DimenRes final int topLeftCornerRadiusId,
      @DimenRes final int topRightCornerRadiusId,
      @DimenRes final int bottomLeftCornerRadiusId,
      @DimenRes final int bottomRightCornerRadiusId) {
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
            bottomLeftCornerRadiusId,
            bottomRightCornerRadiusId);
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

  /** Long clicks end or start icon. */
  public static ViewAction longClickIcon(final boolean isEndIcon) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Long clicks the end or start icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout item = (TextInputLayout) view;
        // Reach in and find the icon view since we don't have a public API to get a reference to it
        CheckableImageButton iconView =
            item.findViewById(isEndIcon ? R.id.text_input_end_icon : R.id.text_input_start_icon);
        iconView.performLongClick();
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

  /** Sets prefix. */
  public static ViewAction setPrefixText(final CharSequence prefixText) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets prefix text.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setPrefixText(prefixText);
      }
    };
  }

  /** Sets suffix. */
  public static ViewAction setSuffixText(final CharSequence suffixText) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets suffix text.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setSuffixText(suffixText);
      }
    };
  }

  /** Sets whether the hint expands. */
  public static ViewAction setExpandedHintEnabled(final boolean expandedHintEnabled) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets whether the hint expands.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setExpandedHintEnabled(expandedHintEnabled);
      }
    };
  }

  /** Sets the input type on an AutoCompleteTextView. */
  public static ViewAction setInputType(final int inputType) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(AutoCompleteTextView.class);
      }

      @Override
      public String getDescription() {
        return "Sets input type.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view;
        autoCompleteTextView.setInputType(inputType);
      }
    };
  }

  /** Sets the raw input type on an AutoCompleteTextView. */
  public static ViewAction setRawInputType(final int inputType) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(AutoCompleteTextView.class);
      }

      @Override
      public String getDescription() {
        return "Sets raw input type.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view;
        autoCompleteTextView.setRawInputType(inputType);
      }
    };
  }

  /** Sets start icon minimum size. */
  public static ViewAction setStartIconMinSize(int iconSize) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets start icon min size.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setStartIconMinSize(iconSize);
      }
    };
  }

  /** Sets end icon minimum size. */
  public static ViewAction setEndIconMinSize(int iconSize) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "Sets end icon min size.";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TextInputLayout layout = (TextInputLayout) view;
        layout.setEndIconMinSize(iconSize);
      }
    };
  }

  /** Sets end icon content description on {@link TextInputLayout} */
  public static ViewAction setEndIconContentDescription(final CharSequence contentDescription) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(TextInputLayout.class);
      }

      @Override
      public String getDescription() {
        return "set end icon content description";
      }

      @Override
      public void perform(UiController uiController, View view) {
        ((TextInputLayout) view).setEndIconContentDescription(contentDescription);
      }
    };
  }
}
