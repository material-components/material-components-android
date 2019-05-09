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
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class models the edges and corners of a shape, which are used by {@link
 * MaterialShapeDrawable} to generate and render the shape for a view's background.
 */
public class ShapeAppearanceModel {

  /**
   * Listener called every time a {@link ShapeAppearanceModel} corner or edge is modified and
   * notifies the {@link MaterialShapeDrawable} that the shape has changed so that it can invalidate
   * itself. Components that need to respond to shape changes can use this interface to get a
   * callback to respond to shape changes.
   */
  public interface OnChangedListener {

    /** Callback invoked when a corner or edge of the {@link ShapeAppearanceModel} changes. */
    void onShapeAppearanceModelChanged();
  }

  // Constant corner radius value to indicate that shape should use 50% height corner radii
  public static final int PILL = -1;

  private CornerTreatment topLeftCorner;
  private CornerTreatment topRightCorner;
  private CornerTreatment bottomRightCorner;
  private CornerTreatment bottomLeftCorner;
  private EdgeTreatment topEdge;
  private EdgeTreatment rightEdge;
  private EdgeTreatment bottomEdge;
  private EdgeTreatment leftEdge;

  private final Set<OnChangedListener> onChangedListeners = new LinkedHashSet<>();

  /** Constructs a default path generator with default edge and corner treatments. */
  public ShapeAppearanceModel() {
    setTopLeftCornerInternal(MaterialShapeUtils.createDefaultCornerTreatment());
    setTopRightCornerInternal(MaterialShapeUtils.createDefaultCornerTreatment());
    setBottomRightCornerInternal(MaterialShapeUtils.createDefaultCornerTreatment());
    setBottomLeftCornerInternal(MaterialShapeUtils.createDefaultCornerTreatment());

    setLeftEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setTopEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setRightEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setBottomEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());

