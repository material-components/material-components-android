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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_NEVER;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.ViewCompat.ScrollAxis;
import androidx.core.view.WindowInsetsCompat;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.TransformationCallback;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior.OnScrollStateChangedListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.motion.MotionUtils;
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
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/BottomAppBar.md">component
 * developer guidance</a> and <a
 * href="https://material.io/components/bottom-app-bar/overview">design guidelines</a>.
 *
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_backgroundTint
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabAlignmentMode
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabAnchorMode
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabAnimationMode
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleMargin
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleRoundedCornerRadius
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_fabCradleVerticalOffset
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_hideOnScroll
 * @attr ref com.google.android.material.R.styleable#BottomAppBar_paddingBottomSystemWindowInsets
 */
public class BottomAppBar extends Toolbar implements AttachedBehavior {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_BottomAppBar;

  private static final int FAB_ALIGNMENT_ANIM_DURATION_DEFAULT = 300;
  private static final int FAB_ALIGNMENT_ANIM_DURATION_ATTR = R.attr.motionDurationLong2;
  private static final int FAB_ALIGNMENT_ANIM_EASING_ATTR =
      R.attr.motionEasingEmphasizedInterpolator;
  private static final float FAB_ALIGNMENT_ANIM_EASING_MIDPOINT = .2F;

  public static final int FAB_ALIGNMENT_MODE_CENTER = 0;
  public static final int FAB_ALIGNMENT_MODE_END = 1;

  /**
   * The fabAlignmentMode determines the horizontal positioning of the cradle and the FAB which can
   * be centered or aligned to the end.
   */
  @IntDef({FAB_ALIGNMENT_MODE_CENTER, FAB_ALIGNMENT_MODE_END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAlignmentMode {}

  /** The FAB is embedded inside the BottomAppBar. */
  public static final int FAB_ANCHOR_MODE_EMBED = 0;
  /** The FAB is cradled at the top of the BottomAppBar. */
  public static final int FAB_ANCHOR_MODE_CRADLE = 1;

  /**
   * The fabAnchorMode determines the placement of the FAB within the BottomAppBar. The FAB can be
   * cradled at the top of the BottomAppBar, or embedded within it.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({FAB_ANCHOR_MODE_EMBED, FAB_ANCHOR_MODE_CRADLE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAnchorMode {}

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

  /** The menu items are aligned automatically to avoid the FAB. */
  public static final int MENU_ALIGNMENT_MODE_AUTO = 0;
  /** The menu items are aligned to the start. */
  public static final int MENU_ALIGNMENT_MODE_START = 1;

  /**
   * The menuAlignmentMode determines the alignment of the menu items in the BottomAppBar.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({MENU_ALIGNMENT_MODE_AUTO, MENU_ALIGNMENT_MODE_START})
  @Retention(RetentionPolicy.SOURCE)
  public @interface MenuAlignmentMode {}

  @Nullable private Integer navigationIconTint;
  private final MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();

  @Nullable private Animator modeAnimator;
  @Nullable private Animator menuAnimator;
  @FabAlignmentMode private int fabAlignmentMode;
  @FabAnimationMode private int fabAnimationMode;
  @FabAnchorMode private int fabAnchorMode;

  /** No end margin for the FAB. */
  private static final int NO_FAB_END_MARGIN = -1;

  private final int fabOffsetEndMode;
  @Px private int fabAlignmentModeEndMargin;

  @MenuAlignmentMode private int menuAlignmentMode;
  private final boolean removeEmbeddedFabElevation;
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
              fab.getVisibility() == View.VISIBLE && fabAnchorMode == FAB_ANCHOR_MODE_CRADLE
                  ? fab.getScaleY()
                  : 0);
        }

