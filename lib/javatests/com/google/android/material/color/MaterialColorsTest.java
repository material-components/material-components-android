/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.color;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.color.MaterialColors}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MaterialColorsTest {

  private static final int[][] states =
      new int[][] {
        new int[] {
          android.R.attr.state_enabled, android.R.attr.state_selected,
        },
        new int[] {
          android.R.attr.state_enabled, android.R.attr.state_checked,
        },
        new int[] {android.R.attr.state_enabled},
        new int[] {},
      };

  private static final int[] state_empty = {};
  private static final int[] state_enabled = {android.R.attr.state_enabled};

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test
  public void testLayerColorStateLists() {
    ColorStateList overlayColor =
        AppCompatResources.getColorStateList(context, R.color.test_background_state);
    ColorStateList backgroundColor =
        AppCompatResources.getColorStateList(context, R.color.test_surface_state);
    ColorStateList compositeColor =
        MaterialColors.layer(backgroundColor, -1, overlayColor, 0, states);

    List<int[][]> testStatesList = generateTestStatesList();
    for (int[][] testStates : testStatesList) {
      for (int[] state : testStates) {
        compareLayeredColorsForStates(compositeColor, backgroundColor, overlayColor, state);
      }
    }
  }

  private static List<int[][]> generateTestStatesList() {
    List<int[][]> testStatesList = new ArrayList<>();
    int[][] testStates1 =
        new int[][] {
          new int[] {
            android.R.attr.state_enabled,
            android.R.attr.state_selected,
            android.R.attr.state_checked,
          },
          state_enabled,
          state_empty,
        };
    testStatesList.add(testStates1);
    int[][] testState2 = {
      {android.R.attr.state_enabled, android.R.attr.state_selected}, state_enabled, state_empty
    };
    testStatesList.add(testState2);
    int[][] testState3 = {
      {android.R.attr.state_checked, android.R.attr.state_enabled}, state_enabled, state_empty
    };
    testStatesList.add(testState3);
    int[][] testState4 = {
      {android.R.attr.state_pressed, android.R.attr.state_checked, android.R.attr.state_enabled},
      state_enabled,
      state_empty
    };
    testStatesList.add(testState4);
    return testStatesList;
  }

  private static void compareLayeredColorsForStates(
      ColorStateList compositeColor,
      ColorStateList backgroundColor,
      ColorStateList overlayColor,
      int[] state) {
    int composite = compositeColor.getColorForState(state, -1);
    int background = backgroundColor.getColorForState(state, -1);
    int overlay = overlayColor.getColorForState(state, -1);
    int expectedColor = MaterialColors.layer(background, overlay);
    assertColorsAreEqual(expectedColor, composite);
  }

  private static void assertColorsAreEqual(@ColorInt int expectedColor, @ColorInt int color) {
    Assert.assertEquals(expectedColor, color);
  }
}
