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

package com.google.android.material.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Utils class for custom views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ViewUtils {

  private ViewUtils() {}

  public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
    switch (value) {
      case 3:
        return PorterDuff.Mode.SRC_OVER;
      case 5:
        return PorterDuff.Mode.SRC_IN;
      case 9:
        return PorterDuff.Mode.SRC_ATOP;
      case 14:
        return PorterDuff.Mode.MULTIPLY;
      case 15:
        return PorterDuff.Mode.SCREEN;
      case 16:
        return PorterDuff.Mode.ADD;
      default:
        return defaultMode;
    }
  }

  public static boolean isLayoutRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  public static float dpToPx(@NonNull Context context, @Dimension(unit = Dimension.DP) int dp) {
    Resources r = context.getResources();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
  }

  public static void requestFocusAndShowKeyboard(@NonNull final View view) {
    view.requestFocus();
    view.post(
        new Runnable() {
          @Override
          public void run() {
            InputMethodManager inputMethodManager =
                (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
          }
        });
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} which also passes
   * the initial padding set on the view. Used with {@link #doOnApplyWindowInsets(View,
   * OnApplyWindowInsetsListener)}.
   */
  public interface OnApplyWindowInsetsListener {

    /**
     * When {@link View#setOnApplyWindowInsetsListener(View.OnApplyWindowInsetsListener) set} on a
     * View, this listener method will be called instead of the view's own {@link
     * View#onApplyWindowInsets(WindowInsets)} method. The {@code initialPadding} is the view's
     * original padding which can be updated and will be applied to the view automatically. This
     * method should return a new {@link WindowInsetsCompat} with any insets consumed.
     */
    WindowInsetsCompat onApplyWindowInsets(
        View view, WindowInsetsCompat insets, RelativePadding initialPadding);
  }

  /** Simple data object to store the initial padding for a view. */
  public static class RelativePadding {
    public int start;
    public int top;
    public int end;
    public int bottom;

    public RelativePadding(int start, int top, int end, int bottom) {
      this.start = start;
      this.top = top;
      this.end = end;
      this.bottom = bottom;
    }

    public RelativePadding(@NonNull RelativePadding other) {
      this.start = other.start;
      this.top = other.top;
      this.end = other.end;
      this.bottom = other.bottom;
    }

    /** Applies this relative padding to the view. */
    public void applyToView(View view) {
      ViewCompat.setPaddingRelative(view, start, top, end, bottom);
    }
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} that can
   * automatically apply inset padding based on view attributes.
   */
  public static void doOnApplyWindowInsets(
      @NonNull View view, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    doOnApplyWindowInsets(view, attrs, defStyleAttr, defStyleRes, null);
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} that can
   * automatically apply inset padding based on view attributes.
   */
  public static void doOnApplyWindowInsets(
      @NonNull View view,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes,
      @Nullable final OnApplyWindowInsetsListener listener) {
    TypedArray a =
        view.getContext()
            .obtainStyledAttributes(attrs, R.styleable.Insets, defStyleAttr, defStyleRes);

    final boolean paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingBottomSystemWindowInsets, false);
    final boolean paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingLeftSystemWindowInsets, false);
    final boolean paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingRightSystemWindowInsets, false);

    a.recycle();

    doOnApplyWindowInsets(
        view,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            if (paddingBottomSystemWindowInsets) {
              initialPadding.bottom += insets.getSystemWindowInsetBottom();
            }
            boolean isRtl = isLayoutRtl(view);
            if (paddingLeftSystemWindowInsets) {
              if (isRtl) {
                initialPadding.end += insets.getSystemWindowInsetLeft();
              } else {
                initialPadding.start += insets.getSystemWindowInsetLeft();
              }
            }
            if (paddingRightSystemWindowInsets) {
              if (isRtl) {
                initialPadding.start += insets.getSystemWindowInsetRight();
              } else {
                initialPadding.end += insets.getSystemWindowInsetRight();
              }
            }
            initialPadding.applyToView(view);
            return listener != null
                ? listener.onApplyWindowInsets(view, insets, initialPadding)
                : insets;
          }
        });
  }

  /**
   * Wrapper around {@link androidx.core.view.OnApplyWindowInsetsListener} that records the
   * initial padding of the view and requests that insets are applied when attached.
   */
  public static void doOnApplyWindowInsets(
      @NonNull View view, @NonNull final OnApplyWindowInsetsListener listener) {
    // Create a snapshot of the view's padding state.
    final RelativePadding initialPadding =
        new RelativePadding(
            ViewCompat.getPaddingStart(view),
            view.getPaddingTop(),
            ViewCompat.getPaddingEnd(view),
            view.getPaddingBottom());
    // Set an actual OnApplyWindowInsetsListener which proxies to the given callback, also passing
    // in the original padding state.
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
            return listener.onApplyWindowInsets(view, insets, new RelativePadding(initialPadding));
          }
        });
    // Request some insets.
    requestApplyInsetsWhenAttached(view);
  }

  /** Requests that insets should be applied to this view once it is attached. */
  public static void requestApplyInsetsWhenAttached(@NonNull View view) {
    if (ViewCompat.isAttachedToWindow(view)) {
      // We're already attached, just request as normal.
      ViewCompat.requestApplyInsets(view);
    } else {
      // We're not attached to the hierarchy, add a listener to request when we are.
      view.addOnAttachStateChangeListener(
          new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
              v.removeOnAttachStateChangeListener(this);
              ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {}
          });
    }
  }

  /**
   * Returns the absolute elevation of the parent of the provided {@code view}, or in other words,
   * the sum of the elevations of all ancestors of the {@code view}.
   */
  public static float getParentAbsoluteElevation(@NonNull View view) {
    float absoluteElevation = 0;
    ViewParent viewParent = view.getParent();
    while (viewParent instanceof View) {
      absoluteElevation += ViewCompat.getElevation((View) viewParent);
      viewParent = viewParent.getParent();
    }
    return absoluteElevation;
  }

  /**
   * Backward-compatible {@link View#getOverlay()}. TODO(b/144937975): Remove and use the official
   * version from androidx when it's available.
   */
  @Nullable
  public static ViewOverlayImpl getOverlay(@Nullable View view) {
    if (view == null) {
      return null;
    }
    if (Build.VERSION.SDK_INT >= 18) {
      return new ViewOverlayApi18(view);
    }
    return ViewOverlayApi14.createFrom(view);
  }

  /** Returns the content view that is the parent of the provided view. */
  @Nullable
  public static ViewGroup getContentView(@Nullable View view) {
    if (view == null) {
      return null;
    }

    View rootView = view.getRootView();
    ViewGroup contentView = rootView.findViewById(android.R.id.content);
    if (contentView != null) {
      return contentView;
    }

    // Account for edge cases: Parent's parent can be null without ever having found
    // android.R.id.content (e.g. if view is in an overlay during a transition).
    // Additionally, sometimes parent's parent is neither a ViewGroup nor a View (e.g. if view
    // is in a PopupWindow).
    if (rootView != view && rootView instanceof ViewGroup) {
      return (ViewGroup) rootView;
    }

    return null;
  }

  /**
   * Returns the content view overlay that can be used to add drawables on top of all other views.
   */
  @Nullable
  public static ViewOverlayImpl getContentViewOverlay(@NonNull View view) {
    return getOverlay(getContentView(view));
  }

  public static void addOnGlobalLayoutListener(
      @Nullable View view, @NonNull OnGlobalLayoutListener victim) {
    if (view != null) {
      view.getViewTreeObserver().addOnGlobalLayoutListener(victim);
    }
  }

  public static void removeOnGlobalLayoutListener(
      @Nullable View view, @NonNull OnGlobalLayoutListener victim) {
    if (view != null) {
      removeOnGlobalLayoutListener(view.getViewTreeObserver(), victim);
    }
  }

  public static void removeOnGlobalLayoutListener(
      @NonNull ViewTreeObserver viewTreeObserver, @NonNull OnGlobalLayoutListener victim) {
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
      viewTreeObserver.removeOnGlobalLayoutListener(victim);
    } else {
      viewTreeObserver.removeGlobalOnLayoutListener(victim);
    }
  }
}
