/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.material.button;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

/**
 * A container for two {@link MaterialButton}s that together create a split button. The {@link
 * MaterialButton}s in this group will be shown on a single line.
 *
 * <p>This layout currently only supports child views of type {@link MaterialButton}. Buttons can be
 * added to this view group via XML, as follows:
 *
 * <pre>
 * &lt;com.google.android.material.button.MaterialSplitButton
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/split_button"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"&gt;
 *
 *     &lt;Button
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:text="@string/split_button_label"
 *         app:icon="@drawable/ic_edit_vd_theme_24dp"
 *         app:iconGravity="start"/&gt;
 *     &lt;Button
 *        style="?attr/materialSplitButtonIconFilledStyle"
 *        android:layout_width="wrap_content"
 *        android:layout_height="wrap_content"
 *        android:contentDescription="@string/split_button_label_chevron"
 *        app:icon="@drawable/m3_split_button_chevron_avd"/&gt;
 *
 * &lt;/com.google.android.material.button.MaterialSplitButton&gt;
 * </pre>
 *
 * <p>Buttons can also be added to this view group programmatically via the {@link #addView(View)}
 * methods.
 *
 * <p>MaterialSplitButton is a {@link MaterialButtonGroup} with only two {@link MaterialButton}s.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Button.md">component
 * developer guidance</a> and <a href="https://material.io/components/buttons/overview">design
 * guidelines</a>.
 */
public class MaterialSplitButton extends MaterialButtonGroup {

  private static final int DEF_STYLE_RES = R.style.Widget_Material3_MaterialSplitButton;
  private static final int REQUIRED_BUTTON_COUNT = 2;

  public MaterialSplitButton(@NonNull Context context) {
    this(context, null);
  }

  public MaterialSplitButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialSplitButtonStyle);
  }

  public MaterialSplitButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
  }

  /**
   * This override prohibits Views other than {@link MaterialButton} to be added where the leading
   * {@link MaterialButton} has either an icon and/or a label and the trailing {@link
   * MaterialButton} has an animated vector drawable as an icon. It also makes updates to the add
   * button shape and margins.
   */
  @Override
  public void addView(@NonNull View child, int index, @Nullable ViewGroup.LayoutParams params) {
    if (!(child instanceof MaterialButton)) {
      throw new IllegalArgumentException("MaterialSplitButton can only hold MaterialButtons.");
    }
    if (getChildCount() > REQUIRED_BUTTON_COUNT) {
      throw new IllegalArgumentException("MaterialSplitButton can only hold two MaterialButtons.");
    }

    MaterialButton buttonChild = (MaterialButton) child;
    super.addView(child, index, params);
    if (indexOfChild(child) == 1) {
      buttonChild.setCheckable(true);
      buttonChild.setA11yClassName(Button.class.getName());
      // Set initial content description based on checked state when focused.
      ViewCompat.setStateDescription(
          buttonChild,
          getResources()
              .getString(
                  buttonChild.isChecked()
                      ? R.string.mtrl_button_expanded_content_description
                      : R.string.mtrl_button_collapsed_content_description));

      buttonChild.addOnCheckedChangeListener(
          (button, isChecked) -> {
            // Update content description when checked state changes.
            ViewCompat.setStateDescription(
                buttonChild,
                getResources()
                    .getString(
                        isChecked
                            ? R.string.mtrl_button_expanded_content_description
                            : R.string.mtrl_button_collapsed_content_description));
          });
    }
  }
}
