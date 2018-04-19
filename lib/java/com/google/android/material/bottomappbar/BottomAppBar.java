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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapePathModel;
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
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_backgroundTint
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabAlignmentMode
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabAttached
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleDiameter
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleRoundedCornerRadius
 * @attr ref com.google.android.material.bottomappbar.R.styleable#BottomAppBar_fabCradleVerticalOffset
 *
 */
public class BottomAppBar extends Toolbar implements AttachedBehavior {
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

  private final int fabOffsetEndMode;
  private final MaterialShapeDrawable materialShapeDrawable;
  private final BottomAppBarTopEdgeTreatment topEdgeTreatment;

  @Nullable private Animator attachAnimator;
  @Nullable private Animator modeAnimator;
  @Nullable private Animator menuAnimator;
  @FabAlignmentMode private int fabAlignmentMode;
  private boolean hideOnScroll;

  /** If the fab is actually cradled in the {@link BottomAppBar} or if it's floating above it. */
  private boolean fabAttached;

  public BottomAppBar(Context context) {
    this(context, null, 0);
  }

  public BottomAppBar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomAppBarStyle);
  }

  public BottomAppBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.BottomAppBar,
            defStyleAttr,
            R.style.Widget_MaterialComponents_BottomAppBar);

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(context, a, R.styleable.BottomAppBar_backgroundTint);

    float fabCradleDiameter =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleDiameter, 0);
    float fabCornerRadius =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleRoundedCornerRadius, 0);
    float fabVerticalOffset =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleVerticalOffset, 0);
    fabAttached = a.getBoolean(R.styleable.BottomAppBar_fabAttached, true);
    fabAlignmentMode =
        a.getInt(R.styleable.BottomAppBar_fabAlignmentMode, FAB_ALIGNMENT_MODE_CENTER);
    hideOnScroll = a.getBoolean(R.styleable.BottomAppBar_hideOnScroll, false);

    a.recycle();

    fabOffsetEndMode =
        getResources().getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fabOffsetEndMode);

    topEdgeTreatment =
        new BottomAppBarTopEdgeTreatment(fabCradleDiameter, fabCornerRadius, fabVerticalOffset);
    ShapePathModel appBarModel = new ShapePathModel();
    appBarModel.setTopEdge(topEdgeTreatment);
    materialShapeDrawable = new MaterialShapeDrawable(appBarModel);
    materialShapeDrawable.setStrokeWidth(1f /* hairline */);
    materialShapeDrawable.setShadowEnabled(true);
    materialShapeDrawable.setPaintStyle(Style.FILL);
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

  /** Returns true if the FAB should be cradled, false otherwise. */
  public boolean isFabAttached() {
    return fabAttached;
  }

  /** Sets the current state which determines if the FAB is cradled or not. */
  public void setFabAttached(boolean attached) {
    maybeAnimateAttachChange(attached);
    maybeAnimateMenuView(fabAlignmentMode, attached);
    this.fabAttached = attached;
  }

  /** Returns the vertical offset for the cradle. */
  public float getCradleVerticalOffset() {
    return topEdgeTreatment.getCradleVerticalOffset();
  }

  /**
   * Sets the cradle vertical offset
   *
   * @param verticalOffset
   */
  public void setCradleVerticalOffset(int verticalOffset) {
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

  private void maybeAnimateModeChange(@FabAlignmentMode int targetMode) {
    if (fabAlignmentMode == targetMode || !ViewCompat.isLaidOut(this)) {
      return;
    }

    if (modeAnimator != null) {
      modeAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    createCradleTranslationAnimation(targetMode, animators);
    createFabTranslationXAnimation(targetMode, animators);

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

  private void createCradleTranslationAnimation(
      @FabAlignmentMode int targetMode, List<Animator> animators) {
    if (!fabAttached) {
      return;
    }

    ValueAnimator animator =
        ValueAnimator.ofFloat(
            topEdgeTreatment.getHorizontalOffset(), getFabTranslationX(targetMode));

    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            topEdgeTreatment.setHorizontalOffset((Float) animation.getAnimatedValue());
            materialShapeDrawable.invalidateSelf();
          }
        });
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
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

  private boolean isVisibleFab() {
    FloatingActionButton fab = findDependentFab();
    return fab != null && fab.isOrWillBeShown();
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
    if (!isVisibleFab()) {
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

    if ((fabAttached || (targetAttached && isVisibleFab()))
        && (fabAlignmentMode == FAB_ALIGNMENT_MODE_END || targetMode == FAB_ALIGNMENT_MODE_END)) {
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

  private void maybeAnimateAttachChange(boolean targetAttached) {
    if (!ViewCompat.isLaidOut(this)) {
      return;
    }

    if (attachAnimator != null) {
      attachAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    createCradleShapeAnimation(targetAttached && isVisibleFab(), animators);
    createFabTranslationYAnimation(targetAttached, animators);

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    attachAnimator = set;
    attachAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            attachAnimator = null;
          }
        });
    attachAnimator.start();
  }

  private void createCradleShapeAnimation(boolean showCradle, List<Animator> animators) {
    // If we are animating the fab in, set the correct horizontal offset
    if (showCradle) {
      topEdgeTreatment.setHorizontalOffset(getFabTranslationX());
    }

    ValueAnimator animator =
        ValueAnimator.ofFloat(materialShapeDrawable.getInterpolation(), showCradle ? 1 : 0);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            materialShapeDrawable.setInterpolation((Float) animation.getAnimatedValue());
          }
        });
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
  }

  private void createFabTranslationYAnimation(boolean targetAttached, List<Animator> animators) {
    FloatingActionButton fab = findDependentFab();
    if (fab == null) {
      return;
    }

    ObjectAnimator animator =
        ObjectAnimator.ofFloat(fab, "translationY", getFabTranslationY(targetAttached));
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
  }

  private float getFabTranslationY(boolean targetAttached) {
    FloatingActionButton fab = findDependentFab();
    if (fab == null) {
      return 0;
    }

    // Get the content rect to calculate the amount of padding added with shadow.
    Rect fabContentRect = new Rect();
    fab.getContentRect(fabContentRect);

    float fabHeight = fabContentRect.height();
    if (fabHeight == 0) {
      // If the fab hasn't been laid out yet, lets look at the measured height.
      fabHeight = fab.getMeasuredHeight();
    }
    float fabBottomShadow = fab.getHeight() - fabContentRect.bottom;
    float fabVerticalShadowPadding = fab.getHeight() - fabContentRect.height();

    float attached = -getCradleVerticalOffset() + fabHeight / 2 + fabBottomShadow;
    float detached = fabVerticalShadowPadding - fab.getPaddingBottom();

    return -getMeasuredHeight() + (targetAttached ? attached : detached);
  }

  private float getFabTranslationY() {
    return getFabTranslationY(fabAttached);
  }

  private int getFabTranslationX(@FabAlignmentMode int fabAlignmentMode) {
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
    actionMenuView.setTranslationX(
        fabAlignmentMode == FAB_ALIGNMENT_MODE_END && fabAttached ? offset : 0);
  }

  private void cancelAnimations() {
    if (attachAnimator != null) {
      attachAnimator.cancel();
    }
    if (menuAnimator != null) {
      menuAnimator.cancel();
    }
    if (modeAnimator != null) {
      modeAnimator.cancel();
    }
  }

  private boolean isAnimationRunning() {
    return (attachAnimator != null && attachAnimator.isRunning())
        || (menuAnimator != null && menuAnimator.isRunning())
        || (modeAnimator != null && modeAnimator.isRunning());
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    // Stop any animations that might be trying to move things around.
    cancelAnimations();

    setCutoutState();
  }

  private void setCutoutState() {
    // Layout all elements related to the positioning of the fab.
    topEdgeTreatment.setHorizontalOffset(getFabTranslationX());
    FloatingActionButton fab = findDependentFab();
    materialShapeDrawable.setInterpolation(fabAttached && isVisibleFab() ? 1 : 0);
    if (fab != null) {
      fab.setTranslationY(getFabTranslationY());
      fab.setTranslationX(getFabTranslationX());
    }
    ActionMenuView actionMenuView = getActionMenuView();
    if (actionMenuView != null) {
      actionMenuView.setAlpha(1.0f);
      if (!isVisibleFab()) {
        translateActionMenuView(actionMenuView, FAB_ALIGNMENT_MODE_CENTER, false);
      } else {
        translateActionMenuView(actionMenuView, fabAlignmentMode, fabAttached);
      }
    }
  }

  /**
   * Listens to the FABs hide or show animation to kick off an animation on BottomAppBar that reacts
   * to the change.
   */
  AnimatorListenerAdapter fabAnimationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
          maybeAnimateAttachChange(fabAttached);
          maybeAnimateMenuView(fabAlignmentMode, fabAttached);
        }
      };

  /**
   * Ensures that the FAB show and hide animations are linked to this BottomAppBar so it can react
   * to changes in the FABs visibility.
   *
   * @param fab the FAB to link the animations with
   */
  private void addFabAnimationListeners(@NonNull FloatingActionButton fab) {
    removeFabAnimationListeners(fab);
    fab.addOnHideAnimationListener(fabAnimationListener);
    fab.addOnShowAnimationListener(fabAnimationListener);
  }

  private void removeFabAnimationListeners(@NonNull FloatingActionButton fab) {
    fab.removeOnHideAnimationListener(fabAnimationListener);
    fab.removeOnShowAnimationListener(fabAnimationListener);
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
    return new Behavior();
  }

  /**
   * Behavior designed for use with {@link BottomAppBar} instances. Its main function is to link a
   * dependent {@link FloatingActionButton} so that it can be shown docked in the cradle.
   */
  public static class Behavior extends HideBottomViewOnScrollBehavior<BottomAppBar> {

    /** Default constructor for instantiating this Behavior. */
    public Behavior() {}

    /**
     * Default constructor for inflating this Behavior from layout.
     *
     * @param context The {@link Context}.
     * @param attrs The {@link AttributeSet}.
     */
    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    private boolean updateFabPositionAndVisibility(FloatingActionButton fab, BottomAppBar child) {
      // Set the initial position of the FloatingActionButton with the BottomAppBar vertical offset.
      CoordinatorLayout.LayoutParams fabLayoutParams =
          (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
      fabLayoutParams.anchorGravity = Gravity.CENTER;

      // Ensure the FAB is correctly linked to this BAB so the animations can run correctly
      child.addFabAnimationListeners(fab);

      return true;
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, BottomAppBar child, int layoutDirection) {
      FloatingActionButton fab = child.findDependentFab();
      if (fab != null) {
        updateFabPositionAndVisibility(fab, child);
      }

      // If an animation is running, it should update the cutout to match the FAB, so don't do
      // anything here.
      if (!child.isAnimationRunning()) {
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
    protected void slideUp(BottomAppBar child) {
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
    protected void slideDown(BottomAppBar child) {
      super.slideDown(child);
      FloatingActionButton fab = child.findDependentFab();
      if (fab != null) {
        Rect contentRect = new Rect();
        fab.getContentRect(contentRect);
        float fabShadowPadding = fab.getMeasuredHeight() - contentRect.height();

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
