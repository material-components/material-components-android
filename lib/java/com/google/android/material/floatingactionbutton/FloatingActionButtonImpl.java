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
import static java.lang.Math.max;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Preconditions;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.ImageMatrixProperty;
import com.google.android.material.animation.MatrixEvaluator;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.motion.MotionUtils;
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

  @Nullable private StateListAnimator stateListAnimator;

  @Nullable private Animator currentAnimator;
  @Nullable private MotionSpec showMotionSpec;
  @Nullable private MotionSpec hideMotionSpec;

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

  @Nullable private ViewTreeObserver.OnPreDrawListener preDrawListener;

  @SuppressWarnings("nullness")
  FloatingActionButtonImpl(FloatingActionButton view, ShadowViewDelegate shadowViewDelegate) {
    this.view = view;
    this.shadowViewDelegate = shadowViewDelegate;
  }

  void initializeBackgroundDrawable(
      ColorStateList backgroundTint,
      @Nullable PorterDuff.Mode backgroundTintMode,
      ColorStateList rippleColor,
      int borderWidth) {
    // Now we need to tint the shape background with the tint
    shapeDrawable = createShapeDrawable();
    shapeDrawable.setTintList(backgroundTint);
    if (backgroundTintMode != null) {
      shapeDrawable.setTintMode(backgroundTintMode);
    }
    shapeDrawable.initializeElevationOverlay(view.getContext());

    final Drawable rippleContent;
    if (borderWidth > 0) {
      borderDrawable = createBorderDrawable(borderWidth, backgroundTint);
      rippleContent =
          new LayerDrawable(
              new Drawable[] {checkNotNull(borderDrawable), checkNotNull(shapeDrawable)});
    } else {
      borderDrawable = null;
      rippleContent = shapeDrawable;
    }

    rippleDrawable =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(rippleColor), rippleContent, null);

    contentBackground = rippleDrawable;
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
    if (rippleDrawable instanceof RippleDrawable) {
      ((RippleDrawable) rippleDrawable).setColor(
          RippleUtils.sanitizeRippleDrawableColor(rippleColor));
    } else if (rippleDrawable != null) {
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
    return view.getElevation();
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

  final boolean ignoreExpandBoundsForA11y() {
    return ensureMinTouchTargetSize && view.getSizeDimension() < minTouchTargetSize;
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
    if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
      // Animations produce NPE in version 21. Bluntly set the values instead in
      // #onDrawableStateChanged (matching the logic in the animations below).
      view.refreshDrawableState();
    } else if (view.getStateListAnimator() == stateListAnimator) {
      // FAB is using the default StateListAnimator created here. Updates it with the new elevation.
      stateListAnimator =
          createDefaultStateListAnimator(
              elevation, hoveredFocusedTranslationZ, pressedTranslationZ);
      view.setStateListAnimator(stateListAnimator);
    }

    if (shouldAddPadding()) {
      updatePadding();
    }
  }

  @NonNull
  private StateListAnimator createDefaultStateListAnimator(
      final float elevation,
      final float hoveredFocusedTranslationZ,
      final float pressedTranslationZ) {
    StateListAnimator stateListAnimator = new StateListAnimator();

    // Animate elevation and translationZ to our values when pressed, focused, and hovered
    stateListAnimator.addState(
        PRESSED_ENABLED_STATE_SET, createElevationAnimator(elevation, pressedTranslationZ));
    stateListAnimator.addState(
        HOVERED_FOCUSED_ENABLED_STATE_SET,
        createElevationAnimator(elevation, hoveredFocusedTranslationZ));
    stateListAnimator.addState(
        FOCUSED_ENABLED_STATE_SET, createElevationAnimator(elevation, hoveredFocusedTranslationZ));
    stateListAnimator.addState(
        HOVERED_ENABLED_STATE_SET, createElevationAnimator(elevation, hoveredFocusedTranslationZ));

    // Animate translationZ to 0 if not pressed, focused, or hovered
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();
    animators.add(ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(0));
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1
        && Build.VERSION.SDK_INT <= VERSION_CODES.N) {
      // This is a no-op animation which exists here only for introducing the duration
      // because setting the delay (on the next animation) via "setDelay" or "after"
      // can trigger a NPE between android versions 22 and 24 (due to a framework
      // bug). The issue has been fixed in version 25.
      animators.add(
          ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, view.getTranslationZ())
              .setDuration(ELEVATION_ANIM_DELAY));
    }
    animators.add(
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, 0f).setDuration(ELEVATION_ANIM_DURATION));
    set.playSequentially(animators.toArray(new Animator[0]));
    set.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
    stateListAnimator.addState(ENABLED_STATE_SET, set);

    // Animate everything to 0 when disabled
    stateListAnimator.addState(EMPTY_STATE_SET, createElevationAnimator(0f, 0f));

    return stateListAnimator;
  }

  void updateShapeElevation(float elevation) {
    if (shapeDrawable != null) {
      shapeDrawable.setElevation(elevation);
    }
  }

  void onDrawableStateChangedForLollipop() {
    if (view.isEnabled()) {
      view.setElevation(elevation);
      if (view.isPressed()) {
        view.setTranslationZ(pressedTranslationZ);
      } else if (view.isFocused() || view.isHovered()) {
        view.setTranslationZ(hoveredFocusedTranslationZ);
      } else {
        view.setTranslationZ(0);
      }
    } else {
      view.setElevation(0);
      view.setTranslationZ(0);
    }
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
        set = createAnimator(hideMotionSpec, HIDE_OPACITY, SPEC_HIDE_SCALE, SPEC_HIDE_ICON_SCALE);
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
        set = createAnimator(showMotionSpec, SHOW_OPACITY, SHOW_SCALE, SHOW_ICON_SCALE);
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
   * <p>The created animation uses theme-based values for duration and easing. The benefits of this
   * default animator is that it is able to use a single, value-driven animator to make property
   * updates to the FAB. These property updates share a duration and follow the same easing curve
   * and are able to interpolate values on their own to change the progress range over which they
   * are changed.
   */
  private AnimatorSet createDefaultAnimator(
      final float targetOpacity,
      final float targetScale,
      final float targetIconScale,
      final int duration,
      final int interpolator) {
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();
    ValueAnimator animator = ValueAnimator.ofFloat(0F, 1F);
    final float startAlpha = view.getAlpha();
    final float startScaleX = view.getScaleX();
    final float startScaleY = view.getScaleY();
    final float startImageMatrixScale = imageMatrixScale;
    final Matrix matrix = new Matrix(tmpMatrix);
    animator.addUpdateListener(
        animation -> {
          float progress = (float) animation.getAnimatedValue();
          // Animate the opacity over the first 20% of the animation
          view.setAlpha(AnimationUtils.lerp(startAlpha, targetOpacity, 0F, 0.2F, progress));
          view.setScaleX(AnimationUtils.lerp(startScaleX, targetScale, progress));
          view.setScaleY(AnimationUtils.lerp(startScaleY, targetScale, progress));
          imageMatrixScale = AnimationUtils.lerp(startImageMatrixScale, targetIconScale, progress);
          calculateImageMatrixFromScale(
              AnimationUtils.lerp(startImageMatrixScale, targetIconScale, progress), matrix);
          view.setImageMatrix(matrix);
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
            view.getContext(), interpolator, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
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

    animator.setEvaluator(
        new TypeEvaluator<Float>() {
          final FloatEvaluator floatEvaluator = new FloatEvaluator();

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
    updatePadding();
  }

  final void updatePadding() {
    Rect rect = tmpRect;
    getPadding(rect);
    onPaddingUpdated(rect);
    shadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
  }

  void getPadding(@NonNull Rect rect) {
    if (shadowViewDelegate.isCompatPaddingEnabled()) {
      final int touchTargetPadding = getTouchTargetPadding();
      final float maxShadowSize = shadowPaddingEnabled ? (getElevation() + pressedTranslationZ) : 0;
      final int hPadding = max(touchTargetPadding, (int) Math.ceil(maxShadowSize));
      final int vPadding =
          max(touchTargetPadding, (int) Math.ceil(maxShadowSize * SHADOW_MULTIPLIER));
      rect.set(hPadding, vPadding, hPadding, vPadding);
    } else if (ignoreExpandBoundsForA11y()) {
      int minPadding = (minTouchTargetSize - view.getSizeDimension()) / 2;
      rect.set(minPadding, minPadding, minPadding, minPadding);
    } else {
      rect.set(0, 0, 0, 0);
    }
  }

  int getTouchTargetPadding() {
    return ensureMinTouchTargetSize
        ? max((minTouchTargetSize - view.getSizeDimension()) / 2, 0)
        : 0;
  }

  void onPaddingUpdated(@NonNull Rect padding) {
    Preconditions.checkNotNull(contentBackground, "Didn't initialize content background");
    if (shouldAddPadding()) {
      InsetDrawable insetDrawable =
          new InsetDrawable(
              contentBackground, padding.left, padding.top, padding.right, padding.bottom);
      shadowViewDelegate.setBackgroundDrawable(insetDrawable);
    } else {
      shadowViewDelegate.setBackgroundDrawable(contentBackground);
    }
  }

  boolean shouldAddPadding() {
    return shadowViewDelegate.isCompatPaddingEnabled() || ignoreExpandBoundsForA11y();
  }

  void onAttachedToWindow() {
    if (shapeDrawable != null) {
      MaterialShapeUtils.setParentAbsoluteElevation(view, shapeDrawable);
    }
  }

  void onDetachedFromWindow() {
    ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
    if (preDrawListener != null) {
      viewTreeObserver.removeOnPreDrawListener(preDrawListener);
      preDrawListener = null;
    }
  }

  @NonNull
  BorderDrawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
    final Context context = view.getContext();
    BorderDrawable borderDrawable = new BorderDrawable(checkNotNull(shapeAppearance));
    borderDrawable.setGradientColors(
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_outer_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_outer_color));
    borderDrawable.setBorderWidth(borderWidth);
    borderDrawable.setBorderTint(backgroundTint);
    return borderDrawable;
  }

  MaterialShapeDrawable createShapeDrawable() {
    ShapeAppearanceModel shapeAppearance = checkNotNull(this.shapeAppearance);
    return new AlwaysStatefulMaterialShapeDrawable(shapeAppearance);
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
  private Animator createElevationAnimator(float elevation, float translationZ) {
    AnimatorSet set = new AnimatorSet();
    set.play(ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(0))
        .with(
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, translationZ)
                .setDuration(ELEVATION_ANIM_DURATION));
    set.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
    return set;
  }

  private boolean shouldAnimateVisibilityChange() {
    return view.isLaidOut() && !view.isInEditMode();
  }

  /**
   * LayerDrawable on L+ caches its isStateful() state and doesn't refresh it, meaning that if we
   * apply a tint to one of its children, the parent doesn't become stateful and the tint doesn't
   * work for state changes. We workaround it by saying that we are always stateful. If we don't
   * have a stateful tint, the change is ignored anyway.
   */
  static class AlwaysStatefulMaterialShapeDrawable extends MaterialShapeDrawable {

    AlwaysStatefulMaterialShapeDrawable(ShapeAppearanceModel shapeAppearanceModel) {
      super(shapeAppearanceModel);
    }

    @Override
    public boolean isStateful() {
      return true;
    }
  }
}
