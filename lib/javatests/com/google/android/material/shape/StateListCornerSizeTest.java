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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.test.core.app.ApplicationProvider;
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
public class StateListCornerSizeTest {
  private static final double FLOAT_TOLERANCE = 0.00001;
  private static final Context context = ApplicationProvider.getApplicationContext();

  private Map<Integer, String> attributeMap;

  @Before
  public void clearAttributeMap() {
    attributeMap = new HashMap<>();
  }

  @Test
  public void testCreateStateListWithStateList() {
    attributeMap.put(R.attr.testCornerSizeAttr, "@xml/state_list_corner_size");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ShapeTest);
    StateListCornerSize stateListCornerSize =
        StateListCornerSize.create(
            context, attrs, R.styleable.ShapeTest_testCornerSizeAttr, new AbsoluteCornerSize(0));

    CornerSize pressedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_pressed});
    CornerSize unspecifiedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_hovered});
    CornerSize defaultCornerSize = stateListCornerSize.getDefaultCornerSize();

    assertTrue(pressedCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) pressedCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
    assertTrue(unspecifiedCornerSize instanceof RelativeCornerSize);
    assertEquals(
        ((RelativeCornerSize) unspecifiedCornerSize).getRelativePercent(), 0.5, FLOAT_TOLERANCE);
    assertTrue(defaultCornerSize instanceof RelativeCornerSize);
    assertEquals(
        ((RelativeCornerSize) defaultCornerSize).getRelativePercent(), 0.5, FLOAT_TOLERANCE);
  }

  @Test
  public void testCreateStateListWithStateListWithoutDefault() {
    attributeMap.put(R.attr.testCornerSizeAttr, "@xml/state_list_corner_size_without_default");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.ShapeTest);
    StateListCornerSize stateListCornerSize =
        StateListCornerSize.create(
            context, attrs, R.styleable.ShapeTest_testCornerSizeAttr, new AbsoluteCornerSize(0));

    CornerSize pressedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_pressed});
    CornerSize unspecifiedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_hovered});
    CornerSize defaultCornerSize = stateListCornerSize.getDefaultCornerSize();

    assertTrue(pressedCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) pressedCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
    assertTrue(unspecifiedCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) unspecifiedCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
    assertTrue(defaultCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) defaultCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
  }

  @Test
  public void testCreateStateListWithDimensionValue() {
    attributeMap.put(R.attr.testCornerSizeAttr, "2dp");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs =
        context.obtainStyledAttributes(attributeSet, new int[] {R.attr.testCornerSizeAttr});
    StateListCornerSize stateListCornerSize =
        StateListCornerSize.create(
            context, attrs, R.styleable.ShapeTest_testCornerSizeAttr, new AbsoluteCornerSize(0));

    CornerSize pressedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_pressed});
    CornerSize defaultCornerSize = stateListCornerSize.getDefaultCornerSize();

    assertTrue(pressedCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) pressedCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
    assertTrue(defaultCornerSize instanceof AbsoluteCornerSize);
    assertEquals(((AbsoluteCornerSize) defaultCornerSize).getCornerSize(), 2, FLOAT_TOLERANCE);
  }

  @Test
  public void testCreateStateListWithFractionValue() {
    attributeMap.put(R.attr.testCornerSizeAttr, "50%");
    AttributeSet attributeSet = setupAttributeSetForTest();
    TypedArray attrs =
        context.obtainStyledAttributes(attributeSet, new int[] {R.attr.testCornerSizeAttr});
    StateListCornerSize stateListCornerSize =
        StateListCornerSize.create(
            context, attrs, R.styleable.ShapeTest_testCornerSizeAttr, new AbsoluteCornerSize(0));

    CornerSize pressedCornerSize =
        stateListCornerSize.getCornerSizeForState(new int[] {android.R.attr.state_pressed});
    CornerSize defaultCornerSize = stateListCornerSize.getDefaultCornerSize();

    assertTrue(pressedCornerSize instanceof RelativeCornerSize);
    assertEquals(
        ((RelativeCornerSize) pressedCornerSize).getRelativePercent(), 0.5, FLOAT_TOLERANCE);
    assertTrue(defaultCornerSize instanceof RelativeCornerSize);
    assertEquals(
        ((RelativeCornerSize) defaultCornerSize).getRelativePercent(), 0.5, FLOAT_TOLERANCE);
  }

  private AttributeSet setupAttributeSetForTest() {
    AttributeSetBuilder attributeSetBuilder = Robolectric.buildAttributeSet();
    for (Map.Entry<Integer, String> entry : attributeMap.entrySet()) {
      attributeSetBuilder.addAttribute(entry.getKey(), entry.getValue());
    }
    return attributeSetBuilder.build();
  }
}
