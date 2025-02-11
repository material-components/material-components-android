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

import static java.lang.Math.max;
import static java.lang.Math.min;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.math.MathUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RestrictTo(Scope.LIBRARY_GROUP)
public class KeylineStateList {

  private static final int NO_INDEX = -1;

  private final KeylineState defaultState;

  private final List<KeylineState> startStateSteps;
  private final List<KeylineState> endStateSteps;

  private final float[] startStateStepsInterpolationPoints;
  private final float[] endStateStepsInterpolationPoints;

  private final float startShiftRange;
  private final float endShiftRange;

  private KeylineStateList(
      @NonNull KeylineState defaultState,
      List<KeylineState> startStateSteps,
      List<KeylineState> endStateSteps) {
    this.defaultState = defaultState;
    this.startStateSteps = Collections.unmodifiableList(startStateSteps);
    this.endStateSteps = Collections.unmodifiableList(endStateSteps);

    // Calculate the total distance the keylines will shift when moving between the default state
    // and the left or right state.
    this.startShiftRange =
        startStateSteps.get(startStateSteps.size() - 1).getFirstKeyline().loc
            - defaultState.getFirstKeyline().loc;
    this.endShiftRange =
        defaultState.getLastKeyline().loc
            - endStateSteps.get(endStateSteps.size() - 1).getLastKeyline().loc;

    // Calculate points that are used to determine which steps should be interpolated between for
    // a 0-1 interpolation value.
    this.startStateStepsInterpolationPoints =
        getStateStepInterpolationPoints(
            startShiftRange, startStateSteps, /* isShiftingLeft= */ true);
    this.endStateStepsInterpolationPoints =
        getStateStepInterpolationPoints(endShiftRange, endStateSteps, /* isShiftingLeft= */ false);
  }

  /** Creates a new {@link KeylineStateList} from a {@link KeylineState}. */
  static KeylineStateList from(
      Carousel carousel,
      KeylineState state,
      float itemMargins,
      float leftOrTopPadding,
      float rightOrBottomPadding,
      CarouselStrategy.StrategyType strategyType) {
    return new KeylineStateList(
        state,
        getStateStepsStart(carousel, state, itemMargins, leftOrTopPadding, strategyType),
        getStateStepsEnd(carousel, state, itemMargins, rightOrBottomPadding, strategyType));
  }

  /** Returns the default state for this state list. */
  KeylineState getDefaultState() {
    return defaultState;
  }

  /**
   * Gets the keyline state for when keylines have been shifted to the left or top of a carousel.
   */
  KeylineState getStartState() {
    return startStateSteps.get(startStateSteps.size() - 1);
  }

