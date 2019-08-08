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

import com.google.android.material.R;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton.OnChangedListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = LOLLIPOP)
@LooperMode(LooperMode.Mode.PAUSED)
public class ExtendedFloatingActionButtonTest {

  private AppCompatActivity activity;

  private ExtendedFloatingActionButton fabForTest;

  @Before
  public void createAndThemeApplicationContext() {
    ApplicationProvider.getApplicationContext().setTheme(
        R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    fabForTest = createFabForTest();
  }

  @Test
  public void sizeAndCallsCorrectListener_forExtend() {
    // shrink first so that extend is not a no op
    int originalWidth = fabForTest.getMeasuredWidth();
    fabForTest.shrink();

    OnChangedListener onChangedListener = mock(OnChangedListener.class);
    fabForTest.extend(onChangedListener);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedListener, times(1)).onExtended(fabForTest);
    assertThat(fabForTest.getLayoutParams().width).isEqualTo(originalWidth);
  }

  @Test
  public void sizeAndCallsCorrectListener_forCollapse() {
    OnChangedListener onChangedListener = mock(OnChangedListener.class);

    fabForTest.shrink(onChangedListener);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedListener, times(1)).onShrunken(fabForTest);
    assertThat(fabForTest.getLayoutParams().width).isEqualTo(fabForTest.getCollapsedSize());
  }

  @Test
  public void hideAndShow_correctVisibilityAndListener() {
    OnChangedListener onChangedListener = mock(OnChangedListener.class);

    fabForTest.hide(onChangedListener);
    shadowOf(Looper.getMainLooper()).idle();

    verify(onChangedListener, times(1)).onHidden(fabForTest);
    assertThat(fabForTest.getVisibility()).isEqualTo(View.GONE);


    fabForTest.show(onChangedListener);

    verify(onChangedListener, times(1)).onShown(fabForTest);
    assertThat(fabForTest.getVisibility()).isEqualTo(View.VISIBLE);
  }

  private ExtendedFloatingActionButton createFabForTest() {
    ExtendedFloatingActionButton fab = new ExtendedFloatingActionButton(activity);
    fab.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    fab.setText("Test text");
    fab.setIconResource(android.R.drawable.btn_star);
    fab.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

    return fab;
  }
}
