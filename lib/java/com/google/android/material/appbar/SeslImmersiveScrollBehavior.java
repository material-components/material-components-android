/*
 * Copyright 2024 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowInsetsAnimationControlListener;
import android.view.WindowInsetsAnimationController;
import android.view.WindowInsetsController;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.PathInterpolator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.view.SeslDecorViewReflector;

import com.google.android.material.R;
import com.google.android.material.internal.SeslContextUtils;
import com.google.android.material.internal.SeslDisplayUtils;

import java.util.List;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
@RequiresApi(api = Build.VERSION_CODES.R)
public final class SeslImmersiveScrollBehavior extends AppBarLayout.Behavior {
  private static final String TAG = "SeslImmersiveScrollBehavior";
  private static final int MSG_APPEAR_ANIMATION = 100;

  private WindowInsetsAnimationController mAnimationController;
  private AppBarLayout mAppBarLayout;
  private View mBottomArea;
  private CancellationSignal mCancellationSignal;
  private CollapsingToolbarLayout mCollapsingToolbarLayout;
  private View mContentView;
  private Context mContext;
  private CoordinatorLayout mCoordinatorLayout;
  private WindowInsetsAnimation.Callback mCustomWindowInsetsAnimation = null;
  private View mDecorView;
  private WindowInsets mDecorViewInset;
  private View mNavigationBarBg;
  private ValueAnimator mOffsetAnimator;
  private WindowInsetsAnimationController mPendingRequestOnReady;
  private View mStatusBarBg;
  private View mTargetView;
  private WindowInsetsController mWindowInsetsController = null;

  private Handler mAnimationHandler
          = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == MSG_APPEAR_ANIMATION) {
        startRestoreAnimation();
      }
    }
  };

  private final AppBarLayout.OnOffsetChangedListener mOffsetChangedListener
          = new AppBarLayout.OnOffsetChangedListener() {

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
      if (mAppBarLayout == null || !mAppBarLayout.isDetachedState()) {

        if (!useCustomAnimationCallback) {
          float immOffset;

          if (mCanImmersiveScroll) {
            final int bottomAreaHeight = mBottomArea != null ?  mBottomArea.getHeight(): 0;
            float collapsedHeight = appBarLayout.seslGetCollapsedHeight();

            int bottomInset = Float.compare(collapsedHeight, 0.0F);

            final float appBarHeight = (float)(mNavigationBarHeight + bottomAreaHeight);
            final float scrollRatio = appBarHeight / (collapsedHeight == 0f? 1f: collapsedHeight);
            final int totalScrollRange = appBarLayout.getTotalScrollRange();
            final float scrolledRange = (float)(totalScrollRange - appBarLayout.seslGetTCScrollRange() + verticalOffset) - collapsedHeight;
            float scrollOffset = (float)mStatusBarHeight + scrolledRange;

            final float remainingScrollY = (scrollRatio + 1.0F) * scrolledRange;
            final float currentHeight = Math.max(Math.min((float)mNavigationBarHeight, (float)mNavigationBarHeight + remainingScrollY), 0.0F);

            float navBarHeight;

            if ((float)appBarLayout.getBottom() <= collapsedHeight) {

              if (dispatchImmersiveScrollEnable()) {
                if (mBottomArea != null && mBottomArea.getVisibility() != View.GONE && bottomAreaHeight != 0) {
                  navBarHeight = Math.min((float)bottomAreaHeight + remainingScrollY, currentHeight);
                  mBottomArea.setTranslationY(-navBarHeight);

                  if (mBottomArea.getVisibility() != View.VISIBLE) {
                    navBarHeight = Math.max((float)navBarHeight, 0.0F);
                  }else {
                    navBarHeight = Math.max((float) bottomAreaHeight + navBarHeight, 0.0F);
                  }
                } else {
                  navBarHeight = Math.max(currentHeight, 0.0F);
                }


                if (mNavigationBarBg != null) {

                  if (!isHideCameraCutout(mDecorViewInset)) {
                    mNavigationBarBg.setTranslationY(-Math.min(0.0F, remainingScrollY));
                  } else {
                    mNavigationBarBg.setTranslationY(0.0F);
                  }
                } else if (mNavigationBarHeight != 0) {
                  findSystemBarsBackground();
                  if (mNavigationBarBg != null) {
                    mNavigationBarBg.setTranslationY(0.0F);
                  }
                }

                if (mStatusBarBg != null) {
                  mStatusBarBg.setTranslationY(Math.min(0.0F, scrolledRange));
                }

                if (mCurOffset != scrollOffset) {
                  mCurOffset = scrollOffset;
                  if (mAnimationController != null) {
                    if (mAnimationController.isFinished()) {
                      Log.e(TAG, "AnimationController is already finished by App side");
                    } else {
                      int leftInset = 0;
                      int rightInset = 0;
                      forceHideRoundedCorner( (int)currentHeight);

                      if (SeslDisplayUtils.isPinEdgeEnabled(mContext)) {
                        Insets navBarInsets = mDecorViewInset.getInsets(WindowInsets.Type.navigationBars());
                        final int pinnedEdgeWidth = SeslDisplayUtils.getPinnedEdgeWidth(mContext);
                        final int activeEdgeArea = SeslDisplayUtils.getEdgeArea(mContext);

                        if (pinnedEdgeWidth == navBarInsets.left && activeEdgeArea == 0) {
                          leftInset = pinnedEdgeWidth;
                        }
                      }

                      final float topInset = Math.min((float)mStatusBarHeight, (float)mStatusBarHeight + scrolledRange);
                      final float animationProgress = (navBarHeight - currentHeight) / (float)(mNavigationBarHeight == 0 ? 1 : mNavigationBarHeight);

                      mAnimationController.setInsetsAndAlpha(
                              Insets.of(leftInset, (int)topInset, rightInset, (int)currentHeight),
                              1.0F,
                              animationProgress
                      );
                    }
                  }
                }

                immOffset = navBarHeight + totalScrollRange + (float)verticalOffset;

              } else {
                if (mStatusBarBg != null) {
                  mStatusBarBg.setTranslationY(0.0F);
                }

                if (mNavigationBarBg != null) {
                  mNavigationBarBg.setTranslationY(0.0F);
                }

                scrollOffset = (float)(mAppBarLayout.getTotalScrollRange() + verticalOffset);

                if (mBottomArea != null) {
                  float translationY = (float)bottomAreaHeight;
                  if (bottomInset == 0) {
                    collapsedHeight = 1.0F;
                  }
                  translationY -= (float)mAppBarLayout.getBottom() * (float)bottomAreaHeight / collapsedHeight;
                  mBottomArea.setTranslationY(Math.max(translationY, 0.0F));
                  immOffset = (float)((int)(scrollOffset + (float)mBottomArea.getHeight() - Math.max(translationY, 0.0F)));
                }else{
                  immOffset = scrollOffset;
                }

                finishWindowInsetsAnimationController();
              }
            } else {
              float translationY = (float)(mAppBarLayout.getTotalScrollRange() + verticalOffset);
              if (mIsMultiWindow) {
                if (mBottomArea != null) {
                  mBottomArea.setTranslationY(0.0F);
                  translationY += (float)mBottomArea.getHeight();
                }
              }

              immOffset = translationY;
              if (!mIsMultiWindow) {
                if (mBottomArea != null) {
                  if (mDecorViewInset != null) {
                    if (isNavigationBarBottomPosition()) {
                      mBottomArea.setTranslationY((float)(-mNavigationBarHeight));
                    } else if (mNavigationBarBg != null && mNavigationBarBg.getTranslationY() != 0.0F) {
                      mBottomArea.setTranslationY(0.0F);
                    }
                    immOffset = translationY + (float)mBottomArea.getHeight() + (float)mNavigationBarHeight;
                  }
                }
              }
            }
          } else {
            if (mStatusBarBg != null) {
              mStatusBarBg.setTranslationY(0.0F);
            }

            if (mNavigationBarBg != null) {
              mNavigationBarBg.setTranslationY(0.0F);
            }

            immOffset = 0.0F;
            if (mBottomArea != null) {
              mBottomArea.setTranslationY(0.0F);
            }
          }

          if (mAppBarLayout != null) {
            mAppBarLayout.onImmOffsetChanged((int)immOffset);
          }

        }
      }
    }
  };

  private final WindowInsetsAnimation.Callback mWindowAnimationCallback
          = new WindowInsetsAnimation.Callback(
          WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
    @NonNull
    @Override
    public WindowInsets onProgress(@NonNull WindowInsets windowInsets,
                                   @NonNull List<WindowInsetsAnimation> list) {
      return windowInsets;
    }

    @Override
    public void onEnd(@NonNull WindowInsetsAnimation animation) {
      super.onEnd(animation);
      if (mContentView != null && !mAppBarLayout.isDetachedState()) {
        mDecorViewInset = mContentView.getRootWindowInsets();
        if (mDecorViewInset != null) {
          mContentView.dispatchApplyWindowInsets(mDecorViewInset);
        }
      }
    }
  };

  private final WindowInsetsAnimationControlListener mWindowInsetsAnimationControlListener
          = new WindowInsetsAnimationControlListener() {
    @Override
    public void onReady(@NonNull WindowInsetsAnimationController controller, int types) {
      if (mDecorView != null) {
        mCancellationSignal = null;
        mAnimationController = controller;
        mPendingRequestOnReady = null;
        setInsetsAndAlphaToDefault();
      }
    }

    @Override
    public void onFinished(@NonNull WindowInsetsAnimationController controller) {
      resetWindowInsetsAnimationController();
    }

    @Override
    public void onCancelled(@Nullable WindowInsetsAnimationController controller) {
      cancelWindowInsetsAnimationController();
    }
  };

  private int mNavigationBarHeight;
  private int mPrevOffset;
  private int mPrevOrientation;
  private int mStatusBarHeight;

  private float mCurOffset = 0f;
  private float mHeightProportion;

  boolean mCalledHideShowOnLayoutChlid = false;
  private boolean mCanImmersiveScroll;
  private boolean mIsDeskTopMode;
  private boolean mIsMultiWindow;
  private boolean mNeedRestoreAnim = true;
  private boolean mShownAtDown;
  private boolean mToolIsMouse;
  private boolean isRoundedCornerHide = false;
  private boolean useCustomAnimationCallback = false;

  public SeslImmersiveScrollBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    updateSystemBarsHeight();
    updateAppBarHeightProportion();
  }

  private boolean isDexEnabled() {
    if (mContext == null) {
      return false;
    }
    return SeslConfigurationReflector
            .isDexEnabled(mContext.getResources().getConfiguration());
  }

  private boolean getCurrentNavbarCanMoveState() {
    try {
      final int resId = Resources.getSystem()
              .getIdentifier("config_navBarCanMove",
                      "bool", "android");
      return mContext.getApplicationContext()
              .getResources().getBoolean(resId);
    } catch (Exception e) {
      Log.e(TAG, "ERROR, e : " + e.getMessage());
      return true;
    }
  }

  private boolean isAccessibilityEnable() {
    if (mContext == null) {
      return false;
    }
    AccessibilityManager manager
            = (AccessibilityManager) mContext
            .getSystemService(Context.ACCESSIBILITY_SERVICE);
    return manager.isTouchExplorationEnabled();
  }

  private boolean canImmersiveScroll() {
    if (mAppBarLayout != null && Build.VERSION.SDK_INT >= 30) {
      if (!isDexEnabled() && !useCustomAnimationCallback) {
        if (mAppBarLayout.getIsMouse()) {
          prepareImmersiveScroll(false, false);
          return false;
        } else if (isAccessibilityEnable()) {
          Log.i(TAG,
                  "Disable ImmersiveScroll due to accessibility enabled");
          updateOrientationState();
          prepareImmersiveScroll(false, true);
          return false;
        } else {
          if (mDecorView != null) {
            mDecorViewInset = mDecorView.getRootWindowInsets();
            if (mDecorViewInset != null) {
              final boolean isImeVisible
                      = mDecorViewInset.isVisible(WindowInsets.Type.ime());
              updateOrientationState();
              if (isImeVisible || (mDecorView.findFocus() instanceof EditText)) {
                prepareImmersiveScroll(false, true);
                return false;
              }
            }
          }

          if (mAppBarLayout.isActivatedImmsersiveScroll()) {
            prepareImmersiveScroll(true, false);
            final boolean isPortrait = getCurrentNavbarCanMoveState() ?
                    updateOrientationState() : true;

            if (mContext != null) {
              Activity activity = SeslContextUtils.getActivity(mContext);

              if (activity == null && mAppBarLayout != null) {
                mContext = mAppBarLayout.getContext();
                activity = SeslContextUtils.getActivity(mAppBarLayout.getContext());
              }

              if (activity != null) {
                final boolean isMultiWindow = isMultiWindow(activity);

                if (mIsMultiWindow != isMultiWindow) {
                  forceRestoreWindowInset(true);
                  cancelWindowInsetsAnimationController();
                }

                mIsMultiWindow = isMultiWindow;

                if (isMultiWindow) {
                  return false;
                }
              }
            }

            return isPortrait;
          }

          if (mAppBarLayout != null && mAppBarLayout.isImmersiveActivatedByUser()) {
            cancelWindowInsetsAnimationController();
          }
          prepareImmersiveScroll(false, false);
        }
      }
    }

    return false;
  }

  void setupDecorFitsSystemWindow(boolean decorFitsSystemWindows) {
    Activity activity = SeslContextUtils.getActivity(mContext);

    if (activity != null && mAppBarLayout != null) {
      activity.getWindow().setDecorFitsSystemWindows(decorFitsSystemWindows);
      if (mBottomArea != null) {
        mBottomArea.setTranslationY(0f);
      }
    }

    if (mStatusBarBg != null && mStatusBarBg.getTranslationY() != 0f) {
      mStatusBarBg.setTranslationY(0f);
    }
  }

  protected boolean dispatchImmersiveScrollEnable() {
    if (mAppBarLayout != null && !mAppBarLayout.isDetachedState()) {
      final boolean canImmersiveScroll = canImmersiveScroll();
      setupDecorsFitSystemWindowState(canImmersiveScroll);
      updateAppBarHeightProportion();
      updateSystemBarsHeight();
      return canImmersiveScroll;
    } else {
      return false;
    }
  }

  private void prepareImmersiveScroll(boolean canImmersiveScroll,
                                      boolean showWindowInset) {
    if (mCanImmersiveScroll != canImmersiveScroll ) {
      mCanImmersiveScroll = canImmersiveScroll;
      forceRestoreWindowInset(showWindowInset);
      setupDecorsFitSystemWindowState(canImmersiveScroll);
      setAppBarScrolling(canImmersiveScroll);
    }
  }

  private boolean isMultiWindow(Activity activity) {
    return activity.isInMultiWindowMode();
  }

  @Override
  public boolean onMeasureChild(
          @NonNull CoordinatorLayout parent,
          @NonNull AppBarLayout child,
          int parentWidthMeasureSpec,
          int widthUsed,
          int parentHeightMeasureSpec,
          int heightUsed) {
    dispatchImmersiveScrollEnable();
    return super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed);
  }

  private boolean updateOrientationState() {
    if (mAppBarLayout != null) {
      final int currentOrientation
              = mAppBarLayout.getCurrentOrientation();

      if (mPrevOrientation != currentOrientation) {
        mPrevOrientation = currentOrientation;
        forceRestoreWindowInset(true);
        mCalledHideShowOnLayoutChlid = false;
      }

      switch (currentOrientation) {
        case Configuration.ORIENTATION_PORTRAIT:
          return true;
        case Configuration.ORIENTATION_LANDSCAPE:
          return false;
        default:
          Log.e(TAG,
                  "ERROR, e : AppbarLayout Configuration is wrong");
          return false;
      }
    } else {
      return false;
    }
  }

  private boolean isLandscape() {
    return mAppBarLayout != null
            && mAppBarLayout.getCurrentOrientation()
            == Configuration.ORIENTATION_LANDSCAPE;
  }

  private boolean isNavigationBarBottomPosition() {
    if (mDecorViewInset == null) {
      if (mDecorView == null) {
        mDecorView = mAppBarLayout.getRootView();
      }
      mDecorViewInset = mDecorView.getRootWindowInsets();
    }

    return mDecorViewInset == null
            || mDecorViewInset.getInsets(
            WindowInsets.Type.navigationBars()).bottom != 0;
  }

  private void setupDecorsFitSystemWindowState(boolean canImmersiveScroll) {
    if (mDecorView != null
            && mAppBarLayout != null && !useCustomAnimationCallback) {
      if (mContext == null) {
        mContext = mAppBarLayout.getContext();
        if (mContext == null) {
          return;
        }
      }

      Activity activity = SeslContextUtils.getActivity(mContext);
      if (activity == null && mAppBarLayout != null) {
        mContext = mAppBarLayout.getContext();
        activity = SeslContextUtils.getActivity(mAppBarLayout.getContext());
      }

      if (activity != null) {
        Window window = activity.getWindow();

        if (canImmersiveScroll) {
          if (isHideCameraCutout(mDecorViewInset)) {
            mAppBarLayout.setImmersiveTopInset(0);
          } else {
            mAppBarLayout.setImmersiveTopInset(mStatusBarHeight);
          }

          window.setDecorFitsSystemWindows(false);
          window.getDecorView().setFitsSystemWindows(false);

          if (mDecorViewInset != null) {
            final int statusBarHeight = mDecorViewInset
                    .getInsets(WindowInsets.Type.statusBars()).top;
            if (statusBarHeight != 0 && statusBarHeight != mStatusBarHeight) {
              mStatusBarHeight = statusBarHeight;
              mAppBarLayout.setImmersiveTopInset(statusBarHeight);
            }
          }
        } else {
          mAppBarLayout.setImmersiveTopInset(0);

          window.setDecorFitsSystemWindows(true);
          window.getDecorView().setFitsSystemWindows(true);

          if (!isNavigationBarBottomPosition() && isLandscape()) {
            if (mWindowInsetsController == null) {
              setWindowInsetsController();
            }

            mDecorViewInset = mDecorView.getRootWindowInsets();
            if (mWindowInsetsController != null && mDecorViewInset != null) {
              if (mDecorViewInset.getInsets(WindowInsets.Type.statusBars()).top != 0) {
                try {
                  mWindowInsetsController.hide(WindowInsets.Type.statusBars());
                } catch (IllegalStateException e) {
                  Log.w(TAG, "setupDecorsFitSystemWindowState: mWindowInsetsController.hide failed!");
                }
              }
            }
          }
        }
      }
    }
  }

  void updatePunchHole(final boolean update) {
    if (mDecorViewInset == null) {
      if (mDecorView == null) {
        return;
      }

      if (mContentView == null) {
        mContentView = mDecorView.findViewById(android.R.id.content);
      }
      mDecorViewInset = mDecorView.getRootWindowInsets();
    }

    final Insets cutoutInsets = mDecorViewInset
            .getInsets(WindowInsets.Type.displayCutout());
    if (cutoutInsets != null && mContentView != null) {
      mContentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
        @Override
        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
          View child = ((ViewGroup) mDecorView).getChildAt(0);

          boolean setContentPadding = true;
          if (mAppBarLayout != null
                  && mAppBarLayout.isImmersiveActivatedByUser()) {
            setContentPadding = !update;
          } else if (mDecorView != null && child != null) {
            setContentPadding
                    = child.getPaddingStart() != 0 || child.getPaddingEnd() != 0;
          }

          if (setContentPadding) {
            mContentView.setPadding(
                    cutoutInsets.left, 0,
                    cutoutInsets.right, cutoutInsets.bottom);
          } else {
            mContentView.setPadding(0, 0, 0, 0);
          }

          return insets;
        }
      });
    }
  }

  @Override
  protected void layoutChild(
          @NonNull CoordinatorLayout parent, @NonNull AppBarLayout child, int layoutDirection) {
    super.layoutChild(parent, child, layoutDirection);

    if (mWindowInsetsController != null) {
      mWindowInsetsController.addOnControllableInsetsChangedListener(
              new WindowInsetsController.OnControllableInsetsChangedListener() {
                @Override
                public void onControllableInsetsChanged(
                        @NonNull WindowInsetsController controller, int typeMask) {
                  if (isLandscape()
                          && !isNavigationBarBottomPosition() && !mCalledHideShowOnLayoutChlid) {
                    controller.hide(WindowInsets.Type.navigationBars());
                    controller.show(WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    mCalledHideShowOnLayoutChlid = true;
                  }

                  if (typeMask == 8) {
                    mDecorViewInset = mDecorView.getRootWindowInsets();
                    if (mDecorViewInset != null
                            && mDecorViewInset.isVisible(WindowInsets.Type.statusBars())
                            && isAppBarHide()) {
                      seslRestoreTopAndBottom();
                    }
                  }
                }
              });
    }

    if (mAppBarLayout == null || child != mAppBarLayout) {
      initImmViews(parent, child);
    }
  }

  void initImmViews(
          @NonNull CoordinatorLayout parent, @NonNull AppBarLayout abl) {
    mAppBarLayout = abl;
    mCoordinatorLayout = parent;
    mAppBarLayout.addOnOffsetChangedListener(mOffsetChangedListener);
    if (!mAppBarLayout.isImmersiveActivatedByUser() && !isDexEnabled()) {
      mAppBarLayout.internalActivateImmersiveScroll(true, false);
    }

    mDecorView = mAppBarLayout.getRootView();
    mContentView = mDecorView.findViewById(android.R.id.content);

    if (useCustomAnimationCallback) {
      mContentView
              .setWindowInsetsAnimationCallback(mCustomWindowInsetsAnimation);
    } else {
      mContentView
              .setWindowInsetsAnimationCallback(mWindowAnimationCallback);
    }

    findSystemBarsBackground();
    dispatchImmersiveScrollEnable();

    for (int i = 0; i < abl.getChildCount(); i++) {
      View child = abl.getChildAt(i);
      if (mCollapsingToolbarLayout != null) {
        break;
      } else if (child instanceof CollapsingToolbarLayout) {
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) child;
        break;
      }
    }

    View bottomArea = parent.findViewById(R.id.bottom_bar_overlay);
    if (mBottomArea == null || bottomArea != null) {
      mBottomArea = bottomArea;
    }
  }

  void setWindowInsetsAnimationCallback(
          @NonNull AppBarLayout abl, @Nullable WindowInsetsAnimation.Callback callback) {
    if (mContentView == null) {
      mDecorView = abl.getRootView();
      mContentView = mDecorView.findViewById(android.R.id.content);
    }

    if (callback == null) {
      useCustomAnimationCallback = false;
    } else {
      mCustomWindowInsetsAnimation = callback;
      useCustomAnimationCallback = true;
    }

    if (useCustomAnimationCallback) {
      mContentView.setWindowInsetsAnimationCallback(mCustomWindowInsetsAnimation);
      prepareImmersiveScroll(false, false);
      if (mBottomArea != null) {
        mBottomArea.setTranslationY(0f);
      }
    } else {
      mContentView.setPadding(0, 0, 0, 0);
      mContentView.setWindowInsetsAnimationCallback(mWindowAnimationCallback);
    }
  }

  private void findSystemBarsBackground() {
    if (mDecorView != null && mContext != null) {
      mDecorViewInset = mDecorView.getRootWindowInsets();
      mDecorView.getViewTreeObserver().addOnPreDrawListener(
              new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                  mDecorView.getViewTreeObserver()
                          .removeOnPreDrawListener(this);
                  mStatusBarBg = mDecorView
                          .findViewById(android.R.id.statusBarBackground);
                  mNavigationBarBg = mDecorView
                          .findViewById(android.R.id.navigationBarBackground);
                  return false;
                }
              });
      updateSystemBarsHeight();
    }
  }

  private void updateSystemBarsHeight() {
    if (mContext != null) {
      final Resources res = mContext.getResources();

      final int statusBarResId
              = res.getIdentifier(
              "status_bar_height", "dimen", "android");
      if (statusBarResId > 0) {
        mStatusBarHeight = res.getDimensionPixelSize(statusBarResId);
      }

      mNavigationBarHeight = 0;

      if (mDecorView != null) {
        mDecorViewInset = mDecorView.getRootWindowInsets();
        if (mDecorViewInset != null) {
          mNavigationBarHeight
                  = mDecorViewInset.getInsets(WindowInsets.Type.navigationBars()).bottom;
        }
      }

      if (mNavigationBarHeight == 0) {
        final int showNavBarResId = res.getIdentifier(
                "config_showNavigationBar", "bool", "android");
        if (showNavBarResId <= 0 || res.getBoolean(showNavBarResId)) {
          final int navBarHeightResId
                  = res.getIdentifier(
                  "navigation_bar_height", "dimen", "android");
          if (navBarHeightResId > 0) {
            mNavigationBarHeight = res.getDimensionPixelSize(navBarHeightResId);
          }
        }
      }
    }
  }

  private void updateAppBarHeightProportion() {
    if (mAppBarLayout != null) {
      if (mContext == null) {
        mContext = mAppBarLayout.getContext();
        if (mContext == null) {
          return;
        }
      }

      final Resources res = mContext.getResources();

      mHeightProportion = ResourcesCompat
              .getFloat(res, R.dimen.sesl_appbar_height_proportion);

      float immHeightProportion = 0f;
      if (mHeightProportion != 0f) {
        immHeightProportion
                = mHeightProportion + getDifferImmHeightRatio(res);
      }

      if (mCanImmersiveScroll) {
        mAppBarLayout.internalProportion(immHeightProportion);
      } else {
        mAppBarLayout.internalProportion(mHeightProportion);
      }
    }
  }

  private float getDifferImmHeightRatio(Resources res) {
    return mStatusBarHeight / res.getDisplayMetrics().heightPixels;
  }

  private void setAppBarScrolling(boolean canScroll) {
    if (canScroll != mAppBarLayout.getCanScroll()) {
      mAppBarLayout.setCanScroll(canScroll);
    }
  }

  @Override
  public boolean onStartNestedScroll(
          @NonNull CoordinatorLayout parent,
          @NonNull AppBarLayout child,
          @NonNull View directTargetChild,
          @NonNull View target,
          int nestedScrollAxes,
          int type) {
    mTargetView = target;

    if (dispatchImmersiveScrollEnable()
            && mAnimationController == null) {
      startAnimationControlRequest();
    }

    return super.onStartNestedScroll(
            parent,
            child,
            directTargetChild,
            target,
            nestedScrollAxes,
            type);
  }

  @Override
  public void onNestedScroll(
          @NonNull CoordinatorLayout coordinatorLayout,
          @NonNull AppBarLayout child,
          @NonNull View target,
          int dxConsumed,
          int dyConsumed,
          int dxUnconsumed,
          int dyUnconsumed,
          int type,
          int[] consumed) {
    mTargetView = target;
    super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed);
  }

  @Override
  public void onNestedPreScroll(
          CoordinatorLayout coordinatorLayout,
          AppBarLayout child,
          View target,
          int dx,
          int dy,
          int[] consumed,
          int type) {
    mTargetView = target;
    if (mCancellationSignal != null) {
      consumed[0] = dx;
      consumed[1] = dy;
    } else {
      super.onNestedPreScroll(
              coordinatorLayout,
              child,
              target,
              dx,
              dy,
              consumed,
              type);
    }
  }

  @Override
  public void onStopNestedScroll(
          CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target, int type) {
    mTargetView = target;
    super.onStopNestedScroll(coordinatorLayout, abl, target, type);
  }

  void cancelWindowInsetsAnimationController() {
    if (mDecorView != null) {
      mDecorViewInset = mDecorView.getRootWindowInsets();
      if (mDecorViewInset != null) {
        mShownAtDown = mDecorViewInset.isVisible(WindowInsets.Type.statusBars())
                || mDecorViewInset.isVisible(WindowInsets.Type.navigationBars());
      }
    }

    if (mAnimationController != null) {
      mAnimationController.finish(mShownAtDown);
    }

    if (mCancellationSignal != null) {
      mCancellationSignal.cancel();
    }

    resetWindowInsetsAnimationController();
  }

  void notifyOnApplyWindowInsets() {
    if (mAppBarLayout != null) {
      cancelWindowInsetsAnimationController();
      dispatchImmersiveScrollEnable();
      mAppBarLayout.onOffsetChanged(getTopAndBottomOffset());
    }
  }

  void forceRestoreWindowInset(boolean force) {
    if (mWindowInsetsController != null) {
      mDecorViewInset = mDecorView.getRootWindowInsets();
      showWindowInset(force);
    }
  }

  void showWindowInset(boolean force) {
    if (mWindowInsetsController != null && mDecorViewInset != null) {
      if (!(mDecorViewInset.isVisible(WindowInsets.Type.statusBars())
              && mDecorViewInset.isVisible(WindowInsets.Type.navigationBars()))
              || isAppBarHide() || force) {
        try {
          mWindowInsetsController.show(WindowInsets.Type.systemBars());
        } catch (IllegalStateException e) {
          Log.w(TAG, "showWindowInset: mWindowInsetsController.show failed!");
        }
      }
    }
  }

  private void finishWindowInsetsAnimationController() {
    if (mAppBarLayout != null) {
      if (mContentView == null) {
        mDecorView = mAppBarLayout.getRootView();
        mContentView = mDecorView.findViewById(android.R.id.content);
      }

      if (mAnimationController == null) {
        if (mCancellationSignal != null) {
          mCancellationSignal.cancel();
        }
      } else {
        final int currentBottom
                = mAnimationController.getCurrentInsets().bottom;
        final int shownBottom
                = mAnimationController.getShownStateInsets().bottom;
        final int hiddenBottom
                = mAnimationController.getHiddenStateInsets().bottom;

        if (currentBottom == shownBottom) {
          mAnimationController.finish(true);
        } else if (currentBottom == hiddenBottom) {
          mAnimationController.finish(false);
        }
      }
    }
  }

  private void setWindowInsetsController() {
    if (mDecorView != null && mAnimationController == null
            && mWindowInsetsController == null) {
      mWindowInsetsController = mDecorView.getWindowInsetsController();
    }
  }

  private void startAnimationControlRequest() {
    setWindowInsetsController();

    if (mCancellationSignal == null) {
      mCancellationSignal = new CancellationSignal();
    }

    final int systemBars = WindowInsets.Type.systemBars();
    if (!isHideCameraCutout(mDecorViewInset)) {
      try {
        mWindowInsetsController.hide(systemBars);
      } catch (IllegalStateException e) {
        Log.w(TAG, "startAnimationControlRequest: mWindowInsetsController.hide failed!");
      }
    }
    mWindowInsetsController.setSystemBarsBehavior(
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    mWindowInsetsController
            .controlWindowInsetsAnimation(
                    systemBars,
                    -1,
                    null,
                    mCancellationSignal,
                    mWindowInsetsAnimationControlListener);
  }


  private void setInsetsAndAlphaToDefault() {
    int leftInset = 0;
    int rightInset = 0;
    if (SeslDisplayUtils.isPinEdgeEnabled(mContext)) {
      Insets navBarInsets = mDecorViewInset.getInsets(WindowInsets.Type.navigationBars());
      final int pinnedEdgeWidth = SeslDisplayUtils.getPinnedEdgeWidth(mContext);
      final int activeEdgeArea = SeslDisplayUtils.getEdgeArea(mContext);
      if (pinnedEdgeWidth == navBarInsets.left && activeEdgeArea == 0) {
        leftInset = pinnedEdgeWidth;
      }
    }
    mAnimationController.setInsetsAndAlpha(
            Insets.of(leftInset, (int)mStatusBarHeight, rightInset, (int)mNavigationBarHeight),
            1.0F,
            1.0F);
  }

  private void resetWindowInsetsAnimationController() {
    mAnimationController = null;
    mCancellationSignal = null;
    mShownAtDown = false;
    mPendingRequestOnReady = null;
  }

  private boolean isMouseEvent(MotionEvent event) {
    return event.getToolType(0)
            == MotionEvent.TOOL_TYPE_MOUSE;
  }

  @Override
  public boolean onInterceptTouchEvent(
          @NonNull CoordinatorLayout parent,
          @NonNull AppBarLayout child, @NonNull MotionEvent ev) {
    final boolean isMouseEvent = isMouseEvent(ev);
    if (mToolIsMouse != isMouseEvent) {
      mToolIsMouse = isMouseEvent;
      child.seslSetIsMouse(isMouseEvent);
    }
    return super.onInterceptTouchEvent(parent, child, ev);
  }

  @Override
  protected boolean dispatchGenericMotionEvent(MotionEvent event) {
    final boolean isMouseEvent = isMouseEvent(event);
    if (mToolIsMouse != isMouseEvent) {
      mToolIsMouse = isMouseEvent;
      if (mAppBarLayout != null) {
        mAppBarLayout.seslSetIsMouse(isMouseEvent);
        dispatchImmersiveScrollEnable();
      }
    }
    return super.dispatchGenericMotionEvent(event);
  }

  boolean isAppBarHide() {
    return mAppBarLayout != null
            && ((float) (mAppBarLayout.getBottom() + mAppBarLayout.getPaddingBottom()))
            < mAppBarLayout.seslGetCollapsedHeight();
  }

  private boolean startRestoreAnimation() {
    if (!isAppBarHide()) {
      return false;
    } else {
      animateRestoreTopAndBottom(
              mCoordinatorLayout, mAppBarLayout,
              -mAppBarLayout.getUpNestedPreScrollRange());
      return true;
    }
  }

  void seslRestoreTopAndBottom() {
    seslRestoreTopAndBottom(true);
  }

  void seslRestoreTopAndBottom(boolean animate) {
    Log.i(TAG,
            " Restore top and bottom areas [Animate] " + animate);
    mNeedRestoreAnim = animate;
    restoreTopAndBottomInternal();
  }

  void seslSetBottomView(@Nullable View view) {
    mBottomArea = view;
  }

  private void restoreTopAndBottomInternal() {
    if (mAppBarLayout != null && isAppBarHide()) {
      if (mAnimationHandler.hasMessages(MSG_APPEAR_ANIMATION)) {
        mAnimationHandler.removeMessages(MSG_APPEAR_ANIMATION);
      }
      mAnimationHandler.sendEmptyMessageDelayed(
              MSG_APPEAR_ANIMATION, 100);
    }

    if (mBottomArea != null && mNavigationBarBg != null) {
      if (!mAnimationHandler.hasMessages(MSG_APPEAR_ANIMATION)) {
        if (mAppBarLayout != null
                && !mAppBarLayout.isActivatedImmsersiveScroll()) {
          mBottomArea.setTranslationY(0f);
        }
      }
    }
  }

  private void animateRestoreTopAndBottom(
          CoordinatorLayout coordinatorLayout,
          AppBarLayout child,
          int offset) {
    animateOffsetWithDuration(coordinatorLayout, child, offset);
  }


  private void animateOffsetWithDuration(CoordinatorLayout coordinatorLayout,
                                         AppBarLayout child, int offset) {

    mPrevOffset = offset;

    final float appBarCollapsedHeight = mAppBarLayout.seslGetCollapsedHeight()
            - (float)(mAppBarLayout.getHeight());

    if (mOffsetAnimator == null) {
      mOffsetAnimator = new ValueAnimator();
      mOffsetAnimator.addUpdateListener(animation -> {
                if (mTargetView == null) {
                  Log.e(TAG, "mTargetView is null");
                  return;
                }
                final int intValue = (int) animation.getAnimatedValue();
                final int[] iArr = {mPrevOffset - intValue};
                mTargetView.scrollBy(0, -iArr[0]);
                setHeaderTopBottomOffset(coordinatorLayout, child, intValue);
                mPrevOffset = intValue;
              }
      );
    } else {
      mOffsetAnimator.cancel();
    }

    mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);

        if (mNavigationBarBg != null) {
          mNavigationBarBg.setTranslationY(0f);
        }

        if (mAnimationController != null) {
          mAnimationController.finish(true);
        }
      }
    });

    mOffsetAnimator.setDuration(150L);
    mOffsetAnimator.setInterpolator(new PathInterpolator(0.17F,
            0.17F, 0.2F, 1.0F));
    mOffsetAnimator.setStartDelay(0L);

    if (mNeedRestoreAnim) {
      offset = -mAppBarLayout.getHeight();
    } else {
      offset = (int)appBarCollapsedHeight;
    }

    mOffsetAnimator.setIntValues(offset, (int)appBarCollapsedHeight);
    mOffsetAnimator.start();
  }

  private void forceHideRoundedCorner(int bottom) {
    if (mAnimationController != null && mDecorView != null) {
      final boolean isRoundedCornerHide
              = bottom != mAnimationController.getShownStateInsets().bottom;
      if (isRoundedCornerHide != this.isRoundedCornerHide) {
        this.isRoundedCornerHide = isRoundedCornerHide;
        SeslDecorViewReflector
                .semSetForceHideRoundedCorner(mDecorView, isRoundedCornerHide);
      }
    }
  }

  private boolean isHideCameraCutout(WindowInsets insets) {
    return insets.getDisplayCutout() == null
            && insets.getInsets(WindowInsets.Type.systemBars()).top == 0;
  }
}
