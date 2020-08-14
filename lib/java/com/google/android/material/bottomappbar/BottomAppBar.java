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

import static com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.ViewCompat.ScrollAxis;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.animation.TransformationCallback;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * The Bottom App Bar is an extension of Toolbar that supports a shaped background that "cradles" an
 * attached {@link FloatingActionButton}. A FAB is anchored to {@link BottomAppBar} by calling
 * {@link CoordinatorLayout.LayoutParams#setAnchorId(int)}, or by setting {@code app:layout_anchor}
 * on the FAB in xml.
 *
 * <p>Note: The Material Design Guidelines caution against using an {@link
 * ExtendedFloatingActionButton} with a {@link BottomAppBar}, so there is limited support for that
 * use case. {@link ExtendedFloatingActionButton} can be anchored to the {@link BottomAppBar}, but
 * currently animations and the cutout are not supported.
 *
 * <p>There are two modes which determine where the FAB is shown relative to the {@link
 * BottomAppBar}. {@link #FAB_ALIGNMENT_MODE_CENTER} mode is the primary mode with the FAB is
 * centered. {@link #FAB_ALIGNMENT_MODE_END} is the secondary mode with the FAB on the side.
 *
 * <p>Do not use the {@code android:background} attribute or call {@code BottomAppBar.setBackground}
 * because the BottomAppBar manages its background internally. Instead use {@code
 * app:backgroundTint}.
 *
 * <p>To enable color theming for menu items you will also need to set the {@code
 * materialThemeOverlay} attribute to a ThemeOverlay which sets the {@code colorControlNormal}
 * attribute to the correct color. For example, if the background of the BottomAppBar is {@code
 * colorSurface}, as it is in the default style, you should set {@code materialThemeOverlay} to
 * {@code @style/ThemeOverlay.MaterialComponents.BottomAppBar.Surface}.
 *
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_backgroundTint
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabAlignmentMode
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabAnimationMode
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleMargin
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleRoundedCornerRadius
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleVerticalOffset
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_hideOnScroll
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_paddingBottomSystemWindowInsets
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
  private final MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();

  @Nullable private Animator modeAnimator;
  @Nullable private Animator menuAnimator;
  @FabAlignmentMode private int fabAlignmentMode;
  @FabAnimationMode private int fabAnimationMode;
  private boolean hideOnScroll;
  private final boolean paddingBottomSystemWindowInsets;
  private final boolean paddingLeftSystemWindowInsets;
  private final boolean paddingRightSystemWindowInsets;

  /** Keeps track of the number of currently running animations. */
  private int animatingModeChangeCounter = 0;

  private ArrayList<AnimationListener> animationListeners;

  /**
   * Track whether or not a new menu will be inflated along with a FAB alignment change. The
   * inflation of the resource is deferred until an appropriate time during the FAB alignment/menu
   * animation before being set and clearing this pending resource.
   */
  private static final int NO_MENU_RES_ID = 0;

  @MenuRes private int pendingMenuResId = NO_MENU_RES_ID;
  private boolean menuAnimatingWithFabAlignmentMode = false;

  /** Callback to be invoked when the BottomAppBar is animating. */
  interface AnimationListener {
    void onAnimationStart(BottomAppBar bar);

    void onAnimationEnd(BottomAppBar bar);
  }

  /**
   * If the {@link FloatingActionButton} is actually cradled in the {@link BottomAppBar} or if the
   * {@link FloatingActionButton} is detached which will happen when the {@link
   * FloatingActionButton} is not visible, or when the {@link BottomAppBar} is scrolled off the
   * screen.
   */
  private boolean fabAttached = true;

  private Behavior behavior;

  private int bottomInset;
  private int rightInset;
  private int leftInset;

  /**
   * Listens to the FABs hide or show animation to kick off an animation on BottomAppBar that reacts
   * to the change.
   */
  @NonNull
  AnimatorListenerAdapter fabAnimationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
          // If the FAB has begun to animate as a result of the FAB alignment mode changing, the FAB
          // alignment animation will handle coordination of menu animation and this should be
          // skipped. If the FAB has begun to animate as a result of hiding/showing the FAB, the
          // menu animation should be cancelled and restarted.
          if (!menuAnimatingWithFabAlignmentMode) {
            maybeAnimateMenuView(fabAlignmentMode, fabAttached);
          }
        }
      };

  /** Listens to any transformations applied to the FAB so the cutout can react. */
  @NonNull
  TransformationCallback<FloatingActionButton> fabTransformationCallback =
      new TransformationCallback<FloatingActionButton>() {
        @Override
        public void onScaleChanged(@NonNull FloatingActionButton fab) {
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() == View.VISIBLE ? fab.getScaleY() : 0);
        }

        @Override
        public void onTranslationChanged(@NonNull FloatingActionButton fab) {
          float horizontalOffset = fab.getTranslationX();
          if (getTopEdgeTreatment().getHorizontalOffset() != horizontalOffset) {
            getTopEdgeTreatment().setHorizontalOffset(horizontalOffset);
            materialShapeDrawable.invalidateSelf();
          }

          // If the translation of the fab has changed, update the vertical offset.
          float verticalOffset = Math.max(0, -fab.getTranslationY());
          if (getTopEdgeTreatment().getCradleVerticalOffset() != verticalOffset) {
            getTopEdgeTreatment().setCradleVerticalOffset(verticalOffset);
            materialShapeDrawable.invalidateSelf();
          }
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() == View.VISIBLE ? fab.getScaleY() : 0);
        }
      };

  public BottomAppBar(@NonNull Context context) {
    this(context, null, 0);
  }

  public BottomAppBar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomAppBarStyle);
  }

  public BottomAppBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.BottomAppBar, defStyleAttr, DEF_STYLE_RES);

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(context, a, R.styleable.BottomAppBar_backgroundTint);

    int elevation = a.getDimensionPixelSize(R.styleable.BottomAppBar_elevation, 0);
    float fabCradleMargin = a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleMargin, 0);
    float fabCornerRadius =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleRoundedCornerRadius, 0);
    float fabVerticalOffset =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleVerticalOffset, 0);
    fabAlignmentMode =
        a.getInt(R.styleable.BottomAppBar_fabAlignmentMode, FAB_ALIGNMENT_MODE_CENTER);
    fabAnimationMode =
        a.getInt(R.styleable.BottomAppBar_fabAnimationMode, FAB_ANIMATION_MODE_SCALE);
    hideOnScroll = a.getBoolean(R.styleable.BottomAppBar_hideOnScroll, false);
    // Reading out if we are handling bottom padding, so we can apply it to the FAB.
    paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingBottomSystemWindowInsets, false);
    paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingLeftSystemWindowInsets, false);
    paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingRightSystemWindowInsets, false);

    a.recycle();

    fabOffsetEndMode =
        getResources().getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fabOffsetEndMode);

    EdgeTreatment topEdgeTreatment =
        new BottomAppBarTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder().setTopEdge(topEdgeTreatment).build();
    materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    materialShapeDrawable.setShadowCompatibilityMode(SHADOW_COMPAT_MODE_ALWAYS);
    materialShapeDrawable.setPaintStyle(Style.FILL);
    materialShapeDrawable.initializeElevationOverlay(context);
    setElevation(elevation);
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint);
    ViewCompat.setBackground(this, materialShapeDrawable);

    ViewUtils.doOnApplyWindowInsets(
        this,
        attrs,
        defStyleAttr,
        DEF_STYLE_RES,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            // Just read the insets here. doOnApplyWindowInsets will apply the padding under the
            // hood.
            boolean leftInsetsChanged = false;
            boolean rightInsetsChanged = false;
            if (paddingBottomSystemWindowInsets) {
              bottomInset = insets.getSystemWindowInsetBottom();
            }
            if (paddingLeftSystemWindowInsets) {
              leftInsetsChanged = leftInset != insets.getSystemWindowInsetLeft();
              leftInset = insets.getSystemWindowInsetLeft();
            }
            if (paddingRightSystemWindowInsets) {
              rightInsetsChanged = rightInset != insets.getSystemWindowInsetRight();
              rightInset = insets.getSystemWindowInsetRight();
            }

            // We may need to change the position of the cutout or the action menu if the side
            // insets have changed.
            if (leftInsetsChanged || rightInsetsChanged) {
              cancelAnimations();

              setCutoutState();
              setActionMenuViewPosition();
            }

            return insets;
          }
        });
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
   * Sets the current {@code fabAlignmentMode}. An animated transition between current and desired
   * modes will be played.
   *
   * @param fabAlignmentMode the desired fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER}
   *     or {@link #FAB_ALIGNMENT_MODE_END}.
   */
  public void setFabAlignmentMode(@FabAlignmentMode int fabAlignmentMode) {
    setFabAlignmentModeAndReplaceMenu(fabAlignmentMode, NO_MENU_RES_ID);
  }

  /**
   * Sets the current {@code fabAlignmentMode} and replaces the {@code BottomAppBar}'s menu
   * resource. An animated transition between the current and desired mode will be played in
   * coordination with a menu resource swap animation.
   *
   * @param fabAlignmentMode the desired fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER}
   *     or {@link #FAB_ALIGNMENT_MODE_END}.
   * @param newMenu the menu resource of a new menu to be inflated and swapped during the animation.
   *     Passing 0 for newMenu will not clear the menu but will skip all menu manipulation. If you'd
   *     like to animate the FAB's alignment and clear the menu at the same time, use {@code
   *     getMenu().clear()} and {@link #setFabAlignmentMode(int)}.
   */
  public void setFabAlignmentModeAndReplaceMenu(
      @FabAlignmentMode int fabAlignmentMode, @MenuRes int newMenu) {
    this.pendingMenuResId = newMenu;
    this.menuAnimatingWithFabAlignmentMode = true;
    maybeAnimateMenuView(fabAlignmentMode, fabAttached);
    maybeAnimateModeChange(fabAlignmentMode);
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
    return getTopEdgeTreatment().getFabCradleMargin();
  }

  /**
   * Sets the cradle margin for the fab cutout. This is the space between the fab and the cutout.
   */
  public void setFabCradleMargin(@Dimension float cradleMargin) {
    if (cradleMargin != getFabCradleMargin()) {
      getTopEdgeTreatment().setFabCradleMargin(cradleMargin);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /** Returns the rounded corner radius for the cutout. A value of 0 will be a sharp edge. */
  @Dimension
  public float getFabCradleRoundedCornerRadius() {
    return getTopEdgeTreatment().getFabCradleRoundedCornerRadius();
  }

  /** Sets the rounded corner radius for the fab cutout. A value of 0 will be a sharp edge. */
  public void setFabCradleRoundedCornerRadius(@Dimension float roundedCornerRadius) {
    if (roundedCornerRadius != getFabCradleRoundedCornerRadius()) {
      getTopEdgeTreatment().setFabCradleRoundedCornerRadius(roundedCornerRadius);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /**
   * Returns the vertical offset for the fab cutout. An offset of 0 indicates the vertical center of
   * the {@link FloatingActionButton} is positioned on the top edge.
   */
  @Dimension
  public float getCradleVerticalOffset() {
    return getTopEdgeTreatment().getCradleVerticalOffset();
  }

  /**
   * Sets the vertical offset, in pixels, of the {@link FloatingActionButton} being cradled. An
   * offset of 0 indicates the vertical center of the {@link FloatingActionButton} is positioned on
   * the top edge.
   */
  public void setCradleVerticalOffset(@Dimension float verticalOffset) {
    if (verticalOffset != getCradleVerticalOffset()) {
      getTopEdgeTreatment().setCradleVerticalOffset(verticalOffset);
      materialShapeDrawable.invalidateSelf();
      setCutoutState();
    }
  }

  /**
   * Returns true if the {@link BottomAppBar} should hide when a {@link
   * androidx.core.view.NestedScrollingChild} is scrolled. This is handled by {@link
   * BottomAppBar.Behavior}.
   */
  public boolean getHideOnScroll() {
    return hideOnScroll;
  }

  /**
   * Sets if the {@link BottomAppBar} should hide when a {@link
   * androidx.core.view.NestedScrollingChild} is scrolled. This is handled by {@link
   * BottomAppBar.Behavior}.
   */
  public void setHideOnScroll(boolean hide) {
    hideOnScroll = hide;
  }

  /** Animates the {@link BottomAppBar} so it hides off the screen. */
  public void performHide() {
    getBehavior().slideDown(this);
  }

  /** Animates the {@link BottomAppBar} so it is shown on the screen. */
  public void performShow() {
    getBehavior().slideUp(this);
  }

  @Override
  public void setElevation(float elevation) {
    materialShapeDrawable.setElevation(elevation);
    // Make sure the shadow isn't shown if this view slides down with hideOnScroll.
    int topShadowHeight =
        materialShapeDrawable.getShadowRadius() - materialShapeDrawable.getShadowOffsetY();
    getBehavior().setAdditionalHiddenOffsetY(this, topShadowHeight);
  }

  /**
   * A convenience method to replace the contents of the BottomAppBar's menu.
   *
   * @param newMenu the desired new menu.
   */
  public void replaceMenu(@MenuRes int newMenu) {
    if (newMenu != NO_MENU_RES_ID) {
      // Clear any pending menu changes if the menu being passed in happens to be pendingMenuResID.
      pendingMenuResId = NO_MENU_RES_ID;
      getMenu().clear();
      inflateMenu(newMenu);
    }
  }

  /** Add a listener to watch for animation changes to the BottomAppBar and FAB */
  void addAnimationListener(@NonNull AnimationListener listener) {
    if (animationListeners == null) {
      animationListeners = new ArrayList<>();
    }
    animationListeners.add(listener);
  }

  void removeAnimationListener(@NonNull AnimationListener listener) {
    if (animationListeners == null) {
      return;
    }
    animationListeners.remove(listener);
  }

  private void dispatchAnimationStart() {
    if (animatingModeChangeCounter++ == 0 && animationListeners != null) {
      // Only dispatch the starting event if there are 0 running animations before this one starts.
      for (AnimationListener listener : animationListeners) {
        listener.onAnimationStart(this);
      }
    }
  }

  private void dispatchAnimationEnd() {
    if (--animatingModeChangeCounter == 0 && animationListeners != null) {
      // Only dispatch the ending event if there are 0 running animations after this one ends.
      for (AnimationListener listener : animationListeners) {
        listener.onAnimationEnd(this);
      }
    }
  }

  /**
   * Sets the fab diameter. This will be called automatically by the {@link BottomAppBar.Behavior}
   * if the fab is anchored to this {@link BottomAppBar}.
   */
  boolean setFabDiameter(@Px int diameter) {
    if (diameter != getTopEdgeTreatment().getFabDiameter()) {
      getTopEdgeTreatment().setFabDiameter(diameter);
      materialShapeDrawable.invalidateSelf();
      return true;
    }

    return false;
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
          public void onAnimationStart(Animator animation) {
            dispatchAnimationStart();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            dispatchAnimationEnd();
            modeAnimator = null;
          }
        });
    modeAnimator.start();
  }

  @Nullable
  private FloatingActionButton findDependentFab() {
    View view = findDependentView();
    return view instanceof FloatingActionButton ? (FloatingActionButton) view : null;
  }

  @Nullable
  private View findDependentView() {
    if (!(getParent() instanceof CoordinatorLayout)) {
      // If we aren't in a CoordinatorLayout we won't have a dependent FAB.
      return null;
    }

    List<View> dependents = ((CoordinatorLayout) getParent()).getDependents(this);
    for (View v : dependents) {
      if (v instanceof FloatingActionButton || v instanceof ExtendedFloatingActionButton) {
        return v;
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

    dispatchAnimationStart();

    fab.hide(
        new OnVisibilityChangedListener() {
          @Override
          public void onHidden(@NonNull FloatingActionButton fab) {
            fab.setTranslationX(getFabTranslationX(targetMode));
            fab.show(
                new OnVisibilityChangedListener() {
                  @Override
                  public void onShown(FloatingActionButton fab) {
                    dispatchAnimationEnd();
                  }
                });
          }
        });
  }

  private void createFabTranslationXAnimation(
      @FabAlignmentMode int targetMode, @NonNull List<Animator> animators) {
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(findDependentFab(), "translationX", getFabTranslationX(targetMode));
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
  }

  private void maybeAnimateMenuView(@FabAlignmentMode int targetMode, boolean newFabAttached) {
    if (!ViewCompat.isLaidOut(this)) {
      menuAnimatingWithFabAlignmentMode = false;
      // If this method is called before the BottomAppBar is laid out and able to animate, make sure
      // the desired menu is still set even if the animation is skipped.
      replaceMenu(pendingMenuResId);
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
          public void onAnimationStart(Animator animation) {
            dispatchAnimationStart();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            dispatchAnimationEnd();
            menuAnimatingWithFabAlignmentMode = false;
            menuAnimator = null;
          }
        });
    menuAnimator.start();
  }

  private void createMenuViewTranslationAnimation(
      @FabAlignmentMode final int targetMode,
      final boolean targetAttached,
      @NonNull List<Animator> animators) {

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
                boolean replaced = pendingMenuResId != NO_MENU_RES_ID;
                replaceMenu(pendingMenuResId);
                translateActionMenuView(actionMenuView, targetMode, targetAttached, replaced);
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
    return -getTopEdgeTreatment().getCradleVerticalOffset();
  }

  private float getFabTranslationX(@FabAlignmentMode int fabAlignmentMode) {
    boolean isRtl = ViewUtils.isLayoutRtl(this);
    if (fabAlignmentMode == FAB_ALIGNMENT_MODE_END) {
      int systemEndInset = isRtl ? leftInset : rightInset;
      int totalEndInset = fabOffsetEndMode + systemEndInset;
      return (getMeasuredWidth() / 2 - totalEndInset) * (isRtl ? -1 : 1);
    } else {
      return 0;
    }
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

  private void translateActionMenuView(
      @NonNull final ActionMenuView actionMenuView,
      @FabAlignmentMode final int fabAlignmentMode,
      final boolean fabAttached) {
    translateActionMenuView(actionMenuView, fabAlignmentMode, fabAttached, false);
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
      @NonNull final ActionMenuView actionMenuView,
      @FabAlignmentMode final int fabAlignmentMode,
      final boolean fabAttached,
      boolean shouldWaitForMenuReplacement) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        actionMenuView.setTranslationX(
            getActionMenuViewTranslationX(actionMenuView, fabAlignmentMode, fabAttached));
      }
    };
    if (shouldWaitForMenuReplacement) {
      // Wait to ensure the actionMenuView has had it's menu inflated and is able to correctly
      // measure it's width before calculating and translating X.
      actionMenuView.post(runnable);
    } else {
      runnable.run();
    }
  }

  /**
   * Returns the X translation to position the {@link ActionMenuView}. When {@code fabAlignmentMode}
   * is equal to {@link #FAB_ALIGNMENT_MODE_END} and {@code fabAttached} is true, the {@link
   * ActionMenuView} will be aligned to the end of the navigation icon, otherwise the {@link
   * ActionMenuView} is not moved.
   */
  protected int getActionMenuViewTranslationX(
      @NonNull ActionMenuView actionMenuView,
      @FabAlignmentMode int fabAlignmentMode,
      boolean fabAttached) {
    if (fabAlignmentMode != FAB_ALIGNMENT_MODE_END || !fabAttached) {
      return 0;
    }

    boolean isRtl = ViewUtils.isLayoutRtl(this);
    int toolbarLeftContentEnd = isRtl ? getMeasuredWidth() : 0;

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
            isRtl
                ? Math.min(toolbarLeftContentEnd, view.getLeft())
                : Math.max(toolbarLeftContentEnd, view.getRight());
      }
    }

    int actionMenuViewStart = isRtl ? actionMenuView.getRight() : actionMenuView.getLeft();
    int systemStartInset = isRtl ? rightInset : -leftInset;
    int end = actionMenuViewStart + systemStartInset;

    return toolbarLeftContentEnd - end;
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

    // Always ensure the MenuView is in the correct position after a layout.
    setActionMenuViewPosition();
  }

  @NonNull
  private BottomAppBarTopEdgeTreatment getTopEdgeTreatment() {
    return (BottomAppBarTopEdgeTreatment)
        materialShapeDrawable.getShapeAppearanceModel().getTopEdge();
  }

  private void setCutoutState() {
    // Layout all elements related to the positioning of the fab.
    getTopEdgeTreatment().setHorizontalOffset(getFabTranslationX());
    View fab = findDependentView();
    materialShapeDrawable.setInterpolation(fabAttached && isFabVisibleOrWillBeShown() ? 1 : 0);
    if (fab != null) {
      fab.setTranslationY(getFabTranslationY());
      fab.setTranslationX(getFabTranslationX());
    }
  }

  private void setActionMenuViewPosition() {
    ActionMenuView actionMenuView = getActionMenuView();
    // If the menu is null there is no need to translate it. If the menu is currently being
    // animated, the menuAnimator will take care of re-positioning the menu if necessary.
    if (actionMenuView != null && menuAnimator == null) {
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
    fab.addTransformationCallback(fabTransformationCallback);
  }

  private int getBottomInset() {
    return bottomInset;
  }

  private int getRightInset() {
    return rightInset;
  }

  private int getLeftInset() {
    return leftInset;
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
  public Behavior getBehavior() {
    if (behavior == null) {
      behavior = new Behavior();
    }
    return behavior;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, materialShapeDrawable);

    // Automatically don't clip children for the parent view of BottomAppBar. This allows the shadow
    // to be drawn outside the bounds.
    if (getParent() instanceof ViewGroup) {
      ((ViewGroup) getParent()).setClipChildren(false);
    }
  }

  /**
   * Behavior designed for use with {@link BottomAppBar} instances. Its main function is to link a
   * dependent {@link FloatingActionButton} so that it can be shown docked in the cradle.
   */
  public static class Behavior extends HideBottomViewOnScrollBehavior<BottomAppBar> {

    @NonNull private final Rect fabContentRect;

    private WeakReference<BottomAppBar> viewRef;

    private int originalBottomMargin;

    private final OnLayoutChangeListener fabLayoutListener =
        new OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(
              View v,
              int left,
              int top,
              int right,
              int bottom,
              int oldLeft,
              int oldTop,
              int oldRight,
              int oldBottom) {
            BottomAppBar child = viewRef.get();

            // If the child BAB no longer exists, remove the listener.
            if (child == null || !(v instanceof FloatingActionButton)) {
              v.removeOnLayoutChangeListener(this);
              return;
            }

            FloatingActionButton fab = ((FloatingActionButton) v);

            fab.getMeasuredContentRect(fabContentRect);
            int height = fabContentRect.height();

            // Set the cutout diameter based on the height of the fab.
            child.setFabDiameter(height);

            CoordinatorLayout.LayoutParams fabLayoutParams =
                (CoordinatorLayout.LayoutParams) v.getLayoutParams();

            // Manage the bottomMargin of the fab if it wasn't explicitly set to something. This
            // adds space below the fab if the BottomAppBar is hidden.
            if (originalBottomMargin == 0) {
              // Extra padding is added for the fake shadow on API < 21. Ensure we don't add too
              // much space by removing that extra padding.
              int bottomShadowPadding = (fab.getMeasuredHeight() - height) / 2;
              int bottomMargin =
                  child
                      .getResources()
                      .getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fab_bottom_margin);
              // Should be moved above the bottom insets with space ignoring any shadow padding.
              int minBottomMargin = bottomMargin - bottomShadowPadding;
              fabLayoutParams.bottomMargin = child.getBottomInset() + minBottomMargin;
              fabLayoutParams.leftMargin = child.getLeftInset();
              fabLayoutParams.rightMargin = child.getRightInset();
              boolean isRtl = ViewUtils.isLayoutRtl(fab);
              if (isRtl) {
                fabLayoutParams.leftMargin += child.fabOffsetEndMode;
              } else {
                fabLayoutParams.rightMargin += child.fabOffsetEndMode;
              }
            }
          }
        };

    public Behavior() {
      fabContentRect = new Rect();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      fabContentRect = new Rect();
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent, @NonNull BottomAppBar child, int layoutDirection) {
      viewRef = new WeakReference<>(child);

      View dependentView = child.findDependentView();
      if (dependentView != null && !ViewCompat.isLaidOut(dependentView)) {
        // Set the initial position of the FloatingActionButton with the BottomAppBar vertical
        // offset.
        CoordinatorLayout.LayoutParams fabLayoutParams =
            (CoordinatorLayout.LayoutParams) dependentView.getLayoutParams();
        fabLayoutParams.anchorGravity = Gravity.CENTER | Gravity.TOP;

        // Keep track of the original bottom margin for the fab. We will manage the margin if
        // nothing was set.
        originalBottomMargin = fabLayoutParams.bottomMargin;

        if (dependentView instanceof FloatingActionButton) {
          FloatingActionButton fab = ((FloatingActionButton) dependentView);

          // Always update the BAB if the fab is laid out.
          fab.addOnLayoutChangeListener(fabLayoutListener);

          // Ensure the FAB is correctly linked to this BAB so the animations can run correctly
          child.addFabAnimationListeners(fab);
        }

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
  }

  @NonNull
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

    public SavedState(@NonNull Parcel in, ClassLoader loader) {
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
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
