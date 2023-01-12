/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.carousel;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An object that manages a {@link KeylineState} and handles shifting the focal keyline range to the
 * left and right of a the carousel container automatically.
 *
 * <p>This class generates {@link KeylineState}s for each discreet step needed to move the focal
 * range from it's default location to the left or right of the carousel container. These state
 * "steps" can then be interpolated between depending on the scroll offset of the carousel to create
 * a smooth shifting of the focal range along the scroll axis while preserving the look and feel of
 * the original {@link KeylineState} arrangement.
 *
 * <p>This class does not need to account for layout direction. {@link CarouselLayoutManager} will
 * handle reversing a KeylineState when being laid out right-to-left before constructing a
 * KeylineStateList.
 */
class KeylineStateList {

  private static final int NO_INDEX = -1;

  private final KeylineState defaultState;

  private final List<KeylineState> leftStateSteps;
  private final List<KeylineState> rightStateSteps;

  private final float[] leftStateStepsInterpolationPoints;
  private final float[] rightStateStepsInterpolationPoints;

  private final float leftShiftRange;
  private final float rightShiftRange;

  private KeylineStateList(
      KeylineState defaultState,
      List<KeylineState> leftStateSteps,
      List<KeylineState> rightStateSteps) {
    this.defaultState = defaultState;
    this.leftStateSteps = Collections.unmodifiableList(leftStateSteps);
    this.rightStateSteps = Collections.unmodifiableList(rightStateSteps);

    // Calculate the total distance the keylines will shift when moving between the default state
    // and the left or right state.
    this.leftShiftRange =
        leftStateSteps.get(leftStateSteps.size() - 1).getFirstKeyline().loc
            - defaultState.getFirstKeyline().loc;
    this.rightShiftRange =
        defaultState.getLastKeyline().loc
            - rightStateSteps.get(rightStateSteps.size() - 1).getLastKeyline().loc;

    // Calculate points that are used to determine which steps should be interpolated between for
    // a 0-1 interpolation value.
    this.leftStateStepsInterpolationPoints =
        getStateStepInterpolationPoints(leftShiftRange, leftStateSteps, /* isShiftingLeft= */ true);
    this.rightStateStepsInterpolationPoints =
        getStateStepInterpolationPoints(
            rightShiftRange, rightStateSteps, /* isShiftingLeft= */ false);
  }

  /** Creates a new {@link KeylineStateList} from a {@link KeylineState}. */
  static KeylineStateList from(Carousel carousel, KeylineState state) {
    return new KeylineStateList(
        state, getStateStepsLeft(state), getStateStepsRight(carousel, state));
  }

  /** Returns the default state for this state list. */
  KeylineState getDefaultState() {
    return defaultState;
  }

  /** Gets the keyline state for when keylines have been shifted to the left of a carousel. */
  KeylineState getLeftState() {
    return leftStateSteps.get(leftStateSteps.size() - 1);
  }

  /** Gets the keyline state for when keylines have been shifted to the right of a carousel. */
  KeylineState getRightState() {
    return rightStateSteps.get(rightStateSteps.size() - 1);
  }

