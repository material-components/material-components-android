/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.material.theme;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.testapp.theme.MaterialComponentsViewInflaterActivity;
import com.google.android.material.testapp.theme.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class MaterialComponentsViewInflaterTest {
  @Rule
  public final ActivityTestRule<MaterialComponentsViewInflaterActivity> activityTestRule =
      new ActivityTestRule<>(MaterialComponentsViewInflaterActivity.class);

  private Activity testActivity;

  @Before
  public void setUpTestActivity() {
    testActivity = activityTestRule.getActivity();
  }

  @Test
  public void ensureThatInflaterCreatesMaterialButton() {
    View view = testActivity.findViewById(R.id.test_button);
    assertThat(view).isInstanceOf(MaterialButton.class);
  }

  @Test
  public void ensureThatInflaterCreatesMaterialRadioButton() {
    View view = testActivity.findViewById(R.id.test_radiobutton);
    assertThat(view).isInstanceOf(MaterialRadioButton.class);
  }

  @Test
  public void ensureThatInflaterCreatesMaterialCheckBox() {
    View view = testActivity.findViewById(R.id.test_checkbox);
    assertThat(view).isInstanceOf(MaterialCheckBox.class);
  }

  @Test
  public void ensureThatInflaterCreatesMaterialTextView() {
    View view = testActivity.findViewById(R.id.test_text_view);
    assertThat(view).isInstanceOf(MaterialTextView.class);
  }

  @Test
  public void ensureThatInflaterCreatesMaterialAutoCompleteTextView() {
    View view = testActivity.findViewById(R.id.test_autocomplete_text_view);
    assertThat(view).isInstanceOf(MaterialAutoCompleteTextView.class);
  }
}
