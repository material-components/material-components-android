/*
 * Copyright 2017 The Android Open Source Project
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

package com.google.android.material.transformation;

import com.google.android.material.R;

import static com.google.android.material.animation.AnimationUtils.lerp;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.google.android.material.animation.ChildrenAlphaProperty;
import com.google.android.material.animation.DrawableAlphaProperty;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.animation.MotionTiming;
import com.google.android.material.animation.Positioning;
import com.google.android.material.circularreveal.CircularRevealCompat;
import com.google.android.material.circularreveal.CircularRevealHelper;
import com.google.android.material.circularreveal.CircularRevealWidget;
import com.google.android.material.circularreveal.CircularRevealWidget.CircularRevealScrimColorProperty;
import com.google.android.material.circularreveal.CircularRevealWidget.RevealInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base behavior for any non-scrim view that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public abstract class FabTransformationBehavior extends ExpandableTransformationBehavior {

  private final Rect tmpRect = new Rect();
  private final RectF tmpRectF1 = new RectF();
  private final RectF tmpRectF2 = new RectF();
  private final int[] tmpArray = new int[2];

  // The original translation of the dependency. Used to translate the dependency back to its
  // original position.
  private float dependencyOriginalTranslationX;
  private float dependencyOriginalTranslationY;

  public FabTransformationBehavior() {}

  public FabTransformationBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  @CallSuper
  public boolean layoutDependsOn(
      @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
    if (child.getVisibility() == View.GONE) {
      throw new IllegalStateException(
          "This behavior cannot be attached to a GONE view. Set the view to INVISIBLE instead.");
    }

    if (dependency instanceof FloatingActionButton) {
      int expandedComponentIdHint =
          ((FloatingActionButton) dependency).getExpandedComponentIdHint();
      return expandedComponentIdHint == 0 || expandedComponentIdHint == child.getId();
    }
    return false;
  }

  @Override
  @CallSuper
  public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
    if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
      // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
      // we dodge any Snackbars, matching FAB's behavior.
      lp.dodgeInsetEdges = Gravity.BOTTOM;
    }
  }

  @NonNull
  @Override
  protected AnimatorSet onCreateExpandedStateChangeAnimation(
      @NonNull final View dependency,
      @NonNull final View child,
      final boolean expanded,
      boolean isAnimating) {
    FabTransformationSpec spec = onCreateMotionSpec(child.getContext(), expanded);

    if (expanded) {
      dependencyOriginalTranslationX = dependency.getTranslationX();
      dependencyOriginalTranslationY = dependency.getTranslationY();
    }

    List<Animator> animations = new ArrayList<>();
    List<AnimatorListener> listeners = new ArrayList<>();

    createElevationAnimation(
        dependency, child, expanded, isAnimating, spec, animations, listeners);

    RectF childBounds = tmpRectF1;
    createTranslationAnimation(
        dependency, child, expanded, isAnimating, spec, animations, listeners, childBounds);
    float childWidth = childBounds.width();
    float childHeight = childBounds.height();

    createDependencyTranslationAnimation(dependency, child, expanded, spec, animations);
    createIconFadeAnimation(dependency, child, expanded, isAnimating, spec, animations, listeners);
    createExpansionAnimation(
        dependency,
        child,
        expanded,
        isAnimating,
        spec,
        childWidth,
        childHeight,
        animations,
        listeners);
    createColorAnimation(dependency, child, expanded, isAnimating, spec, animations, listeners);
    createChildrenFadeAnimation(
        dependency, child, expanded, isAnimating, spec, animations, listeners);

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animations);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (expanded) {
              child.setVisibility(View.VISIBLE);
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dependency.setAlpha(0f);
              dependency.setVisibility(View.INVISIBLE);
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (!expanded) {
              child.setVisibility(View.INVISIBLE);
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dependency.setAlpha(1f);
              dependency.setVisibility(View.VISIBLE);
            }
          }
        });
    for (int i = 0, count = listeners.size(); i < count; i++) {
      set.addListener(listeners.get(i));
    }
    return set;
  }

  protected abstract FabTransformationSpec onCreateMotionSpec(Context context, boolean expanded);

  private void createElevationAnimation(
      View dependency,
      @NonNull View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations,
      List<AnimatorListener> unusedListeners) {
    float translationZ = child.getElevation() - dependency.getElevation();
    Animator animator;

    if (expanded) {
      if (!currentlyAnimating) {
        child.setTranslationZ(-translationZ);
      }
      animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Z, 0f);
    } else {
      animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Z, -translationZ);
    }

    MotionTiming timing = spec.timings.getTiming("elevation");
    timing.apply(animator);
    animations.add(animator);
  }

  private void createDependencyTranslationAnimation(
      @NonNull View dependency,
      @NonNull View child,
      boolean expanded,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations) {
    float translationX = calculateTranslationX(dependency, child, spec.positioning);
    float translationY = calculateTranslationY(dependency, child, spec.positioning);

    ValueAnimator translationXAnimator;
    ValueAnimator translationYAnimator;

    Pair<MotionTiming, MotionTiming> motionTiming =
        calculateMotionTiming(translationX, translationY, expanded, spec);
    MotionTiming translationXTiming = motionTiming.first;
    MotionTiming translationYTiming = motionTiming.second;

    translationXAnimator =
        ObjectAnimator.ofFloat(
            dependency,
            View.TRANSLATION_X,
            expanded ? translationX : dependencyOriginalTranslationX);
    translationYAnimator =
        ObjectAnimator.ofFloat(
            dependency,
            View.TRANSLATION_Y,
            expanded ? translationY : dependencyOriginalTranslationY);

    translationXTiming.apply(translationXAnimator);
    translationYTiming.apply(translationYAnimator);
    animations.add(translationXAnimator);
    animations.add(translationYAnimator);
  }

  private void createTranslationAnimation(
      @NonNull View dependency,
      @NonNull View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations,
      List<AnimatorListener> unusedListeners,
      @NonNull RectF childBounds) {
    float translationX = calculateTranslationX(dependency, child, spec.positioning);
    float translationY = calculateTranslationY(dependency, child, spec.positioning);

    ValueAnimator translationXAnimator;
    ValueAnimator translationYAnimator;

    Pair<MotionTiming, MotionTiming> motionTiming =
        calculateMotionTiming(translationX, translationY, expanded, spec);
    MotionTiming translationXTiming = motionTiming.first;
    MotionTiming translationYTiming = motionTiming.second;

    if (expanded) {
      if (!currentlyAnimating) {
        child.setTranslationX(-translationX);
        child.setTranslationY(-translationY);
      }
      translationXAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, 0f);
      translationYAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, 0f);

      calculateChildVisibleBoundsAtEndOfExpansion(
          child,
          spec,
          translationXTiming,
          translationYTiming,
          -translationX,
          -translationY,
          0f,
          0f,
          childBounds);
    } else {
      translationXAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, -translationX);
      translationYAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, -translationY);
    }

    translationXTiming.apply(translationXAnimator);
    translationYTiming.apply(translationYAnimator);
    animations.add(translationXAnimator);
    animations.add(translationYAnimator);
  }

  private void createIconFadeAnimation(
      View dependency,
      final View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations,
      @NonNull List<AnimatorListener> listeners) {
    if (!(child instanceof CircularRevealWidget) || !(dependency instanceof ImageView)) {
      return;
    }

    final CircularRevealWidget circularRevealChild = (CircularRevealWidget) child;
    ImageView dependencyImageView = (ImageView) dependency;
    final Drawable icon = dependencyImageView.getDrawable();

    if (icon == null) {
      return;
    }
    icon.mutate();

    ObjectAnimator animator;

    if (expanded) {
      if (!currentlyAnimating) {
        icon.setAlpha(0xFF);
      }
      animator = ObjectAnimator.ofInt(icon, DrawableAlphaProperty.DRAWABLE_ALPHA_COMPAT, 0x00);
    } else {
      animator = ObjectAnimator.ofInt(icon, DrawableAlphaProperty.DRAWABLE_ALPHA_COMPAT, 0xFF);
    }

    // icon.setCallback() is not expected to be called and
    // child.verifyDrawable() is not expected to be implemented.
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            child.invalidate();
          }
        });

    MotionTiming timing = spec.timings.getTiming("iconFade");
    timing.apply(animator);
    animations.add(animator);
    listeners.add(
        new AnimatorListenerAdapter() {

          @Override
          public void onAnimationStart(Animator animation) {
            circularRevealChild.setCircularRevealOverlayDrawable(icon);
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            circularRevealChild.setCircularRevealOverlayDrawable(null);
          }
        });
  }

  private void createExpansionAnimation(
      @NonNull View dependency,
      View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      float childWidth,
      float childHeight,
      @NonNull List<Animator> animations,
      @NonNull List<AnimatorListener> listeners) {
    if (!(child instanceof CircularRevealWidget)) {
      return;
    }
    final CircularRevealWidget circularRevealChild = (CircularRevealWidget) child;

    float revealCenterX = calculateRevealCenterX(dependency, child, spec.positioning);
    float revealCenterY = calculateRevealCenterY(dependency, child, spec.positioning);
    ((FloatingActionButton) dependency).getMeasuredContentRect(tmpRect);
    float dependencyRadius = tmpRect.width() / 2f;

    Animator animator;
    MotionTiming timing = spec.timings.getTiming("expansion");

    if (expanded) {
      if (!currentlyAnimating) {
        circularRevealChild.setRevealInfo(
            new RevealInfo(revealCenterX, revealCenterY, dependencyRadius));
      }
      float fromRadius =
          currentlyAnimating ? circularRevealChild.getRevealInfo().radius : dependencyRadius;
      float toRadius =
          MathUtils.distanceToFurthestCorner(
              revealCenterX, revealCenterY, 0, 0, childWidth, childHeight);

      animator =
          CircularRevealCompat.createCircularReveal(
              circularRevealChild, revealCenterX, revealCenterY, toRadius);
      animator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              // After toRadius has been reached, jump to no circular clip. This shouldn't result in
              // a visual difference, because toRadius has been calculated precisely to avoid it.
              RevealInfo revealInfo = circularRevealChild.getRevealInfo();
              revealInfo.radius = RevealInfo.INVALID_RADIUS;
              circularRevealChild.setRevealInfo(revealInfo);
            }
          });

      createPreFillRadialExpansion(
          child,
          timing.getDelay(),
          (int) revealCenterX,
          (int) revealCenterY,
          fromRadius,
          animations);
      // No need to post fill. In all cases, circular reveal radius is removed.
    } else {
      float fromRadius = circularRevealChild.getRevealInfo().radius;
      float toRadius = dependencyRadius;
      animator =
          CircularRevealCompat.createCircularReveal(
              circularRevealChild, revealCenterX, revealCenterY, toRadius);

      createPreFillRadialExpansion(
          child,
          timing.getDelay(),
          (int) revealCenterX,
          (int) revealCenterY,
          fromRadius,
          animations);
      createPostFillRadialExpansion(
          child,
          timing.getDelay(),
          timing.getDuration(),
          spec.timings.getTotalDuration(),
          (int) revealCenterX,
          (int) revealCenterY,
          toRadius,
          animations);
    }

    timing.apply(animator);
    animations.add(animator);
    listeners.add(CircularRevealCompat.createCircularRevealListener(circularRevealChild));
  }

  private void createColorAnimation(
      @NonNull View dependency,
      View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations,
      List<AnimatorListener> unusedListeners) {
    if (!(child instanceof CircularRevealWidget)) {
      return;
    }
    CircularRevealWidget circularRevealChild = (CircularRevealWidget) child;

    @ColorInt int tint = getBackgroundTint(dependency);
    @ColorInt int transparent = tint & 0x00FFFFFF;
    ObjectAnimator animator;

    if (expanded) {
      if (!currentlyAnimating) {
        circularRevealChild.setCircularRevealScrimColor(tint);
      }
      animator =
          ObjectAnimator.ofInt(
              circularRevealChild,
              CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR,
              transparent);
    } else {
      animator =
          ObjectAnimator.ofInt(
              circularRevealChild,
              CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR,
              tint);
    }

    animator.setEvaluator(ArgbEvaluatorCompat.getInstance());
    MotionTiming timing = spec.timings.getTiming("color");
    timing.apply(animator);
    animations.add(animator);
  }

  private void createChildrenFadeAnimation(
      View unusedDependency,
      View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull FabTransformationSpec spec,
      @NonNull List<Animator> animations,
      List<AnimatorListener> unusedListeners) {
    if (!(child instanceof ViewGroup)) {
      return;
    }
    if (child instanceof CircularRevealWidget
        && CircularRevealHelper.STRATEGY == CircularRevealHelper.BITMAP_SHADER) {
      // Bitmap shader strategy animates a static snapshot of the child.
      return;
    }

    ViewGroup childContentContainer = calculateChildContentContainer(child);
    if (childContentContainer == null) {
      return;
    }

    Animator animator;

    if (expanded) {
      if (!currentlyAnimating) {
        ChildrenAlphaProperty.CHILDREN_ALPHA.set(childContentContainer, 0f);
      }
      animator =
          ObjectAnimator.ofFloat(childContentContainer, ChildrenAlphaProperty.CHILDREN_ALPHA, 1f);
    } else {
      animator =
          ObjectAnimator.ofFloat(childContentContainer, ChildrenAlphaProperty.CHILDREN_ALPHA, 0f);
    }

    MotionTiming timing = spec.timings.getTiming("contentFade");
    timing.apply(animator);
    animations.add(animator);
  }

  @NonNull
  private Pair<MotionTiming, MotionTiming> calculateMotionTiming(
      float translationX,
      float translationY,
      boolean expanded,
      @NonNull FabTransformationSpec spec) {
    MotionTiming translationXTiming;
    MotionTiming translationYTiming;
    if (translationX == 0 || translationY == 0) {
      // Horizontal or vertical motion.
      translationXTiming = spec.timings.getTiming("translationXLinear");
      translationYTiming = spec.timings.getTiming("translationYLinear");
    } else if ((expanded && translationY < 0) || (!expanded && translationY > 0)) {
      // Upwards motion.
      translationXTiming = spec.timings.getTiming("translationXCurveUpwards");
      translationYTiming = spec.timings.getTiming("translationYCurveUpwards");
    } else {
      // Downwards motion.
      translationXTiming = spec.timings.getTiming("translationXCurveDownwards");
      translationYTiming = spec.timings.getTiming("translationYCurveDownwards");
    }

    return new Pair<>(translationXTiming, translationYTiming);
  }

  private float calculateTranslationX(
      @NonNull View dependency, @NonNull View child, @NonNull Positioning positioning) {
    RectF dependencyBounds = tmpRectF1;
    RectF childBounds = tmpRectF2;

    calculateDependencyWindowBounds(dependency, dependencyBounds);
    calculateWindowBounds(child, childBounds);

    float translationX = 0f;
    switch (positioning.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
      case Gravity.LEFT:
        translationX = childBounds.left - dependencyBounds.left;
        break;
      case Gravity.CENTER_HORIZONTAL:
        translationX = childBounds.centerX() - dependencyBounds.centerX();
        break;
      case Gravity.RIGHT:
        translationX = childBounds.right - dependencyBounds.right;
        break;
      default:
        break;
    }
    translationX += positioning.xAdjustment;
    return translationX;
  }

  private float calculateTranslationY(
      @NonNull View dependency, @NonNull View child, @NonNull Positioning positioning) {
    RectF dependencyBounds = tmpRectF1;
    RectF childBounds = tmpRectF2;

    calculateDependencyWindowBounds(dependency, dependencyBounds);
    calculateWindowBounds(child, childBounds);

    float translationY = 0f;
    switch (positioning.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
      case Gravity.TOP:
        translationY = childBounds.top - dependencyBounds.top;
        break;
      case Gravity.CENTER_VERTICAL:
        translationY = childBounds.centerY() - dependencyBounds.centerY();
        break;
      case Gravity.BOTTOM:
        translationY = childBounds.bottom - dependencyBounds.bottom;
        break;
      default:
        break;
    }
    translationY += positioning.yAdjustment;
    return translationY;
  }

  private void calculateWindowBounds(@NonNull View view, RectF rect) {
    RectF windowBounds = rect;
    windowBounds.set(0, 0, view.getWidth(), view.getHeight());

    int[] windowLocation = tmpArray;
    view.getLocationInWindow(windowLocation);

    windowBounds.offsetTo(windowLocation[0], windowLocation[1]);

    // We specifically want to take into account the transformations of all parents only.
    // The idea is that only this transformation should modify the translation of the views itself.
    windowBounds.offset((int) -view.getTranslationX(), (int) -view.getTranslationY());
  }

  private void calculateDependencyWindowBounds(@NonNull View view, @NonNull RectF rect) {
    calculateWindowBounds(view, rect);
    rect.offset(dependencyOriginalTranslationX, dependencyOriginalTranslationY);
  }

  private float calculateRevealCenterX(
      @NonNull View dependency, @NonNull View child, @NonNull Positioning positioning) {
    RectF dependencyBounds = tmpRectF1;
    RectF childBounds = tmpRectF2;

    calculateDependencyWindowBounds(dependency, dependencyBounds);
    calculateWindowBounds(child, childBounds);

    float translationX = calculateTranslationX(dependency, child, positioning);
    childBounds.offset(-translationX, 0);

    return dependencyBounds.centerX() - childBounds.left;
  }

  private float calculateRevealCenterY(
      @NonNull View dependency, @NonNull View child, @NonNull Positioning positioning) {
    RectF dependencyBounds = tmpRectF1;
    RectF childBounds = tmpRectF2;

    calculateDependencyWindowBounds(dependency, dependencyBounds);
    calculateWindowBounds(child, childBounds);

    float translationY = calculateTranslationY(dependency, child, positioning);
    childBounds.offset(0, -translationY);

    return dependencyBounds.centerY() - childBounds.top;
  }

  private void calculateChildVisibleBoundsAtEndOfExpansion(
      @NonNull View child,
      @NonNull FabTransformationSpec spec,
      @NonNull MotionTiming translationXTiming,
      @NonNull MotionTiming translationYTiming,
      float fromX,
      float fromY,
      float toX,
      float toY,
      @NonNull RectF childBounds) {
    float translationX =
        calculateValueOfAnimationAtEndOfExpansion(spec, translationXTiming, fromX, toX);
    float translationY =
        calculateValueOfAnimationAtEndOfExpansion(spec, translationYTiming, fromY, toY);

    // Calculate the window bounds.
    Rect window = tmpRect;
    child.getWindowVisibleDisplayFrame(window);
    RectF windowF = tmpRectF1;
    windowF.set(window);

    // Calculate the visible bounds of the child given its translation and window bounds.
    RectF childVisibleBounds = tmpRectF2;
    calculateWindowBounds(child, childVisibleBounds);
    childVisibleBounds.offset(translationX, translationY);
    childVisibleBounds.intersect(windowF);

    childBounds.set(childVisibleBounds);
  }

  private float calculateValueOfAnimationAtEndOfExpansion(
      @NonNull FabTransformationSpec spec, @NonNull MotionTiming timing, float from, float to) {
    long delay = timing.getDelay();
    long duration = timing.getDuration();

    // Calculate at what time in the translation animation does the expansion animation end.
    MotionTiming expansionTiming = spec.timings.getTiming("expansion");
    long expansionEnd = expansionTiming.getDelay() + expansionTiming.getDuration();
    // Adjust one frame (16.6ms) for Android's draw pipeline.
    // A value set at frame N will be drawn at frame N+1.
    expansionEnd += 17;
    float fraction = (float) (expansionEnd - delay) / duration;

    // Calculate the exact value of the animation at that time.
    fraction = timing.getInterpolator().getInterpolation(fraction);
    return lerp(from, to, fraction);
  }

  /** Given the a child, return the ViewGroup whose children we want to fade. */
  @Nullable
  private ViewGroup calculateChildContentContainer(@NonNull View view) {
    // 1. If an explicitly tagged view exists, use that as the child content container.
    View childContentContainer = view.findViewById(R.id.mtrl_child_content_container);
    if (childContentContainer != null) {
      return toViewGroupOrNull(childContentContainer);
    }

    // 2. If the view is a wrapper container, use its child as the child content container.
    if (view instanceof TransformationChildLayout || view instanceof TransformationChildCard) {
      childContentContainer = ((ViewGroup) view).getChildAt(0);
      return toViewGroupOrNull(childContentContainer);
    }

    // 3. Use the view itself as the child content container.
    return toViewGroupOrNull(view);
  }

  @Nullable
  private ViewGroup toViewGroupOrNull(View view) {
    if (view instanceof ViewGroup) {
      return (ViewGroup) view;
    } else {
      return null;
    }
  }

  private int getBackgroundTint(@NonNull View view) {
    ColorStateList tintList = view.getBackgroundTintList();
    if (tintList != null) {
      return tintList.getColorForState(view.getDrawableState(), tintList.getDefaultColor());
    } else {
      return Color.TRANSPARENT;
    }
  }

  /** Adds pre radial expansion animator. */
  private void createPreFillRadialExpansion(
      View child,
      long delay,
      int revealCenterX,
      int revealCenterY,
      float fromRadius,
      @NonNull List<Animator> animations) {
    if (delay > 0) {
      Animator animator =
          ViewAnimationUtils.createCircularReveal(
              child, revealCenterX, revealCenterY, fromRadius, fromRadius);
      animator.setStartDelay(0);
      animator.setDuration(delay);
      animations.add(animator);
    }
  }

  /** Adds post radial expansion animator. */
  private void createPostFillRadialExpansion(
      View child,
      long delay,
      long duration,
      long totalDuration,
      int revealCenterX,
      int revealCenterY,
      float toRadius,
      @NonNull List<Animator> animations) {
    if (delay + duration < totalDuration) {
      Animator animator =
          ViewAnimationUtils.createCircularReveal(
              child, revealCenterX, revealCenterY, toRadius, toRadius);
      animator.setStartDelay(delay + duration);
      animator.setDuration(totalDuration - (delay + duration));
      animations.add(animator);
    }
  }

  /** Motion spec for a FAB transformation. */
  protected static class FabTransformationSpec {
    @Nullable public MotionSpec timings;
    public Positioning positioning;
  }
}
