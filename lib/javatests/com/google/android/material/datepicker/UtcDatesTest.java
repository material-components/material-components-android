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

import com.google.android.material.test.R;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import androidx.test.core.app.ApplicationProvider;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
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
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/y", hint);
  }

  @Test
  public void textInputHintWith2CharYear() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/yy", hint);
  }

  @Test
  public void textInputHintWith4CharYear() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);

    assertEquals("m/d/yyyy", hint);
  }

  @Test
  @Config(qualifiers = "fr-rFR")
  public void textInputHintWith1CharYearLocalized() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);

    assertEquals("m/j/a", hint);
  }

  @Test
  @Config(qualifiers = "ko")
  public void textInputHintForKorean() {
    SimpleDateFormat sdf = new SimpleDateFormat("yy.M.d.");
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);

    assertEquals("년.월.일.", hint);
  }

  @Test
  public void verbatimTextInputHintForUK() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy", Locale.UK);
    String hint = UtcDates.getDefaultTextInputHint(context.getResources(), sdf);
    SpannableString hintSpannable = UtcDates.getVerbatimTextInputHint(hint);

    assertEquals("dd/mm/yyyy", hintSpannable.toString());
  }

  @Test
  public void normalizeTextInputFormat() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
    sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));

    SimpleDateFormat normalized = (SimpleDateFormat) UtcDates.getNormalizedFormat(sdf);

    assertEquals(TimeZone.getTimeZone("US/Pacific"), sdf.getTimeZone());
    assertEquals(TimeZone.getTimeZone("UTC"), normalized.getTimeZone());
  }

  @Test
  public void getDefaultTextInputFormat() {
    SimpleDateFormat sdf = UtcDates.getDefaultTextInputFormat();

    assertEquals("MM/dd/yyyy", sdf.toPattern());
  }

  @Test
  @Config(qualifiers = "fr-rFR")
  public void getDefaultTextInputFormatForFrench() {
    SimpleDateFormat sdf = UtcDates.getDefaultTextInputFormat();

    assertEquals("dd/MM/yyyy", sdf.toPattern());
  }

  @Test
  @Config(qualifiers = "ru")
  public void getDefaultTextInputFormatForRussian() {
    SimpleDateFormat sdf = UtcDates.getDefaultTextInputFormat();

    assertEquals("dd.MM.yyyy", sdf.toPattern());
  }

  @Test
  @Config(qualifiers = "se")
  public void getDefaultTextInputFormatForSwedish() {
    SimpleDateFormat sdf = UtcDates.getDefaultTextInputFormat();

    assertEquals("yyyy-MM-dd", sdf.toPattern());
  }

  @Test
  public void getDatePatternAsInputFormat_filterDateCharactersAndDelimiters() {
    String pattern = UtcDates.getDatePatternAsInputFormat("ddDe/FMM-mNo._!$% xYyyyyz");

    assertEquals("dd/MM-.yyyy", pattern);
  }

  @Test
  public void getDatePatternAsInputFormat_enforce2CharsForDayMonth4CharsForYear() {
    String pattern = UtcDates.getDatePatternAsInputFormat("d/M/y");

    assertEquals("dd/MM/yyyy", pattern);
  }

  @Test
  public void getDatePatternAsInputFormat_removeDotSuffix() {
    String pattern = UtcDates.getDatePatternAsInputFormat("yyyy.MM.dd.");

    assertEquals("yyyy.MM.dd", pattern);
  }

  @Test
  @Config(qualifiers = "kkj")
  public void getDatePatternAsInputFormat_fixForKakoLanguage() {
    String defaultPattern =
        ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()))
            .toPattern();
    String pattern = UtcDates.getDatePatternAsInputFormat(defaultPattern);

    assertEquals("dd/MM y", defaultPattern);
    assertEquals("dd/MM/yyyy", pattern);
  }
}