  /**
   * Gets the keyline state for when keylines have been shifted to the right or bottom of a
   * carousel.
   */
  KeylineState getEndState() {
    return endStateSteps.get(endStateSteps.size() - 1);
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
  @NonNull
  public KeylineState getShiftedState(
      float scrollOffset, float minScrollOffset, float maxScrollOffset) {
    return getShiftedState(scrollOffset, minScrollOffset, maxScrollOffset, false);
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
   * @param roundToNearestStep if true, returns the {@link KeylineState} that has been shifted and
   *     'rounded' to the nearest 'step'. Otherwise, it returns the {@link KeylineState} that has
   *     been shifted exactly according to the scroll offset.
   * @return a {@link KeylineState} that has been shifted according on the scroll offset.
   */
  KeylineState getShiftedState(
      float scrollOffset,
      float minScrollOffset,
      float maxScrollOffset,
      boolean roundToNearestStep) {
    float startShiftOffset = minScrollOffset + startShiftRange;
    float endShiftOffset = maxScrollOffset - endShiftRange;
    float startPaddingShift = getStartState().getFirstFocalKeyline().leftOrTopPaddingShift;
    float endPaddingShift = getEndState().getFirstFocalKeyline().rightOrBottomPaddingShift;

    // Normally we calculate the interpolation such that by scrollShiftOffset, we are always at the
    // default state. In the case where the start state is equal to the default state but with
    // padding, we want to start shifting earlier/increase startShiftOffset so that the
    // interpolation will choose the start state instead of the default state when the scroll offset
    // is equal to startPaddingShift. This is so we are always at the start state with padding at
    // the beginning of the carousel, instead of getting to a state where the start state is only
    // when scrollOffset <= startPaddingShift.
    // We know that the start state is equal to the default state with padding if the start shift
    // range is equal to the padding.
    if (startShiftRange == startPaddingShift) {
      startShiftOffset += startPaddingShift;
    }
    // Same as above but with the end state/padding.
    if (endShiftRange == endPaddingShift) {
      endShiftOffset -= endPaddingShift;
    }
    List<KeylineState> steps;
    float[] interpolationPoints;
    float interpolation;
    if (scrollOffset < startShiftOffset) {
      interpolation =
          AnimationUtils.lerp(
              /* outputMin= */ 1F,
              /* outputMax= */ 0F,
              /* inputMin: */ minScrollOffset,
              /* inputMax= */ startShiftOffset,
              /* value= */ scrollOffset);
      steps = startStateSteps;
      interpolationPoints = startStateStepsInterpolationPoints;
    } else if (scrollOffset > endShiftOffset) {
      interpolation =
          AnimationUtils.lerp(
              /* outputMin= */ 0F,
              /* outputMax= */ 1F,
              /* inputMin= */ endShiftOffset,
              /* inputMax= */ maxScrollOffset,
              /* value= */ scrollOffset);
      steps = endStateSteps;
      interpolationPoints = endStateStepsInterpolationPoints;
    } else {
      return defaultState;
    }

    if (roundToNearestStep) {
      return closestStateStepFromInterpolation(steps, interpolation, interpolationPoints);
    }
    return lerp(steps, interpolation, interpolationPoints);
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
    float[] stateStepsRange =
        getStateStepsRange(stateSteps, interpolation, stateStepsInterpolationPoints);
    return KeylineState.lerp(
        stateSteps.get((int) stateStepsRange[1]),
        stateSteps.get((int) stateStepsRange[2]),
        stateStepsRange[0]);
  }

  /**
   * Gets the state steps range of the form of an array [progress, fromIndex, toIndex], where
   * progress indicates the progress in between fromIndex and toIndex and the 2 indices represent
   * steps in the given stateSteps.
   *
   * @param stateSteps The steps in which to determine the 2 state steps we are in between.
   * @param interpolation The interpolation of the state steps we are at.
   * @param stateStepsInterpolationPoints The state step interpolation points; each interpolation
   *     point corresponds to at which interpolation we are at the corresponding state step.
   * @return an array of the form [progress, fromIndex, toIndex] where progress represents the
   *     progress in between the state steps at fromIndex and toIndex.
   */
  private static float[] getStateStepsRange(
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
        return new float[] {steppedProgress, fromIndex, toIndex};
      }
      lowerBounds = upperBounds;
    }
    // Return the index of the default state. This should occur if the stateSteps only hold the
    // default KeylineState, meaning the default state's focal range is already placed at the left
    // or right of the carousel container.
    return new float[] {0, 0, 0};
  }

  private KeylineState closestStateStepFromInterpolation(
      List<KeylineState> stateSteps, float interpolation, float[] stateStepsInterpolationPoints) {
    float[] stateStepsRange =
        getStateStepsRange(stateSteps, interpolation, stateStepsInterpolationPoints);
    // If the progress is larger than half, take the state step at the toIndex.
    // If smaller, return the step at fromIndex.
    // If equal, return the step at toIndex because state steps are ordered from default state ->
    // start/end state. If we are interpolating, it means we are within the start or end shifts,
    // and we should prefer the start/end state.
    if (stateStepsRange[0] >= 0.5f) {
      return stateSteps.get((int) stateStepsRange[2]);
    }
    return stateSteps.get((int) stateStepsRange[1]);
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
    return firstFocalItemLeft >= 0F
        && state.getFirstFocalKeyline() == state.getFirstNonAnchorKeyline();
  }

  /**
   * Determines whether or not the first focal item for the given {@code state} is at the right of
   * the carousel container and fully visible.
   *
   * @param carousel the {@link Carousel} associated with this {@link KeylineStateList}.
   * @param state the state to check for right item position
   * @return true if the {@code state}'s first focal item has its right aligned with the right of
   *     the {@code carousel} container and is fully visible.
   */
  private static boolean isLastFocalItemVisibleAtRightOfContainer(
      Carousel carousel, KeylineState state) {
    int containerSize = carousel.getContainerHeight();
    if (carousel.isHorizontal()) {
      containerSize = carousel.getContainerWidth();
    }
    float lastFocalItemRight =
        state.getLastFocalKeyline().locOffset + (state.getLastFocalKeyline().maskedItemSize / 2F);
    return lastFocalItemRight <= containerSize
        && state.getLastFocalKeyline() == state.getLastNonAnchorKeyline();
  }

  @NonNull
  private static KeylineState shiftKeylineStateForPadding(
      @NonNull KeylineState keylineState, float padding, int carouselSize, boolean leftShift,
      float childMargins, CarouselStrategy.StrategyType strategyType) {
    switch (strategyType) {
      case CONTAINED:
        return shiftKeylineStateForPaddingContained(
            keylineState, padding, carouselSize, leftShift, childMargins);
      default:
        return shiftKeylineStateForPaddingUncontained(
            keylineState, padding, carouselSize, leftShift);
    }
  }

