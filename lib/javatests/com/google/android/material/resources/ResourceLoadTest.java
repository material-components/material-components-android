/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.resources;

import com.google.android.material.test.R;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ResourceLoadTest {

  private final Context context = ApplicationProvider.getApplicationContext();
  private final int defaultMinimumTouchTargetSize =
      context.getResources().getDimensionPixelSize(R.dimen.mtrl_min_touch_target_size);

  @Test
  public void defaultThemeReturnsAccessibleMinimumTouchTarget() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
    int minimumTouchTarget = MaterialAttributes.resolveMinimumAccessibleTouchTarget(context);
    assertEquals(defaultMinimumTouchTargetSize, minimumTouchTarget);
  }

  @Test
  public void appCompatThemeReturnsDefaultAccessibleMinimumTouchTarget() {
    context.setTheme(R.style.Theme_AppCompat);
    int minimumTouchTarget = MaterialAttributes.resolveMinimumAccessibleTouchTarget(context);
    assertEquals(defaultMinimumTouchTargetSize, minimumTouchTarget);
  }

  @Test
  public void minTouchTargetSizeAttrAffectsResolution() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_LargeTouch);
    int minimumTouchTarget = MaterialAttributes.resolveMinimumAccessibleTouchTarget(context);
    int pxExpected = context.getResources().getDimensionPixelSize(R.dimen.mtrl_large_touch_target);
    assertEquals(pxExpected, minimumTouchTarget);
  }

  @Test
  public void dimensionValueLoadsFromContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_BarSize);
    int resolvedDimension =
        MaterialAttributes.resolveDimension(
            context, R.attr.actionBarSize, R.dimen.default_dimension);
    assertEquals(context.getResources().getDimensionPixelSize(R.dimen.action_bar_size), resolvedDimension);
  }

  @Test
  public void dimensionValueLoadsFromDefault() {
    context.setTheme(R.style.EmptyTheme);
    int resolvedDimension =
        MaterialAttributes.resolveDimension(
            context, R.attr.actionBarSize, R.dimen.default_dimension);
    assertEquals(
        context.getResources().getDimensionPixelSize(R.dimen.default_dimension), resolvedDimension);
  }
}
