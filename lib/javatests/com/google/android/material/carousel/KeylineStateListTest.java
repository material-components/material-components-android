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

import static com.google.android.material.carousel.CarouselHelper.createCarouselWithWidth;
import static com.google.android.material.carousel.CarouselHelper.getKeylineMaskPercentage;
import static com.google.common.truth.Truth.assertThat;

import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.List;
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(140), state);
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(140), state);
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(140), state);
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeylineRange(50F, 0F, 40F, 2, true)
            .addKeyline(120F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(135F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(140), state);

    float minScroll = 0F;
    float maxScroll = 5 * 40F;
    KeylineState expected = stateList.getDefaultState();
    assertThat(stateList.getShiftedState(60F, minScroll, maxScroll)).isEqualTo(expected);
  }

  @Test
  public void testStartArrangement_shouldShiftStart() {
    KeylineState state =
        new KeylineState.Builder(40F)
            .addKeyline(20F, 0F, 40F, true)
            .addKeyline(50F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(65F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(70), state);

    float[] locOffsets = new float[] {20F, 50F, 65F};

    List<Keyline> actual = stateList.getLeftState().getKeylines();
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
        new KeylineState.Builder(40F)
            .addKeyline(20F, 0F, 40F, true)
            .addKeyline(50F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(65F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(70), state);
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
          new float[] {-10F, 10F, 40F, 70F, 85F},
          new float[] {-10F, 20F, 50F, 70F, 85F}
        };

    KeylineState state =
        new KeylineState.Builder(40F)
            .addKeylineRange(-10F, getKeylineMaskPercentage(20F, 40F), 20F, 2)
            .addKeyline(40F, 0F, 40F, true)
            .addKeyline(70F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(85F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(90), state);

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
          new float[] {-10F, 10F, 40F, 70F, 90F, 110F, 125F},
          new float[] {-10F, 10F, 30F, 60F, 90F, 110F, 125F},
          new float[] {-10F, 10F, 30F, 50F, 80F, 110F, 125F},
        };

    KeylineState state =
        new KeylineState.Builder(40F)
            .addKeylineRange(-10F, getKeylineMaskPercentage(20F, 40F), 20F, 2)
            .addKeyline(40F, 0F, 40F, true)
            .addKeylineRange(70F, getKeylineMaskPercentage(20F, 40F), 20F, 3)
            .addKeyline(125F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(100), state);

    float[] scrollSteps = new float[] {40F, 20F, 0F};
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    for (int j = 0; j < scrollSteps.length; j++) {
      KeylineState s = stateList.getShiftedState(maxScroll - scrollSteps[j], minScroll, maxScroll);
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(50F, 0F, 40F, true)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(70), state);
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
        new KeylineState.Builder(40F)
            .addKeyline(5F, getKeylineMaskPercentage(10F, 40F), 10F)
            .addKeyline(20F, getKeylineMaskPercentage(20F, 40F), 20F)
            .addKeyline(50F, 0F, 40F, true)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(70), state);
    float minScroll = 0F;
    float maxScroll = 5 * 40F;

    KeylineState shiftedState = stateList.getShiftedState(maxScroll, minScroll, maxScroll);
    assertThat(shiftedState).isEqualTo(stateList.getDefaultState());
  }

  @Test
  public void testFullScreenArrangementWithOutOfBoundsKeylines_nothingShifts() {
    float[] locOffsets = new float[] {-5F, 20F, 45F};

    KeylineState state =
        new KeylineState.Builder(40F)
            .addKeyline(-5F, getKeylineMaskPercentage(210F, 40F), 10F)
            .addKeyline(20F, 0F, 40F, true)
            .addKeyline(45F, getKeylineMaskPercentage(10F, 40F), 10F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(40), state);

    List<Keyline> startStep = stateList.getLeftState().getKeylines();
    List<Keyline> endStep = stateList.getRightState().getKeylines();
    for (int i = 0; i < locOffsets.length; i++) {
      assertThat(startStep.get(i).locOffset).isEqualTo(locOffsets[i]);
      assertThat(endStep.get(i).locOffset).isEqualTo(locOffsets[i]);
    }
  }

  @Test
  public void testMultipleFocalItems_shiftsFocalRange() {
    KeylineState state =
        new KeylineState.Builder(100F)
            .addKeyline(25F, .5F, 50F)
            .addKeylineRange(100F, 0F, 100F, 4, true)
            .addKeyline(475F, .5F, 50F)
            .build();
    KeylineStateList stateList = KeylineStateList.from(createCarouselWithWidth(500), state);

    assertThat(stateList.getLeftState().getFirstFocalKeylineIndex()).isEqualTo(0);
    assertThat(stateList.getLeftState().getLastFocalKeylineIndex()).isEqualTo(3);
  }
}