  /**
   * Gets a shifted KeylineState appropriate for a scroll offset.
   *
   * <p>The first and last items in a carousel should never detach or scroll away from the edges of
   * the carousel container. To enforce this while still allowing each item in the carousel to enter
   * the focused range when the focused range is not, by default, at the beginning or end of the
   * list, keylines need to shift along the scrolling axis in order to reach every item.
   *
   * @param scrollOffset the scroll offset
   * @param minScrollOffset the minimum scroll offset. This moves the items as far right in a
   *     container as possible.
   * @param maxScrollOffset the maximum scroll offset. This moves the items as far left in a
   *     container as possible.
   * @return a {@link KeylineState} that has been shifted according on the scroll offset.
   */
  public KeylineState getShiftedState(
      float scrollOffset, float minScrollOffset, float maxScrollOffset) {
    float leftShiftOffset = minScrollOffset + leftShiftRange;
    float rightShiftOffset = maxScrollOffset - rightShiftRange;
    if (scrollOffset < leftShiftOffset) {
      float interpolation =
          AnimationUtils.lerp(
              /* outputMin= */ 1F,
              /* outputMax= */ 0F,
              /* inputMin: */ minScrollOffset,
              /* inputMax= */ leftShiftOffset,
              /* value= */ scrollOffset);
      return lerp(leftStateSteps, interpolation, leftStateStepsInterpolationPoints);
    } else if (scrollOffset > rightShiftOffset) {
      float interpolation =
          AnimationUtils.lerp(
              /* outputMin= */ 0F,
              /* outputMax= */ 1F,
              /* inputMin= */ rightShiftOffset,
              /* inputMax= */ maxScrollOffset,
              /* value= */ scrollOffset);
      return lerp(rightStateSteps, interpolation, rightStateStepsInterpolationPoints);
    } else {
      return defaultState;
    }
  }

  /**
   * Interpolates between {@code shiftSteps} for a given {@code interpolation}.
   *
   * @param stateSteps the steps to shift between
   * @param interpolation a 0 to 1 value representing the percent of {@code shiftRange} that has
   *     been scrolled
   * @param stateStepsInterpolationPoints ranges that define points between 0 and 1 where each index
   *     represents an interpolation value for when the state at the same index in {@code
   *     shiftSteps} should be returned.
   * @return a {@link KeylineState} that has been shifted for the given {@code interpolation}.
   */
  private static KeylineState lerp(
      List<KeylineState> stateSteps, float interpolation, float[] stateStepsInterpolationPoints) {

    int numberOfSteps = stateSteps.size();
    // Find the step that contains `interpolation` and remap the the surrounding interpolation
    // points lower and upper bounds to its own 0-1 value.
    float lowerBounds = stateStepsInterpolationPoints[0];
    for (int i = 1; i < numberOfSteps; i++) {
      float upperBounds = stateStepsInterpolationPoints[i];
      if (interpolation <= upperBounds) {
        int fromIndex = i - 1;
        int toIndex = i;
        float steppedProgress =
            AnimationUtils.lerp(0F, 1F, lowerBounds, upperBounds, interpolation);
        return KeylineState.lerp(
            stateSteps.get(fromIndex), stateSteps.get(toIndex), steppedProgress);
      }
      lowerBounds = upperBounds;
    }

    // Return the default state. This should occur if the stateSteps only hold the default
    // KeylineState, meaning the default state's focal range is already placed at the left or
    // right of the carousel container.
    return stateSteps.get(0);
  }

