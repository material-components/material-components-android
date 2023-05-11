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

import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.carousel.KeylineState.Keyline;
import com.google.common.collect.Iterables;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link HeroCarouselStrategy}. */
@RunWith(RobolectricTestRunner.class)
public class HeroCarouselStrategyTest {

  @Test
  public void testItemSameAsContainerSize_showsOneLargeOneSmall() {
    Carousel carousel = createCarouselWithWidth(400);
    HeroCarouselStrategy config = new HeroCarouselStrategy();
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
  public void testItemSmallerThanContainer_showsOneLargeOneSmall() {
    Carousel carousel = createCarouselWithWidth(400);
    HeroCarouselStrategy config = new HeroCarouselStrategy();
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 100, 400);

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
  public void testKnownArrangement_correctlyCalculatesKeylineLocations() {
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 200);

    HeroCarouselStrategy config = new HeroCarouselStrategy();
    float extraSmallSize =
        view.getResources().getDimension(R.dimen.m3_carousel_gone_size);
    float minSmallItemSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
    // Keyline sizes for the hero variant carousel are:
    // {extraSmallSize, largeSize, minSmallSize, extraSmallSize}
    // The keyline loc offsets are placed so that an item centered on a keyline has the
    //  keyline size described above.
    // The large size is based on whatever width is left over from the minimum small size.
    float[] locOffsets =
        new float[] {
          -extraSmallSize / 2f,
          (400 - minSmallItemSize) / 2f,
          400 - minSmallItemSize / 2f,
          400 + extraSmallSize / 2f
        };

    List<Keyline> keylines =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(400), view).getKeylines();
    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testKnownArrangementWithMargins_correctlyCalculatesKeylineLocations() {
    View view = createViewWithSize(ApplicationProvider.getApplicationContext(), 400, 200);
    LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
    layoutParams.leftMargin += 50;
    layoutParams.rightMargin += 30;

    HeroCarouselStrategy config = new HeroCarouselStrategy();
    float extraSmallSize =
        view.getResources().getDimension(R.dimen.m3_carousel_gone_size);
    float minSmallItemSize =
        view.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
    // Keyline sizes for the hero variant carousel are:
    // {extraSmallSize, largeSize, minSmallSize, extraSmallSize}
    // The keyline loc offsets are placed so that an item centered on a keyline has the
    //  keyline size described above.
    // The large size is based on whatever width is left over from the minimum small size.
    float[] locOffsets =
        new float[] {
            -(extraSmallSize + 80) / 2f,
            (400 - (minSmallItemSize + 80)) / 2f,
            400 - (minSmallItemSize + 80) / 2f,
            400 + (extraSmallSize + 80) / 2f
        };

    List<Keyline> keylines =
        config.onFirstChildMeasuredWithMargins(createCarouselWithWidth(400), view).getKeylines();
    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }
}