        @Override
        public void onTranslationChanged(@NonNull FloatingActionButton fab) {
          if (fabAnchorMode != FAB_ANCHOR_MODE_CRADLE) {
            return;
          }
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
    this(context, null);
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

    if (a.hasValue(R.styleable.BottomAppBar_navigationIconTint)) {
      setNavigationIconTint(a.getColor(R.styleable.BottomAppBar_navigationIconTint, -1));
    }

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
    fabAnchorMode = a.getInt(R.styleable.BottomAppBar_fabAnchorMode, FAB_ANCHOR_MODE_CRADLE);
    removeEmbeddedFabElevation =
        a.getBoolean(R.styleable.BottomAppBar_removeEmbeddedFabElevation, true);

    menuAlignmentMode =
        a.getInt(R.styleable.BottomAppBar_menuAlignmentMode, MENU_ALIGNMENT_MODE_AUTO);
    hideOnScroll = a.getBoolean(R.styleable.BottomAppBar_hideOnScroll, false);
    // Reading out if we are handling bottom padding, so we can apply it to the FAB.
    paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingBottomSystemWindowInsets, false);
    paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingLeftSystemWindowInsets, false);
    paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingRightSystemWindowInsets, false);
    fabAlignmentModeEndMargin =
        a.getDimensionPixelOffset(
            R.styleable.BottomAppBar_fabAlignmentModeEndMargin, NO_FAB_END_MARGIN);

    boolean addElevationShadow = a.getBoolean(R.styleable.BottomAppBar_addElevationShadow, true);
    a.recycle();

    fabOffsetEndMode =
        getResources().getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fabOffsetEndMode);

    EdgeTreatment topEdgeTreatment =
        new BottomAppBarTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder().setTopEdge(topEdgeTreatment).build();
    materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    if (addElevationShadow) {
      materialShapeDrawable.setShadowCompatibilityMode(
          MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
    } else {
      materialShapeDrawable.setShadowCompatibilityMode(SHADOW_COMPAT_MODE_NEVER);
      if (VERSION.SDK_INT >= VERSION_CODES.P) {
        setOutlineAmbientShadowColor(Color.TRANSPARENT);
        setOutlineSpotShadowColor(Color.TRANSPARENT);
      }
    }
    materialShapeDrawable.setPaintStyle(Style.FILL);
    materialShapeDrawable.initializeElevationOverlay(context);
    materialShapeDrawable.setTintList(backgroundTint);
    setElevation(elevation);
    setBackground(materialShapeDrawable);

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

              setCutoutStateAndTranslateFab();
              setActionMenuViewPosition();
            }

            return insets;
          }
        });
  }

  @Override
  public void setNavigationIcon(@Nullable Drawable drawable) {
    super.setNavigationIcon(maybeTintNavigationIcon(drawable));
  }

  /**
   * Sets the color of the toolbar's navigation icon.
   *
   * @see #setNavigationIcon
   */
  public void setNavigationIconTint(@ColorInt int navigationIconTint) {
    this.navigationIconTint = navigationIconTint;
    Drawable navigationIcon = getNavigationIcon();
    if (navigationIcon != null) {
      // Causes navigation icon to be tinted if needed.
      setNavigationIcon(navigationIcon);
    }
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
   * Returns the current {@code fabAnchorMode}, either {@link #FAB_ANCHOR_MODE_CRADLE} or {@link
   * #FAB_ANCHOR_MODE_EMBED}.
   */
  @FabAnchorMode
  public int getFabAnchorMode() {
    return fabAnchorMode;
  }

  /**
   * Sets the current {@code fabAnchorMode}.
   *
   * @param fabAnchorMode the desired fabAnchorMode, either {@link #FAB_ANCHOR_MODE_CRADLE} or
   *     {@link #FAB_ANCHOR_MODE_EMBED}.
   */
  public void setFabAnchorMode(@FabAnchorMode int fabAnchorMode) {
    this.fabAnchorMode = fabAnchorMode;
    setCutoutStateAndTranslateFab();
    View fab = findDependentView();
    if (fab != null) {
      updateFabAnchorGravity(this, fab);
      fab.requestLayout();
      materialShapeDrawable.invalidateSelf();
    }
  }

  private static void updateFabAnchorGravity(BottomAppBar bar, View fab) {
    CoordinatorLayout.LayoutParams fabLayoutParams =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    fabLayoutParams.anchorGravity = Gravity.CENTER;
    if (bar.fabAnchorMode == FAB_ANCHOR_MODE_CRADLE) {
      fabLayoutParams.anchorGravity |= Gravity.TOP;
    }
    if (bar.fabAnchorMode == FAB_ANCHOR_MODE_EMBED) {
      fabLayoutParams.anchorGravity |= Gravity.BOTTOM;
    }
  }

  /**
   * Returns the current {@code fabAnimationMode}, either {@link #FAB_ANIMATION_MODE_SCALE} or
   * {@link #FAB_ANIMATION_MODE_SLIDE}.
   */
  @FabAnimationMode
  public int getFabAnimationMode() {
    return fabAnimationMode;
  }

  /**
   * Sets the current {@code fabAnimationMode}.
   *
   * @param fabAnimationMode the desired fabAlignmentMode, either {@link #FAB_ALIGNMENT_MODE_CENTER}
   *     or {@link #FAB_ALIGNMENT_MODE_END}.
   */
  public void setFabAnimationMode(@FabAnimationMode int fabAnimationMode) {
    this.fabAnimationMode = fabAnimationMode;
  }

  /**
   * Sets the current {@code menuAlignmentMode}. Determines where the menu items in the BottomAppBar
   * will be aligned.
   *
   * @param menuAlignmentMode the desired menuAlignmentMode, either {@link
   *     #MENU_ALIGNMENT_MODE_AUTO} or {@link #MENU_ALIGNMENT_MODE_START}.
   */
  public void setMenuAlignmentMode(@MenuAlignmentMode int menuAlignmentMode) {
    if (this.menuAlignmentMode != menuAlignmentMode) {
      this.menuAlignmentMode = menuAlignmentMode;
      ActionMenuView menu = getActionMenuView();
      if (menu != null) {
        translateActionMenuView(menu, fabAlignmentMode, isFabVisibleOrWillBeShown());
      }
    }
  }

  /**
   * Returns the current menuAlignmentMode, either {@link #MENU_ALIGNMENT_MODE_AUTO} or {@link
   * #MENU_ALIGNMENT_MODE_START}.
   */
  @MenuAlignmentMode
  public int getMenuAlignmentMode() {
    return menuAlignmentMode;
  }

  public void setBackgroundTint(@Nullable ColorStateList backgroundTint) {
    materialShapeDrawable.setTintList(backgroundTint);
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
   * Sets the cradle margin for the fab cutout.
   *
   * This is the space between the fab and the cutout. If
   * the fab anchor mode is not cradled, this will not be respected.
   */
  public void setFabCradleMargin(@Dimension float cradleMargin) {
    if (cradleMargin != getFabCradleMargin()) {
      getTopEdgeTreatment().setFabCradleMargin(cradleMargin);
      materialShapeDrawable.invalidateSelf();
    }
  }

  /**
   * Returns the rounded corner radius for the cutout if it exists. A value of 0 will be a
   * sharp edge.
   */
  @Dimension
  public float getFabCradleRoundedCornerRadius() {
    return getTopEdgeTreatment().getFabCradleRoundedCornerRadius();
  }

  /**
   * Sets the rounded corner radius for the fab cutout. A value of 0 will be a sharp edge.
   * This will not be visible until there is a cradle.
   */
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
   * This will not be visible until there is a cradle.
   */
  public void setCradleVerticalOffset(@Dimension float verticalOffset) {
    if (verticalOffset != getCradleVerticalOffset()) {
      getTopEdgeTreatment().setCradleVerticalOffset(verticalOffset);
      materialShapeDrawable.invalidateSelf();
      setCutoutStateAndTranslateFab();
    }
  }

  /**
   * Returns the {@link FloatingActionButton} end margin pixel offset for the fab if it is set.
   *
   * <p>An end margin of -1 indicates that the default margin will be used.
   */
  @Px
  public int getFabAlignmentModeEndMargin() {
    return fabAlignmentModeEndMargin;
  }

  /**
   * Sets the end margin, in pixels, of the {@link FloatingActionButton}. This will only have an
   * effect if the fab alignment mode is {@link #FAB_ALIGNMENT_MODE_END}.
   *
   * <p>An offset of -1 will use the default margin.
   */
  public void setFabAlignmentModeEndMargin(@Px int margin) {
    if (fabAlignmentModeEndMargin != margin) {
      fabAlignmentModeEndMargin = margin;
      setCutoutStateAndTranslateFab();
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
    performHide(/*animate=*/ true);
  }

  /**
   * Hides the {@link BottomAppBar}.
   *
   * @param animate {@code false} to hide the {@link BottomAppBar} immediately without animation.
   */
  public void performHide(boolean animate) {
    getBehavior().slideDown(this, animate);
  }

  /** Animates the {@link BottomAppBar} so it is shown on the screen. */
  public void performShow() {
    performShow(/*animate=*/ true);
  }

  /**
   * Shows the {@link BottomAppBar}.
   *
   * @param animate {@code false} to show the {@link BottomAppBar} immediately without animation.
   */
  public void performShow(boolean animate) {
    getBehavior().slideUp(this, animate);
  }

  /** Returns true if the {@link BottomAppBar} is scrolled down. */
  public boolean isScrolledDown() {
    return getBehavior().isScrolledDown();
  }

  /** Returns true if the {@link BottomAppBar} is scrolled up. */
  public boolean isScrolledUp() {
    return getBehavior().isScrolledUp();
  }

  /**
   * Add a listener that will be called when the bottom app bar scroll state changes.
   * See {@link OnScrollStateChangedListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnScrollStateChangedListener(OnScrollStateChangedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnScrollStateChangedListener(@NonNull OnScrollStateChangedListener listener) {
    getBehavior().addOnScrollStateChangedListener(listener);
  }

  /**
   * Remove a listener that was previously added via {@link
   * #addOnScrollStateChangedListener(OnScrollStateChangedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnScrollStateChangedListener(
      @NonNull OnScrollStateChangedListener listener) {
    getBehavior().removeOnScrollStateChangedListener(listener);
  }

  /** Remove all previously added {@link OnScrollStateChangedListener}s. */
  public void clearOnScrollStateChangedListeners() {
    getBehavior().clearOnScrollStateChangedListeners();
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
   * if the fab is anchored to this {@link BottomAppBar}..
   */
  boolean setFabDiameter(@Px int diameter) {
    if (diameter != getTopEdgeTreatment().getFabDiameter()) {
      getTopEdgeTreatment().setFabDiameter(diameter);
      materialShapeDrawable.invalidateSelf();
      return true;
    }

    return false;
  }

  void setFabCornerSize(@Dimension float radius) {
    if (radius != getTopEdgeTreatment().getFabCornerRadius()) {
      getTopEdgeTreatment().setFabCornerSize(radius);
      materialShapeDrawable.invalidateSelf();
    }
  }

  private void maybeAnimateModeChange(@FabAlignmentMode int targetMode) {
    if (fabAlignmentMode == targetMode || !isLaidOut()) {
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
    set.setInterpolator(
        MotionUtils.resolveThemeInterpolator(
            getContext(), FAB_ALIGNMENT_ANIM_EASING_ATTR, AnimationUtils.LINEAR_INTERPOLATOR));
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
    animator.setDuration(getFabAlignmentAnimationDuration());
    animators.add(animator);
  }

  private int getFabAlignmentAnimationDuration() {
    return MotionUtils.resolveThemeDuration(
        getContext(), FAB_ALIGNMENT_ANIM_DURATION_ATTR, FAB_ALIGNMENT_ANIM_DURATION_DEFAULT);
  }

  @Nullable
  private Drawable maybeTintNavigationIcon(@Nullable Drawable navigationIcon) {
    if (navigationIcon != null && navigationIconTint != null) {
      Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon.mutate());
      wrappedNavigationIcon.setTint(navigationIconTint);
      return wrappedNavigationIcon;
    } else {
      return navigationIcon;
    }
  }

  private void maybeAnimateMenuView(@FabAlignmentMode int targetMode, boolean newFabAttached) {
    if (!isLaidOut()) {
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

    final float animationDuration = getFabAlignmentAnimationDuration();
    Animator fadeIn = ObjectAnimator.ofFloat(actionMenuView, "alpha", 1);
    fadeIn.setDuration((long) (animationDuration * (1F - FAB_ALIGNMENT_ANIM_EASING_MIDPOINT)));

    float translationXDifference =
        actionMenuView.getTranslationX()
            - getActionMenuViewTranslationX(actionMenuView, targetMode, targetAttached);

    // If the MenuView has moved at least a pixel we will need to animate it.
    if (Math.abs(translationXDifference) > 1) {
      // We need to fade the MenuView out and in because it's position is changing
      Animator fadeOut = ObjectAnimator.ofFloat(actionMenuView, "alpha", 0);
      fadeOut.setDuration((long) (animationDuration * FAB_ALIGNMENT_ANIM_EASING_MIDPOINT));

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
      set.playSequentially(fadeOut, fadeIn);
      animators.add(set);
    } else if (actionMenuView.getAlpha() < 1) {
      // If the previous animation was cancelled in the middle and now we're deciding we don't need
      // fade the MenuView away and back in, we need to ensure the MenuView is visible
      animators.add(fadeIn);
    }
  }

  private float getFabTranslationY() {
    if (fabAnchorMode == FAB_ANCHOR_MODE_CRADLE) {
      return -getTopEdgeTreatment().getCradleVerticalOffset();
    }
    View fab = findDependentView();
    int translationY = 0;
    if (fab != null) {
      translationY = -(getMeasuredHeight() + getBottomInset() - fab.getMeasuredHeight()) / 2;
    }
    return translationY;
  }

  private float getFabTranslationX(@FabAlignmentMode int fabAlignmentMode) {
    boolean isRtl = ViewUtils.isLayoutRtl(this);
    if (fabAlignmentMode == FAB_ALIGNMENT_MODE_END) {
      View fab = findDependentView();
      int systemEndInset = isRtl ? leftInset : rightInset;
      int totalEndInset = systemEndInset;
      if (fabAlignmentModeEndMargin != NO_FAB_END_MARGIN && fab != null) {
        totalEndInset += fab.getMeasuredWidth() / 2 + fabAlignmentModeEndMargin;
      } else {
        // If no fab end margin is specified, it follows the previous behaviour of
        // translating by fabOffsetEndMode instead of a clear-cut margin.
        // This will result in a different padding for different FAB sizes.
        totalEndInset += fabOffsetEndMode;
      }
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
    Runnable runnable =
        new Runnable() {
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
    if (menuAlignmentMode != MENU_ALIGNMENT_MODE_START
        && (fabAlignmentMode != FAB_ALIGNMENT_MODE_END || !fabAttached)) {
      return 0;
    }

    boolean isRtl = ViewUtils.isLayoutRtl(this);
    int toolbarLeftContentEnd = isRtl ? getMeasuredWidth() : 0;

    // Calculate the inner side of the Toolbar's Gravity.START contents.
    for (int i = 0; i < getChildCount(); i++) {
      View view = getChildAt(i);
      boolean isAlignedToStart =
          view.getLayoutParams() instanceof LayoutParams
              && (((LayoutParams) view.getLayoutParams()).gravity
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
    // If there's no navigation icon, we want to add margin since we are translating the menu items
    // to the start.
    int marginStart = 0;
    if (getNavigationIcon() == null) {
      int horizontalMargin =
          getResources().getDimensionPixelOffset(R.dimen.m3_bottomappbar_horizontal_padding);
      marginStart = isRtl ? horizontalMargin : -horizontalMargin;
    }
    int end = actionMenuViewStart + systemStartInset + marginStart;

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

      setCutoutStateAndTranslateFab();
      // If the BAB layout has changed, we should alert the fab so that it can
      // adjust its margins accordingly.
      View dependentView = findDependentView();
      if (dependentView != null && dependentView.isLaidOut()) {
        dependentView.post(() -> dependentView.requestLayout());
      }
    }

    // Always ensure the MenuView is in the correct position after a layout.
    setActionMenuViewPosition();
  }

  @NonNull
  private BottomAppBarTopEdgeTreatment getTopEdgeTreatment() {
    return (BottomAppBarTopEdgeTreatment)
        materialShapeDrawable.getShapeAppearanceModel().getTopEdge();
  }

  private void setCutoutStateAndTranslateFab() {
    // Layout all elements related to the positioning of the fab.
    getTopEdgeTreatment().setHorizontalOffset(getFabTranslationX());
    materialShapeDrawable.setInterpolation(
        fabAttached && isFabVisibleOrWillBeShown() && fabAnchorMode == FAB_ANCHOR_MODE_CRADLE
            ? 1
            : 0);
    View fab = findDependentView();
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
            if (child == null
                || !(v instanceof FloatingActionButton
                    || v instanceof ExtendedFloatingActionButton)) {
              v.removeOnLayoutChangeListener(this);
              return;
            }

            int height = v.getHeight();
            if (v instanceof FloatingActionButton) {
              FloatingActionButton fab = ((FloatingActionButton) v);

              fab.getMeasuredContentRect(fabContentRect);

              height = fabContentRect.height();

              // Set the cutout diameter based on the height of the fab.
              child.setFabDiameter(height);

              // Assume symmetrical corners
              float cornerSize =
                  fab.getShapeAppearanceModel()
                      .getTopLeftCornerSize()
                      .getCornerSize(new RectF(fabContentRect));

              child.setFabCornerSize(cornerSize);
            }

            CoordinatorLayout.LayoutParams fabLayoutParams =
                (CoordinatorLayout.LayoutParams) v.getLayoutParams();

            // Manage the bottomMargin of the fab if it wasn't explicitly set to something. This
            // adds space below the fab if the BottomAppBar is hidden.
            if (originalBottomMargin == 0) {
              // Extra padding is added for the fake shadow on API < 21. Ensure we don't add too
              // much space by removing that extra padding if the fab mode is cradle.
              if (child.fabAnchorMode == FAB_ANCHOR_MODE_CRADLE) {
                int bottomShadowPadding = (v.getMeasuredHeight() - height) / 2;
                int bottomMargin =
                    child
                        .getResources()
                        .getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fab_bottom_margin);
                // Should be moved above the bottom insets with space ignoring any shadow padding.
                int minBottomMargin = bottomMargin - bottomShadowPadding;
                fabLayoutParams.bottomMargin = child.getBottomInset() + minBottomMargin;
              }
              fabLayoutParams.leftMargin = child.getLeftInset();
              fabLayoutParams.rightMargin = child.getRightInset();
              boolean isRtl = ViewUtils.isLayoutRtl(v);
              if (isRtl) {
                fabLayoutParams.leftMargin += child.fabOffsetEndMode;
              } else {
                fabLayoutParams.rightMargin += child.fabOffsetEndMode;
              }
            }
            child.setCutoutStateAndTranslateFab();
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
      if (dependentView != null && !dependentView.isLaidOut()) {
        // Set the initial position of the FloatingActionButton with the BottomAppBar vertical
        // offset.
        updateFabAnchorGravity(child, dependentView);

        // Keep track of the original bottom margin for the fab. We will manage the margin if
        // nothing was set.
        CoordinatorLayout.LayoutParams fabLayoutParams =
            (CoordinatorLayout.LayoutParams) dependentView.getLayoutParams();
        originalBottomMargin = fabLayoutParams.bottomMargin;

        if (dependentView instanceof FloatingActionButton) {
          FloatingActionButton fab = ((FloatingActionButton) dependentView);
          if (child.fabAnchorMode == FAB_ANCHOR_MODE_EMBED && child.removeEmbeddedFabElevation) {
            fab.setElevation(0);
            fab.setCompatElevation(0);
          }

          // TODO (b/185233196): Update to use FABs default animator with motion theming.
          // If there is no motion spec set on the anchored fab, set one which scales the fab to
          // zero so the top edge cutout will be properly animated out when the fab is hidden.
          if (fab.getShowMotionSpec() == null) {
            fab.setShowMotionSpecResource(R.animator.mtrl_fab_show_motion_spec);
          }
          if (fab.getHideMotionSpec() == null) {
            fab.setHideMotionSpecResource(R.animator.mtrl_fab_hide_motion_spec);
          }

          // Ensure the FAB is correctly linked to this BAB so the animations can run correctly
          child.addFabAnimationListeners(fab);
        }
        // Always update the BAB if the fab/efab is laid out.
        dependentView.addOnLayoutChangeListener(fabLayoutListener);

        // Move the fab to the correct position
        child.setCutoutStateAndTranslateFab();
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
