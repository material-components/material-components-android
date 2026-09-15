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

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.animation.AnimationUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An arrangement of keylines that are positioned along a scrolling axis.
 *
 * <p>This class is the model used to tell a scrolling item how it should be masked, offset, or
 * treated at certain points (keylines) along the scrolling axis.
 *
 * <p>Keylines are points located along a scrolling axis, relative to the scrolling container's
 * bounds, that tell an item how it should be treated (masked, offset) when it's center is located
 * at a keyline. When between keylines, a scrolling item is treated by interpolating between the
 * states of its nearest surrounding keylines. When put together, a KeylineState contains all
 * keylines associated with a scrolling container and is able to tell a scrolling item how it should
 * be treated at any point (as the item moves) along the scrolling axis, creating a fluid
 * interpolated motion tied to scroll position.
 *
 * <p>Keylines can be either focal or non-focal. A focal keyline is a keyline where items are
 * considered visible or interactable in their fullest form. This usually means where items will be
 * fully unmaksed and viewable. There must be at least one focal keyline in a KeylineState. The
 * focal keylines are important for usability and alignment. Start-aligned strategies should place
 * focal keylines at the beginning of the scroll container, center-aligned strategies at the center
 * of the scroll container, etc.
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class KeylineState {

  private final float itemSize;
  private int totalVisibleFocalItems;
  private final List<Keyline> keylines;
  private final int firstFocalKeylineIndex;
  private final int lastFocalKeylineIndex;

  private final int carouselSize;

  private KeylineState(
      float itemSize,
      List<Keyline> keylines,
      int firstFocalKeylineIndex,
      int lastFocalKeylineIndex,
      int carouselSize) {
    this.itemSize = itemSize;
    this.keylines = Collections.unmodifiableList(keylines);
    this.firstFocalKeylineIndex = firstFocalKeylineIndex;
    this.lastFocalKeylineIndex = lastFocalKeylineIndex;
    for (int i = firstFocalKeylineIndex; i <= lastFocalKeylineIndex; i++) {
      if (keylines.get(i).cutoff == 0) {
        this.totalVisibleFocalItems += 1;
      }
    }
    this.carouselSize = carouselSize;
  }

  /**
   * Returns the fully unmasked size of an item.
   *
   * <p>Items with a mask of 0 should be laid out at this size.
   */
  float getItemSize() {
    return itemSize;
  }

  /** Returns list of keylines that should be positioned along the scroll axis. */
  List<Keyline> getKeylines() {
    return keylines;
  }

  /** Returns the number of focal items in the keyline state. */
  int getTotalVisibleFocalItems() {
    return totalVisibleFocalItems;
  }

  /** Returns the first focal keyline in the list. */
  Keyline getFirstFocalKeyline() {
    return keylines.get(firstFocalKeylineIndex);
  }

  /** Returns the index of the first focal keyline in the keylines list. */
  int getFirstFocalKeylineIndex() {
    return firstFocalKeylineIndex;
  }

  /** Returns the last focal keyline in the list. */
  Keyline getLastFocalKeyline() {
    return keylines.get(lastFocalKeylineIndex);
  }

  /** Returns the index of the last focal keyline in the keylines list. */
  int getLastFocalKeylineIndex() {
    return lastFocalKeylineIndex;
  }

  /** Returns list of all the keylines that are focal. **/
  List<Keyline> getFocalKeylines() {
    return keylines.subList(firstFocalKeylineIndex, lastFocalKeylineIndex + 1);
  }

  /** Returns the first keyline. */
  Keyline getFirstKeyline() {
    return keylines.get(0);
  }

  /** Returns the last keyline. */
  Keyline getLastKeyline() {
    return keylines.get(keylines.size() - 1);
  }

  /** Returns the first non-anchor keyline. */
  @Nullable
  Keyline getFirstNonAnchorKeyline() {
    for (int i = 0; i < keylines.size(); i++) {
      Keyline keyline = keylines.get(i);
      if (!keyline.isAnchor) {
        return keyline;
      }
    }
    return null;
  }

  /** Returns the last non-anchor keyline. */
  @Nullable
  Keyline getLastNonAnchorKeyline() {
    for (int i = keylines.size() - 1; i >= 0; i--) {
      Keyline keyline = keylines.get(i);
      if (!keyline.isAnchor) {
        return keyline;
      }
    }
    return null;
  }

  /** Returns how many non-anchor keylines. */
  int getNumberOfNonAnchorKeylines() {
    int anchorKeylines = 0;
    for (Keyline keyline : keylines) {
      if (keyline.isAnchor) {
        anchorKeylines += 1;
      }
    }
    return keylines.size() - anchorKeylines;
  }

  /** Returns the size of the carousel used to build this keyline state. */
  int getCarouselSize() {
    return carouselSize;
  }

  /**
   * Linearly interpolate between two {@link KeylineState}s.
   *
   * @param from the start keyline state
   * @param to the end keyline state
   * @param progress the interpolation between from and to. When progress is 0, from will be
   *     returned. When progress is 1, to will be returned.
   */
  static KeylineState lerp(KeylineState from, KeylineState to, float progress) {
    if (from.getItemSize() != to.getItemSize()) {
      throw new IllegalArgumentException(
          "Keylines being linearly interpolated must have the same item size.");
    }
    List<Keyline> fromKeylines = from.getKeylines();
    List<Keyline> toKeylines = to.getKeylines();
    if (fromKeylines.size() != toKeylines.size()) {
      throw new IllegalArgumentException(
          "Keylines being linearly interpolated must have the same number of keylines.");
    }

    List<Keyline> keylines = new ArrayList<>();
    for (int i = 0; i < from.getKeylines().size(); i++) {
      keylines.add(Keyline.lerp(fromKeylines.get(i), toKeylines.get(i), progress));
    }

    int focalKeylineFirstIndex =
        AnimationUtils.lerp(
            from.getFirstFocalKeylineIndex(), to.getFirstFocalKeylineIndex(), progress);
    int focalKeylineLastIndex =
        AnimationUtils.lerp(
            from.getLastFocalKeylineIndex(), to.getLastFocalKeylineIndex(), progress);

    return new KeylineState(
        from.getItemSize(),
        keylines,
        focalKeylineFirstIndex,
        focalKeylineLastIndex,
        from.carouselSize);
  }

  /**
   * Returns a new KeylineState that is the reverse of the passed in {@code keylineState}.
   *
   * <p>This is used to reverse a keyline state for RTL layouts.
   *
   * @param keylineState the {@link KeylineState} to reverse
   * @param carouselSize the size of the carousel used to build this keyline state
   * @return a new {@link KeylineState} that has all keylines reversed.
   */
  static KeylineState reverse(KeylineState keylineState, int carouselSize) {

    KeylineState.Builder builder =
        new KeylineState.Builder(keylineState.getItemSize(), carouselSize);

    // The new start offset should now be the same distance from the left of the carousel container
    // as the last item's right was from the right of the container.
    float start =
        carouselSize
            - keylineState.getLastKeyline().locOffset
            - (keylineState.getLastKeyline().maskedItemSize / 2F);
    for (int i = keylineState.getKeylines().size() - 1; i >= 0; i--) {
      Keyline k = keylineState.getKeylines().get(i);
      float offset = start + (k.maskedItemSize / 2F);
      boolean isFocal =
          i >= keylineState.getFirstFocalKeylineIndex()
              && i <= keylineState.getLastFocalKeylineIndex();
      builder.addKeyline(offset, k.mask, k.maskedItemSize, isFocal, k.isAnchor);
      start += k.maskedItemSize;
    }

    return builder.build();
  }

  /**
   * A builder used to construct a {@link KeylineState}.
   *
   * <p>{@link KeylineState.Builder} enforces the following rules:
   *
   * <ol>
   *   <li>There must be one or more keylines marked as "focal". These are keylines along the
   *       scrolling axis where an item or items are considered fully unmasked and viewable.
   *   <li>Focal keylines must be added adjacent to each other. A non-focal keyline cannot be added
   *       between focal keylines.
   *   <li>A keyline's masked item size can only remain the same size or increase in size as it
   *       approaches the focal range. A keyline's masked item size before the focal range cannot be
   *       larger than a focal keyline's masked item size.
   *   <li>A keyline's masked item size can only remain the same size or decrease in size as it
   *       moves away from the focal range. A keyline's masked item size after the focal range
   *       cannot be larger than a focal keyline's masked item size.
   * </ol>
   *
   * Typically there should be a keyline for every visible item in the scrolling container.
   */
  public static final class Builder {

    private static final int NO_INDEX = -1;
    private static final float UNKNOWN_LOC = Float.MIN_VALUE;

    private final float itemSize;

    private final int carouselSize;

    // A list of keylines that hold all values except the Keyline#loc which needs to be calculated
    // in the build method.
    private final List<Keyline> tmpKeylines = new ArrayList<>();
    private Keyline tmpFirstFocalKeyline;
    private Keyline tmpLastFocalKeyline;
    private int firstFocalKeylineIndex = NO_INDEX;
    private int lastFocalKeylineIndex = NO_INDEX;

    private float lastKeylineMaskedSize = 0F;

    private int latestAnchorKeylineIndex = NO_INDEX;

    /**
     * Creates a new {@link KeylineState.Builder}.
     *
     * @param itemSize The size of a fully unmasked item. This is the size that will be used by the
     *     carousel to measure and lay out all children, overriding each child's desired size.
     * @param carouselSize the size of the carousel used to build this keyline state.
     */
    public Builder(float itemSize, int carouselSize) {
      this.itemSize = itemSize;
      this.carouselSize = carouselSize;
    }

    /**
     * Adds a non-anchor keyline along the scrolling axis where an object should be masked by the
     * given {@code mask} and positioned at {@code offsetLoc}. Non-anchor keylines shift when
     * keylines shift due to scrolling.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. Typically, this means
     * keylines should be added in order of ascending {@code offsetLoc}.
     *
     * @param offsetLoc The location of this keyline along the scrolling axis. An offsetLoc of 0
     *     will be at the start of the scroll container.
     * @param mask The percentage of a child's full size that it should be masked by when its center
     *     is at {@code offsetLoc}. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     * @param isFocal Whether this keyline is considered part of the focal range. Typically, this is
     *     when {@code mask} is equal to 0.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeyline(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        boolean isFocal) {
      return addKeyline(offsetLoc, mask, maskedItemSize, isFocal, /* isAnchor= */ false);
    }

    /**
     * Adds a non-anchor keyline along the scrolling axis where an object should be masked by the
     * given {@code mask} and positioned at {@code offsetLoc}.
     *
     * @see #addKeyline(float, float, float, boolean, boolean)
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeyline(
        float offsetLoc, @FloatRange(from = 0.0F, to = 1.0F) float mask, float maskedItemSize) {
      return addKeyline(offsetLoc, mask, maskedItemSize, false);
    }

    /**
     * Adds a keyline along the scrolling axis where an object should be masked by the given {@code
     * mask} and positioned at {@code offsetLoc}.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. Typically, this means
     * keylines should be added in order of ascending {@code offsetLoc}. The first and last keylines
     * added are 'anchor' keylines that mark the start and ends of the keylines. These keylines do
     * not shift when scrolled.
     *
     * <p>Note also that {@code isFocal} and {@code isAnchor} cannot be true at the same time as
     * anchor keylines refer to keylines offscreen that dictate the ends of the keylines.
     *
     * @param offsetLoc The location of this keyline along the scrolling axis. An offsetLoc of 0
     *     will be at the start of the scroll container.
     * @param mask The percentage of a child's full size that it should be masked by when its center
     *     is at {@code offsetLoc}. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     * @param isFocal Whether this keyline is considered part of the focal range. Typically, this is
     *     when {@code mask} is equal to 0.
     * @param isAnchor Whether this keyline is an anchor keyline. Anchor keylines do not shift when
     *     keylines are shifted.
     * @param cutoff How much the keyline item is out the bounds of the available space.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeyline(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        boolean isFocal,
        boolean isAnchor,
        float cutoff,
        float leftOrTopPaddingShift,
        float rightOrBottomPaddingShift) {
      if (maskedItemSize <= 0F) {
        return this;
      }
      if (isAnchor) {
        if (isFocal) {
          throw new IllegalArgumentException("Anchor keylines cannot be focal.");
        }
        if (latestAnchorKeylineIndex != NO_INDEX && latestAnchorKeylineIndex != 0) {
          throw new IllegalArgumentException(
              "Anchor keylines must be either the first or last keyline.");
        }
        latestAnchorKeylineIndex = tmpKeylines.size();
      }

      Keyline tmpKeyline =
          new Keyline(UNKNOWN_LOC, offsetLoc, mask, maskedItemSize, isAnchor, cutoff,
              leftOrTopPaddingShift, rightOrBottomPaddingShift);
      if (isFocal) {
        if (tmpFirstFocalKeyline == null) {
          tmpFirstFocalKeyline = tmpKeyline;
          firstFocalKeylineIndex = tmpKeylines.size();
        }

        if (lastFocalKeylineIndex != NO_INDEX && tmpKeylines.size() - lastFocalKeylineIndex > 1) {
          throw new IllegalArgumentException(
              "Keylines marked as focal must be placed next to each other. There cannot be"
                  + " non-focal keylines between focal keylines.");
        }
        if (maskedItemSize != tmpFirstFocalKeyline.maskedItemSize) {
          throw new IllegalArgumentException(
              "Keylines that are marked as focal must all have the same masked item size.");
        }
        tmpLastFocalKeyline = tmpKeyline;
        lastFocalKeylineIndex = tmpKeylines.size();
      } else {
        if (tmpFirstFocalKeyline == null && tmpKeyline.maskedItemSize < lastKeylineMaskedSize) {
          throw new IllegalArgumentException(
              "Keylines before the first focal keyline must be ordered by incrementing masked item"
                  + " size.");
        } else if (tmpLastFocalKeyline != null
            && tmpKeyline.maskedItemSize > lastKeylineMaskedSize) {
          throw new IllegalArgumentException(
              "Keylines after the last focal keyline must be ordered by decreasing masked item"
                  + " size.");
        }
      }
      lastKeylineMaskedSize = tmpKeyline.maskedItemSize;
      tmpKeylines.add(tmpKeyline);
      return this;
    }

    /**
     * Adds a keyline along the scrolling axis where an object should be masked by the given {@code
     * mask} and positioned at {@code offsetLoc}.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. Typically, this means
     * keylines should be added in order of ascending {@code offsetLoc}. The first and last keylines
     * added are 'anchor' keylines that mark the start and ends of the keylines. These keylines do
     * not shift when scrolled.
     *
     * <p>Note also that {@code isFocal} and {@code isAnchor} cannot be true at the same time as
     * anchor keylines refer to keylines offscreen that dictate the ends of the keylines.
     *
     * @param offsetLoc The location of this keyline along the scrolling axis. An offsetLoc of 0
     *     will be at the start of the scroll container.
     * @param mask The percentage of a child's full size that it should be masked by when its center
     *     is at {@code offsetLoc}. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     * @param isFocal Whether this keyline is considered part of the focal range. Typically, this is
     *     when {@code mask} is equal to 0.
     * @param isAnchor Whether this keyline is an anchor keyline. Anchor keylines do not shift when
     *     keylines are shifted.
     * @param cutoff How much the keyline item is out the bounds of the available space.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeyline(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        boolean isFocal,
        boolean isAnchor,
        float cutoff) {
      return addKeyline(offsetLoc, mask, maskedItemSize, isFocal, isAnchor, cutoff,
          0, 0);
    }

    /**
     * Adds a keyline along the scrolling axis where an object should be masked by the given {@code
     * mask} and positioned at {@code offsetLoc}. This method also calculates the amount that a
     * keyline may be cut off by the bounds of the available space given.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. Typically, this means
     * keylines should be added in order of ascending {@code offsetLoc}. The first and last keylines
     * added are 'anchor' keylines that mark the start and ends of the keylines. These keylines do
     * not shift when scrolled.
     *
     * <p>Note also that {@code isFocal} and {@code isAnchor} cannot be true at the same time as
     * anchor keylines refer to keylines offscreen that dictate the ends of the keylines.
     *
     * @param offsetLoc The location of this keyline along the scrolling axis. An offsetLoc of 0
     *     will be at the start of the scroll container.
     * @param mask The percentage of a child's full size that it should be masked by when its center
     *     is at {@code offsetLoc}. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     * @param isFocal Whether this keyline is considered part of the focal range. Typically, this is
     *     when {@code mask} is equal to 0.
     * @param isAnchor Whether this keyline is an anchor keyline. Anchor keylines do not shift when
     *     keylines are shifted.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeyline(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        boolean isFocal,
        boolean isAnchor) {
      float cutoff = 0;
      // Calculate if the item will be cut off on either side. Currently we do not support an item
      // cut off on both sides as we do not not support that use case. If an item is cut off on both
      // sides, only the end cutoff will be included in the cutoff.
      float keylineStart = offsetLoc - maskedItemSize / 2F;
      float keylineEnd = offsetLoc + maskedItemSize / 2F;
      if (keylineEnd > carouselSize) {
        cutoff = Math.abs(keylineEnd - max(keylineEnd - maskedItemSize, carouselSize));
      } else if (keylineStart < 0) {
        cutoff = Math.abs(keylineStart - min(keylineStart + maskedItemSize, 0));
      }

      return addKeyline(offsetLoc, mask, maskedItemSize, isFocal, isAnchor, cutoff);
    }

    /**
     * Adds an anchor keyline along the scrolling axis where an object should be masked by the given
     * {@code mask} and positioned at {@code offsetLoc}.
     *
     * <p>Anchor keylines are keylines that are added to increase motion of carousel items going out
     * of bounds of the carousel, and are 'anchored' (ie. does not shift). These keylines must be at
     * the start or end of all keylines.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. This method should be called
     * first, or last of all the `addKeyline` calls.
     *
     * @param offsetLoc The location of this keyline along the scrolling axis. An offsetLoc of 0
     *     will be at the start of the scroll container.
     * @param mask The percentage of a child's full size that it should be masked by when its center
     *     is at {@code offsetLoc}. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addAnchorKeyline(
        float offsetLoc, @FloatRange(from = 0.0F, to = 1.0F) float mask, float maskedItemSize) {
      return addKeyline(
          offsetLoc, mask, maskedItemSize, /* isFocal= */ false, /* isAnchor= */ true);
    }

    /**
     * Adds a range of keylines along the scrolling axis where an item should be masked by {@code
     * mask} when its center is between {@code offsetLoc} and {@code offsetLoc + (maskedItemSize *
     * count)}.
     *
     * @see #addKeylineRange(float, float, float, int, boolean)
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeylineRange(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        int count) {
      return addKeylineRange(offsetLoc, mask, maskedItemSize, count, false);
    }

    /**
     * Adds a range along the scrolling axis where an object should be masked by {@code mask} when
     * its center is between {@code offsetLoc} and {@code offsetLoc + (maskedItemSize * count)}.
     *
     * <p>Note that calls to {@link #addKeyline(float, float, float, boolean)} and {@link
     * #addKeylineRange(float, float, float, int)} are added in order. Typically, this means
     * keylines should be added in order of ascending {@code offsetLoc}.
     *
     * @param offsetLoc the location along the scrolling axis where this range starts. The range's
     *     end will be defined by {@code offsetLoc + (maskedItemSize * count)}. An offsetLoc of 0
     *     will be at the start of the scrolling container.
     * @param mask the percentage of a child's full size that it should be masked by when its center
     *     is within the keyline range. 0 is fully unmasked and 1 is fully masked.
     * @param maskedItemSize the total size of this item when masked. This might differ from {@code
     *     itemSize - (itemSize * mask)} depending on how margins are included in the {@code mask}.
     * @param count The number of items that should be in this range at a time.
     * @param isFocal whether this keyline range is the focal range. Typically this is when {@code
     *     mask} is equal to 0.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder addKeylineRange(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        int count,
        boolean isFocal) {
      if (count <= 0 || maskedItemSize <= 0F) {
        return this;
      }

      for (int i = 0; i < count; i++) {
        float loc = offsetLoc + (maskedItemSize * i);
        addKeyline(loc, mask, maskedItemSize, isFocal);
      }

      return this;
    }

    /** Builds and returns a {@link KeylineState}. */
    @NonNull
    public KeylineState build() {
      if (tmpFirstFocalKeyline == null) {
        throw new IllegalStateException("There must be a keyline marked as focal.");
      }

      List<Keyline> keylines = new ArrayList<>();
      for (int i = 0; i < tmpKeylines.size(); i++) {
        Keyline tmpKeyline = tmpKeylines.get(i);
        Keyline keyline =
            new Keyline(
                calculateKeylineLocationForItemPosition(
                    tmpFirstFocalKeyline.locOffset, itemSize, firstFocalKeylineIndex, i),
                tmpKeyline.locOffset,
                tmpKeyline.mask,
                tmpKeyline.maskedItemSize,
                tmpKeyline.isAnchor,
                tmpKeyline.cutoff,
                tmpKeyline.leftOrTopPaddingShift,
                tmpKeyline.rightOrBottomPaddingShift);
        keylines.add(keyline);
      }

      return new KeylineState(
          itemSize,
          keylines,
          firstFocalKeylineIndex,
          lastFocalKeylineIndex,
          carouselSize);
    }

    /**
     * Calculates the location for a keyline where the item will be laid out if it were laid out
     * end-to-end.
     *
     * <p>The first focal keyline acts as a pivot and locations are shifted to correctly reflect
     * where the focal range lies.
     *
     * @param firstFocalLoc the location of the first focal item
     * @param itemSize the size of each item
     * @param firstFocalPosition the number of items to the left of the first focal item.
     * @param itemPosition the position of the item whose location is being calculated.
     * @return the location of the item at {@code position} if it were laid out end-to-end.
     */
    private static float calculateKeylineLocationForItemPosition(
        float firstFocalLoc, float itemSize, int firstFocalPosition, int itemPosition) {
      return firstFocalLoc - (itemSize * firstFocalPosition) + (itemPosition * itemSize);
    }
  }

  /**
   * A data class that represents a state an item should be in when its center is at a position
   * along the scroll axis.
   */
  static final class Keyline {
    final float loc;
    final float locOffset;
    final float mask;
    final float maskedItemSize;
    final boolean isAnchor;
    final float cutoff;
    final float leftOrTopPaddingShift;
    final float rightOrBottomPaddingShift;

    /**
     * Creates a non-anchor keyline along a scroll axis.
     *
     * @param loc Where this item will be along the scroll axis if it were laid out end-to-end when
     *     it should be in the state defined by {@code locOffset} and {@code mask}.
     * @param locOffset The location within the carousel where an item should be when its center is
     *     at {@code loc}.
     * @param mask The percentage of this items full size that it should be masked by when its
     *     center is at {@code loc}.
     * @param maskedItemSize The size of this item when masked.
     */
    Keyline(float loc, float locOffset, float mask, float maskedItemSize) {
      this(loc, locOffset, mask, maskedItemSize, /* isAnchor= */ false, 0,
          0, 0);
    }

    /**
     * Creates a keyline along a scroll axis.
     *
     * @param loc Where this item will be along the scroll axis if it were laid out end-to-end when
     *     it should be in the state defined by {@code locOffset} and {@code mask}.
     * @param locOffset The location within the carousel where an item should be when its center is
     *     at {@code loc}.
     * @param mask The percentage of this items full size that it should be masked by when its
     *     center is at {@code loc}.
     * @param maskedItemSize The size of this item when masked.
     * @param isAnchor Whether or not the keyline is an anchor keyline (keylines at the end that do
     *     not shift).
     * @param cutoff The amount by which the keyline item is cut off by the bounds of the carousel.
     * @param leftOrTopPaddingShift The amount by which this keyline was shifted to account for left
     *     or top padding
     * @param rightOrBottomPaddingShift The amount by which this keyline was shifted to account for
     *     right or bottom padding
     */
    Keyline(
        float loc,
        float locOffset,
        float mask,
        float maskedItemSize,
        boolean isAnchor,
        float cutoff,
        float leftOrTopPaddingShift,
        float rightOrBottomPaddingShift) {
      this.loc = loc;
      this.locOffset = locOffset;
      this.mask = mask;
      this.maskedItemSize = maskedItemSize;
      this.isAnchor = isAnchor;
      this.cutoff = cutoff;
      this.leftOrTopPaddingShift = leftOrTopPaddingShift;
      this.rightOrBottomPaddingShift = rightOrBottomPaddingShift;
    }

    /** Linearly interpolates between two keylines and returns the interpolated object. */
    static Keyline lerp(Keyline from, Keyline to, @FloatRange(from = 0, to = 1) float progress) {
      return new Keyline(
          AnimationUtils.lerp(from.loc, to.loc, progress),
          AnimationUtils.lerp(from.locOffset, to.locOffset, progress),
          AnimationUtils.lerp(from.mask, to.mask, progress),
          AnimationUtils.lerp(from.maskedItemSize, to.maskedItemSize, progress));
    }
  }
}
