/*
 * Copyright 2024 The Android Open Source Project
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
package com.google.android.material.shape;

import com.google.android.material.test.R;

import static com.google.android.material.shape.StateListSizeChange.SizeChangeType.PERCENT;
import static com.google.android.material.shape.StateListSizeChange.SizeChangeType.PIXELS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.StateListSizeChange.SizeChange;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.AttributeSetBuilder;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class StateListSizeChangeTest {
  private static final double FLOAT_TOLERANCE = 0.00001;
  private static final Context context = ApplicationProvider.getApplicationContext();

  private Map<Integer, String> attributeMap;

  @Before
  public void clearAttributeMap() {
    attributeMap = new HashMap<>();
  }

  @Test
  public void testCreateStateList() {
    attributeMap.put(R.attr.testSizeChangeAttr, "@xml/state_list_size_change");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ShapeTest);
    StateListSizeChange stateListSizeChange =
        StateListSizeChange.create(context, attrs, R.styleable.ShapeTest_testSizeChangeAttr);

    assertNotNull(stateListSizeChange);

    SizeChange pressedSizeChange =
        stateListSizeChange.getSizeChangeForState(new int[] {android.R.attr.state_pressed});
    SizeChange unspecifiedStateSizeChange =
        stateListSizeChange.getSizeChangeForState(new int[] {android.R.attr.state_hovered});
    SizeChange defaultSizeChange = stateListSizeChange.getDefaultSizeChange();

    assertEquals(PERCENT, pressedSizeChange.widthChange.type);
    assertEquals(0.15, pressedSizeChange.widthChange.amount, FLOAT_TOLERANCE);
    assertEquals(PIXELS, unspecifiedStateSizeChange.widthChange.type);
    assertEquals(0, unspecifiedStateSizeChange.widthChange.amount, FLOAT_TOLERANCE);
    assertEquals(PIXELS, defaultSizeChange.widthChange.type);
    assertEquals(0, defaultSizeChange.widthChange.amount, FLOAT_TOLERANCE);
  }

  @Test
  public void testCreateStateListWithoutDefault() {
    attributeMap.put(R.attr.testSizeChangeAttr, "@xml/state_list_size_change_without_default");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ShapeTest);
    StateListSizeChange stateListSizeChange =
        StateListSizeChange.create(context, attrs, R.styleable.ShapeTest_testSizeChangeAttr);

    assertNotNull(stateListSizeChange);

    SizeChange pressedSizeChange =
        stateListSizeChange.getSizeChangeForState(new int[] {android.R.attr.state_pressed});
    SizeChange unspecifiedStateSizeChange =
        stateListSizeChange.getSizeChangeForState(new int[] {android.R.attr.state_hovered});
    SizeChange defaultSizeChange = stateListSizeChange.getDefaultSizeChange();

    assertEquals(PERCENT, pressedSizeChange.widthChange.type);
    assertEquals(0.15, pressedSizeChange.widthChange.amount, FLOAT_TOLERANCE);
    assertEquals(PERCENT, unspecifiedStateSizeChange.widthChange.type);
    assertEquals(0.15, unspecifiedStateSizeChange.widthChange.amount, FLOAT_TOLERANCE);
    assertEquals(PERCENT, defaultSizeChange.widthChange.type);
    assertEquals(0.15, defaultSizeChange.widthChange.amount, FLOAT_TOLERANCE);
  }

  private AttributeSet setupAttributeSetForTest() {
    AttributeSetBuilder attributeSetBuilder = Robolectric.buildAttributeSet();
    for (Map.Entry<Integer, String> entry : attributeMap.entrySet()) {
      attributeSetBuilder.addAttribute(entry.getKey(), entry.getValue());
    }
    return attributeSetBuilder.build();
  }
}
