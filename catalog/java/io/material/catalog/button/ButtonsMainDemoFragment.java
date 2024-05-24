/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.button;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays main button demos for the Catalog app. */
public class ButtonsMainDemoFragment extends DemoFragment {

  @NonNull private List<MaterialButton> buttons = new ArrayList<>();
  @NonNull private List<MaterialButton> iconButtons = new ArrayList<>();
  @NonNull private SwitchCompat toggleableSwitch;
  @NonNull private SwitchCompat enabledSwitch;
  @NonNull private SwitchCompat morphCornerSwitch;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getButtonsContent(), viewGroup, false /* attachToRoot */);

    buttons = DemoUtils.findViewsWithType(view, MaterialButton.class);

    ViewGroup iconOnlyButtonsView = view.findViewById(R.id.material_icon_only_buttons_view);
    // Icon only buttons demo may not be there in derived demos.
    if (iconOnlyButtonsView != null) {
      iconButtons = DemoUtils.findViewsWithType(iconOnlyButtonsView, MaterialButton.class);
    }

    int maxMeasuredWidth = 0;
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    for (MaterialButton button : buttons) {
      button.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
      maxMeasuredWidth = Math.max(maxMeasuredWidth, button.getMeasuredWidth());
      button.setOnClickListener(
          v -> {
            // Show a Snackbar with an action button, which should also have a MaterialButton style
            Snackbar snackbar = Snackbar.make(v, R.string.cat_button_clicked, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.cat_snackbar_action_button_text, v1 -> {});
            snackbar.show();
          });
    }

    // Using SwitchCompat here to avoid class cast issues in derived demos.
    enabledSwitch = view.findViewById(R.id.cat_button_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtons());

    // Using SwitchCompat here to avoid class cast issues in derived demos.
    toggleableSwitch = view.findViewById(R.id.cat_button_toggleable_icon_buttons);
    toggleableSwitch.setOnCheckedChangeListener((buttonView, isCheckable) -> updateIconButtons());

    // Using SwitchCompat here to avoid class cast issues in derived demos.
    morphCornerSwitch = view.findViewById(R.id.cat_button_morph_corner_switch);
    morphCornerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtons());

    updateButtons();
    updateIconButtons();

    return view;
  }

  private void updateButtons() {
    boolean isResponsive = morphCornerSwitch.isChecked();
    boolean isEnabled = enabledSwitch.isChecked();
    CharSequence updatedText =
        getText(isEnabled ? R.string.cat_button_label_enabled : R.string.cat_button_label_disabled);
    for (MaterialButton button : buttons) {
      if (!TextUtils.isEmpty(button.getText())) {
        // Do not update icon only button.
        button.setText(updatedText);
      }
      button.setEnabled(isEnabled);
      button.setFocusable(isEnabled);
      button.setCornerAnimationMode(
          isResponsive
              ? MaterialButton.CORNER_ANIMATION_MODE_SHRINK_ON_PRESS
                  | MaterialButton.CORNER_ANIMATION_MODE_GROW_ON_CHECK
              : MaterialButton.CORNER_ANIMATION_MODE_NONE);
    }
  }

  private void updateIconButtons() {
    for (MaterialButton button : iconButtons) {
      button.setCheckable(toggleableSwitch.isChecked());
      button.setChecked(false);
    }
  }

  @LayoutRes
  protected int getButtonsContent() {
    return R.layout.cat_buttons_fragment;
  }
}
