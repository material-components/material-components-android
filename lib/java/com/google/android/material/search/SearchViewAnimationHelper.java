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

import static com.google.android.material.animation.AnimationUtils.lerp;
import static java.lang.Math.max;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.BackEventCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ClippableRoundedCornerLayout;
import com.google.android.material.internal.FadeThroughDrawable;
import com.google.android.material.internal.FadeThroughUpdateListener;
import com.google.android.material.internal.MultiViewUpdateListener;
import com.google.android.material.internal.RectEvaluator;
import com.google.android.material.internal.ReversableAnimatedValueInterpolator;
import com.google.android.material.internal.ToolbarUtils;
import com.google.android.material.internal.TouchObserverFrameLayout;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MaterialMainContainerBackHelper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Helper class for {@link SearchView} animations. */
@SuppressWarnings("RestrictTo")
class SearchViewAnimationHelper {

  // Constants for show expand animation
  private static final long SHOW_DURATION_MS = 300;
  private static final long SHOW_CLEAR_BUTTON_ALPHA_DURATION_MS = 50;
  private static final long SHOW_CLEAR_BUTTON_ALPHA_START_DELAY_MS = 250;
  private static final long SHOW_CONTENT_ALPHA_DURATION_MS = 150;
  private static final long SHOW_CONTENT_ALPHA_START_DELAY_MS = 75;
  private static final long SHOW_CONTENT_SCALE_DURATION_MS = SHOW_DURATION_MS;

  // Constants for hide collapse animation
  private static final long HIDE_DURATION_MS = 250;
  private static final long HIDE_CLEAR_BUTTON_ALPHA_DURATION_MS = 42;
  private static final long HIDE_CLEAR_BUTTON_ALPHA_START_DELAY_MS = 0;
  private static final long HIDE_CONTENT_ALPHA_DURATION_MS = 83;
  private static final long HIDE_CONTENT_ALPHA_START_DELAY_MS = 0;
  private static final long HIDE_CONTENT_SCALE_DURATION_MS = HIDE_DURATION_MS;

  private static final float CONTENT_FROM_SCALE = 0.95f;

  // Constants for show translate animation
  private static final long SHOW_TRANSLATE_DURATION_MS = 350;
  private static final long SHOW_TRANSLATE_KEYBOARD_START_DELAY_MS = 150;

  // Constants for hide translate animation
  private static final long HIDE_TRANSLATE_DURATION_MS = 300;

  private final SearchView searchView;
  private final View scrim;
  private final ClippableRoundedCornerLayout rootView;
  private final FrameLayout headerContainer;
  private final FrameLayout toolbarContainer;
  private final Toolbar toolbar;
  private final Toolbar dummyToolbar;
  private final TextView searchPrefix;
  private final EditText editText;
  private final ImageButton clearButton;
  private final View divider;
  private final TouchObserverFrameLayout contentContainer;

  private final MaterialMainContainerBackHelper backHelper;
  @Nullable private AnimatorSet backProgressAnimatorSet;

  private SearchBar searchBar;

  SearchViewAnimationHelper(SearchView searchView) {
    this.searchView = searchView;
    this.scrim = searchView.scrim;
    this.rootView = searchView.rootView;
    this.headerContainer = searchView.headerContainer;
    this.toolbarContainer = searchView.toolbarContainer;
    this.toolbar = searchView.toolbar;
    this.dummyToolbar = searchView.dummyToolbar;
    this.searchPrefix = searchView.searchPrefix;
    this.editText = searchView.editText;
    this.clearButton = searchView.clearButton;
    this.divider = searchView.divider;
    this.contentContainer = searchView.contentContainer;

    backHelper = new MaterialMainContainerBackHelper(rootView);
  }

  void setSearchBar(SearchBar searchBar) {
    this.searchBar = searchBar;
  }

  void show() {
    if (searchBar != null) {
      startShowAnimationExpand();
    } else {
      startShowAnimationTranslate();
    }
  }

