/*
 * Copyright 2019 The Android Open Source Project
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import io.material.catalog.R;
import androidx.annotation.LayoutRes;
import com.google.android.material.textfield.TextInputLayout;

/** A fragment that displays the filled text field demos with controls for the Catalog app. */
public class TextFieldPrefixSuffixDemoFragment extends TextFieldControllableDemoFragment {

  @Override
  @LayoutRes
  public int getTextFieldContent() {
    return R.layout.cat_textfield_prefix_suffix_content;
  }

  @Override
  public void initTextFieldDemoControls(LayoutInflater layoutInflater, View view) {
    super.initTextFieldDemoControls(layoutInflater, view);

    Button togglePrefixSuffixButton = view.findViewById(R.id.button_toggle_prefixsuffix);
    togglePrefixSuffixButton.setOnClickListener(
        v -> {
          if (!textfields.get(0).isPrefixAlwaysVisible()) {
            setAllPrefixSuffixAlwaysVisible(true);
            togglePrefixSuffixButton.setText(
                getResources().getString(R.string.cat_textfield_always_visible_false_text));
          } else {
            setAllPrefixSuffixAlwaysVisible(false);
            togglePrefixSuffixButton.setText(
                getResources().getString(R.string.cat_textfield_always_visible_true_text));
          }
        });
  }

  private void setAllPrefixSuffixAlwaysVisible(boolean alwaysVisible) {
    ViewGroup parent = (ViewGroup) textfields.get(0).getParent();
    for (TextInputLayout textfield : textfields) {
      if (textfield.getPrefixText() != null) {
        textfield.setPrefixAlwaysVisible(alwaysVisible);
      }
      if (textfield.getSuffixText() != null) {
        textfield.setSuffixAlwaysVisible(alwaysVisible);
      }
    }
  }

}
