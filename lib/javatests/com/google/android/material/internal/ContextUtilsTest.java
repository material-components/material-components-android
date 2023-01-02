/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.internal;

import com.google.android.material.test.R;

import android.app.Activity;
import android.content.ContextWrapper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link ContextUtils}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ContextUtilsTest {

  @Test
  public void testWithApplicationContext() {
    Activity activity = ContextUtils.getActivity(ApplicationProvider.getApplicationContext());
    Assert.assertNull(activity);
  }

  @Test
  public void testWithContextWrapper() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_AppCompat);
    AppCompatActivity appCompatActivity = Robolectric.setupActivity(AppCompatActivity.class);
    ContextWrapper contextWrapper = new ContextWrapper(appCompatActivity);
    Activity activity = ContextUtils.getActivity(contextWrapper);
    Assert.assertNotNull(activity);
  }
}
