/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import java.text.SimpleDateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class UtcDatesTest {

  private Context context;

  @Before
  public void setup() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
  }

  @Test
  public void textInputHintWith1CharYear() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
    String hint = UtcDates.getTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/yyyy", hint);
  }

  @Test
  public void textInputHintWith2CharYear() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
    String hint = UtcDates.getTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/yy", hint);
  }

  @Test
  public void textInputHintWith4CharYear() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
    String hint = UtcDates.getTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/yyyy", hint);
  }

  @Test
  @Config(qualifiers = "fr-rFR")
  public void textInputHintWith1CharYearLocalized() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
    String hint = UtcDates.getTextInputHint(context.getResources(), sdf);

    assertEquals("m/j/aaaa", hint);
  }
}
