/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.google.android.material.focus;

import com.google.android.material.test.R;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ContextThemeWrapper;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.GraphicsMode;

/** Unit tests for {@link FocusRingDrawable}. */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FocusRingDrawableTest {

  private static final int[] FOCUSED_STATE_SET = {
    android.R.attr.state_focused, android.R.attr.state_window_focused
  };

  private Context enabledContext;
  private Context disabledContext;

  @Before
  public void setUp() {
    Context appContext = ApplicationProvider.getApplicationContext();
    enabledContext =
        new ContextThemeWrapper(
            appContext, R.style.Theme_Material3Expressive_Light_NoActionBar_FocusRings);
    disabledContext =
        new ContextThemeWrapper(appContext, R.style.Theme_Material3Expressive_Light_NoActionBar);
  }

  @Test
  public void constructor_alreadyLaidOutGradientDrawable_preservesBoundsAndComputesCornerRadius() {
    GradientDrawable child = new GradientDrawable();
    child.setColor(Color.TRANSPARENT);
    child.setCornerRadius(30f);
    Rect expectedBounds = new Rect(10, 20, 110, 80);
    child.setBounds(expectedBounds);

    FocusRingDrawable focusRing = new FocusRingDrawable(enabledContext, child);

    assertThat(focusRing.getBounds()).isEqualTo(expectedBounds);
    assertThat(child.getBounds()).isEqualTo(expectedBounds);
    assertThat(child.getCallback()).isSameInstanceAs(focusRing);

    // Verify the pill corner radius was calculated during construction without requiring another
    // setBounds() call: the top-edge midpoint should be stroked, while the top-left square corner
    // outside the 30px radius should remain transparent.
    focusRing.setState(FOCUSED_STATE_SET);
    focusRing.jumpToCurrentState();
    shadowOf(getMainLooper()).idle();
    Bitmap bitmap = Bitmap.createBitmap(120, 100, Bitmap.Config.ARGB_8888);
    focusRing.draw(new Canvas(bitmap));
    assertThat(bitmap.getPixel(60, 21)).isNotEqualTo(Color.TRANSPARENT);
    assertThat(bitmap.getPixel(11, 21)).isEqualTo(Color.TRANSPARENT);
  }

  @Test
  public void setFocusRingShapeAppearance_afterWrappingLaidOutDrawable_recalculatesShape() {
    Drawable child = new ColorDrawable(Color.TRANSPARENT);
    child.setBounds(10, 20, 110, 80);
    FocusRingDrawable focusRing = (FocusRingDrawable) FocusRingDrawable.wrap(enabledContext, child);

    // Mutate geometry setters after wrap() without triggering another setBounds() call.
    focusRing.setFocusRingShapeAppearance(
        ShapeAppearanceModel.builder().setAllCornerSizes(new RelativeCornerSize(0.5f)).build());
    focusRing.setFocusRingInset(4f);

    focusRing.setState(FOCUSED_STATE_SET);
    focusRing.jumpToCurrentState();
    shadowOf(getMainLooper()).idle();
    Bitmap bitmap = Bitmap.createBitmap(120, 100, Bitmap.Config.ARGB_8888);
    focusRing.draw(new Canvas(bitmap));
    // Inset by 4px from top=20 -> outer stroke is drawn around y=25, while the top-left square
    // corner outside the pill radius remains transparent.
    assertThat(bitmap.getPixel(60, 25)).isNotEqualTo(Color.TRANSPARENT);
    assertThat(bitmap.getPixel(15, 25)).isEqualTo(Color.TRANSPARENT);
  }

  @Test
  public void setFocusRingBounds_afterLayout_recalculatesShapeAtCustomBounds() {
    Drawable child = new ColorDrawable(Color.TRANSPARENT);
    child.setBounds(0, 0, 120, 100);
    FocusRingDrawable focusRing = (FocusRingDrawable) FocusRingDrawable.wrap(enabledContext, child);
    focusRing.setFocusRingShapeAppearance(
        ShapeAppearanceModel.builder().setAllCornerSizes(new RelativeCornerSize(0.5f)).build());

    // Restrict custom focus ring bounds to (10, 20, 110, 80) without changing drawable bounds.
    focusRing.setFocusRingBounds(10, 20, 110, 80);

    focusRing.setState(FOCUSED_STATE_SET);
    focusRing.jumpToCurrentState();
    shadowOf(getMainLooper()).idle();
    Bitmap bitmap = Bitmap.createBitmap(120, 100, Bitmap.Config.ARGB_8888);
    focusRing.draw(new Canvas(bitmap));
    assertThat(bitmap.getPixel(60, 21)).isNotEqualTo(Color.TRANSPARENT);
    assertThat(bitmap.getPixel(11, 21)).isEqualTo(Color.TRANSPARENT);
  }

  @Test
  public void wrap_enabledTheme_preservesExistingBounds() {
    Drawable child = new ColorDrawable(Color.RED);
    Rect expectedBounds = new Rect(10, 20, 110, 120);
    child.setBounds(expectedBounds);

    Drawable wrapped = FocusRingDrawable.wrap(enabledContext, child);

    assertThat(wrapped).isInstanceOf(FocusRingDrawable.class);
    assertThat(wrapped.getBounds()).isEqualTo(expectedBounds);
    assertThat(child.getBounds()).isEqualTo(expectedBounds);
    assertThat(child.getCallback()).isSameInstanceAs(wrapped);
  }

  @Test
  public void wrap_alreadyFocusRingDrawable_returnsSameInstanceAndPreservesBounds() {
    Drawable child = new ColorDrawable(Color.RED);
    Rect expectedBounds = new Rect(10, 20, 110, 120);
    child.setBounds(expectedBounds);

    Drawable wrappedOnce = FocusRingDrawable.wrap(enabledContext, child);
    Drawable wrappedTwice = FocusRingDrawable.wrap(enabledContext, wrappedOnce);

    assertThat(wrappedTwice).isSameInstanceAs(wrappedOnce);
    assertThat(wrappedTwice.getBounds()).isEqualTo(expectedBounds);
    assertThat(child.getBounds()).isEqualTo(expectedBounds);
  }

  @Test
  public void wrap_layerDrawableAlreadyContainingFocusRing_returnsSameInstance() {
    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] {new ColorDrawable(Color.RED)});
    FocusRingDrawable.layer(enabledContext, layerDrawable);

    Drawable wrapped = FocusRingDrawable.wrap(enabledContext, layerDrawable);

    assertThat(wrapped).isSameInstanceAs(layerDrawable);
  }

  @Test
  public void wrap_disabledTheme_returnsOriginalAndPreservesBounds() {
    Drawable child = new ColorDrawable(Color.RED);
    Rect expectedBounds = new Rect(10, 20, 110, 120);
    child.setBounds(expectedBounds);

    Drawable wrapped = FocusRingDrawable.wrap(disabledContext, child);

    assertThat(wrapped).isSameInstanceAs(child);
    assertThat(child.getBounds()).isEqualTo(expectedBounds);
  }

  @Test
  public void wrap_nullDrawable_enabledTheme_returnsFocusRingDrawable() {
    Drawable wrapped = FocusRingDrawable.wrap(enabledContext, null);

    assertThat(wrapped).isInstanceOf(FocusRingDrawable.class);
    assertThat(((FocusRingDrawable) wrapped).getDrawable()).isNull();
  }

  @Test
  public void wrapInner_insetDrawableAlreadyLaidOut_preservesInsetBoundsAndCallbacks() {
    Drawable child = new ColorDrawable(Color.RED);
    InsetDrawable insetDrawable = new InsetDrawable(child, 10, 12, 14, 16);
    AtomicInteger invalidationCount = new AtomicInteger();
    Drawable.Callback rootCallback = createCountingCallback(invalidationCount);
    insetDrawable.setCallback(rootCallback);
    insetDrawable.setBounds(0, 0, 100, 60);
    Rect expectedInnerBounds = new Rect(10, 12, 86, 44);
    assertThat(child.getBounds()).isEqualTo(expectedInnerBounds);

    FocusRingDrawable focusRing = FocusRingDrawable.wrapInner(enabledContext, insetDrawable);

    assertThat(focusRing).isNotNull();
    assertThat(insetDrawable.getDrawable()).isSameInstanceAs(focusRing);
    assertThat(focusRing.getDrawable()).isSameInstanceAs(child);
    assertThat(FocusRingDrawable.find(insetDrawable)).isSameInstanceAs(focusRing);
    assertThat(insetDrawable.getBounds()).isEqualTo(new Rect(0, 0, 100, 60));
    assertThat(focusRing.getBounds()).isEqualTo(expectedInnerBounds);
    assertThat(child.getBounds()).isEqualTo(expectedInnerBounds);
    assertThat(focusRing.getCallback()).isSameInstanceAs(insetDrawable);
    assertThat(child.getCallback()).isSameInstanceAs(focusRing);

    invalidationCount.set(0);
    child.invalidateSelf();
    assertThat(invalidationCount.get()).isEqualTo(1);
  }

  @Test
  public void wrapInner_beforeBoundsSet_appliesInsetBoundsOnSubsequentSetBounds() {
    Drawable child = new ColorDrawable(Color.RED);
    InsetDrawable insetDrawable = new InsetDrawable(child, 10, 12, 14, 16);

    FocusRingDrawable focusRing = FocusRingDrawable.wrapInner(enabledContext, insetDrawable);

    assertThat(focusRing).isNotNull();
    assertThat(focusRing.getBounds()).isEqualTo(new Rect(0, 0, 0, 0));
    assertThat(child.getBounds()).isEqualTo(new Rect(0, 0, 0, 0));

    insetDrawable.setBounds(0, 0, 100, 60);

    Rect expectedInnerBounds = new Rect(10, 12, 86, 44);
    assertThat(focusRing.getBounds()).isEqualTo(expectedInnerBounds);
    assertThat(child.getBounds()).isEqualTo(expectedInnerBounds);
  }

  @Test
  public void wrapInner_disabledTheme_returnsNullWithoutClobberingBoundsOrInvalidating() {
    Drawable child = new ColorDrawable(Color.RED);
    InsetDrawable insetDrawable = new InsetDrawable(child, 10, 12, 14, 16);
    insetDrawable.setBounds(0, 0, 100, 60);
    Rect expectedInnerBounds = new Rect(10, 12, 86, 44);
    AtomicInteger invalidationCount = new AtomicInteger();
    Drawable.Callback rootCallback = createCountingCallback(invalidationCount);
    insetDrawable.setCallback(rootCallback);

    FocusRingDrawable focusRing = FocusRingDrawable.wrapInner(disabledContext, insetDrawable);

    assertThat(focusRing).isNull();
    assertThat(insetDrawable.getDrawable()).isSameInstanceAs(child);
    assertThat(child.getBounds()).isEqualTo(expectedInnerBounds);
    assertThat(child.getCallback()).isSameInstanceAs(insetDrawable);
    assertThat(invalidationCount.get()).isEqualTo(0);
  }

  @Test
  public void wrapInner_alreadyWrapped_isIdempotentAndPreservesInsetBounds() {
    Drawable child = new ColorDrawable(Color.RED);
    InsetDrawable insetDrawable = new InsetDrawable(child, 10, 12, 14, 16);
    insetDrawable.setBounds(0, 0, 100, 60);
    Rect expectedInnerBounds = new Rect(10, 12, 86, 44);

    FocusRingDrawable first = FocusRingDrawable.wrapInner(enabledContext, insetDrawable);
    FocusRingDrawable second = FocusRingDrawable.wrapInner(enabledContext, insetDrawable);

    assertThat(first).isNotNull();
    assertThat(second).isSameInstanceAs(first);
    assertThat(insetDrawable.getDrawable()).isSameInstanceAs(first);
    assertThat(first.getDrawable()).isSameInstanceAs(child);
    assertThat(first.getBounds()).isEqualTo(expectedInnerBounds);
    assertThat(child.getBounds()).isEqualTo(expectedInnerBounds);
  }

  @Test
  public void wrapInner_focusRingDrawableWrapper_returnsSameInstance() {
    Drawable child = new ColorDrawable(Color.RED);
    Rect expectedBounds = new Rect(10, 20, 110, 120);
    child.setBounds(expectedBounds);
    FocusRingDrawable existing = new FocusRingDrawable(enabledContext, child);

    FocusRingDrawable result = FocusRingDrawable.wrapInner(enabledContext, existing);

    assertThat(result).isSameInstanceAs(existing);
    assertThat(existing.getDrawable()).isSameInstanceAs(child);
    assertThat(existing.getBounds()).isEqualTo(expectedBounds);
  }

  @Test
  public void wrapInner_nullWrapperOrNullChild_returnsNull() {
    assertThat(FocusRingDrawable.wrapInner(enabledContext, null)).isNull();

    DrawableWrapper emptyWrapper = new DrawableWrapper(null) {};
    assertThat(FocusRingDrawable.wrapInner(enabledContext, emptyWrapper)).isNull();
    assertThat(emptyWrapper.getDrawable()).isNull();
  }

  @Test
  public void wrapInner_mutatesWrapper_doesNotPolluteSharedConstantState() {
    Drawable child = new ColorDrawable(Color.RED);
    InsetDrawable insetDrawable = new InsetDrawable(child, 10, 12, 14, 16);
    insetDrawable.setBounds(0, 0, 100, 60);
    Drawable.ConstantState sharedState = insetDrawable.getConstantState();
    assertThat(sharedState).isNotNull();

    FocusRingDrawable focusRing = FocusRingDrawable.wrapInner(enabledContext, insetDrawable);

    assertThat(focusRing).isNotNull();
    assertThat(insetDrawable.getDrawable()).isSameInstanceAs(focusRing);
    Drawable fromSharedState = sharedState.newDrawable();
    assertThat(fromSharedState).isInstanceOf(InsetDrawable.class);
    assertThat(((InsetDrawable) fromSharedState).getDrawable())
        .isInstanceOf(ColorDrawable.class);
    assertThat(FocusRingDrawable.find(fromSharedState)).isNull();
  }

  private static Drawable.Callback createCountingCallback(AtomicInteger invalidationCount) {
    return new Drawable.Callback() {
      @Override
      public void invalidateDrawable(@NonNull Drawable who) {
        invalidationCount.incrementAndGet();
      }

      @Override
      public void scheduleDrawable(
          @NonNull Drawable who, @NonNull Runnable what, long when) {}

      @Override
      public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {}
    };
  }
}
