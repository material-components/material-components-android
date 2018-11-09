/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.floatingactionbutton;

import com.google.android.material.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.ImageMatrixProperty;
import com.google.android.material.animation.MatrixEvaluator;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.internal.CircularBorderDrawable;
import com.google.android.material.internal.StateListAnimator;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shadow.ShadowViewDelegate;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import java.util.ArrayList;
import java.util.List;

class FloatingActionButtonImpl {
  static final TimeInterpolator ELEVATION_ANIM_INTERPOLATOR =
      AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;
  static final long ELEVATION_ANIM_DURATION = 100;
  static final long ELEVATION_ANIM_DELAY = 100;

  static final int ANIM_STATE_NONE = 0;
  static final int ANIM_STATE_HIDING = 1;
  static final int ANIM_STATE_SHOWING = 2;
  static final float SHADOW_MULTIPLIER = 1.5f;

  private static final float HIDE_OPACITY = 0f;
  private static final float HIDE_SCALE = 0f;
  private static final float HIDE_ICON_SCALE = 0f;
  private static final float SHOW_OPACITY = 1f;
  private static final float SHOW_SCALE = 1f;
  private static final float SHOW_ICON_SCALE = 1f;
  private static final float ELEVATION_MULTIPLIER = .75f;
  private static final float OFFSET_MULTIPLIER = .25f;

  int animState = ANIM_STATE_NONE;
  @Nullable Animator currentAnimator;
  @Nullable MotionSpec showMotionSpec;
  @Nullable MotionSpec hideMotionSpec;
  @Nullable ShapeAppearanceModel shapeAppearance;
  boolean usingDefaultCorner;

  @Nullable private MotionSpec defaultShowMotionSpec;
  @Nullable private MotionSpec defaultHideMotionSpec;

  private final StateListAnimator stateListAnimator;
  private float rotation;
  private InsetDrawable insetDrawable;

  MaterialShapeDrawable shapeDrawable;
  Drawable rippleDrawable;
  CircularBorderDrawable borderDrawable;
  Drawable contentBackground;

  float elevation;
  float hoveredFocusedTranslationZ;
  float pressedTranslationZ;
  int minTouchTargetSize;

  int maxImageSize;
  float imageMatrixScale = 1f;

  private ArrayList<AnimatorListener> showListeners;
  private ArrayList<AnimatorListener> hideListeners;
  private ArrayList<InternalTransformationListener> transformationListeners;

  interface InternalTransformationListener {
    void onTranslationChanged();

    void onScaleChanged();
  }

  interface InternalVisibilityChangedListener {
    void onShown();

    void onHidden();
  }

