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

import static com.google.android.material.carousel.CarouselHelper.createCarouselWithSizeAndOrientation;
import static com.google.android.material.carousel.CarouselHelper.createCarouselWithWidth;
import static com.google.android.material.carousel.CarouselHelper.getKeylineMaskPercentage;
import static com.google.common.truth.Truth.assertThat;

import com.google.android.material.carousel.CarouselStrategy.StrategyType;
import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link KeylineStateList}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class KeylineStateListTest {

  @Test
  public void testCenterArrangement_shouldShiftStart() {
    final float[][] centerStepsStartLocOffsets =
        new float[][] {
          new float[] {5F, 20F, 50F, 90F, 120F, 135F},
          new float[] {10F, 40F, 80F, 110F, 125F, 135F},
          new float[] {20F, 60F, 90F, 110F, 125F, 135F}
        };
    KeylineState state =
        new KeylineState.Builder(40F, 140)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(140), state, 0, 0, 0, StrategyType.CONTAINED);
    float[] scrollSteps = new float[] {50F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(centerStepsStartLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testCenterArrangement_shouldCreateIntermediaryStates() {
    final float[][] centerStepsStartLocOffsets =
        new float[][] {
          new float[] {5F, 20F, 50F, 90F, 120F, 135F},
          new float[] {10F, 40F, 80F, 110F, 125F, 135F},
          new float[] {20F, 60F, 90F, 110F, 125F, 135F}
        };
    KeylineState state =
        new KeylineState.Builder(40F, 140)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(140), state, 0, 0, 0, StrategyType.CONTAINED);
    float[] scrollOffsets = new float[] {35F, 10F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollOffsets.length; j++) {
      KeylineState s = stateList.getShiftedState(scrollOffsets[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isAtLeast(centerStepsStartLocOffsets[j][i]);
        assertThat(s.getKeylines().get(i).locOffset).isAtMost(centerStepsStartLocOffsets[j + 1][i]);
      }
    }
  }

  @Test
  public void testCenterArrangement_shouldShiftEnd() {
    final float[][] centerStepsEndLocOffsets =
        new float[][] {
          new float[] {5F, 20F, 50F, 90F, 120F, 135F},
          new float[] {5F, 15F, 30F, 60F, 100F, 130F},
          new float[] {5F, 15F, 30F, 50F, 80F, 120F}
        };

    KeylineState state =
        new KeylineState.Builder(40F, 140)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(140), state, 0, 0, 0, StrategyType.CONTAINED);
    float[] scrollSteps = new float[] {50F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(centerStepsEndLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testCenterArrangement_shouldNotShift() {
    KeylineState state =
        new KeylineState.Builder(40F, 140)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(140), state, 0, 0, 0, StrategyType.CONTAINED);

    float minScroll = 0F;
    float maxScroll = 5 * 40F;
    KeylineState expected = stateList.getDefaultState();
    assertThat(stateList.getShiftedState(60F, minScroll, maxScroll)).isEqualTo(expected);
  }

  @Test
  public void testStartArrangement_shouldShiftStart() {
    KeylineState state =
        new KeylineState.Builder(40F, 70)
            .addKeyline(20F, 0F, 40F, true)
            .addKeyline(50F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(65F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(70), state, 0, 0, 0, StrategyType.CONTAINED);

    float[] locOffsets = new float[] {20F, 50F, 65F};

    List<Keyline> actual = stateList.getStartState().getKeylines();
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testStartArrangement_shouldShiftEnd() {
    float[][] endStepsLocOffsets =
        new float[][] {
          new float[] {20F, 50F, 65F},
          new float[] {5F, 30F, 60F},
          new float[] {5F, 20F, 50F},
        };

    KeylineState state =
        new KeylineState.Builder(40F, 70)
            .addKeyline(20F, 0F, 40F, true)
            .addKeyline(50F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(65F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(70), state, 0, 0, 0, StrategyType.CONTAINED);
    float[] scrollSteps = new float[] {50F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 2 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(endStepsLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testStartArrangementWithOutOfBoundsKeylines_shouldShiftStart() {
    float[][] startStepsLocOffsets =
        new float[][] {
          new float[] {-10F, 10F, 40F, 70F, 90F},
          new float[] {-10F, 20F, 50F, 70F, 90F}
        };

    KeylineState state =
        new KeylineState.Builder(40F, 90)
            .addAnchorKeyline(-10F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 40F), 20F, false)
            .addKeyline(40F, 0F, 40F, true)
            .addKeyline(70F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(90F, getKeylineMaskPercentage(20F, 40F), 20F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(90), state, 0, 0, 0, StrategyType.CONTAINED);

    float[] scrollSteps = new float[] {20F, 0F};
    float minScroll = 0F;
    float maxScroll = 3 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(startStepsLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testStartArrangementWithOutOfBoundsKeyline_shouldShiftEnd() {
    float[][] endStepsLocOffsets =
        new float[][] {
            // keyline sizes are as follows: {20, 20, 20, 20, 40, 20, 20}
            new float[] {-50F, -30F, -10F, 10F, 40F, 70F, 90F},
            // keyline sizes are as follows: {20, 20, 20, 20, 20, 40, 20}
            new float[] {-50F, -30F, -10F, 10F, 30F, 60F, 90F},
            // keyline sizes are as follows: {20, 20, 20, 20, 20, 20, 40}
            new float[] {-50F, -30F, -10F, 10F, 30F, 50F, 80F},
        };

    KeylineState state =
        new KeylineState.Builder(40F, 100)
            .addAnchorKeyline(-10F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 40F), 20F, false)
            .addKeyline(40F, 0F, 40F, true)
            .addKeylineRange(70F, getKeylineMaskPercentage(20F, 40F), 20F, 3)
            .addKeyline(130F, getKeylineMaskPercentage(20F, 40F), 20F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(100), state, 0, 0, 0, StrategyType.CONTAINED);

    float[] scrollSteps = new float[] {40F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s =
          stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll, true);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(endStepsLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testEndArrangement_shouldShiftStart() {
    final float[][] stepLocOffsets =
        new float[][] {
          new float[] {5F, 20F, 50F},
          new float[] {10F, 40F, 65F},
          new float[] {20F, 50F, 65F}
        };
    KeylineState state =
        new KeylineState.Builder(40F, 70)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(50F, 0F, 40F, true)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(70), state, 0, 0, 0, StrategyType.CONTAINED);
    float[] scrollSteps = new float[] {50F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(stepLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testEndArrangement_shouldNotShiftEnd() {
    KeylineState state =
        new KeylineState.Builder(40F, 70)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(50F, 0F, 40F, true)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(70), state, 0, 0, 0, StrategyType.CONTAINED);
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    KeylineState shiftedState = stateList.getShiftedState(maxScroll, minScroll, maxScroll);
    assertThat(shiftedState).isEqualTo(stateList.getDefaultState());
  }

  @Test
  public void testFullScreenArrangementWithAnchorKeylines_nothingShifts() {
    float[] locOffsets = new float[] {-5F, 20F, 45F};

    KeylineState state =
        new KeylineState.Builder(40F, 40)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(210F, 40F), 10F)
            .addKeyline(20F, 0F, 40F, true)
            .addAnchorKeyline(45F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(40), state, 0, 0, 0, StrategyType.CONTAINED);

    List<Keyline> startStep = stateList.getStartState().getKeylines();
    List<Keyline> endStep = stateList.getEndState().getKeylines();
    for (int i = 0; i < locOffsets.length; i++) {
      assertThat(startStep.get(i).locOffset).isEqualTo(locOffsets[i]);
      assertThat(endStep.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testMultipleFocalItems_shiftsFocalRange() {
    KeylineState state =
        new KeylineState.Builder(100F, 500)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 4, true)
            .addKeyline(475F, .5F, 50F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(500), state, 0, 0, 0, StrategyType.CONTAINED);

    assertThat(stateList.getStartState().getFirstFocalKeylineIndex()).isEqualTo(0);
    assertThat(stateList.getStartState().getLastFocalKeylineIndex()).isEqualTo(3);
  }

  @Test
  public void testKeylineStateForPosition() {
    KeylineState state =
        new KeylineState.Builder(100F, 500)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 4, true)
            .addKeyline(475F, .5F, 50F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(500), state, 0, 0, 0, StrategyType.CONTAINED);
    int itemCount = 10;
    Map<Integer, KeylineState> positionMap =
        stateList.getKeylineStateForPositionMap(itemCount, 0, 1000, false);
    float latestKeylineLoc = positionMap.get(0).getFirstFocalKeyline().loc;

    assertThat(latestKeylineLoc).isEqualTo(stateList.getStartState().getFirstFocalKeyline().loc);
    // Test that the keylines are always increasing
    for (int i = 1; i < itemCount; i++) {
      KeylineState k = positionMap.get(i);
      if (k == null) {
        k = stateList.getDefaultState();
      }
      float keylineLoc = k.getFirstFocalKeyline().loc;
      assertThat(keylineLoc).isAtLeast(latestKeylineLoc);
      latestKeylineLoc = keylineLoc;
    }
    assertThat(latestKeylineLoc).isEqualTo(stateList.getEndState().getFirstFocalKeyline().loc);
  }

  @Test
  public void testKeylineStateForPositionRTL() {
    KeylineState state =
        new KeylineState.Builder(100F, 500)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 4, true)
            .addKeyline(475F, .5F, 50F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(500), state, 0, 0, 0, StrategyType.CONTAINED);
    int itemCount = 10;
    Map<Integer, KeylineState> positionMap =
        stateList.getKeylineStateForPositionMap(
            itemCount, -1000, 0, true);
    float latestKeylineLoc = positionMap.get(0).getFirstFocalKeyline().loc;

    assertThat(latestKeylineLoc).isEqualTo(stateList.getEndState().getFirstFocalKeyline().loc);
    // Test that the keylines are always decreasing
    for (int i = 1; i < itemCount; i++) {
      KeylineState k = positionMap.get(i);
      if (k == null) {
        k = stateList.getDefaultState();
      }
      float keylineLoc = k.getFirstFocalKeyline().loc;
      assertThat(keylineLoc).isAtMost(latestKeylineLoc);
      latestKeylineLoc = keylineLoc;
    }
    assertThat(latestKeylineLoc).isEqualTo(stateList.getStartState().getFirstFocalKeyline().loc);
  }

  @Test
  public void testKeylineStateForPositionVertical() {
    KeylineState state =
        new KeylineState.Builder(100F, 500)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 4, true)
            .addKeyline(475F, .5F, 50F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(
            createCarouselWithSizeAndOrientation(500, CarouselLayoutManager.VERTICAL),
            state,
            0,
            0,
            0,
            StrategyType.CONTAINED);
    int itemCount = 10;
    Map<Integer, KeylineState> positionMap = stateList.getKeylineStateForPositionMap(
        itemCount, 0, 1000, false);
    float latestKeylineLoc = positionMap.get(0).getFirstFocalKeyline().loc;

    assertThat(latestKeylineLoc).isEqualTo(stateList.getStartState().getFirstFocalKeyline().loc);
    // Test that the keylines are always increasing
    for (int i = 1; i < itemCount; i++) {
      KeylineState k = positionMap.get(i);
      if (k == null) {
        k = stateList.getDefaultState();
      }
      float keylineLoc = k.getFirstFocalKeyline().loc;
      assertThat(keylineLoc).isAtLeast(latestKeylineLoc);
      latestKeylineLoc = keylineLoc;
    }
    assertThat(latestKeylineLoc).isEqualTo(stateList.getEndState().getFirstFocalKeyline().loc);
  }

  @Test
  public void testCutoffEndKeylines_changeEndKeylineLocOffsets() {
    float[][] endStepsLocOffsets =
        new float[][] {
            // keyline sizes are as follows: {large, large, cutoff-large}
            new float[] {-5F, 20F, 60F, 100F, 125F},
            // keyline sizes are as follows: {cutoff-large, large, large}
            new float[] {-25F, 0F, 40F, 80F, 105F},
        };

    // Carousel size is 100, with 2 larges and a cutoff large
    KeylineState state =
        new KeylineState.Builder(40F, 100)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, 0F, 40F, /* isFocal= */ true)
            .addKeyline(60F, 0F, 40F, /* isFocal= */ true)
            .addKeyline(100F, 0F, 40F, /* isFocal= */ true)
            .addAnchorKeyline(125F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(100), state, 0, 0, 0, StrategyType.CONTAINED);

    float[] scrollSteps = new float[] {40F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 80F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(endStepsLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testCutoffStartKeylines_doesNotChangeEndKeylineLocOffsets() {
    float[][] endStepsLocOffsets =
        new float[][] {
            // keyline sizes are as follows: {cutoff-large, large, large}
            new float[] {-25F, 0F, 40F, 80F, 105F},
            // keyline sizes are as follows: {cutoff-large, large, large}
            new float[] {-25F, 0F, 40F, 80F, 105F},
        };

    // Carousel size is 100, with cutoff large and 2 larges
    KeylineState state =
        new KeylineState.Builder(40F, 100)
            .addAnchorKeyline(-25F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(0F, 0F, 40F, /* isFocal= */ true)
            .addKeyline(40F, 0F, 40F, /* isFocal= */ true)
            .addKeyline(80F, 0F, 40F, /* isFocal= */ true)
            .addAnchorKeyline(105F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList =
        KeylineStateList.from(createCarouselWithWidth(100), state, 0, 0, 0, StrategyType.CONTAINED);

    float[] scrollSteps = new float[] {40F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 80F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll);
      for (int i = 0; i < s.getKeylines().size(); i++) {
        assertThat(s.getKeylines().get(i).locOffset).isEqualTo(endStepsLocOffsets[j][i]);
      }
    }
  }

  @Test
  public void testStartPadding_shiftsContainedStartState() {
    // Default state: [small, large, small] where small is 20F and large is 60F
    KeylineState state =
        new KeylineState.Builder(60F, 100)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(10F, 60F), 10F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addKeyline(50F, 0F, 60F, true)
            .addKeyline(90F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addAnchorKeyline(105F, getKeylineMaskPercentage(10F, 60F), 10F)
            .build();
    Carousel carousel = createCarouselWithWidth(100);
    KeylineStateList stateList =
        KeylineStateList.from(
            carousel,
            state,
            /* itemMargins= */ 0,
            /* leftOrTopPadding= */ 12,
            /* rightOrBottomPadding= */20,
            /* strategyType= */ StrategyType.CONTAINED);

    // Normally start state is expected to have locOffests of [30F, 70F, 90F] but with a start
    // padding of 12, it should be evenly decreased from all items. So the first item should start
    // at padding + new item size/2F = 12F + (60F - 4F)/2F = 12F + 28F = 40F.
    float[] locOffsets = new float[] {-5F, 40F, 76F, 92F, 105F};

    List<Keyline> actual = stateList.getStartState().getKeylines();
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testEndPadding_shiftsContainedEndState() {
    // Default state: [small, large, small] where small is 20F and large is 60F
    KeylineState state =
        new KeylineState.Builder(60F, 100)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(10F, 60F), 10F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addKeyline(50F, 0F, 60F, true)
            .addKeyline(90F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addAnchorKeyline(105F, getKeylineMaskPercentage(10F, 60F), 10F)
            .build();
    Carousel carousel = createCarouselWithWidth(100);
    KeylineStateList stateList =
        KeylineStateList.from(
            carousel,
            state,
            /* itemMargins= */ 0,
            /* leftOrTopPadding= */ 12,
            /* rightOrBottomPadding= */ 24,
            /* strategyType= */ StrategyType.CONTAINED);

    // Normally start state is expected to have locOffests of [10F, 30F, 70F] but with an end
    // padding of 24, it should be evenly decreased from all items.
    float[] locOffsets = new float[] {-5F, 6F, 18F, 50F, 105F};

    List<Keyline> actual = stateList.getEndState().getKeylines();
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testStartPadding_shiftsUncontainedStartState() {
    // Default state: [small, large, small] where small is 20F and large is 60F
    KeylineState state =
        new KeylineState.Builder(60F, 100)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(10F, 60F), 10F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addKeyline(50F, 0F, 60F, true)
            .addKeyline(90F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addAnchorKeyline(105F, getKeylineMaskPercentage(10F, 60F), 10F)
            .build();
    Carousel carousel = createCarouselWithWidth(100);
    KeylineStateList stateList =
        KeylineStateList.from(
            carousel,
            state,
            /* itemMargins= */ 0,
            /* leftOrTopPadding= */ 12,
            /* rightOrBottomPadding= */20,
            /* strategyType= */ StrategyType.UNCONTAINED);

    // Normally start state is expected to have locOffests of [30F, 70F, 90F]. Shift by left padding
    float[] locOffsets = new float[] {-5F, 42F, 82F, 102F, 117F};
    float[] cutoffs = new float[] {10F, 0F, 0F, 12F, 22F};

    List<Keyline> actual = stateList.getStartState().getKeylines();
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i).locOffset).isEqualTo(locOffsets[i]);
      assertThat(actual.get(i).cutoff).isEqualTo(cutoffs[i]);
    }
  }

  @Test
  public void testEndPadding_shiftsUncontainedEndState() {
    // Default state: [small, large, small] where small is 20F and large is 60F
    KeylineState state =
        new KeylineState.Builder(60F, 100)
            .addAnchorKeyline(-5F, getKeylineMaskPercentage(10F, 60F), 10F)
            .addKeyline(10F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addKeyline(50F, 0F, 60F, true)
            .addKeyline(90F, getKeylineMaskPercentage(20F, 60F), 20F)
            .addAnchorKeyline(105F, getKeylineMaskPercentage(10F, 60F), 10F)
            .build();
    Carousel carousel = createCarouselWithWidth(100);
    KeylineStateList stateList =
        KeylineStateList.from(
            carousel,
            state,
            /* itemMargins= */ 0,
            /* leftOrTopPadding= */ 12,
            /* rightOrBottomPadding= */ 24,
            /* strategyType= */ StrategyType.UNCONTAINED);

    // Normally end state is expected to have locOffsets of [10F, 30F, 70F]. Shift by right
    // padding
    float[] locOffsets = new float[] {-29F, -14F, 6F, 46F, 105F};
    float[] cutoffs = new float[] {34F, 24F, 4F, 0F, 10F};

    List<Keyline> actual = stateList.getEndState().getKeylines();
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i).locOffset).isEqualTo(locOffsets[i]);
      assertThat(actual.get(i).cutoff).isEqualTo(cutoffs[i]);
    }
  }
}