  @NonNull
  private static KeylineState shiftKeylineStateForPaddingUncontained(
      @NonNull KeylineState keylineState, float padding, int carouselSize, boolean leftShift) {
    List<Keyline> tmpKeylines = new ArrayList<>(keylineState.getKeylines());
    KeylineState.Builder builder =
        new KeylineState.Builder(keylineState.getItemSize(), carouselSize);
    int unchangingAnchorPosition = leftShift ? 0 : tmpKeylines.size() - 1;
    for (int j = 0; j < tmpKeylines.size(); j++) {
      Keyline k = tmpKeylines.get(j);
      if (k.isAnchor && j == unchangingAnchorPosition) {
        builder.addKeyline(k.locOffset, k.mask, k.maskedItemSize, false, true, k.cutoff);
        continue;
      }
      float newOffset = leftShift ? k.locOffset + padding : k.locOffset - padding;
      float leftOrTopPadding = leftShift ? padding : 0;
      float rightOrBottomPadding = leftShift ? 0 : padding;
      boolean isFocal =
          j >= keylineState.getFirstFocalKeylineIndex()
              && j <= keylineState.getLastFocalKeylineIndex();
      builder.addKeyline(
          newOffset,
          k.mask,
          k.maskedItemSize,
          isFocal,
          k.isAnchor,
          Math.abs(
              leftShift
                  ? max(0, newOffset + k.maskedItemSize / 2 - carouselSize)
                  : min(0, newOffset - k.maskedItemSize / 2)),
          leftOrTopPadding,
          rightOrBottomPadding);
    }
    return builder.build();
  }