  static final int[] PRESSED_ENABLED_STATE_SET = {
    android.R.attr.state_pressed, android.R.attr.state_enabled
  };
  static final int[] HOVERED_FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_focused, android.R.attr.state_enabled
  };
  static final int[] FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_focused, android.R.attr.state_enabled
  };
  static final int[] HOVERED_ENABLED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_enabled
  };
  static final int[] ENABLED_STATE_SET = {android.R.attr.state_enabled};
  static final int[] EMPTY_STATE_SET = new int[0];

  final FloatingActionButton view;
  final ShadowViewDelegate shadowViewDelegate;

  private final Rect tmpRect = new Rect();
  private final RectF tmpRectF1 = new RectF();
  private final RectF tmpRectF2 = new RectF();
  private final Matrix tmpMatrix = new Matrix();

  private ViewTreeObserver.OnPreDrawListener preDrawListener;

  FloatingActionButtonImpl(FloatingActionButton view, ShadowViewDelegate shadowViewDelegate) {
    this.view = view;
    this.shadowViewDelegate = shadowViewDelegate;

    stateListAnimator = new StateListAnimator();

    // Elevate with translationZ when pressed, focused, or hovered
    stateListAnimator.addState(
        PRESSED_ENABLED_STATE_SET,
        createElevationAnimator(new ElevateToPressedTranslationZAnimation()));
    stateListAnimator.addState(
        HOVERED_FOCUSED_ENABLED_STATE_SET,
        createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
    stateListAnimator.addState(
        FOCUSED_ENABLED_STATE_SET,
        createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
    stateListAnimator.addState(
        HOVERED_ENABLED_STATE_SET,
        createElevationAnimator(new ElevateToHoveredFocusedTranslationZAnimation()));
    // Reset back to elevation by default
    stateListAnimator.addState(
        ENABLED_STATE_SET, createElevationAnimator(new ResetElevationAnimation()));
    // Set to 0 when disabled
    stateListAnimator.addState(
        EMPTY_STATE_SET, createElevationAnimator(new DisabledElevationAnimation()));

    rotation = this.view.getRotation();
  }

  void setBackgroundDrawable(
      ColorStateList backgroundTint,
      PorterDuff.Mode backgroundTintMode,
      ColorStateList rippleColor,
      int borderWidth) {
    // Now we need to tint the original background with the tint, using
    // an InsetDrawable if we have a border width
    shapeDrawable = createShapeDrawable();
    shapeDrawable.setTintList(backgroundTint);
    if (backgroundTintMode != null) {
      shapeDrawable.setTintMode(backgroundTintMode);
    }

    shapeDrawable.setShadowColor(Color.DKGRAY);

    // Now we created a mask Drawable which will be used for touch feedback.
    MaterialShapeDrawable touchFeedbackShape = createShapeDrawable();
    touchFeedbackShape.setTintList(RippleUtils.convertToRippleDrawableColor(rippleColor));
    rippleDrawable = touchFeedbackShape;

    final Drawable[] layers = new Drawable[] {shapeDrawable, rippleDrawable};
    contentBackground = new LayerDrawable(layers);
    shadowViewDelegate.setBackgroundDrawable(contentBackground);
  }

  void setBackgroundTintList(ColorStateList tint) {
    if (shapeDrawable != null) {
      shapeDrawable.setTintList(tint);
    }
    if (borderDrawable != null) {
      borderDrawable.setBorderTint(tint);
    }
  }

  void setBackgroundTintMode(PorterDuff.Mode tintMode) {
    if (shapeDrawable != null) {
      DrawableCompat.setTintMode(shapeDrawable, tintMode);
    }
  }

  void setMinTouchTargetSize(int minTouchTargetSize) {
    this.minTouchTargetSize = minTouchTargetSize;
  }

  void setRippleColor(ColorStateList rippleColor) {
    if (rippleDrawable != null) {
      DrawableCompat.setTintList(
          rippleDrawable, RippleUtils.convertToRippleDrawableColor(rippleColor));
    }
  }

  final void setElevation(float elevation) {
    if (this.elevation != elevation) {
      this.elevation = elevation;
      onElevationsChanged(this.elevation, hoveredFocusedTranslationZ, pressedTranslationZ);
    }
  }

  float getElevation() {
    return elevation;
  }

  float getHoveredFocusedTranslationZ() {
    return hoveredFocusedTranslationZ;
  }

  float getPressedTranslationZ() {
    return pressedTranslationZ;
  }

  final void setHoveredFocusedTranslationZ(float translationZ) {
    if (hoveredFocusedTranslationZ != translationZ) {
      hoveredFocusedTranslationZ = translationZ;
      onElevationsChanged(elevation, hoveredFocusedTranslationZ, pressedTranslationZ);
    }
  }

  final void setPressedTranslationZ(float translationZ) {
    if (pressedTranslationZ != translationZ) {
      pressedTranslationZ = translationZ;
      onElevationsChanged(elevation, hoveredFocusedTranslationZ, pressedTranslationZ);
    }
  }

  final void setMaxImageSize(int maxImageSize) {
    if (this.maxImageSize != maxImageSize) {
      this.maxImageSize = maxImageSize;
      updateImageMatrixScale();
    }
  }

  /** Call this whenever the image drawable changes or the view size changes. */
  final void updateImageMatrixScale() {
    // Recompute the image matrix needed to maintain the same scale.
    setImageMatrixScale(imageMatrixScale);
  }

  final void setImageMatrixScale(float scale) {
    this.imageMatrixScale = scale;

    Matrix matrix = tmpMatrix;
    calculateImageMatrixFromScale(scale, matrix);
    view.setImageMatrix(matrix);
  }

  private void calculateImageMatrixFromScale(float scale, Matrix matrix) {
    matrix.reset();

    Drawable drawable = view.getDrawable();
    if (drawable != null && maxImageSize != 0) {
      // First make sure our image respects mMaxImageSize.
      RectF drawableBounds = tmpRectF1;
      RectF imageBounds = tmpRectF2;
      drawableBounds.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
      imageBounds.set(0, 0, maxImageSize, maxImageSize);
      matrix.setRectToRect(drawableBounds, imageBounds, ScaleToFit.CENTER);

      // Then scale it as requested.
      matrix.postScale(scale, scale, maxImageSize / 2f, maxImageSize / 2f);
    }
  }

  final void setShapeAppearance(ShapeAppearanceModel shapeAppearance, boolean usingDefaultCorner) {
    this.shapeAppearance = shapeAppearance;
    this.usingDefaultCorner = usingDefaultCorner;
  }

  @Nullable
  final MotionSpec getShowMotionSpec() {
    return showMotionSpec;
  }

  final void setShowMotionSpec(@Nullable MotionSpec spec) {
    showMotionSpec = spec;
  }

  @Nullable
  final MotionSpec getHideMotionSpec() {
    return hideMotionSpec;
  }

  final void setHideMotionSpec(@Nullable MotionSpec spec) {
    hideMotionSpec = spec;
  }

  final boolean isAccessible() {
    return view.getSizeDimension() >= minTouchTargetSize;
  }

  void onElevationsChanged(
      float elevation, float hoveredFocusedTranslationZ, float pressedTranslationZ) {
    updatePadding();
    updateShadow(elevation);
  }

  private void updateShadow(float elevation) {
    // TODO material shape drawable should handle this calculations.
    shapeDrawable.setShadowElevation((int) Math.ceil((elevation * ELEVATION_MULTIPLIER)));
    shapeDrawable.setShadowVerticalOffset((int) Math.ceil((elevation * OFFSET_MULTIPLIER)));
  }

  void onDrawableStateChanged(int[] state) {
    stateListAnimator.setState(state);
  }

  void jumpDrawableToCurrentState() {
    stateListAnimator.jumpToCurrentState();
  }

  void addOnShowAnimationListener(@NonNull AnimatorListener listener) {
    if (showListeners == null) {
      showListeners = new ArrayList<>();
    }
    showListeners.add(listener);
  }

  void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    if (showListeners == null) {
      // This can happen if this method is called before the first call to
      // addOnShowAnimationListener.
      return;
    }
    showListeners.remove(listener);
  }

  public void addOnHideAnimationListener(@NonNull AnimatorListener listener) {
    if (hideListeners == null) {
      hideListeners = new ArrayList<>();
    }
    hideListeners.add(listener);
  }

  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    if (hideListeners == null) {
      // This can happen if this method is called before the first call to
      // addOnHideAnimationListener.
      return;
    }
    hideListeners.remove(listener);
  }

  void hide(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
    if (isOrWillBeHidden()) {
      // We either are or will soon be hidden, skip the call
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
    }

    if (shouldAnimateVisibilityChange()) {
      AnimatorSet set =
          createAnimator(
              hideMotionSpec != null ? hideMotionSpec : getDefaultHideMotionSpec(),
              HIDE_OPACITY,
              HIDE_SCALE,
              HIDE_ICON_SCALE);
      set.addListener(
          new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
              view.internalSetVisibility(View.VISIBLE, fromUser);

              animState = ANIM_STATE_HIDING;
              currentAnimator = animation;
              cancelled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
              cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              animState = ANIM_STATE_NONE;
              currentAnimator = null;

              if (!cancelled) {
                view.internalSetVisibility(fromUser ? View.GONE : View.INVISIBLE, fromUser);
                if (listener != null) {
                  listener.onHidden();
                }
              }
            }
          });
      if (hideListeners != null) {
        for (AnimatorListener l : hideListeners) {
          set.addListener(l);
        }
      }
      set.start();
    } else {
      // If the view isn't laid out, or we're in the editor, don't run the animation
      view.internalSetVisibility(fromUser ? View.GONE : View.INVISIBLE, fromUser);
      if (listener != null) {
        listener.onHidden();
      }
    }
  }

  void show(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
    if (isOrWillBeShown()) {
      // We either are or will soon be visible, skip the call
      return;
    }

    if (currentAnimator != null) {
      currentAnimator.cancel();
    }

    if (shouldAnimateVisibilityChange()) {
      if (view.getVisibility() != View.VISIBLE) {
        // If the view isn't visible currently, we'll animate it from a single pixel
        view.setAlpha(0f);
        view.setScaleY(0f);
        view.setScaleX(0f);
        setImageMatrixScale(0f);
      }

      AnimatorSet set =
          createAnimator(
              showMotionSpec != null ? showMotionSpec : getDefaultShowMotionSpec(),
              SHOW_OPACITY,
              SHOW_SCALE,
              SHOW_ICON_SCALE);
      set.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
              view.internalSetVisibility(View.VISIBLE, fromUser);

              animState = ANIM_STATE_SHOWING;
              currentAnimator = animation;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              animState = ANIM_STATE_NONE;
              currentAnimator = null;

              if (listener != null) {
                listener.onShown();
              }
            }
          });
      if (showListeners != null) {
        for (AnimatorListener l : showListeners) {
          set.addListener(l);
        }
      }
      set.start();
    } else {
      view.internalSetVisibility(View.VISIBLE, fromUser);
      view.setAlpha(1f);
      view.setScaleY(1f);
      view.setScaleX(1f);
      setImageMatrixScale(1f);
      if (listener != null) {
        listener.onShown();
      }
    }
  }

  private MotionSpec getDefaultShowMotionSpec() {
    if (defaultShowMotionSpec == null) {
      defaultShowMotionSpec =
          MotionSpec.createFromResource(view.getContext(), R.animator.design_fab_show_motion_spec);
    }
    return defaultShowMotionSpec;
  }

  private MotionSpec getDefaultHideMotionSpec() {
    if (defaultHideMotionSpec == null) {
      defaultHideMotionSpec =
          MotionSpec.createFromResource(view.getContext(), R.animator.design_fab_hide_motion_spec);
    }
    return defaultHideMotionSpec;
  }

  @NonNull
  private AnimatorSet createAnimator(
      @NonNull MotionSpec spec, float opacity, float scale, float iconScale) {
    List<Animator> animators = new ArrayList<>();
    Animator animator;

    animator = ObjectAnimator.ofFloat(view, View.ALPHA, opacity);
    spec.getTiming("opacity").apply(animator);
    animators.add(animator);

    animator = ObjectAnimator.ofFloat(view, View.SCALE_X, scale);
    spec.getTiming("scale").apply(animator);
    animators.add(animator);

    animator = ObjectAnimator.ofFloat(view, View.SCALE_Y, scale);
    spec.getTiming("scale").apply(animator);
    animators.add(animator);

    calculateImageMatrixFromScale(iconScale, tmpMatrix);
    animator =
        ObjectAnimator.ofObject(
            view,
            new ImageMatrixProperty(),
            new MatrixEvaluator() {
              @Override
              public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
                // Also set the current imageMatrixScale fraction so it can be used to correctly
                // calculate the image matrix at any given point.
                imageMatrixScale = fraction;
                return super.evaluate(fraction, startValue, endValue);
              }
            },
            new Matrix(tmpMatrix));
    spec.getTiming("iconScale").apply(animator);
    animators.add(animator);

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }

  void addTransformationListener(@NonNull InternalTransformationListener listener) {
    if (transformationListeners == null) {
      transformationListeners = new ArrayList<>();
    }
    transformationListeners.add(listener);
  }

  void removeTransformationListener(@NonNull InternalTransformationListener listener) {
    if (transformationListeners == null) {
      // This can happen if this method is called before the first call to
      // addTransformationListener.
      return;
    }
    transformationListeners.remove(listener);
  }

  void onTranslationChanged() {
    if (transformationListeners != null) {
      for (InternalTransformationListener l : transformationListeners) {
        l.onTranslationChanged();
      }
    }
  }

  void onScaleChanged() {
    if (transformationListeners != null) {
      for (InternalTransformationListener l : transformationListeners) {
        l.onScaleChanged();
      }
    }
  }

  final Drawable getContentBackground() {
    return contentBackground;
  }

  void onCompatShadowChanged() {
    // Ignore pre-v21
  }

  void updateSize() {
    if (!usingDefaultCorner) {
      // Leave shape appearance as is.
      return;
    }

    ShapeAppearanceModel shapeAppearanceModel = shapeDrawable.getShapeAppearanceModel();
    shapeAppearanceModel.setCornerRadius(view.getSizeDimension() / 2);
  }

  final void updatePadding() {
    Rect rect = tmpRect;
    getPadding(rect);
    onPaddingUpdated(rect);
    shadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
  }

  void getPadding(Rect rect) {
    final int minPadding = (minTouchTargetSize - view.getSizeDimension()) / 2;
    final float maxShadowSize = (getElevation() + pressedTranslationZ);
    final int hPadding = Math.max(minPadding, (int) Math.ceil(maxShadowSize));
    final int vPadding = Math.max(minPadding, (int) Math.ceil(maxShadowSize * SHADOW_MULTIPLIER));
    rect.set(hPadding, vPadding, hPadding, vPadding);
  }

  void onPaddingUpdated(Rect padding) {
    if (shouldAddPadding()) {
      insetDrawable =
          new InsetDrawable(
              contentBackground, padding.left, padding.top, padding.right, padding.bottom);
      shadowViewDelegate.setBackgroundDrawable(insetDrawable);
    } else {
      shadowViewDelegate.setBackgroundDrawable(contentBackground);
    }
  }

  boolean shouldAddPadding() {
    return true;
  }

  void onAttachedToWindow() {
    if (requirePreDrawListener()) {
      ensurePreDrawListener();
      view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
    }
  }

  void onDetachedFromWindow() {
    if (preDrawListener != null) {
      view.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
      preDrawListener = null;
    }
  }

  boolean requirePreDrawListener() {
    return true;
  }

  void onPreDraw() {
    final float rotation = view.getRotation();
    if (this.rotation != rotation) {
      this.rotation = rotation;
      updateFromViewRotation();
    }
  }

  private void ensurePreDrawListener() {
    if (preDrawListener == null) {
      preDrawListener =
          new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
              FloatingActionButtonImpl.this.onPreDraw();
              return true;
            }
          };
    }
  }

  MaterialShapeDrawable createShapeDrawable() {
    if (usingDefaultCorner) {
      shapeAppearance.setCornerRadius(view.getSizeDimension() / 2);
    }
    return new MaterialShapeDrawable(shapeAppearance);
  }

  boolean isOrWillBeShown() {
    if (view.getVisibility() != View.VISIBLE) {
      // If we not currently visible, return true if we're animating to be shown
      return animState == ANIM_STATE_SHOWING;
    } else {
      // Otherwise if we're visible, return true if we're not animating to be hidden
      return animState != ANIM_STATE_HIDING;
    }
  }

  boolean isOrWillBeHidden() {
    if (view.getVisibility() == View.VISIBLE) {
      // If we currently visible, return true if we're animating to be hidden
      return animState == ANIM_STATE_HIDING;
    } else {
      // Otherwise if we're not visible, return true if we're not animating to be shown
      return animState != ANIM_STATE_SHOWING;
    }
  }

  private ValueAnimator createElevationAnimator(@NonNull ShadowAnimatorImpl impl) {
    final ValueAnimator animator = new ValueAnimator();
    animator.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
    animator.setDuration(ELEVATION_ANIM_DURATION);
    animator.addListener(impl);
    animator.addUpdateListener(impl);
    animator.setFloatValues(0, 1);
    return animator;
  }

  private abstract class ShadowAnimatorImpl extends AnimatorListenerAdapter
      implements ValueAnimator.AnimatorUpdateListener {
    private boolean validValues;
    private float shadowSizeStart;
    private float shadowSizeEnd;

    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
      if (!validValues) {
        shadowSizeStart = shapeDrawable.getShadowElevation();
        shadowSizeEnd = getTargetShadowSize();
        validValues = true;
      }

      updateShadow((int)
          (shadowSizeStart
              + ((shadowSizeEnd - shadowSizeStart) * animator.getAnimatedFraction())));
    }

    @Override
    public void onAnimationEnd(Animator animator) {
      updateShadow((int) shadowSizeEnd);
      validValues = false;
    }

    /** @return the shadow size we want to animate to. */
    protected abstract float getTargetShadowSize();
  }

  private class ResetElevationAnimation extends ShadowAnimatorImpl {
    ResetElevationAnimation() {}

    @Override
    protected float getTargetShadowSize() {
      return elevation;
    }
  }

  private class ElevateToHoveredFocusedTranslationZAnimation extends ShadowAnimatorImpl {
    ElevateToHoveredFocusedTranslationZAnimation() {}

    @Override
    protected float getTargetShadowSize() {
      return elevation + hoveredFocusedTranslationZ;
    }
  }

  private class ElevateToPressedTranslationZAnimation extends ShadowAnimatorImpl {
    ElevateToPressedTranslationZAnimation() {}

    @Override
    protected float getTargetShadowSize() {
      return elevation + pressedTranslationZ;
    }
  }

  private class DisabledElevationAnimation extends ShadowAnimatorImpl {
    DisabledElevationAnimation() {}

    @Override
    protected float getTargetShadowSize() {
      return 0f;
    }
  }

  private boolean shouldAnimateVisibilityChange() {
    return ViewCompat.isLaidOut(view) && !view.isInEditMode();
  }

  void updateFromViewRotation() {
    if (Build.VERSION.SDK_INT == 19) {
      // KitKat seems to have an issue with views which are rotated with angles which are
      // not divisible by 90. Worked around by moving to software rendering in these cases.
      if ((rotation % 90) != 0) {
        if (view.getLayerType() != View.LAYER_TYPE_SOFTWARE) {
          view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
      } else {
        if (view.getLayerType() != View.LAYER_TYPE_NONE) {
          view.setLayerType(View.LAYER_TYPE_NONE, null);
        }
      }
    }

    // Offset any View rotation
    if (shapeDrawable != null) {
      shapeDrawable.setShadowCompatRotation((int) rotation);
    }
    if (borderDrawable != null) {
      borderDrawable.setRotation(-rotation);
    }
  }
}