  /**
   * Creates and returns a float array containing points between 0 and 1 that represent
   * interpolation values for when the {@code KeylineState} at the corresponding index in {@code
   * stateSteps} should be visible when interpolating through {@code stateSteps}.
   *
   * <p>For example, if there are 4 steps in {@code stateSteps}, this method will return an array of
   * 4 float values that could look like [0, .33, .66, 1]. When interpolating through a list of
   * {@link KeylineState}s, an interpolation value will be between 0-1. This interpolation will be
   * used to find the range it falls within from this methods returned value. If interpolation is
   * .25, that would fall between the 0 and .33, the 0th and 1st indices of the float array. Meaning
   * the 0th and 1st items from {@code stateSteps} should be the current {@code KeylineState}s being
   * interpolated. This is an example with equally distributed values but these values will
   * typically be unequally distributed since their size depends on the distance keylines will shift
   * between each step.
   *
   * @see #lerp(List, float, float[]) for more details on how interpolation points are used
   * @see #getShiftedState(float, float, float) for more details on how interpolation points are
   *     used
   * @param shiftRange the total distance keylines will shift between the first and last {@link
   *     KeylineState} of {@code stateSteps}
   * @param stateSteps the steps to find interpolation points for
   * @param isShiftingLeft true if this method should find interpolation points for shifting
   *     keylines to the left of a carousel, false if this method should find interpolation points
   *     for shifting keylines to the end of a carousel
   * @return a float array, equal in size to {@code stateSteps#size} that contains points between
   *     0-1 that align with when a {@code KeylineState} from {@code stateSteps} should be shown for
   *     a 0-1 interpolation value
   */
  private static float[] getStateStepInterpolationPoints(
      float shiftRange, List<KeylineState> stateSteps, boolean isShiftingLeft) {
    int numberOfSteps = stateSteps.size();
    float[] stateStepsInterpolationPoints = new float[numberOfSteps];
    // Interpolation must be split into numberOfSteps. Each split is not an equal portion. Instead,
    // each split corresponds to the distance by which each step moves the keylines.
    // The first interpolation point is always zero for the default state. Start the loop at 1.
    for (int i = 1; i < numberOfSteps; i++) {
      KeylineState prevState = stateSteps.get(i - 1);
      KeylineState currState = stateSteps.get(i);
      // Get the distance between the first keyline of the current and previous states.
      float distanceShifted =
          isShiftingLeft
              ? currState.getFirstKeyline().loc - prevState.getFirstKeyline().loc
              : prevState.getLastKeyline().loc - currState.getLastKeyline().loc;
      // Convert the distance this step will shift into a percentage of the total shift range.
      float stepProgress = distanceShifted / shiftRange;
      stateStepsInterpolationPoints[i] =
          i == numberOfSteps - 1 ? 1F : stateStepsInterpolationPoints[i - 1] + stepProgress;
    }

    return stateStepsInterpolationPoints;
  }

  /**
   * Determines whether or not the first focal item for the given {@code state} is at the left of
   * the carousel container and fully visible.
   *
   * @param state the state to check for start item position
   * @return true if the {@code state}'s focal start item has its start aligned with the start of
   *     the {@code carousel} container
   */
  private static boolean isFirstFocalItemAtLeftOfContainer(KeylineState state) {
    float firstFocalItemLeft =
        state.getFirstFocalKeyline().locOffset - (state.getFirstFocalKeyline().maskedItemSize / 2F);
    return firstFocalItemLeft <= 0F || state.getFirstFocalKeyline() == state.getFirstKeyline();
  }

  /**
   * Generates discreet steps which move the focal range from it's original position until it
   * reaches the start of the carousel container.
   *
   * <p>Each step can only move the focal start keyline by one keyline at a time to ensure every
   * item in the list passes through the focal range. Each step removes the keyline at the start of
   * the container and re-inserts it after the focal range in an order that retains visual balance.
   * This is repeated until the focal start keyline is at the start of the container. Re-inserting
   * keylines after the focal range in a balanced way is done by looking at the mask of they keyline
   * next to the keyline that is being re-positioned and finding a match on the other side of the
   * focal range.
   *
   * <p>The first state in the returned list is always the original/default {@code state} while the
   * last state will be the start state or the state that has the focal range at the beginning of
   * the carousel.
   */
  private static List<KeylineState> getStateStepsLeft(KeylineState defaultState) {
    List<KeylineState> steps = new ArrayList<>();
    steps.add(defaultState);
    int firstInBoundsKeylineIndex = findFirstInBoundsKeylineIndex(defaultState);
    // If the first focal item is already at the left of the container or there are no in bounds
    // keylines, return a list of steps that only includes the default state (there is nowhere to
    // shift).
    if (isFirstFocalItemAtLeftOfContainer(defaultState)
        || firstInBoundsKeylineIndex == NO_INDEX) {
      return steps;
    }

    int start = firstInBoundsKeylineIndex;
    int end = defaultState.getFirstFocalKeylineIndex() - 1;
    int numberOfSteps = end - start;

    float originalStart =
        defaultState.getFirstKeyline().locOffset
            - (defaultState.getFirstKeyline().maskedItemSize / 2F);

    for (int i = 0; i <= numberOfSteps; i++) {
      KeylineState prevStepState = steps.get(steps.size() - 1);
      int itemOrigIndex = start + i;
      // If this is the first item from the original state, place it at the end of the dest state.
      // Otherwise, use it's adjacent item's mask to find suitable index on the other side of the
      // focal range where it can be placed.
      int dstIndex = defaultState.getKeylines().size() - 1;
      if (itemOrigIndex - 1 >= 0) {
        float originalAdjacentMaskLeft = defaultState.getKeylines().get(itemOrigIndex - 1).mask;
        dstIndex =
            findFirstIndexAfterLastFocalKeylineWithMask(prevStepState, originalAdjacentMaskLeft)
                - 1;
      }

      int newFirstFocalIndex = defaultState.getFirstFocalKeylineIndex() - i - 1;
      int newLastFocalIndex = defaultState.getLastFocalKeylineIndex() - i - 1;

      KeylineState shifted =
          moveKeylineAndCreateKeylineState(
              prevStepState,
              /* keylineSrcIndex= */ firstInBoundsKeylineIndex,
              /* keylineDstIndex= */ dstIndex,
              originalStart,
              newFirstFocalIndex,
              newLastFocalIndex);
      steps.add(shifted);
    }
    return steps;
  }

