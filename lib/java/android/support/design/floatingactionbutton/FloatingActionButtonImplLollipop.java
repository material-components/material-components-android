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

package android.support.design.floatingactionbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.CircularBorderDrawable;
import android.support.design.internal.CircularBorderDrawableLollipop;
import android.support.design.internal.VisibilityAwareImageButton;
import android.support.design.ripple.RippleUtils;
import android.support.design.shadow.ShadowDrawableWrapper;
import android.support.design.shadow.ShadowViewDelegate;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(21)
class FloatingActionButtonImplLollipop extends FloatingActionButtonImpl {

  private InsetDrawable insetDrawable;

  FloatingActionButtonImplLollipop(
      VisibilityAwareImageButton view, ShadowViewDelegate shadowViewDelegate) {
    super(view, shadowViewDelegate);
  }

  @Override
  void setBackgroundDrawable(
      ColorStateList backgroundTint,
      PorterDuff.Mode backgroundTintMode,
      ColorStateList rippleColor,
      int borderWidth) {
    // Now we need to tint the shape background with the tint
    shapeDrawable = DrawableCompat.wrap(createShapeDrawable());
    DrawableCompat.setTintList(shapeDrawable, backgroundTint);
    if (backgroundTintMode != null) {
      DrawableCompat.setTintMode(shapeDrawable, backgroundTintMode);
    }

    final Drawable rippleContent;
    if (borderWidth > 0) {
      borderDrawable = createBorderDrawable(borderWidth, backgroundTint);
      rippleContent = new LayerDrawable(new Drawable[] {borderDrawable, shapeDrawable});
    } else {
      borderDrawable = null;
      rippleContent = shapeDrawable;
    }

    rippleDrawable =
        new RippleDrawable(
            RippleUtils.convertToRippleDrawableColor(rippleColor), rippleContent, null);

    contentBackground = rippleDrawable;

    shadowViewDelegate.setBackgroundDrawable(rippleDrawable);
  }

  @Override
  void setRippleColor(ColorStateList rippleColor) {
    if (rippleDrawable instanceof RippleDrawable) {
      ((RippleDrawable) rippleDrawable)
          .setColor(RippleUtils.convertToRippleDrawableColor(rippleColor));
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

    if (shadowViewDelegate.isCompatPaddingEnabled()) {
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
  void onPaddingUpdated(Rect padding) {
    if (shadowViewDelegate.isCompatPaddingEnabled()) {
      insetDrawable =
          new InsetDrawable(
              rippleDrawable, padding.left, padding.top, padding.right, padding.bottom);
      shadowViewDelegate.setBackgroundDrawable(insetDrawable);
    } else {
      shadowViewDelegate.setBackgroundDrawable(rippleDrawable);
    }
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
    ;
  }

  @Override
  void jumpDrawableToCurrentState() {
    // no-op
  }

  @Override
  boolean requirePreDrawListener() {
    return false;
  }

  @Override
  CircularBorderDrawable newCircularDrawable() {
    return new CircularBorderDrawableLollipop();
  }

  @Override
  GradientDrawable newGradientDrawableForShape() {
    return new AlwaysStatefulGradientDrawable();
  }

  @Override
  void getPadding(Rect rect) {
    if (shadowViewDelegate.isCompatPaddingEnabled()) {
      final float radius = shadowViewDelegate.getRadius();
      final float maxShadowSize = getElevation() + pressedTranslationZ;
      final int hPadding =
          (int)
              Math.ceil(
                  ShadowDrawableWrapper.calculateHorizontalPadding(maxShadowSize, radius, false));
      final int vPadding =
          (int)
              Math.ceil(
                  ShadowDrawableWrapper.calculateVerticalPadding(maxShadowSize, radius, false));
      rect.set(hPadding, vPadding, hPadding, vPadding);
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
  static class AlwaysStatefulGradientDrawable extends GradientDrawable {
    @Override
    public boolean isStateful() {
      return true;
    }
  }
}
