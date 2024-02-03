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

package com.google.android.material.appbar;

import com.google.android.material.R;

import static androidx.core.math.MathUtils.clamp;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.util.SeslMisc;
import androidx.coordinatorlayout.widget.AppBarLayoutBehavior;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowInsetsAnimation;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.reflect.content.res.SeslConfigurationReflector;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>SESL Variant</b><br><br>
 *
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of material
 * designs app bar concept, namely scrolling gestures.
 *
 * <p>Children should provide their desired scrolling behavior through {@link
 * LayoutParams#setScrollFlags(int)} and the associated layout xml attribute: {@code
 * app:layout_scrollFlags}.
 *
 * <p>This view depends heavily on being used as a direct child within a {@link CoordinatorLayout}.
 * If you use AppBarLayout within a different {@link ViewGroup}, most of its functionality will not
 * work.
 *
 * <p>AppBarLayout also requires a separate scrolling sibling in order to know when to scroll. The
 * binding is done through the {@link ScrollingViewBehavior} behavior class, meaning that you should
 * set your scrolling view's behavior to be an instance of {@link ScrollingViewBehavior}. A string
 * resource containing the full class name is available.
 *
 * <pre>
 * &lt;androidx.coordinatorlayout.widget.CoordinatorLayout
 *         xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *         xmlns:app=&quot;http://schemas.android.com/apk/res-auto&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;androidx.core.widget.NestedScrollView
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;match_parent&quot;
 *             app:layout_behavior=&quot;@string/appbar_scrolling_view_behavior&quot;&gt;
 *
 *         &lt;!-- Your scrolling content --&gt;
 *
 *     &lt;/androidx.core.widget.NestedScrollView&gt;
 *
 *     &lt;com.google.android.material.appbar.AppBarLayout
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:layout_width=&quot;match_parent&quot;&gt;
 *
 *         &lt;androidx.appcompat.widget.Toolbar
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *         &lt;com.google.android.material.tabs.TabLayout
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *     &lt;/com.google.android.material.appbar.AppBarLayout&gt;
 *
 * &lt;/androidx.coordinatorlayout.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/TopAppBar.md">component
 * developer guidance</a> and <a href="https://material.io/components/top-app-bar/overview">design
 * guidelines</a>.
 */
public class AppBarLayout extends LinearLayout implements CoordinatorLayout.AttachedBehavior,
    AppBarLayoutBehavior {

  //Sesl
  private static final String TAG = "AppBarLayout";
  private static final int TAPPABLE_ELEMENT = WindowInsetsCompat.Type.tappableElement();
  private static final int SYSTEM_BARS = WindowInsetsCompat.Type.systemBars();
  private static final float DEFAULT_HEIGHT_RATIO_TO_SCREEN = 0.39f;
  public static final int IMMERSIVE_DETACH_OPTION_SET_FIT_SYSTEM_WINDOW = 1;
  static final int PENDING_ACTION_COLLAPSED_IMM = 1 << 9;
  public static final int SESL_STATE_COLLAPSED = 0;
  public static final int SESL_STATE_EXPANDED = 1;
  public static final int SESL_STATE_HIDE = 2;
  public static final int SESL_STATE_IDLE = 3;
  private SeslAppbarState mAppbarState;
  private Drawable mBackground;
  private List<SeslBaseOnImmOffsetChangedListener> mImmOffsetListener;
  private WindowInsetsCompat mLastInsets = null;
  private Insets mLastSysInsets;
  private Insets mLastTappableInsets;
  private Resources mResources;
  private boolean isMouse = false;
  private int mBottomPadding = 0;
  private int mCurrentOrientation;
  private int mCurrentScreenHeight;
  private int mCustomHeight = -1;
  private int mImmersiveTopInset = 0;
  private int mSeslTCScrollRange = 0;
  private float mCollapsedHeight;
  private float mCustomHeightProportion;
  private float mHeightProportion;
  private boolean mImmHideStatusBar = false;
  private boolean mIsActivatedByUser = false;
  private boolean mIsActivatedImmersiveScroll = false;
  private boolean mIsCanScroll = false;
  private boolean mIsDetachedState = false;
  private boolean mIsReservedImmersiveDetachOption = false;
  private boolean mReservedFitSystemWindow = false;
  private boolean mRestoreAnim = false;
  private boolean mSetCustomHeight;
  private boolean mSetCustomProportion;
  private boolean mUseCollapsedHeight = false;
  private boolean mUseCustomHeight;
  private boolean mUseCustomPadding;

  public interface SeslBaseOnImmOffsetChangedListener<T extends AppBarLayout> {
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  public interface SeslOnImmOffsetChangedListener extends SeslBaseOnImmOffsetChangedListener<AppBarLayout> {
    void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
  }

  public static class SeslAppbarState {
    private int mCurrentState = SESL_STATE_IDLE;

    SeslAppbarState() { }

    void onStateChanged(int state) {
      mCurrentState = state;
    }

    public int getState() {
      return mCurrentState;
    }
  }
  // Sesl

  static final int PENDING_ACTION_NONE = 0x0;
  static final int PENDING_ACTION_EXPANDED = 0x1;
  static final int PENDING_ACTION_COLLAPSED = 1 << 1;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 1 << 2;
  static final int PENDING_ACTION_FORCE = 1 << 3;

  /**
   * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
   * offset changes.
   */
  // TODO(b/76413401): remove this base interface after the widget migration
  public interface BaseOnOffsetChangedListener<T extends AppBarLayout> {

    /**
     * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows child
     * views to implement custom behavior based on the offset (for instance pinning a view at a
     * certain y value).
     *
     * @param appBarLayout the {@link AppBarLayout} which offset has changed
     * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
     */
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  /**
   * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
   * offset changes.
   */
  // TODO(b/76413401): update this interface after the widget migration
  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
    @Override
    void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
  }

  /**
   * Definition for a callback to be invoked when the lift on scroll elevation and background color
   * change.
   */
  public interface LiftOnScrollListener {
    void onUpdate(@Dimension float elevation, @ColorInt int backgroundColor);
  }

  private static final int DEF_STYLE_RES = R.style.Widget_Design_AppBarLayout;
  private static final int INVALID_SCROLL_RANGE = -1;

  private int currentOffset;
  private int totalScrollRange = INVALID_SCROLL_RANGE;
  private int downPreScrollRange = INVALID_SCROLL_RANGE;
  private int downScrollRange = INVALID_SCROLL_RANGE;

  private boolean haveChildWithInterpolator;

  private int pendingAction = PENDING_ACTION_NONE;

  @Nullable private WindowInsetsCompat lastInsets;

  private List<BaseOnOffsetChangedListener> listeners;

  private boolean liftableOverride;
  private boolean liftable;
  private boolean lifted;

  private boolean liftOnScroll;
  @IdRes private int liftOnScrollTargetViewId;
  @Nullable private WeakReference<View> liftOnScrollTargetView;
  //  private final boolean hasLiftOnScrollColor;
//  @Nullable private ValueAnimator liftOnScrollColorAnimator;
//  @Nullable private AnimatorUpdateListener liftOnScrollColorUpdateListener;
  private final List<LiftOnScrollListener> liftOnScrollListeners = new ArrayList<>();
//
//  private final long liftOnScrollColorDuration;
//  private final TimeInterpolator liftOnScrollColorInterpolator;
  @Nullable private ValueAnimator elevationOverlayAnimator;

  private int[] tmpStatesArray;

  @Nullable private Drawable statusBarForeground;
//  @Nullable private Integer statusBarForegroundOriginalColor;

//  private final float appBarElevation;

  private Behavior behavior;

  public AppBarLayout(@NonNull Context context) {
    this(context, null);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.appBarLayoutStyle);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    setOrientation(VERTICAL);

    if (VERSION.SDK_INT >= 21) {
//      // Use the bounds view outline provider so that we cast a shadow, even without a
//      // background
//      if (getOutlineProvider() == ViewOutlineProvider.BACKGROUND) {
//        ViewUtilsLollipop.setBoundsViewOutlineProvider(this);
//      }

      // If we're running on API 21+, we should reset any state list animator from our
      // default style
      ViewUtilsLollipop.setStateListAnimatorFromAttrs(this, attrs, defStyleAttr, DEF_STYLE_RES);
    }

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.AppBarLayout, defStyleAttr, DEF_STYLE_RES);

//    ViewCompat.setBackground(this, a.getDrawable(R.styleable.AppBarLayout_android_background));
//
//    ColorStateList liftOnScrollColor =
//        MaterialResources.getColorStateList(context, a, R.styleable.AppBarLayout_liftOnScrollColor);
//    hasLiftOnScrollColor = liftOnScrollColor != null;
//
//    ColorStateList originalBackgroundColor = DrawableUtils.getColorStateListOrNull(getBackground());
//    if (originalBackgroundColor != null) {
//      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
//      materialShapeDrawable.setFillColor(originalBackgroundColor);
//      // If there is a lift on scroll color specified, we do not initialize the elevation overlay
//      // and set the alpha to zero manually.
//      if (liftOnScrollColor != null) {
//        initializeLiftOnScrollWithColor(
//            materialShapeDrawable, originalBackgroundColor, liftOnScrollColor);
//      } else {
//        initializeLiftOnScrollWithElevation(context, materialShapeDrawable);
//      }
//    }
//
//    liftOnScrollColorDuration = MotionUtils.resolveThemeDuration(context,
//        R.attr.motionDurationMedium2,
//        getResources().getInteger(R.integer.app_bar_elevation_anim_duration));
//    liftOnScrollColorInterpolator = MotionUtils.resolveThemeInterpolator(context,
//        R.attr.motionEasingStandardInterpolator, AnimationUtils.LINEAR_INTERPOLATOR);

    //Sesl
    mAppbarState = new SeslAppbarState();
    mResources = getResources();
    final boolean isLightTheme = SeslMisc.isLightTheme(context);

    if (a.hasValue(R.styleable.AppBarLayout_android_background)) {
      mBackground = a.getDrawable(R.styleable.AppBarLayout_android_background);
      ViewCompat.setBackground(this, mBackground);
    } else {
      mBackground = null;
      setBackgroundColor(mResources.getColor(isLightTheme
          ? R.color.sesl_action_bar_background_color_light : R.color.sesl_action_bar_background_color_dark));
    }

    ColorStateList backgroundColorStateList =
        DrawableUtils.getColorStateListOrNull(getBackground());
    if (backgroundColorStateList != null) {
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(backgroundColorStateList);
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }
    //sesl

    if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
      setExpanded(
          a.getBoolean(R.styleable.AppBarLayout_expanded, false),
          /* animate= */ false,
          /* force= */ false);
    }

    //Sesl
    if (a.hasValue(R.styleable.AppBarLayout_seslUseCustomHeight)) {
      mUseCustomHeight = a.getBoolean(R.styleable.AppBarLayout_seslUseCustomHeight, false);
    }

    if (a.hasValue(R.styleable.AppBarLayout_seslHeightProportion)) {
      mSetCustomProportion = true;
      mCustomHeightProportion = a.getFloat(R.styleable.AppBarLayout_seslHeightProportion, DEFAULT_HEIGHT_RATIO_TO_SCREEN);
    } else {
      mSetCustomProportion = false;
      mCustomHeightProportion = DEFAULT_HEIGHT_RATIO_TO_SCREEN;
    }

    mHeightProportion = ResourcesCompat.getFloat(mResources, R.dimen.sesl_appbar_height_proportion);

    if (a.hasValue(R.styleable.AppBarLayout_seslUseCustomPadding)) {
      mUseCustomPadding = a.getBoolean(R.styleable.AppBarLayout_seslUseCustomPadding, false);
    }

    if (mUseCustomPadding) {
      mBottomPadding = a.getDimensionPixelSize(R.styleable.AppBarLayout_android_paddingBottom, 0);
    } else {
      mBottomPadding = mResources.getDimensionPixelOffset(R.dimen.sesl_extended_appbar_bottom_padding);
    }
    setPadding(0, 0, 0, mBottomPadding);

    mCollapsedHeight = mResources.getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding) + mBottomPadding;
    seslSetCollapsedHeight(mCollapsedHeight, false);
    //sesl

    if (VERSION.SDK_INT >= 21 && a.hasValue(R.styleable.AppBarLayout_elevation)) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(
          this, a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0));
    }

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      // In O+, we have these values set in the style. Since there is no defStyleAttr for
      // AppBarLayout at the AppCompat level, check for these attributes here.
      if (a.hasValue(R.styleable.AppBarLayout_android_keyboardNavigationCluster)) {
        this.setKeyboardNavigationCluster(
            a.getBoolean(R.styleable.AppBarLayout_android_keyboardNavigationCluster, false));
      }
      if (a.hasValue(R.styleable.AppBarLayout_android_touchscreenBlocksFocus)) {
        this.setTouchscreenBlocksFocus(
            a.getBoolean(R.styleable.AppBarLayout_android_touchscreenBlocksFocus, false));
      }
    }

