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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shadow.ShadowViewDelegate;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(21)
class FloatingActionButtonImplLollipop extends FloatingActionButtonImpl {

  FloatingActionButtonImplLollipop(
      FloatingActionButton view, ShadowViewDelegate shadowViewDelegate) {
    super(view, shadowViewDelegate);
  }

  @Override
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
      rippleContent = new LayerDrawable(
          new Drawable[]{checkNotNull(borderDrawable), checkNotNull(shapeDrawable)});
    } else {
      borderDrawable = null;
      rippleContent = shapeDrawable;
    }

    rippleDrawable =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(rippleColor), rippleContent, null);

    contentBackground = rippleDrawable;
  }

  @Override
  void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (rippleDrawable instanceof RippleDrawable) {
      ((RippleDrawable) rippleDrawable)
          .setColor(RippleUtils.sanitizeRippleDrawableColor(rippleColor));
    } else {
      super.setRippleColor(rippleColor);
    }
  }

  @Override
  void onElevationsChanged(
      final float elevation,
      final float hoveredFocusedTranslationZ,
      final float pressedTranslationZ) {

    if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
      // Animations produce NPE in version 21. Bluntly set the values instead in
      // #onDrawableStateChanged (matching the logic in the animations below).
      view.refreshDrawableState();
    } else {
      final StateListAnimator stateListAnimator = new StateListAnimator();

      // Animate elevation and translationZ to our values when pressed, focused, and hovered
      stateListAnimator.addState(
          PRESSED_ENABLED_STATE_SET, createElevationAnimator(elevation, pressedTranslationZ));
      stateListAnimator.addState(
          HOVERED_FOCUSED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));
      stateListAnimator.addState(
          FOCUSED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));
      stateListAnimator.addState(
          HOVERED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));

      // Animate translationZ to 0 if not pressed, focused, or hovered
      AnimatorSet set = new AnimatorSet();
      List<Animator> animators = new ArrayList<>();
      animators.add(ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(0));
      if (Build.VERSION.SDK_INT >= 22 && Build.VERSION.SDK_INT <= 24) {
        // This is a no-op animation which exists here only for introducing the duration
        // because setting the delay (on the next animation) via "setDelay" or "after"
        // can trigger a NPE between android versions 22 and 24 (due to a framework
        // bug). The issue has been fixed in version 25.
        animators.add(
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, view.getTranslationZ())
                .setDuration(ELEVATION_ANIM_DELAY));
      }
      animators.add(
          ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, 0f)
              .setDuration(ELEVATION_ANIM_DURATION));
      set.playSequentially(animators.toArray(new Animator[0]));
      set.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
      stateListAnimator.addState(ENABLED_STATE_SET, set);

      // Animate everything to 0 when disabled
      stateListAnimator.addState(EMPTY_STATE_SET, createElevationAnimator(0f, 0f));

      view.setStateListAnimator(stateListAnimator);
    }

    if (shouldAddPadding()) {
      updatePadding();
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

  @Override
  public float getElevation() {
    return view.getElevation();
  }

  @Override
  void onCompatShadowChanged() {
    updatePadding();
  }

  @Override
  boolean shouldAddPadding() {
    return shadowViewDelegate.isCompatPaddingEnabled() || !shouldExpandBoundsForA11y();
  }

  @Override
  void onDrawableStateChanged(int[] state) {
    if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
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
  }

  @Override
  void jumpDrawableToCurrentState() {
    // no-op
  }

  @Override
  void updateFromViewRotation() {
    // no-op
  }

  @Override
  boolean requirePreDrawListener() {
    return false;
  }

  @NonNull
  BorderDrawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
    final Context context = view.getContext();
    BorderDrawable borderDrawable =  new BorderDrawable(checkNotNull(shapeAppearance));
    borderDrawable.setGradientColors(
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_outer_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_outer_color));
    borderDrawable.setBorderWidth(borderWidth);
    borderDrawable.setBorderTint(backgroundTint);
    return borderDrawable;
  }

  @NonNull
  @Override
  MaterialShapeDrawable createShapeDrawable() {
    ShapeAppearanceModel shapeAppearance = checkNotNull(this.shapeAppearance);
    return new AlwaysStatefulMaterialShapeDrawable(shapeAppearance);
  }

  @Override
  void getPadding(@NonNull Rect rect) {
    if (shadowViewDelegate.isCompatPaddingEnabled()) {
      super.getPadding(rect);
    } else if (!shouldExpandBoundsForA11y()) {
      int minPadding = (minTouchTargetSize - view.getSizeDimension()) / 2;
      rect.set(minPadding, minPadding, minPadding, minPadding);
    } else {
      rect.set(0, 0, 0, 0);
    }
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
