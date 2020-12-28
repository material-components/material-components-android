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
package com.google.android.material.appbar;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.LayoutRes;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.appbar.MaterialToolbar}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class MaterialToolbarTest {

  private AppCompatActivity activity;
  private int colorSurface;
  private float elevation;

  @Before
  public void setUpActivityAndResources() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    colorSurface =
        MaterialColors.getColor(
            activity, R.attr.colorSurface, MaterialToolbarTest.class.getSimpleName());
    elevation = activity.getResources().getDimension(R.dimen.design_appbar_elevation);
  }

  @Test
  public void
      givenMinimalUsage_whenInflated_backgroundIsMaterialShapeDrawableWithTransparentColor() {
    MaterialToolbar materialToolbar = inflate(R.layout.test_toolbar);

    assertThat(materialToolbar.getBackground()).isInstanceOf(MaterialShapeDrawable.class);
    assertThat(getMaterialShapeDrawable(materialToolbar).getFillColor().getDefaultColor())
        .isEqualTo(Color.TRANSPARENT);
  }

  @Test
  public void givenCustomNonColorBackground_whenInflated_backgroundIsNotMaterialShapeDrawable() {
    MaterialToolbar materialToolbar = inflate(R.layout.test_toolbar_custom_background);

    assertThat(materialToolbar.getBackground()).isNotInstanceOf(MaterialShapeDrawable.class);
  }

  @Test
  public void givenSurfaceStyle_whenInflated_backgroundIsMaterialShapeDrawableWithSurfaceColor() {
    MaterialToolbar materialToolbar = inflate(R.layout.test_toolbar_surface);

    assertThat(materialToolbar.getBackground()).isInstanceOf(MaterialShapeDrawable.class);
    assertThat(getMaterialShapeDrawable(materialToolbar).getFillColor().getDefaultColor())
        .isEqualTo(colorSurface);
  }

  @Test
  public void givenElevation_whenInflated_backgroundIsMaterialShapeDrawableWithElevation() {
    MaterialToolbar materialToolbar = inflate(R.layout.test_toolbar_elevation);

    assertThat(materialToolbar.getBackground()).isInstanceOf(MaterialShapeDrawable.class);
    assertThat(getMaterialShapeDrawable(materialToolbar).getElevation()).isEqualTo(elevation);
  }

  @Test
  public void givenNoElevation_whenSetElevation_setsMaterialShapeDrawableElevation() {
    MaterialToolbar materialToolbar = inflate(R.layout.test_toolbar);

    ViewCompat.setElevation(materialToolbar, elevation);

    assertThat(materialToolbar.getBackground()).isInstanceOf(MaterialShapeDrawable.class);
    assertThat(getMaterialShapeDrawable(materialToolbar).getElevation()).isEqualTo(elevation);
  }

  private MaterialToolbar inflate(@LayoutRes int layoutResId) {
    return (MaterialToolbar) activity.getLayoutInflater().inflate(layoutResId, null);
  }

  private MaterialShapeDrawable getMaterialShapeDrawable(MaterialToolbar materialToolbar) {
    return (MaterialShapeDrawable) materialToolbar.getBackground();
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    }
  }
}