  /**
   * Determines whether or not the first focal item for the given {@code state} is at the right of
   * the carousel container and fully visible.
   *
   * @param carousel the {@link Carousel} associated with this {@link KeylineStateList}.
   * @param state the state to check for right item position
   * @return true if the {@code state}'s first focal item has its right aligned with the right of
   *     the {@code carousel} container
   */
  private static boolean isLastFocalItemAtRightOfContainer(Carousel carousel, KeylineState state) {
    float firstFocalItemRight =
        state.getLastFocalKeyline().locOffset + (state.getLastFocalKeyline().maskedItemSize / 2F);
    return firstFocalItemRight >= carousel.getContainerWidth()
        || state.getLastFocalKeyline() == state.getLastKeyline();
  }

  /**
   * Generates discreet steps which move the focal range from it's original position until it
   * reaches the right of the carousel container.
   *
   * <p>Each step can only move the focal range by one keyline at a time to ensure every item in the
   * list passes through the focal range. Each step removes the keyline at the end of the carousel
   * container and re-inserts it before the focal range in an order that retains visual balance.
   * This is repeated until the focal end keyline is at the end of the carousel container.
   * Re-inserting keylines before the focal range is done by looking at the mask of they keyline
   * next to the keyline that is being re-positioned and finding a match on the other side of the
   * focal range.
   *
   * <p>The first state in the returned list is always the original/default {@code state} while the
   * last state will be the right state or the state that has the focal range at the right of the
   * carousel.
   */
  private static List<KeylineState> getStateStepsRight(
      Carousel carousel, KeylineState defaultState) {
    List<KeylineState> steps = new ArrayList<>();
    steps.add(defaultState);
    int lastInBoundsKeylineIndex = findLastInBoundsKeylineIndex(carousel, defaultState);
    // If the focal end item is already at the end of the container or there are no in bounds
    // keylines, return a list of steps that only includes the default state (there is nowhere to
    // shift).
    if (isLastFocalItemAtRightOfContainer(carousel, defaultState)
        || lastInBoundsKeylineIndex == NO_INDEX) {
      return steps;
    }

    int start = defaultState.getLastFocalKeylineIndex();
    int end = lastInBoundsKeylineIndex;
    int numberOfSteps = end - start;

    float originalStart =
        defaultState.getFirstKeyline().locOffset
            - (defaultState.getFirstKeyline().maskedItemSize / 2F);

    for (int i = 0; i < numberOfSteps; i++) {
      KeylineState prevStepState = steps.get(steps.size() - 1);
      int itemOrigIndex = end - i;
      // If this is the last item from the original state, place it at the start of the dest state.
      // Otherwise, use it's adjacent item's mask to find suitable index on the other side of the
      // focal range where it can be placed.
      int dstIndex = 0;
      if (itemOrigIndex + 1 < defaultState.getKeylines().size()) {
        float originalAdjacentMaskRight = defaultState.getKeylines().get(itemOrigIndex + 1).mask;
        dstIndex =
            findLastIndexBeforeFirstFocalKeylineWithMask(prevStepState, originalAdjacentMaskRight)
                + 1;
      }

      // The index of the start and end focal keylines in this step's keyline state.
      int newFirstFocalIndex = defaultState.getFirstFocalKeylineIndex() + i + 1;
      int newLastFocalIndex = defaultState.getLastFocalKeylineIndex() + i + 1;

      KeylineState shifted =
          moveKeylineAndCreateKeylineState(
              prevStepState,
              /* keylineSrcIndex= */ lastInBoundsKeylineIndex,
              /* keylineDstIndex= */ dstIndex,
              originalStart,
              newFirstFocalIndex,
              newLastFocalIndex);
      steps.add(shifted);
    }

    return steps;
  }

