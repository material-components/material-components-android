/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.circularreveal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.google.android.material.circularreveal.CircularRevealWidget.RevealInfo;
import com.google.android.material.math.MathUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link CircularRevealHelper}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class CircularRevealHelperTest {

  private static final int DELEGATE_WIDTH = 100;
  private static final int DELEGATE_HEIGHT = 200;

  private TestDelegate delegate;
  private Activity activity;
  private CircularRevealHelper helper;
  private Canvas canvas;

  // Small circular reveal from center.
  private RevealInfo smallRevealInfo =
      new RevealInfo(DELEGATE_WIDTH / 2f, DELEGATE_HEIGHT / 2f, DELEGATE_WIDTH / 4f);
  // Huge circular reveal from top/left corner.
  RevealInfo hugeRevealInfo = new RevealInfo(0f, 0f, 10000f);

  @Before
  public void setUp() {
    activity = Robolectric.setupActivity(Activity.class);

    delegate = new TestDelegate(activity);
    delegate.measure(
        MeasureSpec.makeMeasureSpec(DELEGATE_WIDTH, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(DELEGATE_HEIGHT, MeasureSpec.EXACTLY));
    delegate.layout(0, 0, DELEGATE_WIDTH, DELEGATE_HEIGHT);

    helper = new CircularRevealHelper(delegate);
    canvas = spy(new Canvas());
  }

  @Test
  public void hugeRevealInfoRadiusIsModified() {
    helper.setRevealInfo(hugeRevealInfo);

    float diagonal = MathUtils.dist(0f, 0f, DELEGATE_WIDTH, DELEGATE_HEIGHT);
    assertThat(helper.getRevealInfo().radius).isWithin(0.0001f).of(diagonal);
  }

  @Test
  @Config(sdk = Config.OLDEST_SDK)
  public void lUsesRevealAnimatorStrategy() {
    helper = new CircularRevealHelper(delegate);
    helper.setRevealInfo(smallRevealInfo);

    helper.draw(canvas);

    verify(canvas, never()).clipPath(ArgumentMatchers.<Path>any());
    verify(canvas, never())
        .drawCircle(anyFloat(), anyFloat(), anyFloat(), ArgumentMatchers.<Paint>any());
  }

  @Test
  @Config(sdk = Config.OLDEST_SDK)
  public void lDrawsScrim() {
    helper = new CircularRevealHelper(delegate);
    helper.setCircularRevealScrimColor(Color.RED);

    helper.setRevealInfo(smallRevealInfo);
    helper.draw(canvas);

    verify(canvas)
        .drawRect(
            eq(0f),
            eq(0f),
            eq((float) DELEGATE_WIDTH),
            eq((float) DELEGATE_HEIGHT),
            ArgumentMatchers.<Paint>any());
  }

  private static class TestDelegate extends View implements CircularRevealWidget {

    public TestDelegate(Context context) {
      super(context);
    }

    @Override
    public void actualDraw(Canvas canvas) {}

    @Override
    public boolean actualIsOpaque() {
      return false;
    }

    @Override
    public void buildCircularRevealCache() {}

    @Override
    public void destroyCircularRevealCache() {}

    @Nullable
    @Override
    public RevealInfo getRevealInfo() {
      return null;
    }

    @Override
    public void setRevealInfo(@Nullable RevealInfo revealInfo) {}

    @Override
    public int getCircularRevealScrimColor() {
      return 0;
    }

    @Override
    public void setCircularRevealScrimColor(@ColorInt int color) {}

    @Nullable
    @Override
    public Drawable getCircularRevealOverlayDrawable() {
      return null;
    }

    @Override
    public void setCircularRevealOverlayDrawable(@Nullable Drawable drawable) {}
  }
}
