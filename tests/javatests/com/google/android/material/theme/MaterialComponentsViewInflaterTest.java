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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.testapp.theme.MaterialComponentsViewInflaterActivity;
import com.google.android.material.testapp.theme.R;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.checkbox.MaterialCheckBox;
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

  private Button button;
  private RadioButton radioButton;
  private CheckBox checkBox;

  @Before
  public void setUp() throws Exception {
    final MaterialComponentsViewInflaterActivity activity = activityTestRule.getActivity();
    button = activity.findViewById(R.id.test_button);
    radioButton = activity.findViewById(R.id.test_radiobutton);
    checkBox = activity.findViewById(R.id.test_checkbox);
  }

  @Test
  public void testBasics() {
    assertThat(button, is(instanceOf(MaterialButton.class)));
    assertThat(radioButton, is(instanceOf(MaterialRadioButton.class)));
    assertThat(checkBox, is(instanceOf(MaterialCheckBox.class)));
  }
}
