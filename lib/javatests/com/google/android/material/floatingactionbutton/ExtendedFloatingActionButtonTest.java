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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton.OnChangedCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
@LooperMode(LooperMode.Mode.PAUSED)
public class ExtendedFloatingActionButtonTest {

  private AppCompatActivity activity;

  private ExtendedFloatingActionButton fabForTest;

  @Before
  public void createAndThemeApplicationContext() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    fabForTest = createFabForTest();
  }

  @Test
  public void sizeAndCallsCorrectListener_forExtend() {
    // shrink first so that extend is not a no op
    int originalWidth = fabForTest.getMeasuredWidth();
    fabForTest.shrink();

    OnChangedCallback onChangedCallback = mock(OnChangedCallback.class);
    fabForTest.extend(onChangedCallback);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedCallback, times(1)).onExtended(fabForTest);
    assertThat(fabForTest.getMeasuredWidth()).isEqualTo(originalWidth);
  }

  @Test
  public void sizeAndCallsCorrectListener_forCollapse() {
    OnChangedCallback onChangedCallback = mock(OnChangedCallback.class);

    fabForTest.shrink(onChangedCallback);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedCallback, times(1)).onShrunken(fabForTest);
    assertThat(fabForTest.getLayoutParams().width).isEqualTo(fabForTest.getCollapsedSize());
  }

  @Test
  public void hideAndShow_correctVisibilityAndListener() {
    OnChangedCallback onChangedCallback = mock(OnChangedCallback.class);

    fabForTest.hide(onChangedCallback);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedCallback, times(1)).onHidden(fabForTest);
    assertThat(fabForTest.getVisibility()).isEqualTo(View.GONE);


    fabForTest.show(onChangedCallback);

    verify(onChangedCallback, times(1)).onShown(fabForTest);
    assertThat(fabForTest.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void setExtended_correctSize_whenExtendedFalse() {
    fabForTest.setExtended(false);

    assertThat(fabForTest.getLayoutParams().width).isEqualTo(fabForTest.getCollapsedSize());
  }

  @Test
  public void setExtended_correctSize_whenExtendedTrue() {
    int originalWidth = fabForTest.getMeasuredWidth();
    // collapse first so it's not a noop.
    fabForTest.setExtended(false);
    fabForTest.setExtended(true);

    assertThat(fabForTest.getMeasuredWidth()).isEqualTo(originalWidth);
  }

  private ExtendedFloatingActionButton createFabForTest() {
    ExtendedFloatingActionButton fab = new ExtendedFloatingActionButton(activity);
    fab.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    fab.setText("Test text");
    fab.setIconResource(android.R.drawable.btn_star);
    fab.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

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
