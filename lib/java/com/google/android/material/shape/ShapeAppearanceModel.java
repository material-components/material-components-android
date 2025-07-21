/*
 * Copyright 2018 The Android Open Source Project
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

package com.google.android.material.shape;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * This class models the edges and corners of a shape, which are used by {@link
 * MaterialShapeDrawable} to generate and render the shape for a view's background.
 */
public class ShapeAppearanceModel implements ShapeAppearance {
  public static final int NUM_CORNERS = 4;

  /** Flag representing top left corner of the shape. */
  public static final int CORNER_TOP_LEFT = 0x1;

  /** Flag representing top right corner of the shape. */
  public static final int CORNER_TOP_RIGHT = 0x2;

  /** Flag representing bottom left corner of the shape. */
  public static final int CORNER_BOTTOM_LEFT = 0x4;

  /** Flag representing bottom right corner of the shape. */
  public static final int CORNER_BOTTOM_RIGHT = 0x8;

  /** Builder to create instances of {@link ShapeAppearanceModel}s. */
  public static final class Builder {

    @NonNull
    private CornerTreatment topLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment topRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment bottomRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull
    private CornerTreatment bottomLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    @NonNull private CornerSize topLeftCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize topRightCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize bottomRightCornerSize = new AbsoluteCornerSize(0);
    @NonNull private CornerSize bottomLeftCornerSize = new AbsoluteCornerSize(0);

    @NonNull private EdgeTreatment topEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment rightEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment bottomEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    @NonNull private EdgeTreatment leftEdge = MaterialShapeUtils.createDefaultEdgeTreatment();

    public Builder() {}

    public Builder(@NonNull ShapeAppearanceModel other) {
      topLeftCorner = other.topLeftCorner;
      topRightCorner = other.topRightCorner;
      bottomRightCorner = other.bottomRightCorner;
      bottomLeftCorner = other.bottomLeftCorner;

      topLeftCornerSize = other.topLeftCornerSize;
      topRightCornerSize = other.topRightCornerSize;
      bottomRightCornerSize = other.bottomRightCornerSize;
      bottomLeftCornerSize = other.bottomLeftCornerSize;

      topEdge = other.topEdge;
      rightEdge = other.rightEdge;
      bottomEdge = other.bottomEdge;
      leftEdge = other.leftEdge;
    }