  private static KeylineState shiftKeylineStateForPaddingContained(
      KeylineState keylineState, float padding, int carouselSize, boolean leftShift,
      float childMargins) {

    List<Keyline> tmpKeylines = new ArrayList<>(keylineState.getKeylines());
    KeylineState.Builder builder =
        new KeylineState.Builder(keylineState.getItemSize(), carouselSize);

    int numOfNonAnchorKeylines = keylineState.getNumberOfNonAnchorKeylines();
    float toDecreaseBy = padding / numOfNonAnchorKeylines;
    float nextOffset = 0;

    if (leftShift) {
      nextOffset = padding;
    }
    for (int j = 0; j < tmpKeylines.size(); j++) {
      Keyline k = tmpKeylines.get(j);
      if (k.isAnchor) {
        builder.addKeyline(
            k.locOffset,
            k.mask,
            k.maskedItemSize,
            false,
            true,
            k.cutoff);
        continue;
      }

      boolean isFocal =
          j >= keylineState.getFirstFocalKeylineIndex()
              && j <= keylineState.getLastFocalKeylineIndex();
      // Shift all the keylines over and adjust size equally for each one.
      float maskedItemSize = k.maskedItemSize - toDecreaseBy;
      float mask =
          CarouselStrategy.getChildMaskPercentage(
              maskedItemSize, keylineState.getItemSize(), childMargins);
      float locOffset = nextOffset + maskedItemSize / 2F;

      float actualPaddingShift = Math.abs(locOffset - k.locOffset);

      builder.addKeyline(
          locOffset,
          mask,
          maskedItemSize,
          isFocal,
          false,
          k.cutoff,
          leftShift ? actualPaddingShift : 0,
          leftShift ? 0 : actualPaddingShift);
      nextOffset = nextOffset + maskedItemSize;
    }
    return builder.build();
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
  private static List<KeylineState> getStateStepsStart(
      Carousel carousel, KeylineState defaultState, float itemMargins,
      float leftOrTopPaddingForKeylineShift, CarouselStrategy.StrategyType strategyType) {
    List<KeylineState> steps = new ArrayList<>();
    steps.add(defaultState);
    int firstNonAnchorKeylineIndex = findFirstNonAnchorKeylineIndex(defaultState);
    int carouselSize =
        carousel.isHorizontal() ? carousel.getContainerWidth() : carousel.getContainerHeight();

    // If the first focal item is already at the left of the container or there are no in bounds
    // keylines, return a list of steps that only includes the default state (there is nowhere to
    // shift).
    if (isFirstFocalItemAtLeftOfContainer(defaultState) || firstNonAnchorKeylineIndex == NO_INDEX) {
      // If there exists padding, add a start step that offsets the default state.
      if (leftOrTopPaddingForKeylineShift > 0) {
        steps.add(
            shiftKeylineStateForPadding(
                defaultState,
                leftOrTopPaddingForKeylineShift,
                carouselSize,
                true,
                itemMargins,
                strategyType));
      }
      return steps;
    }

    int start = firstNonAnchorKeylineIndex;
    int end = defaultState.getFirstFocalKeylineIndex();
    int numberOfSteps = end - start;
    float originalStart =
        defaultState.getFirstKeyline().locOffset
            - (defaultState.getFirstKeyline().maskedItemSize / 2F);

    if (numberOfSteps <= 0 && defaultState.getFirstFocalKeyline().cutoff > 0) {
      // If there are no steps, there still might be a cutoff focal item that we should shift into
      // view. Add a step that shifts all the keylines over to bring the first focal item into full
      // view.
      float cutoffs = defaultState.getFirstFocalKeyline().cutoff;
      steps.add(
          shiftKeylinesAndCreateKeylineState(
              defaultState,
              originalStart + cutoffs + leftOrTopPaddingForKeylineShift,
              carouselSize));
      return steps;
    }

    float cutoffs = 0;
    for (int i = 0; i < numberOfSteps; i++) {
      KeylineState prevStepState = steps.get(steps.size() - 1);
      int itemOrigIndex = start + i;
      // If this is the first item from the original state, place it at the end of the dest state.
      // Otherwise, use it's adjacent item's mask to find suitable index on the other side of the
      // focal range where it can be placed.
      int dstIndex = defaultState.getKeylines().size() - 1;
      cutoffs += defaultState.getKeylines().get(itemOrigIndex).cutoff;
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
              /* keylineSrcIndex= */ firstNonAnchorKeylineIndex,
              /* keylineDstIndex= */ dstIndex,
              originalStart + cutoffs,
              newFirstFocalIndex,
              newLastFocalIndex,
              carouselSize);

      // If we are the last step ie. the start state, and we have padding, shift keylines.
      if (i == numberOfSteps - 1 && leftOrTopPaddingForKeylineShift > 0) {
        shifted = shiftKeylineStateForPadding(
            shifted,
            leftOrTopPaddingForKeylineShift,
            carouselSize,
            true,
            itemMargins,
            strategyType);
      }
      steps.add(shifted);
    }
    return steps;
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
  private static List<KeylineState> getStateStepsEnd(Carousel carousel, KeylineState defaultState,
      float itemMargins, float rightOrBottomPaddingForKeylineShift,
      CarouselStrategy.StrategyType strategyType) {
    List<KeylineState> steps = new ArrayList<>();
    steps.add(defaultState);
    int lastNonAnchorKeylineIndex = findLastNonAnchorKeylineIndex(defaultState);
    int carouselSize =
        carousel.isHorizontal() ? carousel.getContainerWidth() : carousel.getContainerHeight();

    // If the focal end item is already at the end of the container and is fully visible or there
    // are no in bounds keylines, return a list of steps that only includes the default state
    // (there is nowhere to shift).
    if (isLastFocalItemVisibleAtRightOfContainer(carousel, defaultState)
        || lastNonAnchorKeylineIndex == NO_INDEX) {
      // If there exists padding, add an end step that offsets the default state.
      if (rightOrBottomPaddingForKeylineShift > 0) {
        steps.add(
            shiftKeylineStateForPadding(
                defaultState,
                rightOrBottomPaddingForKeylineShift,
                carouselSize,
                false,
                itemMargins,
                strategyType));
      }
      return steps;
    }

    int start = defaultState.getLastFocalKeylineIndex();
    int end = lastNonAnchorKeylineIndex;
    int numberOfSteps = end - start;
    float originalStart =
        defaultState.getFirstKeyline().locOffset
            - (defaultState.getFirstKeyline().maskedItemSize / 2F);

    if (numberOfSteps <= 0 && defaultState.getLastFocalKeyline().cutoff > 0) {
      // If there are no steps, there still might be a cutoff focal item that we should shift into
      // view. Add a step that shifts all the keylines over to bring the last focal item into full
      // view.
      float cutoffs = defaultState.getLastFocalKeyline().cutoff;
      steps.add(shiftKeylinesAndCreateKeylineState(defaultState,
          originalStart - cutoffs - rightOrBottomPaddingForKeylineShift, carouselSize));
      return steps;
    }

    float cutoffs = 0;
    for (int i = 0; i < numberOfSteps; i++) {
      KeylineState prevStepState = steps.get(steps.size() - 1);
      int itemOrigIndex = end - i;
      cutoffs += defaultState.getKeylines().get(itemOrigIndex).cutoff;
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
              /* keylineSrcIndex= */ lastNonAnchorKeylineIndex,
              /* keylineDstIndex= */ dstIndex,
              originalStart - cutoffs,
              newFirstFocalIndex,
              newLastFocalIndex,
              carouselSize);
      // If we are the last step ie. the start state, and we have padding, shift keylines.
      if (i == numberOfSteps - 1 && rightOrBottomPaddingForKeylineShift > 0) {
        shifted = shiftKeylineStateForPadding(
            shifted,
            rightOrBottomPaddingForKeylineShift,
            carouselSize,
            false,
            itemMargins,
            strategyType);
      }
      steps.add(shifted);
    }

    return steps;
  }

