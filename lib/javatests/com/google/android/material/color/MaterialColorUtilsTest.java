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

import android.content.Context;
import android.view.View;
import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.R;
import com.google.android.material.button.MaterialButton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

import static com.google.common.truth.Truth.assertThat;

/** Tests for the Material themes. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MaterialColorUtilsTest {

  private static final String TAG = MaterialColorUtilsTest.class.getSimpleName();

  private AppCompatActivity activity;
  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
  }

  @Test
  public void testMaterialColor_usingContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
    assertThat(MaterialColorUtils.colorPrimary(context)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorPrimaryVariant(context)).isEqualTo(getColor(R.attr.colorPrimaryVariant));
    assertThat(MaterialColorUtils.colorOnPrimary(context)).isEqualTo(getColor(R.attr.colorOnPrimary));
    assertThat(MaterialColorUtils.colorSecondary(context)).isEqualTo(getColor(R.attr.colorSecondary));
    assertThat(MaterialColorUtils.colorSecondaryVariant(context)).isEqualTo(getColor(R.attr.colorSecondaryVariant));
    assertThat(MaterialColorUtils.colorOnSecondary(context)).isEqualTo(getColor(R.attr.colorOnSecondary));
    assertThat(MaterialColorUtils.colorSurface(context)).isEqualTo(getColor(R.attr.colorSurface));
    assertThat(MaterialColorUtils.colorOnSurface(context)).isEqualTo(getColor(R.attr.colorOnSurface));
    assertThat(MaterialColorUtils.colorBackground(context)).isEqualTo(getColor(android.R.attr.colorBackground));
    assertThat(MaterialColorUtils.colorOnBackground(context)).isEqualTo(getColor(R.attr.colorOnBackground));
    assertThat(MaterialColorUtils.colorError(context)).isEqualTo(getColor(R.attr.colorError));
    assertThat(MaterialColorUtils.colorOnError(context)).isEqualTo(getColor(R.attr.colorOnError));
  }

  @Test
  public void testMaterialColor_usingView() {
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_color_view, null);
    MaterialButton view = inflated.findViewById(R.id.viewTestColor);
    assertThat(MaterialColorUtils.colorPrimary(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorPrimaryVariant(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorOnPrimary(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorSecondary(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorSecondaryVariant(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorOnSecondary(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorSurface(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorOnSurface(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorBackground(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorOnBackground(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorError(view)).isEqualTo(getColor(R.attr.colorPrimary));
    assertThat(MaterialColorUtils.colorOnError(view)).isEqualTo(getColor(R.attr.colorPrimary));
  }

  private int getColor(@AttrRes int colorAttributeResId) {
    return MaterialColors.getColor(context, colorAttributeResId, TAG);
  }
}