    /**
     * Sets all corner treatments to {@link CornerTreatment}s generated from a {@code cornerFamily}
     * and {@code cornerSize}.
     *
     * @param cornerFamily The family to be used to create the {@link CornerTreatment}s for all four
     *     corners. May be one of {@link CornerFamily#ROUNDED} or {@link CornerFamily#CUT}.
     * @param cornerSize The size to be used to create the {@link CornerTreatment}s for all four
     *     corners.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setAllCorners(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setAllCorners(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setAllCornerSizes(cornerSize);
    }

    /**
     * Sets all corner treatments.
     *
     * @param cornerTreatment the corner treatment to use for all four corners.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setAllCorners(@NonNull CornerTreatment cornerTreatment) {
      return setTopLeftCorner(cornerTreatment)
          .setTopRightCorner(cornerTreatment)
          .setBottomRightCorner(cornerTreatment)
          .setBottomLeftCorner(cornerTreatment);
    }

    /**
     * Sets all corner sizes.
     *
     * @param cornerSize the corner size to use for all four corners.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setAllCornerSizes(@NonNull CornerSize cornerSize) {
      return setTopLeftCornerSize(cornerSize)
          .setTopRightCornerSize(cornerSize)
          .setBottomRightCornerSize(cornerSize)
          .setBottomLeftCornerSize(cornerSize);
    }

    /** Sets the corner size of all four corner treatments to {@code cornerSize}. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setAllCornerSizes(@Dimension float cornerSize) {
      return setTopLeftCornerSize(cornerSize)
          .setTopRightCornerSize(cornerSize)
          .setBottomRightCornerSize(cornerSize)
          .setBottomLeftCornerSize(cornerSize);
    }

    /** Sets the top left corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopLeftCornerSize(@Dimension float cornerSize) {
      topLeftCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    /** Sets the top left corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopLeftCornerSize(@NonNull CornerSize cornerSize) {
      topLeftCornerSize = cornerSize;
      return this;
    }

    /** Sets the top right corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopRightCornerSize(@Dimension float cornerSize) {
      topRightCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    /** Sets the top right corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopRightCornerSize(@NonNull CornerSize cornerSize) {
      topRightCornerSize = cornerSize;
      return this;
    }

    /** Sets the bottom right corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomRightCornerSize(@Dimension float cornerSize) {
      bottomRightCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    /** Sets the bottom right corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomRightCornerSize(@NonNull CornerSize cornerSize) {
      bottomRightCornerSize = cornerSize;
      return this;
    }

    /** Sets the bottom left corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomLeftCornerSize(@Dimension float cornerSize) {
      bottomLeftCornerSize = new AbsoluteCornerSize(cornerSize);
      return this;
    }

    /** Sets the bottom left corner size for the current corner. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomLeftCornerSize(@NonNull CornerSize cornerSize) {
      bottomLeftCornerSize = cornerSize;
      return this;
    }

    /**
     * Sets the corner treatment for the top left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopLeftCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopLeftCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the top left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the {@link CornerSize} to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopLeftCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopLeftCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the top left corner.
     *
     * @param topLeftCorner the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopLeftCorner(@NonNull CornerTreatment topLeftCorner) {
      this.topLeftCorner = topLeftCorner;
      // For backwards compatibility, set the size from the treatment if it exists
      float size = compatCornerTreatmentSize(topLeftCorner);
      if (size != -1) {
        setTopLeftCornerSize(size);
      }
      return this;
    }

    /**
     * Sets the corner treatment for the top right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopRightCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopRightCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the top right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the {@link CornerSize} to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopRightCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setTopRightCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the top right corner.
     *
     * @param topRightCorner the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopRightCorner(@NonNull CornerTreatment topRightCorner) {
      this.topRightCorner = topRightCorner;
      // For backwards compatibility, set the size from the treatment if it exists
      float size = compatCornerTreatmentSize(topRightCorner);
      if (size != -1) {
        setTopRightCornerSize(size);
      }
      return this;
    }

    /**
     * Sets the corner treatment for the bottom right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomRightCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomRightCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the bottom right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the {@link CornerSize} to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomRightCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomRightCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the bottom right corner.
     *
     * @param bottomRightCorner the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomRightCorner(@NonNull CornerTreatment bottomRightCorner) {
      this.bottomRightCorner = bottomRightCorner;
      // For backwards compatibility, set the size from the treatment if it exists
      float size = compatCornerTreatmentSize(bottomRightCorner);
      if (size != -1) {
        setBottomRightCornerSize(size);
      }
      return this;
    }

    /**
     * Sets the corner treatment for the bottom left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomLeftCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomLeftCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the bottom left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the {@link CornerSize} to use for the corner
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomLeftCorner(
        @CornerFamily int cornerFamily, @NonNull CornerSize cornerSize) {
      return setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily))
          .setBottomLeftCornerSize(cornerSize);
    }

    /**
     * Sets the corner treatment for the bottom left corner.
     *
     * @param bottomLeftCorner the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomLeftCorner(@NonNull CornerTreatment bottomLeftCorner) {
      this.bottomLeftCorner = bottomLeftCorner;
      // For backwards compatibility, set the size from the treatment if it exists
      float size = compatCornerTreatmentSize(bottomLeftCorner);
      if (size != -1) {
        setBottomLeftCornerSize(size);
      }
      return this;
    }

    /**
     * Sets all edge treatments.
     *
     * @param edgeTreatment the edge treatment to use for all four edges.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setAllEdges(@NonNull EdgeTreatment edgeTreatment) {
      return setLeftEdge(edgeTreatment)
          .setTopEdge(edgeTreatment)
          .setRightEdge(edgeTreatment)
          .setBottomEdge(edgeTreatment);
    }

    /**
     * Sets the edge treatment for the left edge.
     *
     * @param leftEdge the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setLeftEdge(@NonNull EdgeTreatment leftEdge) {
      this.leftEdge = leftEdge;
      return this;
    }

    /**
     * Sets the edge treatment for the top edge.
     *
     * @param topEdge the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTopEdge(@NonNull EdgeTreatment topEdge) {
      this.topEdge = topEdge;
      return this;
    }

    /**
     * Sets the edge treatment for the right edge.
     *
     * @param rightEdge the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setRightEdge(@NonNull EdgeTreatment rightEdge) {
      this.rightEdge = rightEdge;
      return this;
    }

    /**
     * Sets the edge treatment for the bottom edge.
     *
     * @param bottomEdge the desired treatment.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setBottomEdge(@NonNull EdgeTreatment bottomEdge) {
      this.bottomEdge = bottomEdge;
      return this;
    }

    /** Pulls the corner size from specific CornerTreatments for backwards compatibility */
    private static float compatCornerTreatmentSize(CornerTreatment treatment) {
      if (treatment instanceof RoundedCornerTreatment) {
        return ((RoundedCornerTreatment) treatment).radius;
      } else if (treatment instanceof CutCornerTreatment) {
        return ((CutCornerTreatment) treatment).size;
      }
      return -1;
    }

