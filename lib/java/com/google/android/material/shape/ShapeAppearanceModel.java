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
import static com.google.android.material.shape.CornerTreatment.withSizeAndCornerClassCheck;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

/**
 * This class models the edges and corners of a shape, which are used by {@link
 * MaterialShapeDrawable} to generate and render the shape for a view's background.
 */
public class ShapeAppearanceModel {

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
    public Builder setAllCorners(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setAllCorners(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
    }

    /**
     * Sets all corner treatments.
     *
     * @param cornerTreatment the corner treatment to use for all four corners.
     */
    @NonNull
    public Builder setAllCorners(@NonNull CornerTreatment cornerTreatment) {
      return setTopLeftCorner(cornerTreatment)
          .setTopRightCorner(cornerTreatment)
          .setBottomRightCorner(cornerTreatment)
          .setBottomLeftCorner(cornerTreatment);
    }

    /**
     * Sets the corner size of all four corner treatments to {@code cornerRadius}.
     *
     * <p>Note: This method will use {@link CornerTreatment#withSize(float)} to create a new
     * instance of the current corner treatment with the given size. This size will be lost if
     * something like {@link #setAllCorners(CornerTreatment)} is called after it.
     */
    @NonNull
    public Builder setCornerRadius(@Dimension float cornerRadius) {
      return setTopLeftCornerSize(cornerRadius)
          .setTopRightCornerSize(cornerRadius)
          .setBottomRightCornerSize(cornerRadius)
          .setBottomLeftCornerSize(cornerRadius);
    }

    /**
     * Sets the top left corner size for the current corner.
     *
     * <p>Note: This method will use {@link CornerTreatment#withSize(float)} to create a new
     * instance of the current corner treatment with the given size. This size will be lost if
     * something like {@link #setTopLeftCorner(CornerTreatment)} is called after it.
     */
    @NonNull
    public Builder setTopLeftCornerSize(@Dimension float topLeftCornerSize) {
      topLeftCorner = withSizeAndCornerClassCheck(topLeftCorner, topLeftCornerSize);
      return this;
    }

    /**
     * Sets the top right corner size for the current corner.
     *
     * <p>Note: This method will use {@link CornerTreatment#withSize(float)} to create a new
     * instance of the current corner treatment with the given size. This size will be lost if
     * something like {@link #setTopRightCorner(CornerTreatment)} is called after it.
     */
    @NonNull
    public Builder setTopRightCornerSize(@Dimension float topRightCornerSize) {
      topRightCorner = withSizeAndCornerClassCheck(topRightCorner, topRightCornerSize);
      return this;
    }

    /**
     * Sets the bottom right corner size for the current corner.
     *
     * <p>Note: This method will use {@link CornerTreatment#withSize(float)} to create a new
     * instance of the current corner treatment with the given size. This size will be lost if
     * something like {@link #setBottomRightCorner(CornerTreatment)} is called after it.
     */
    @NonNull
    public Builder setBottomRightCornerSize(@Dimension float bottomRightCornerSize) {
      bottomRightCorner =
          withSizeAndCornerClassCheck(bottomRightCorner, bottomRightCornerSize);
      return this;
    }

    /**
     * Sets the bottom left corner size for the current corner.
     *
     * <p>Note: This method will use {@link CornerTreatment#withSize(float)} to create a new
     * instance of the current corner treatment with the given size. This size will be lost if
     * something like {@link #setBottomLeftCorner(CornerTreatment)} is called after it.
     */
    @NonNull
    public Builder setBottomLeftCornerSize(@Dimension float bottomLeftCornerSize) {
      bottomLeftCorner = withSizeAndCornerClassCheck(bottomLeftCorner, bottomLeftCornerSize);
      return this;
    }

    /**
     * Sets the corner treatment for the top left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use to create the corner treatment
     */
    @NonNull
    public Builder setTopLeftCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
    }

    /**
     * Sets the corner treatment for the top left corner.
     *
     * @param topLeftCorner the desired treatment.
     */
    @NonNull
    public Builder setTopLeftCorner(@NonNull CornerTreatment topLeftCorner) {
      this.topLeftCorner = topLeftCorner;
      return this;
    }

    /**
     * Sets the corner treatment for the top right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use to create the corner treatment
     */
    @NonNull
    public Builder setTopRightCorner(@CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
    }

    /**
     * Sets the corner treatment for the top right corner.
     *
     * @param topRightCorner the desired treatment.
     */
    @NonNull
    public Builder setTopRightCorner(@NonNull CornerTreatment topRightCorner) {
      this.topRightCorner = topRightCorner;
      return this;
    }

    /**
     * Sets the corner treatment for the bottom right corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use to create the corner treatment
     */
    @NonNull
    public Builder setBottomRightCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomRightCorner(
          MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
    }

    /**
     * Sets the corner treatment for the bottom right corner.
     *
     * @param bottomRightCorner the desired treatment.
     */
    @NonNull
    public Builder setBottomRightCorner(@NonNull CornerTreatment bottomRightCorner) {
      this.bottomRightCorner = bottomRightCorner;
      return this;
    }