//    // TODO(b/249786834): This should be a customizable attribute.
//    appBarElevation = getResources().getDimension(R.dimen.design_appbar_elevation);

    liftOnScroll = a.getBoolean(R.styleable.AppBarLayout_liftOnScroll, false);
    liftOnScrollTargetViewId =
        a.getResourceId(R.styleable.AppBarLayout_liftOnScrollTargetViewId, View.NO_ID);

    setStatusBarForeground(a.getDrawable(R.styleable.AppBarLayout_statusBarForeground));
    a.recycle();

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            //Sesl
            Insets systemBarInsets = insets.getInsets(AppBarLayout.SYSTEM_BARS);
            Insets tappableInsets = insets.getInsets(AppBarLayout.TAPPABLE_ELEMENT);
            if (!tappableInsets.equals(mLastTappableInsets) || !systemBarInsets.equals(mLastSysInsets)) {
              Log.d(TAG, "[onApplyWindowInsets] sysInsets : " + systemBarInsets + ", tappableInsets : " + tappableInsets);
              if (getImmBehavior() != null && VERSION.SDK_INT >= VERSION_CODES.R) {
                getImmBehavior().notifyOnApplyWindowInsets();
              }
              mLastSysInsets = systemBarInsets;
              mLastTappableInsets = tappableInsets;
            }
            //sesl
            return onWindowInsetChanged(insets);
          }
        });
    //Sesl
    mCurrentOrientation = mResources.getConfiguration().orientation;
    mCurrentScreenHeight = mResources.getConfiguration().screenHeightDp;
    //sesl
  }

