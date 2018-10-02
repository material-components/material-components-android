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

package com.google.android.material.bottomappbar;

import com.google.android.material.R;

import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Dimension;
import android.support.annotation.IntDef;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.TransformationListener;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.AttachedBehavior;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewCompat.NestedScrollType;
import android.support.v4.view.ViewCompat.ScrollAxis;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * The Bottom App Bar is an extension of Toolbar that supports a shaped background that "cradles" an
 * attached {@link FloatingActionButton}. A FAB is anchored to {@link BottomAppBar} by calling
 * {@link CoordinatorLayout.LayoutParams#setAnchorId(int)}, or by setting {@code app:layout_anchor}
 * on the FAB in xml.
 *
 * <p>There are two modes which determine where the FAB is shown relative to the {@link
 * BottomAppBar}. {@link #FAB_ALIGNMENT_MODE_CENTER} mode is the primary mode with the FAB is
 * centered. {@link #FAB_ALIGNMENT_MODE_END} is the secondary mode with the FAB on the side.
 *
 * <p>Do not use the {@code android:background} attribute or call {@code BottomAppBar.setBackground}
 * because the BottomAppBar manages its background internally. Instead use {@code
 * app:backgroundTint}.
 *
 * <p>We are currently waiting for better attribute support in ColorStateLists to land in the
 * support library. https://android-review.googlesource.com/757091. Because we can't fully support
 * color theming on all API levels for the BottomAppBar, to use the default theme, we are requiring
 * you to opt in for now.
 *
 * <p>As a workaround to enable correct color theming in your app for API < 23, in addition to
 * setting the colorOnSurface attribute in your theme, redefine {@code
 * mtrl_on_surface_emphasis_medium} to match with the correct opacity. For example, if you set
 * {@code colorOnSurface} in your theme to red (#FF0000). You should redefine {@code
 * mtrl_on_surface_emphasis_medium} to be #99FF0000. This sets the color value to be the correct
 * color and opacity to match the correct color theming that will be applied on API level 23 and up.
 * When the bugs are fixed in the support library you can remove these color definitions.
 *
 * <p>To enable color theming for menu items you will also need to set the {@code
 * materialThemeOverlay} attribute to a ThemeOverlay which sets the {@code colorControlNormal}
 * attribute to the correct color. For example, if the background of the BottomAppBar is {@code
 * colorSurface}, as it is in the default style, you should set {@code materialThemeOverlay} to
 * {@code @style/ThemeOverlay.MaterialComponents.BottomAppBar.Surface}.
 *
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_backgroundTint
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabAlignmentMode
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabAnimationMode
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleMargin
 * @attr ref
 *     com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleRoundedCornerRadius
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleVerticalOffset
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_hideOnScroll
 */
public class BottomAppBar extends Toolbar implements AttachedBehavior {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_BottomAppBar;

  private static final long ANIMATION_DURATION = 300;

  public static final int FAB_ALIGNMENT_MODE_CENTER = 0;
  public static final int FAB_ALIGNMENT_MODE_END = 1;

  /**
   * The fabAlignmentMode determines the horizontal positioning of the cradle and the FAB which can
   * be centered or aligned to the end.
   */
  @IntDef({FAB_ALIGNMENT_MODE_CENTER, FAB_ALIGNMENT_MODE_END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAlignmentMode {}

  public static final int FAB_ANIMATION_MODE_SCALE = 0;
  public static final int FAB_ANIMATION_MODE_SLIDE = 1;

  /**
   * The fabAnimationMode determines the animation used to move the FAB between different alignment
   * modes. Can be either scale, or slide. Scale mode will scale the fab down to a point and then
   * scale it back in at it's new position. Slide mode will slide the fab from one position to the
   * other.
   */
  @IntDef({FAB_ANIMATION_MODE_SCALE, FAB_ANIMATION_MODE_SLIDE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAnimationMode {}

  private final int fabOffsetEndMode;
  private final MaterialShapeDrawable materialShapeDrawable;
  private final BottomAppBarTopEdgeTreatment topEdgeTreatment;

  @Nullable private Animator modeAnimator;
  @Nullable private Animator menuAnimator;
  private final int fabVerticalOffset;
  @FabAlignmentMode private int fabAlignmentMode;
  @FabAnimationMode private int fabAnimationMode;
  private boolean hideOnScroll;

  /**
   * If the {@link FloatingActionButton} is actually cradled in the {@link BottomAppBar} or if the
   * {@link FloatingActionButton} is detached which will happen when the {@link
   * FloatingActionButton} is not visible, or when the {@link BottomAppBar} is scrolled off the
   * screen.
   */
  private boolean fabAttached = true;

  private Behavior behavior;

  /**
   * Listens to the FABs hide or show animation to kick off an animation on BottomAppBar that reacts
   * to the change.
   */
  AnimatorListenerAdapter fabAnimationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
          maybeAnimateMenuView(fabAlignmentMode, fabAttached);
        }
      };

  /** Listens to any transformations applied to the FAB so the cutout can react. */
  TransformationListener<FloatingActionButton> fabTransformationListener =
      new TransformationListener<FloatingActionButton>() {
        @Override
        public void onScaleChanged(FloatingActionButton fab) {
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() != View.GONE ? fab.getScaleY() : 0);
        }

        @Override
        public void onTranslationChanged(FloatingActionButton fab) {
          topEdgeTreatment.setHorizontalOffset(fab.getTranslationX());
          topEdgeTreatment.setCradleVerticalOffset(-fab.getTranslationY());
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() != View.GONE ? fab.getScaleY() : 0);
        }
      };

  public BottomAppBar(Context context) {
    this(context, null, 0);
  }

  public BottomAppBar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomAppBarStyle);
  }

  public BottomAppBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.BottomAppBar, defStyleAttr, DEF_STYLE_RES);

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(context, a, R.styleable.BottomAppBar_backgroundTint);

    float fabCradleMargin = a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleMargin, 0);
    float fabCornerRadius =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleRoundedCornerRadius, 0);
    fabVerticalOffset =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleVerticalOffset, 0);
    fabAlignmentMode =
        a.getInt(R.styleable.BottomAppBar_fabAlignmentMode, FAB_ALIGNMENT_MODE_CENTER);
    fabAnimationMode =
        a.getInt(R.styleable.BottomAppBar_fabAnimationMode, FAB_ANIMATION_MODE_SCALE);
    hideOnScroll = a.getBoolean(R.styleable.BottomAppBar_hideOnScroll, false);

    a.recycle();

    fabOffsetEndMode =
        getResources().getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fabOffsetEndMode);

    topEdgeTreatment =
        new BottomAppBarTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset);
    ShapeAppearanceModel appBarModel = new ShapeAppearanceModel();
    appBarModel.setTopEdge(topEdgeTreatment);
    materialShapeDrawable = new MaterialShapeDrawable(appBarModel);
    materialShapeDrawable.setShadowEnabled(true);
    materialShapeDrawable.setPaintStyle(Style.FILL);
    materialShapeDrawable.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint);
    ViewCompat.setBackground(this, materialShapeDrawable);
  }

  /**
   * Returns the current fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER} or {@link
   * #FAB_ALIGNMENT_MODE_END}.
   */
  @FabAlignmentMode
  public int getFabAlignmentMode() {
    return fabAlignmentMode;
  }

  /**
   * Sets the current fabAlignmentMode. An animated transition between current and desired modes
   * will be played.
   *
   * @param fabAlignmentMode the desired fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER}
   *     or {@link #FAB_ALIGNMENT_MODE_END}.
   */
  public void setFabAlignmentMode(@FabAlignmentMode int fabAlignmentMode) {
    maybeAnimateModeChange(fabAlignmentMode);
    maybeAnimateMenuView(fabAlignmentMode, fabAttached);
    this.fabAlignmentMode = fabAlignmentMode;
  }

  /**
   * Returns the current fabAlignmentMode, either {@link #FAB_ANIMATION_MODE_SCALE} or {@link
   * #FAB_ANIMATION_MODE_SLIDE}.
   */
  @FabAnimationMode
  public int getFabAnimationMode() {
    return fabAnimationMode;
  }

  /**
   * Sets the current fabAlignmentMode. Determines which animation will be played when the fab is
   * animated from from one {@link FabAlignmentMode} to another.
   *
   * @param fabAnimationMode the desired fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER}
   *     or {@link #FAB_ALIGNMENT_MODE_END}.
   */
  public void setFabAnimationMode(@FabAnimationMode int fabAnimationMode) {
    this.fabAnimationMode = fabAnimationMode;
  }

  public void setBackgroundTint(@Nullable ColorStateList backgroundTint) {
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint);
  }

  @Nullable
  public ColorStateList getBackgroundTint() {
    return materialShapeDrawable.getTintList();
  }

  /**
   * Returns the cradle margin for the fab cutout. This is the space between the fab and the cutout.
   */
  public float getFabCradleMargin() {
    return topEdgeTreatment.getFabCradleMargin();
  }

  /**
   * Sets the cradle margin for the fab cutout. This is the space between the fab and the cutout.
   */
  public void setFabCradleMargin(@Dimension float cradleMargin) {
    if (cradleMargin != getFabCradleMargin()) {
      topEdgeTreatment.setFabCradleMargin(cradleMargin);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /** Returns the rounded corner radius for the cutout. A value of 0 will be a sharp edge. */
  @Dimension
  public float getFabCradleRoundedCornerRadius() {
    return topEdgeTreatment.getFabCradleRoundedCornerRadius();
  }

  /** Sets the rounded corner radius for the fab cutout. A value of 0 will be a sharp edge. */
  public void setFabCradleRoundedCornerRadius(@Dimension float roundedCornerRadius) {
    if (roundedCornerRadius != getFabCradleRoundedCornerRadius()) {
      topEdgeTreatment.setFabCradleRoundedCornerRadius(roundedCornerRadius);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /**
   * Returns the vertical offset for the fab cutout. An offset of 0 indicates the vertical center of
   * the {@link FloatingActionButton} is positioned on the top edge.
   */
  @Dimension
  public float getCradleVerticalOffset() {
    return topEdgeTreatment.getCradleVerticalOffset();
  }

  /**
   * Sets the vertical offset, in pixels, of the {@link FloatingActionButton} being cradled. An
   * offset of 0 indicates the vertical center of the {@link FloatingActionButton} is positioned on
   * the top edge.
   */
  public void setCradleVerticalOffset(@Dimension float verticalOffset) {
    if (verticalOffset != getCradleVerticalOffset()) {
      topEdgeTreatment.setCradleVerticalOffset(verticalOffset);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /**
   * Returns true if the {@link BottomAppBar} should hide when a {@link
   * android.support.v4.view.NestedScrollingChild} is scrolled. This is handled by {@link
   * BottomAppBar.Behavior}.
   */
  public boolean getHideOnScroll() {
    return hideOnScroll;
  }

  /**
   * Sets if the {@link BottomAppBar} should hide when a {@link
   * android.support.v4.view.NestedScrollingChild} is scrolled. This is handled by {@link
   * BottomAppBar.Behavior}.
   */
  public void setHideOnScroll(boolean hide) {
    hideOnScroll = hide;
  }

  /**
   * A convenience method to replace the contents of the BottomAppBar's menu.
   *
   * @param newMenu the desired new menu.
   */
  public void replaceMenu(@MenuRes int newMenu) {
    getMenu().clear();
    inflateMenu(newMenu);
  }

  /**
   * Sets the fab diameter. This will be called automatically by the {@link BottomAppBar.Behavior}
   * if the fab is anchored to this {@link BottomAppBar}.
   */
  void setFabDiameter(@Px int diameter) {
    if (diameter != topEdgeTreatment.getFabDiameter()) {
      topEdgeTreatment.setFabDiameter(diameter);
      materialShapeDrawable.invalidateSelf();
    }
  }

  private void maybeAnimateModeChange(@FabAlignmentMode int targetMode) {
    if (fabAlignmentMode == targetMode || !ViewCompat.isLaidOut(this)) {
      return;
    }

    if (modeAnimator != null) {
      modeAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    if (fabAnimationMode == FAB_ANIMATION_MODE_SLIDE) {
      createFabTranslationXAnimation(targetMode, animators);
    } else {
      createFabDefaultXAnimation(targetMode, animators);
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    modeAnimator = set;
    modeAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            modeAnimator = null;
          }
        });
    modeAnimator.start();
  }

  @Nullable
  private FloatingActionButton findDependentFab() {
    if (!(getParent() instanceof CoordinatorLayout)) {
      // If we aren't in a CoordinatorLayout we won't have a dependent FAB.
      return null;
    }

    List<View> dependents = ((CoordinatorLayout) getParent()).getDependents(this);
    for (View v : dependents) {
      if (v instanceof FloatingActionButton) {
        return (FloatingActionButton) v;
      }
    }

    return null;
  }

  private boolean isFabVisibleOrWillBeShown() {
    FloatingActionButton fab = findDependentFab();
    return fab != null && fab.isOrWillBeShown();
  }

  /**
   * Creates the default animation for moving a fab between alignment modes. Can be overridden by
   * extending classes to create a custom animation. Animations that should be executed should be
   * added to the animators list. The default animation defined here calls {@link
   * FloatingActionButton#hide()} and {@link FloatingActionButton#show()} rather than using custom
   * animations.
   */
  protected void createFabDefaultXAnimation(
      final @FabAlignmentMode int targetMode, List<Animator> animators) {
    final FloatingActionButton fab = findDependentFab();

    if (fab == null || fab.isOrWillBeHidden()) {
      return;
    }

    fab.hide(
        new OnVisibilityChangedListener() {
          @Override
          public void onHidden(FloatingActionButton fab) {
            fab.setTranslationX(getFabTranslationX(targetMode));
            fab.show();
          }
        });
  }

  private void createFabTranslationXAnimation(
      @FabAlignmentMode int targetMode, List<Animator> animators) {
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(findDependentFab(), "translationX", getFabTranslationX(targetMode));
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
  }

  private void maybeAnimateMenuView(@FabAlignmentMode int targetMode, boolean newFabAttached) {
    if (!ViewCompat.isLaidOut(this)) {
      return;
    }

    if (menuAnimator != null) {
      menuAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    // If there's no visible FAB, treat the animation like the FAB is going away.
    if (!isFabVisibleOrWillBeShown()) {
      targetMode = FAB_ALIGNMENT_MODE_CENTER;
      newFabAttached = false;
    }

    createMenuViewTranslationAnimation(targetMode, newFabAttached, animators);

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    menuAnimator = set;
    menuAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            menuAnimator = null;
          }
        });
    menuAnimator.start();
  }

  private void createMenuViewTranslationAnimation(
      @FabAlignmentMode final int targetMode,
      final boolean targetAttached,
      List<Animator> animators) {

    final ActionMenuView actionMenuView = getActionMenuView();

    // Stop if there is no action menu view to animate
    if (actionMenuView == null) {
      return;
    }

    Animator fadeIn = ObjectAnimator.ofFloat(actionMenuView, "alpha", 1);

    float translationXDifference =
        actionMenuView.getTranslationX()
            - getActionMenuViewTranslationX(actionMenuView, targetMode, targetAttached);

    // If the MenuView has moved at least a pixel we will need to animate it.
    if (Math.abs(translationXDifference) > 1) {
      // We need to fade the MenuView out and in because it's position is changing
      Animator fadeOut = ObjectAnimator.ofFloat(actionMenuView, "alpha", 0);

      fadeOut.addListener(
          new AnimatorListenerAdapter() {
            public boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
              cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              if (!cancelled) {
                translateActionMenuView(actionMenuView, targetMode, targetAttached);
              }
            }
          });

      AnimatorSet set = new AnimatorSet();
      set.setDuration(ANIMATION_DURATION / 2);
      set.playSequentially(fadeOut, fadeIn);
      animators.add(set);
    } else if (actionMenuView.getAlpha() < 1) {
      // If the previous animation was cancelled in the middle and now we're deciding we don't need
      // fade the MenuView away and back in, we need to ensure the MenuView is visible
      animators.add(fadeIn);
    }
  }

  private float getFabTranslationY() {
    return -fabVerticalOffset;
  }

  private float getFabTranslationX(@FabAlignmentMode int fabAlignmentMode) {
    boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    return fabAlignmentMode == FAB_ALIGNMENT_MODE_END
        ? (getMeasuredWidth() / 2 - fabOffsetEndMode) * (isRtl ? -1 : 1)
        : 0;
  }

  private float getFabTranslationX() {
    return getFabTranslationX(fabAlignmentMode);
  }

  @Nullable
  private ActionMenuView getActionMenuView() {
    for (int i = 0; i < getChildCount(); i++) {
      View view = getChildAt(i);
      if (view instanceof ActionMenuView) {
        return (ActionMenuView) view;
      }
    }

    return null;
  }

  /**
   * Translates the ActionMenuView so that it is aligned correctly depending on the fabAlignmentMode
   * and if the fab is attached. The view will be translated to the left when the fab is attached
   * and on the end. Otherwise it will be in its normal position.
   *
   * @param actionMenuView the ActionMenuView to translate
   * @param fabAlignmentMode the fabAlignmentMode used to determine the position of the
   *     ActionMenuView
   * @param fabAttached whether the ActionMenuView should be moved
   */
  private void translateActionMenuView(
      ActionMenuView actionMenuView, @FabAlignmentMode int fabAlignmentMode, boolean fabAttached) {
    actionMenuView.setTranslationX(
        getActionMenuViewTranslationX(actionMenuView, fabAlignmentMode, fabAttached));
  }

  private int getActionMenuViewTranslationX(
      ActionMenuView actionMenuView, @FabAlignmentMode int fabAlignmentMode, boolean fabAttached) {
    int toolbarLeftContentEnd = 0;
    boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

    // Calculate the inner side of the Toolbar's Gravity.START contents.
    for (int i = 0; i < getChildCount(); i++) {
      View view = getChildAt(i);
      boolean isAlignedToStart =
          view.getLayoutParams() instanceof Toolbar.LayoutParams
              && (((Toolbar.LayoutParams) view.getLayoutParams()).gravity
                      & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
                  == Gravity.START;
      if (isAlignedToStart) {
        toolbarLeftContentEnd =
            Math.max(toolbarLeftContentEnd, isRtl ? view.getLeft() : view.getRight());
      }
    }

    int end = isRtl ? actionMenuView.getRight() : actionMenuView.getLeft();
    int offset = toolbarLeftContentEnd - end;

    return fabAlignmentMode == FAB_ALIGNMENT_MODE_END && fabAttached ? offset : 0;
  }

  private void cancelAnimations() {
    if (menuAnimator != null) {
      menuAnimator.cancel();
    }
    if (modeAnimator != null) {
      modeAnimator.cancel();
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    // If the layout hasn't changed this means the position and size hasn't changed so we don't need
    // to update the position of the cutout and we can continue any running animations. Otherwise,
    // we should stop any animations that might be trying to move things around and reset the
    // position of the cutout.
    if (changed) {
      cancelAnimations();

      setCutoutState();
    }
  }

  private void setCutoutState() {
    // Layout all elements related to the positioning of the fab.
    topEdgeTreatment.setHorizontalOffset(getFabTranslationX());
    FloatingActionButton fab = findDependentFab();
    materialShapeDrawable.setInterpolation(fabAttached && isFabVisibleOrWillBeShown() ? 1 : 0);
    if (fab != null) {
      fab.setTranslationY(getFabTranslationY());
      fab.setTranslationX(getFabTranslationX());
    }
    ActionMenuView actionMenuView = getActionMenuView();
    if (actionMenuView != null) {
      actionMenuView.setAlpha(1.0f);
      if (!isFabVisibleOrWillBeShown()) {
        translateActionMenuView(actionMenuView, FAB_ALIGNMENT_MODE_CENTER, false);
      } else {
        translateActionMenuView(actionMenuView, fabAlignmentMode, fabAttached);
      }
    }
  }

  /**
   * Ensures that the FAB show and hide animations are linked to this BottomAppBar so it can react
   * to changes in the FABs visibility.
   *
   * @param fab the FAB to link the animations with
   */
  private void addFabAnimationListeners(@NonNull FloatingActionButton fab) {
    fab.addOnHideAnimationListener(fabAnimationListener);
    fab.addOnShowAnimationListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            fabAnimationListener.onAnimationStart(animation);

            // Any time the fab is being shown make sure it is in the correct position.
            FloatingActionButton fab = findDependentFab();
            if (fab != null) {
              fab.setTranslationX(getFabTranslationX());
            }
          }
        });
    fab.addTransformationListener(fabTransformationListener);
  }

  @Override
  public void setTitle(CharSequence title) {
    // Don't do anything. BottomAppBar can't have a title.
  }

  @Override
  public void setSubtitle(CharSequence subtitle) {
    // Don't do anything. BottomAppBar can't have a subtitle.
  }

  @NonNull
  @Override
  public CoordinatorLayout.Behavior<BottomAppBar> getBehavior() {
    if (behavior == null) {
      behavior = new Behavior();
    }
    return behavior;
  }

  /**
   * Behavior designed for use with {@link BottomAppBar} instances. Its main function is to link a
   * dependent {@link FloatingActionButton} so that it can be shown docked in the cradle.
   */
  public static class Behavior extends HideBottomViewOnScrollBehavior<BottomAppBar> {

    private final Rect fabContentRect;

    /** Default constructor for instantiating this Behavior. */
    public Behavior() {
      fabContentRect = new Rect();
    }

    /**
     * Default constructor for inflating this Behavior from layout.
     *
     * @param context The {@link Context}.
     * @param attrs The {@link AttributeSet}.
     */
    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      fabContentRect = new Rect();
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, BottomAppBar child, int layoutDirection) {
      FloatingActionButton fab = child.findDependentFab();
      if (fab != null && !ViewCompat.isLaidOut(fab)) {
        // Set the initial position of the FloatingActionButton with the BottomAppBar vertical
        // offset.
        CoordinatorLayout.LayoutParams fabLayoutParams =
            (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fabLayoutParams.anchorGravity = Gravity.CENTER | Gravity.TOP;

        // Ensure the FAB is correctly linked to this BAB so the animations can run correctly
        child.addFabAnimationListeners(fab);

        // Set the correct cutout diameter
        fab.getMeasuredContentRect(fabContentRect);
        child.setFabDiameter(fabContentRect.height());

        // Move the fab to the correct position
        child.setCutoutState();
      }

      // Now let the CoordinatorLayout lay out the BAB
      parent.onLayoutChild(child, layoutDirection);
      return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(
        @NonNull CoordinatorLayout coordinatorLayout,
        @NonNull BottomAppBar child,
        @NonNull View directTargetChild,
        @NonNull View target,
        @ScrollAxis int axes,
        @NestedScrollType int type) {
      // We will ask to start on nested scroll if the BottomAppBar is set to hide.
      return child.getHideOnScroll()
          && super.onStartNestedScroll(
              coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    @Override
    public void slideUp(BottomAppBar child) {
      super.slideUp(child);
      FloatingActionButton fab = child.findDependentFab();
      if (fab != null) {
        fab.clearAnimation();
        fab.animate()
            .translationY(child.getFabTranslationY())
            .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            .setDuration(ENTER_ANIMATION_DURATION);
      }
    }

    @Override
    public void slideDown(BottomAppBar child) {
      super.slideDown(child);
      FloatingActionButton fab = child.findDependentFab();
      if (fab != null) {
        fab.getContentRect(fabContentRect);
        float fabShadowPadding = fab.getMeasuredHeight() - fabContentRect.height();

        fab.clearAnimation();
        fab.animate()
            .translationY(-fab.getPaddingBottom() + fabShadowPadding)
            .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
            .setDuration(EXIT_ANIMATION_DURATION);
      }
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.fabAlignmentMode = fabAlignmentMode;
    savedState.fabAttached = fabAttached;
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    fabAlignmentMode = savedState.fabAlignmentMode;
    fabAttached = savedState.fabAttached;
  }

  static class SavedState extends AbsSavedState {
    @FabAlignmentMode int fabAlignmentMode;
    boolean fabAttached;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(Parcel in, ClassLoader loader) {
      super(in, loader);
      fabAlignmentMode = in.readInt();
      fabAttached = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(fabAlignmentMode);
      out.writeInt(fabAttached ? 1 : 0);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in, null);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
