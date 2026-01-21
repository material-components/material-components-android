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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.BackEventCompat;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.internal.ClippableRoundedCornerLayout;
import com.google.android.material.internal.ContextUtils;
import com.google.android.material.internal.FadeThroughDrawable;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ToolbarUtils;
import com.google.android.material.internal.TouchObserverFrameLayout;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MaterialBackHandler;
import com.google.android.material.motion.MaterialBackOrchestrator;
import com.google.android.material.motion.MaterialMainContainerBackHelper;
import com.google.android.material.shape.MaterialShapeUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Layout that provides a full screen search view and can be used with {@link SearchBar}.
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
 * developer guidance</a> and <a href="https://material.io/components/search/overview">design
 * guidelines</a>.
 */
@SuppressWarnings("RestrictTo")
public class SearchView extends FrameLayout
    implements CoordinatorLayout.AttachedBehavior, MaterialBackHandler {

  private static final long TALKBACK_FOCUS_CHANGE_DELAY_MS = 100;
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_SearchView;

  final View scrim;
  final ClippableRoundedCornerLayout rootView;
  final View backgroundView;
  final View statusBarSpacer;
  final FrameLayout headerContainer;
  final FrameLayout toolbarContainer;
  final MaterialToolbar toolbar;
  final Toolbar dummyToolbar;
  final TextView searchPrefix;
  final LinearLayout textContainer;
  final EditText editText;
  final ImageButton clearButton;
  final View divider;
  final TouchObserverFrameLayout contentContainer;

  private final boolean layoutInflated;
  private final SearchViewAnimationHelper searchViewAnimationHelper;

  @NonNull
  private final MaterialBackOrchestrator backOrchestrator = new MaterialBackOrchestrator(this);

  private final boolean backHandlingEnabled;
  private final ElevationOverlayProvider elevationOverlayProvider;
  private final Set<TransitionListener> transitionListeners = new LinkedHashSet<>();

  @Nullable private SearchBar searchBar;
  private int softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
  private boolean animatedNavigationIcon;
  private boolean animatedMenuItems;
  private boolean autoShowKeyboard;
  @ColorInt private final int backgroundColor;
  private boolean useWindowInsetsController;
  private boolean statusBarSpacerEnabledOverride;
  private final boolean dividerVisible;
  @NonNull private TransitionState currentTransitionState = TransitionState.HIDDEN;
  private Map<View, Integer> childImportantForAccessibilityMap;
  private final OnTouchModeChangeListener touchModeChangeListener =
      new OnTouchModeChangeListener() {
        @Override
        public void onTouchModeChanged(boolean isInTouchMode) {
          // If we enter non touch mode and the SearchView is showing in the currently focused
          // window, request focus on the EditText to prevent focusing views behind SearchView.
          if (!isInTouchMode
              && hasWindowFocus()
              && isShowing()
              && editText != null
              && !editText.isFocused()) {
            editText.post(editText::requestFocus);
          }
        }
      };

  public SearchView(@NonNull Context context) {
    this(context, null);
  }

  public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialSearchViewStyle);
  }

  public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.SearchView, defStyleAttr, DEF_STYLE_RES);

    backgroundColor = a.getColor(R.styleable.SearchView_backgroundTint, 0);
    int headerLayoutResId = a.getResourceId(R.styleable.SearchView_headerLayout, -1);
    int textAppearanceResId = a.getResourceId(R.styleable.SearchView_android_textAppearance, -1);
    String text = a.getString(R.styleable.SearchView_android_text);
    String hint = a.getString(R.styleable.SearchView_android_hint);
    String searchPrefixText = a.getString(R.styleable.SearchView_searchPrefixText);
    boolean useDrawerArrowDrawable =
        a.getBoolean(R.styleable.SearchView_useDrawerArrowDrawable, false);
    animatedNavigationIcon = a.getBoolean(R.styleable.SearchView_animateNavigationIcon, true);
    animatedMenuItems = a.getBoolean(R.styleable.SearchView_animateMenuItems, true);
    boolean hideNavigationIcon = a.getBoolean(R.styleable.SearchView_hideNavigationIcon, false);
    autoShowKeyboard = a.getBoolean(R.styleable.SearchView_autoShowKeyboard, true);
    backHandlingEnabled = a.getBoolean(R.styleable.SearchView_backHandlingEnabled, true);
    dividerVisible = a.getBoolean(R.styleable.SearchView_dividerVisible, true);

    a.recycle();

    LayoutInflater.from(context).inflate(R.layout.mtrl_search_view, this);
    layoutInflated = true;

    scrim = findViewById(R.id.open_search_view_scrim);
    rootView = findViewById(R.id.open_search_view_root);
    backgroundView = findViewById(R.id.open_search_view_background);
    statusBarSpacer = findViewById(R.id.open_search_view_status_bar_spacer);
    headerContainer = findViewById(R.id.open_search_view_header_container);
    toolbarContainer = findViewById(R.id.open_search_view_toolbar_container);
    toolbar = findViewById(R.id.open_search_view_toolbar);
    dummyToolbar = findViewById(R.id.open_search_view_dummy_toolbar);
    searchPrefix = findViewById(R.id.open_search_view_search_prefix);
    textContainer = findViewById(R.id.open_search_view_text_container);
    editText = findViewById(R.id.open_search_view_edit_text);
    clearButton = findViewById(R.id.open_search_view_clear_button);
    divider = findViewById(R.id.open_search_view_divider);
    contentContainer = findViewById(R.id.open_search_view_content_container);

    searchViewAnimationHelper = new SearchViewAnimationHelper(this);
    elevationOverlayProvider = new ElevationOverlayProvider(context);

    setUpRootView();
    setUpBackgroundViewElevationOverlay();
    setUpHeaderLayout(headerLayoutResId);
    setSearchPrefixText(searchPrefixText);
    setUpEditText(textAppearanceResId, text, hint);
    setUpBackButton(useDrawerArrowDrawable, hideNavigationIcon);
    setUpClearButton();
    setUpDivider();
    setUpContentOnTouchListener();
    setUpInsetListeners();

    // Necessary to enable keyboard navigation to the searchview contents due to toolbar being a
    // keyboard navigation cluster from API 26+
    setToolbarTouchscreenBlocksFocus(false);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (layoutInflated) {
      contentContainer.addView(child, index, params);
    } else {
      super.addView(child, index, params);
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    updateSoftInputMode();
  }

  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    setUpBackgroundViewElevationOverlay(elevation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
    TransitionState state = getCurrentTransitionState();
    updateModalForAccessibility(state);
    updateListeningForBackCallbacks(state);
    getViewTreeObserver().addOnTouchModeChangeListener(touchModeChangeListener);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    setModalForAccessibility(/* isSearchViewModal= */ false);
    backOrchestrator.stopListeningForBackCallbacks();
    getViewTreeObserver().removeOnTouchModeChangeListener(touchModeChangeListener);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<SearchView> getBehavior() {
    return new SearchView.Behavior();
  }

  @Override
  public void startBackProgress(@NonNull BackEventCompat backEvent) {
    if (isHiddenOrHiding() || searchBar == null) {
      return;
    }
    if (searchBar != null) {
      searchBar.setPlaceholderText(editText.getText().toString());
    }
    searchViewAnimationHelper.startBackProgress(backEvent);
  }

  @Override
  public void updateBackProgress(@NonNull BackEventCompat backEvent) {
    if (isHiddenOrHiding()
        || searchBar == null
        || VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      return;
    }
    searchViewAnimationHelper.updateBackProgress(backEvent);
  }

  @Override
  public void handleBackInvoked() {
    if (isHiddenOrHiding()) {
      return;
    }

    BackEventCompat backEvent = searchViewAnimationHelper.onHandleBackInvoked();
    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE
        && searchBar != null
        && backEvent != null) {
      searchViewAnimationHelper.finishBackProgress();
    } else {
      hide();
    }
  }

  @Override
  public void cancelBackProgress() {
    if (isHiddenOrHiding()
        || searchBar == null
        || VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      return;
    }
    searchViewAnimationHelper.cancelBackProgress();
  }

  @VisibleForTesting
  MaterialMainContainerBackHelper getBackHelper() {
    return searchViewAnimationHelper.getBackHelper();
  }

  private boolean isHiddenOrHiding() {
    return currentTransitionState.equals(TransitionState.HIDDEN)
        || currentTransitionState.equals(TransitionState.HIDING);
  }

  @Nullable
  private Window getActivityWindow() {
    Activity activity = ContextUtils.getActivity(getContext());
    return activity == null ? null : activity.getWindow();
  }

  @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate.
  private void setUpRootView() {
    rootView.setOnTouchListener((v, event) -> true);
  }

  private void setUpBackgroundViewElevationOverlay() {
    setUpBackgroundViewElevationOverlay(getOverlayElevation());
  }

  private void setUpBackgroundViewElevationOverlay(float elevation) {
    if (elevationOverlayProvider == null || backgroundView == null) {
      return;
    }
    int backgroundColorWithOverlay =
        elevationOverlayProvider.compositeOverlayIfNeeded(backgroundColor, elevation);
    backgroundView.setBackgroundColor(backgroundColorWithOverlay);
  }

  private float getOverlayElevation() {
    if (searchBar != null) {
      return searchBar.getCompatElevation();
    } else {
      return getResources().getDimension(R.dimen.m3_searchview_elevation);
    }
  }

  private void setUpHeaderLayout(int headerLayoutResId) {
    if (headerLayoutResId != -1) {
      View headerView =
          LayoutInflater.from(getContext()).inflate(headerLayoutResId, headerContainer, false);
      addHeaderView(headerView);
    }
  }

  private void setUpEditText(@StyleRes int textAppearanceResId, String text, String hint) {
    if (textAppearanceResId != -1) {
      TextViewCompat.setTextAppearance(editText, textAppearanceResId);
    }
    editText.setText(text);
    editText.setHint(hint);
  }

  private void setUpBackButton(boolean useDrawerArrowDrawable, boolean hideNavigationIcon) {
    if (hideNavigationIcon) {
      toolbar.setNavigationIcon(null);
      return;
    }

    toolbar.setNavigationOnClickListener(v -> hide());

    if (useDrawerArrowDrawable) {
      DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(getContext());
      drawerArrowDrawable.setColor(MaterialColors.getColor(this, R.attr.colorOnSurface));
      toolbar.setNavigationIcon(drawerArrowDrawable);
    }
  }

  private void setUpClearButton() {
    clearButton.setOnClickListener(
        v -> {
          clearText();
          requestFocusAndShowKeyboardIfNeeded();
        });

    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            clearButton.setVisibility(s.length() > 0 ? VISIBLE : GONE);
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }

  private void setUpDivider() {
    divider.setVisibility(dividerVisible ? VISIBLE : GONE);
  }

  @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate.
  private void setUpContentOnTouchListener() {
    contentContainer.setOnTouchListener(
        (v, event) -> {
          if (isAdjustNothingSoftInputMode()) {
            clearFocusAndHideKeyboard();
          }
          return false;
        });
  }

  private void setUpStatusBarSpacer(@Px int height) {
    if (statusBarSpacer.getLayoutParams().height != height) {
      statusBarSpacer.getLayoutParams().height = height;
      statusBarSpacer.requestLayout();
    }
  }

  @Px
  private int getStatusBarHeight() {
    @SuppressLint({
      "DiscouragedApi",
      "InternalInsetResource"
    }) // Used for initial value. A WindowInsetsListener will apply correct insets later.
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      return getResources().getDimensionPixelSize(resourceId);
    } else {
      return 0;
    }
  }

  /**
   * Note: DrawerArrowDrawable supports RTL, so there is no need to update the navigation icon for
   * RTL if it is a DrawerArrowDrawable.
   */
  private void updateNavigationIconIfNeeded() {
    if (toolbar == null) {
      return;
    }

    if (isNavigationIconDrawerArrowDrawable(toolbar)) {
      return;
    }

    int navigationIcon = getDefaultNavigationIconResource();
    if (searchBar == null) {
      toolbar.setNavigationIcon(navigationIcon);
    } else {
      Drawable navigationIconDrawable =
          DrawableCompat.wrap(
              AppCompatResources.getDrawable(getContext(), navigationIcon).mutate());
      if (toolbar.getNavigationIconTint() != null) {
        navigationIconDrawable.setTint(toolbar.getNavigationIconTint());
      }
      DrawableCompat.setLayoutDirection(navigationIconDrawable, getLayoutDirection());
      toolbar.setNavigationIcon(
          new FadeThroughDrawable(searchBar.getNavigationIcon(), navigationIconDrawable));
      updateNavigationIconProgressIfNeeded();
    }
  }

  private boolean isNavigationIconDrawerArrowDrawable(@NonNull Toolbar toolbar) {
    return DrawableCompat.unwrap(toolbar.getNavigationIcon()) instanceof DrawerArrowDrawable;
  }

  /**
   * Listens to {@link WindowInsetsCompat} and adjusts layouts accordingly.
   *
   * <p><b>NOTE</b>: window insets are only delivered if no other layout consumed them before. E.g.:
   *
   * <ul>
   *   <li>by declaring {@code fitsSystemWindows=true}
   *   <li>by consuming insets via specific consume-methods (e.g. {@link
   *       WindowInsetsCompat#consumeSystemWindowInsets()}
   * </ul>
   */
  private void setUpInsetListeners() {
    setUpToolbarInsetListener();
    setUpDividerInsetListener();
    setUpStatusBarSpacerInsetListener();
  }

  private void setUpToolbarInsetListener() {
    ViewUtils.doOnApplyWindowInsets(
        toolbar,
        (view, insets, initialPadding) -> {
          boolean isRtl = ViewUtils.isLayoutRtl(toolbar);
          int paddingLeft = isRtl ? initialPadding.end : initialPadding.start;
          int paddingRight = isRtl ? initialPadding.start : initialPadding.end;
          Insets systemBarCutoutInsets =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
          paddingLeft += systemBarCutoutInsets.left;
          paddingRight += systemBarCutoutInsets.right;

          toolbar.setPadding(paddingLeft, initialPadding.top, paddingRight, initialPadding.bottom);
          return insets;
        });
  }

  private void setUpStatusBarSpacerInsetListener() {
    // Set an initial height based on the default system value to support pre-L behavior.
    setUpStatusBarSpacer(getStatusBarHeight());

    // Listen to system window insets on L+ and adjusts status bar height based on the top inset.
    ViewCompat.setOnApplyWindowInsetsListener(
        statusBarSpacer,
        (v, insets) -> {
          int systemWindowInsetTop =
              insets.getInsets(
                      WindowInsetsCompat.Type.systemBars()
                          | WindowInsetsCompat.Type.displayCutout())
                  .top;
          setUpStatusBarSpacer(systemWindowInsetTop);
          if (!statusBarSpacerEnabledOverride) {
            setStatusBarSpacerEnabledInternal(systemWindowInsetTop > 0);
          }
          return insets;
        });
  }

  private void setUpDividerInsetListener() {
    MarginLayoutParams layoutParams = (MarginLayoutParams) divider.getLayoutParams();
    int leftMargin = layoutParams.leftMargin;
    int rightMargin = layoutParams.rightMargin;
    ViewCompat.setOnApplyWindowInsetsListener(
        divider,
        (v, insets) -> {
          Insets systemBarCutoutInsets =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
          layoutParams.leftMargin = leftMargin + systemBarCutoutInsets.left;
          layoutParams.rightMargin = rightMargin + systemBarCutoutInsets.right;
          return insets;
        });
  }

  /** Returns whether or not this {@link SearchView} is set up with an {@link SearchBar}. */
  public boolean isSetupWithSearchBar() {
    return this.searchBar != null;
  }

  /**
   * Sets up this {@link SearchView} with an {@link SearchBar}, which will result in the {@link
   * SearchView} being shown when the {@link SearchBar} is clicked. This behavior will be set up
   * automatically if the {@link SearchBar} and {@link SearchView} are in a {@link
   * CoordinatorLayout} and the {@link SearchView} is anchored to the {@link SearchBar}.
   */
  public void setupWithSearchBar(@Nullable SearchBar searchBar) {
    this.searchBar = searchBar;
    searchViewAnimationHelper.setSearchBar(searchBar);
    if (searchBar != null) {
      searchBar.setOnClickListener(v -> show());
      if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
        try {
          searchBar.setHandwritingDelegatorCallback(this::show);
          editText.setIsHandwritingDelegate(true);
        } catch (LinkageError e) {
          // Running on a device with an older build of Android U
          // TODO(b/274154553): Remove try/catch block after Android U Beta 1 is released
        }
      }
    }
    updateNavigationIconIfNeeded();
    setUpBackgroundViewElevationOverlay();
    updateListeningForBackCallbacks(getCurrentTransitionState());
  }

  /**
   * Add a header view to this {@link SearchView}, which will be placed above the search text area.
   *
   * <p>Note: due to complications with the expand/collapse animation, a header view is intended to
   * be used with a standalone {@link SearchView} which slides up/down instead of morphing from an
   * {@link SearchBar}.
   */
  public void addHeaderView(@NonNull View headerView) {
    headerContainer.addView(headerView);
    headerContainer.setVisibility(VISIBLE);
  }

  /** Remove a header view from the section above the search text area. */
  public void removeHeaderView(@NonNull View headerView) {
    headerContainer.removeView(headerView);
    if (headerContainer.getChildCount() == 0) {
      headerContainer.setVisibility(GONE);
    }
  }

  /** Remove all header views from the section above the search text area. */
  public void removeAllHeaderViews() {
    headerContainer.removeAllViews();
    headerContainer.setVisibility(GONE);
  }

  /**
   * Sets whether the navigation icon should be animated from the {@link SearchBar} to {@link
   * SearchView}.
   */
  public void setAnimatedNavigationIcon(boolean animatedNavigationIcon) {
    this.animatedNavigationIcon = animatedNavigationIcon;
  }

  /**
   * Returns whether the navigation icon should be animated from the {@link SearchBar} to {@link
   * SearchView}.
   */
  public boolean isAnimatedNavigationIcon() {
    return animatedNavigationIcon;
  }

  /**
   * Sets whether the menu items should be animated from the {@link SearchBar} to {@link
   * SearchView}.
   */
  public void setMenuItemsAnimated(boolean menuItemsAnimated) {
    this.animatedMenuItems = menuItemsAnimated;
  }

  /**
   * Returns whether the menu items should be animated from the {@link SearchBar} to {@link
   * SearchView}.
   */
  public boolean isMenuItemsAnimated() {
    return animatedMenuItems;
  }

  /** Sets whether the soft keyboard should be shown when the {@link SearchView} is shown. */
  public void setAutoShowKeyboard(boolean autoShowKeyboard) {
    this.autoShowKeyboard = autoShowKeyboard;
  }

  /** Returns whether the soft keyboard should be shown when the {@link SearchView} is shown. */
  public boolean isAutoShowKeyboard() {
    return autoShowKeyboard;
  }

  /**
   * Sets whether the soft keyboard should be shown with {@code WindowInsetsController}.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setUseWindowInsetsController(boolean useWindowInsetsController) {
    this.useWindowInsetsController = useWindowInsetsController;
  }

  /**
   * Returns whether the soft keyboard should be shown with {@code WindowInsetsController}.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isUseWindowInsetsController() {
    return useWindowInsetsController;
  }

  /** Adds a listener to handle {@link SearchView} transitions such as showing and closing. */
  public void addTransitionListener(@NonNull TransitionListener transitionListener) {
    transitionListeners.add(transitionListener);
  }

  /** Removes a listener to handle {@link SearchView} transitions such as showing and closing. */
  public void removeTransitionListener(@NonNull TransitionListener transitionListener) {
    transitionListeners.remove(transitionListener);
  }

  /** Inflate a menu to provide additional options. */
  public void inflateMenu(@MenuRes int menuResId) {
    toolbar.inflateMenu(menuResId);
  }

  /** Set a listener to handle menu item clicks. */
  public void setOnMenuItemClickListener(
      @Nullable OnMenuItemClickListener onMenuItemClickListener) {
    toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
  }

  /** Returns the search prefix {@link TextView}, which appears before the main {@link EditText}. */
  @NonNull
  public TextView getSearchPrefix() {
    return searchPrefix;
  }

  /** Sets the search prefix text. */
  public void setSearchPrefixText(@Nullable CharSequence searchPrefixText) {
    searchPrefix.setText(searchPrefixText);
    searchPrefix.setVisibility(TextUtils.isEmpty(searchPrefixText) ? GONE : VISIBLE);
  }

  /** Returns the search prefix text. */
  @Nullable
  public CharSequence getSearchPrefixText() {
    return searchPrefix.getText();
  }

  /** Returns the {@link Toolbar} used by the {@link SearchView}. */
  @NonNull
  public Toolbar getToolbar() {
    return toolbar;
  }

  /** Returns the container view containing the non-scrim content of the {@link SearchView}. */
  @NonNull
  public View getSearchContainer() {
    return rootView;
  }

  /** Returns the main {@link EditText} which can be used for hint and search text. */
  @NonNull
  public EditText getEditText() {
    return editText;
  }

  /** Returns the text of main {@link EditText}, which usually represents the search text. */
  @SuppressLint("KotlinPropertyAccess") // Editable extends CharSequence.
  @NonNull // EditText never returns null after initialization.
  public Editable getText() {
    return editText.getText();
  }

  /** Sets the text of main {@link EditText}. */
  @SuppressLint("KotlinPropertyAccess") // Editable extends CharSequence.
  public void setText(@Nullable CharSequence text) {
    editText.setText(text);
  }

  /** Sets the text of main {@link EditText}. */
  public void setText(@StringRes int textResId) {
    editText.setText(textResId);
  }

  /** Clears the text of main {@link EditText}. */
  public void clearText() {
    editText.setText("");
  }

  /** Returns the hint of main {@link EditText}. */
  @Nullable
  public CharSequence getHint() {
    return editText.getHint();
  }

  /** Sets the hint of main {@link EditText}. */
  public void setHint(@Nullable CharSequence hint) {
    editText.setHint(hint);
  }

  /** Sets the hint of main {@link EditText}. */
  public void setHint(@StringRes int hintResId) {
    editText.setHint(hintResId);
  }

  /** Returns the current value of this {@link SearchView}'s soft input mode. */
  @SuppressLint("KotlinPropertyAccess") // This is a not property.
  public int getSoftInputMode() {
    return softInputMode;
  }

  /**
   * Sets the soft input mode for this {@link SearchView}. This is important because the {@link
   * SearchView} will use this to determine whether the keyboard should be shown/hidden at the same
   * time as the expand/collapse animation, or if the keyboard should be staggered with the
   * animation to avoid glitchiness due to a resize of the screen. This will be set automatically by
   * the {@link SearchView} during initial render but make sure to invoke this if you are changing
   * the soft input mode at runtime.
   */
  public void updateSoftInputMode() {
    Window window = getActivityWindow();
    if (window != null) {
      this.softInputMode = window.getAttributes().softInputMode;
    }
  }

  /**
   * Enables/disables the status bar spacer, which can be used in cases where the status bar is
   * translucent and the {@link SearchView} should not overlap the status bar area. This will be set
   * automatically by the {@link SearchView} during initial render, but make sure to invoke this if
   * you would like to override the default behavior.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setStatusBarSpacerEnabled(boolean enabled) {
    statusBarSpacerEnabledOverride = true;
    setStatusBarSpacerEnabledInternal(enabled);
  }

  private void setStatusBarSpacerEnabledInternal(boolean enabled) {
    statusBarSpacer.setVisibility(enabled ? VISIBLE : GONE);
  }

  /** Returns the current {@link TransitionState} for this {@link SearchView}. */
  @NonNull
  public TransitionState getCurrentTransitionState() {
    return currentTransitionState;
  }

  void setTransitionState(@NonNull TransitionState state) {
    setTransitionState(state, /* updateModalForAccessibility= */ true);
  }

  private void setTransitionState(
      @NonNull TransitionState state, boolean updateModalForAccessibility) {
    if (currentTransitionState.equals(state)) {
      return;
    }

    if (updateModalForAccessibility) {
      updateModalForAccessibility(state);
    }

    TransitionState previousState = currentTransitionState;
    currentTransitionState = state;
    Set<TransitionListener> listeners = new LinkedHashSet<>(transitionListeners);
    for (TransitionListener listener : listeners) {
      listener.onStateChanged(this, previousState, state);
    }

    updateListeningForBackCallbacks(state);

    if (searchBar != null && state == TransitionState.HIDDEN) {
      searchBar.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }
  }

  private void updateModalForAccessibility(@NonNull TransitionState state) {
    if (state == TransitionState.SHOWN) {
      setModalForAccessibility(true);
    } else if (state == TransitionState.HIDDEN) {
      setModalForAccessibility(false);
    }
  }

  private void updateListeningForBackCallbacks(@NonNull TransitionState state) {
    // Only automatically handle back if we have a search bar to collapse to, and if back handling
    // is enabled for the SearchView.
    if (searchBar != null && backHandlingEnabled) {
      if (state.equals(TransitionState.SHOWN)) {
        backOrchestrator.startListeningForBackCallbacks();
      } else if (state.equals(TransitionState.HIDDEN)) {
        backOrchestrator.stopListeningForBackCallbacks();
      }
    }
  }

  /** Returns whether the {@link SearchView}'s main content view is shown or showing. */
  public boolean isShowing() {
    return currentTransitionState.equals(TransitionState.SHOWN)
        || currentTransitionState.equals(TransitionState.SHOWING);
  }

  /**
   * Shows the {@link SearchView} with an animation.
   *
   * <p>Note: the show animation will not be started if the {@link SearchView} is currently shown or
   * showing.
   */
  public void show() {
    if (currentTransitionState.equals(TransitionState.SHOWN)
        || currentTransitionState.equals(TransitionState.SHOWING)) {
      return;
    }
    searchViewAnimationHelper.show();
  }

  /**
   * Hides the {@link SearchView} with an animation.
   *
   * <p>Note: the hide animation will not be started if the {@link SearchView} is currently hidden
   * or hiding.
   */
  public void hide() {
    if (currentTransitionState.equals(TransitionState.HIDDEN)
        || currentTransitionState.equals(TransitionState.HIDING)) {
      return;
    }
    if (searchBar != null && searchBar.isAttachedToWindow()) {
      searchBar.setPlaceholderText(editText.getText().toString());
      searchBar.post(searchViewAnimationHelper::hide);
    } else {
      searchViewAnimationHelper.hide();
    }
  }

  /** Updates the visibility of the {@link SearchView} without an animation. */
  public void setVisible(boolean visible) {
    boolean wasVisible = rootView.getVisibility() == VISIBLE;
    rootView.setVisibility(visible ? VISIBLE : GONE);
    updateNavigationIconProgressIfNeeded();
    setTransitionState(
        visible ? TransitionState.SHOWN : TransitionState.HIDDEN,
        /* updateModalForAccessibility= */ wasVisible != visible);
  }

  private void updateNavigationIconProgressIfNeeded() {
    ImageButton backButton = ToolbarUtils.getNavigationIconButton(toolbar);
    if (backButton == null) {
      return;
    }

    int progress = rootView.getVisibility() == VISIBLE ? 1 : 0;
    Drawable drawable = DrawableCompat.unwrap(backButton.getDrawable());
    if (drawable instanceof DrawerArrowDrawable) {
      ((DrawerArrowDrawable) drawable).setProgress(progress);
    }
    if (drawable instanceof FadeThroughDrawable) {
      ((FadeThroughDrawable) drawable).setProgress(progress);
    }
  }

  /**
   * Requests focus on the main {@link EditText} and shows the soft keyboard if automatic showing of
   * the keyboard is enabled.
   */
  void requestFocusAndShowKeyboardIfNeeded() {
    if (autoShowKeyboard) {
      requestFocusAndShowKeyboard();
    } else if (!isInTouchMode()) {
      // We still want to request focus if we are in non-touch mode so that focus doesn't go
      // behind the searchview.
      editText.postDelayed(
          () -> {
            if (editText.requestFocus()) {
              // Workaround for talkback issue when clear button is clicked
              editText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
          },
          TALKBACK_FOCUS_CHANGE_DELAY_MS);
    }
  }

  /** Requests focus on the main {@link EditText} and shows the soft keyboard. */
  public void requestFocusAndShowKeyboard() {
    // Without a delay requesting focus on edit text fails when talkback is active.
    editText.postDelayed(
        () -> {
          if (editText.requestFocus()) {
            // Workaround for talkback issue when clear button is clicked
            editText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
          }
          ViewUtils.showKeyboard(editText, useWindowInsetsController);
        },
        TALKBACK_FOCUS_CHANGE_DELAY_MS);
  }

  /** Clears focus on the main {@link EditText} and hides the soft keyboard. */
  public void clearFocusAndHideKeyboard() {
    editText.post(
        () -> {
          editText.clearFocus();
          ViewUtils.hideKeyboard(editText, useWindowInsetsController);
        });
  }

  boolean isAdjustNothingSoftInputMode() {
    return softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
  }

  /**
   * Sets whether the {@link SearchView} is modal for accessibility, i.e., whether views that are
   * not nested within the {@link SearchView} are important for accessibility.
   */
  public void setModalForAccessibility(boolean isSearchViewModal) {
    ViewGroup rootView = (ViewGroup) this.getRootView();

    if (isSearchViewModal) {
      childImportantForAccessibilityMap = new HashMap<>(rootView.getChildCount());
    }
    updateChildImportantForAccessibility(rootView, isSearchViewModal);

    if (!isSearchViewModal) {
      // When SearchView is not modal, reset the important for accessibility map.
      childImportantForAccessibilityMap = null;
    }
  }

  /**
   * Sets the 'touchscreenBlocksFocus' attribute of the nested toolbar. This is set to 'false' by
   * default, which allows keyboard navigation between the search view toolbar and the search
   * results.
   */
  public void setToolbarTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
    toolbar.setTouchscreenBlocksFocus(touchscreenBlocksFocus);
  }

  @SuppressLint("InlinedApi") // View Compat will handle the differences.
  private void updateChildImportantForAccessibility(ViewGroup parent, boolean isSearchViewModal) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      final View child = parent.getChildAt(i);
      if (child == this) {
        continue;
      }

      if (child.findViewById(this.rootView.getId()) != null) {
        // If this child node contains SearchView, look at this node's children instead.
        updateChildImportantForAccessibility((ViewGroup) child, isSearchViewModal);
        continue;
      }

      if (!isSearchViewModal) {
        if (childImportantForAccessibilityMap != null
            && childImportantForAccessibilityMap.containsKey(child)) {
          // Restores the original important for accessibility value of the child view.
          child.setImportantForAccessibility(childImportantForAccessibilityMap.get(child));
        }
      } else {
        // Saves the important for accessibility value of the child view.
        childImportantForAccessibilityMap.put(child, child.getImportantForAccessibility());

        child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }
  }

  /**
   * Provides the resource identifier for the back arrow icon.
   *
   * @hide
   */
  @DrawableRes
  @RestrictTo(LIBRARY_GROUP)
  protected int getDefaultNavigationIconResource() {
    return R.drawable.ic_arrow_back_black_24;
  }

  /** Behavior that sets up an {@link SearchView} with an {@link SearchBar}. */
  public static class Behavior extends CoordinatorLayout.Behavior<SearchView> {

    public Behavior() {}

    public Behavior(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(
        @NonNull CoordinatorLayout parent, @NonNull SearchView child, @NonNull View dependency) {
      if (!child.isSetupWithSearchBar() && dependency instanceof SearchBar) {
        child.setupWithSearchBar((SearchBar) dependency);
      }
      return false;
    }
  }

  /** Callback interface that provides important transition events for a {@link SearchView}. */
  public interface TransitionListener {

    /** Called when the given {@link SearchView SearchView's} transition state has changed. */
    void onStateChanged(
        @NonNull SearchView searchView,
        @NonNull TransitionState previousState,
        @NonNull TransitionState newState);
  }

  /** Enum that defines the possible transition states of an {@link SearchView}. */
  public enum TransitionState {
    HIDING,
    HIDDEN,
    SHOWING,
    SHOWN,
  }

  @Override
  @NonNull
  protected Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState(super.onSaveInstanceState());
    CharSequence text = getText();
    savedState.text = text == null ? null : text.toString();
    savedState.visibility = rootView.getVisibility();
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
    setVisible(savedState.visibility == VISIBLE);
  }

  static class SavedState extends AbsSavedState {

    String text;
    int visibility;

    public SavedState(Parcel source) {
      this(source, null);
    }

    public SavedState(Parcel source, @Nullable ClassLoader classLoader) {
      super(source, classLoader);
      text = source.readString();
      visibility = source.readInt();
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
      dest.writeInt(visibility);
    }
  }
}
