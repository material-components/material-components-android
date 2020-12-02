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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * CollapsingToolbarLayout is a wrapper for {@link Toolbar} which implements a collapsing app bar.
 * It is designed to be used as a direct child of a {@link AppBarLayout}. CollapsingToolbarLayout
 * contains the following features:
 *
 * <h4>Collapsing title</h4>
 *
 * A title which is larger when the layout is fully visible but collapses and becomes smaller as the
 * layout is scrolled off screen. You can set the title to display via {@link
 * #setTitle(CharSequence)}. The title appearance can be tweaked via the {@code
 * collapsedTextAppearance} and {@code expandedTextAppearance} attributes.
 *
 * <h4>Content scrim</h4>
 *
 * A full-bleed scrim which is show or hidden when the scroll position has hit a certain threshold.
 * You can change this via {@link #setContentScrim(Drawable)}.
 *
 * <h4>Status bar scrim</h4>
 *
 * A scrim which is shown or hidden behind the status bar when the scroll position has hit a certain
 * threshold. You can change this via {@link #setStatusBarScrim(Drawable)}. This only works on
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} devices when we set to fit system
 * windows.
 *
 * <h4>Parallax scrolling children</h4>
 *
 * Child views can opt to be scrolled within this layout in a parallax fashion. See {@link
 * LayoutParams#COLLAPSE_MODE_PARALLAX} and {@link LayoutParams#setParallaxMultiplier(float)}.
 *
 * <h4>Pinned position children</h4>
 *
 * Child views can opt to be pinned in space globally. This is useful when implementing a collapsing
 * as it allows the {@link Toolbar} to be fixed in place even though this layout is moving. See
 * {@link LayoutParams#COLLAPSE_MODE_PIN}.
 *
 * <p><strong>Do not manually add views to the Toolbar at run time</strong>. We will add a 'dummy
 * view' to the Toolbar which allows us to work out the available space for the title. This can
 * interfere with any views which you add.
 *
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_contentScrim
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_statusBarScrim
 * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_toolbarId
 */
public class CollapsingToolbarLayout extends FrameLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_CollapsingToolbar;
  private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

  private boolean refreshToolbar = true;
  private int toolbarId;
  @Nullable private ViewGroup toolbar;
  @Nullable private View toolbarDirectChild;
  private View dummyView;

  private int expandedMarginStart;
  private int expandedMarginTop;
  private int expandedMarginEnd;
  private int expandedMarginBottom;

  private final Rect tmpRect = new Rect();
  @NonNull final CollapsingTextHelper collapsingTextHelper;
  private boolean collapsingTitleEnabled;
  private boolean drawCollapsingTitle;

  @Nullable private Drawable contentScrim;
  @Nullable Drawable statusBarScrim;
  private int scrimAlpha;
  private boolean scrimsAreShown;
  private ValueAnimator scrimAnimator;
  private long scrimAnimationDuration;
  private int scrimVisibleHeightTrigger = -1;

  private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;

  int currentOffset;

  @Nullable WindowInsetsCompat lastInsets;

  public CollapsingToolbarLayout(@NonNull Context context) {
    this(context, null);
  }

  public CollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.collapsingToolbarLayoutStyle);
  }

  public CollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    collapsingTextHelper = new CollapsingTextHelper(this);
    collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.CollapsingToolbarLayout,
            defStyleAttr,
            DEF_STYLE_RES);

    collapsingTextHelper.setExpandedTextGravity(
        a.getInt(
            R.styleable.CollapsingToolbarLayout_expandedTitleGravity,
            GravityCompat.START | Gravity.BOTTOM));
    collapsingTextHelper.setCollapsedTextGravity(
        a.getInt(
            R.styleable.CollapsingToolbarLayout_collapsedTitleGravity,
            GravityCompat.START | Gravity.CENTER_VERTICAL));

    expandedMarginStart =
        expandedMarginTop =
            expandedMarginEnd =
                expandedMarginBottom =
                    a.getDimensionPixelSize(
                        R.styleable.CollapsingToolbarLayout_expandedTitleMargin, 0);

    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart)) {
      expandedMarginStart =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd)) {
      expandedMarginEnd =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop)) {
      expandedMarginTop =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
      expandedMarginBottom =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);
    }

    collapsingTitleEnabled = a.getBoolean(R.styleable.CollapsingToolbarLayout_titleEnabled, true);
    setTitle(a.getText(R.styleable.CollapsingToolbarLayout_title));

    // First load the default text appearances
    collapsingTextHelper.setExpandedTextAppearance(
        R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
    collapsingTextHelper.setCollapsedTextAppearance(
        androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

    // Now overlay any custom text appearances
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance)) {
      collapsingTextHelper.setExpandedTextAppearance(
          a.getResourceId(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, 0));
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance)) {
      collapsingTextHelper.setCollapsedTextAppearance(
          a.getResourceId(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, 0));
    }

    scrimVisibleHeightTrigger =
        a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

    if (a.hasValue(R.styleable.CollapsingToolbarLayout_maxLines)) {
      collapsingTextHelper.setMaxLines(a.getInt(R.styleable.CollapsingToolbarLayout_maxLines, 1));
    }

    scrimAnimationDuration =
        a.getInt(
            R.styleable.CollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION);

    setContentScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim));
    setStatusBarScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim));

    toolbarId = a.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1);

    a.recycle();

    setWillNotDraw(false);

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Add an OnOffsetChangedListener if possible
    final ViewParent parent = getParent();
    if (parent instanceof AppBarLayout) {
      // Copy over from the ABL whether we should fit system windows
      ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

      if (onOffsetChangedListener == null) {
        onOffsetChangedListener = new OffsetUpdateListener();
      }
      ((AppBarLayout) parent).addOnOffsetChangedListener(onOffsetChangedListener);

      // We're attached, so lets request an inset dispatch
      ViewCompat.requestApplyInsets(this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // Remove our OnOffsetChangedListener if possible and it exists
    final ViewParent parent = getParent();
    if (onOffsetChangedListener != null && parent instanceof AppBarLayout) {
      ((AppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    super.onDetachedFromWindow();
  }

  WindowInsetsCompat onWindowInsetChanged(@NonNull final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;
    }

    // If our insets have changed, keep them and invalidate the scroll ranges...
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      requestLayout();
    }

    // Consume the insets. This is done so that child views with fitSystemWindows=true do not
    // get the default padding functionality from View
    return insets.consumeSystemWindowInsets();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
    // Instead, we draw it here, before our collapsing text.
    ensureToolbar();
    if (toolbar == null && contentScrim != null && scrimAlpha > 0) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
    }

    // Let the collapsing text helper draw its text
    if (collapsingTitleEnabled && drawCollapsingTitle) {
      collapsingTextHelper.draw(canvas);
    }

    // Now draw the status bar scrim
    if (statusBarScrim != null && scrimAlpha > 0) {
      final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
      if (topInset > 0) {
        statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
        statusBarScrim.mutate().setAlpha(scrimAlpha);
        statusBarScrim.draw(canvas);
      }
    }
  }

  @Override
  protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
    // but in front of any other children which are behind it. To do this we intercept the
    // drawChild() call, and draw our scrim just before the Toolbar is drawn
    boolean invalidated = false;
    if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
      invalidated = true;
    }
    return super.drawChild(canvas, child, drawingTime) || invalidated;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (contentScrim != null) {
      contentScrim.setBounds(0, 0, w, h);
    }
  }

  private void ensureToolbar() {
    if (!refreshToolbar) {
      return;
    }

    // First clear out the current Toolbar
    this.toolbar = null;
    toolbarDirectChild = null;

    if (toolbarId != -1) {
      // If we have an ID set, try and find it and it's direct parent to us
      this.toolbar = findViewById(toolbarId);
      if (this.toolbar != null) {
        toolbarDirectChild = findDirectChild(this.toolbar);
      }
    }

    if (this.toolbar == null) {
      // If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
      // one from our direct children
      ViewGroup toolbar = null;
      for (int i = 0, count = getChildCount(); i < count; i++) {
        final View child = getChildAt(i);
        if (isToolbar(child)) {
          toolbar = (ViewGroup) child;
          break;
        }
      }
      this.toolbar = toolbar;
    }

    updateDummyView();
    refreshToolbar = false;
  }

  private static boolean isToolbar(View view) {
    return view instanceof androidx.appcompat.widget.Toolbar
        || (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && view instanceof android.widget.Toolbar);
  }

  private boolean isToolbarChild(View child) {
    return (toolbarDirectChild == null || toolbarDirectChild == this)
        ? child == toolbar
        : child == toolbarDirectChild;
  }

  /** Returns the direct child of this layout, which itself is the ancestor of the given view. */
  @NonNull
  private View findDirectChild(@NonNull final View descendant) {
    View directChild = descendant;
    for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
      if (p instanceof View) {
        directChild = (View) p;
      }
    }
    return directChild;
  }

  private void updateDummyView() {
    if (!collapsingTitleEnabled && dummyView != null) {
      // If we have a dummy view and we have our title disabled, remove it from its parent
      final ViewParent parent = dummyView.getParent();
      if (parent instanceof ViewGroup) {
        ((ViewGroup) parent).removeView(dummyView);
      }
    }
    if (collapsingTitleEnabled && toolbar != null) {
      if (dummyView == null) {
        dummyView = new View(getContext());
      }
      if (dummyView.getParent() == null) {
        toolbar.addView(dummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    ensureToolbar();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    final int mode = MeasureSpec.getMode(heightMeasureSpec);
    final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
    if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
      // If we have a top inset and we're set to wrap_content height we need to make sure
      // we add the top inset to our height, therefore we re-measure
      heightMeasureSpec =
          MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // Set our minimum height to enable proper AppBarLayout collapsing
    if (toolbar != null) {
      if (toolbarDirectChild == null || toolbarDirectChild == this) {
        setMinimumHeight(getHeightWithMargins(toolbar));
      } else {
        setMinimumHeight(getHeightWithMargins(toolbarDirectChild));
      }
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (lastInsets != null) {
      // Shift down any views which are not set to fit system windows
      final int insetTop = lastInsets.getSystemWindowInsetTop();
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (!ViewCompat.getFitsSystemWindows(child)) {
          if (child.getTop() < insetTop) {
            // If the child isn't set to fit system windows but is drawing within
            // the inset offset it down
            ViewCompat.offsetTopAndBottom(child, insetTop);
          }
        }
      }
    }

    // Update our child view offset helpers so that they track the correct layout coordinates
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).onViewLayout();
    }

    // Update the collapsed bounds by getting its transformed bounds
    if (collapsingTitleEnabled && dummyView != null) {
      // We only draw the title if the dummy view is being displayed (Toolbar removes
      // views if there is no space)
      drawCollapsingTitle =
          ViewCompat.isAttachedToWindow(dummyView) && dummyView.getVisibility() == VISIBLE;

      if (drawCollapsingTitle) {
        final boolean isRtl =
            ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

        // Update the collapsed bounds
        updateCollapsedBounds(isRtl);

        // Update the expanded bounds
        collapsingTextHelper.setExpandedBounds(
            isRtl ? expandedMarginEnd : expandedMarginStart,
            tmpRect.top + expandedMarginTop,
            right - left - (isRtl ? expandedMarginStart : expandedMarginEnd),
            bottom - top - expandedMarginBottom);

        // Now recalculate using the new bounds
        collapsingTextHelper.recalculate();
      }
    }

    if (toolbar != null) {
      if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.getText())) {
        // If we do not currently have a title, try and grab it from the Toolbar
        setTitle(getToolbarTitle(toolbar));
      }
    }

    updateScrimVisibility();

    // Apply any view offsets, this should be done at the very end of layout
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).applyOffsets();
    }
  }

  private void updateCollapsedBounds(boolean isRtl) {
    final int maxOffset =
        getMaxOffsetForPinChild(toolbarDirectChild != null ? toolbarDirectChild : toolbar);
    DescendantOffsetUtils.getDescendantRect(this, dummyView, tmpRect);
    final int titleMarginStart;
    final int titleMarginEnd;
    final int titleMarginTop;
    final int titleMarginBottom;
    if (toolbar instanceof androidx.appcompat.widget.Toolbar) {
      androidx.appcompat.widget.Toolbar compatToolbar = (androidx.appcompat.widget.Toolbar) toolbar;
      titleMarginStart = compatToolbar.getTitleMarginStart();
      titleMarginEnd = compatToolbar.getTitleMarginEnd();
      titleMarginTop = compatToolbar.getTitleMarginTop();
      titleMarginBottom = compatToolbar.getTitleMarginBottom();
    } else if (VERSION.SDK_INT >= VERSION_CODES.N && toolbar instanceof android.widget.Toolbar) {
      android.widget.Toolbar frameworkToolbar = (android.widget.Toolbar) toolbar;
      titleMarginStart = frameworkToolbar.getTitleMarginStart();
      titleMarginEnd = frameworkToolbar.getTitleMarginEnd();
      titleMarginTop = frameworkToolbar.getTitleMarginTop();
      titleMarginBottom = frameworkToolbar.getTitleMarginBottom();
    } else {
      titleMarginStart = 0;
      titleMarginEnd = 0;
      titleMarginTop = 0;
      titleMarginBottom = 0;
    }
    collapsingTextHelper.setCollapsedBounds(
        tmpRect.left + (isRtl ? titleMarginEnd : titleMarginStart),
        tmpRect.top + maxOffset + titleMarginTop,
        tmpRect.right - (isRtl ? titleMarginStart : titleMarginEnd),
        tmpRect.bottom + maxOffset - titleMarginBottom);
  }

  private static CharSequence getToolbarTitle(View view) {
    if (view instanceof androidx.appcompat.widget.Toolbar) {
      return ((androidx.appcompat.widget.Toolbar) view).getTitle();
    } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
        && view instanceof android.widget.Toolbar) {
      return ((android.widget.Toolbar) view).getTitle();
    } else {
      return null;
    }
  }

  private static int getHeightWithMargins(@NonNull final View view) {
    final ViewGroup.LayoutParams lp = view.getLayoutParams();
    if (lp instanceof MarginLayoutParams) {
      final MarginLayoutParams mlp = (MarginLayoutParams) lp;
      return view.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;
    }
    return view.getMeasuredHeight();
  }

  @NonNull
  static ViewOffsetHelper getViewOffsetHelper(@NonNull View view) {
    ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
    if (offsetHelper == null) {
      offsetHelper = new ViewOffsetHelper(view);
      view.setTag(R.id.view_offset_helper, offsetHelper);
    }
    return offsetHelper;
  }

  /**
   * Sets the title to be displayed by this view, if enabled.
   *
   * @see #setTitleEnabled(boolean)
   * @see #getTitle()
   * @attr ref R.styleable#CollapsingToolbarLayout_title
   */
  public void setTitle(@Nullable CharSequence title) {
    collapsingTextHelper.setText(title);
    updateContentDescriptionFromTitle();
  }

  /**
   * Returns the title currently being displayed by this view. If the title is not enabled, then
   * this will return {@code null}.
   *
   * @attr ref R.styleable#CollapsingToolbarLayout_title
   */
  @Nullable
  public CharSequence getTitle() {
    return collapsingTitleEnabled ? collapsingTextHelper.getText() : null;
  }

  /**
   * Sets whether this view should display its own title.
   *
   * <p>The title displayed by this view will shrink and grow based on the scroll offset.
   *
   * @see #setTitle(CharSequence)
   * @see #isTitleEnabled()
   * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
   */
  public void setTitleEnabled(boolean enabled) {
    if (enabled != collapsingTitleEnabled) {
      collapsingTitleEnabled = enabled;
      updateContentDescriptionFromTitle();
      updateDummyView();
      requestLayout();
    }
  }

  /**
   * Returns whether this view is currently displaying its own title.
   *
   * @see #setTitleEnabled(boolean)
   * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
   */
  public boolean isTitleEnabled() {
    return collapsingTitleEnabled;
  }

  /**
   * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
   * vertical scroll may overwrite this value. Any visibility change will be animated if this view
   * has already been laid out.
   *
   * @param shown whether the scrims should be shown
   * @see #getStatusBarScrim()
   * @see #getContentScrim()
   */
  public void setScrimsShown(boolean shown) {
    setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
  }

  /**
   * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
   * vertical scroll may overwrite this value.
   *
   * @param shown whether the scrims should be shown
   * @param animate whether to animate the visibility change
   * @see #getStatusBarScrim()
   * @see #getContentScrim()
   */
  public void setScrimsShown(boolean shown, boolean animate) {
    if (scrimsAreShown != shown) {
      if (animate) {
        animateScrim(shown ? 0xFF : 0x0);
      } else {
        setScrimAlpha(shown ? 0xFF : 0x0);
      }
      scrimsAreShown = shown;
    }
  }

  private void animateScrim(int targetAlpha) {
    ensureToolbar();
    if (scrimAnimator == null) {
      scrimAnimator = new ValueAnimator();
      scrimAnimator.setDuration(scrimAnimationDuration);
      scrimAnimator.setInterpolator(
          targetAlpha > scrimAlpha
              ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
              : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
      scrimAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              setScrimAlpha((int) animator.getAnimatedValue());
            }
          });
    } else if (scrimAnimator.isRunning()) {
      scrimAnimator.cancel();
    }

    scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
    scrimAnimator.start();
  }

  void setScrimAlpha(int alpha) {
    if (alpha != scrimAlpha) {
      final Drawable contentScrim = this.contentScrim;
      if (contentScrim != null && toolbar != null) {
        ViewCompat.postInvalidateOnAnimation(toolbar);
      }
      scrimAlpha = alpha;
      ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
    }
  }

  int getScrimAlpha() {
    return scrimAlpha;
  }

  /**
   * Set the drawable to use for the content scrim from resources. Providing null will disable the
   * scrim functionality.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrim(@Nullable Drawable drawable) {
    if (contentScrim != drawable) {
      if (contentScrim != null) {
        contentScrim.setCallback(null);
      }
      contentScrim = drawable != null ? drawable.mutate() : null;
      if (contentScrim != null) {
        contentScrim.setBounds(0, 0, getWidth(), getHeight());
        contentScrim.setCallback(this);
        contentScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Set the color to use for the content scrim.
   *
   * @param color the color to display
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrimColor(@ColorInt int color) {
    setContentScrim(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the content scrim from resources.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #getContentScrim()
   */
  public void setContentScrimResource(@DrawableRes int resId) {
    setContentScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the foreground scrim.
   *
   * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
   * @see #setContentScrim(Drawable)
   */
  @Nullable
  public Drawable getContentScrim() {
    return contentScrim;
  }

  /**
   * Set the drawable to use for the status bar scrim from resources. Providing null will disable
   * the scrim functionality.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrim(@Nullable Drawable drawable) {
    if (statusBarScrim != drawable) {
      if (statusBarScrim != null) {
        statusBarScrim.setCallback(null);
      }
      statusBarScrim = drawable != null ? drawable.mutate() : null;
      if (statusBarScrim != null) {
        if (statusBarScrim.isStateful()) {
          statusBarScrim.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
        statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
        statusBarScrim.setCallback(this);
        statusBarScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    Drawable d = statusBarScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    d = contentScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    if (collapsingTextHelper != null) {
      changed |= collapsingTextHelper.setState(state);
    }

    if (changed) {
      invalidate();
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
      statusBarScrim.setVisible(visible, false);
    }
    if (contentScrim != null && contentScrim.isVisible() != visible) {
      contentScrim.setVisible(visible, false);
    }
  }

  /**
   * Set the color to use for the status bar scrim.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param color the color to display
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrimColor(@ColorInt int color) {
    setStatusBarScrim(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the status bar scrim from resources.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #getStatusBarScrim()
   */
  public void setStatusBarScrimResource(@DrawableRes int resId) {
    setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the status bar scrim.
   *
   * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
   * @see #setStatusBarScrim(Drawable)
   */
  @Nullable
  public Drawable getStatusBarScrim() {
    return statusBarScrim;
  }

  /**
   * Sets the text color and size for the collapsed title from the specified TextAppearance
   * resource.
   *
   * @attr ref
   *     com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
   */
  public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setCollapsedTextAppearance(resId);
  }

  /**
   * Sets the text color of the collapsed title.
   *
   * @param color The new text color in ARGB format
   */
  public void setCollapsedTitleTextColor(@ColorInt int color) {
    setCollapsedTitleTextColor(ColorStateList.valueOf(color));
  }

  /**
   * Sets the text colors of the collapsed title.
   *
   * @param colors ColorStateList containing the new text colors
   */
  public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
    collapsingTextHelper.setCollapsedTextColor(colors);
  }

  /**
   * Sets the horizontal alignment of the collapsed title and the vertical gravity that will be used
   * when there is extra space in the collapsed bounds beyond what is required for the title itself.
   *
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
   */
  public void setCollapsedTitleGravity(int gravity) {
    collapsingTextHelper.setCollapsedTextGravity(gravity);
  }

  /**
   * Returns the horizontal and vertical alignment for title when collapsed.
   *
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
   */
  public int getCollapsedTitleGravity() {
    return collapsingTextHelper.getCollapsedTextGravity();
  }

  /**
   * Sets the text color and size for the expanded title from the specified TextAppearance resource.
   *
   * @attr ref
   *     com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
   */
  public void setExpandedTitleTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setExpandedTextAppearance(resId);
  }

  /**
   * Sets the text color of the expanded title.
   *
   * @param color The new text color in ARGB format
   */
  public void setExpandedTitleColor(@ColorInt int color) {
    setExpandedTitleTextColor(ColorStateList.valueOf(color));
  }

  /**
   * Sets the text colors of the expanded title.
   *
   * @param colors ColorStateList containing the new text colors
   */
  public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
    collapsingTextHelper.setExpandedTextColor(colors);
  }

  /**
   * Sets the horizontal alignment of the expanded title and the vertical gravity that will be used
   * when there is extra space in the expanded bounds beyond what is required for the title itself.
   *
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
   */
  public void setExpandedTitleGravity(int gravity) {
    collapsingTextHelper.setExpandedTextGravity(gravity);
  }

  /**
   * Returns the horizontal and vertical alignment for title when expanded.
   *
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
   */
  public int getExpandedTitleGravity() {
    return collapsingTextHelper.getExpandedTextGravity();
  }

  /**
   * Set the typeface to use for the collapsed title.
   *
   * @param typeface typeface to use, or {@code null} to use the default.
   */
  public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
    collapsingTextHelper.setCollapsedTypeface(typeface);
  }

  /** Returns the typeface used for the collapsed title. */
  @NonNull
  public Typeface getCollapsedTitleTypeface() {
    return collapsingTextHelper.getCollapsedTypeface();
  }

  /**
   * Set the typeface to use for the expanded title.
   *
   * @param typeface typeface to use, or {@code null} to use the default.
   */
  public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
    collapsingTextHelper.setExpandedTypeface(typeface);
  }

  /** Returns the typeface used for the expanded title. */
  @NonNull
  public Typeface getExpandedTitleTypeface() {
    return collapsingTextHelper.getExpandedTypeface();
  }

  /**
   * Sets the expanded title margins.
   *
   * @param start the starting title margin in pixels
   * @param top the top title margin in pixels
   * @param end the ending title margin in pixels
   * @param bottom the bottom title margin in pixels
   * @see #getExpandedTitleMarginStart()
   * @see #getExpandedTitleMarginTop()
   * @see #getExpandedTitleMarginEnd()
   * @see #getExpandedTitleMarginBottom()
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
   */
  public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
    expandedMarginStart = start;
    expandedMarginTop = top;
    expandedMarginEnd = end;
    expandedMarginBottom = bottom;
    requestLayout();
  }

  /**
   * @return the starting expanded title margin in pixels
   * @see #setExpandedTitleMarginStart(int)
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
   */
  public int getExpandedTitleMarginStart() {
    return expandedMarginStart;
  }

  /**
   * Sets the starting expanded title margin in pixels.
   *
   * @param margin the starting title margin in pixels
   * @see #getExpandedTitleMarginStart()
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
   */
  public void setExpandedTitleMarginStart(int margin) {
    expandedMarginStart = margin;
    requestLayout();
  }

  /**
   * @return the top expanded title margin in pixels
   * @see #setExpandedTitleMarginTop(int)
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginTop
   */
  public int getExpandedTitleMarginTop() {
    return expandedMarginTop;
  }

  /**
   * Sets the top expanded title margin in pixels.
   *
   * @param margin the top title margin in pixels
   * @see #getExpandedTitleMarginTop()
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginTop
   */
  public void setExpandedTitleMarginTop(int margin) {
    expandedMarginTop = margin;
    requestLayout();
  }

  /**
   * @return the ending expanded title margin in pixels
   * @see #setExpandedTitleMarginEnd(int)
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
   */
  public int getExpandedTitleMarginEnd() {
    return expandedMarginEnd;
  }

  /**
   * Sets the ending expanded title margin in pixels.
   *
   * @param margin the ending title margin in pixels
   * @see #getExpandedTitleMarginEnd()
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
   */
  public void setExpandedTitleMarginEnd(int margin) {
    expandedMarginEnd = margin;
    requestLayout();
  }

  /**
   * @return the bottom expanded title margin in pixels
   * @see #setExpandedTitleMarginBottom(int)
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
   */
  public int getExpandedTitleMarginBottom() {
    return expandedMarginBottom;
  }

  /**
   * Sets the bottom expanded title margin in pixels.
   *
   * @param margin the bottom title margin in pixels
   * @see #getExpandedTitleMarginBottom()
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
   */
  public void setExpandedTitleMarginBottom(int margin) {
    expandedMarginBottom = margin;
    requestLayout();
  }

  /**
   * Sets the maximum number of lines to display in the expanded state.
   * Experimental Feature.
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setMaxLines(int maxLines) {
    collapsingTextHelper.setMaxLines(maxLines);
  }

  /**
   * Gets the maximum number of lines to display in the expanded state.
   * Experimental Feature.
   */
  @RestrictTo(LIBRARY_GROUP)
  public int getMaxLines() {
    return collapsingTextHelper.getMaxLines();
  }

  /**
   * Set the amount of visible height in pixels used to define when to trigger a scrim visibility
   * change.
   *
   * <p>If the visible height of this view is less than the given value, the scrims will be made
   * visible, otherwise they are hidden.
   *
   * @param height value in pixels used to define when to trigger a scrim visibility change
   * @attr ref
   *     com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimVisibleHeightTrigger
   */
  public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
    if (scrimVisibleHeightTrigger != height) {
      scrimVisibleHeightTrigger = height;
      // Update the scrim visibility
      updateScrimVisibility();
    }
  }

  /**
   * Returns the amount of visible height in pixels used to define when to trigger a scrim
   * visibility change.
   *
   * @see #setScrimVisibleHeightTrigger(int)
   */
  public int getScrimVisibleHeightTrigger() {
    if (scrimVisibleHeightTrigger >= 0) {
      // If we have one explicitly set, return it
      return scrimVisibleHeightTrigger;
    }

    // Otherwise we'll use the default computed value
    final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight > 0) {
      // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
      return Math.min((minHeight * 2) + insetTop, getHeight());
    }

    // If we reach here then we don't have a min height set. Instead we'll take a
    // guess at 1/3 of our height being visible
    return getHeight() / 3;
  }

  /**
   * Set the duration used for scrim visibility animations.
   *
   * @param duration the duration to use in milliseconds
   * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimAnimationDuration
   */
  public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
    scrimAnimationDuration = duration;
  }

  /** Returns the duration in milliseconds used for scrim visibility animations. */
  public long getScrimAnimationDuration() {
    return scrimAnimationDuration;
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  public static class LayoutParams extends FrameLayout.LayoutParams {

    private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
    @Retention(RetentionPolicy.SOURCE)
    @interface CollapseMode {}

    /** The view will act as normal with no collapsing behavior. */
    public static final int COLLAPSE_MODE_OFF = 0;

    /**
     * The view will pin in place until it reaches the bottom of the {@link
     * CollapsingToolbarLayout}.
     */
    public static final int COLLAPSE_MODE_PIN = 1;

    /**
     * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)} to
     * change the multiplier used.
     */
    public static final int COLLAPSE_MODE_PARALLAX = 2;

    int collapseMode = COLLAPSE_MODE_OFF;
    float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);

      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CollapsingToolbarLayout_Layout);
      collapseMode =
          a.getInt(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF);
      setParallaxMultiplier(
          a.getFloat(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier,
              DEFAULT_PARALLAX_MULTIPLIER));
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      super(width, height, gravity);
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
    }

    /**
     * Set the collapse mode.
     *
     * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or {@link
     *     #COLLAPSE_MODE_PARALLAX}.
     */
    public void setCollapseMode(@CollapseMode int collapseMode) {
      this.collapseMode = collapseMode;
    }

    /**
     * Returns the requested collapse mode.
     *
     * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or
     *     {@link #COLLAPSE_MODE_PARALLAX}.
     */
    @CollapseMode
    public int getCollapseMode() {
      return collapseMode;
    }

    /**
     * Set the parallax scroll multiplier used in conjunction with {@link #COLLAPSE_MODE_PARALLAX}.
     * A value of {@code 0.0} indicates no movement at all, {@code 1.0f} indicates normal scroll
     * movement.
     *
     * @param multiplier the multiplier.
     * @see #getParallaxMultiplier()
     */
    public void setParallaxMultiplier(float multiplier) {
      parallaxMult = multiplier;
    }

    /**
     * Returns the parallax scroll multiplier used in conjunction with {@link
     * #COLLAPSE_MODE_PARALLAX}.
     *
     * @see #setParallaxMultiplier(float)
     */
    public float getParallaxMultiplier() {
      return parallaxMult;
    }
  }

  /** Show or hide the scrims if needed */
  final void updateScrimVisibility() {
    if (contentScrim != null || statusBarScrim != null) {
      setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
    }
  }

  final int getMaxOffsetForPinChild(@NonNull View child) {
    final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
  }

  private void updateContentDescriptionFromTitle() {
    // Set this layout's contentDescription to match the title if it's shown by CollapsingTextHelper
    setContentDescription(getTitle());
  }

  private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
    OffsetUpdateListener() {}

    @Override
    public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
      currentOffset = verticalOffset;

      final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

        switch (lp.collapseMode) {
          case LayoutParams.COLLAPSE_MODE_PIN:
            offsetHelper.setTopAndBottomOffset(
                MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
            break;
          case LayoutParams.COLLAPSE_MODE_PARALLAX:
            offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
            break;
          default:
            break;
        }
      }

      // Show or hide the scrims if needed
      updateScrimVisibility();

      if (statusBarScrim != null && insetTop > 0) {
        ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
      }

      // Update the collapsing text's fraction
      final int expandRange =
          getHeight() - ViewCompat.getMinimumHeight(CollapsingToolbarLayout.this) - insetTop;
      collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);
    }
  }
}
