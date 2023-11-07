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
import static com.google.android.material.carousel.CarouselHelper.createCenterAlignedCarouselWithSize;
import static com.google.android.material.carousel.CarouselHelper.createViewWithSize;
import static com.google.android.material.carousel.CarouselStrategyHelper.getSmallSizeMin;
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
    UncontainedCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);
    float xSmallSize =
        view.getResources().getDimension(R.dimen.m3_carousel_gone_size);

    // A fullscreen layout should be [xSmall-large-xSmall] where the xSmall items are
    // outside the bounds of the carousel container and the large item takes up the
    // containers full width.
    assertThat(keylineState.getKeylines()).hasSize(3);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).locOffset)
        .isEqualTo(carousel.getContainerWidth() + xSmallSize / 2F);
  }

  @Test
  public void testLargeItem_largerThanFullCarouselWidth() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = setupStrategy();
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
    UncontainedCarouselStrategy config = setupStrategy();
    // With size 125px, 3 large items can fit with in 400px, with 25px left. 25px * 3 = 75px, which
    // will be the size of the medium item since it can be a third cut off and it is less than the
    // threshold percentage * large item size.
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
  public void testRemainingSpaceWithItemSize_fitsMediumItemWithCutoff() {
    Carousel carousel = createCarouselWithWidth(400);
    UncontainedCarouselStrategy config = setupStrategy();
    int itemSize = 105;
    // With size 105px, 3 large items can fit with in 400px, with 85px left over.  85*3 = 255 which
    // is well over the size of the large item, so the medium size will be limited to whichever is
    // larger between 85% of the large size, or 110% of the remainingSpace to make it at most
    // 10% cut off.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), itemSize, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-large-large-large-medium-xSmall]
    assertThat(keylineState.getKeylines()).hasSize(6);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).mask).isEqualTo(0F);
    // remainingSpace * 120%
    assertThat(keylineState.getKeylines().get(4).maskedItemSize).isEqualTo(85*1.2F);
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testCenterAligned_defaultKeylineHasTwoCutoffs() {
    Carousel carousel = createCenterAlignedCarouselWithSize(400);
    UncontainedCarouselStrategy config = setupStrategy();
    int itemSize = 250;
    // With this item size, we have 400 - 250 = 150 remaining space which means 75 on each side
    // of one focal item.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), itemSize, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-medium-large-medium-xSmall]
    assertThat(keylineState.getKeylines()).hasSize(5);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).cutoff)
        .isEqualTo(150F); // 75*2 since 2/3 should be cut off
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).cutoff)
        .isEqualTo(150F); // 75*2 since 2/3 should be cut off
    assertThat(keylineState.getKeylines().get(4).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testCenterAligned_cutoffMinSize() {
    Carousel carousel = createCenterAlignedCarouselWithSize(400);
    UncontainedCarouselStrategy config = setupStrategy();
    int itemSize = 200;
    // 2 items fit perfectly in the width so there is no remaining space. Medium items should still
    // be the minimum item mask size.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), itemSize, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    float minSmallSize = getSmallSizeMin(ApplicationProvider.getApplicationContext());

    // The layout should be [xSmall-medium-large-large-medium-xSmall]
    assertThat(keylineState.getKeylines()).hasSize(6);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).cutoff)
        .isEqualTo(keylineState.getKeylines().get(1).maskedItemSize);
    assertThat(keylineState.getKeylines().get(1).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo(minSmallSize);
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(4).cutoff)
        .isEqualTo(keylineState.getKeylines().get(1).maskedItemSize);
    assertThat(keylineState.getKeylines().get(4).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
    assertThat(keylineState.getKeylines().get(4).maskedItemSize).isEqualTo(minSmallSize);
    assertThat(keylineState.getKeylines().get(5).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  @Test
  public void testCenterAligned_cutoffMaxSize() {
    Carousel carousel = createCenterAlignedCarouselWithSize(400);
    UncontainedCarouselStrategy config = setupStrategy();
    int itemSize = 140;
    // 2 items fit into width of 400 with 120 remaining space; 60 on each side. Only a 1/3 should be
    // showing which means an item width of 180 for the cut off items, but we do not want these
    // items to be bigger than the focal item so the max item size should be the focal item size.
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), itemSize, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    // The layout should be [xSmall-medium-large-large-medium-xSmall]
    assertThat(keylineState.getKeylines()).hasSize(6);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo((float) itemSize);
    // Item size should be max size: 180F, so 140 - 60 (remaining space) = 80
    assertThat(keylineState.getKeylines().get(1).cutoff).isEqualTo(80F);
    assertThat(keylineState.getKeylines().get(2).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(3).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(4).maskedItemSize).isEqualTo((float) itemSize);
    // Item size should be max size: 180F, so 140 - 60 (remaining space) = 80
    assertThat(keylineState.getKeylines().get(4).cutoff).isEqualTo(80F);
    assertThat(keylineState.getKeylines().get(5).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
  }

  private UncontainedCarouselStrategy setupStrategy() {
    UncontainedCarouselStrategy strategy = new UncontainedCarouselStrategy();
    strategy.initialize(ApplicationProvider.getApplicationContext());
    return strategy;
  }
}
