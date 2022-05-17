/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.textfield;

import com.google.android.material.test.R;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class TextInputLayoutAccessibilityTest {

  private TextInputLayout textInputLayout;
  private final OnClickListener onClickListener =
      new OnClickListener() {
        @Override
        public void onClick(View v) {
          /* Do something */
        }
      };

  @Before
  public void themeApplicationContext() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Theme_Material3_DayNight_NoActionBar);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_text_input_layout, null);
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(inflated);
    textInputLayout = inflated.findViewById(R.id.text_input_layout);
  }

  @Test
  public void testPasswordEndIcon_importantForA11y() {
    textInputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

    assertThat(textInputLayout.getEndIconView().isImportantForAccessibility(), is(true));
  }

  @Test
  public void testClearTextEndIcon_importantForA11y() {
    textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

    assertThat(textInputLayout.getEndIconView().isImportantForAccessibility(), is(true));
  }

  @Test
  public void testClickableCustomEndIcon_importantForA11y() {
    textInputLayout.setEndIconMode(END_ICON_CUSTOM);
    textInputLayout.setEndIconDrawable(new ColorDrawable(Color.GREEN));
    textInputLayout.setEndIconOnClickListener(onClickListener);

    assertThat(textInputLayout.getEndIconView().isImportantForAccessibility(), is(true));
  }

  @Test
  public void testNonClickableCustomEndIcon_notImportantForA11y() {
    textInputLayout.setEndIconMode(END_ICON_CUSTOM);
    textInputLayout.setEndIconDrawable(new ColorDrawable(Color.GREEN));

    assertThat(textInputLayout.getEndIconView().isImportantForAccessibility(), is(false));
  }
}
