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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import androidx.appcompat.widget.ActionMenuView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.animation.AnimatableView;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.internal.ExpandCollapseAnimationHelper;
import com.google.android.material.internal.MultiViewUpdateListener;
import com.google.android.material.internal.ToolbarUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.search.SearchBar.OnLoadAnimationCallback;
import com.google.android.material.shape.MaterialShapeDrawable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Helper class for {@link SearchBar} animations. */
class SearchBarAnimationHelper {

  // On load animation constants
  private static final long ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS = 250;
  private static final long ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_IN_START_DELAY_MS = 500;
  private static final long ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_OUT_START_DELAY_MS = 750;
  private static final long ON_LOAD_ANIM_SECONDARY_DURATION_MS = 250;
  private static final long ON_LOAD_ANIM_SECONDARY_START_DELAY_MS = 250;

  // Expand and collapse animation constants
  private static final long EXPAND_DURATION_MS = 300;
  private static final long EXPAND_FADE_OUT_CHILDREN_DURATION_MS = 75;
  private static final long COLLAPSE_DURATION_MS = 250;
  private static final long COLLAPSE_FADE_IN_CHILDREN_DURATION_MS = 100;

  private final Set<OnLoadAnimationCallback> onLoadAnimationCallbacks = new LinkedHashSet<>();
  private final Set<AnimatorListenerAdapter> expandAnimationListeners = new LinkedHashSet<>();
  private final Set<AnimatorListenerAdapter> collapseAnimationListeners = new LinkedHashSet<>();

  @Nullable private Animator secondaryViewAnimator;
  @Nullable private Animator defaultCenterViewAnimator;
  private boolean expanding;
  private boolean collapsing;
  private boolean onLoadAnimationFadeInEnabled = true;
  private Animator runningExpandOrCollapseAnimator = null;

