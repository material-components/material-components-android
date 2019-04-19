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
package com.google.android.material.picker.selector;

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import com.google.android.material.picker.Month;
import com.google.android.material.picker.MonthAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.GridView;
import androidx.test.core.app.ApplicationProvider;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class DateGridSelectorTest {

  private Context context;
  private DateGridSelector dateGridSelector;
  private MonthAdapter adapter;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
    GridView gridView = new GridView(context);
    dateGridSelector = new DateGridSelector();
    adapter = new MonthAdapter(context, Month.create(2016, Calendar.FEBRUARY), dateGridSelector);
    gridView.setAdapter(adapter);
  }

  @Test
  public void dateDrawCell() {
    int position = 8;
    assertTrue(adapter.withinMonth(position));
    dateGridSelector.changeSelection(adapter, /* view= */ null, position, /* row= */ 0);
    GridSelectorTestUtils.assertCellColor(
        context, dateGridSelector, adapter, position, DateGridSelector.selectedColor);
    GridSelectorTestUtils.assertCellColor(
        context, dateGridSelector, adapter, /* position= */ 15, DateGridSelector.emptyColor);
  }

  @Test
  public void dateGridSelectorMaintainsSelectionAfterParceling() {
    int position = 8;
    assertTrue(adapter.withinMonth(position));
    dateGridSelector.changeSelection(adapter, /* view= */ null, position, /* row= */ 0);
    Calendar expected = adapter.getItem(position);
    DateGridSelector dateGridSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(dateGridSelector, DateGridSelector.CREATOR);
    assertEquals(expected, dateGridSelectorFromParcel.getSelection());
  }

  @Test
  public void nullDateSelectionFromParcel() {
    DateGridSelector dateGridSelectorFromParcel =
        ParcelableTestUtils.parcelAndCreate(dateGridSelector, DateGridSelector.CREATOR);
    assertNull(dateGridSelectorFromParcel.getSelection());
  }
}
