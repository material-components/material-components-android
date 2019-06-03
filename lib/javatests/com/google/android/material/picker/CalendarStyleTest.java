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
package com.google.android.material.picker;

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import com.google.android.material.shape.MaterialShapeDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class CalendarStyleTest {

  private Context context;
  private CalendarStyle calendarStyle;
  private TextView textView;

  @Before
  public void setupCalendarStyleAndTestView() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Test_Theme_MaterialComponents_MaterialCalendar);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    calendarStyle = new CalendarStyle(context);
    textView = new TextView(context);
  }

  @Test
  public void testTextStyling() {
    ColorStateList expectedTextColor =
        context.getResources().getColorStateList(R.color.test_mtrl_calendar_day);
    calendarStyle.day.styleItem(textView);
    ColorStateList textColor = textView.getTextColors();
    assertEquals(expectedTextColor.getDefaultColor(), textColor.getDefaultColor());
  }

  @Test
  public void testSelectedStyling() {
    ColorStateList expectedTextColor =
        context.getResources().getColorStateList(R.color.test_mtrl_calendar_day_selected);
    calendarStyle.selectedDay.styleItem(textView);
    ColorStateList textColor = textView.getTextColors();
    assertEquals(expectedTextColor.getDefaultColor(), textColor.getDefaultColor());
  }

  @Test
  public void testShapeStyling() {
    calendarStyle.day.styleItem(textView);
    Drawable backgroundDrawable = textView.getBackground();
    MaterialShapeDrawable shapeDrawable;
    if (backgroundDrawable instanceof LayerDrawable) {
      LayerDrawable layerDrawable = (LayerDrawable) backgroundDrawable;
      int maskIndex = layerDrawable.findIndexByLayerId(android.R.id.mask);
      shapeDrawable = (MaterialShapeDrawable) layerDrawable.getDrawable(maskIndex);
    } else {
      shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;
    }
    float cornerSize =
        context.getResources().getDimension(R.dimen.test_mtrl_calendar_day_cornerSize);
    assertEquals(
        cornerSize,
        shapeDrawable.getShapeAppearanceModel().getTopLeftCorner().getCornerSize(),
        0.1);
  }
}
