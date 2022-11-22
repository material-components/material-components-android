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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
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
import com.google.android.material.shape.MaterialShapeUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Layout that provides a full screen search view and can be used with {@link SearchBar}. */
@SuppressWarnings("RestrictTo")
public class SearchView extends FrameLayout implements CoordinatorLayout.AttachedBehavior {

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
  final EditText editText;
  final ImageButton clearButton;
  final View divider;
  final TouchObserverFrameLayout contentContainer;

  private final boolean layoutInflated;
  private final SearchViewAnimationHelper searchViewAnimationHelper;
  private final ElevationOverlayProvider elevationOverlayProvider;
  private final Set<TransitionListener> transitionListeners = new LinkedHashSet<>();

  @Nullable private SearchBar searchBar;
  private int softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
  private boolean animatedNavigationIcon;
  private boolean animatedMenuItems;
  private boolean autoShowKeyboard;
  private boolean useWindowInsetsController;
  @NonNull private TransitionState currentTransitionState = TransitionState.HIDDEN;
  private Map<View, Integer> childImportantForAccessibilityMap;

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

    a.recycle();

    LayoutInflater.from(context).inflate(R.layout.mtrl_search_view, this);
    layoutInflated = true;

    scrim = findViewById(R.id.search_view_scrim);
    rootView = findViewById(R.id.search_view_root);
    backgroundView = findViewById(R.id.search_view_background);
    statusBarSpacer = findViewById(R.id.search_view_status_bar_spacer);
    headerContainer = findViewById(R.id.search_view_header_container);
    toolbarContainer = findViewById(R.id.search_view_toolbar_container);
    toolbar = findViewById(R.id.search_view_toolbar);
    dummyToolbar = findViewById(R.id.search_view_dummy_toolbar);
    searchPrefix = findViewById(R.id.search_view_search_prefix);
    editText = findViewById(R.id.search_view_edit_text);
    clearButton = findViewById(R.id.search_view_clear_button);
    divider = findViewById(R.id.search_view_divider);
    contentContainer = findViewById(R.id.search_view_content_container);

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

