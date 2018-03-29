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

package android.support.design.snackbar;

import android.support.design.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Base class for {@link Snackbar}. */
// TODO: remove/merge with {@link Snackbar} after the widget migration
public abstract class BaseSnackbar<B extends BaseSnackbar<B>> extends BaseTransientBottomBar<B> {

  /**
   * Show the Snackbar indefinitely. This means that the Snackbar will be displayed from the time
   * that is {@link #show() shown} until either it is dismissed, or another Snackbar is shown.
   *
   * @see #setDuration
   */
  public static final int LENGTH_INDEFINITE = BaseTransientBottomBar.LENGTH_INDEFINITE;

  /**
   * Show the Snackbar for a short period of time.
   *
   * @see #setDuration
   */
  public static final int LENGTH_SHORT = BaseTransientBottomBar.LENGTH_SHORT;

  /**
   * Show the Snackbar for a long period of time.
   *
   * @see #setDuration
   */
  public static final int LENGTH_LONG = BaseTransientBottomBar.LENGTH_LONG;

  @Nullable private BaseCallback<B> callback;

  protected BaseSnackbar(
      ViewGroup parent,
      View content,
      android.support.design.snackbar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
  }

  protected static SnackbarContentLayout makeSnackbarContentLayout(ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final boolean isUsingMaterialTheme = ThemeEnforcement.isMaterialTheme(parent.getContext());
    return (SnackbarContentLayout)
        inflater.inflate(
            isUsingMaterialTheme
                ? R.layout.design_layout_snackbar_include_material
                : R.layout.design_layout_snackbar_include,
            parent,
            false);
  }

  protected static ViewGroup findSuitableParent(View view) {
    ViewGroup fallback = null;
    do {
      if (view instanceof CoordinatorLayout) {
        // We've found a CoordinatorLayout, use it
        return (ViewGroup) view;
      } else if (view instanceof FrameLayout) {
        if (view.getId() == android.R.id.content) {
          // If we've hit the decor content view, then we didn't find a CoL in the
          // hierarchy, so use it.
          return (ViewGroup) view;
        } else {
          // It's not the content view but we'll use it as our fallback
          fallback = (ViewGroup) view;
        }
      }

      if (view != null) {
        // Else, we will loop and crawl up the view hierarchy and try to find a parent
        final ViewParent parent = view.getParent();
        view = parent instanceof View ? (View) parent : null;
      }
    } while (view != null);

    // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
    return fallback;
  }

  /**
   * Update the text in this {@link BaseSnackbar}.
   *
   * @param message The new text for this {@link BaseTransientBottomBar}.
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public B setText(@NonNull CharSequence message) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setText(message);
    return (B) this;
  }

  /**
   * Update the text in this {@link BaseSnackbar}.
   *
   * @param resId The new text for this {@link BaseTransientBottomBar}.
   */
  @NonNull
  public B setText(@StringRes int resId) {
    return setText(getContext().getText(resId));
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param resId String resource to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @NonNull
  public B setAction(@StringRes int resId, View.OnClickListener listener) {
    return setAction(getContext().getText(resId), listener);
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param text Text to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public B setAction(CharSequence text, final View.OnClickListener listener) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) this.view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();

    if (TextUtils.isEmpty(text) || listener == null) {
      tv.setVisibility(View.GONE);
      tv.setOnClickListener(null);
    } else {
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
      tv.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              listener.onClick(view);
              // Now dismiss the Snackbar
              dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
            }
          });
    }
    return (B) this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public B setActionTextColor(ColorStateList colors) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(colors);
    return (B) this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public B setActionTextColor(@ColorInt int color) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(color);
    return (B) this;
  }

  /**
   * Set a callback to be called when this the visibility of this {@link BaseSnackbar} changes. Note
   * that this method is deprecated and you should use {@link #addCallback(BaseCallback)} to add a
   * callback and {@link #removeCallback(BaseCallback)} to remove a registered callback.
   *
   * @param callback Callback to notify when transient bottom bar events occur.
   * @deprecated Use {@link #addCallback(BaseCallback)}
   * @see #addCallback(BaseCallback)
   * @see #removeCallback(BaseCallback)
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  @NonNull
  public B setCallback(BaseCallback<B> callback) {
    // The logic in this method emulates what we had before support for multiple
    // registered callbacks.
    if (this.callback != null) {
      removeCallback(this.callback);
    }
    if (callback != null) {
      addCallback(callback);
    }
    // Update the deprecated field so that we can remove the passed callback the next
    // time we're called
    this.callback = callback;
    return (B) this;
  }

  /**
   * @hide Note: this class is here to provide backwards-compatible way for apps written before the
   *     existence of the base {@link BaseTransientBottomBar} class.
   */
  @RestrictTo(LIBRARY_GROUP)
  public static class SnackbarLayout extends SnackbarBaseLayout {
    public SnackbarLayout(Context context) {
      super(context);
    }

    public SnackbarLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      // Work around our backwards-compatible refactoring of Snackbar and inner content
      // being inflated against snackbar's parent (instead of against the snackbar itself).
      // Every child that is width=MATCH_PARENT is remeasured again and given the full width
      // minus the paddings.
      int childCount = getChildCount();
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
          child.measure(
              MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
              MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
      }
    }
  }
}
