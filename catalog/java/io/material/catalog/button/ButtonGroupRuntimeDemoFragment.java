/*
 * Copyright 2025 The Android Open Source Project
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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a button group demo for adding and removing buttons at runtime. */
public class ButtonGroupRuntimeDemoFragment extends DemoFragment {

  private static final int MAX_COUNT = 10;
  private final String[] labels = new String[MAX_COUNT];
  private final Drawable[] icons = new Drawable[MAX_COUNT];
  private MaterialButtonGroup buttonGroup;
  private int buttonCount;
  private Button addButton;
  private Button removeButton;

  /**
   * Create a Demo View with {@link MaterialButtonGroup}, in which, buttons are added and removed at
   * runtime.
   */
  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_buttons_group_runtime_fragment, viewGroup, /* attachToRoot= */ false);
    Context context = view.getContext();

    buttonGroup = view.findViewById(R.id.cat_dynamic_button_group_label_only);

    addButton = view.findViewById(R.id.cat_add_button);
    removeButton = view.findViewById(R.id.cat_remove_button);
    addButton.setOnClickListener(
        new OnClickListener() {
          @SuppressLint("SetTextI18n")
          @Override
          public void onClick(View v) {
            addButton(context);
            buttonCount++;
            updateControl();
          }
        });
    removeButton.setOnClickListener(
        v -> {
          buttonGroup.removeViewAt(buttonCount - 1);
          buttonCount--;
          updateControl();
        });

    updateControl();
    loadResources();

    MaterialSwitch lastCheckedSwitch = view.findViewById(R.id.last_checked_switch);
    lastCheckedSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (ensureAtLeastOneButton()) {
            MaterialButton lastButton = (MaterialButton) buttonGroup.getChildAt(buttonCount - 1);
            lastButton.setChecked(isChecked);
            lastButton.refreshDrawableState();
          }
        });
    MaterialSwitch lastCheckableSwitch = view.findViewById(R.id.last_checkable_switch);
    lastCheckableSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (ensureAtLeastOneButton()) {
            MaterialButton lastButton = (MaterialButton) buttonGroup.getChildAt(buttonCount - 1);
            lastButton.setCheckable(isChecked);
            lastButton.refreshDrawableState();
            lastCheckedSwitch.setEnabled(isChecked);
          }
        });
    MaterialSwitch enableSwitch = view.findViewById(R.id.last_enabled_switch);
    enableSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (ensureAtLeastOneButton()) {
            MaterialButton lastButton = (MaterialButton) buttonGroup.getChildAt(buttonCount - 1);
            lastButton.setEnabled(isChecked);
            lastButton.refreshDrawableState();
          }
        });

    return view;
  }

  private boolean ensureAtLeastOneButton() {
    if (buttonCount == 0) {
      Snackbar.make(buttonGroup, "Add a button first.", Snackbar.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  private void addButton(@NonNull Context context) {
    MaterialButton button = new MaterialButton(context);
    button.setText(labels[buttonCount]);
    button.setIcon(icons[buttonCount]);
    button.setCheckable(true);
    button.setMaxLines(1);
    buttonGroup.addView(button, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1));
    MaterialButtonGroup.LayoutParams lp =
        (MaterialButtonGroup.LayoutParams) button.getLayoutParams();
    lp.overflowText = labels[buttonCount];
    lp.overflowIcon = icons[buttonCount];
  }

  private void loadResources() {
    TypedArray labelsRes = getResources().obtainTypedArray(R.array.cat_button_group_dynamic_labels);
    for (int i = 0; i < MAX_COUNT; i++) {
      labels[i] = labelsRes.getString(i % labelsRes.length());
    }
    labelsRes.recycle();
    TypedArray iconsRes = getResources().obtainTypedArray(R.array.cat_button_group_dynamic_icons);
    for (int i = 0; i < MAX_COUNT; i++) {
      int iconId = iconsRes.getResourceId(i % iconsRes.length(), 0);
      if (iconId != 0) {
        icons[i] = getResources().getDrawable(iconId);
      }
    }
    iconsRes.recycle();
  }

  private void updateControl() {
    if (buttonCount == 0) {
      removeButton.setEnabled(false);
    } else if (buttonCount == MAX_COUNT) {
      addButton.setEnabled(false);
    } else {
      addButton.setEnabled(true);
      removeButton.setEnabled(true);
    }
  }
}