  void startOnLoadAnimation(SearchBar searchBar) {
    dispatchOnLoadAnimation(OnLoadAnimationCallback::onAnimationStart);
    TextView textView = searchBar.getTextView();
    View centerView = searchBar.getCenterView();

    View secondaryActionMenuItemView = ToolbarUtils.getSecondaryActionMenuItemView(searchBar);
    Animator secondaryViewAnimator =
        getSecondaryViewAnimator(textView, secondaryActionMenuItemView);
    secondaryViewAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            dispatchOnLoadAnimation(OnLoadAnimationCallback::onAnimationEnd);
          }
        });
    this.secondaryViewAnimator = secondaryViewAnimator;

    textView.setAlpha(0f);
    if (secondaryActionMenuItemView != null) {
      secondaryActionMenuItemView.setAlpha(0f);
    }
    if (centerView instanceof AnimatableView) {
      ((AnimatableView) centerView).startAnimation(secondaryViewAnimator::start);
    } else if (centerView != null) {
      centerView.setAlpha(0f);
      centerView.setVisibility(View.VISIBLE);
      Animator defaultCenterViewAnimator = getDefaultCenterViewAnimator(centerView);
      this.defaultCenterViewAnimator = defaultCenterViewAnimator;
      defaultCenterViewAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              centerView.setVisibility(View.GONE);
              secondaryViewAnimator.start();
            }
          });
      defaultCenterViewAnimator.start();
    } else {
      secondaryViewAnimator.start();
    }
  }

  void stopOnLoadAnimation(SearchBar searchBar) {
    if (secondaryViewAnimator != null) {
      secondaryViewAnimator.end();
    }
    if (defaultCenterViewAnimator != null) {
      defaultCenterViewAnimator.end();
    }
    View centerView = searchBar.getCenterView();
    if (centerView instanceof AnimatableView) {
      ((AnimatableView) centerView).stopAnimation();
    }
    if (centerView != null) {
      centerView.setAlpha(0);
    }
  }

  boolean isOnLoadAnimationFadeInEnabled() {
    return onLoadAnimationFadeInEnabled;
  }

  void setOnLoadAnimationFadeInEnabled(boolean onLoadAnimationFadeInEnabled) {
    this.onLoadAnimationFadeInEnabled = onLoadAnimationFadeInEnabled;
  }

  void addOnLoadAnimationCallback(OnLoadAnimationCallback onLoadAnimationCallback) {
    onLoadAnimationCallbacks.add(onLoadAnimationCallback);
  }

  boolean removeOnLoadAnimationCallback(OnLoadAnimationCallback onLoadAnimationCallback) {
    return onLoadAnimationCallbacks.remove(onLoadAnimationCallback);
  }

  private void dispatchOnLoadAnimation(OnLoadAnimationInvocation invocation) {
    for (OnLoadAnimationCallback onLoadAnimationCallback : onLoadAnimationCallbacks) {
      invocation.invoke(onLoadAnimationCallback);
    }
  }

  private interface OnLoadAnimationInvocation {
    void invoke(OnLoadAnimationCallback onLoadAnimationCallback);
  }

  private Animator getDefaultCenterViewAnimator(@Nullable View centerView) {
    ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0, 1);
    fadeInAnimator.addUpdateListener(MultiViewUpdateListener.alphaListener(centerView));
    fadeInAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    fadeInAnimator.setDuration(
        onLoadAnimationFadeInEnabled ? ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS : 0);
    fadeInAnimator.setStartDelay(
        onLoadAnimationFadeInEnabled ? ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_IN_START_DELAY_MS : 0);

    ValueAnimator fadeOutAnimator = ValueAnimator.ofFloat(1, 0);
    fadeOutAnimator.addUpdateListener(MultiViewUpdateListener.alphaListener(centerView));
    fadeOutAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    fadeOutAnimator.setDuration(ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS);
    fadeOutAnimator.setStartDelay(ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_OUT_START_DELAY_MS);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playSequentially(fadeInAnimator, fadeOutAnimator);
    return animatorSet;
  }

  private Animator getSecondaryViewAnimator(
      TextView textView, @Nullable View secondaryActionMenuItemView) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setStartDelay(ON_LOAD_ANIM_SECONDARY_START_DELAY_MS);
    animatorSet.play(getTextViewAnimator(textView));
    if (secondaryActionMenuItemView != null) {
      animatorSet.play(getSecondaryActionMenuItemAnimator(secondaryActionMenuItemView));
    }
    return animatorSet;
  }

  private Animator getTextViewAnimator(TextView textView) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(textView));
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.setDuration(ON_LOAD_ANIM_SECONDARY_DURATION_MS);
    return animator;
  }

  private Animator getSecondaryActionMenuItemAnimator(@Nullable View secondaryActionMenuItemView) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(secondaryActionMenuItemView));
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.setDuration(ON_LOAD_ANIM_SECONDARY_DURATION_MS);
    return animator;
  }

  void startExpandAnimation(
      SearchBar searchBar,
      View expandedView,
      @Nullable AppBarLayout appBarLayout,
      boolean skipAnimation) {
    // If we are in the middle of an collapse animation we should cancel it before we start the
    // expand.
    if (isCollapsing() && runningExpandOrCollapseAnimator != null) {
      runningExpandOrCollapseAnimator.cancel();
    }
    expanding = true;
    expandedView.setVisibility(View.INVISIBLE);
    expandedView.post(
        () -> {
          AnimatorSet fadeAndExpandAnimatorSet = new AnimatorSet();
          Animator fadeOutChildrenAnimator = getFadeOutChildrenAnimator(searchBar, expandedView);
          Animator expandAnimator = getExpandAnimator(searchBar, expandedView, appBarLayout);

          fadeAndExpandAnimatorSet.playSequentially(fadeOutChildrenAnimator, expandAnimator);
          fadeAndExpandAnimatorSet.addListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  runningExpandOrCollapseAnimator = null;
                }
              });
          for (AnimatorListenerAdapter listener : expandAnimationListeners) {
            fadeAndExpandAnimatorSet.addListener(listener);
          }
          if (skipAnimation) {
            fadeAndExpandAnimatorSet.setDuration(0);
          }
          fadeAndExpandAnimatorSet.start();

          runningExpandOrCollapseAnimator = fadeAndExpandAnimatorSet;
        });
  }

  private Animator getExpandAnimator(
      SearchBar searchBar, View expandedView, @Nullable AppBarLayout appBarLayout) {
    return getExpandCollapseAnimationHelper(searchBar, expandedView, appBarLayout)
        .setDuration(EXPAND_DURATION_MS)
        .addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationStart(Animator animation) {
                searchBar.setVisibility(View.INVISIBLE);
              }

              @Override
              public void onAnimationEnd(Animator animation) {
                expanding = false;
              }
            })
        .getExpandAnimator();
  }

  boolean isExpanding() {
    return expanding;
  }

  void addExpandAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    expandAnimationListeners.add(listener);
  }

  boolean removeExpandAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    return expandAnimationListeners.remove(listener);
  }

  void startCollapseAnimation(
      SearchBar searchBar,
      View expandedView,
      @Nullable AppBarLayout appBarLayout,
      boolean skipAnimation) {
    // If we are in the middle of an expand animation we should cancel it before we start the
    // collapse.
    if (isExpanding() && runningExpandOrCollapseAnimator != null) {
      runningExpandOrCollapseAnimator.cancel();
    }

    collapsing = true;
    AnimatorSet collapseAndFadeAnimatorSet = new AnimatorSet();
    Animator collapseAnimator = getCollapseAnimator(searchBar, expandedView, appBarLayout);
    Animator fadeInChildrenAnimator = getFadeInChildrenAnimator(searchBar);

    collapseAndFadeAnimatorSet.playSequentially(collapseAnimator, fadeInChildrenAnimator);
    collapseAndFadeAnimatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            runningExpandOrCollapseAnimator = null;
          }
        });
    for (AnimatorListenerAdapter listener : collapseAnimationListeners) {
      collapseAndFadeAnimatorSet.addListener(listener);
    }
    if (skipAnimation) {
      collapseAndFadeAnimatorSet.setDuration(0);
    }
    collapseAndFadeAnimatorSet.start();

    runningExpandOrCollapseAnimator = collapseAndFadeAnimatorSet;
  }

  private Animator getCollapseAnimator(
      SearchBar searchBar, View expandedView, AppBarLayout appBarLayout) {
    return getExpandCollapseAnimationHelper(searchBar, expandedView, appBarLayout)
        .setDuration(COLLAPSE_DURATION_MS)
        .addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationStart(Animator animation) {
                searchBar.stopOnLoadAnimation();
              }

              @Override
              public void onAnimationEnd(Animator animation) {
                searchBar.setVisibility(View.VISIBLE);
                collapsing = false;
              }
            })
        .getCollapseAnimator();
  }

  boolean isCollapsing() {
    return collapsing;
  }

  void addCollapseAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    collapseAnimationListeners.add(listener);
  }

  boolean removeCollapseAnimationListener(@NonNull AnimatorListenerAdapter listener) {
    return collapseAnimationListeners.remove(listener);
  }

  private ExpandCollapseAnimationHelper getExpandCollapseAnimationHelper(
      SearchBar searchBar, View expandedView, @Nullable AppBarLayout appBarLayout) {
    return new ExpandCollapseAnimationHelper(searchBar, expandedView)
        .setAdditionalUpdateListener(
            getExpandedViewBackgroundUpdateListener(searchBar, expandedView))
        .setCollapsedViewOffsetY(appBarLayout != null ? appBarLayout.getTop() : 0)
        .addEndAnchoredViews(getEndAnchoredViews(expandedView));
  }

  private AnimatorUpdateListener getExpandedViewBackgroundUpdateListener(
      SearchBar searchBar, View expandedView) {
    MaterialShapeDrawable expandedViewBackground =
        MaterialShapeDrawable.createWithElevationOverlay(expandedView.getContext());
    expandedViewBackground.setCornerSize(searchBar.getCornerSize());
    expandedViewBackground.setElevation(searchBar.getElevation());

    return valueAnimator -> {
      expandedViewBackground.setInterpolation(1 - valueAnimator.getAnimatedFraction());
      expandedView.setBackground(expandedViewBackground);

      // Ensures that the expanded view is visible, in the case where ActionMode is used.
      expandedView.setAlpha(1);
    };
  }

  private Animator getFadeOutChildrenAnimator(SearchBar searchBar, View expandedView) {
    List<View> children = getFadeChildren(searchBar);
    ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(children));
    animator.addUpdateListener(
        animation -> {
          // Ensures that the expanded view is not visible while the children are fading out, in
          // the case where ActionMode is used.
          expandedView.setAlpha(0);
        });
    animator.setDuration(EXPAND_FADE_OUT_CHILDREN_DURATION_MS);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    return animator;
  }

  private Animator getFadeInChildrenAnimator(SearchBar searchBar) {
    List<View> children = getFadeChildren(searchBar);
    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    animator.addUpdateListener(MultiViewUpdateListener.alphaListener(children));
    animator.setDuration(COLLAPSE_FADE_IN_CHILDREN_DURATION_MS);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    return animator;
  }

  private List<View> getFadeChildren(SearchBar searchBar) {
    List<View> children = ViewUtils.getChildren(searchBar);
    if (searchBar.getCenterView() != null) {
      children.remove(searchBar.getCenterView());
    }
    return children;
  }

  private List<View> getEndAnchoredViews(View expandedView) {
    boolean isRtl = ViewUtils.isLayoutRtl(expandedView);
    List<View> endAnchoredViews = new ArrayList<>();
    if (expandedView instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) expandedView;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View child = viewGroup.getChildAt(i);
        if ((!isRtl && child instanceof ActionMenuView)
            || (isRtl && !(child instanceof ActionMenuView))) {
          endAnchoredViews.add(child);
        }
      }
    }
    return endAnchoredViews;
  }
}
