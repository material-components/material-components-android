/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.progressindicator;

/** A spec class managing all attributes of {@link ProgressIndicator}. */
public final class ProgressIndicatorSpec {

  /** The type of the progress indicator, either {@code #LINEAR} or {@code #CIRCULAR}. */
  public int indicatorType;

  /** The width of the progress track and indicator. */
  public int indicatorWidth;

  /**
   * When this is greater than 0, the corners of both the track and the indicator will be rounded
   * with this radius. If the radius is greater than half of the track width, an {@code
   * IllegalArgumentException} will be thrown during initialization.
   */
  public int indicatorCornerRadius;

  /**
   * The color array of the progress stroke. In determinate mode and single color indeterminate
   * mode, only the first item will be used. This field combines the attribute indicatorColor and
   * indicatorColors defined in the XML.
   */
  public int[] indicatorColors;

  /**
   * The color used for the progress track. If not defined, it will be set to the indicatorColor and
   * apply the first disable alpha value from the theme.
   */
  public int trackColor;

  /**
   * Whether to inverse the progress direction. Linear positive directory is start-to-end; circular
   * positive directory is clockwise.
   */
  public boolean inverse;

  /**
   * How the progress indicator appears and disappears. {@see #GROW_MODE_NONE} {@see
   * #GROW_MODE_INCOMING} {@see #GROW_MODE_OUTGOING} {@see #GROW_MODE_BIDIRECTIONAL}
   */
  public int growMode;

  /** The extra space from the edge of the stroke to the edge of canvas. Ignored in linear mode. */
  public int circularInset;

  /** The radius of the outer bound of the circular progress stroke. Ignored in linear mode. */
  public int circularRadius;

  /**
   * The animation style used in indeterminate mode. The strokes in different colors are end-to-end
   * connected. Ignored for determinate mode and indeterminate mode with less than 3 colors.
   */
  public boolean linearSeamless;
}
