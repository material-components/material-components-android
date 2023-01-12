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

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import com.google.android.material.animation.AnimationUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An arrangement of {@link Keyline}s that are positioned along a scrolling axis.
 *
 * <p>This class is the structure used to tell a scrolling item how it should be masked, offset, or
 * treated, at certain points along the scrolling axis.
 *
 * <p>KeylineState enforces the following rules:
 *
 * <ol>
 *   <li>There must be one or two keylines marked as "focal". These define the range along the
 *       scrolling axis where an item or items are considered fully unmasked and viewable.
 *   <li>Keylines can only remain the same size or increase in size as they approach the focal
 *       range. Keylines before the focal range cannot be larger than a focal item.
 *   <li>Keylines can only remain the same size or decrease in size as they move away from the focal
 *       range. Keylines after the focal range cannot be larger than a focal item.
 * </ol>
 */
final class KeylineState {

  private final float itemSize;
  private final List<Keyline> keylines;
  private final int firstFocalKeylineIndex;
  private final int lastFocalKeylineIndex;

  KeylineState(
      float itemSize,
      List<Keyline> keylines,
      int firstFocalKeylineIndex,
      int lastFocalKeylineIndex) {
    this.itemSize = itemSize;
    this.keylines = Collections.unmodifiableList(keylines);
    this.firstFocalKeylineIndex = firstFocalKeylineIndex;
    this.lastFocalKeylineIndex = lastFocalKeylineIndex;
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

  /** Returns the first keyline. */
  Keyline getFirstKeyline() {
    return keylines.get(0);
  }

  /** Returns the last keyline. */
  Keyline getLastKeyline() {
    return keylines.get(keylines.size() - 1);
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
        from.getItemSize(), keylines, focalKeylineFirstIndex, focalKeylineLastIndex);
  }

  /**
   * Returns a new KeylineState that is the reverse of the passed in {@code keylineState}.
   *
   * <p>This is used to reverse a keyline state for RTL layouts.
   *
   * @param keylineState the {@link KeylineState} to reverse
   * @return a new {@link KeylineState} that has all keylines reversed.
   */
  static KeylineState reverse(KeylineState keylineState) {

    KeylineState.Builder builder = new KeylineState.Builder(keylineState.getItemSize());

    float start =
        keylineState.getFirstKeyline().locOffset
            - (keylineState.getFirstKeyline().maskedItemSize / 2F);
    for (int i = keylineState.getKeylines().size() - 1; i >= 0; i--) {
      Keyline k = keylineState.getKeylines().get(i);
      float offset = start + (k.maskedItemSize / 2F);
      boolean isFocal =
          i >= keylineState.getFirstFocalKeylineIndex()
              && i <= keylineState.getLastFocalKeylineIndex();
      builder.addKeyline(offset, k.mask, k.maskedItemSize, isFocal);
      start += k.maskedItemSize;
    }

    return builder.build();
  }

  static final class Builder {

    private static final int NO_INDEX = -1;
    private static final float UNKNOWN_LOC = Float.MIN_VALUE;

    private final float itemSize;

    // A list of keylines that hold all values except the Keyline#loc which needs to be calculated
    // in the build method.
    private final List<Keyline> tmpKeylines = new ArrayList<>();
    private Keyline tmpFirstFocalKeyline;
    private Keyline tmpLastFocalKeyline;
    private int firstFocalKeylineIndex = NO_INDEX;
    private int lastFocalKeylineIndex = NO_INDEX;

    private float lastKeylineMaskedSize = 0F;

    /**
     * Creates a new {@link KeylineState.Builder}.
     *
     * @param itemSize the size of a fully unmaksed item. All mask values will be a percentage of
     *     this size.
     */
    Builder(float itemSize) {
      this.itemSize = itemSize;
    }

    /**
     * Adds a point along the scrolling axis where an object should be masked by the given {@code
     * mask} percentage.
     */
    @NonNull
    @CanIgnoreReturnValue
    Builder addKeyline(
        float offsetLoc, @FloatRange(from = 0.0F, to = 1.0F) float mask, float maskedItemSize) {
      return addKeyline(offsetLoc, mask, maskedItemSize, false);
    }

    /**
     * Adds a point along the scrolling axis where an object should be masked by the given {@code
     * mask} percentage.
     *
     * <p>Keylines are added in order! Keylines added at the beginning of the list will appear at
     * the start of the scroll axis.
     *
     * @param offsetLoc The location along the axis where this keyline is positioned.
     * @param mask The percentage of {@code itemSize} that a child should be masked by when its
     *     center is at {@code loc}.
     * @param maskedItemSize The total size of this item when masked. This might differ from the
     *     masked size depending on how margins are included in the mask.
     * @param isFocal Whether this keyline marks the beginning or end of the focal range.
     */
    @NonNull
    @CanIgnoreReturnValue
    Builder addKeyline(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        boolean isFocal) {
      if (maskedItemSize <= 0F) {
        return this;
      }

      Keyline tmpKeyline = new Keyline(UNKNOWN_LOC, offsetLoc, mask, maskedItemSize);
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
     * Adds a range along the scrolling axis where an object should be masked by {@code mask} when
     * its center is between {@code offsetLoc} and {@code offsetLoc * (maskedItemSize + count)}.
     */
    @NonNull
    @CanIgnoreReturnValue
    Builder addKeylineRange(
        float offsetLoc,
        @FloatRange(from = 0.0F, to = 1.0F) float mask,
        float maskedItemSize,
        int count) {
      return addKeylineRange(offsetLoc, mask, maskedItemSize, count, false);
    }

    /**
     * Adds a range along the scrolling axis where an object should be masked by {@code mask} when
     * its center is between {@code offsetLoc} and {@code offsetLoc * (maskedItemSize + count)}.
     *
     * <p>Keyline ranges are added in order! Keyline ranges added at the beginning of the list will
     * appear at the start of the scroll axis. Also note that keylines can only increase in size or
     * remain the same size as they approach the focal range and decrease in size or remain the same
     * size as they exit the focal range.
     *
     * @param offsetLoc location along the axis where this range starts.
     * @param mask The percentage of {@code itemSize} that a child should be masked by when its
     *     center is within this keyline range. 0F is fully unmasked and 1F is fully masked.
     * @param maskedItemSize The total size of this item when masked. This might differ from the
     *     masked size depending on how margins are included in the mask.
     * @param count The number of items that should be in this range at a time.
     * @param isFocal Whether this range should be used to align the keylines within the scroll
     *     container.
     */
    @NonNull
    @CanIgnoreReturnValue
    Builder addKeylineRange(
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

    @NonNull
    KeylineState build() {
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
                tmpKeyline.maskedItemSize);
        keylines.add(keyline);
      }

      return new KeylineState(itemSize, keylines, firstFocalKeylineIndex, lastFocalKeylineIndex);
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

    /**
     * Creates a keyline along a scroll axis.
     *
     * @param loc Where this item will be along the scroll axis if it were laid out end-to-end when
     *     it should be in the state defined by {@code locOffset} and {@code mask}.
     * @param locOffset The location within the carousel where an item should be when its center is
     *     at {@code loc}.
     * @param mask The percentage of this items full width that it should be masked by when its
     *     center is at {@code loc}.
     * @param maskedItemSize The size of this item when masked.
     */
    Keyline(float loc, float locOffset, float mask, float maskedItemSize) {
      this.loc = loc;
      this.locOffset = locOffset;
      this.mask = mask;
      this.maskedItemSize = maskedItemSize;
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