    Activity activity = ContextUtils.getActivity(getContext());
    if (activity != null) {
      Window window = activity.getWindow();
      setSoftInputMode(window);
      setStatusBarSpacerEnabled(shouldShowStatusBarSpacer(window));
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    setUpBackgroundViewElevationOverlay(elevation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<SearchView> getBehavior() {
    return new SearchView.Behavior();
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
    int backgroundColor =
        elevationOverlayProvider.compositeOverlayWithThemeSurfaceColorIfNeeded(elevation);
    backgroundView.setBackgroundColor(backgroundColor);
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
    int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
    int dividerColor = ColorUtils.setAlphaComponent(colorOnSurface, Math.round(0.12f * 255));
    divider.setBackgroundColor(dividerColor);
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

    int navigationIcon = R.drawable.ic_arrow_back_black_24;
    if (searchBar == null) {
      toolbar.setNavigationIcon(navigationIcon);
    } else {
      Drawable navigationIconDrawable =
          DrawableCompat.wrap(
              AppCompatResources.getDrawable(getContext(), navigationIcon).mutate());
      if (toolbar.getNavigationIconTint() != null) {
        DrawableCompat.setTint(navigationIconDrawable, toolbar.getNavigationIconTint());
      }
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
          toolbar.setPadding(
              paddingLeft + insets.getSystemWindowInsetLeft(), initialPadding.top,
              paddingRight + insets.getSystemWindowInsetRight(), initialPadding.bottom);
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
          setUpStatusBarSpacer(insets.getSystemWindowInsetTop());
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
          layoutParams.leftMargin = leftMargin + insets.getSystemWindowInsetLeft();
          layoutParams.rightMargin = rightMargin + insets.getSystemWindowInsetRight();
          return insets;
        });
  }

  /** Returns whether or not this {@link SearchView} is set up with an {@link SearchBar}. */
  public boolean isSetUpWithSearchBar() {
    return this.searchBar != null;
  }

  /**
   * Sets up this {@link SearchView} with an {@link SearchBar}, which will result in the {@link
   * SearchView} being shown when the {@link SearchBar} is clicked. This behavior will be set up
   * automatically if the {@link SearchBar} and {@link SearchView} are in a {@link
   * CoordinatorLayout} and the {@link SearchView} is anchored to the {@link SearchBar}.
   */
  public void setUpWithSearchBar(@Nullable SearchBar searchBar) {
    this.searchBar = searchBar;
    searchViewAnimationHelper.setSearchBar(searchBar);
    if (searchBar != null) {
      searchBar.setOnClickListener(v -> show());
    }
    updateNavigationIconIfNeeded();
    setUpBackgroundViewElevationOverlay();
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
  public void setAnimatedMenuItems(boolean animatedMenuItems) {
    this.animatedMenuItems = animatedMenuItems;
  }

  /**
   * Returns whether the menu items should be animated from the {@link SearchBar} to {@link
   * SearchView}.
   */
  public boolean isAnimatedMenuItems() {
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

  /** Sets whether the soft keyboard should be shown with {@code WindowInsetsController}. */
  public void setUseWindowInsetsController(boolean useWindowInsetsController) {
    this.useWindowInsetsController = useWindowInsetsController;
  }

  /** Returns whether the soft keyboard should be shown with {@code WindowInsetsController}. */
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

  /** Returns the main {@link EditText} which can be used for hint and search text. */
  @NonNull
  public EditText getEditText() {
    return editText;
  }

  /** Returns the text of main {@link EditText}, which usually represents the search text. */
  @SuppressLint("KotlinPropertyAccess") // Editable extends CharSequence.
  @Nullable
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
  public void setSoftInputMode(@Nullable Window window) {
    if (window != null) {
      this.softInputMode = window.getAttributes().softInputMode;
    }
  }

  /**
   * Enables/disables the status bar spacer, which can be used in cases where the status bar is
   * translucent and the {@link SearchView} should not overlap the status bar area. This will be set
   * automatically by the {@link SearchView} during initial render based on {@link
   * #shouldShowStatusBarSpacer(Window)}, but make sure to invoke this if you would like to override
   * the default behavior.
   */
  public void setStatusBarSpacerEnabled(boolean enabled) {
    statusBarSpacer.setVisibility(enabled ? VISIBLE : GONE);
  }

  /** Returns the current {@link TransitionState} for this {@link SearchView}. */
  @NonNull
  public TransitionState getCurrentTransitionState() {
    return currentTransitionState;
  }

  void setTransitionState(@NonNull TransitionState state) {
    if (currentTransitionState.equals(state)) {
      return;
    }

    TransitionState previousState = currentTransitionState;
    currentTransitionState = state;
    Set<TransitionListener> listeners = new LinkedHashSet<>(transitionListeners);
    for (TransitionListener listener : listeners) {
      listener.onStateChanged(this, previousState, state);
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
    setModalForAccessibility(true);
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
    searchViewAnimationHelper.hide();
    setModalForAccessibility(false);
  }

  /** Updates the visibility of the {@link SearchView} without an animation. */
  public void setVisible(boolean visible) {
    boolean wasVisible = rootView.getVisibility() == VISIBLE;
    rootView.setVisibility(visible ? VISIBLE : GONE);
    updateNavigationIconProgressIfNeeded();
    if (wasVisible != visible) {
      setModalForAccessibility(visible);
    }
    setTransitionState(visible ? TransitionState.SHOWN : TransitionState.HIDDEN);
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
    }
  }

  /** Requests focus on the main {@link EditText} and shows the soft keyboard. */
  public void requestFocusAndShowKeyboard() {
    editText.post(
        () -> {
          editText.requestFocus();
          ViewUtils.showKeyboard(editText, useWindowInsetsController);
        });
  }

  /** Clears focus on the main {@link EditText} and hides the soft keyboard. */
  public void clearFocusAndHideKeyboard() {
    editText.clearFocus();
    ViewUtils.hideKeyboard(editText, useWindowInsetsController);
  }

  private static boolean shouldShowStatusBarSpacer(@Nullable Window window) {
    if (window == null) {
      return false;
    }
    WindowManager.LayoutParams lp = window.getAttributes();
    boolean translucentStatus =
        VERSION.SDK_INT >= VERSION_CODES.KITKAT
            && (lp.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    boolean layoutNoLimits =
        (lp.flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            == WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
    boolean edgeToEdge =
        VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
            && (window.getDecorView().getSystemUiVisibility() & ViewUtils.EDGE_TO_EDGE_FLAGS)
                == ViewUtils.EDGE_TO_EDGE_FLAGS;
    return translucentStatus || layoutNoLimits || edgeToEdge;
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && isSearchViewModal) {
      childImportantForAccessibilityMap = new HashMap<>(rootView.getChildCount());
    }
    updateChildImportantForAccessibility(rootView, isSearchViewModal);

    if (!isSearchViewModal) {
      // When SearchView is not modal, reset the important for accessibility map.
      childImportantForAccessibilityMap = null;
    }
  }

  /**
   * Sets the 'touchscreenBlocksFocus' attribute of the nested toolbar. The attribute defaults to
   * 'true' for API level 26+. We need to set it to 'false' if keyboard navigation is needed for the
   * search results.
   */
  public void setToolbarTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      toolbar.setTouchscreenBlocksFocus(touchscreenBlocksFocus);
    }
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
          ViewCompat.setImportantForAccessibility(
              child, childImportantForAccessibilityMap.get(child));
        }
      } else {
        // Saves the important for accessibility value of the child view.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          childImportantForAccessibilityMap.put(child, child.getImportantForAccessibility());
        }

        ViewCompat.setImportantForAccessibility(
            child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }
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
      if (!child.isSetUpWithSearchBar() && dependency instanceof SearchBar) {
        child.setUpWithSearchBar((SearchBar) dependency);
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