    @NonNull
    @CanIgnoreReturnValue
    public Builder setCornerSizeOverride(int cornerPositionSet, @NonNull CornerSize cornerSize) {
      if (containsFlag(cornerPositionSet, CORNER_TOP_LEFT)) {
        setTopLeftCornerSize(cornerSize);
      }
      if (containsFlag(cornerPositionSet, CORNER_TOP_RIGHT)) {
        setTopRightCornerSize(cornerSize);
      }
      if (containsFlag(cornerPositionSet, CORNER_BOTTOM_LEFT)) {
        setBottomLeftCornerSize(cornerSize);
      }
      if (containsFlag(cornerPositionSet, CORNER_BOTTOM_RIGHT)) {
        setBottomRightCornerSize(cornerSize);
      }
      return this;
    }

    /** Builds an instance of a {@link ShapeAppearanceModel} */
    @NonNull
    public ShapeAppearanceModel build() {
      return new ShapeAppearanceModel(this);
    }
  }

  @NonNull
  public static Builder builder() {
    return new ShapeAppearanceModel.Builder();
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    return builder(context, attrs, defStyleAttr, defStyleRes, 0);
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      int defaultCornerSize) {
    return builder(
        context, attrs, defStyleAttr, defStyleRes, new AbsoluteCornerSize(defaultCornerSize));
  }