    onShapeAppearanceModelChanged();
  }

  public ShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
    setTopLeftCornerInternal(shapeAppearanceModel.getTopLeftCorner().clone());
    setTopRightCornerInternal(shapeAppearanceModel.getTopRightCorner().clone());
    setBottomRightCornerInternal(shapeAppearanceModel.getBottomRightCorner().clone());
    setBottomLeftCornerInternal(shapeAppearanceModel.getBottomLeftCorner().clone());

    setLeftEdgeInternal(shapeAppearanceModel.getLeftEdge().clone());
    setTopEdgeInternal(shapeAppearanceModel.getTopEdge().clone());
    setRightEdgeInternal(shapeAppearanceModel.getRightEdge().clone());
    setBottomEdgeInternal(shapeAppearanceModel.getBottomEdge().clone());
  }

  public ShapeAppearanceModel(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    this(context, attrs, defStyleAttr, defStyleRes, 0);
  }

  public ShapeAppearanceModel(
      Context context,
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
    initFromShapeAppearanceStyle(
        context, shapeAppearanceResId, shapeAppearanceOverlayResId, defaultCornerSize);
  }

  public ShapeAppearanceModel(
      Context context,
      @StyleRes int shapeAppearanceResId,
      @StyleRes int shapeAppearanceOverlayResId) {
    initFromShapeAppearanceStyle(context, shapeAppearanceResId, shapeAppearanceOverlayResId, 0);
  }

  private final void initFromShapeAppearanceStyle(
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

    setTopLeftCornerInternal(
        MaterialShapeUtils.createCornerTreatment(cornerFamilyTopLeft, cornerSizeTopLeft));
    setTopRightCornerInternal(
        MaterialShapeUtils.createCornerTreatment(cornerFamilyTopRight, cornerSizeTopRight));
    setBottomRightCornerInternal(
        MaterialShapeUtils.createCornerTreatment(cornerFamilyBottomRight, cornerSizeBottomRight));
    setBottomLeftCornerInternal(
        MaterialShapeUtils.createCornerTreatment(cornerFamilyBottomLeft, cornerSizeBottomLeft));

    setTopEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setRightEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setBottomEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());
    setLeftEdgeInternal(MaterialShapeUtils.createDefaultEdgeTreatment());

    a.recycle();
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
  public void setAllCorners(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setAllCorners(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets all corner treatments.
   *
   * @param cornerTreatment the corner treatment to use for all four corners.
   */
  public void setAllCorners(CornerTreatment cornerTreatment) {
    boolean changed = setTopLeftCornerInternal(cornerTreatment.clone());
    changed |= setTopRightCornerInternal(cornerTreatment.clone());
    changed |= setBottomRightCornerInternal(cornerTreatment.clone());
    changed |= setBottomLeftCornerInternal(cornerTreatment.clone());

    if (changed) {
      onShapeAppearanceModelChanged();
    }
  }

  /**
   * Sets the corner size of all four corner treatments to {@code cornerRadius}. This is a 
   * convenience method for {@link #setCornerRadii(float, float, float, float)})}.
   *
   * <p>Note: This method does not create new {@link CornerTreatment}s for all four corners.
   * Instead, it directly modifies the corner size of each existing corner treatment.
   *
   * @see #setCornerRadii(float, float, float, float)
   */
  public void setCornerRadius(float cornerRadius) {
    setCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
  }

  /**
   * Sets the corner size of all four corner treatments using the {@code topLeftCornerRadius},
   * {@code topRightCornerRadius}, {@code bottomRightCornerRadius}, and {@code
   * bottomLeftCornerRadius}.
   *
   * <p>Note: This method does not create new {@link CornerTreatment}s for all four corners.
   * Instead, it directly modifies the corner size of each existing corner treatment.
   */
  public void setCornerRadii(
      float topLeftCornerRadius,
      float topRightCornerRadius,
      float bottomRightCornerRadius,
      float bottomLeftCornerRadius) {
    boolean changed = setTopLeftCornerSizeInternal(topLeftCornerRadius);
    changed |= setTopRightCornerSizeInternal(topRightCornerRadius);
    changed |= setBottomRightCornerSizeInternal(bottomRightCornerRadius);
    changed |= setBottomLeftCornerSizeInternal(bottomLeftCornerRadius);

    if (changed) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setTopLeftCornerSizeInternal(float topLeftCornerSize) {
    boolean changed = false;
    if (this.topLeftCorner.cornerSize != topLeftCornerSize) {
      this.topLeftCorner.cornerSize = topLeftCornerSize;
      changed = true;
    }
    return changed;
  }

  private boolean setTopRightCornerSizeInternal(float topRightCornerSize) {
    boolean changed = false;
    if (this.topRightCorner.cornerSize != topRightCornerSize) {
      this.topRightCorner.cornerSize = topRightCornerSize;
      changed = true;
    }
    return changed;
  }

  private boolean setBottomRightCornerSizeInternal(float bottomRightCornerSize) {
    boolean changed = false;
    if (this.bottomRightCorner.cornerSize != bottomRightCornerSize) {
      this.bottomRightCorner.cornerSize = bottomRightCornerSize;
      changed = true;
    }
    return changed;
  }

  private boolean setBottomLeftCornerSizeInternal(float bottomLeftCornerSize) {
    boolean changed = false;
    if (this.bottomLeftCorner.cornerSize != bottomLeftCornerSize) {
      this.bottomLeftCorner.cornerSize = bottomLeftCornerSize;
      changed = true;
    }
    return changed;
  }

  /**
   * Sets all edge treatments.
   *
   * @param edgeTreatment the edge treatment to use for all four edges.
   */
  public void setAllEdges(EdgeTreatment edgeTreatment) {
    boolean changed = setLeftEdgeInternal(edgeTreatment.clone());
    changed |= setTopEdgeInternal(edgeTreatment.clone());
    changed |= setRightEdgeInternal(edgeTreatment.clone());
    changed |= setBottomEdgeInternal(edgeTreatment.clone());

    if (changed) {
      onShapeAppearanceModelChanged();
    }
  }

  /**
   * Sets corner treatments.
   *
   * @param topLeftCorner the corner treatment to use in the top left corner.
   * @param topRightCorner the corner treatment to use in the top right corner.
   * @param bottomRightCorner the corner treatment to use in the bottom right corner.
   * @param bottomLeftCorner the corner treatment to use in the bottom left corner.
   */
  public void setCornerTreatments(
      CornerTreatment topLeftCorner,
      CornerTreatment topRightCorner,
      CornerTreatment bottomRightCorner,
      CornerTreatment bottomLeftCorner) {
    boolean changed = setTopLeftCornerInternal(topLeftCorner);
    changed |= setTopRightCornerInternal(topRightCorner);
    changed |= setBottomRightCornerInternal(bottomRightCorner);
    changed |= setBottomLeftCornerInternal(bottomLeftCorner);

    if (changed) {
      onShapeAppearanceModelChanged();
    }
  }

  /**
   * Sets edge treatments.
   *
   * @param leftEdge the edge treatment to use on the left edge.
   * @param topEdge the edge treatment to use on the top edge.
   * @param rightEdge the edge treatment to use on the right edge.
   * @param bottomEdge the edge treatment to use on the bottom edge.
   */
  public void setEdgeTreatments(
      EdgeTreatment leftEdge,
      EdgeTreatment topEdge,
      EdgeTreatment rightEdge,
      EdgeTreatment bottomEdge) {
    boolean changed = setLeftEdgeInternal(leftEdge);
    changed |= setTopEdgeInternal(topEdge);
    changed |= setRightEdgeInternal(rightEdge);
    changed |= setBottomEdgeInternal(bottomEdge);

    if (changed) {
      onShapeAppearanceModelChanged();
    }
  }

  /**
   * Sets the corner treatment for the top left corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setTopLeftCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the top left corner.
   *
   * @param topLeftCorner the desired treatment.
   */
  public void setTopLeftCorner(CornerTreatment topLeftCorner) {
    if (setTopLeftCornerInternal(topLeftCorner)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setTopLeftCornerInternal(CornerTreatment topLeftCorner) {
    boolean changed = false;
    if (this.topLeftCorner != topLeftCorner) {
      this.topLeftCorner = topLeftCorner;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the corner treatment for the top left corner.
   *
   * @return the corner treatment for the top left corner.
   */
  public CornerTreatment getTopLeftCorner() {
    return topLeftCorner;
  }

  /**
   * Sets the corner treatment for the top right corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setTopRightCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the top right corner.
   *
   * @param topRightCorner the desired treatment.
   */
  public void setTopRightCorner(CornerTreatment topRightCorner) {
    if (setTopRightCornerInternal(topRightCorner)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setTopRightCornerInternal(CornerTreatment topRightCorner) {
    boolean changed = false;
    if (this.topRightCorner != topRightCorner) {
      this.topRightCorner = topRightCorner;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the corner treatment for the top right corner.
   *
   * @return the corner treatment for the top right corner.
   */
  public CornerTreatment getTopRightCorner() {
    return topRightCorner;
  }

  /**
   * Sets the corner treatment for the bottom right corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setBottomRightCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the bottom right corner.
   *
   * @param bottomRightCorner the desired treatment.
   */
  public void setBottomRightCorner(CornerTreatment bottomRightCorner) {
    if (setBottomRightCornerInternal(bottomRightCorner)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setBottomRightCornerInternal(CornerTreatment bottomRightCorner) {
    boolean changed = false;
    if (this.bottomRightCorner != bottomRightCorner) {
      this.bottomRightCorner = bottomRightCorner;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the corner treatment for the bottom right corner.
   *
   * @return the corner treatment for the bottom right corner.
   */
  public CornerTreatment getBottomRightCorner() {
    return bottomRightCorner;
  }

  /**
   * Sets the corner treatment for the bottom left corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setBottomLeftCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the bottom left corner.
   *
   * @param bottomLeftCorner the desired treatment.
   */
  public void setBottomLeftCorner(CornerTreatment bottomLeftCorner) {
    if (setBottomLeftCornerInternal(bottomLeftCorner)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setBottomLeftCornerInternal(CornerTreatment bottomLeftCorner) {
    boolean changed = false;
    if (this.bottomLeftCorner != bottomLeftCorner) {
      this.bottomLeftCorner = bottomLeftCorner;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the corner treatment for the bottom left corner.
   *
   * @return the corner treatment for the bottom left corner.
   */
  public CornerTreatment getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  /**
   * Sets the edge treatment for the left edge.
   *
   * @param leftEdge the desired treatment.
   */
  public void setLeftEdge(EdgeTreatment leftEdge) {
    if (setLeftEdgeInternal(leftEdge)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setLeftEdgeInternal(EdgeTreatment leftEdge) {
    boolean changed = false;
    if (this.leftEdge != leftEdge) {
      this.leftEdge = leftEdge;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the edge treatment for the left edge.
   *
   * @return the edge treatment for the left edge.
   */
  public EdgeTreatment getLeftEdge() {
    return leftEdge;
  }

  /**
   * Sets the edge treatment for the top edge.
   *
   * @param topEdge the desired treatment.
   */
  public void setTopEdge(EdgeTreatment topEdge) {
    if (setTopEdgeInternal(topEdge)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setTopEdgeInternal(EdgeTreatment topEdge) {
    boolean changed = false;
    if (this.topEdge != topEdge) {
      this.topEdge = topEdge;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the edge treatment for the top edge.
   *
   * @return the edge treatment for the top edge.
   */
  public EdgeTreatment getTopEdge() {
    return topEdge;
  }

  /**
   * Sets the edge treatment for the right edge.
   *
   * @param rightEdge the desired treatment.
   */
  public void setRightEdge(EdgeTreatment rightEdge) {
    if (setRightEdgeInternal(rightEdge)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setRightEdgeInternal(EdgeTreatment rightEdge) {
    boolean changed = false;
    if (this.rightEdge != rightEdge) {
      this.rightEdge = rightEdge;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the edge treatment for the right edge.
   *
   * @return the edge treatment for the right edge.
   */
  public EdgeTreatment getRightEdge() {
    return rightEdge;
  }

  /**
   * Sets the edge treatment for the bottom edge.
   *
   * @param bottomEdge the desired treatment.
   */
  public void setBottomEdge(EdgeTreatment bottomEdge) {
    if (setBottomEdgeInternal(bottomEdge)) {
      onShapeAppearanceModelChanged();
    }
  }

  private boolean setBottomEdgeInternal(EdgeTreatment bottomEdge) {
    boolean changed = false;
    if (this.bottomEdge != bottomEdge) {
      this.bottomEdge = bottomEdge;
      changed = true;
    }
    return changed;
  }

  /**
   * Gets the edge treatment for the bottom edge.
   *
   * @return the edge treatment for the bottom edge.
   */
  public EdgeTreatment getBottomEdge() {
    return bottomEdge;
  }

  void addOnChangedListener(@Nullable OnChangedListener onChangedListener) {
    onChangedListeners.add(onChangedListener);
  }

  void removeOnChangedListener(@Nullable OnChangedListener onChangedListener) {
    onChangedListeners.remove(onChangedListener);
  }

  /** Checks if all four corners of this ShapeAppearanceModel are of size {@link #PILL}. */
  public boolean isUsingPillCorner() {
    return getTopRightCorner().getCornerSize() == PILL
        && getTopLeftCorner().getCornerSize() == PILL
        && getBottomLeftCorner().getCornerSize() == PILL
        && getBottomRightCorner().getCornerSize() == PILL;
  }

  private void onShapeAppearanceModelChanged() {
    for (OnChangedListener onChangedListener : onChangedListeners) {
      if (onChangedListener != null) {
        onChangedListener.onShapeAppearanceModelChanged();
      }
    }
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