//  private void initializeLiftOnScrollWithColor(
//      MaterialShapeDrawable background,
//      @NonNull ColorStateList originalBackgroundColor,
//      @NonNull ColorStateList liftOnScrollColor) {
//    Integer colorSurface = MaterialColors.getColorOrNull(getContext(), R.attr.colorSurface);
//    liftOnScrollColorUpdateListener =
//        valueAnimator -> {
//          float liftProgress = (float) valueAnimator.getAnimatedValue();
//          int mixedColor =
//              MaterialColors.layer(
//                  originalBackgroundColor.getDefaultColor(),
//                  liftOnScrollColor.getDefaultColor(),
//                  liftProgress);
//          background.setFillColor(ColorStateList.valueOf(mixedColor));
//          if (statusBarForeground != null
//              && statusBarForegroundOriginalColor != null
//              && statusBarForegroundOriginalColor.equals(colorSurface)) {
//            DrawableCompat.setTint(statusBarForeground, mixedColor);
//          }
//
//          if (!liftOnScrollListeners.isEmpty()) {
//            for (LiftOnScrollListener liftOnScrollListener : liftOnScrollListeners) {
//              if (background.getFillColor() != null) {
//                liftOnScrollListener.onUpdate(0, mixedColor);
//              }
//            }
//          }
//        };
//    ViewCompat.setBackground(this, background);
//  }
//
//  private void initializeLiftOnScrollWithElevation(
//      Context context, MaterialShapeDrawable background) {
//    background.initializeElevationOverlay(context);
//    liftOnScrollColorUpdateListener = valueAnimator -> {
//      float elevation = (float) valueAnimator.getAnimatedValue();
//      background.setElevation(elevation);
//      if (statusBarForeground instanceof MaterialShapeDrawable) {
//        ((MaterialShapeDrawable) statusBarForeground).setElevation(elevation);
//      }
//      for (LiftOnScrollListener liftOnScrollListener : liftOnScrollListeners) {
//        liftOnScrollListener.onUpdate(elevation, background.getResolvedTintColor());
//      }
//    };
//    ViewCompat.setBackground(this, background);
//  }
//


  /**
   * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
   *
   * @param listener The listener that will be called when the offset changes.]
   * @see #removeOnOffsetChangedListener(OnOffsetChangedListener)
   */
  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    addOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  /**
   * Remove the previously added {@link OnOffsetChangedListener}.
   *
   * @param listener the listener to remove.
   */
  // TODO(b/76413401): change back to removeOnOffsetChangedListener once the widget migration is
  // finished since the shim class needs to implement this method.
  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners != null && listener != null) {
      listeners.remove(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    removeOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  /**
   * Add a {@link LiftOnScrollListener} that will be called when the lift on scroll elevation and
   * background color of this {@link AppBarLayout} change.
   */
  public void addLiftOnScrollListener(@NonNull LiftOnScrollListener liftOnScrollListener) {
    liftOnScrollListeners.add(liftOnScrollListener);
  }

  /** Remove a previously added {@link LiftOnScrollListener}. */
  public boolean removeLiftOnScrollListener(@NonNull LiftOnScrollListener liftOnScrollListener) {
    return liftOnScrollListeners.remove(liftOnScrollListener);
  }

  /** Remove all previously added {@link LiftOnScrollListener}s. */
  public void clearLiftOnScrollListener() {
    liftOnScrollListeners.clear();
  }


  /**
   * Set the drawable to use for the status bar foreground drawable. Providing null will disable the
   * scrim functionality.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForeground(@Nullable Drawable drawable) {
    if (statusBarForeground != drawable) {
      if (statusBarForeground != null) {
        statusBarForeground.setCallback(null);
      }
      statusBarForeground = drawable != null ? drawable.mutate() : null;
      //statusBarForegroundOriginalColor = extractStatusBarForegroundColor();
      if (statusBarForeground != null) {
        if (statusBarForeground.isStateful()) {
          statusBarForeground.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarForeground, ViewCompat.getLayoutDirection(this));
        statusBarForeground.setVisible(getVisibility() == VISIBLE, false);
        statusBarForeground.setCallback(this);
      }
      updateWillNotDraw();
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Set the color to use for the status bar foreground.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param color the color to display
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForegroundColor(@ColorInt int color) {
    setStatusBarForeground(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the status bar foreground from resources.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForegroundResource(@DrawableRes int resId) {
    setStatusBarForeground(AppCompatResources.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the status bar foreground.
   *
   * @see #setStatusBarForeground(Drawable)
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   */
  @Nullable
  public Drawable getStatusBarForeground() {
    return statusBarForeground;
  }

//  @Nullable
//  private Integer extractStatusBarForegroundColor() {
//    if (statusBarForeground instanceof MaterialShapeDrawable) {
//      return ((MaterialShapeDrawable) statusBarForeground).getResolvedTintColor();
//    }
//    ColorStateList statusBarForegroundColorStateList =
//        DrawableUtils.getColorStateListOrNull(statusBarForeground);
//    if (statusBarForegroundColorStateList != null) {
//      return statusBarForegroundColorStateList.getDefaultColor();
//    }
//    return null;
//  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    // Draw the status bar foreground drawable if we have a top inset
    if (shouldDrawStatusBarForeground()) {
      int saveCount = canvas.save();
      canvas.translate(0f, -currentOffset);
      statusBarForeground.draw(canvas);
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();

    Drawable d = statusBarForeground;
    if (d != null && d.isStateful() && d.setState(state)) {
      invalidateDrawable(d);
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == statusBarForeground;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarForeground != null) {
      statusBarForeground.setVisible(visible, false);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updateInternalHeight();//sesl
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    // If we're set to handle system windows but our first child is not, we need to add some
    // height to ourselves to pad the first child down below the status bar
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode != MeasureSpec.EXACTLY
        && ViewCompat.getFitsSystemWindows(this)
        && shouldOffsetFirstChild()) {
      int newHeight = getMeasuredHeight();
      switch (heightMode) {
        case MeasureSpec.AT_MOST:
          // For AT_MOST, we need to clamp our desired height with the max height
          newHeight =
              clamp(
                  getMeasuredHeight() + getTopInset(), 0, MeasureSpec.getSize(heightMeasureSpec));
          break;
        case MeasureSpec.UNSPECIFIED:
          // For UNSPECIFIED we can use any height so just add the top inset
          newHeight += getTopInset();
          break;
        case MeasureSpec.EXACTLY:
        default: // fall out
      }
      setMeasuredDimension(getMeasuredWidth(), newHeight);
    }

    invalidateScrollRanges();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
      // If we need to offset the first child, we need to offset all of them to make space
      final int topInset = getTopInset();
      for (int z = getChildCount() - 1; z >= 0; z--) {
        ViewCompat.offsetTopAndBottom(getChildAt(z), topInset);
      }
    }

    invalidateScrollRanges();

    haveChildWithInterpolator = false;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
      final Interpolator interpolator = childLp.getScrollInterpolator();

      if (interpolator != null) {
        haveChildWithInterpolator = true;
        break;
      }
    }

    if (statusBarForeground != null) {
      statusBarForeground.setBounds(0, 0, getWidth(), getTopInset());
    }

    // If the user has set liftable manually, don't set liftable state automatically.
    if (!liftableOverride) {
      setLiftableState(liftOnScroll || hasCollapsibleChild());
    }
  }

  private void updateWillNotDraw() {
    setWillNotDraw(!shouldDrawStatusBarForeground());
  }

  private boolean shouldDrawStatusBarForeground() {
    return statusBarForeground != null && getTopInset() > 0;
  }

  private boolean hasCollapsibleChild() {
    for (int i = 0, z = getChildCount(); i < z; i++) {
      if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
        return true;
      }
    }
    return false;
  }

  private void invalidateScrollRanges() {
    // Saves the current scrolling state when we need to recalculate scroll ranges
    // If the total scroll range is not known yet, the ABL is never scrolled.
    // If there's a pending action, we should skip this step and respect the pending action.
    BaseBehavior.SavedState savedState =
        behavior == null
            || totalScrollRange == INVALID_SCROLL_RANGE
            || pendingAction != PENDING_ACTION_NONE
            ? null : behavior.saveScrollState(AbsSavedState.EMPTY_STATE, this);
    // Invalidate the scroll ranges
    totalScrollRange = INVALID_SCROLL_RANGE;
    downPreScrollRange = INVALID_SCROLL_RANGE;
    downScrollRange = INVALID_SCROLL_RANGE;
//    // Restores the previous scrolling state. Don't override if there's a previously saved state
//    // which has not be restored yet. Multiple re-measuring can happen before the scroll state
//    // is actually restored. We don't want to restore the state in-between those re-measuring,
//    // since they can be incorrect.
//    if (savedState != null) {
//      behavior.restoreScrollState(savedState, false);
//    }
  }

  @Override
  public void setOrientation(int orientation) {
    if (orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "AppBarLayout is always vertical and does not support horizontal orientation");
    }
    super.setOrientation(orientation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mIsDetachedState = false;//sesl

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<AppBarLayout> getBehavior() {
    return new AppBarLayout.Behavior();
  }

  @Nullable
  public MaterialShapeDrawable getMaterialShapeBackground() {
    Drawable background = getBackground();
    return background instanceof MaterialShapeDrawable ? (MaterialShapeDrawable) background : null;
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  /**
   * Sets whether this {@link AppBarLayout} is expanded or not, animating if it has already been
   * laid out.
   *
   * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
   * child of a {@link CoordinatorLayout}.
   *
   * @param expanded true if the layout should be fully expanded, false if it should be fully
   *     collapsed
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
   */
  public void setExpanded(boolean expanded) {
    setExpanded(expanded, ViewCompat.isLaidOut(this));
  }

  /**
   * Sets whether this {@link AppBarLayout} is expanded or not.
   *
   * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
   * child of a {@link CoordinatorLayout}.
   *
   * @param expanded true if the layout should be fully expanded, false if it should be fully
   *     collapsed
   * @param animate Whether to animate to the new state
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
   */
  public void setExpanded(boolean expanded, boolean animate) {
    setExpanded(expanded, animate, true);
  }

  private void setExpanded(boolean expanded, boolean animate, boolean force) {
    setLifted(!expanded);//sesl
    pendingAction =
        (expanded ? PENDING_ACTION_EXPANDED : (seslGetImmersiveScroll()//sesl
            ? PENDING_ACTION_COLLAPSED_IMM : PENDING_ACTION_COLLAPSED))
            | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0)
            | (force ? PENDING_ACTION_FORCE : 0);
    requestLayout();
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
      return new LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) p);
    }
    return new LayoutParams(p);
  }

  @SuppressLint("NewApi")
  @Override
  protected void onDetachedFromWindow() {
    //Sesl
    SeslImmersiveScrollBehavior immBehavior = getImmBehavior();
    mIsDetachedState = true;
    if (mIsReservedImmersiveDetachOption
        && immBehavior != null && mReservedFitSystemWindow) {
      Log.i(TAG, "fits system window Immersive detached");
      immBehavior.setupDecorFitsSystemWindow(true);
    }
    //sesl
    super.onDetachedFromWindow();

    clearLiftOnScrollTargetView();
  }

  boolean hasChildWithInterpolator() {
    return haveChildWithInterpolator;
  }

  /**
   * Returns the scroll range of all children.
   *
   * @return the scroll range in px
   */
  public final int getTotalScrollRange() {
    if (totalScrollRange != INVALID_SCROLL_RANGE) {
      return totalScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        // Gone views should not be included in the scroll range calculation.
        continue;
      }
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        // We're set to scroll so add the child's height
        range += childHeight + lp.topMargin + lp.bottomMargin;

        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          // If this is the first child and it wants to handle system windows, we need to make
          // sure we don't scroll it past the inset
          range -= getTopInset();
        }
        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // For a collapsing scroll, we to take the collapsed height into account.
          // We also break straight away since later views can't scroll beneath
          // us
          if (getCanScroll()) {
            range += getTopInset() + mBottomPadding + seslGetTCScrollRange();//sesl
          } else {
            range -= ViewCompat.getMinimumHeight(child);
          }
          break;
        }
      } else {
        // As soon as a view doesn't have the scroll flag, we end the range calculation.
        // This is because views below can not scroll under a fixed view.
        break;
      }
    }
    return totalScrollRange = Math.max(0, range);
  }

  boolean hasScrollableChildren() {
    return getTotalScrollRange() != 0;
  }

  /** Return the scroll range when scrolling up from a nested pre-scroll. */
  int getUpNestedPreScrollRange() {
    return getTotalScrollRange();
  }

  /** Return the scroll range when scrolling down from a nested pre-scroll. */
  int getDownNestedPreScrollRange() {
    if (downPreScrollRange != INVALID_SCROLL_RANGE) {
      // If we already have a valid value, return it
      return downPreScrollRange;
    }

    int range = 0;
    for (int i = getChildCount() - 1; i >= 0; i--) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        // Gone views should not be included in the scroll range calculation.
        continue;
      }
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
        // First take the margin into account
        int childRange = lp.topMargin + lp.bottomMargin;
        // The view has the quick return flag combination...
        if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
          // If they're set to enter collapsed, use the minimum height
          childRange += ViewCompat.getMinimumHeight(child);
        } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // Only enter by the amount of the collapsed height
          childRange += childHeight - ViewCompat.getMinimumHeight(child);
        } else {
          // Else use the full height
          childRange += childHeight;
        }
        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          // If this is the first child and it wants to handle system windows, we need to make
          // sure we don't scroll past the inset
          childRange = Math.min(childRange, childHeight - getTopInset());
        }
        range += childRange;
      } else if (getCanScroll()) {//sesl
        range += seslGetCollapsedHeight() + seslGetTCScrollRange();//sesl
      }
    }
    return downPreScrollRange = Math.max(0, range);
  }

  /** Return the scroll range when scrolling down from a nested scroll. */
  int getDownNestedScrollRange() {
    if (downScrollRange != INVALID_SCROLL_RANGE) {
      // If we already have a valid value, return it
      return downScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      int childHeight = child.getMeasuredHeight();
      childHeight += lp.topMargin + lp.bottomMargin;

      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        // We're set to scroll so add the child's height
        range += childHeight;

        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // For a collapsing exit scroll, we to take the collapsed height into account.
          // We also break the range straight away since later views can't scroll
          // beneath us
          if (mIsCanScroll && child instanceof CollapsingToolbarLayout) {
            range -= ((CollapsingToolbarLayout) child).seslGetMinimumHeightWithoutMargin();//sesl
          } else {
            range -= ViewCompat.getMinimumHeight(child);
          }
          break;
        }
      } else {
        // As soon as a view doesn't have the scroll flag, we end the range calculation.
        // This is because views below can not scroll under a fixed view.
        break;
      }
    }
    return downScrollRange = Math.max(0, range);
  }

  void onOffsetChanged(int offset) {
    currentOffset = offset;

    //Sesl
    final int totalScrollRange = getTotalScrollRange();
    final int height = getHeight() - (int) seslGetCollapsedHeight();
    if (Math.abs(offset) >= totalScrollRange) {
      if (getCanScroll()) {
        if (mAppbarState.getState() != SESL_STATE_HIDE) {
          mAppbarState.onStateChanged(SESL_STATE_HIDE);
        }
      } else if (mAppbarState.getState() != SESL_STATE_COLLAPSED) {
        mAppbarState.onStateChanged(SESL_STATE_COLLAPSED);
      }
    } else if (Math.abs(offset) >= height) {
      if (mAppbarState.getState() != SESL_STATE_COLLAPSED) {
        mAppbarState.onStateChanged(SESL_STATE_COLLAPSED);
      }
    } else if (Math.abs(offset) == 0) {
      if (mAppbarState.getState() != SESL_STATE_EXPANDED) {
        mAppbarState.onStateChanged(SESL_STATE_EXPANDED);
      }
    } else if (mAppbarState.getState() != SESL_STATE_IDLE) {
      mAppbarState.onStateChanged(SESL_STATE_IDLE);
    }
    //sesl

    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

    // Iterate backwards through the list so that most recently added listeners
    // get the first chance to decide
    if (listeners != null) {
      for (int i = 0, z = listeners.size(); i < z; i++) {
        final BaseOnOffsetChangedListener listener = listeners.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }

  public final int getMinimumHeightForVisibleOverlappingContent() {
    final int topInset = getTopInset();
    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight != 0) {
      // If this layout has a min height, use it (doubled)
      return (minHeight * 2) + topInset;
    }

    // Otherwise, we'll use twice the min height of our last child
    final int childCount = getChildCount();
    final int lastChildMinHeight =
        childCount >= 1 ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) : 0;
    if (lastChildMinHeight != 0) {
      return (lastChildMinHeight * 2) + topInset;
    }

    // If we reach here then we don't have a min height explicitly set. Instead we'll take a
    // guess at 1/3 of our height being visible
    return getHeight() / 3;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (tmpStatesArray == null) {
      // Note that we can't allocate this at the class level (in declaration) since some paths in
      // super View constructor are going to call this method before that
      tmpStatesArray = new int[4];
    }
    final int[] extraStates = tmpStatesArray;
    final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

    extraStates[0] = liftable ? R.attr.state_liftable : -R.attr.state_liftable;
    extraStates[1] = liftable && lifted ? R.attr.state_lifted : -R.attr.state_lifted;

    // Note that state_collapsible and state_collapsed are deprecated. This is to keep compatibility
    // with existing state list animators that depend on these states.
    extraStates[2] = liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
    extraStates[3] = liftable && lifted ? R.attr.state_collapsed : -R.attr.state_collapsed;

    return mergeDrawableStates(states, extraStates);
  }

  /**
   * Sets whether the {@link AppBarLayout} is liftable or not.
   *
   * @return true if the liftable state changed
   */
  public boolean setLiftable(boolean liftable) {
    this.liftableOverride = true;
    return setLiftableState(liftable);
  }

  // Internal helper method that updates liftable state without enabling the override.
  private boolean setLiftableState(boolean liftable) {
    if (this.liftable != liftable) {
      this.liftable = liftable;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  /**
   * Sets whether the {@link AppBarLayout} is in a lifted state or not.
   *
   * @return true if the lifted state changed
   */
  public boolean setLifted(boolean lifted) {
    return setLiftedState(lifted);
  }

  /** Returns whether the {@link AppBarLayout} is in a lifted state or not. */
  public boolean isLifted() {
    return lifted;
  }

  // Internal helper method that updates lifted state.
  boolean setLiftedState(boolean lifted) {
    if (this.lifted != lifted) {
      this.lifted = lifted;
      refreshDrawableState();
      if (liftOnScroll && getBackground() instanceof MaterialShapeDrawable) {
        startLiftOnScrollElevationOverlayAnimation((MaterialShapeDrawable) getBackground(), lifted);
      }
      return true;
    }
    return false;
  }

  private void startLiftOnScrollElevationOverlayAnimation(
      @NonNull final MaterialShapeDrawable background, boolean lifted) {
    float appBarElevation = mResources.getDimension(R.dimen.sesl_appbar_elevation);
    float fromElevation = lifted ? 0 : appBarElevation;
    float toElevation = lifted ? appBarElevation : 0;

    if (elevationOverlayAnimator != null) {
      elevationOverlayAnimator.cancel();
    }

    elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation);
    elevationOverlayAnimator.setDuration(
        mResources.getInteger(R.integer.app_bar_elevation_anim_duration));
    elevationOverlayAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    elevationOverlayAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            float elevation = (float) valueAnimator.getAnimatedValue();
            background.setElevation(elevation);
            if (statusBarForeground instanceof MaterialShapeDrawable) {
              ((MaterialShapeDrawable) statusBarForeground).setElevation(elevation);
            }
            for (LiftOnScrollListener liftOnScrollListener : liftOnScrollListeners) {
              liftOnScrollListener.onUpdate(elevation, background.getResolvedTintColor());
            }
          }
        });
    elevationOverlayAnimator.start();
  }

  /**
   * Sets whether the {@link AppBarLayout} lifts on scroll or not.
   *
   * <p>If set to true, the {@link AppBarLayout} will animate to the lifted, or elevated, state when
   * content is scrolled beneath it. Requires
   * `app:layout_behavior="@string/appbar_scrolling_view_behavior` to be set on the scrolling
   * sibling (e.g., `NestedScrollView`, `RecyclerView`, etc.).
   */
  public void setLiftOnScroll(boolean liftOnScroll) {
    this.liftOnScroll = liftOnScroll;
  }

  /** Returns whether the {@link AppBarLayout} lifts on scroll or not. */
  public boolean isLiftOnScroll() {
    return liftOnScroll;
  }

  /**
   * Sets the id of the view that the {@link AppBarLayout} should use to determine whether it should
   * be lifted.
   */
  public void setLiftOnScrollTargetViewId(@IdRes int liftOnScrollTargetViewId) {
    this.liftOnScrollTargetViewId = liftOnScrollTargetViewId;
    // Invalidate cached target view so it will be looked up on next scroll.
    clearLiftOnScrollTargetView();
  }

  /**
   * Returns the id of the view that the {@link AppBarLayout} should use to determine whether it
   * should be lifted.
   */
  @IdRes
  public int getLiftOnScrollTargetViewId() {
    return liftOnScrollTargetViewId;
  }

  boolean shouldLift(@Nullable View defaultScrollingView) {
    View scrollingView = findLiftOnScrollTargetView(defaultScrollingView);
    if (scrollingView == null) {
      scrollingView = defaultScrollingView;
    }
    return scrollingView != null
        && (scrollingView.canScrollVertically(-1) || scrollingView.getScrollY() > 0);
  }

  @Nullable
  private View findLiftOnScrollTargetView(@Nullable View defaultScrollingView) {
    if (liftOnScrollTargetView == null && liftOnScrollTargetViewId != View.NO_ID) {
      View targetView = null;
      if (defaultScrollingView != null) {
        targetView = defaultScrollingView.findViewById(liftOnScrollTargetViewId);
      }
      if (targetView == null && getParent() instanceof ViewGroup) {
        // Assumes the scrolling view is a child of the AppBarLayout's parent,
        // which should be true due to the CoordinatorLayout pattern.
        targetView = ((ViewGroup) getParent()).findViewById(liftOnScrollTargetViewId);
      }
      if (targetView != null) {
        liftOnScrollTargetView = new WeakReference<>(targetView);
      }
    }
    return liftOnScrollTargetView != null ? liftOnScrollTargetView.get() : null;
  }

  private void clearLiftOnScrollTargetView() {
    if (liftOnScrollTargetView != null) {
      liftOnScrollTargetView.clear();
    }
    liftOnScrollTargetView = null;
  }

  /**
   * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
   *     a {@link android.animation.StateListAnimator}. If a target elevation is set, either by this
   *     method or the {@code app:elevation} attribute, a new state list animator is created which
   *     uses the given {@code elevation} value.
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_elevation
   */
  @Deprecated
  public void setTargetElevation(float elevation) {
    if (Build.VERSION.SDK_INT >= 21) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, elevation);
    }
  }

  /**
   * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
   *     a {@link android.animation.StateListAnimator}. This method now always returns 0.
   */
  @Deprecated
  public float getTargetElevation() {
    return 0;
  }

  int getPendingAction() {
    return pendingAction;
  }

  void resetPendingAction() {
    pendingAction = PENDING_ACTION_NONE;
  }

  @VisibleForTesting
  final int getTopInset() {
    return lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
  }

  /**
   * Whether the first child needs to be offset because it does not want to handle the top window
   * inset
   */
  private boolean shouldOffsetFirstChild() {
    if (getChildCount() > 0) {
      final View firstChild = getChildAt(0);
      return firstChild.getVisibility() != GONE && !ViewCompat.getFitsSystemWindows(firstChild);
    }
    return false;
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;
    }

    // If our insets have changed, keep them and trigger a layout...
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      updateWillNotDraw();
      requestLayout();
    }

    return insets;
  }


  //Sesl
  void onImmOffsetChanged(int offset) {
    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

    if (mImmOffsetListener != null) {
      for (int i = 0; i < mImmOffsetListener.size(); i++) {
        final SeslBaseOnImmOffsetChangedListener listener = mImmOffsetListener.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }


  @RequiresApi(30)
  public void seslReserveImmersiveDetachOption(int flag) {
    if (flag != 0) {
      mIsReservedImmersiveDetachOption = true;
      mReservedFitSystemWindow
          = (flag & IMMERSIVE_DETACH_OPTION_SET_FIT_SYSTEM_WINDOW) != 0;
    } else {
      mIsReservedImmersiveDetachOption = false;
    }
  }

  public void seslRemoveOnImmOffsetChangedListener(SeslBaseOnImmOffsetChangedListener listener) {
    if (mImmOffsetListener != null && listener != null) {
      mImmOffsetListener.remove(listener);
    }
  }

  public void seslRemoveOnImmOffsetChangedListener(OnOffsetChangedListener listener) {
    seslRemoveOnImmOffsetChangedListener((SeslBaseOnImmOffsetChangedListener) listener);
  }

  public void seslAddOnImmOffsetChangedListener(@Nullable SeslBaseOnImmOffsetChangedListener listener) {
    if (mImmOffsetListener == null) {
      mImmOffsetListener = new ArrayList();
    }
    if (listener != null && !mImmOffsetListener.contains(listener)) {
      mImmOffsetListener.add(listener);
    }
  }

  public void seslAddOnImmOffsetChangedListener(SeslOnImmOffsetChangedListener listener) {
    seslAddOnImmOffsetChangedListener((SeslBaseOnImmOffsetChangedListener) listener);
  }


  void updateInternalCollapsedHeight() {
    if (!useCollapsedHeight()) {
      if (getImmBehavior() == null || !getCanScroll()) {
        final float oldCollapsedHeight = seslGetCollapsedHeight();
        final float newCollapsedHeight = (float) (getHeight() - getTotalScrollRange());
        if (newCollapsedHeight != oldCollapsedHeight && newCollapsedHeight > 0.0f) {
          Log.i(TAG, "Internal collapsedHeight/ oldCollapsedHeight :" + oldCollapsedHeight
              + " newCollapsedHeight :" + newCollapsedHeight);
          seslSetCollapsedHeight(newCollapsedHeight, false);
          updateInternalHeight();
        }
      }
    }
  }


  void updateInternalCollapsedHeightOnce() {
    if (!useCollapsedHeight()) {
      if (getImmBehavior() == null || !getCanScroll()) {
        final float collapsedHeight = seslGetCollapsedHeight();
        Log.i(TAG, "update InternalCollapsedHeight from " +
            "updateInternalHeight() : " + collapsedHeight);
        seslSetCollapsedHeight(collapsedHeight, false);
      }
    }
  }

  public SeslAppbarState seslGetAppBarState() {
    return mAppbarState;
  }

  public void seslSetCustomHeightProportion(boolean enabled,
                                            @FloatRange(from = 0, to = 1) float proportion) {
    if (proportion > 1.0f) {
      Log.e(TAG, "Height proportion float range is 0..1");
      return;
    }
    mUseCustomHeight = enabled;
    mSetCustomProportion = enabled;
    mSetCustomHeight = false;
    mCustomHeightProportion = proportion;
    updateInternalHeight();
    requestLayout();
  }

  public void seslSetCustomHeight(int height) {
    mCustomHeight = height;
    mUseCustomHeight = true;
    mSetCustomHeight = true;
    mSetCustomProportion = false;

    CoordinatorLayout.LayoutParams lp;
    try {
      lp = (CoordinatorLayout.LayoutParams) getLayoutParams();
    } catch (ClassCastException e) {
      lp = null;
      Log.e(TAG, Log.getStackTraceString(e));
    }
    if (lp != null) {
      lp.height = height;
      setLayoutParams(lp);
    }
  }
  //sesl

  protected int getCurrentOrientation() {
    return mCurrentOrientation;
  }

  @RequiresApi(api = VERSION_CODES.R)
  @Deprecated
  public void seslRestoreTopAndBottom(View view) {
    seslRestoreTopAndBottom();
  }

  private SeslImmersiveScrollBehavior getImmBehavior() {
    if (VERSION.SDK_INT >= 30) {
      ViewGroup.LayoutParams lp = getLayoutParams();
      if (lp instanceof CoordinatorLayout.LayoutParams) {
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) lp).getBehavior();
        if (behavior instanceof SeslImmersiveScrollBehavior) {
          return (SeslImmersiveScrollBehavior) behavior;
        }
      }
    }

    return null;
  }

  public boolean seslHaveImmersiveBehavior() {
    return getImmBehavior() != null;
  }

  public void seslSetWindowInsetsAnimationCallback(Object callback) {
    if (VERSION.SDK_INT >= 30) {
      SeslImmersiveScrollBehavior behavior = getImmBehavior();
      if (behavior != null) {
        if (callback == null) {
          behavior.setWindowInsetsAnimationCallback(this, null);
        }
        if (callback instanceof WindowInsetsAnimation.Callback) {
          behavior.setWindowInsetsAnimationCallback(this, (WindowInsetsAnimation.Callback) callback);
        }
      }
    }
  }


  @SuppressLint("NewApi")
  public void seslRestoreTopAndBottom() {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslRestoreTopAndBottom();
    }
  }

  @RequiresApi(api = VERSION_CODES.R)
  public void seslRestoreTopAndBottom(boolean restore) {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslRestoreTopAndBottom(restore);
    }
  }

  public void resetAppBarAndInsets() {
    seslResetAppBarAndInsets(true);
  }

  @SuppressLint("NewApi")
  public void seslResetAppBarAndInsets(boolean force) {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      Log.i(TAG, "seslResetAppBarAndInsets() force = " + force);
      behavior.seslRestoreTopAndBottom();
      behavior.showWindowInset(force);
    }
  }

  @SuppressLint("NewApi")
  public void seslCancelWindowInsetsAnimationController() {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      Log.i(TAG, "seslCancelWindowInsetsAnimationController");
      behavior.cancelWindowInsetsAnimationController();
    }
  }

  public void seslImmHideStatusBarForLandscape(boolean hide) {
    mImmHideStatusBar = hide;
  }

  boolean isImmHideStatusBarForLandscape() {
    return mImmHideStatusBar;
  }

  @Deprecated
  public void seslSetBottomView(View view, View bottomView) {
    seslSetBottomView(bottomView);
  }

  @SuppressLint("NewApi")
  public void seslSetBottomView(View bottomView) {
    if (bottomView == null) {
      Log.w(TAG, "bottomView is null");
    }
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslSetBottomView(bottomView);
    }
  }

  @SuppressLint("NewApi")
  protected void internalActivateImmersiveScroll(boolean activate, boolean byUser) {
    mIsActivatedImmersiveScroll = activate;
    mIsActivatedByUser = byUser;

    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    Log.i(TAG, "internalActivateImmersiveScroll : " + activate
        + " , byUser : " + byUser
        + " , behavior : " + behavior);
    if (behavior != null) {
      if (!activate || behavior.isAppBarHide()) {
        behavior.seslRestoreTopAndBottom(mRestoreAnim);
      }
    }
  }

  public void seslActivateImmersiveScroll(boolean activate, boolean byUser) {
    if (isDexEnabled()) {
      Log.i(TAG, "Dex Enabled Set false ImmersiveScroll");
      activate = false;
    }
    mRestoreAnim = byUser;

    if (VERSION.SDK_INT >= 30) {
      internalActivateImmersiveScroll(activate, true);

      boolean z = true;
      SeslImmersiveScrollBehavior behavior = getImmBehavior();
      if (behavior != null) {
        z = behavior.dispatchImmersiveScrollEnable();
      }

      if (z || !activate) {
        setCanScroll(activate);
      }
    }
  }

  public void seslSetImmersiveScroll(boolean activate, boolean byUser) {
    seslActivateImmersiveScroll(activate, byUser);
  }

  public void seslActivateImmersiveScroll(boolean activate) {
    seslActivateImmersiveScroll(activate, true);
  }

  public void seslSetImmersiveScroll(boolean activate) {
    seslActivateImmersiveScroll(activate);
  }

  public boolean isActivatedImmsersiveScroll() {
    return mIsActivatedImmersiveScroll;
  }

  public boolean seslGetImmersiveScroll() {
    return isActivatedImmsersiveScroll();
  }

  protected boolean isImmersiveActivatedByUser() {
    return mIsActivatedByUser;
  }

  protected void setCanScroll(boolean canScroll) {
    if (mIsCanScroll != canScroll) {
      mIsCanScroll = canScroll;
      invalidateScrollRanges();
      requestLayout();
    }
  }

  public void seslSetTCScrollRange(int range) {
    mSeslTCScrollRange = range;
  }

  protected int seslGetTCScrollRange() {
    return mSeslTCScrollRange;
  }

  protected boolean getCanScroll() {
    return mIsCanScroll;
  }

  public void seslSetCollapsedHeight(float height) {
    Log.i(TAG, "seslSetCollapsedHeight, height : " + height);
    seslSetCollapsedHeight(height, true);
  }

  private void seslSetCollapsedHeight(float height, boolean useCollapsedHeight) {
    mUseCollapsedHeight = useCollapsedHeight;
    mCollapsedHeight = height;
  }

  void internalProportion(float proportion) {
    if (!mUseCustomHeight && mHeightProportion != proportion) {
      mHeightProportion = proportion;
      updateInternalHeight();
    }
  }

  void setImmersiveTopInset(int topInset) {
    mImmersiveTopInset = topInset;
  }

  final int getImmersiveTopInset() {
    if (mIsCanScroll) {
      return mImmersiveTopInset;
    }
    return 0;
  }

  public float seslGetCollapsedHeight() {
    return mCollapsedHeight + getImmersiveTopInset();
  }

  public float seslGetHeightProPortion() {
    return mHeightProportion;
  }

  @RestrictTo(LIBRARY_GROUP)
  boolean useCollapsedHeight() {
    return mUseCollapsedHeight;
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    if (mBackground != null) {
      setBackgroundDrawable(mBackground == getBackground() ? mBackground : getBackground());
    } else if (getBackground() != null) {
      mBackground = getBackground();
      setBackgroundDrawable(mBackground);
    } else {
      mBackground = null;
      setBackgroundColor(mResources.getColor(
          SeslMisc.isLightTheme(getContext())
              ? R.color.sesl_action_bar_background_color_light
              : R.color.sesl_action_bar_background_color_dark));
    }

    if (mCurrentScreenHeight != newConfig.screenHeightDp
        || mCurrentOrientation != newConfig.orientation) {
      if (!mUseCustomPadding && !mUseCollapsedHeight) {
        Log.i(TAG, "Update bottom padding");
        mBottomPadding = mResources.getDimensionPixelSize(R.dimen.sesl_extended_appbar_bottom_padding);
        setPadding(0, 0, 0, mBottomPadding);
        mCollapsedHeight = (float) (mResources.getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding) + mBottomPadding);
        seslSetCollapsedHeight(mCollapsedHeight, false);
      } else if (mUseCustomPadding && mBottomPadding == 0 && !mUseCollapsedHeight) {
        mCollapsedHeight = (float) mResources.getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
        seslSetCollapsedHeight(mCollapsedHeight, false);
      }
    }

    if (!mSetCustomProportion) {
      mHeightProportion = ResourcesCompat.getFloat(mResources, R.dimen.sesl_appbar_height_proportion);
    }

    updateInternalHeight();

    if (lifted || (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT
        && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)) {
      setExpanded(false, false, true);
    } else {
      setExpanded(true, false, true);
    }

    mCurrentOrientation = newConfig.orientation;
    mCurrentScreenHeight = newConfig.screenHeightDp;
  }

  @Override
  public boolean dispatchGenericMotionEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
      if (liftOnScrollTargetView != null) {
        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
          setExpanded(false);
        } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f
            && !canScrollVertically(-1)) {
          setExpanded(true);
        }
      } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
        setExpanded(false);
      } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f) {
        setExpanded(true);
      }
    }

    return super.dispatchGenericMotionEvent(event);
  }

  private void updateInternalHeight() {
    final int windowHeight = getWindowHeight();

    final float proportion;
    if (mUseCustomHeight) {
      if (mCustomHeightProportion != 0.0f) {
        proportion = mCustomHeightProportion + (getCanScroll() ? getDifferImmHeightRatio() : 0.0f);
      } else {
        proportion = 0.0f;
      }
    } else {
      proportion = mHeightProportion;
    }

    float collapsedHeight = ((float) windowHeight) * proportion;
    if (collapsedHeight == 0.0f) {
      updateInternalCollapsedHeightOnce();
      collapsedHeight = seslGetCollapsedHeight();
    }

    CoordinatorLayout.LayoutParams lp;
    try {
      lp = (CoordinatorLayout.LayoutParams) getLayoutParams();
    } catch (ClassCastException e) {
      lp = null;
      Log.e(TAG, Log.getStackTraceString(e));
    }

    String message = "[updateInternalHeight] orientation : " + mResources.getConfiguration().orientation
        + ", density : " + mResources.getConfiguration().densityDpi
        + ", windowHeight : " + windowHeight;
    if (mUseCustomHeight) {
      if (mSetCustomProportion) {
        if (lp != null) {
          lp.height = (int) collapsedHeight;
          setLayoutParams(lp);
          message += ", [1]updateInternalHeight: lp.height : " + lp.height
              + ", mCustomHeightProportion : " + mCustomHeightProportion;
        }
      } else if (mSetCustomHeight && lp != null) {
        lp.height = mCustomHeight + getImmersiveTopInset();
        setLayoutParams(lp);
        message += ", [2]updateInternalHeight: CustomHeight : "
            + mCustomHeight + "lp.height : " + lp.height;
      }
    } else if (lp != null) {
      lp.height = (int) collapsedHeight;
      setLayoutParams(lp);
      message += ", [3]updateInternalHeight: lp.height : " + lp.height
          + ", mHeightProportion : " + mHeightProportion;
    }
    if (VERSION.SDK_INT >= 30) {
      message += " , mIsImmersiveScroll : " + mIsActivatedImmersiveScroll
          + " , mIsSetByUser : " + mIsActivatedByUser;
    }
    Log.i(TAG, message);
  }

  private float getDifferImmHeightRatio() {
    float windowHeight = getWindowHeight();
    final float immersiveTopInset = getImmersiveTopInset();
    if (windowHeight == 0.0f) {
      windowHeight = 1.0f;
    }
    return immersiveTopInset / windowHeight;
  }

  private int getWindowHeight() {
    return mResources.getDisplayMetrics().heightPixels;
  }

  @Override
  public void seslSetExpanded(boolean expanded) {
    setExpanded(expanded);
  }

  @Override
  public boolean seslIsCollapsed() {
    return lifted;
  }

  @Override
  public void seslSetIsMouse(boolean isMouse) {
    this.isMouse = isMouse;
  }

  protected boolean getIsMouse() {
    return isMouse;
  }

  /** A {@link ViewGroup.LayoutParams} implementation for {@link AppBarLayout}. */
  public static class LayoutParams extends LinearLayout.LayoutParams {
    // Sesl
    private static final int FLAG_NO_SCROLL_HOLD = 1 << 16;
    private static final int FLAG_NO_SNAP = 1 << 12;
    public static final int SESL_SCROLL_FLAG_NO_SCROLL_HOLD = FLAG_NO_SCROLL_HOLD;
    public static final int SESL_SCROLL_FLAG_NO_SNAP = FLAG_NO_SNAP;
    // Sesl

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
        flag = true,
        value = {
            SCROLL_FLAG_NO_SCROLL,
            SCROLL_FLAG_SCROLL,
            SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
            SCROLL_FLAG_ENTER_ALWAYS,
            SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
            SCROLL_FLAG_SNAP,
            SCROLL_FLAG_SNAP_MARGINS,
            SESL_SCROLL_FLAG_NO_SNAP,
            SESL_SCROLL_FLAG_NO_SCROLL_HOLD
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {}
    /**
     * Disable scrolling on the view. This flag should not be combined with any of the other scroll
     * flags.
     */
    public static final int SCROLL_FLAG_NO_SCROLL = 0x0;

    /**
     * The view will be scroll in direct relation to scroll events. This flag needs to be set for
     * any of the other flags to take effect. If any sibling views before this one do not have this
     * flag, then this value has no effect.
     */
    public static final int SCROLL_FLAG_SCROLL = 0x1;

    /**
     * When exiting (scrolling off screen) the view will be scrolled until it is 'collapsed'. The
     * collapsed height is defined by the view's minimum height.
     *
     * @see ViewCompat#getMinimumHeight(View)
     * @see View#setMinimumHeight(int)
     */
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 1 << 1;

    /**
     * When entering (scrolling on screen) the view will scroll on any downwards scroll event,
     * regardless of whether the scrolling view is also scrolling. This is commonly referred to as
     * the 'quick return' pattern.
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 1 << 2;

    /**
     * An additional flag for 'enterAlways' which modifies the returning view to only initially
     * scroll back to it's collapsed height. Once the scrolling view has reached the end of it's
     * scroll range, the remainder of this view will be scrolled into view. The collapsed height is
     * defined by the view's minimum height.
     *
     * @see ViewCompat#getMinimumHeight(View)
     * @see View#setMinimumHeight(int)
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 1 << 3;

    /**
     * Upon a scroll ending, if the view is only partially visible then it will be snapped and
     * scrolled to its closest edge. For example, if the view only has its bottom 25% displayed, it
     * will be scrolled off screen completely. Conversely, if its bottom 75% is visible then it will
     * be scrolled fully into view.
     */
    public static final int SCROLL_FLAG_SNAP = 1 << 4;

    /**
     * An additional flag to be used with 'snap'. If set, the view will be snapped to its top and
     * bottom margins, as opposed to the edges of the view itself.
     */
    public static final int SCROLL_FLAG_SNAP_MARGINS = 1 << 5;

    /** Internal flags which allows quick checking features */
    static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;

    static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
    static final int COLLAPSIBLE_FLAGS =
        SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;

    int scrollFlags = SCROLL_FLAG_SCROLL;
    Interpolator scrollInterpolator;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_Layout);
      scrollFlags = a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0);
      if (a.hasValue(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator)) {
        int resId = a.getResourceId(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator, 0);
        scrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
      }
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LinearLayout.LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
      scrollFlags = source.scrollFlags;
      scrollInterpolator = source.scrollInterpolator;
    }

    /**
     * Set the scrolling flags.
     *
     * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL}, {@link
     *     #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS}, {@link
     *     #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED}, {@link #SCROLL_FLAG_SNAP}, and {@link
     *     #SCROLL_FLAG_SNAP_MARGINS}. Otherwise, use {@link #SCROLL_FLAG_NO_SCROLL} to disable
     *     scrolling.
     * @see #getScrollFlags()
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    public void setScrollFlags(@ScrollFlags int flags) {
      scrollFlags = flags;
    }

    /**
     * Returns the scrolling flags.
     *
     * @see #setScrollFlags(int)
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    @ScrollFlags
    public int getScrollFlags() {
      return scrollFlags;
    }

    /**
     * Set the interpolator to when scrolling the view associated with this {@link LayoutParams}.
     *
     * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
     * @see #getScrollInterpolator()
     */
    public void setScrollInterpolator(Interpolator interpolator) {
      scrollInterpolator = interpolator;
    }

    /**
     * Returns the {@link Interpolator} being used for scrolling the view associated with this
     * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
     *
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
     * @see #setScrollInterpolator(Interpolator)
     */
    public Interpolator getScrollInterpolator() {
      return scrollInterpolator;
    }

    /** Returns true if the scroll flags are compatible for 'collapsing' */
    boolean isCollapsible() {
      return (scrollFlags & SCROLL_FLAG_SCROLL) == SCROLL_FLAG_SCROLL
          && (scrollFlags & COLLAPSIBLE_FLAGS) != 0;
    }
  }

  /**
   * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested scroll
   * handling with offsetting.
   */
  // TODO(b/76413401): remove the base class and generic type after the widget migration is done
  public static class Behavior extends BaseBehavior<AppBarLayout> {

    /** Callback to allow control over any {@link AppBarLayout} dragging. */
    public abstract static class DragCallback extends BaseBehavior.BaseDragCallback<AppBarLayout> {}

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  /**
   * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested scroll
   * handling with offsetting.
   */
  // TODO(b/76413401): remove this base class and generic type after the widget migration is done
  protected static class BaseBehavior<T extends AppBarLayout> extends HeaderBehavior<T> {
    // Sesl
    private float mDiffY_Touch;
    private float mLastMotionY_Touch;
    private float mVelocity = 0.0f;
    private float touchX;
    private float touchY;

    private int mTouchSlop = -1;

    private boolean mDirectTouchAppbar = false;
    private boolean mIsScrollHold = false;
    private boolean mIsSetStaticDuration = false;
    private boolean mIsFlingScrollDown = false;
    private boolean mIsFlingScrollUp = false;
    private boolean mLifted;
    private boolean mToolisMouse;
    public boolean coordinatorLayoutA11yScrollable;

    // Sesl
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600; // ms
    private static final int INVALID_POSITION = -1;

    /** Callback to allow control over any {@link AppBarLayout} dragging. */
    // TODO(b/76413401): remove this base class and generic type after the widget migration
    public abstract static class BaseDragCallback<T extends AppBarLayout> {
      /**
       * Allows control over whether the given {@link AppBarLayout} can be dragged or not.
       *
       * <p>Dragging is defined as a direct touch on the AppBarLayout with movement. This call does
       * not affect any nested scrolling.
       *
       * @return true if we are in a position to scroll the AppBarLayout via a drag, false if not.
       */
      public abstract boolean canDrag(@NonNull T appBarLayout);
    }

    private int offsetDelta;

    @NestedScrollType private int lastStartedType;

    private ValueAnimator offsetAnimator;

    private SavedState savedState;

    private int offsetToChildIndexOnLayout = INVALID_POSITION;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;

    @Nullable private WeakReference<View> lastNestedScrollingChildRef;
    private BaseDragCallback onDragCallback;

    public BaseBehavior() {}

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        @NonNull View directTargetChild,
        View target,
        int nestedScrollAxes,
        int type) {
      // Return true if we're nested scrolling vertically, and we either have lift on scroll enabled
      // or we can scroll the children.
      final boolean started =
          (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
              && (child.isLiftOnScroll() || canScrollChildren(parent, child, directTargetChild));

      if (started && offsetAnimator != null) {
        // Cancel any offset animation
        offsetAnimator.cancel();
      }

      if (child.getBottom() <= child.seslGetCollapsedHeight()) {
        mLifted = true;
        child.setLifted(true);
        mDiffY_Touch = 0.0f;
      } else {
        mLifted = false;
        child.setLifted(false);
      }
      child.updateInternalCollapsedHeight();

      // A new nested scroll has started so clear out the previous ref
      lastNestedScrollingChildRef = null;

      // Track the last started type so we know if a fling is about to happen once scrolling ends
      lastStartedType = type;

      mToolisMouse = child.getIsMouse();

      return started;
    }

    // Return true if there are scrollable children and the scrolling view is big enough to scroll.
    private boolean canScrollChildren(
        @NonNull CoordinatorLayout parent, @NonNull T child, @NonNull View directTargetChild) {
      return child.hasScrollableChildren()
          && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
    }

    @Override
    public void onNestedPreScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
        View target,
        int dx,
        int dy,
        int[] consumed,
        int type) {
      if (dy != 0) {
        int min;
        int max;
        if (dy < 0) {
          // We're scrolling down
          min = -child.getTotalScrollRange();
          max = min + child.getDownNestedPreScrollRange();
          mIsFlingScrollDown = true;
          mIsFlingScrollUp = false;
          if (child.getBottom() >= child.getHeight() * 0.52d) {
            mIsSetStaticDuration = true;
          }
          if (dy < -30) {
            mIsFlingScrollDown = true;
          } else {
            mVelocity = 0.0f;
            mIsFlingScrollDown = false;
          }
        } else {
          // We're scrolling up
          min = -child.getUpNestedPreScrollRange();
          max = 0;
          mIsFlingScrollDown = false;
          mIsFlingScrollUp = true;
          if (child.getBottom() <= child.getHeight() * 0.43d) {
            mIsSetStaticDuration = true;
          }
          if (dy > 30) {
            mIsFlingScrollUp = true;
          } else {
            mVelocity = 0.0f;
            mIsFlingScrollUp = false;
          }
          if (getTopAndBottomOffset() == min) {
            mIsScrollHold = true;
          }
        }
        if (isFlingRunnable()) {
          onFlingFinished(coordinatorLayout, child);
        }
        if (min != max) {
          consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
        }
      }
      if (child.isLiftOnScroll()) {
        child.setLiftedState(child.shouldLift(target));
      }
      stopNestedScrollIfNeeded(dy, child, target, type);
    }

    @Override
    public void onNestedScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
        View target,
        int dxConsumed,
        int dyConsumed,
        int dxUnconsumed,
        int dyUnconsumed,
        int type,
        int[] consumed) {
      if (isScrollHoldMode(child)) {
        if (dyUnconsumed >= 0 || mIsScrollHold) {
          ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
        } else {
          consumed[1] =
              scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
          stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
        }
      } else if (dyUnconsumed < 0) {
        // If the scrolling view is scrolling down but not consuming, it's probably be at
        // the top of it's content
        consumed[1] =
            scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
      }

      if (dyUnconsumed == 0) {
        // The scrolling view may scroll to the top of its content without updating the actions, so
        // update here.
        updateAccessibilityActions(coordinatorLayout, child);
      }
    }

    private void stopNestedScrollIfNeeded(
        int dy, @NonNull T child, View target, int type) {
      if (type == ViewCompat.TYPE_NON_TOUCH) {
        final int offset = getTopBottomOffsetForScrollingSibling();
        final int maxOffset = -child.getDownNestedScrollRange();
        if ((dy < 0 && offset == 0) || (dy > 0 && offset == maxOffset)) {
          ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
        }
      }
    }

    @Override
    public void onStopNestedScroll(
        CoordinatorLayout coordinatorLayout, @NonNull T abl, View target, int type) {
      if (mLastTouchEvent == MotionEvent.ACTION_CANCEL
          || mLastTouchEvent == MotionEvent.ACTION_UP
          || mLastInterceptTouchEvent == MotionEvent.ACTION_CANCEL
          || mLastInterceptTouchEvent == MotionEvent.ACTION_UP) {
        snapToChildIfNeeded(coordinatorLayout, abl);
      }
      // onStartNestedScroll for a fling will happen before onStopNestedScroll for the scroll. This
      // isn't necessarily guaranteed yet, but it should be in the future. We use this to our
      // advantage to check if a fling (ViewCompat.TYPE_NON_TOUCH) will start after the touch scroll
      // (ViewCompat.TYPE_TOUCH) ends
      if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        // If we haven't been flung, or a fling is ending
        if (abl.isLiftOnScroll()) {
          abl.setLiftedState(abl.shouldLift(target));
        }
        if (mIsScrollHold) {
          mIsScrollHold = false;
        }
      }

      // Keep a reference to the previous nested scrolling child
      lastNestedScrollingChildRef = new WeakReference<>(target);
    }

    /**
     * Set a callback to control any {@link AppBarLayout} dragging.
     *
     * @param callback the callback to use, or {@code null} to use the default behavior.
     */
    public void setDragCallback(@Nullable BaseDragCallback callback) {
      onDragCallback = callback;
    }

    private void animateOffsetTo(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T child,
        final int offset,
        float velocity) {
      int duration;
      velocity = Math.abs(mVelocity);
      if (velocity > 0.0f && velocity <= 3000.0f) {
        duration = (int) ((3000.0f - velocity) * 0.4d);
      } else {
        duration = 250;
      }
      if (duration <= 250) {
        duration = 250;
      }
      if (mIsSetStaticDuration) {
        mIsSetStaticDuration = false;
        duration = 250;
      }

      if (velocity < 2000.0f) {
        animateOffsetWithDuration(coordinatorLayout, child, offset, duration);
      }
      mVelocity = 0.0f;
    }

    private void animateOffsetWithDuration(
        final CoordinatorLayout coordinatorLayout,
        final T child,
        final int offset,
        final int duration) {
      final int currentOffset = getTopBottomOffsetForScrollingSibling();
      if (currentOffset == offset) {
        if (offsetAnimator != null && offsetAnimator.isRunning()) {
          offsetAnimator.cancel();
        }
        return;
      }

      if (offsetAnimator == null) {
        offsetAnimator = new ValueAnimator();
        offsetAnimator.setInterpolator(SeslAnimationUtils.SINE_OUT_80);
        offsetAnimator.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
              @Override
              public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                setHeaderTopBottomOffset(
                    coordinatorLayout, child, (int) animator.getAnimatedValue());
              }
            });
      } else {
        offsetAnimator.cancel();
      }

      offsetAnimator.setDuration(Math.min(duration, MAX_OFFSET_ANIMATION_DURATION));
      offsetAnimator.setIntValues(currentOffset, offset);
      offsetAnimator.start();
    }

    private int getChildIndexOnOffset(@NonNull T abl, int offset) {
      offset += abl.isLifted() ? abl.getPaddingBottom() : 0;
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        int top = child.getTop();
        int bottom = child.getBottom();

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (checkFlag(lp.getScrollFlags(), LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
          // Update top and bottom to include margins
          top -= lp.topMargin;
          bottom += lp.bottomMargin;
        }

        if (abl.seslGetTCScrollRange() != 0) {
          bottom += abl.seslGetTCScrollRange();
        }

        if (top <= -offset && bottom >= -offset) {
          return i;
        }
      }
      return -1;
    }

    // TODO rework this method
    // kang
    private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, T t) {
      int topBottomOffsetForScrollingSibling = getTopBottomOffsetForScrollingSibling();
      int childIndexOnOffset = getChildIndexOnOffset(t, topBottomOffsetForScrollingSibling);
      View childAt = coordinatorLayout.getChildAt(1);
      if (childIndexOnOffset >= 0) {
        View childAt2 = t.getChildAt(childIndexOnOffset);
        LayoutParams layoutParams = (LayoutParams) childAt2.getLayoutParams();
        int scrollFlags = layoutParams.getScrollFlags();
        if ((scrollFlags & 4096) == 4096) {
          seslHasNoSnapFlag(true);
          return;
        }
        seslHasNoSnapFlag(false);
        int seslGetTCScrollRange = t.getCanScroll() ? t.seslGetTCScrollRange() : 0;
        if (((float) t.getBottom()) >= t.seslGetCollapsedHeight()) {
          int i = -childAt2.getTop();
          int i2 = -childAt2.getBottom();
          if (childIndexOnOffset == t.getChildCount() - 1) {
            i2 += t.getTopInset();
          }
          if (checkFlag(scrollFlags, 2)) {
            if (t.getCanScroll()) {
              i2 = (int) (((float) i2) + (t.seslGetCollapsedHeight() - ((float) t.getPaddingBottom())));
            } else {
              i2 += ViewCompat.getMinimumHeight(childAt2);
            }
          } else if (checkFlag(scrollFlags, 5)) {
            int minimumHeight = ViewCompat.getMinimumHeight(childAt2) + i2;
            if (topBottomOffsetForScrollingSibling < minimumHeight) {
              i = minimumHeight;
            } else {
              i2 = minimumHeight;
            }
          }
          if (checkFlag(scrollFlags, 32)) {
            i += layoutParams.topMargin;
            i2 -= layoutParams.bottomMargin;
          }
          int i3 = (!this.mLifted ? ((double) topBottomOffsetForScrollingSibling)
              >= ((double) (i2 + i)) * 0.43d : ((double) topBottomOffsetForScrollingSibling)
              >= ((double) (i2 + i)) * 0.52d) ? i : i2;
          if (childAt == null) {
            Log.w(TAG, "coordinatorLayout.getChildAt(1) is null");
            i = i3;
          } else {
            if (this.mIsFlingScrollUp) {
              this.mIsFlingScrollUp = false;
              this.mIsFlingScrollDown = false;
            } else {
              i2 = i3;
            }
            if (!this.mIsFlingScrollDown || ((float) childAt.getTop()) <= t.seslGetCollapsedHeight()) {
              i = i2;
            } else {
              this.mIsFlingScrollDown = false;
            }
          }
          animateOffsetTo(coordinatorLayout, t, clamp(i, -t.getTotalScrollRange(), 0), 0.0f);
        } else if (t.getCanScroll()) {
          int seslGetCollapsedHeight = (((int) t.seslGetCollapsedHeight()) - t.getTotalScrollRange()) + seslGetTCScrollRange;
          int i4 = -t.getTotalScrollRange();
          int i5 = ((double) (t.getBottom() + seslGetTCScrollRange)) >= ((double) t.seslGetCollapsedHeight()) * 0.48d ? seslGetCollapsedHeight : i4;
          if (!this.mIsFlingScrollUp) {
            i4 = i5;
          }
          if (!this.mIsFlingScrollDown) {
            seslGetCollapsedHeight = i4;
          }
          animateOffsetTo(coordinatorLayout, t, clamp(seslGetCollapsedHeight, -t.getTotalScrollRange(), 0), 0.0f);
        }
      }
    }
    // kang

    private static boolean checkFlag(final int flags, final int check) {
      return (flags & check) == check;
    }

    @Override
    public boolean onMeasureChild(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        int parentWidthMeasureSpec,
        int widthUsed,
        int parentHeightMeasureSpec,
        int heightUsed) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (lp.height == CoordinatorLayout.LayoutParams.WRAP_CONTENT) {
        // If the view is set to wrap on it's height, CoordinatorLayout by default will
        // cap the view at the CoL's height. Since the AppBarLayout can scroll, this isn't
        // what we actually want, so we measure it ourselves with an unspecified spec to
        // allow the child to be larger than it's parent
        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            heightUsed);
        return true;
      }

      // Let the parent handle it as normal
      return super.onMeasureChild(
          parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private int getImmPendingActionOffset(AppBarLayout abl) {
      if (VERSION.SDK_INT >= 30) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) abl.getLayoutParams();
        Behavior behavior = (Behavior) lp.getBehavior();
        if (abl.getCanScroll() && behavior instanceof SeslImmersiveScrollBehavior) {
          return ((int) abl.seslGetCollapsedHeight()) + abl.seslGetTCScrollRange();
        }
      }
      return 0;
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent, @NonNull T abl, int layoutDirection) {
      boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

      // The priority for actions here is (first which is true wins):
      // 1. forced pending actions
      // 2. offsets for restorations
      // 3. non-forced pending actions
      final int pendingAction = abl.getPendingAction();
      if (offsetToChildIndexOnLayout >= 0 && (pendingAction & PENDING_ACTION_FORCE) == 0) {
        View child = abl.getChildAt(offsetToChildIndexOnLayout);
        int offset = -child.getBottom();
        if (offsetToChildIndexOnLayoutIsMinHeight) {
          offset += ViewCompat.getMinimumHeight(child) + abl.getTopInset();
        } else {
          offset += Math.round(child.getHeight() * offsetToChildIndexOnLayoutPerc);
        }
        setHeaderTopBottomOffset(parent, abl, offset);
      } else if (pendingAction != PENDING_ACTION_NONE) {
        final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
        if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
          final float offset = (-abl.getTotalScrollRange()) + getImmPendingActionOffset(abl) - abl.getImmersiveTopInset();
          if (animate) {
            animateOffsetTo(parent, abl, (int) offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, (int) offset);
          }
        } else if ((pendingAction & PENDING_ACTION_COLLAPSED_IMM) != 0) {
          float offset = (-abl.getTotalScrollRange()) + getImmPendingActionOffset(abl);
          if (parent.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
              && abl.getImmersiveTopInset() == 0 && abl.seslGetHeightProPortion() == 0.0f) {
            offset = 0.0f;
          }
          if (animate) {
            animateOffsetTo(parent, abl, (int) offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, (int) offset);
          }
        } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
          if (animate) {
            animateOffsetTo(parent, abl, 0, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, 0);
          }
        }
      }

      // Finally reset any pending states
      abl.resetPendingAction();
      offsetToChildIndexOnLayout = INVALID_POSITION;

      // We may have changed size, so let's constrain the top and bottom offset correctly,
      // just in case we're out of the bounds
      setTopAndBottomOffset(
          clamp(getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));

      // Update the AppBarLayout's drawable state for any elevation changes. This is needed so that
      // the elevation is set in the first layout, so that we don't get a visual jump pre-N (due to
      // the draw dispatch skip)
      updateAppBarLayoutDrawableState(
          parent, abl, getTopAndBottomOffset(), 0 /* direction */, false /* forceJump */);

      // Make sure we dispatch the offset update
      abl.onOffsetChanged(getTopAndBottomOffset());

      updateAccessibilityActions(parent, abl);
      return handled;
    }

    private void updateAccessibilityActions(
        CoordinatorLayout coordinatorLayout, @NonNull T appBarLayout) {
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_FORWARD.getId());
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_BACKWARD.getId());
      View scrollingView = findFirstScrollingChild(coordinatorLayout);
      // Don't add a11y actions if there is no scrolling view that the abl depends on for scrolling
      // or the abl has no scroll range.
      if (scrollingView == null || appBarLayout.getTotalScrollRange() == 0) {
        return;
      }
      if (!ViewCompat.hasAccessibilityDelegate(coordinatorLayout)) {
        ViewCompat.setAccessibilityDelegate(coordinatorLayout, new AccessibilityDelegateCompat() { // from class: com.google.android.material.appbar.AppBarLayout.BaseBehavior.2
          @Override
          public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat infoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, infoCompat);
            infoCompat.setScrollable(coordinatorLayoutA11yScrollable);
            infoCompat.setClassName(ScrollView.class.getName());
          }
        });
      }

      // Don't add actions if the scrolling view doesn't have the behavior that will cause the abl
      // to scroll.
      CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) scrollingView.getLayoutParams();
      if (!(lp.getBehavior() instanceof ScrollingViewBehavior)) {
        return;
      }
      coordinatorLayoutA11yScrollable = addAccessibilityScrollActions(coordinatorLayout, appBarLayout, scrollingView);
    }

    private boolean addAccessibilityScrollActions(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T appBarLayout,
        @NonNull final View scrollingView) {
      boolean z = false;
      if (getTopBottomOffsetForScrollingSibling() != -appBarLayout.getTotalScrollRange()) {
        // Add a collapsing action if the view can scroll up and the offset isn't the abl scroll
        // range. (This offset means the view is completely collapsed). Collapse to minimum height.
        addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_FORWARD, false);
        z = true;
      }

      // Don't add an expanding action if the sibling offset is 0, which would mean the abl is
      // completely expanded.
      if (getTopBottomOffsetForScrollingSibling() != 0) {
        if (scrollingView.canScrollVertically(-1)) {
          // Expanding action. If the view can scroll down, expand the app bar reflecting the logic
          // in onNestedPreScroll.
          final int dy = -appBarLayout.getDownNestedPreScrollRange();
          // Offset by non-zero.
          if (dy != 0) {
            ViewCompat.replaceAccessibilityAction(
                coordinatorLayout,
                ACTION_SCROLL_BACKWARD,
                null,
                new AccessibilityViewCommand() {
                  @Override
                  public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
                    onNestedPreScroll(
                        coordinatorLayout,
                        appBarLayout,
                        scrollingView,
                        0,
                        dy,
                        new int[] {0, 0},
                        ViewCompat.TYPE_NON_TOUCH);
                    return true;
                  }
                });
            return true;

          }
        } else {
          // If the view can't scroll down, we are probably at the top of the scrolling content so
          // expand completely.
          addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_BACKWARD, true);
          return true;

        }
      }
      return z;
    }

    private void addActionToExpand(
        CoordinatorLayout parent,
        @NonNull final T appBarLayout,
        @NonNull AccessibilityActionCompat action,
        final boolean expand) {
      ViewCompat.replaceAccessibilityAction(
          parent,
          action,
          null,
          new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
              appBarLayout.setExpanded(expand);
              return true;
            }
          });
    }

    @Override
    boolean canDragView(T view) {
      if (onDragCallback != null) {
        // If there is a drag callback set, it's in control
        return onDragCallback.canDrag(view);
      }

      // Else we'll use the default behaviour of seeing if it can scroll down
      if (lastNestedScrollingChildRef != null) {
        // If we have a reference to a scrolling view, check it
        final View scrollingView = lastNestedScrollingChildRef.get();
        return scrollingView != null
            && scrollingView.isShown()
            && !scrollingView.canScrollVertically(-1);
      } else {
        // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
        return true;
      }
    }

    @Override
    void onFlingFinished(@NonNull CoordinatorLayout parent, @NonNull T layout) {
      if (scroller != null) {
        scroller.forceFinished(true);
      }
    }

    @Override
    int getMaxDragOffset(@NonNull T view) {
      return -view.getDownNestedScrollRange();
    }

    @Override
    int getScrollRangeForDragFling(@NonNull T view) {
      return view.getTotalScrollRange();
    }

    @Override
    int setHeaderTopBottomOffset(
        @NonNull CoordinatorLayout coordinatorLayout,
        @NonNull T appBarLayout,
        int newOffset,
        int minOffset,
        int maxOffset) {
      final int curOffset = getTopBottomOffsetForScrollingSibling();
      int consumed = 0;

      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        // If we have some scrolling range, and we're currently within the min and max
        // offsets, calculate a new offset
        newOffset = clamp(newOffset, minOffset, maxOffset);
        if (curOffset != newOffset) {
          final int interpolatedOffset =
              appBarLayout.hasChildWithInterpolator()
                  ? interpolateOffset(appBarLayout, newOffset)
                  : newOffset;

          final boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

          // Update how much dy we have consumed
          consumed = curOffset - newOffset;
          // Update the stored sibling offset
          offsetDelta = newOffset - interpolatedOffset;

          if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
            // If the offset hasn't changed and we're using an interpolated scroll
            // then we need to keep any dependent views updated. CoL will do this for
            // us when we move, but we need to do it manually when we don't (as an
            // interpolated scroll may finish early).
            coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
          }

          // Dispatch the updates to any listeners
          appBarLayout.onOffsetChanged(getTopAndBottomOffset());

          // Update the AppBarLayout's drawable state (for any elevation changes)
          updateAppBarLayoutDrawableState(
              coordinatorLayout,
              appBarLayout,
              newOffset,
              newOffset < curOffset ? -1 : 1,
              false /* forceJump */);
        }
      } else {
        // Reset the offset delta
        offsetDelta = 0;
      }

      updateAccessibilityActions(coordinatorLayout, appBarLayout);
      return consumed;
    }

    @VisibleForTesting
    boolean isOffsetAnimatorRunning() {
      return offsetAnimator != null && offsetAnimator.isRunning();
    }

    int interpolateOffset(@NonNull T layout, final int offset) {
      final int absOffset = Math.abs(offset);

      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final Interpolator interpolator = childLp.getScrollInterpolator();

        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          if (interpolator != null) {
            int childScrollableHeight = 0;
            final int flags = childLp.getScrollFlags();
            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
              // We're set to scroll so add the child's height plus margin
              childScrollableHeight += child.getHeight() + childLp.topMargin + childLp.bottomMargin;

              if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                // For a collapsing scroll, we to take the collapsed height
                // into account.
                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
              }
            }

            if (ViewCompat.getFitsSystemWindows(child)) {
              childScrollableHeight -= layout.getTopInset();
            }

            if (childScrollableHeight > 0) {
              final int offsetForView = absOffset - child.getTop();
              final int interpolatedDiff =
                  Math.round(
                      childScrollableHeight
                          * interpolator.getInterpolation(
                          offsetForView / (float) childScrollableHeight));

              return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
            }
          }

          // If we get to here then the view on the offset isn't suitable for interpolated
          // scrolling. So break out of the loop
          break;
        }
      }

      return offset;
    }

    void updateAppBarLayoutDrawableState(
        @NonNull final CoordinatorLayout parent,
        @NonNull final T layout,
        final int offset,
        final int direction,
        final boolean forceJump) {
      final View child = getAppBarChildOnOffset(layout, offset);
      if (child != null) {
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final int flags = childLp.getScrollFlags();
        boolean lifted = false;

        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
          final int minHeight = ViewCompat.getMinimumHeight(child);

          if (direction > 0
              && (flags
              & (LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
              | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED))
              != 0) {
            // We're set to enter always collapsed so we are only collapsed when
            // being scrolled down, and in a collapsed offset
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset()
                - layout.getImmersiveTopInset();
          } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
            // We're set to exit until collapsed, so any offset which results in
            // the minimum height (or less) being shown is collapsed
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset()
                - layout.getImmersiveTopInset();
          }
        }

        if (layout.isLiftOnScroll()) {
          // Use first scrolling child as default scrolling view for updating lifted state because
          // it represents the content that would be scrolled beneath the app bar.
          lifted = layout.shouldLift(findFirstScrollingChild(parent));
        }

        final boolean changed = layout.setLiftedState(lifted);

        if (forceJump || (changed && shouldJumpElevationState(parent, layout))) {
          // If the collapsed state changed, we may need to
          // jump to the current state if we have an overlapping view
          layout.jumpDrawablesToCurrentState();
        }
      }
    }

    private boolean shouldJumpElevationState(@NonNull CoordinatorLayout parent, @NonNull T layout) {
      // We should jump the elevated state if we have a dependent scrolling view which has
      // an overlapping top (i.e. overlaps us)
      final List<View> dependencies = parent.getDependents(layout);
      for (int i = 0, size = dependencies.size(); i < size; i++) {
        final View dependency = dependencies.get(i);
        final CoordinatorLayout.LayoutParams lp =
            (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        final CoordinatorLayout.Behavior behavior = lp.getBehavior();

        if (behavior instanceof ScrollingViewBehavior) {
          return ((ScrollingViewBehavior) behavior).getOverlayTop() != 0;
        }
      }
      return false;
    }

    @Nullable
    private static View getAppBarChildOnOffset(
        @NonNull final AppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);
      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          return child;
        }
      }
      return null;
    }

    @Nullable
    private View findFirstScrollingChild(@NonNull CoordinatorLayout parent) {
      for (int i = 0, z = parent.getChildCount(); i < z; i++) {
        final View child = parent.getChildAt(i);
        if (child instanceof NestedScrollingChild
            || child instanceof ListView
            || child instanceof ScrollView) {
          return child;
        }
      }
      return null;
    }

    @Override
    int getTopBottomOffsetForScrollingSibling() {
      return getTopAndBottomOffset() + offsetDelta;
    }


    @Override
    public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull T abl) {
      Parcelable superState = super.onSaveInstanceState(parent, abl);
      SavedState scrollState = saveScrollState(superState, abl);
      return scrollState == null ? superState : scrollState;
    }

    @Override
    public void onRestoreInstanceState(
        @NonNull CoordinatorLayout parent, @NonNull T appBarLayout, Parcelable state) {
      if (state instanceof SavedState) {
        restoreScrollState((SavedState) state, true);
        super.onRestoreInstanceState(parent, appBarLayout, savedState.getSuperState());
      } else {
        super.onRestoreInstanceState(parent, appBarLayout, state);
        savedState = null;
      }
    }

    @Nullable
    SavedState saveScrollState(@Nullable Parcelable superState, @NonNull T abl) {
      final int offset = getTopAndBottomOffset();

      // Try and find the first visible child...
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        final int visBottom = child.getBottom() + offset;

        if (child.getTop() + offset <= 0 && visBottom >= 0) {
          final SavedState ss =
              new SavedState(superState == null ? AbsSavedState.EMPTY_STATE : superState);
          ss.fullyExpanded = offset == 0;
          ss.fullyScrolled = !ss.fullyExpanded && -offset >= abl.getTotalScrollRange();
          ss.firstVisibleChildIndex = i;
          ss.firstVisibleChildAtMinimumHeight =
              visBottom == (ViewCompat.getMinimumHeight(child) + abl.getTopInset());
          ss.firstVisibleChildPercentageShown = visBottom / (float) child.getHeight();
          return ss;
        }
      }
      return null;
    }

    void restoreScrollState(@Nullable SavedState state, boolean force) {
      if (savedState == null || force) {
        savedState = state;
      }
    }

    /** A {@link Parcelable} implementation for {@link AppBarLayout}. */
    protected static class SavedState extends AbsSavedState {
      boolean fullyScrolled;
      boolean fullyExpanded;
      int firstVisibleChildIndex;
      float firstVisibleChildPercentageShown;
      boolean firstVisibleChildAtMinimumHeight;

      public SavedState(@NonNull Parcel source, ClassLoader loader) {
        super(source, loader);
        fullyScrolled = source.readByte() != 0;
        fullyExpanded = source.readByte() != 0;
        firstVisibleChildIndex = source.readInt();
        firstVisibleChildPercentageShown = source.readFloat();
        firstVisibleChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      @Override
      public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (fullyScrolled ? 1 : 0));
        dest.writeByte((byte) (fullyExpanded ? 1 : 0));
        dest.writeInt(firstVisibleChildIndex);
        dest.writeFloat(firstVisibleChildPercentageShown);
        dest.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
      }

      public static final Creator<SavedState> CREATOR =
          new ClassLoaderCreator<SavedState>() {
            @NonNull
            @Override
            public SavedState createFromParcel(@NonNull Parcel source, ClassLoader loader) {
              return new SavedState(source, loader);
            }

            @Nullable
            @Override
            public SavedState createFromParcel(@NonNull Parcel source) {
              return new SavedState(source, null);
            }

            @NonNull
            @Override
            public SavedState[] newArray(int size) {
              return new SavedState[size];
            }
          };
    }

    private boolean isScrollHoldMode(T appBarLayout) {
      if (mToolisMouse) {
        return false;
      }

      final int offset = getTopBottomOffsetForScrollingSibling();
      final int offsetChildIndex = getChildIndexOnOffset(appBarLayout, offset);
      View child = appBarLayout.getChildAt(offsetChildIndex);
      LayoutParams lp = (LayoutParams) child.getLayoutParams();
      return offsetChildIndex < 0
          || (lp.getScrollFlags() & LayoutParams.SESL_SCROLL_FLAG_NO_SCROLL_HOLD)
          != LayoutParams.SESL_SCROLL_FLAG_NO_SCROLL_HOLD;
    }

    @Override
    public boolean onNestedPreFling(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        @NonNull View target,
        float velocityX,
        float velocityY) {
      mVelocity = velocityY;
      if (velocityY < -300.0f) {
        mIsFlingScrollDown = true;
        mIsFlingScrollUp = false;
      } else if (velocityY > 300.0f) {
        mIsFlingScrollDown = false;
        mIsFlingScrollUp = true;
      } else {
        mVelocity = 0.0f;
        mIsFlingScrollDown = false;
        mIsFlingScrollUp = false;
        return true;
      }

      return super.onNestedPreFling(parent, child, target, velocityX, velocityY);
    }

    @Override
    public boolean onTouchEvent(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        @NonNull MotionEvent ev) {
      if (mTouchSlop < 0) {
        mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
      }

      final int action = ev.getAction();
      mToolisMouse = child.getIsMouse();

      switch (action) {
        case MotionEvent.ACTION_DOWN:
          mDirectTouchAppbar = true;
          touchX = ev.getX();
          touchY = ev.getY();
          mLastMotionY_Touch = touchY;
          mDiffY_Touch = 0.0f;
          break;

        case MotionEvent.ACTION_MOVE:
          mDirectTouchAppbar = true;
          final float currentY = ev.getY();
          if (currentY - mLastMotionY_Touch != 0.0f) {
            mDiffY_Touch = currentY - mLastMotionY_Touch;
          }
          if (Math.abs(mDiffY_Touch) > mTouchSlop) {
            mLastMotionY_Touch = currentY;
          }
          break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          if (Math.abs(mDiffY_Touch) > 21.0f) {
            if (mDiffY_Touch < 0.0f) {
              mIsFlingScrollUp = true;
              mIsFlingScrollDown = false;
            } else if (mDiffY_Touch > 0.0f) {
              mIsFlingScrollUp = false;
              mIsFlingScrollDown = true;
            }
          } else {
            touchX = 0.0f;
            touchY = 0.0f;
            mIsFlingScrollUp = false;
            mIsFlingScrollDown = false;
            mLastMotionY_Touch = 0.0f;
          }

          if (mDirectTouchAppbar) {
            mDirectTouchAppbar = false;
            snapToChildIfNeeded(parent, child);
          }
          break;
      }

      return super.onTouchEvent(parent, child, ev);
    }
  }

  /**
   * Behavior which should be used by {@link View}s which can scroll vertically and support nested
   * scrolling to automatically scroll any {@link AppBarLayout} siblings.
   */
  public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {

    public ScrollingViewBehavior() {}

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);

      final TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
      setOverlayTop(
          a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
      a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
      // We depend on any AppBarLayouts
      return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      offsetChildAsNeeded(child, dependency);
      updateLiftedStateIfNeeded(child, dependency);
      return false;
    }

    @Override
    public void onDependentViewRemoved(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      if (dependency instanceof AppBarLayout) {
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_FORWARD.getId());
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_BACKWARD.getId());
      }
    }

    @Override
    public boolean onRequestChildRectangleOnScreen(
        @NonNull CoordinatorLayout parent,
        @NonNull View child,
        @NonNull Rect rectangle,
        boolean immediate) {
      final AppBarLayout header = findFirstDependency(parent.getDependencies(child));
      if (header != null) {
        // Offset the rect by the child's left/top
        rectangle.offset(child.getLeft(), child.getTop());

        final Rect parentRect = tempRect1;
        parentRect.set(0, 0, parent.getWidth(), parent.getHeight());

        if (!parentRect.contains(rectangle)) {
          // If the rectangle can not be fully seen the visible bounds, collapse
          // the AppBarLayout
          header.setExpanded(false, !immediate);
          return true;
        }
      }
      return false;
    }

    private void offsetChildAsNeeded(@NonNull View child, @NonNull View dependency) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        // Offset the child, pinning it to the bottom the header-dependency, maintaining
        // any vertical gap and overlap
        final BaseBehavior ablBehavior = (BaseBehavior) behavior;
        ViewCompat.offsetTopAndBottom(
            child,
            (dependency.getBottom() - child.getTop())
                + ablBehavior.offsetDelta
                + getVerticalLayoutGap()
                - getOverlapPixelsForOffset(dependency));
      }
    }

    @Override
    float getOverlapRatioForOffset(final View header) {
      if (header instanceof AppBarLayout) {
        final AppBarLayout abl = (AppBarLayout) header;
        final int totalScrollRange = abl.getTotalScrollRange();
        final int preScrollDown = abl.getDownNestedPreScrollRange();
        final int offset = getAppBarLayoutOffset(abl);

        if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
          // If we're in a pre-scroll down. Don't use the offset at all.
          return 0;
        } else {
          final int availScrollRange = totalScrollRange - preScrollDown;
          if (availScrollRange != 0) {
            // Else we'll use a interpolated ratio of the overlap, depending on offset
            return 1f + (offset / (float) availScrollRange);
          }
        }
      }
      return 0f;
    }

    private static int getAppBarLayoutOffset(@NonNull AppBarLayout abl) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        return ((BaseBehavior) behavior).getTopBottomOffsetForScrollingSibling();
      }
      return 0;
    }

    @Nullable
    @Override
    AppBarLayout findFirstDependency(@NonNull List<View> views) {
      for (int i = 0, z = views.size(); i < z; i++) {
        View view = views.get(i);
        if (view instanceof AppBarLayout) {
          return (AppBarLayout) view;
        }
      }
      return null;
    }

    @Override
    int getScrollRange(View v) {
      if (v instanceof AppBarLayout) {
        return ((AppBarLayout) v).getTotalScrollRange();
      } else {
        return super.getScrollRange(v);
      }
    }

    private void updateLiftedStateIfNeeded(View child, View dependency) {
      if (dependency instanceof AppBarLayout) {
        AppBarLayout appBarLayout = (AppBarLayout) dependency;
        if (appBarLayout.isLiftOnScroll()) {
          appBarLayout.setLiftedState(appBarLayout.shouldLift(child));
        }
      }
    }
  }

  private boolean isDexEnabled() {
    if (getContext() == null) {
      return false;
    }
    return SeslConfigurationReflector.isDexEnabled(getContext().getResources().getConfiguration());
  }

  protected boolean isDetachedState() {
    return mIsDetachedState;
  }
}