  @CanIgnoreReturnValue
  AnimatorSet hide() {
    if (searchBar != null) {
      return startHideAnimationCollapse();
    } else {
      return startHideAnimationTranslate();
    }
  }

  private void startShowAnimationExpand() {
    if (searchView.isAdjustNothingSoftInputMode()) {
      searchView.requestFocusAndShowKeyboardIfNeeded();
    }
    searchView.setTransitionState(SearchView.TransitionState.SHOWING);
    setUpDummyToolbarIfNeeded();
    editText.setText(searchBar.getText());
    editText.setSelection(editText.getText().length());
    rootView.setVisibility(View.INVISIBLE);
    rootView.post(
        () -> {
          AnimatorSet animatorSet = getExpandCollapseAnimatorSet(true);
          animatorSet.addListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                  rootView.setVisibility(View.VISIBLE);
                  searchBar.stopOnLoadAnimation();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                  if (!searchView.isAdjustNothingSoftInputMode()) {
                    searchView.requestFocusAndShowKeyboardIfNeeded();
                  }
                  searchView.setTransitionState(SearchView.TransitionState.SHOWN);
                }
              });
          animatorSet.start();
        });
  }

  private AnimatorSet startHideAnimationCollapse() {
    if (searchView.isAdjustNothingSoftInputMode()) {
      searchView.clearFocusAndHideKeyboard();
    }
    AnimatorSet animatorSet = getExpandCollapseAnimatorSet(false);
    animatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            searchView.setTransitionState(SearchView.TransitionState.HIDING);
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            rootView.setVisibility(View.GONE);
            if (!searchView.isAdjustNothingSoftInputMode()) {
              searchView.clearFocusAndHideKeyboard();
            }
            searchView.setTransitionState(SearchView.TransitionState.HIDDEN);
          }
        });
    animatorSet.start();
    return animatorSet;
  }

  private void startShowAnimationTranslate() {
    if (searchView.isAdjustNothingSoftInputMode()) {
      searchView.postDelayed(
          searchView::requestFocusAndShowKeyboardIfNeeded,
          SHOW_TRANSLATE_KEYBOARD_START_DELAY_MS);
    }
    rootView.setVisibility(View.INVISIBLE);
    rootView.post(
        () -> {
          rootView.setTranslationY(rootView.getHeight());
          AnimatorSet animatorSet = getTranslateAnimatorSet(true);
          animatorSet.addListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                  rootView.setVisibility(View.VISIBLE);
                  searchView.setTransitionState(SearchView.TransitionState.SHOWING);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                  if (!searchView.isAdjustNothingSoftInputMode()) {
                    searchView.requestFocusAndShowKeyboardIfNeeded();
                  }
                  searchView.setTransitionState(SearchView.TransitionState.SHOWN);
                }
              });
          animatorSet.start();
        });
  }

  private AnimatorSet startHideAnimationTranslate() {
    if (searchView.isAdjustNothingSoftInputMode()) {
      searchView.clearFocusAndHideKeyboard();
    }
    AnimatorSet animatorSet = getTranslateAnimatorSet(false);
    animatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            searchView.setTransitionState(SearchView.TransitionState.HIDING);
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            rootView.setVisibility(View.GONE);
            if (!searchView.isAdjustNothingSoftInputMode()) {
              searchView.clearFocusAndHideKeyboard();
            }
            searchView.setTransitionState(SearchView.TransitionState.HIDDEN);
          }
        });
    animatorSet.start();
    return animatorSet;
  }

  private AnimatorSet getTranslateAnimatorSet(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(getTranslationYAnimator());
    addBackButtonProgressAnimatorIfNeeded(animatorSet);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    animatorSet.setDuration(show ? SHOW_TRANSLATE_DURATION_MS : HIDE_TRANSLATE_DURATION_MS);
    return animatorSet;
  }

  private Animator getTranslationYAnimator() {
    ValueAnimator animator = ValueAnimator.ofFloat(rootView.getHeight(), 0);
    animator.addUpdateListener(MultiViewUpdateListener.translationYListener(rootView));
    return animator;
  }

  private AnimatorSet getExpandCollapseAnimatorSet(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    boolean backProgress = backProgressAnimatorSet != null;
    if (!backProgress) {
      animatorSet.playTogether(
          getButtonsProgressAnimator(show), getButtonsTranslationAnimator(show));
    }
    animatorSet.playTogether(
        getScrimAlphaAnimator(show),
        getRootViewAnimator(show),
        getClearButtonAnimator(show),
        getContentAnimator(show),
        getHeaderContainerAnimator(show),
        getDummyToolbarAnimator(show),
        getActionMenuViewsAlphaAnimator(show),
        getEditTextAnimator(show),
        getSearchPrefixAnimator(show));
    animatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            setContentViewsAlpha(show ? 0 : 1);
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            setContentViewsAlpha(show ? 1 : 0);
            // After expanding or collapsing, we should reset the clip bounds so it can react to the
            // screen or layout changes. Otherwise it will result in wrong clipping on the layout.
            rootView.resetClipBoundsAndCornerRadius();
          }
        });
    return animatorSet;
  }

  private void setContentViewsAlpha(float alpha) {
    clearButton.setAlpha(alpha);
    divider.setAlpha(alpha);
    contentContainer.setAlpha(alpha);
    setActionMenuViewAlphaIfNeeded(alpha);
  }

  private void setActionMenuViewAlphaIfNeeded(float alpha) {
    if (searchView.isMenuItemsAnimated()) {
      ActionMenuView actionMenuView = ToolbarUtils.getActionMenuView(toolbar);
      if (actionMenuView != null) {
        actionMenuView.setAlpha(alpha);
      }
    }
  }

  private Animator getScrimAlphaAnimator(boolean show) {
    TimeInterpolator interpolator =
        show ? AnimationUtils.LINEAR_INTERPOLATOR : AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;

    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animator.setInterpolator(ReversableAnimatedValueInterpolator.of(show, interpolator));
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(scrim));
    return animator;
  }

  private Animator getRootViewAnimator(boolean show) {
    Rect initialHideToClipBounds = backHelper.getInitialHideToClipBounds();
    Rect initialHideFromClipBounds = backHelper.getInitialHideFromClipBounds();
    Rect toClipBounds =
        initialHideToClipBounds != null
            ? initialHideToClipBounds
            : ViewUtils.calculateRectFromBounds(searchView);
    Rect fromClipBounds =
        initialHideFromClipBounds != null
            ? initialHideFromClipBounds
            : ViewUtils.calculateOffsetRectFromBounds(rootView, searchBar);
    Rect clipBounds = new Rect(fromClipBounds);

    float fromCornerRadius = searchBar.getCornerSize();
    float toCornerRadius = max(rootView.getCornerRadius(), backHelper.getExpandedCornerSize());

    ValueAnimator animator =
        ValueAnimator.ofObject(new RectEvaluator(clipBounds), fromClipBounds, toClipBounds);
    animator.addUpdateListener(
        valueAnimator -> {
          float cornerRadius =
              lerp(fromCornerRadius, toCornerRadius, valueAnimator.getAnimatedFraction());
          rootView.updateClipBoundsAndCornerRadius(clipBounds, cornerRadius);
        });
    animator.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animator;
  }

  private Animator getClearButtonAnimator(boolean show) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(
        show ? SHOW_CLEAR_BUTTON_ALPHA_DURATION_MS : HIDE_CLEAR_BUTTON_ALPHA_DURATION_MS);
    animator.setStartDelay(
        show ? SHOW_CLEAR_BUTTON_ALPHA_START_DELAY_MS : HIDE_CLEAR_BUTTON_ALPHA_START_DELAY_MS);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.LINEAR_INTERPOLATOR));
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(clearButton));
    return animator;
  }

  private AnimatorSet getButtonsProgressAnimator(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    addBackButtonProgressAnimatorIfNeeded(animatorSet);
    animatorSet.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animatorSet;
  }

  private AnimatorSet getButtonsTranslationAnimator(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    addBackButtonTranslationAnimatorIfNeeded(animatorSet);
    addActionMenuViewAnimatorIfNeeded(animatorSet);
    animatorSet.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animatorSet;
  }

  private void addBackButtonTranslationAnimatorIfNeeded(AnimatorSet animatorSet) {
    ImageButton backButton = ToolbarUtils.getNavigationIconButton(toolbar);
    if (backButton == null) {
      return;
    }

    ValueAnimator backButtonAnimatorX =
        ValueAnimator.ofFloat(getFromTranslationXStart(backButton), 0);
    backButtonAnimatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(backButton));

    ValueAnimator backButtonAnimatorY = ValueAnimator.ofFloat(getFromTranslationY(), 0);
    backButtonAnimatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(backButton));

    animatorSet.playTogether(backButtonAnimatorX, backButtonAnimatorY);
  }

  private void addBackButtonProgressAnimatorIfNeeded(AnimatorSet animatorSet) {
    ImageButton backButton = ToolbarUtils.getNavigationIconButton(toolbar);
    if (backButton == null) {
      return;
    }

    Drawable drawable = DrawableCompat.unwrap(backButton.getDrawable());
    if (searchView.isAnimatedNavigationIcon()) {
      addDrawerArrowDrawableAnimatorIfNeeded(animatorSet, drawable);
      addFadeThroughDrawableAnimatorIfNeeded(animatorSet, drawable);
    } else {
      setFullDrawableProgressIfNeeded(drawable);
    }
  }

  private void addDrawerArrowDrawableAnimatorIfNeeded(AnimatorSet animatorSet, Drawable drawable) {
    if (drawable instanceof DrawerArrowDrawable) {
      DrawerArrowDrawable drawerArrowDrawable = (DrawerArrowDrawable) drawable;
      ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
      animator.addUpdateListener(
          animation -> drawerArrowDrawable.setProgress((Float) animation.getAnimatedValue()));
      animatorSet.playTogether(animator);
    }
  }

  private void addFadeThroughDrawableAnimatorIfNeeded(AnimatorSet animatorSet, Drawable drawable) {
    if (drawable instanceof FadeThroughDrawable) {
      FadeThroughDrawable fadeThroughDrawable = (FadeThroughDrawable) drawable;
      ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
      animator.addUpdateListener(
          animation -> fadeThroughDrawable.setProgress((Float) animation.getAnimatedValue()));
      animatorSet.playTogether(animator);
    }
  }

  private void setFullDrawableProgressIfNeeded(Drawable drawable) {
    if (drawable instanceof DrawerArrowDrawable) {
      ((DrawerArrowDrawable) drawable).setProgress(1);
    }
    if (drawable instanceof FadeThroughDrawable) {
      ((FadeThroughDrawable) drawable).setProgress(1);
    }
  }

  private void addActionMenuViewAnimatorIfNeeded(AnimatorSet animatorSet) {
    ActionMenuView actionMenuView = ToolbarUtils.getActionMenuView(toolbar);
    if (actionMenuView == null) {
      return;
    }

    ValueAnimator actionMenuViewAnimatorX =
        ValueAnimator.ofFloat(getFromTranslationXEnd(actionMenuView), 0);
    actionMenuViewAnimatorX.addUpdateListener(
        MultiViewUpdateListener.translationXListener(actionMenuView));

    ValueAnimator actionMenuViewAnimatorY = ValueAnimator.ofFloat(getFromTranslationY(), 0);
    actionMenuViewAnimatorY.addUpdateListener(
        MultiViewUpdateListener.translationYListener(actionMenuView));

    animatorSet.playTogether(actionMenuViewAnimatorX, actionMenuViewAnimatorY);
  }

  private Animator getDummyToolbarAnimator(boolean show) {
    return getTranslationAnimator(show, false, dummyToolbar);
  }

  private Animator getHeaderContainerAnimator(boolean show) {
    return getTranslationAnimator(show, false, headerContainer);
  }

  private Animator getActionMenuViewsAlphaAnimator(boolean show) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));

    if (searchView.isMenuItemsAnimated()) {
      ActionMenuView dummyActionMenuView = ToolbarUtils.getActionMenuView(dummyToolbar);
      ActionMenuView actionMenuView = ToolbarUtils.getActionMenuView(toolbar);
      animator.addUpdateListener(
          new FadeThroughUpdateListener(dummyActionMenuView, actionMenuView));
    }

    return animator;
  }

  private Animator getSearchPrefixAnimator(boolean show) {
    return getTranslationAnimator(show, true, searchPrefix);
  }

  private Animator getEditTextAnimator(boolean show) {
    return getTranslationAnimator(show, true, editText);
  }

  private Animator getContentAnimator(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        getContentAlphaAnimator(show), getDividerAnimator(show), getContentScaleAnimator(show));
    return animatorSet;
  }

  private Animator getContentAlphaAnimator(boolean show) {
    ValueAnimator animatorAlpha = ValueAnimator.ofFloat(0, 1);
    animatorAlpha.setDuration(
        show ? SHOW_CONTENT_ALPHA_DURATION_MS : HIDE_CONTENT_ALPHA_DURATION_MS);
    animatorAlpha.setStartDelay(
        show ? SHOW_CONTENT_ALPHA_START_DELAY_MS : HIDE_CONTENT_ALPHA_START_DELAY_MS);
    animatorAlpha.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.LINEAR_INTERPOLATOR));
    animatorAlpha.addUpdateListener(
        MultiViewUpdateListener.alphaListener(divider, contentContainer));
    return animatorAlpha;
  }

  private Animator getDividerAnimator(boolean show) {
    float dividerTranslationY =
        (float) contentContainer.getHeight() * (1f - CONTENT_FROM_SCALE) / 2f;

    ValueAnimator animatorDivider = ValueAnimator.ofFloat(dividerTranslationY, 0);
    animatorDivider.setDuration(
        show ? SHOW_CONTENT_SCALE_DURATION_MS : HIDE_CONTENT_SCALE_DURATION_MS);
    animatorDivider.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    animatorDivider.addUpdateListener(MultiViewUpdateListener.translationYListener(divider));
    return animatorDivider;
  }

  private Animator getContentScaleAnimator(boolean show) {
    ValueAnimator animatorScale = ValueAnimator.ofFloat(CONTENT_FROM_SCALE, 1);
    animatorScale.setDuration(
        show ? SHOW_CONTENT_SCALE_DURATION_MS : HIDE_CONTENT_SCALE_DURATION_MS);
    animatorScale.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    animatorScale.addUpdateListener(MultiViewUpdateListener.scaleListener(contentContainer));
    return animatorScale;
  }

  private Animator getTranslationAnimator(boolean show, boolean anchoredToStart, View view) {
    int startX = anchoredToStart ? getFromTranslationXStart(view) : getFromTranslationXEnd(view);
    ValueAnimator animatorX = ValueAnimator.ofFloat(startX, 0);
    animatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(view));

    ValueAnimator animatorY = ValueAnimator.ofFloat(getFromTranslationY(), 0);
    animatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(view));

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(animatorX, animatorY);
    animatorSet.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animatorSet;
  }

  private int getFromTranslationXStart(View view) {
    int marginStart =
        MarginLayoutParamsCompat.getMarginStart((MarginLayoutParams) view.getLayoutParams());
    int paddingStart = ViewCompat.getPaddingStart(searchBar);
    return ViewUtils.isLayoutRtl(searchBar)
        ? searchBar.getWidth() - searchBar.getRight() + marginStart - paddingStart
        : searchBar.getLeft() - marginStart + paddingStart;
  }

  private int getFromTranslationXEnd(View view) {
    int marginEnd =
        MarginLayoutParamsCompat.getMarginEnd((MarginLayoutParams) view.getLayoutParams());
    return ViewUtils.isLayoutRtl(searchBar)
        ? searchBar.getLeft() - marginEnd
        : searchBar.getRight() - searchView.getWidth() + marginEnd;
  }

  private int getFromTranslationY() {
    int toolbarMiddleY = (toolbarContainer.getTop() + toolbarContainer.getBottom()) / 2;
    int searchBarMiddleY = (searchBar.getTop() + searchBar.getBottom()) / 2;
    return searchBarMiddleY - toolbarMiddleY;
  }

  private void setUpDummyToolbarIfNeeded() {
    Menu menu = dummyToolbar.getMenu();
    if (menu != null) {
      menu.clear();
    }
    if (searchBar.getMenuResId() != -1 && searchView.isMenuItemsAnimated()) {
      dummyToolbar.inflateMenu(searchBar.getMenuResId());
      setMenuItemsNotClickable(dummyToolbar);
      dummyToolbar.setVisibility(View.VISIBLE);
    } else {
      dummyToolbar.setVisibility(View.GONE);
    }
  }

  private void setMenuItemsNotClickable(Toolbar toolbar) {
    ActionMenuView actionMenuView = ToolbarUtils.getActionMenuView(toolbar);
    if (actionMenuView != null) {
      for (int i = 0; i < actionMenuView.getChildCount(); i++) {
        View menuItem = actionMenuView.getChildAt(i);
        menuItem.setClickable(false);
        menuItem.setFocusable(false);
        menuItem.setFocusableInTouchMode(false);
      }
    }
  }

  void startBackProgress(@NonNull BackEventCompat backEvent) {
    backHelper.startBackProgress(backEvent, searchBar);
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void updateBackProgress(@NonNull BackEventCompat backEvent) {
    if (backEvent.getProgress() <= 0f) {
      return;
    }

    backHelper.updateBackProgress(backEvent, searchBar, searchBar.getCornerSize());

    if (backProgressAnimatorSet == null) {
      if (searchView.isAdjustNothingSoftInputMode()) {
        searchView.clearFocusAndHideKeyboard();
      }

      // Early return if navigation icon animation is disabled.
      if (!searchView.isAnimatedNavigationIcon()) {
        return;
      }

      // Start and immediately pause the animator set so we can seek it with setCurrentPlayTime() in
      // subsequent updateBackProgress() calls when the progress value changes.
      backProgressAnimatorSet = getButtonsProgressAnimator(/* show= */ false);
      backProgressAnimatorSet.start();
      backProgressAnimatorSet.pause();
    } else {
      backProgressAnimatorSet.setCurrentPlayTime(
          (long) (backEvent.getProgress() * backProgressAnimatorSet.getDuration()));
    }
  }

  @Nullable
  public BackEventCompat onHandleBackInvoked() {
    return backHelper.onHandleBackInvoked();
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void finishBackProgress() {
    AnimatorSet hideAnimatorSet = hide();
    long totalDuration = hideAnimatorSet.getTotalDuration();

    backHelper.finishBackProgress(totalDuration, searchBar);

    if (backProgressAnimatorSet != null) {
      getButtonsTranslationAnimator(/* show= */ false).start();
      backProgressAnimatorSet.resume();
    }

    backProgressAnimatorSet = null;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void cancelBackProgress() {
    backHelper.cancelBackProgress(searchBar);

    if (backProgressAnimatorSet != null) {
      backProgressAnimatorSet.reverse();
    }
    backProgressAnimatorSet = null;
  }

  MaterialMainContainerBackHelper getBackHelper() {
    return backHelper;
  }
}
