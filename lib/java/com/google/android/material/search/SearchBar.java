/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.search;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ToolbarUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * The {@link SearchBar} represents a floating search field with affordances for search and
 * navigation.
 *
 * <p>Note: {@link SearchBar} does not support the {@link #setTitle} and {@link #setSubtitle}
 * methods, or their corresponding xml attributes. Instead, use {@link #setHint} or {@link
 * #setText}, or their corresponding xml attributes, to provide a text affordance for your {@link
 * SearchBar}.
 *
 * <p>The example below shows how to use the {@link SearchBar} and {@link SearchView} together:
 *
 * <pre>
 * &lt;androidx.coordinatorlayout.widget.CoordinatorLayout
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *   &lt;!-- NestedScrollingChild goes here (NestedScrollView, RecyclerView, etc.). --&gt;
 *   &lt;androidx.core.widget.NestedScrollView
 *       android:layout_width=&quot;match_parent&quot;
 *       android:layout_height=&quot;match_parent&quot;
 *       app:layout_behavior=&quot;@string/searchbar_scrolling_view_behavior&quot;&gt;
 *     &lt;!-- Screen content goes here. --&gt;
 *   &lt;/androidx.core.widget.NestedScrollView&gt;
 *
 *   &lt;com.google.android.material.appbar.AppBarLayout
 *       android:layout_width=&quot;match_parent&quot;
 *       android:layout_height=&quot;wrap_content&quot;&gt;
 *     &lt;com.google.android.material.search.SearchBar
 *         android:id=&quot;@+id/search_bar&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:hint=&quot;@string/searchbar_hint&quot; /&gt;
 *   &lt;/com.google.android.material.appbar.AppBarLayout&gt;
 *
 *   &lt;com.google.android.material.search.SearchView
 *       android:layout_width=&quot;match_parent&quot;
 *       android:layout_height=&quot;match_parent&quot;
 *       android:hint=&quot;@string/searchbar_hint&quot;
 *       app:layout_anchor=&quot;@id/search_bar&quot;&gt;
 *     &lt;!-- Search suggestions/results go here (ScrollView, RecyclerView, etc.). --&gt;
 *   &lt;/com.google.android.material.search.SearchView&gt;
 * &lt;/androidx.coordinatorlayout.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Search.md">component
 * developer guidance</a> and <a
 * href="https://material.io/components/search/overview">design guidelines</a>.
 */
public class SearchBar extends Toolbar {

  private static final int DEF_STYLE_RES = R.style.Widget_Material3_SearchBar;

  private static final int DEFAULT_SCROLL_FLAGS =
      AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
          | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
          | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
          | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP_MARGINS;
  private static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

  private final TextView textView;
  private final boolean layoutInflated;
  private final boolean defaultMarginsEnabled;
  private final SearchBarAnimationHelper searchBarAnimationHelper;
  private final Drawable defaultNavigationIcon;
  private final boolean tintNavigationIcon;
  private final boolean forceDefaultNavigationOnClickListener;
  @Nullable private View centerView;
  @Nullable private Integer navigationIconTint;
  @Nullable private Drawable originalNavigationIconBackground;
  private int menuResId = -1;
  private boolean defaultScrollFlagsEnabled;
  private MaterialShapeDrawable backgroundShape;

  public SearchBar(@NonNull Context context) {
    this(context, null);
  }

  public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialSearchBarStyle);
  }

  public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    validateAttributes(attrs);

    defaultNavigationIcon =
        AppCompatResources.getDrawable(context, getDefaultNavigationIconResource());
    searchBarAnimationHelper = new SearchBarAnimationHelper();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.SearchBar, defStyleAttr, DEF_STYLE_RES);

    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();
    int backgroundColor = a.getColor(R.styleable.SearchBar_backgroundTint, 0);
    float elevation = a.getDimension(R.styleable.SearchBar_elevation, 0);
    defaultMarginsEnabled = a.getBoolean(R.styleable.SearchBar_defaultMarginsEnabled, true);
    defaultScrollFlagsEnabled = a.getBoolean(R.styleable.SearchBar_defaultScrollFlagsEnabled, true);
    boolean hideNavigationIcon = a.getBoolean(R.styleable.SearchBar_hideNavigationIcon, false);
    forceDefaultNavigationOnClickListener =
        a.getBoolean(R.styleable.SearchBar_forceDefaultNavigationOnClickListener, false);
    tintNavigationIcon = a.getBoolean(R.styleable.SearchBar_tintNavigationIcon, true);
    if (a.hasValue(R.styleable.SearchBar_navigationIconTint)) {
      navigationIconTint = a.getColor(R.styleable.SearchBar_navigationIconTint, -1);
    }
    int textAppearanceResId = a.getResourceId(R.styleable.SearchBar_android_textAppearance, -1);
    String text = a.getString(R.styleable.SearchBar_android_text);
    String hint = a.getString(R.styleable.SearchBar_android_hint);
    float strokeWidth = a.getDimension(R.styleable.SearchBar_strokeWidth, -1);
    int strokeColor = a.getColor(R.styleable.SearchBar_strokeColor, Color.TRANSPARENT);

    a.recycle();

    if (!hideNavigationIcon) {
      initNavigationIcon();
    }
    setClickable(true);
    setFocusable(true);

    LayoutInflater.from(context).inflate(R.layout.mtrl_search_bar, this);
    layoutInflated = true;

    textView = findViewById(R.id.open_search_bar_text_view);

    setElevation(elevation);
    initTextView(textAppearanceResId, text, hint);
    initBackground(shapeAppearanceModel, backgroundColor, elevation, strokeWidth, strokeColor);
  }

  private void validateAttributes(@Nullable AttributeSet attributeSet) {
    if (attributeSet == null) {
      return;
    }
    if (attributeSet.getAttributeValue(NAMESPACE_APP, "title") != null) {
      throw new UnsupportedOperationException(
          "SearchBar does not support title. Use hint or text instead.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_APP, "subtitle") != null) {
      throw new UnsupportedOperationException(
          "SearchBar does not support subtitle. Use hint or text instead.");
    }
  }

  private void initNavigationIcon() {
    // If no navigation icon, set up the default one; otherwise, re-set it for tinting if needed.
    setNavigationIcon(getNavigationIcon() == null ? defaultNavigationIcon : getNavigationIcon());

    // Make the navigation icon button decorative (not clickable/focusable) by default so that the
    // overall search bar handles the click. If a navigation icon click listener is set later on,
    // the button will be made clickable/focusable.
    setNavigationIconDecorative(true);
  }

  private void initTextView(@StyleRes int textAppearanceResId, String text, String hint) {
    if (textAppearanceResId != -1) {
      TextViewCompat.setTextAppearance(textView, textAppearanceResId);
    }
    setText(text);
    setHint(hint);
    if (getNavigationIcon() == null) {
      ((MarginLayoutParams) textView.getLayoutParams()).setMarginStart(getResources()
          .getDimensionPixelSize(R.dimen.m3_searchbar_text_margin_start_no_navigation_icon));
    }
  }

  private void initBackground(
      ShapeAppearanceModel shapeAppearance,
      @ColorInt int backgroundColor,
      float elevation,
      float strokeWidth,
      @ColorInt int strokeColor) {
    backgroundShape = new MaterialShapeDrawable(shapeAppearance);
    backgroundShape.initializeElevationOverlay(getContext());
    backgroundShape.setElevation(elevation);
    if (strokeWidth >= 0) {
      backgroundShape.setStroke(strokeWidth, strokeColor);
    }

    int rippleColor = MaterialColors.getColor(this, R.attr.colorControlHighlight);
    Drawable background;
    backgroundShape.setFillColor(ColorStateList.valueOf(backgroundColor));
    background =
        new RippleDrawable(ColorStateList.valueOf(rippleColor), backgroundShape, backgroundShape);
    setBackground(background);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (layoutInflated && centerView == null && !(child instanceof ActionMenuView)) {
      centerView = child;
      centerView.setAlpha(0);
    }
    super.addView(child, index, params);
  }

  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (backgroundShape != null) {
      backgroundShape.setElevation(elevation);
    }
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(EditText.class.getCanonicalName());
    info.setEditable(isEnabled());

    CharSequence text = getText();
    boolean isTextEmpty = TextUtils.isEmpty(text);
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      info.setHintText(getHint());
      info.setShowingHintText(isTextEmpty);
    }

    if (isTextEmpty) {
      text = getHint();
    }

    info.setText(text);
  }

  @Override
  public void setNavigationOnClickListener(OnClickListener listener) {
    if (forceDefaultNavigationOnClickListener) {
      // Ignore the listener if forcing of the default navigation icon is enabled.
      return;
    }
    super.setNavigationOnClickListener(listener);
    setNavigationIconDecorative(listener == null);
  }

  @Override
  public void setNavigationIcon(@Nullable Drawable navigationIcon) {
    super.setNavigationIcon(maybeTintNavigationIcon(navigationIcon));
  }

  @Nullable
  private Drawable maybeTintNavigationIcon(@Nullable Drawable navigationIcon) {
    if (!tintNavigationIcon || navigationIcon == null) {
      return navigationIcon;
    }

    int navigationIconColor;
    if (navigationIconTint != null) {
      navigationIconColor = navigationIconTint;
    } else {
      // Navigational icons such as the "hamburger", back arrow, etc. are supposed to be emphasized
      // with colorOnSurface as opposed to colorOnSurfaceVariant. Here we assume any icon that's not
      // the default search icon is navigational.
      int navigationIconColorAttr =
          navigationIcon == defaultNavigationIcon
              ? R.attr.colorOnSurfaceVariant
              : R.attr.colorOnSurface;
      navigationIconColor = MaterialColors.getColor(this, navigationIconColorAttr);
    }

    Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon.mutate());
    wrappedNavigationIcon.setTint(navigationIconColor);
    return wrappedNavigationIcon;
  }

  private void setNavigationIconDecorative(boolean decorative) {
    ImageButton navigationIconButton = ToolbarUtils.getNavigationIconButton(this);
    if (navigationIconButton == null) {
      return;
    }

    navigationIconButton.setClickable(!decorative);
    navigationIconButton.setFocusable(!decorative);

    Drawable navigationIconBackground = navigationIconButton.getBackground();
    if (navigationIconBackground != null) {
      // Save original navigation icon background so we can restore it later if needed.
      originalNavigationIconBackground = navigationIconBackground;
    }
    // Even if the navigation icon is not clickable/focusable, a ripple will still show up when the
    // parent view (overall search bar) is clicked. So here we set the background to null to avoid
    // that, and restore the original background when the icon becomes clickable.
    navigationIconButton.setBackgroundDrawable(
        decorative ? null : originalNavigationIconBackground);

    setHandwritingBoundsInsets();
  }

  @Override
  public void inflateMenu(@MenuRes int resId) {
    // Pause dispatching item changes during inflation to improve performance.
    Menu menu = getMenu();
    if (menu instanceof MenuBuilder) {
      ((MenuBuilder) menu).stopDispatchingItemsChanged();
    }
    super.inflateMenu(resId);
    this.menuResId = resId;
    if (menu instanceof MenuBuilder) {
      ((MenuBuilder) menu).startDispatchingItemsChanged();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    measureCenterView(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    layoutCenterView();
    setHandwritingBoundsInsets();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, backgroundShape);
    setDefaultMargins();
    setOrClearDefaultScrollFlags();
  }

  /**
   * {@link SearchBar} does not support the {@link Toolbar#setTitle} method, or its corresponding
   * xml attribute. Instead, use {@link #setHint} or {@link #setText}, or their corresponding xml
   * attributes, to provide a text affordance for your {@link SearchBar}.
   */
  @Override
  public void setTitle(CharSequence title) {
    // Don't do anything. SearchBar can't have a title.
    // Note: we can't throw an exception here because setTitle() is called by setSupportActionBar().
  }

  /**
   * {@link SearchBar} does not support the {@link Toolbar#setSubtitle} method, or its corresponding
   * xml attribute. Instead, use {@link #setHint} or {@link #setText}, or their corresponding xml
   * attributes, to provide a text affordance for your {@link SearchBar}.
   */
  @Override
  public void setSubtitle(CharSequence subtitle) {
    // Don't do anything. SearchBar can't have a subtitle.
    // Note: we can't throw an exception here because setSubtitle() is called by
    // ActionBar#setDisplayShowTitleEnabled().
  }

  private void setDefaultMargins() {
    if (defaultMarginsEnabled && getLayoutParams() instanceof MarginLayoutParams) {
      Resources resources = getResources();
      int marginHorizontal =
          resources.getDimensionPixelSize(R.dimen.m3_searchbar_margin_horizontal);
      int marginVertical = resources.getDimensionPixelSize(getDefaultMarginVerticalResource());
      MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
      lp.leftMargin = defaultIfZero(lp.leftMargin, marginHorizontal);
      lp.topMargin = defaultIfZero(lp.topMargin, marginVertical);
      lp.rightMargin = defaultIfZero(lp.rightMargin, marginHorizontal);
      lp.bottomMargin = defaultIfZero(lp.bottomMargin, marginVertical);
    }
  }

  /** @hide */
  @DimenRes
  @RestrictTo(LIBRARY_GROUP)
  protected int getDefaultMarginVerticalResource() {
    return R.dimen.m3_searchbar_margin_vertical;
  }

  /** @hide */
  @DrawableRes
  @RestrictTo(LIBRARY_GROUP)
  protected int getDefaultNavigationIconResource() {
    return R.drawable.ic_search_black_24;
  }

  private int defaultIfZero(int value, int defValue) {
    return value == 0 ? defValue : value;
  }

  private void setOrClearDefaultScrollFlags() {
    if (getLayoutParams() instanceof AppBarLayout.LayoutParams) {
      AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) getLayoutParams();
      if (defaultScrollFlagsEnabled) {
        if (lp.getScrollFlags() == 0) {
          lp.setScrollFlags(DEFAULT_SCROLL_FLAGS);
        }
      } else {
        if (lp.getScrollFlags() == DEFAULT_SCROLL_FLAGS) {
          lp.setScrollFlags(0);
        }
      }
    }
  }

  private void measureCenterView(int widthMeasureSpec, int heightMeasureSpec) {
    if (centerView != null) {
      centerView.measure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private void layoutCenterView() {
    if (centerView == null) {
      return;
    }

    int centerViewWidth = centerView.getMeasuredWidth();
    int left = getMeasuredWidth() / 2 - centerViewWidth / 2;
    int right = left + centerViewWidth;

    int centerViewHeight = centerView.getMeasuredHeight();
    int top = getMeasuredHeight() / 2 - centerViewHeight / 2;
    int bottom = top + centerViewHeight;

    layoutChild(centerView, left, top, right, bottom);
  }

  private void layoutChild(View child, int left, int top, int right, int bottom) {
    if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      child.layout(getMeasuredWidth() - right, top, getMeasuredWidth() - left, bottom);
    } else {
      child.layout(left, top, right, bottom);
    }
  }

  private void setHandwritingBoundsInsets() {
    if (VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      return;
    }

    boolean isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

    // If the navigation icon is non-decorative, exclude it from the handwriting bounds.
    int startInset = 0;
    View navigationIconButton = ToolbarUtils.getNavigationIconButton(this);
    if (navigationIconButton != null && navigationIconButton.isClickable()) {
      startInset =
          isRtl ? (getWidth() - navigationIconButton.getLeft()) : navigationIconButton.getRight();
    }

    // Exclude the menu items from the handwriting bounds.
    int endInset = 0;
    View actionMenuView = ToolbarUtils.getActionMenuView(this);
    if (actionMenuView != null) {
      endInset = isRtl ? actionMenuView.getRight() : (getWidth() -  actionMenuView.getLeft());
    }

    setHandwritingBoundsOffsets(
        -(isRtl ? endInset : startInset), 0, -(isRtl ? startInset : endInset), 0);
  }

  /** Returns the optional centered child view of this {@link SearchBar} */
  @Nullable
  public View getCenterView() {
    return centerView;
  }

  /** Sets the center view as a child. Pass in null for {@code view} to remove the center view. */
  public void setCenterView(@Nullable View view) {
    if (centerView != null) {
      removeView(centerView);
      centerView = null;
    }
    if (view != null) {
      addView(view);
    }
  }

  /** Returns the main {@link TextView} which can be used for hint and search text. */
  @NonNull
  public TextView getTextView() {
    return textView;
  }

  /** Returns the text of main {@link TextView}, which usually represents the search text. */
  @NonNull // TextView.getText() never returns null after initialization.
  public CharSequence getText() {
    return textView.getText();
  }

  /** Sets the text of main {@link TextView}. */
  public void setText(@Nullable CharSequence text) {
    textView.setText(text);
  }

  /** Sets the text of main {@link TextView}. */
  public void setText(@StringRes int textResId) {
    textView.setText(textResId);
  }

  /** Clears the text of main {@link TextView}. */
  public void clearText() {
    textView.setText("");
  }

  /** Returns the hint of main {@link TextView}. */
  @Nullable
  public CharSequence getHint() {
    return textView.getHint();
  }

  /** Sets the hint of main {@link TextView}. */
  public void setHint(@Nullable CharSequence hint) {
    textView.setHint(hint);
  }

  /** Sets the hint of main {@link TextView}. */
  public void setHint(@StringRes int hintResId) {
    textView.setHint(hintResId);
  }

  /** Returns the color of the {@link SearchBar} outline stroke. */
  @ColorInt
  public int getStrokeColor() {
    return backgroundShape.getStrokeColor().getDefaultColor();
  }

  /** Sets the color of the {@link SearchBar} outline stroke. */
  public void setStrokeColor(@ColorInt int strokeColor) {
    if (getStrokeColor() != strokeColor) {
      backgroundShape.setStrokeColor(ColorStateList.valueOf(strokeColor));
    }
  }

  /** Returns the width in pixels of the {@link SearchBar} outline stroke. */
  @Dimension
  public float getStrokeWidth() {
    return backgroundShape.getStrokeWidth();
  }

  /** Sets the width in pixels of the {@link SearchBar} outline stroke. */
  public void setStrokeWidth(@Dimension float strokeWidth) {
    if (getStrokeWidth() != strokeWidth) {
      backgroundShape.setStrokeWidth(strokeWidth);
    }
  }

  /** Returns the size in pixels of the {@link SearchBar} corners. */
  public float getCornerSize() {
    // Assume all corner sizes are the same.
    return backgroundShape.getTopLeftCornerResolvedSize();
  }

  /**
   * Returns whether the default {@link AppBarLayout} scroll flags are enabled. See {@link
   * SearchBar#DEFAULT_SCROLL_FLAGS}.
   */
  public boolean isDefaultScrollFlagsEnabled() {
    return defaultScrollFlagsEnabled;
  }

  /**
   * Sets whether the default {@link AppBarLayout} scroll flags are enabled. See {@link
   * SearchBar#DEFAULT_SCROLL_FLAGS}.
   */
  public void setDefaultScrollFlagsEnabled(boolean defaultScrollFlagsEnabled) {
    this.defaultScrollFlagsEnabled = defaultScrollFlagsEnabled;
    setOrClearDefaultScrollFlags();
  }

  /**
   * Starts the on load animation which transitions from the center view to the hint {@link
   * TextView}.
   */
  public void startOnLoadAnimation() {
    // Use a post() to make sure the SearchBar's menu is inflated before the animation starts.
    post(() -> searchBarAnimationHelper.startOnLoadAnimation(this));
  }

  /**
   * Stops the on load animation which transitions from the center view to the hint {@link
   * TextView}.
   */
  public void stopOnLoadAnimation() {
    searchBarAnimationHelper.stopOnLoadAnimation(this);
  }

  /** Returns whether the fade in part is enabled for the on load animation. */
  public boolean isOnLoadAnimationFadeInEnabled() {
    return searchBarAnimationHelper.isOnLoadAnimationFadeInEnabled();
  }

  /** Sets whether the fade in part is enabled for the on load animation. */
  public void setOnLoadAnimationFadeInEnabled(boolean onLoadAnimationFadeInEnabled) {
    searchBarAnimationHelper.setOnLoadAnimationFadeInEnabled(onLoadAnimationFadeInEnabled);
  }

  /**
   * Registers a callback for the On Load Animation, started and stopped via {@link
   * #startOnLoadAnimation()} and {@link #stopOnLoadAnimation()}.
   */
  public void addOnLoadAnimationCallback(
      @NonNull OnLoadAnimationCallback onLoadAnimationCallback) {
    searchBarAnimationHelper.addOnLoadAnimationCallback(onLoadAnimationCallback);
  }

  /**
   * Unregisters a callback for the On Load Animation, started and stopped via {@link
   * #startOnLoadAnimation()} and {@link #stopOnLoadAnimation()}.
   */
  public boolean removeOnLoadAnimationCallback(
      @NonNull OnLoadAnimationCallback onLoadAnimationCallback) {
    return searchBarAnimationHelper.removeOnLoadAnimationCallback(onLoadAnimationCallback);
  }

  /** Returns whether the expand animation is running. */
  public boolean isExpanding() {
    return searchBarAnimationHelper.isExpanding();
  }

  /** See {@link SearchBar#expand(View, AppBarLayout, boolean)}. */
  @CanIgnoreReturnValue
  public boolean expand(@NonNull View expandedView) {
    return expand(expandedView, /* appBarLayout= */ null);
  }

  /** See {@link SearchBar#expand(View, AppBarLayout, boolean)}. */
  @CanIgnoreReturnValue
  public boolean expand(@NonNull View expandedView, @Nullable AppBarLayout appBarLayout) {
    return expand(expandedView, appBarLayout, /* skipAnimation= */ false);
  }

  /**
   * Starts an expand animation, if it's not already started, which transitions from the {@link
   * SearchBar} to the {@code expandedView}, e.g., a contextual {@link Toolbar}.
   *
   * <p>Note: If you are using an {@link AppBarLayout} in conjunction with the {@link SearchBar},
   * you may pass in a reference to your {@link AppBarLayout} so that its visibility and offset can
   * be taken into account for the animation.
   *
   * @return whether or not the expand animation was started
   */
  @CanIgnoreReturnValue
  public boolean expand(
      @NonNull View expandedView, @Nullable AppBarLayout appBarLayout, boolean skipAnimation) {
    // Start the expand if the expanded view is not already showing or in the process of expanding,
    // or if the expanded view is collapsing since the final state should be expanded.
    if ((expandedView.getVisibility() != View.VISIBLE && !isExpanding()) || isCollapsing()) {
      searchBarAnimationHelper.startExpandAnimation(
          this, expandedView, appBarLayout, skipAnimation);
      return true;
    }
    return false;
  }

  /**
   * Adds a listener for the expand animation started via {@link #expand(View)} and {@link
   * #expand(View, AppBarLayout)}.
   */
  public void addExpandAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    searchBarAnimationHelper.addExpandAnimationListener(listener);
  }

  /**
   * Removes a listener for the expand animation started via {@link #expand(View)} and {@link
   * #expand(View, AppBarLayout)}.
   *
   * @return true if a listener was removed as a result of this call
   */
  public boolean removeExpandAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    return searchBarAnimationHelper.removeExpandAnimationListener(listener);
  }

  /** Returns whether the collapse animation is running. */
  public boolean isCollapsing() {
    return searchBarAnimationHelper.isCollapsing();
  }

  /** See {@link SearchBar#collapse(View, AppBarLayout, boolean)}. */
  @CanIgnoreReturnValue
  public boolean collapse(@NonNull View expandedView) {
    return collapse(expandedView, /* appBarLayout= */ null);
  }

  /** See {@link SearchBar#collapse(View, AppBarLayout, boolean)}. */
  @CanIgnoreReturnValue
  public boolean collapse(@NonNull View expandedView, @Nullable AppBarLayout appBarLayout) {
    return collapse(expandedView, /* appBarLayout= */ appBarLayout, /* skipAnimation= */ false);
  }

  /**
   * Starts a collapse animation, if it's not already started, which transitions from the {@code
   * expandedView}, e.g., a contextual {@link Toolbar}, to the {@link SearchBar}.
   *
   * <p>Note: If you are using an {@link AppBarLayout} in conjunction with the {@link SearchBar},
   * you may pass in a reference to your {@link AppBarLayout} so that its visibility and offset can
   * be taken into account for the animation.
   *
   * @return whether or not the collapse animation was started
   */
  @CanIgnoreReturnValue
  public boolean collapse(
      @NonNull View expandedView, @Nullable AppBarLayout appBarLayout, boolean skipAnimation) {
    // Start the collapse if the expanded view is showing and not in the process of collapsing, or
    // if the expanded view is expanding since the final state should be collapsed.
    if ((expandedView.getVisibility() == View.VISIBLE && !isCollapsing()) || isExpanding()) {
      searchBarAnimationHelper.startCollapseAnimation(
          this, expandedView, appBarLayout, skipAnimation);
      return true;
    }
    return false;
  }

  /**
   * Adds a listener for the collapse animation started via {@link #collapse(View)} and {@link
   * #collapse(View, AppBarLayout)}.
   */
  public void addCollapseAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    searchBarAnimationHelper.addCollapseAnimationListener(listener);
  }

  /**
   * Removes a listener for the collapse animation started via {@link #collapse(View)} and {@link
   * #collapse(View, AppBarLayout)}.
   *
   * @return true if a listener was removed as a result of this call
   */
  public boolean removeCollapseAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    return searchBarAnimationHelper.removeCollapseAnimationListener(listener);
  }

  int getMenuResId() {
    return menuResId;
  }

  float getCompatElevation() {
    return backgroundShape != null ? backgroundShape.getElevation() : getElevation();
  }

  /** Behavior that sets up the scroll-away mode for an {@link SearchBar}. */
  public static class ScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private boolean initialized = false;

    public ScrollingViewBehavior() {}

    public ScrollingViewBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      boolean changed = super.onDependentViewChanged(parent, child, dependency);
      if (!initialized && dependency instanceof AppBarLayout) {
        initialized = true;
        AppBarLayout appBarLayout = (AppBarLayout) dependency;
        setAppBarLayoutTransparent(appBarLayout);
      }
      return changed;
    }

    private void setAppBarLayoutTransparent(AppBarLayout appBarLayout) {
      appBarLayout.setBackgroundColor(Color.TRANSPARENT);

      // Remove AppBarLayout elevation shadow
      if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
        // Workaround for elevation crash that only happens on Android 5.0
        // Similar to https://stackoverflow.com/q/40928788
        appBarLayout.setOutlineProvider(null);
      } else {
        appBarLayout.setTargetElevation(0);
      }
    }

    @Override
    protected boolean shouldHeaderOverlapScrollingChild() {
      return true;
    }
  }

  /**
   * Callback for the animation started and stopped via {@link #startOnLoadAnimation()} and {@link
   * #stopOnLoadAnimation()}.
   */
  public abstract static class OnLoadAnimationCallback {
    public void onAnimationStart() {}

    public void onAnimationEnd() {}
  }

  @Override
  @NonNull
  protected Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState(super.onSaveInstanceState());
    CharSequence text = getText();
    savedState.text = text == null ? null : text.toString();
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
    setText(savedState.text);
  }

  static class SavedState extends AbsSavedState {

    String text;

    public SavedState(Parcel source) {
      this(source, null);
    }

    public SavedState(Parcel source, @Nullable ClassLoader classLoader) {
      super(source, classLoader);
      text = source.readString();
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public static final Parcelable.Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {

          @Override
          public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source, loader);
          }

          @Override
          public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeString(text);
    }
  }
}
