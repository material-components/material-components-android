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
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.BackEventCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
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
  private static final long SHOW_SCRIM_ALPHA_DURATION_MS = 100;

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
  private final LinearLayout textContainer;
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
    this.textContainer = searchView.textContainer;

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
        getSearchPrefixAnimator(show),
        getTextAnimator(show));
    animatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            setContentViewsAlpha(show ? 0 : 1);
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            setContentViewsAlpha(show ? 1 : 0);
            // Reset edittext and searchbar textview alphas after the animations are finished since
            // the visibilities for searchview and searchbar have been set accordingly.
            editText.setAlpha(1);
            if (searchBar != null) {
              searchBar.getTextView().setAlpha(1);
            }
            // Reset clip bounds so it can react to the screen or layout changes.
            editText.setClipBounds(null);

            // After expanding or collapsing, we should reset the clip bounds so it can react to the
            // screen or layout changes. Otherwise it will result in wrong clipping on the layout.
            rootView.resetClipBoundsAndCornerRadii();

            // After collapsing, we should reset the expanded corner radii in case the search view
            // is shown in a different location the next time.
            if (!show) {
              backHelper.clearExpandedCornerRadii();
            }
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
    animator.setStartDelay(show ? SHOW_SCRIM_ALPHA_DURATION_MS : 0);
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
    float[] toCornerRadius =
        maxCornerRadii(rootView.getCornerRadii(), backHelper.getExpandedCornerRadii());

    ValueAnimator animator =
        ValueAnimator.ofObject(new RectEvaluator(clipBounds), fromClipBounds, toClipBounds);
    animator.addUpdateListener(
        valueAnimator -> {
          float[] cornerRadii =
              lerpCornerRadii(
                  fromCornerRadius, toCornerRadius, valueAnimator.getAnimatedFraction());
          rootView.updateClipBoundsAndCornerRadii(clipBounds, cornerRadii);
        });
    animator.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animator.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animator;
  }

  private static float[] maxCornerRadii(float[] startValue, float[] endValue) {
    return new float[] {
        max(startValue[0], endValue[0]),
        max(startValue[1], endValue[1]),
        max(startValue[2], endValue[2]),
        max(startValue[3], endValue[3]),
        max(startValue[4], endValue[4]),
        max(startValue[5], endValue[5]),
        max(startValue[6], endValue[6]),
        max(startValue[7], endValue[7])
    };
  }

  private static float[] lerpCornerRadii(float startValue, float[] endValue, float fraction) {
    return new float[] {
        lerp(startValue, endValue[0], fraction),
        lerp(startValue, endValue[1], fraction),
        lerp(startValue, endValue[2], fraction),
        lerp(startValue, endValue[3], fraction),
        lerp(startValue, endValue[4], fraction),
        lerp(startValue, endValue[5], fraction),
        lerp(startValue, endValue[6], fraction),
        lerp(startValue, endValue[7], fraction)
    };
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
    ImageButton searchViewBackButton = ToolbarUtils.getNavigationIconButton(toolbar);
    if (searchViewBackButton == null) {
      return;
    }
    ImageButton searchBarBackButton = ToolbarUtils.getNavigationIconButton(searchBar);

    ValueAnimator backButtonAnimatorX =
        ValueAnimator.ofFloat(
            getTranslationXBetweenViews(searchBarBackButton, searchViewBackButton), 0);
    backButtonAnimatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(searchViewBackButton));

    ValueAnimator backButtonAnimatorY = ValueAnimator.ofFloat(getFromTranslationY(), 0);
    backButtonAnimatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(searchViewBackButton));

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
      addBackButtonAnimatorIfNeeded(animatorSet, backButton);
    } else {
      setFullDrawableProgressIfNeeded(drawable);
    }
  }

  private void addBackButtonAnimatorIfNeeded(AnimatorSet animatorSet, ImageButton backButton) {
    // If there's no navigation icon on the search bar, we should set the alpha for the button
    // itself instead of the drawables since the button background has a ripple.
    if (searchBar == null || searchBar.getNavigationIcon() != null) {
      return;
    }

    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(
        animation -> backButton.setAlpha((Float) animation.getAnimatedValue()));
    animatorSet.playTogether(animator);
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
    ActionMenuView searchViewActionMenuView = ToolbarUtils.getActionMenuView(toolbar);
    if (searchViewActionMenuView == null) {
      return;
    }
    ActionMenuView searchBarActionMenuView = ToolbarUtils.getActionMenuView(searchBar);

    ValueAnimator actionMenuViewAnimatorX =
        ValueAnimator.ofFloat(
            getTranslationXBetweenViews(searchBarActionMenuView, searchViewActionMenuView), 0);
    actionMenuViewAnimatorX.addUpdateListener(
        MultiViewUpdateListener.translationXListener(searchViewActionMenuView));

    ValueAnimator actionMenuViewAnimatorY = ValueAnimator.ofFloat(getFromTranslationY(), 0);
    actionMenuViewAnimatorY.addUpdateListener(
        MultiViewUpdateListener.translationYListener(searchViewActionMenuView));

    animatorSet.playTogether(actionMenuViewAnimatorX, actionMenuViewAnimatorY);
  }

  private Animator getDummyToolbarAnimator(boolean show) {
    return getTranslationAnimator(
        show,
        dummyToolbar,
        getFromTranslationXEnd(dummyToolbar),
        getFromTranslationY());
  }

  private Animator getHeaderContainerAnimator(boolean show) {
    return getTranslationAnimator(
        show,
        headerContainer,
        getFromTranslationXEnd(headerContainer),
        getFromTranslationY());
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
    return getTranslationAnimatorForText(show, searchPrefix);
  }

  private Animator getEditTextAnimator(boolean show) {
    return getTranslationAnimatorForText(show, editText);
  }

  private AnimatorSet getTextAnimator(boolean show) {
    AnimatorSet animatorSet = new AnimatorSet();
    addTextFadeAnimatorIfNeeded(animatorSet);
    addEditTextClipAnimator(animatorSet);
    animatorSet.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.LINEAR_INTERPOLATOR));
    return animatorSet;
  }

  private void addEditTextClipAnimator(AnimatorSet animatorSet) {
    // We only want to add a clip animation if the edittext and searchbar text is the same, which
    // means it is translating instead of fading.
    if (searchBar == null || !TextUtils.equals(editText.getText(), searchBar.getText())) {
      return;
    }
    Rect editTextClipBounds =
        new Rect(0, 0, editText.getWidth(), editText.getHeight());
    ValueAnimator animator =
        ValueAnimator.ofInt(
            searchBar.getTextView().getWidth(), editText.getWidth());
    animator.addUpdateListener(
        animation -> {
          editTextClipBounds.right = (int) animation.getAnimatedValue();
          editText.setClipBounds(editTextClipBounds);
        });
    animatorSet.playTogether(animator);
  }

  private void addTextFadeAnimatorIfNeeded(AnimatorSet animatorSet) {
    if (searchBar == null || TextUtils.equals(editText.getText(), searchBar.getText())) {
      return;
    }
    // If the searchbar text is not equal to the searchview edittext, we want to fade out the
    // edittext and fade in the searchbar text
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(
        animation -> {
          editText.setAlpha((Float) animation.getAnimatedValue());
          searchBar.getTextView().setAlpha(1 - (Float) animation.getAnimatedValue());
        });
    animatorSet.playTogether(animator);
  }

  private Animator getTranslationAnimatorForText(boolean show, View v) {
    TextView textView = searchBar.getPlaceholderTextView();
    // If the placeholder text is empty, we animate to the searchbar textview instead.
    // Or if we're showing the searchview, we always animate from the searchbar textview, not
    // from the placeholder text.
    if (TextUtils.isEmpty(textView.getText()) || show) {
      textView = searchBar.getTextView();
    }
    int startX =
        getViewLeftFromSearchViewParent(textView) - (v.getLeft() + textContainer.getLeft());
    return getTranslationAnimator(show, v, startX, getFromTranslationY());
  }

  private int getViewLeftFromSearchViewParent(@NonNull View v) {
    int left = v.getLeft();
    ViewParent viewParent = v.getParent();
    while (viewParent instanceof View && viewParent != searchView.getParent()) {
      left += ((View) viewParent).getLeft();
      viewParent = viewParent.getParent();
    }
    return left;
  }

  private int getViewTopFromSearchViewParent(@NonNull View v) {
    int top = v.getTop();
    ViewParent viewParent = v.getParent();
    while (viewParent instanceof View && viewParent != searchView.getParent()) {
      top += ((View) viewParent).getTop();
      viewParent = viewParent.getParent();
    }
    return top;
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

  private Animator getTranslationAnimator(boolean show, View view, int startX, int startY) {
    ValueAnimator animatorX = ValueAnimator.ofFloat(startX, 0);
    animatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(view));

    ValueAnimator animatorY = ValueAnimator.ofFloat(startY, 0);
    animatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(view));

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(animatorX, animatorY);
    animatorSet.setDuration(show ? SHOW_DURATION_MS : HIDE_DURATION_MS);
    animatorSet.setInterpolator(
        ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    return animatorSet;
  }

  private int getTranslationXBetweenViews(
      @Nullable View searchBarSubView, @NonNull View searchViewSubView) {
    // If there is no equivalent for the SearchView subview in the SearchBar, we return the
    // translation between the SearchBar and the start of the SearchView subview
    if (searchBarSubView == null) {
      int marginStart = ((MarginLayoutParams) searchViewSubView.getLayoutParams()).getMarginStart();
      int paddingStart = searchBar.getPaddingStart();
      int searchBarLeft = getViewLeftFromSearchViewParent(searchBar);
      return ViewUtils.isLayoutRtl(searchBar)
          ? searchBarLeft
              + searchBar.getWidth()
              + marginStart
              - paddingStart
              - searchView.getRight()
          : (searchBarLeft - marginStart + paddingStart);
    }
    return getViewLeftFromSearchViewParent(searchBarSubView)
        - getViewLeftFromSearchViewParent(searchViewSubView);
  }

  private int getFromTranslationXEnd(View view) {
    int marginEnd = ((MarginLayoutParams) view.getLayoutParams()).getMarginEnd();
    int viewLeft = getViewLeftFromSearchViewParent(searchBar);
    return ViewUtils.isLayoutRtl(searchBar)
        ? viewLeft - marginEnd
        : viewLeft + searchBar.getWidth() + marginEnd - searchView.getWidth();
  }

  private int getFromTranslationY() {
    int toolbarMiddleY = toolbarContainer.getTop() + toolbarContainer.getHeight() / 2;
    int searchBarMiddleY =
        getViewTopFromSearchViewParent(searchBar)
            + searchBar.getHeight() / 2;
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
