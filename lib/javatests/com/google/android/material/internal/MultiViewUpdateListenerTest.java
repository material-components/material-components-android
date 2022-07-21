/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.internal.MultiViewUpdateListener.Listener;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MultiViewUpdateListenerTest {

  private final float expectedAnimatedValue = 0.5f;

  private ValueAnimator valueAnimator;

  @Before
  public void setUp() {
    valueAnimator = generateValueAnimator(expectedAnimatedValue);
  }

  @Test
  public void givenCollectionViews_whenOnAnimationUpdate_thenUpdatesAllViews() {
    Listener listener = (animator, view) -> view.setAlpha((Float) animator.getAnimatedValue());
    List<View> views = generateViews();

    new MultiViewUpdateListener(listener, views).onAnimationUpdate(valueAnimator);

    for (View view : views) {
      assertEquals(expectedAnimatedValue, view.getAlpha(), 0.01);
    }
  }

  @Test
  public void givenVarArgConstructor_whenOnAnimationUpdate_thenUpdatesAllViews() {
    Listener listener = (animator, view) -> view.setAlpha((Float) animator.getAnimatedValue());
    View view1 = new View(ApplicationProvider.getApplicationContext());
    View view2 = new View(ApplicationProvider.getApplicationContext());

    new MultiViewUpdateListener(listener, view1, view2).onAnimationUpdate(valueAnimator);

    assertEquals(expectedAnimatedValue, view1.getAlpha(), 0.01);
    assertEquals(expectedAnimatedValue, view2.getAlpha(), 0.01);
  }

  @Test
  public void givenAlphaListener_whenOnAnimationUpdate_thenUpdatesViewAlpha() {
    View view = new View(ApplicationProvider.getApplicationContext());

    MultiViewUpdateListener.alphaListener(view).onAnimationUpdate(valueAnimator);

    assertEquals(expectedAnimatedValue, view.getAlpha(), 0.01);
  }

  @Test
  public void givenScaleListener_whenOnAnimationUpdate_thenUpdatesViewScaleXAndY() {
    View view = new View(ApplicationProvider.getApplicationContext());

    MultiViewUpdateListener.scaleListener(view).onAnimationUpdate(valueAnimator);

    assertEquals(expectedAnimatedValue, view.getScaleX(), 0.01);
    assertEquals(expectedAnimatedValue, view.getScaleY(), 0.01);
  }

  @Test
  public void givenTranslationXListener_whenOnAnimationUpdate_thenUpdatesViewTranslationX() {
    View view = new View(ApplicationProvider.getApplicationContext());

    MultiViewUpdateListener.translationXListener(view).onAnimationUpdate(valueAnimator);

    assertEquals(expectedAnimatedValue, view.getTranslationX(), 0.01);
  }

  @Test
  public void givenTranslationYListener_whenOnAnimationUpdate_thenUpdatesViewTranslationY() {
    View view = new View(ApplicationProvider.getApplicationContext());

    MultiViewUpdateListener.translationYListener(view).onAnimationUpdate(valueAnimator);

    assertEquals(expectedAnimatedValue, view.getTranslationY(), 0.01);
  }

  private static List<View> generateViews() {
    Context context = ApplicationProvider.getApplicationContext();
    List<View> views = new ArrayList<>();
    views.add(new View(context));
    views.add(new View(context));
    views.add(new View(context));
    return views;
  }

  private static ValueAnimator generateValueAnimator(float expectedAnimatedValue) {
    ValueAnimator valueAnimator = mock(ValueAnimator.class);
    when(valueAnimator.getAnimatedValue()).thenReturn(expectedAnimatedValue);
    return valueAnimator;
  }
}
