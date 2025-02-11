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

import static androidx.core.util.Preconditions.checkNotNull;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
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
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.ImageMatrixProperty;
import com.google.android.material.animation.MatrixEvaluator;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.internal.StateListAnimator;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.ripple.RippleDrawableCompat;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shadow.ShadowViewDelegate;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
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
  private static final float HIDE_SCALE = 0.4f;
  private static final float HIDE_ICON_SCALE = 0.4f;
  private static final float SHOW_OPACITY = 1f;
  private static final float SHOW_SCALE = 1f;
  private static final float SHOW_ICON_SCALE = 1f;

  private static final float SPEC_HIDE_SCALE = 0f;
  private static final float SPEC_HIDE_ICON_SCALE = 0f;

  private static final int SHOW_ANIM_DURATION_ATTR = R.attr.motionDurationLong2;
  private static final int SHOW_ANIM_EASING_ATTR = R.attr.motionEasingEmphasizedInterpolator;
  private static final int HIDE_ANIM_DURATION_ATTR = R.attr.motionDurationMedium1;
  private static final int HIDE_ANIM_EASING_ATTR =
      R.attr.motionEasingEmphasizedAccelerateInterpolator;

  @Nullable ShapeAppearanceModel shapeAppearance;
  @Nullable MaterialShapeDrawable shapeDrawable;
  @Nullable Drawable rippleDrawable;
  @Nullable BorderDrawable borderDrawable;
  @Nullable Drawable contentBackground;

  boolean ensureMinTouchTargetSize;
  boolean shadowPaddingEnabled = true;
  float elevation;
  float hoveredFocusedTranslationZ;
  float pressedTranslationZ;
  int minTouchTargetSize;

  @NonNull private final StateListAnimator stateListAnimator;

  @Nullable private Animator currentAnimator;
  @Nullable private MotionSpec showMotionSpec;
  @Nullable private MotionSpec hideMotionSpec;

  private float rotation;
  private float imageMatrixScale = 1f;
  private int maxImageSize;
  private int animState = ANIM_STATE_NONE;

  private ArrayList<AnimatorListener> showListeners;
  private ArrayList<AnimatorListener> hideListeners;
  private ArrayList<InternalTransformationCallback> transformationCallbacks;

  interface InternalTransformationCallback {

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

  @Nullable
  private ViewTreeObserver.OnPreDrawListener preDrawListener;

  @SuppressWarnings("nullness")
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

  void initializeBackgroundDrawable(
      ColorStateList backgroundTint,
      @Nullable PorterDuff.Mode backgroundTintMode,
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
    shapeDrawable.initializeElevationOverlay(view.getContext());

    // Now we created a mask Drawable which will be used for touch feedback.
    RippleDrawableCompat touchFeedbackShape =
        new RippleDrawableCompat(shapeDrawable.getShapeAppearanceModel());
    touchFeedbackShape.setTintList(RippleUtils.sanitizeRippleDrawableColor(rippleColor));
    rippleDrawable = touchFeedbackShape;

    final Drawable[] layers = new Drawable[]{
        checkNotNull(shapeDrawable),
        touchFeedbackShape};
    contentBackground = new LayerDrawable(layers);
  }

  void setBackgroundTintList(@Nullable ColorStateList tint) {
    if (shapeDrawable != null) {
      shapeDrawable.setTintList(tint);
    }
    if (borderDrawable != null) {
      borderDrawable.setBorderTint(tint);
    }
  }

  void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (shapeDrawable != null) {
      shapeDrawable.setTintMode(tintMode);
    }
  }

  void setMinTouchTargetSize(int minTouchTargetSize) {
    this.minTouchTargetSize = minTouchTargetSize;
  }

  void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (rippleDrawable != null) {
      rippleDrawable.setTintList(RippleUtils.sanitizeRippleDrawableColor(rippleColor));
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

  private void calculateImageMatrixFromScale(float scale, @NonNull Matrix matrix) {
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

  final void setShapeAppearance(@NonNull ShapeAppearanceModel shapeAppearance) {
    this.shapeAppearance = shapeAppearance;
    if (shapeDrawable != null) {
      shapeDrawable.setShapeAppearanceModel(shapeAppearance);
    }

    if (rippleDrawable instanceof Shapeable) {
      ((Shapeable) rippleDrawable).setShapeAppearanceModel(shapeAppearance);
    }

    if (borderDrawable != null) {
      borderDrawable.setShapeAppearanceModel(shapeAppearance);
    }
  }

  @Nullable
  final ShapeAppearanceModel getShapeAppearance() {
    return shapeAppearance;
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

  final boolean shouldExpandBoundsForA11y() {
    return !ensureMinTouchTargetSize || view.getSizeDimension() >= minTouchTargetSize;
  }

  boolean getEnsureMinTouchTargetSize() {
    return ensureMinTouchTargetSize;
  }

  void setEnsureMinTouchTargetSize(boolean flag) {
    ensureMinTouchTargetSize = flag;
  }

  void setShadowPaddingEnabled(boolean shadowPaddingEnabled) {
    this.shadowPaddingEnabled = shadowPaddingEnabled;
    updatePadding();
  }

  void onElevationsChanged(
      float elevation, float hoveredFocusedTranslationZ, float pressedTranslationZ) {
    // If there is currently a state animation, we want to end the animation by jumping
    // the drawable to the current state before changing the elevation.
    jumpDrawableToCurrentState();
    updatePadding();
    updateShapeElevation(elevation);
  }

  void updateShapeElevation(float elevation) {
    if (shapeDrawable != null) {
      shapeDrawable.setElevation(elevation);
    }
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
      AnimatorSet set;
      if (hideMotionSpec != null) {
        set =
            createAnimator(
                hideMotionSpec,
                HIDE_OPACITY,
                SPEC_HIDE_SCALE,
                SPEC_HIDE_ICON_SCALE);
      } else {
        set =
            createDefaultAnimator(
                HIDE_OPACITY,
                HIDE_SCALE,
                HIDE_ICON_SCALE,
                HIDE_ANIM_DURATION_ATTR,
                HIDE_ANIM_EASING_ATTR);
      }

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

    boolean useDefaultAnimation = showMotionSpec == null;

    if (shouldAnimateVisibilityChange()) {
      if (view.getVisibility() != View.VISIBLE) {
        // If the view isn't visible currently, we'll animate it in.
        view.setAlpha(HIDE_OPACITY);
        view.setScaleY(useDefaultAnimation ? HIDE_SCALE : SPEC_HIDE_SCALE);
        view.setScaleX(useDefaultAnimation ? HIDE_SCALE : SPEC_HIDE_SCALE);
        setImageMatrixScale(useDefaultAnimation ? HIDE_ICON_SCALE : SPEC_HIDE_ICON_SCALE);
      }


      AnimatorSet set;
      if (showMotionSpec != null) {
        set =
            createAnimator(
                showMotionSpec,
                SHOW_OPACITY,
                SHOW_SCALE,
                SHOW_ICON_SCALE);
      } else {
        set =
            createDefaultAnimator(
                SHOW_OPACITY,
                SHOW_SCALE,
                SHOW_ICON_SCALE,
                SHOW_ANIM_DURATION_ATTR,
                SHOW_ANIM_EASING_ATTR);
      }

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
      view.setAlpha(SHOW_OPACITY);
      view.setScaleY(SHOW_SCALE);
      view.setScaleX(SHOW_SCALE);
      setImageMatrixScale(SHOW_ICON_SCALE);
      if (listener != null) {
        listener.onShown();
      }
    }
  }

  @NonNull
  private AnimatorSet createAnimator(
      @NonNull MotionSpec spec, float opacity, float scale, float iconScale) {
    List<Animator> animators = new ArrayList<>();

    ObjectAnimator animatorOpacity = ObjectAnimator.ofFloat(view, View.ALPHA, opacity);
    spec.getTiming("opacity").apply(animatorOpacity);
    animators.add(animatorOpacity);

    ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, scale);
    spec.getTiming("scale").apply(animatorScaleX);
    workAroundOreoBug(animatorScaleX);
    animators.add(animatorScaleX);

    ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scale);
    spec.getTiming("scale").apply(animatorScaleY);
    workAroundOreoBug(animatorScaleY);
    animators.add(animatorScaleY);

    calculateImageMatrixFromScale(iconScale, tmpMatrix);
    ObjectAnimator animatorIconScale =
        ObjectAnimator.ofObject(
            view,
            new ImageMatrixProperty(),
            new MatrixEvaluator() {
              @Override
              public Matrix evaluate(
                  float fraction, @NonNull Matrix startValue, @NonNull Matrix endValue) {
                // Also set the current imageMatrixScale fraction so it can be used to correctly
                // calculate the image matrix at any given point.
                imageMatrixScale = fraction;
                return super.evaluate(fraction, startValue, endValue);
              }
            },
            new Matrix(tmpMatrix));
    spec.getTiming("iconScale").apply(animatorIconScale);
    animators.add(animatorIconScale);

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }

  /**
   * Create an AnimatorSet when there is no motion spec specified for a show or hide animation.
   *
   * The created animation uses theme-based values for duration and easing. The benefits of this
   * default animator is that it is able to use a single, value-driven animator to make property
   * updates to the FAB. These property updates share a duration and follow the same easing curve
   * and are able to interpolate values on their own to change the progress range over which they
   * are changed.
   */
  private AnimatorSet createDefaultAnimator(
      final float targetOpacity, final float targetScale, final float targetIconScale,
      final int duration, final int interpolator) {
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();
    ValueAnimator animator = ValueAnimator.ofFloat(0F, 1F);
    final float startAlpha = view.getAlpha();
    final float startScaleX = view.getScaleX();
    final float startScaleY = view.getScaleY();
    final float startImageMatrixScale = imageMatrixScale;
    final Matrix matrix = new Matrix(tmpMatrix);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            // Animate the opacity over the first 20% of the animation
            view.setAlpha(AnimationUtils.lerp(startAlpha, targetOpacity, 0F, 0.2F, progress));
            view.setScaleX(AnimationUtils.lerp(startScaleX, targetScale, progress));
            view.setScaleY(AnimationUtils.lerp(startScaleY, targetScale, progress));
            imageMatrixScale =
                AnimationUtils.lerp(startImageMatrixScale, targetIconScale, progress);
            calculateImageMatrixFromScale(
                AnimationUtils.lerp(startImageMatrixScale, targetIconScale, progress), matrix);
            view.setImageMatrix(matrix);
          }
        });
    animators.add(animator);
    AnimatorSetCompat.playTogether(set, animators);
    set.setDuration(
        MotionUtils.resolveThemeDuration(
            view.getContext(),
            duration,
            view.getContext()
                .getResources()
                .getInteger(R.integer.material_motion_duration_long_1)));
    set.setInterpolator(
        MotionUtils.resolveThemeInterpolator(
            view.getContext(),
            interpolator,
            AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return set;
  }

  /**
   * There appears to be a bug in the OpenGL shadow rendering code on API 26. We can work around it
   * by preventing any scaling close to 0.
   */
  private void workAroundOreoBug(final ObjectAnimator animator) {
    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
      return;
    }

    animator.setEvaluator(new TypeEvaluator<Float>() {
      FloatEvaluator floatEvaluator = new FloatEvaluator();
      @Override
      public Float evaluate(float fraction, Float startValue, Float endValue) {
        float evaluated = floatEvaluator.evaluate(fraction, startValue, endValue);
        return evaluated < 0.1f ? 0.0f : evaluated;
      }
    });
  }

  void addTransformationCallback(@NonNull InternalTransformationCallback listener) {
    if (transformationCallbacks == null) {
      transformationCallbacks = new ArrayList<>();
    }
    transformationCallbacks.add(listener);
  }

  void removeTransformationCallback(@NonNull InternalTransformationCallback listener) {
    if (transformationCallbacks == null) {
      // This can happen if this method is called before the first call to
      // addTransformationCallback.
      return;
    }
    transformationCallbacks.remove(listener);
  }

  void onTranslationChanged() {
    if (transformationCallbacks != null) {
      for (InternalTransformationCallback l : transformationCallbacks) {
        l.onTranslationChanged();
      }
    }
  }

  void onScaleChanged() {
    if (transformationCallbacks != null) {
      for (InternalTransformationCallback l : transformationCallbacks) {
        l.onScaleChanged();
      }
    }
  }

  @Nullable
  final Drawable getContentBackground() {
    return contentBackground;
  }

  void onCompatShadowChanged() {
    // Ignore pre-v21
  }

  final void updatePadding() {
    Rect rect = tmpRect;
    getPadding(rect);
    onPaddingUpdated(rect);
    shadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
  }

  void getPadding(@NonNull Rect rect) {
    final int touchTargetPadding = getTouchTargetPadding();
    final float maxShadowSize = shadowPaddingEnabled ? (getElevation() + pressedTranslationZ) : 0;
    final int hPadding = Math.max(touchTargetPadding, (int) Math.ceil(maxShadowSize));
    final int vPadding = Math.max(
        touchTargetPadding, (int) Math.ceil(maxShadowSize * SHADOW_MULTIPLIER));
    rect.set(hPadding, vPadding, hPadding, vPadding);
  }

  int getTouchTargetPadding() {
    return ensureMinTouchTargetSize
        ? Math.max((minTouchTargetSize - view.getSizeDimension()) / 2, 0)
        : 0;
  }

  void onPaddingUpdated(@NonNull Rect padding) {
    Preconditions.checkNotNull(contentBackground, "Didn't initialize content background");
    if (shouldAddPadding()) {
      InsetDrawable insetDrawable = new InsetDrawable(
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
    if (shapeDrawable != null) {
      MaterialShapeUtils.setParentAbsoluteElevation(view, shapeDrawable);
    }

    if (requirePreDrawListener()) {
      view.getViewTreeObserver().addOnPreDrawListener(getOrCreatePreDrawListener());
    }
  }

  void onDetachedFromWindow() {
    ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
    if (preDrawListener != null) {
      viewTreeObserver.removeOnPreDrawListener(preDrawListener);
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

  @NonNull
  private ViewTreeObserver.OnPreDrawListener getOrCreatePreDrawListener() {
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

    return preDrawListener;
  }

  MaterialShapeDrawable createShapeDrawable() {
    ShapeAppearanceModel shapeAppearance = checkNotNull(this.shapeAppearance);
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

  @NonNull
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
    public void onAnimationUpdate(@NonNull ValueAnimator animator) {
      if (!validValues) {
        shadowSizeStart = shapeDrawable == null ? 0 : shapeDrawable.getElevation();
        shadowSizeEnd = getTargetShadowSize();
        validValues = true;
      }

      updateShapeElevation(
          (int)
              (shadowSizeStart
                  + ((shadowSizeEnd - shadowSizeStart) * animator.getAnimatedFraction())));
    }

    @Override
    public void onAnimationEnd(Animator animator) {
      updateShapeElevation((int) shadowSizeEnd);
      validValues = false;
    }

    /** Returns the shadow size we want to animate to. */
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
    return view.isLaidOut() && !view.isInEditMode();
  }

  void updateFromViewRotation() {
    // Offset any View rotation
    if (shapeDrawable != null) {
      shapeDrawable.setShadowCompatRotation((int) rotation);
    }
  }
}
