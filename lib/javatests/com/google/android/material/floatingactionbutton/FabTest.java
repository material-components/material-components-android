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

package com.google.android.material.floatingactionbutton;

import com.google.android.material.test.R;

import static com.google.android.material.floatingactionbutton.FloatingActionButton.SIZE_MINI;
import static com.google.android.material.internal.ViewUtils.dpToPx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View.MeasureSpec;
import androidx.annotation.RequiresApi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
public class FabTest {

  private static final double DELTA = 0.01;
  private static final int MIN_SIZE_FOR_ALLY_DP = 48;
  private Context activity;

  @Before
  public void createAndThemeApplicationContext() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
  }

  @Test
  public void ensureMinTouchTarget_is48dp() {
    FloatingActionButton fab = createFabForTest(true);

    float expectedSize = dpToPx(activity, MIN_SIZE_FOR_ALLY_DP);

    android.util.Log.i("marian", String.valueOf(expectedSize));

    assertEquals(
        "Fab width was: " + fab.getMeasuredWidth(),
        expectedSize, fab.getMeasuredWidth(), DELTA);

    assertEquals(
        "Fab width was: " + fab.getMeasuredHeight(),
        expectedSize, fab.getMeasuredHeight(), DELTA);
  }

  @Test
  public void ensureMinTouchTargetFalse_isLessThan48dp() {
    FloatingActionButton fab = createFabForTest(false);

    float minSize = dpToPx(activity, MIN_SIZE_FOR_ALLY_DP);

    assertNotEquals(fab.getMeasuredWidth(), minSize, DELTA);

    assertTrue(
        "Fab width was: " + fab.getMeasuredWidth(),
        fab.getMeasuredWidth() < minSize);

    assertTrue(fab.getMeasuredHeight() < minSize);
  }

  @RequiresApi(VERSION_CODES.O)
  @Config(sdk = VERSION_CODES.O)
  @Test
  public void setContentDescription_setsTooltipText() {
    FloatingActionButton fab = new FloatingActionButton(activity);
    String description = "test description";

    fab.setContentDescription(description);

    assertEquals(description, fab.getTooltipText().toString());
  }

  private FloatingActionButton createFabForTest(boolean ensureMinTouchTarget) {
    FloatingActionButton fab = new FloatingActionButton(activity);
    float dimen = dpToPx(activity, MIN_SIZE_FOR_ALLY_DP);
    fab.setSize(SIZE_MINI);
    fab.setEnsureMinTouchTargetSize(ensureMinTouchTarget);
    int measureSpec = MeasureSpec.makeMeasureSpec((int) (dimen * 2), MeasureSpec.AT_MOST);
    fab.measure(measureSpec, measureSpec);
    return fab;
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    }
  }
}