    /**
     * Sets the corner treatment for the bottom left corner.
     *
     * @param cornerFamily the family to use to create the corner treatment
     * @param cornerSize the size to use to create the corner treatment
     */
    @NonNull
    public Builder setBottomLeftCorner(
        @CornerFamily int cornerFamily, @Dimension float cornerSize) {
      return setBottomLeftCorner(
          MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
    }

    /**
     * Sets the corner treatment for the bottom left corner.
     *
     * @param bottomLeftCorner the desired treatment.
     */
    @NonNull
    public Builder setBottomLeftCorner(@NonNull CornerTreatment bottomLeftCorner) {
      this.bottomLeftCorner = bottomLeftCorner;
      return this;
    }

    /**
     * Sets all edge treatments.
     *
     * @param edgeTreatment the edge treatment to use for all four edges.
     */
    @NonNull
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
    public Builder setBottomEdge(@NonNull EdgeTreatment bottomEdge) {
      this.bottomEdge = bottomEdge;
      return this;
    }

    /** Adjusts all the corners by the offset. */
    @NonNull
    public Builder adjustCorners(@Dimension float offset) {
      return setTopLeftCornerSize(getOffsetCornerSize(topLeftCorner, offset))
          .setTopRightCornerSize(getOffsetCornerSize(topRightCorner, offset))
          .setBottomRightCornerSize(getOffsetCornerSize(bottomRightCorner, offset))
          .setBottomLeftCornerSize(getOffsetCornerSize(bottomLeftCorner, offset));
    }

    private static float getOffsetCornerSize(CornerTreatment cornerTreatment, float offset) {
      return Math.max(0, cornerTreatment.getCornerSize() + offset);
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
    // The attributes in shapeAppearanceOverlay should be applied on top of shapeAppearance.
    if (shapeAppearanceOverlayResId != 0) {
      context = new ContextThemeWrapper(context, shapeAppearanceResId);
      shapeAppearanceResId = shapeAppearanceOverlayResId;
    }

    TypedArray a =
        context.obtainStyledAttributes(shapeAppearanceResId, R.styleable.ShapeAppearance);

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

      int cornerSize =
          a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSize, defaultCornerSize);
      int cornerSizeTopLeft =
          a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeTopLeft, cornerSize);
      int cornerSizeTopRight =
          a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeTopRight, cornerSize);
      int cornerSizeBottomRight =
          a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeBottomRight, cornerSize);
      int cornerSizeBottomLeft =
          a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeBottomLeft, cornerSize);

      return new Builder()
          .setTopLeftCorner(cornerFamilyTopLeft, cornerSizeTopLeft)
          .setTopRightCorner(cornerFamilyTopRight, cornerSizeTopRight)
          .setBottomRightCorner(cornerFamilyBottomRight, cornerSizeBottomRight)
          .setBottomLeftCorner(cornerFamilyBottomLeft, cornerSizeBottomLeft);
    } finally {
      a.recycle();
    }
  }

  // Constant corner radius value to indicate that shape should use 50% height corner radii
  public static final int PILL = -1;

  CornerTreatment topLeftCorner;
  CornerTreatment topRightCorner;
  CornerTreatment bottomRightCorner;
  CornerTreatment bottomLeftCorner;
  EdgeTreatment topEdge;
  EdgeTreatment rightEdge;
  EdgeTreatment bottomEdge;
  EdgeTreatment leftEdge;

  private ShapeAppearanceModel(@NonNull ShapeAppearanceModel.Builder builder) {
    topLeftCorner = builder.topLeftCorner;
    topRightCorner = builder.topRightCorner;
    bottomRightCorner = builder.bottomRightCorner;
    bottomLeftCorner = builder.bottomLeftCorner;
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

  /** Checks if all four corners of this ShapeAppearanceModel are of size {@link #PILL}. */
  public boolean isUsingPillCorner() {
    return getTopRightCorner().getCornerSize() == PILL
        && getTopLeftCorner().getCornerSize() == PILL
        && getBottomLeftCorner().getCornerSize() == PILL
        && getBottomRightCorner().getCornerSize() == PILL;
  }

  /** Returns a builder with the edges and corners from this {@link ShapeAppearanceModel} */
  @NonNull
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Returns a copy of this {@link ShapeAppearanceModel} with the same edges and corners, but with
   * the corner radius for all corners updated.
   */
  @NonNull
  public ShapeAppearanceModel withCornerRadius(float cornerRadius) {
    return toBuilder().setCornerRadius(cornerRadius).build();
  }

  /**
   * Returns a copy of this {@link ShapeAppearanceModel} with the same edges and corners, but with
   * the corner radius for all corners offset by an adjustment.
   */
  @NonNull
  public ShapeAppearanceModel withAdjustedCorners(float offset) {
    return toBuilder().adjustCorners(offset).build();
  }

  /**
   * Checks Corner and Edge treatments to see if we can use {@link Canvas#drawRoundRect(RectF,float,
   * float, Paint)} "} to draw this model.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isRoundRect() {
    boolean hasDefaultEdges =
        leftEdge.getClass().equals(EdgeTreatment.class)
            && rightEdge.getClass().equals(EdgeTreatment.class)
            && topEdge.getClass().equals(EdgeTreatment.class)
            && bottomEdge.getClass().equals(EdgeTreatment.class);

    float cornerSize = topLeftCorner.getCornerSize();

    boolean cornersHaveSameSize =
        topRightCorner.getCornerSize() == cornerSize
            && bottomLeftCorner.getCornerSize() == cornerSize
            && bottomRightCorner.getCornerSize() == cornerSize;

    boolean hasRoundedCorners =
        topRightCorner instanceof RoundedCornerTreatment
            && topLeftCorner instanceof RoundedCornerTreatment
            && bottomRightCorner instanceof RoundedCornerTreatment
            && bottomLeftCorner instanceof RoundedCornerTreatment;

    return hasDefaultEdges && cornersHaveSameSize && hasRoundedCorners;
  }
}