  /**
   * Creates a new, valid KeylineState that has the same order as {@code state} but with all
   * keylines shifted along the scrolling axis.
   *
   * @param state the state to shift
   * @param startOffset the point along the scrolling axis where keylines should start being added
   *     from
   * @param carouselSize the size of the carousel container
   * @return a new {@link KeylineState} with the shifted keylines
   */
  private static KeylineState shiftKeylinesAndCreateKeylineState(
      KeylineState state, float startOffset, int carouselSize) {
    return moveKeylineAndCreateKeylineState(
        state,
        0,
        0,
        startOffset,
        state.getFirstFocalKeylineIndex(),
        state.getLastFocalKeylineIndex(),
        carouselSize);
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
      int newLastFocalIndex,
      int carouselSize) {

    List<Keyline> tmpKeylines = new ArrayList<>(state.getKeylines());
    Keyline item = tmpKeylines.remove(keylineSrcIndex);
    tmpKeylines.add(keylineDstIndex, item);

    KeylineState.Builder builder = new KeylineState.Builder(state.getItemSize(), carouselSize);

    for (int j = 0; j < tmpKeylines.size(); j++) {
      Keyline k = tmpKeylines.get(j);
      float offset = startOffset + (k.maskedItemSize / 2F);

      boolean isFocal = j >= newFirstFocalIndex && j <= newLastFocalIndex;
      // We must keep the same cutoff value from the default keylines instead of re-calculating
      // them based on the new offset.
      builder.addKeyline(offset, k.mask, k.maskedItemSize, isFocal, k.isAnchor, k.cutoff);
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

  private static int findFirstNonAnchorKeylineIndex(KeylineState state) {
    for (int i = 0; i < state.getKeylines().size(); i++) {
      if (!state.getKeylines().get(i).isAnchor) {
        return i;
      }
    }

    return NO_INDEX;
  }

  private static int findLastNonAnchorKeylineIndex(KeylineState state) {
    for (int i = state.getKeylines().size() - 1; i >= 0; i--) {
      if (!state.getKeylines().get(i).isAnchor) {
        return i;
      }
    }

    return NO_INDEX;
  }

  Map<Integer, KeylineState> getKeylineStateForPositionMap(
      int itemCount, int minHorizontalScroll, int maxHorizontalScroll, boolean isRTL) {
    float itemSize = defaultState.getItemSize();
    Map<Integer, KeylineState> keylineStates = new HashMap<>();
    int endStepsIndex = 0;
    int startStepsIndex = 0;
    // Associate the positions with the appropriate end (right/bottom) state step.
    for (int i = 0; i < itemCount; i++) {
      // If RTL, the scroll position is negative since item positions are backwards.
      int position = isRTL ? itemCount - i - 1 : i;
      float itemPosition = position * itemSize * (isRTL ? -1 : 1);
      if (itemPosition > maxHorizontalScroll - endShiftRange
          || i >= itemCount - endStateSteps.size()) {
        keylineStates.put(
            position,
            endStateSteps.get(MathUtils.clamp(endStepsIndex, 0, endStateSteps.size() - 1)));
        endStepsIndex++;
      }
    }
    // If the item is in the start (left/top) shift range, we associate the position with the
    // appropriate start state step. Since the startStateSteps goes from the default
    // state to the start state, we iterate backwards.
    for (int i = itemCount - 1; i >= 0; i--) {
      // If RTL, the scroll position is negative since item positions are backwards.
      int position = isRTL ? itemCount - i - 1 : i;
      float itemPosition = position * itemSize * (isRTL ? -1 : 1);
      if (itemPosition < minHorizontalScroll + startShiftRange || i < startStateSteps.size()) {
        // If the item is in range of the start shift, we start associating the
        // position with the startStateSteps.
        keylineStates.put(
            position,
            startStateSteps.get(MathUtils.clamp(startStepsIndex, 0, startStateSteps.size() - 1)));
        startStepsIndex++;
      }
    }
    return keylineStates;
  }
}