  @NonNull
  public static Builder builder(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @NonNull CornerSize defaultCornerSize) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialShape, defStyleAttr, defStyleRes);

    int shapeAppearanceResId = a.getResourceId(R.styleable.MaterialShape_shapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.MaterialShape_shapeAppearanceOverlay, 0);
    a.recycle();
    return builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId, defaultCornerSize);
  }

  @NonNull
  public static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId) {
    return builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId, 0);
  }

  @NonNull
  private static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId,
      int defaultCornerSize) {
    return builder(
        context,
        shapeAppearanceResId,
        shapeAppearanceOverlayResId,
        new AbsoluteCornerSize(defaultCornerSize));
  }

  @NonNull
  private static Builder builder(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId,
      @NonNull CornerSize defaultCornerSize) {
    // Note: we need to wrap shape appearance and shape appearance overlay to workaround b/230755281
    context = new ContextThemeWrapper(context, shapeAppearanceResId);
    if (shapeAppearanceOverlayResId != 0) {
      context.getTheme().applyStyle(shapeAppearanceOverlayResId, /* force= */ true);
    }
    TypedArray a = context.obtainStyledAttributes(R.styleable.ShapeAppearance);
    return builder(a, defaultCornerSize);
  }

  @NonNull
  private static Builder builder(TypedArray a, @NonNull CornerSize defaultCornerSize) {
    try {
      int cornerFamily = a.getInt(R.styleable.ShapeAppearance_cornerFamily, CornerFamily.ROUNDED);
      int cornerFamilyTopLeft =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopLeft, cornerFamily);
      int cornerFamilyTopRight =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopRight, cornerFamily);
      int cornerFamilyBottomRight =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomRight, cornerFamily);
      int cornerFamilyBottomLeft =
          a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomLeft, cornerFamily);

      CornerSize cornerSize =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSize, defaultCornerSize);

      CornerSize cornerSizeTopLeft =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeTopLeft, cornerSize);
      CornerSize cornerSizeTopRight =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeTopRight, cornerSize);
      CornerSize cornerSizeBottomRight =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeBottomRight, cornerSize);
      CornerSize cornerSizeBottomLeft =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSizeBottomLeft, cornerSize);

      return new Builder()
          .setTopLeftCorner(cornerFamilyTopLeft, cornerSizeTopLeft)
          .setTopRightCorner(cornerFamilyTopRight, cornerSizeTopRight)
          .setBottomRightCorner(cornerFamilyBottomRight, cornerSizeBottomRight)
          .setBottomLeftCorner(cornerFamilyBottomLeft, cornerSizeBottomLeft);
    } finally {
      a.recycle();
    }
  }

  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public static CornerSize getCornerSize(
      @NonNull TypedArray a, int index, @NonNull CornerSize defaultValue) {
    TypedValue value = a.peekValue(index);
    if (value == null) {
      return defaultValue;
    }

    if (value.type == TypedValue.TYPE_DIMENSION) {
      // Eventually we might want to change this to call getDimension() since corner sizes support
      // floats.
      return new AbsoluteCornerSize(
          TypedValue.complexToDimensionPixelSize(value.data, a.getResources().getDisplayMetrics()));
    } else if (value.type == TypedValue.TYPE_FRACTION) {
      return new RelativeCornerSize(value.getFraction(1.0f, 1.0f));
    } else {
      return defaultValue;
    }
  }

  // Constant corner size value to indicate that shape should use 50% height corner radii
  public static final CornerSize PILL = new RelativeCornerSize(0.5f);

  CornerTreatment topLeftCorner;
  CornerTreatment topRightCorner;
  CornerTreatment bottomRightCorner;
  CornerTreatment bottomLeftCorner;
  CornerSize topLeftCornerSize;
  CornerSize topRightCornerSize;
  CornerSize bottomRightCornerSize;
  CornerSize bottomLeftCornerSize;
  EdgeTreatment topEdge;
  EdgeTreatment rightEdge;
  EdgeTreatment bottomEdge;
  EdgeTreatment leftEdge;

  private ShapeAppearanceModel(@NonNull ShapeAppearanceModel.Builder builder) {
    topLeftCorner = builder.topLeftCorner;
    topRightCorner = builder.topRightCorner;
    bottomRightCorner = builder.bottomRightCorner;
    bottomLeftCorner = builder.bottomLeftCorner;

    topLeftCornerSize = builder.topLeftCornerSize;
    topRightCornerSize = builder.topRightCornerSize;
    bottomRightCornerSize = builder.bottomRightCornerSize;
    bottomLeftCornerSize = builder.bottomLeftCornerSize;

    topEdge = builder.topEdge;
    rightEdge = builder.rightEdge;
    bottomEdge = builder.bottomEdge;
    leftEdge = builder.leftEdge;
  }

  /** Constructs a default path generator with default edge and corner treatments. */
  public ShapeAppearanceModel() {
    topLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    topRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    bottomRightCorner = MaterialShapeUtils.createDefaultCornerTreatment();
    bottomLeftCorner = MaterialShapeUtils.createDefaultCornerTreatment();

    topLeftCornerSize = new AbsoluteCornerSize(0);
    topRightCornerSize = new AbsoluteCornerSize(0);
    bottomRightCornerSize = new AbsoluteCornerSize(0);
    bottomLeftCornerSize = new AbsoluteCornerSize(0);

    topEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    rightEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    bottomEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
    leftEdge = MaterialShapeUtils.createDefaultEdgeTreatment();
  }

  /**
   * Gets the corner treatment for the top left corner.
   *
   * @return the corner treatment for the top left corner.
   */
  @NonNull
  public CornerTreatment getTopLeftCorner() {
    return topLeftCorner;
  }

  /**
   * Gets the corner treatment for the top right corner.
   *
   * @return the corner treatment for the top right corner.
   */
  @NonNull
  public CornerTreatment getTopRightCorner() {
    return topRightCorner;
  }

  /**
   * Gets the corner treatment for the bottom right corner.
   *
   * @return the corner treatment for the bottom right corner.
   */
  @NonNull
  public CornerTreatment getBottomRightCorner() {
    return bottomRightCorner;
  }

  /**
   * Gets the corner treatment for the bottom left corner.
   *
   * @return the corner treatment for the bottom left corner.
   */
  @NonNull
  public CornerTreatment getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  /**
   * Gets the corner size for the top left corner.
   *
   * @return the corner size for the top left corner.
   */
  @NonNull
  public CornerSize getTopLeftCornerSize() {
    return topLeftCornerSize;
  }

  /**
   * Gets the corner size for the top right corner.
   *
   * @return the corner size for the top right corner.
   */
  @NonNull
  public CornerSize getTopRightCornerSize() {
    return topRightCornerSize;
  }

  /**
   * Gets the corner size for the bottom right corner.
   *
   * @return the corner size for the bottom right corner.
   */
  @NonNull
  public CornerSize getBottomRightCornerSize() {
    return bottomRightCornerSize;
  }

  /**
   * Gets the corner size for the bottom left corner.
   *
   * @return the corner size for the bottom left corner.
   */
  @NonNull
  public CornerSize getBottomLeftCornerSize() {
    return bottomLeftCornerSize;
  }

  /**
   * Gets the edge treatment for the left edge.
   *
   * @return the edge treatment for the left edge.
   */
  @NonNull
  public EdgeTreatment getLeftEdge() {
    return leftEdge;
  }

  /**
   * Gets the edge treatment for the top edge.
   *
   * @return the edge treatment for the top edge.
   */
  @NonNull
  public EdgeTreatment getTopEdge() {
    return topEdge;
  }

  /**
   * Gets the edge treatment for the right edge.
   *
   * @return the edge treatment for the right edge.
   */
  @NonNull
  public EdgeTreatment getRightEdge() {
    return rightEdge;
  }

  /**
   * Gets the edge treatment for the bottom edge.
   *
   * @return the edge treatment for the bottom edge.
   */
  @NonNull
  public EdgeTreatment getBottomEdge() {
    return bottomEdge;
  }

  /** Returns a builder with the edges and corners from this {@link ShapeAppearanceModel} */
  @NonNull
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Returns a copy of this {@link ShapeAppearanceModel} with the same edges and corners, but with
   * the corner size for all corners updated.
   */
  @Override
  @NonNull
  public ShapeAppearanceModel withCornerSize(float cornerSize) {
    return toBuilder().setAllCornerSizes(cornerSize).build();
  }

  @Override
  @NonNull
  public ShapeAppearanceModel withCornerSize(@NonNull CornerSize cornerSize) {
    return toBuilder().setAllCornerSizes(cornerSize).build();
  }

  @Override
  public boolean isStateful() {
    return false;
  }

  /**
   * A UnaryOperator that takes and returns a CornerSize.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public interface CornerSizeUnaryOperator {
    @NonNull
    CornerSize apply(@NonNull CornerSize cornerSize);
  }

  /**
   * Returns a copy of this {@link ShapeAppearanceModel} with the same edges and corners, but with
   * the corner size for all corners converted by a {@link CornerSizeUnaryOperator}.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public ShapeAppearanceModel withTransformedCornerSizes(@NonNull CornerSizeUnaryOperator op) {
    return toBuilder()
        .setTopLeftCornerSize(op.apply(getTopLeftCornerSize()))
        .setTopRightCornerSize(op.apply(getTopRightCornerSize()))
        .setBottomLeftCornerSize(op.apply(getBottomLeftCornerSize()))
        .setBottomRightCornerSize(op.apply(getBottomRightCornerSize()))
        .build();
  }

  /**
   * Checks Corner and Edge treatments to see if we can use {@link Canvas#drawRoundRect(RectF,float,
   * float, Paint)} "} to draw this model.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isRoundRect(@NonNull RectF bounds) {
    boolean hasDefaultEdges =
        leftEdge.getClass().equals(EdgeTreatment.class)
            && rightEdge.getClass().equals(EdgeTreatment.class)
            && topEdge.getClass().equals(EdgeTreatment.class)
            && bottomEdge.getClass().equals(EdgeTreatment.class);

    float cornerSize = topLeftCornerSize.getCornerSize(bounds);

    boolean cornersHaveSameSize =
        topRightCornerSize.getCornerSize(bounds) == cornerSize
            && bottomLeftCornerSize.getCornerSize(bounds) == cornerSize
            && bottomRightCornerSize.getCornerSize(bounds) == cornerSize;

    return hasDefaultEdges && cornersHaveSameSize && hasRoundedCorners();
  }

  /**
   * Checks if all corners are rounded corners.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean hasRoundedCorners() {
    return topRightCorner instanceof RoundedCornerTreatment
        && topLeftCorner instanceof RoundedCornerTreatment
        && bottomRightCorner instanceof RoundedCornerTreatment
        && bottomLeftCorner instanceof RoundedCornerTreatment;
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getDefaultShape() {
    return this;
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeForState(@NonNull int[] stateSet) {
    return this;
  }

  @NonNull
  @Override
  public ShapeAppearanceModel[] getShapeAppearanceModels() {
    return new ShapeAppearanceModel[] { this };
  }

  static boolean containsFlag(int flagSet, int flag) {
    return (flagSet | flag) == flagSet;
  }

  @NonNull
  @Override
  public String toString() {
    return "["
        + getTopLeftCornerSize()
        + ", "
        + getTopRightCornerSize()
        + ", "
        + getBottomRightCornerSize()
        + ", "
        + getBottomLeftCornerSize()
        + "]";
  }
}
