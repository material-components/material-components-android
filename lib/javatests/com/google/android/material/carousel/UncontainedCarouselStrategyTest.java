/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.carousel;

import com.google.android.material.test.R;

import static com.google.android.material.carousel.CarouselHelper.createCarouselWithWidth;
import static com.google.android.material.carousel.CarouselHelper.createViewWithSize;
import static com.google.common.truth.Truth.assertThat;

import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link UncontainedCarouselStrategy}. */
@RunWith(RobolectricTestRunner.class)
public class UncontainedCarouselStrategyTest {

  @Test
  public void testLargeItem_withFullCarouselWidth() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = new UncontainedCarouselStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);
    float xSmallSize =
        view.getResources().getDimension(R.dimen.m3_carousel_gone_size);

    // A fullscreen layout should be [xSmall-large-xSmall-xSmall] where the xSmall items are
    // outside the bounds of the carousel container and the large item takes up the
    // containers full width.
    assertThat(keylineState.getKeylines()).hasSize(4);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).locOffset).isEqualTo(carousel.getContainerWidth() + xSmallSize/2F);
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testLargeItem_largerThanFullCarouselWidth() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = new UncontainedCarouselStrategy();
    int cutOff = 10;
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400 + cutOff, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-large-xSmall] where the xSmall items are
    // outside the bounds of the carousel container and the large item takes up the
    // containers full width.
    assertThat(keylineState.getKeylines()).hasSize(3);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testRemainingSpaceWithItemSize_fitsItemWithThirdCutoff() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = new UncontainedCarouselStrategy();
    // With size 125px, 3 large items can fit with in 400px, with 25px left over which is less than
    // half 125px. This means that a large keyline will not fit in the remaining space
    // such that any motion is seen when scrolling past the keyline, so a medium item
    // should be added.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 125, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-large-large-large-medium-xSmall] where medium is a size
    // such that a third of it is cut off.
    assertThat(keylineState.getKeylines()).hasSize(6);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(4).locOffset)
        .isLessThan((float) carousel.getContainerWidth());
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testRemainingSpaceWithItemSize_fitsLargeItemWithCutoff() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = new UncontainedCarouselStrategy();
    int itemSize = 105;
    // With size 105px, 3 large items can fit with in 400px, with 85px left over which is more than
    // half 105px. This means that an extra large keyline will fit in the remaining space such
    // that motion is seen when scrolling past the keyline, so it should add a large item.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), itemSize, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-large-large-large-large-xSmall]
    assertThat(keylineState.getKeylines()).hasSize(6);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(4).mask).isEqualTo(0F);
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }
}
