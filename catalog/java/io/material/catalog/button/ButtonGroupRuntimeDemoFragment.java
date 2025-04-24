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

import android.annotation.SuppressLint;
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
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a button group demo for adding and removing buttons at runtime. */
public class ButtonGroupRuntimeDemoFragment extends DemoFragment {

  private static final int MAX_COUNT = 10;
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

    MaterialButtonGroup buttonGroup = view.findViewById(R.id.cat_dynamic_button_group);
    addButton = view.findViewById(R.id.cat_add_button);
    removeButton = view.findViewById(R.id.cat_remove_button);
    updateControl();
    addButton.setOnClickListener(
        new OnClickListener() {
          @SuppressLint("SetTextI18n")
          @Override
          public void onClick(View v) {
            MaterialButton button = new MaterialButton(view.getContext());
            button.setText("Button");
            buttonGroup.addView(
                button,
                -1,
                new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            buttonCount++;
            updateControl();
          }
        });
    removeButton.setOnClickListener(
        v -> {
          buttonGroup.removeViewAt(buttonGroup.getChildCount() - 1);
          buttonCount--;
          updateControl();
        });

    return view;
  }

  private void updateControl(){
    if(buttonCount == 0){
      removeButton.setEnabled(false);
    }else if(buttonCount == MAX_COUNT){
      addButton.setEnabled(false);
    }else{
      addButton.setEnabled(true);
      removeButton.setEnabled(true);
    }
  }
}
