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

import static com.google.android.material.carousel.CarouselHelper.getKeylineMaskPercentage;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link KeylineState}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public final class KeylineStateTest {

  private static final float[] BUILT_LOCS =
      new float[] {-1015F, -565F, -115F, 335F, 785F, 1235F, 1685F, 2135F, 2585F, 3035F, 3485F};
  private static final float[] BUILT_LOC_OFFSETS =
      new float[] {5F, 85F, 235F, 435F, 785F, 1235F, 1685F, 2035F, 2235F, 2385F, 2465F};

  @Test
  public void testNoFocalRange_throwsException() {
    assertThrows(
        IllegalStateException.class,
        () -> new KeylineState.Builder(100F, 0).addKeyline(0, .5F, 50F).build());
  }

  @Test
  public void testZeroSizedKeyline_shouldNotAddKeyline() {
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0)
            .addKeyline(50F, 0F, 100F, true)
            .addKeyline(100F, 1F, 0F)
            .build();
    assertThat(keylineState.getKeylines()).hasSize(1);
  }

  @Test
  public void testZeroCountKeylineRange_shouldNotAddKeylines() {
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0)
            .addKeyline(50F, 0F, 100F, true)
            .addKeylineRange(110F, .8F, 20F, 0)
            .build();
    assertThat(keylineState.getKeylines()).hasSize(1);
  }

  @Test
  public void testStartFocalItemWithDifferentSize_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new KeylineState.Builder(100F, 0)
                .addKeyline(25F, .5F, 50F, true)
                .addKeyline(100F, 0F, 100F)
                .addKeyline(200F, 0F, 100F, true)
                .build());
  }

  @Test
  public void testEndFocalItemWithDifferentSize_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new KeylineState.Builder(100F, 0)
                .addKeyline(50F, 0F, 100F, true)
                .addKeyline(150F, 0F, 100F)
                .addKeyline(275F, .5F, 50F, true)
                .build());
  }

  @Test
  public void testMiddleFocalItemWithDifferentSize_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new KeylineState.Builder(100F, 0)
                .addKeyline(50F, 0F, 100F, true)
                .addKeyline(125F, .5F, 50F)
                .addKeyline(200F, 0F, 100F, true)
                .build());
  }

  @Test
  public void testKeylines_areSortedByOffsetLocation() {
    KeylineState keylineState = createKeylineStateBuilder().build();
    assertThat(keylineState.getKeylines())
        .isInOrder((Comparator<Keyline>) (k1, k2) -> (int) (k1.locOffset - k2.locOffset));
  }

  @Test
  public void testReverseKeylines_shouldReverse() {
    int recyclerWidth = 100;
    // Extra small items are 10F, Small items are 50F, large items are 100F
    KeylineState keylineState =
        new KeylineState.Builder(100F, recyclerWidth)
            // left edge of xSmall item is -10 from left edge of carousel container
            .addKeyline(-5F, getKeylineMaskPercentage(10F, 100F), 10F, false, true)
            .addKeyline(50F, 0F, 100F, true)
            .addKeyline(125F, getKeylineMaskPercentage(50F, 100F), 50F)
            // right edge of xSmall item is 60 from right edge of carousel container
            .addKeyline(155F, getKeylineMaskPercentage(10F, 100F), 10F, false, true)
            .build();

    KeylineState expectedState =
        new KeylineState.Builder(100F, recyclerWidth)
            // left edge of xSmall item is -60 from left of carousel container
            .addKeyline(-55F, getKeylineMaskPercentage(10F, 100F), 10F, false, true)
            .addKeyline(-25F, getKeylineMaskPercentage(50, 100F), 50F)
            .addKeyline(50F, 0F, 100F, true)
            // right edge of xSmall item is 10 from right of carousel container
            .addKeyline(105F, getKeylineMaskPercentage(10F, 100F), 10F, false, true)
            .build();
    KeylineState reversedState = KeylineState.reverse(keylineState, recyclerWidth);

    assertThat(reversedState.getKeylines()).hasSize(expectedState.getKeylines().size());
    for (int i = 0; i < reversedState.getKeylines().size(); i++) {
      assertThat(reversedState.getKeylines().get(i).locOffset)
          .isEqualTo(expectedState.getKeylines().get(i).locOffset);
      assertThat(reversedState.getKeylines().get(i).loc)
          .isEqualTo(expectedState.getKeylines().get(i).loc);
      assertThat(reversedState.getKeylines().get(i).mask)
          .isEqualTo(expectedState.getKeylines().get(i).mask);
      assertThat(reversedState.getKeylines().get(i).maskedItemSize)
          .isEqualTo(expectedState.getKeylines().get(i).maskedItemSize);
    }
  }

  @Test
  public void testCenteredArrangement_calculatesOffsetLocations() {
    KeylineState keylineState = createKeylineStateBuilder().build();
    List<Keyline> keylines = keylineState.getKeylines();

    for (int i = 0; i < keylines.size(); i++) {
      assertThat(keylines.get(i).loc).isEqualTo(BUILT_LOCS[i]);
      assertThat(keylines.get(i).locOffset).isEqualTo(BUILT_LOC_OFFSETS[i]);
    }
  }

  @Test
  public void testStartArrangement_hasFocalRangeAtFrontOfList() {
    // Create a keyline state that has a [e-e-p-p] arrangement.
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0)
            .addKeylineRange(50F, 0F, 100F, 2, true)
            .addKeylineRange(325F, .5F, 50F, 2)
            .build();

    assertThat(keylineState.getKeylines().get(0).mask).isEqualTo(0F);
    assertThat(keylineState.getKeylines().get(1).mask).isEqualTo(0F);
  }

  @Test
  public void testAddKeyline_onlyAddsSingleKeyline() {
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0).addKeyline(50F, 0F, 100F, true).build();
    assertThat(keylineState.getKeylines()).hasSize(1);
  }

  @Test
  public void testAddKeylineRange_addsTwoKeylines() {
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0).addKeylineRange(50F, 0F, 100F, 2, true).build();
    assertThat(keylineState.getKeylines()).hasSize(2);
  }

  @Test
  public void testLerpKeylineStates_statesHaveDifferentNumberOfKeylinesShouldThrowException() {
    KeylineState keylineDefaultState =
        new KeylineState.Builder(100F, 0)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 2, true)
            .addKeyline(275F, .5F, 50F)
            .build();
    KeylineState keylineStartState =
        new KeylineState.Builder(100F, 0)
            .addKeylineRange(50F, 0F, 100F, 2, true)
            .addKeylineRange(225F, .5F, 50F, 3)
            .build();

    assertThrows(
        IllegalArgumentException.class,
        () -> KeylineState.lerp(keylineStartState, keylineDefaultState, .5F));
  }

  @Test
  public void testLerpKeylineStates_focalIndicesShiftAtHalfWayPoint() {
    KeylineState keylineDefaultState =
        new KeylineState.Builder(100F, 0)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 2, true)
            .addKeyline(275F, .5F, 50F)
            .build();
    KeylineState keylineStartState =
        new KeylineState.Builder(100F, 0)
            .addKeylineRange(50F, 0F, 100F, 2, true)
            .addKeylineRange(225F, .5F, 50F, 2)
            .build();

    assertThat(keylineDefaultState.getFirstFocalKeylineIndex()).isEqualTo(1);
    assertThat(keylineDefaultState.getLastFocalKeylineIndex()).isEqualTo(2);
    assertThat(keylineStartState.getFirstFocalKeylineIndex()).isEqualTo(0);
    assertThat(keylineStartState.getLastFocalKeylineIndex()).isEqualTo(1);

    // Interpolate less than half way between the start state and default state. The focal indices
    // should remain the same (as the start state) until interpolating half way (< .5F).
    KeylineState lerpedState = KeylineState.lerp(keylineStartState, keylineDefaultState, .4F);
    assertThat(lerpedState.getFirstFocalKeylineIndex())
        .isEqualTo(keylineStartState.getFirstFocalKeylineIndex());
    assertThat(lerpedState.getLastFocalKeylineIndex())
        .isEqualTo(keylineStartState.getLastFocalKeylineIndex());

    // Interpolate half way or more between the start state and the default state. The focal
    // indices shift to equal that of the default state after reaching half way (>= .5F).
    lerpedState = KeylineState.lerp(keylineStartState, keylineDefaultState, .6F);
    assertThat(lerpedState.getFirstFocalKeylineIndex())
        .isEqualTo(keylineDefaultState.getFirstFocalKeylineIndex());
    assertThat(lerpedState.getLastFocalKeylineIndex())
        .isEqualTo(keylineDefaultState.getLastFocalKeylineIndex());

    lerpedState = KeylineState.lerp(keylineStartState, keylineDefaultState, .5F);
    assertThat(lerpedState.getFirstFocalKeylineIndex())
        .isEqualTo(keylineDefaultState.getFirstFocalKeylineIndex());
    assertThat(lerpedState.getLastFocalKeylineIndex())
        .isEqualTo(keylineDefaultState.getLastFocalKeylineIndex());
  }

  @Test
  public void testAddingAnchorKeyline_mustBeAtStartOrEnd() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new KeylineState.Builder(100F, 0)
                .addAnchorKeyline(25F, .5F, 50F)
                .addAnchorKeyline(100F, 0F, 100F)
                .addAnchorKeyline(200F, 0F, 100F)
                .build());
    }

  @Test
  public void testGetFirstNonAnchorKeyline() {
    KeylineState keylineDefaultState =
        new KeylineState.Builder(100F, 0)
            .addAnchorKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 2, true)
            .build();
    assertThat(keylineDefaultState.getFirstNonAnchorKeyline().maskedItemSize).isEqualTo(100F);
  }

  @Test
  public void testGetLastNonAnchorKeyline() {
    KeylineState keylineDefaultState =
        new KeylineState.Builder(100F, 0)
            .addAnchorKeyline(25F, .5F, 50F)
            .addKeyline(100F, 0F, 100F, true)
            .addAnchorKeyline(175F, .5F, 50F)
            .build();
    assertThat(keylineDefaultState.getFirstNonAnchorKeyline().maskedItemSize).isEqualTo(100F);
  }

  @Test
  public void testAnchorKeyline_cannotBeFocal() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new KeylineState.Builder(100F, 0)
                .addKeyline(25F, .5F, 50F, /* isFocal= */ true, /* isAnchor= */ true)
                .build());
  }

  @Test
  public void testGetFocalKeylines() {
    KeylineState keylineState =
        new KeylineState.Builder(100F, 0)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 2, true)
            .addKeyline(275F, .5F, 50F)
            .build();

    List<Keyline> focalKeylines = keylineState.getFocalKeylines();

    assertThat(focalKeylines).isEqualTo(keylineState.getKeylines().subList(1, 3));
  }

  /**
   * Creates a {@link KeylineState.Builder} that has a centered focal range with three large items,
   * and one medium item, two small items, and one extra small item on each side of the focal range.
   *
   * <p>The resulting arrangement looks like (xs = extra small, s = small, m = medium, l = large):
   * [xs-s-s-m-l-l-l-m-s-s-xs]
   */
  private KeylineState.Builder createKeylineStateBuilder() {
    float extraSmallSize = 10F;
    float smallSize = 150F;
    float mediumSize = 250F;
    float largeSize = 450F;

    float extraSmallMask = 1F - (extraSmallSize / largeSize);
    float smallMask = 1F - (smallSize / largeSize);
    float mediumMask = 1F - (mediumSize / largeSize);

    return new KeylineState.Builder(largeSize, 2470)
        .addKeyline(5F, extraSmallMask, extraSmallSize)
        .addKeylineRange(85F, smallMask, smallSize, 2)
        .addKeyline(435F, mediumMask, mediumSize)
        .addKeylineRange(785F, 0F, largeSize, 3, true)
        .addKeyline(2035F, mediumMask, mediumSize)
        .addKeylineRange(2235F, smallMask, smallSize, 2)
        .addKeyline(2465F, extraSmallMask, extraSmallSize);
  }
}
