/*
 * Copyright 2022 The Android Open Source Project
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

import static com.google.android.material.carousel.CarouselHelper.createCarousel;
import static com.google.android.material.carousel.CarouselHelper.createCarouselWithItemCount;
import static com.google.android.material.carousel.CarouselHelper.createCarouselWithWidth;
import static com.google.android.material.carousel.CarouselHelper.createViewWithSize;
import static com.google.common.truth.Truth.assertThat;

import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.carousel.KeylineState.Keyline;
import com.google.common.collect.Iterables;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link MultiBrowseCarouselStrategy}. */
@RunWith(RobolectricTestRunner.class)
public class MultiBrowseCarouselStrategyTest {

  @Test
  public void testOnFirstItemMeasuredWithMargins_createsKeylineStateWithCorrectItemSize() {
    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 200, 200);

    KeylineState keylineState =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(584), view);
    assertThat(keylineState.getItemSize()).isEqualTo(200F);
  }

  @Test
  public void testItemLargerThanContainer_resizesToFit() {
    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 400);

    KeylineState keylineState =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(100), view);
    assertThat(keylineState.getItemSize()).isAtMost(100F);
  }

  @Test
  public void testItemLargerThanContainerSize_defaultsToOneLargeOneSmall() {
    Carousel carousel = createCarouselWithWidth(100);
    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 400);

    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);
    float minSmallItemSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);

    // A fullscreen layout should be [xSmall-large-small-xSmall] where the xSmall items are
    // outside the bounds of the carousel container and the large center item takes up the
    // containers full width.
    assertThat(keylineState.getKeylines()).hasSize(4);
    assertThat(keylineState.getKeylines().get(0).locOffset).isLessThan(0F);
    assertThat(Iterables.getLast(keylineState.getKeylines()).locOffset)
        .isGreaterThan((float) carousel.getContainerWidth());
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(2).maskedItemSize).isEqualTo(minSmallItemSize);
  }


  @Test
  public void testSmallContainer_shouldShowOneLargeItem() {
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 100, 400);
    float minSmallItemSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
    // Create a carousel that will not fit a large and small item where the large item is at least
    // as big as the min small item.
    int carouselWidth = (int) (minSmallItemSize * 1.5f);
    Carousel carousel = createCarouselWithWidth(carouselWidth);

    MultiBrowseCarouselStrategy config = setupStrategy();
    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    assertThat(keylineState.getKeylines()).hasSize(3);
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo((float) carouselWidth);
  }

  @Test
  public void testContainer_shouldShowOneLargeOneSmallItem() {
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 100, 400);
    float minSmallItemSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
    // Create a carousel that will fit at least one small item and one larger item
    int carouselWidth = ((int) (minSmallItemSize * 2f)) + 1;
    Carousel carousel = createCarouselWithWidth(carouselWidth);

    MultiBrowseCarouselStrategy config = setupStrategy();
    KeylineState keylineState = config.onFirstChildMeasuredWithMargins(carousel, view);

    assertThat(keylineState.getKeylines()).hasSize(4);
    assertThat(keylineState.getKeylines().get(2).maskedItemSize).isEqualTo(minSmallItemSize);
  }

  @Test
  public void testKnownArrangementWithMediumItem_correctlyCalculatesKeylineLocations() {
    float[] locOffsets = new float[] {-.5F, 100F, 300F, 464F, 556F, 584.5F};

    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 200, 200);

    List<Keyline> keylines =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(584), view).getKeylines();
    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testKnownArrangementWithoutMediumItem_correctlyCalculatesKeylineLocations() {
    float[] locOffsets = new float[] {-.5F, 100F, 300F, 428F, 456.5F};

    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 200, 200);

    List<Keyline> keylines =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(456), view).getKeylines();
    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testArrangementFit_onlyAdjustsMediumSizeUp() {
    float largeSize = 56F * 3F;
    float smallSize = 56F;
    float mediumSize = (largeSize + smallSize) / 2F;
    float maxMediumAdjustment = mediumSize * .1F;
    // Create a carousel that is larger than 1 of each items added together but within the range of
    // the medium item being able to flex to fit the space.
    int carouselSize = (int) (largeSize + mediumSize + smallSize + maxMediumAdjustment);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), (int) largeSize, (int) largeSize);
    KeylineState keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(carouselSize), view);

    // Large and small items should not be adjusted in size by the strategy
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo(largeSize);
    assertThat(keylineState.getKeylines().get(3).maskedItemSize).isEqualTo(smallSize);
    // The medium item should use its flex to fit the arrangement
    assertThat(keylineState.getKeylines().get(2).maskedItemSize).isGreaterThan(mediumSize);
  }

  @Test
  public void testArrangementFit_onlyAdjustsMediumSizeDown() {
    float largeSize = 40F * 3F;
    float smallSize = 40F;
    float mediumSize = (largeSize + smallSize) / 2F;
    float maxMediumAdjustment = mediumSize * .1F;
    int carouselSize = (int) (largeSize + mediumSize + smallSize - maxMediumAdjustment);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), (int) largeSize, (int) largeSize);
    KeylineState keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(carouselSize), view);

    // Large and small items should not be adjusted in size by the strategy
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo(largeSize);
    assertThat(keylineState.getKeylines().get(3).maskedItemSize).isEqualTo(smallSize);
    // The medium item should use its flex to fit the arrangement
    assertThat(keylineState.getKeylines().get(2).maskedItemSize).isLessThan(mediumSize);
  }

  @Test
  public void testArrangementFit_onlyAdjustsSmallSizeDown() {
    float largeSize = 56F * 3;
    float smallSize = 56F;
    float mediumSize = (largeSize + smallSize) / 2F;

    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), (int) largeSize, (int) largeSize);
    float minSmallSize = view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
    int carouselSize = (int) (largeSize + mediumSize + minSmallSize);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    KeylineState keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(carouselSize), view);

    // Large items should not change
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo(largeSize);
    // Small items should be adjusted to the small size
    assertThat(keylineState.getKeylines().get(3).maskedItemSize).isEqualTo(minSmallSize);
  }

  @Test
  public void testArrangementFit_onlyAdjustsSmallSizeUp() {
    float largeSize = 40F * 3;
    float smallSize = 40F;
    float mediumSize = (largeSize + smallSize) / 2F;

    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), (int) largeSize, (int) largeSize);
    float maxSmallSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_max);
    int carouselSize = (int) (largeSize + mediumSize + maxSmallSize);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    KeylineState keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(carouselSize), view);

    // Large items should not change
    assertThat(keylineState.getKeylines().get(1).maskedItemSize).isEqualTo(largeSize);
    // Small items should be adjusted to the small size
    assertThat(keylineState.getKeylines().get(3).maskedItemSize).isEqualTo(maxSmallSize);
  }

  @Test
  public void testKnownCenterAlignmentArrangement_correctlyCalculatesKeylineLocations() {
    float largeSize = 40F * 3; // 120F
    float smallSize = 40F;
    float mediumSize = (largeSize + smallSize) / 2F; // 80F

    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), (int) largeSize, (int) largeSize);
    int carouselSize = (int) (largeSize + mediumSize * 2 + smallSize * 2);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    List<Keyline> keylines =
        strategy.onFirstChildMeasuredWithMargins(
            createCarousel(
                carouselSize,
                carouselSize,
                CarouselLayoutManager.HORIZONTAL,
                CarouselLayoutManager.ALIGNMENT_CENTER), view).getKeylines();

    float[] locOffsets = new float[] {-.5F, 20F, 80F, 180F, 280F, 340F, 360.5F};

    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testLessItemsThanKeylines_updatesStrategy() {
    MultiBrowseCarouselStrategy config = setupStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 200, 200);

    // With a carousel of size 500 and large item size of 200, the keylines will be
    // {xsmall, large, large, medium, small, xsmall}
    Carousel carousel =
        createCarouselWithItemCount(
            /* size= */ 500, CarouselLayoutManager.ALIGNMENT_START, /* itemCount= */ 4);
    KeylineState keylineState =
        config.onFirstChildMeasuredWithMargins(carousel, view);

    // An item count of 4 should not affect the keyline number.
    assertThat(keylineState.getKeylines()).hasSize(6);

    carousel =
        createCarouselWithItemCount(
            /* size= */ 500, CarouselLayoutManager.ALIGNMENT_START, /* itemCount= */ 3);
    keylineState =
        config.onFirstChildMeasuredWithMargins(carousel, view);

    // An item count of 3 should change the keyline number to be 3: {xsmall, large, large, medium,
    // xsmall}
    assertThat(keylineState.getKeylines()).hasSize(5);

    carousel = createCarouselWithItemCount(500, CarouselLayoutManager.ALIGNMENT_START, 2);
    keylineState =
        config.onFirstChildMeasuredWithMargins(carousel, view);

    // An item count of 2 should have the keyline number to be 5:
    // {xsmall, large, large, medium, xsmall} because even with only 2 items, we still want a medium
    // keyline so the carousel is not just large items.
    assertThat(keylineState.getKeylines()).hasSize(5);
  }

  @Test
  public void testSettingSmallRange_updatesSmallSize() {
    View view =
        createViewWithSize(
            ApplicationProvider.getApplicationContext(), 100, 100);

    MultiBrowseCarouselStrategy strategy = setupStrategy();
    KeylineState keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(400), view);

    List<Keyline> keylines = keylineState.getKeylines();
    float originalSmallSize = keylines.get(keylines.size() - 2).maskedItemSize;

    strategy.setSmallItemSizeMin(20);
    strategy.setSmallItemSizeMax(20);
    keylineState =
        strategy.onFirstChildMeasuredWithMargins(createCarouselWithWidth(400), view);
    keylines = keylineState.getKeylines();

    assertThat(originalSmallSize).isNotEqualTo(20f);
    // Small items should be adjusted to the small size
    assertThat(keylines.get(keylines.size() - 2).maskedItemSize).isEqualTo(20f);
  }

  private MultiBrowseCarouselStrategy setupStrategy() {
    MultiBrowseCarouselStrategy strategy = new MultiBrowseCarouselStrategy();
    strategy.initialize(ApplicationProvider.getApplicationContext());
    return strategy;
  }
}