  /**
   * Creates a new, valid KeylineState from a list of keylines that have been re-arranged.
   *
   * @param state the KeylineState who will have its keyline at {@code keylineSrcIndex} moved to
   *     {@code keylineDstIndex}
   * @param keylineSrcIndex the index of the Keyline from {@code state} that should be moved
   * @param keylineDstIndex the index where the Keyline at {@code keylineSrcIndex} from {@code
   *     state} should be moved to
   * @param startOffset where the start of the first item should be placed
   * @param newFirstFocalIndex the index of the first focal keyline in the returned keyline state
   * @param newLastFocalIndex the index of the last focal keyline in the returned keyline state
   * @return a keyline state that has items re-ordered
   */
  private static KeylineState moveKeylineAndCreateKeylineState(
      KeylineState state,
      int keylineSrcIndex,
      int keylineDstIndex,
      float startOffset,
      int newFirstFocalIndex,
      int newLastFocalIndex) {

    List<Keyline> tmpKeylines = new ArrayList<>(state.getKeylines());
    Keyline item = tmpKeylines.remove(keylineSrcIndex);
    tmpKeylines.add(keylineDstIndex, item);

    KeylineState.Builder builder = new KeylineState.Builder(state.getItemSize());

    for (int j = 0; j < tmpKeylines.size(); j++) {
      Keyline k = tmpKeylines.get(j);
      float offset = startOffset + (k.maskedItemSize / 2F);

      boolean isFocal = j >= newFirstFocalIndex && j <= newLastFocalIndex;
      builder.addKeyline(offset, k.mask, k.maskedItemSize, isFocal);
      startOffset += k.maskedItemSize;
    }

    return builder.build();
  }

  private static int findFirstIndexAfterLastFocalKeylineWithMask(KeylineState state, float mask) {
    int focalEndIndex = state.getLastFocalKeylineIndex();
    for (int i = focalEndIndex; i < state.getKeylines().size(); i++) {
      if (mask == state.getKeylines().get(i).mask) {
        return i;
      }
    }

    return state.getKeylines().size() - 1;
  }

  private static int findLastIndexBeforeFirstFocalKeylineWithMask(KeylineState state, float mask) {
    int focalStartIndex = state.getFirstFocalKeylineIndex() - 1;
    for (int i = focalStartIndex; i >= 0; i--) {
      if (mask == state.getKeylines().get(i).mask) {
        return i;
      }
    }

    return 0;
  }

  private static int findFirstInBoundsKeylineIndex(KeylineState state) {
    for (int i = 0; i < state.getKeylines().size(); i++) {
      if (state.getKeylines().get(i).locOffset >= 0) {
        return i;
      }
    }

    return NO_INDEX;
  }

  private static int findLastInBoundsKeylineIndex(Carousel carousel, KeylineState state) {
    for (int i = state.getKeylines().size() - 1; i >= 0; i--) {
      if (state.getKeylines().get(i).locOffset <= carousel.getContainerWidth()) {
        return i;
      }
    }

    return NO_INDEX;
  }
}
