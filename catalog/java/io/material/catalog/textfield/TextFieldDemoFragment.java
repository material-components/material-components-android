/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.textfield;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/**
 * Base class that provides a structure for text field demos with optional controls for the Catalog
 * app.
 */
public abstract class TextFieldDemoFragment extends DemoFragment {
  protected List<TextInputLayout> textfields;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_textfield_fragment, viewGroup, false /* attachToRoot */);
    initTextFields(layoutInflater, view);
    initTextFieldDemoControls(layoutInflater, view);
    return view;
  }

  private void initTextFields(LayoutInflater layoutInflater, View view) {
    inflateTextFields(layoutInflater, view.findViewById(R.id.content));
    // Add text fields from the content layout before the text fields from the demo controls to
    // allow for modifying the demo text fields without modifying the textfields used for the
    // demo controls.
    addTextFieldsToList(view);
  }

  private void inflateTextFields(LayoutInflater layoutInflater, ViewGroup content) {
    content.addView(layoutInflater.inflate(getTextFieldContent(), content, false));
  }

  public void initTextFieldDemoControls(LayoutInflater layoutInflater, View view) {
    inflateTextFieldDemoControls(layoutInflater, view.findViewById(R.id.content));
  }

  private void inflateTextFieldDemoControls(LayoutInflater layoutInflater, ViewGroup content) {
    @LayoutRes int demoControls = getTextFieldDemoControlsLayout();
    if (demoControls != 0) {
      content.addView(layoutInflater.inflate(getTextFieldDemoControlsLayout(), content, false));
    }
  }

  private void addTextFieldsToList(View view) {
    textfields = DemoUtils.findViewsWithType(view, TextInputLayout.class);
  }

  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_content;
  }

  @LayoutRes
  public int getTextFieldDemoControlsLayout() {
    return 0;
  }
}
