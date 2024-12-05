/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.material.catalog.floatingtoolbar;

import io.material.catalog.R;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingtoolbar.FloatingToolbarLayout;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A fragment that displays the main Floating Toolbar demo for the Catalog app. */
public class FloatingToolbarMainDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {

    View view = layoutInflater.inflate(getLayoutResId(), viewGroup, /* attachToRoot= */ false);
    TextView bodyText = view.findViewById(R.id.body_text);

    // Initialize group of floating toolbars.
    List<FloatingToolbarLayout> floatingToolbars =
        DemoUtils.findViewsWithType(view, FloatingToolbarLayout.class);

    // Initialize group of bold buttons.
    List<MaterialButton> boldButtons =
        initializeFormatButtons(floatingToolbars, R.id.floating_toolbar_button_bold);
    for (MaterialButton boldButton : boldButtons) {
      boldButton.addOnCheckedChangeListener(
          (button, isChecked) -> {
            Typeface typeface = bodyText.getTypeface();
            if (isChecked) {
              bodyText.setTypeface(
                  typeface, typeface.isItalic() ? Typeface.BOLD_ITALIC : Typeface.BOLD);
            } else {
              bodyText.setTypeface(
                  Typeface.create(
                      bodyText.getTypeface(),
                      typeface.isItalic() ? Typeface.ITALIC : Typeface.NORMAL));
            }
            propagateCheckedButtonState(boldButtons, isChecked);
          });
    }

    // Initialize group of italics format buttons.
    List<MaterialButton> italicButtons =
        initializeFormatButtons(floatingToolbars, R.id.floating_toolbar_button_italic);
    for (MaterialButton italicButton : italicButtons) {
      italicButton.addOnCheckedChangeListener(
          (button, isChecked) -> {
            Typeface typeface = bodyText.getTypeface();
            if (isChecked) {
              bodyText.setTypeface(
                  typeface, typeface.isBold() ? Typeface.BOLD_ITALIC : Typeface.ITALIC);
            } else {
              bodyText.setTypeface(
                  Typeface.create(
                      bodyText.getTypeface(), typeface.isBold() ? Typeface.BOLD : Typeface.NORMAL));
            }
            propagateCheckedButtonState(italicButtons, isChecked);
          });
    }

    // Initialize group of underline format buttons.
    List<MaterialButton> underlineButtons =
        initializeFormatButtons(floatingToolbars, R.id.floating_toolbar_button_underlined);
    for (MaterialButton underlineButton : underlineButtons) {
      underlineButton.addOnCheckedChangeListener(
          (button, isChecked) -> {
            int paintFlags = bodyText.getPaintFlags();
            if (isChecked) {
              bodyText.setPaintFlags(paintFlags | Paint.UNDERLINE_TEXT_FLAG);
            } else {
              bodyText.setPaintFlags(paintFlags & ~Paint.UNDERLINE_TEXT_FLAG);
            }
            propagateCheckedButtonState(underlineButtons, isChecked);
          });
    }

    // Initialize color text format buttons.
    List<MaterialButton> colorTextButtons =
        initializeFormatButtons(floatingToolbars, R.id.floating_toolbar_button_color_text);
    for (MaterialButton colorTextButton : colorTextButtons) {
      colorTextButton.setOnClickListener(v -> bodyText.setTextColor(getRandomColor()));
    }

    // Initialize color fill format buttons.
    List<MaterialButton> colorFillButtons =
        initializeFormatButtons(floatingToolbars, R.id.floating_toolbar_button_color_fill);
    for (MaterialButton colorFillButton : colorFillButtons) {
      colorFillButton.setOnClickListener(v -> view.setBackgroundColor(getRandomColor()));
    }

    // Initialize orientation configuration selection controls.
    initializeOrientationButton(
        view, floatingToolbars, R.id.bottom_button, R.id.floating_toolbar_bottom);
    initializeOrientationButton(
        view, floatingToolbars, R.id.left_button, R.id.floating_toolbar_left);
    initializeOrientationButton(
        view, floatingToolbars, R.id.right_button, R.id.floating_toolbar_right);

    // Select bottom configuration button to represent the toolbar that's initially visible.
    view.findViewById(R.id.bottom_button).performClick();

    return view;
  }

  private void initializeOrientationButton(
      @NonNull View view,
      @NonNull List<FloatingToolbarLayout> floatingToolbars,
      @IdRes int buttonId,
      @IdRes int floatingToolbarId) {
    MaterialButton orientationButton = view.findViewById(buttonId);
    orientationButton.setOnClickListener(
        v -> updateFloatingToolbarVisibilities(floatingToolbars, floatingToolbarId));
  }

  private void updateFloatingToolbarVisibilities(
      @NonNull List<FloatingToolbarLayout> floatingToolbars, @IdRes int visibleFloatingToolbarId) {
    for (FloatingToolbarLayout floatingToolbar : floatingToolbars) {
      if (floatingToolbar.getId() == visibleFloatingToolbarId) {
        floatingToolbar.setVisibility(View.VISIBLE);
      } else {
        floatingToolbar.setVisibility(View.GONE);
      }
    }
  }

  @NonNull
  private List<MaterialButton> initializeFormatButtons(
      @NonNull List<FloatingToolbarLayout> floatingToolbars, @IdRes int formatButtonId) {
    List<MaterialButton> formatButtons = new ArrayList<>();
    for (FloatingToolbarLayout floatingToolbar : floatingToolbars) {
      formatButtons.add(floatingToolbar.findViewById(formatButtonId));
    }
    return formatButtons;
  }

  private void propagateCheckedButtonState(List<MaterialButton> buttons, boolean isChecked) {
    for (MaterialButton button : buttons) {
      button.setChecked(isChecked);
    }
  }

  @ColorInt
  private int getRandomColor() {
    Random random = new Random();
    final int bound = 256;
    return Color.rgb(random.nextInt(bound), random.nextInt(bound), random.nextInt(bound));
  }

  @LayoutRes
  protected int getLayoutResId() {
    return R.layout.cat_floating_toolbar_fragment;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
