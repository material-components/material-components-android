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

package com.google.android.material.snackbar;

import com.google.android.material.R;

import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_ICONS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.button.MaterialButton;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Snackbars provide lightweight feedback about an operation. They show a brief message at the
 * bottom of the screen on mobile and lower left on larger devices. Snackbars appear above all other
 * elements on screen and only one can be displayed at a time.
 *
 * <p>Snackbars can contain an action which is set via {@link #setAction(CharSequence,
 * android.view.View.OnClickListener)}.
 *
 * <p>Snackbars automatically disappear after a timeout. They can also be dismissed by being swiped
 * off screen, by action click, from a new snackbar being displayed, or manually via a call to
 * {@link #dismiss()}.
 *
 * <p>To be notified when a snackbar has been shown or dismissed, you can provide a {@link Callback}
 * via {@link BaseTransientBottomBar#addCallback(BaseCallback)}.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Snackbar.md">component
 * developer guidance</a> and <a href="https://material.io/components/snackbar/overview">design
 * guidelines</a>.
 */
public class Snackbar extends BaseTransientBottomBar<Snackbar> {

  @Nullable private final AccessibilityManager accessibilityManager;
  private boolean hasAction;

  private static final int[] SNACKBAR_BUTTON_STYLE_ATTR = new int[] {R.attr.snackbarButtonStyle};
  private static final int[] SNACKBAR_CONTENT_STYLE_ATTRS =
      new int[] {R.attr.snackbarButtonStyle, R.attr.snackbarTextViewStyle};

  /**
   * Callback class for {@link Snackbar} instances.
   *
   * <p>Note: this class is here to provide backwards-compatible way for apps written before the
   * existence of the base {@link BaseTransientBottomBar} class.
   *
   * @see BaseTransientBottomBar#addCallback(BaseCallback)
   */
  public static class Callback extends BaseCallback<Snackbar> {
    /** Indicates that the Snackbar was dismissed via a swipe. */
    public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;

    /** Indicates that the Snackbar was dismissed via an action click. */
    public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;

    /** Indicates that the Snackbar was dismissed via a timeout. */
    public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;

    /** Indicates that the Snackbar was dismissed via a call to {@link #dismiss()}. */
    public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;

    /** Indicates that the Snackbar was dismissed from a new Snackbar being shown. */
    public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

    /**
     * Called when the given {@link Snackbar} is visible.
     *
     * @param sb The snackbar which is now visible.
     * @see Snackbar#show()
     */
    @Override
    public void onShown(Snackbar sb) {
      // Stub implementation to make API check happy.
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
      // Stub implementation to make API check happy.
    }
  }

  @Nullable private BaseCallback<Snackbar> callback;

  private Snackbar(
      @NonNull Context context,
      @NonNull ViewGroup parent,
      @NonNull View content,
      @NonNull com.google.android.material.snackbar.ContentViewCallback contentViewCallback) {
    super(context, parent, content, contentViewCallback);
    accessibilityManager =
        (AccessibilityManager) parent.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public void show() {
    super.show();
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public void dismiss() {
    super.dismiss();
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public boolean isShown() {
    return super.isShown();
  }

  /**
   * Make a Snackbar to display a message
   *
   * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
   * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
   * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
   * first.
   *
   * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
   * features, such as swipe-to-dismiss and automatically moving of widgets.
   *
   * @param view The view to find a parent from. This view is also used to find the anchor view when
   *     calling {@link Snackbar#setAnchorView(int)}.
   * @param text The text to show. Can be formatted text.
   * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
   *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
   */
  @NonNull
  public static Snackbar make(
      @NonNull View view, @NonNull CharSequence text, @Duration int duration) {
    return makeInternal(/* context= */ null, view, text, duration);
  }

  /**
   * Make a Snackbar to display a message.
   *
   * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
   * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
   * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
   * first.
   *
   * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
   * features, such as swipe-to-dismiss and automatically moving of widgets.
   *
   * @param view The view to find a parent from.
   * @param resId The resource id of the string resource to use. Can be formatted text.
   * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
   *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
   */
  @NonNull
  public static Snackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
    return make(view, view.getResources().getText(resId), duration);
  }

  /**
   * Make a Snackbar to display a message
   *
   * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
   * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
   * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
   * first.
   *
   * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
   * features, such as swipe-to-dismiss and automatically moving of widgets.
   *
   * @param context The context to use to create the Snackbar view.
   * @param view The view to find a parent from. This view is also used to find the anchor view when
   *     calling {@link Snackbar#setAnchorView(int)}.
   * @param text The text to show. Can be formatted text.
   * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
   *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
   */
  @NonNull
  public static Snackbar make(
      @NonNull Context context,
      @NonNull View view,
      @NonNull CharSequence text,
      @Duration int duration) {
    return makeInternal(context, view, text, duration);
  }

  /**
   * Makes a Snackbar using the given context if non-null, otherwise uses the parent view context.
   */
  @NonNull
  private static Snackbar makeInternal(
      @Nullable Context context,
      @NonNull View view,
      @NonNull CharSequence text,
      @Duration int duration) {
    final ViewGroup parent = findSuitableParent(view);
    if (parent == null) {
      throw new IllegalArgumentException(
          "No suitable parent found from the given view. Please provide a valid view.");
    }

    if (context == null) {
      context = parent.getContext();
    }

    final LayoutInflater inflater = LayoutInflater.from(context);
    final SnackbarContentLayout content =
        (SnackbarContentLayout)
            inflater.inflate(
                hasSnackbarContentStyleAttrs(context)
                    ? R.layout.mtrl_layout_snackbar_include
                    : R.layout.design_layout_snackbar_include,
                parent,
                false);
    final Snackbar snackbar = new Snackbar(context, parent, content, content);
    snackbar.setText(text);
    snackbar.setDuration(duration);
    return snackbar;
  }

  /**
   * {@link Snackbar}s should still work with AppCompat themes, which don't specify a {@code
   * snackbarButtonStyle}. This method helps to check if a valid {@code snackbarButtonStyle} is set
   * within the current context, so that we know whether we can use the attribute.
   *
   * @deprecated This is for backward compatibility with AppCompat themes.
   */
  @Deprecated
  protected static boolean hasSnackbarButtonStyleAttr(@NonNull Context context) {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_BUTTON_STYLE_ATTR);
    int snackbarButtonStyleResId = a.getResourceId(0, -1);
    a.recycle();
    return snackbarButtonStyleResId != -1;
  }

  private static boolean hasSnackbarContentStyleAttrs(@NonNull Context context) {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_CONTENT_STYLE_ATTRS);
    int snackbarButtonStyleResId = a.getResourceId(0, -1);
    int snackbarTextViewStyleResId = a.getResourceId(1, -1);
    a.recycle();
    return snackbarButtonStyleResId != -1 && snackbarTextViewStyleResId != -1;
  }

  @Nullable
  private static ViewGroup findSuitableParent(View view) {
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
   * Update the text in this {@link Snackbar}.
   *
   * @param message The new text for this {@link BaseTransientBottomBar}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setText(@NonNull CharSequence message) {
    getMessageView().setText(message);
    return this;
  }

  /**
   * Update the text in this {@link Snackbar}.
   *
   * @param resId The new text for this {@link BaseTransientBottomBar}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setText(@StringRes int resId) {
    return setText(getContext().getText(resId));
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param resId String resource to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
    return setAction(getContext().getText(resId), listener);
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param text Text to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setAction(
      @Nullable CharSequence text, @Nullable final View.OnClickListener listener) {
    final TextView tv = getActionView();
    if (TextUtils.isEmpty(text) || listener == null) {
      tv.setVisibility(View.GONE);
      tv.setOnClickListener(null);
      hasAction = false;
    } else {
      hasAction = true;
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
      tv.setOnClickListener(
          view -> {
            listener.onClick(view);
            // Now dismiss the Snackbar
            dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
          });
    }
    return this;
  }

  /**
   * Show an optional close icon at the end of the snackbar.
   *
   * <p>The close icon is set to {@code View.GONE} by default. Setting the icon to visible will show
   * the icon at the end of the layout and automatically dismiss the snackbar when clicked. This
   * method and other related closeIcon setters can only be used within the context of a Material 3
   * or greater theme.
   *
   * <p>When using a close icon, please consider setting the snackbar's duration to {@code
   * Snackbar.LENGTH_INDEFINITE}.
   *
   * <p>This method will automatically remove any end padding from the snackbar layout to properly
   * align the close icon button. To manually set end padding, use {@link Snackbar#getView()} and
   * call {@link View#setPaddingRelative(int, int, int, int)} before showing the snackbar.
   *
   * @param visible true to make the close icon visible
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCloseIconVisible(boolean visible) {
    final Button closeButton = getCloseViewOrThrow();

    int newVisibility = visible ? View.VISIBLE : View.GONE;
    closeButton.setVisibility(newVisibility);
    closeButton.setOnClickListener(visible ? v -> dismiss() : null);
    getSnackbarLayout().removeOrRestorePaddingEnd(/* remove= */ visible);
    return this;
  }

  /**
   * Set the tint color of the optional close icon.
   *
   * @param tint the tint color to be used for the close icon
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCloseIconTint(@ColorInt int tint) {
    return setCloseIconTintList(ColorStateList.valueOf(tint));
  }

  /**
   * Set the tint list of the optional close icon.
   *
   * @param tintList the tint list to be used for the close icon
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCloseIconTintList(@Nullable ColorStateList tintList) {
    getCloseViewOrThrow().setIconTint(tintList);
    return this;
  }

  /**
   * Set the drawable used for the optional close icon.
   *
   * @param icon the drawable for the close icon
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCloseIconDrawable(@Nullable Drawable icon) {
    getCloseViewOrThrow().setIcon(icon);
    return this;
  }

  /**
   * Set the drawable resource used for the optional close icon.
   *
   * @param iconResourceId the drawable resource id for the close icon
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCloseIconResource(@DrawableRes int iconResourceId) {
    getCloseViewOrThrow().setIconResource(iconResourceId);
    return this;
  }

  @Override
  @Duration
  public int getDuration() {
    int userSetDuration = super.getDuration();
    if (userSetDuration == LENGTH_INDEFINITE) {
      return LENGTH_INDEFINITE;
    }

    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      int controlsFlag = hasAction ? FLAG_CONTENT_CONTROLS : 0;
      return accessibilityManager.getRecommendedTimeoutMillis(
          userSetDuration, controlsFlag | FLAG_CONTENT_ICONS | FLAG_CONTENT_TEXT);
    }

    // If touch exploration is enabled override duration to give people chance to interact.
    return hasAction && accessibilityManager.isTouchExplorationEnabled()
        ? LENGTH_INDEFINITE
        : userSetDuration;
  }

  /**
   * Sets the text color of the message specified in {@link #setText(CharSequence)} and {@link
   * #setText(int)}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setTextColor(ColorStateList colors) {
    getMessageView().setTextColor(colors);
    return this;
  }

  /**
   * Sets the text color of the message specified in {@link #setText(CharSequence)} and {@link
   * #setText(int)}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setTextColor(@ColorInt int color) {
    getMessageView().setTextColor(color);
    return this;
  }

  /**
   * Sets the max line count of the message specified in {@link #setText(CharSequence)} and {@link
   * #setText(int)}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setTextMaxLines(int maxLines) {
    getMessageView().setMaxLines(maxLines);
    return this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setActionTextColor(ColorStateList colors) {
    getActionView().setTextColor(colors);
    return this;
  }

  /**
   * Sets the max width of the action to be in the same line as the message. If the width is
   * exceeded the action would go to the next line.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setMaxInlineActionWidth(@Dimension int width) {
    getContentLayout().setMaxInlineActionWidth(width);
    return this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setActionTextColor(@ColorInt int color) {
    getActionView().setTextColor(color);
    return this;
  }

  /** Sets the tint color of the background Drawable. */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setBackgroundTint(@ColorInt int color) {
    return setBackgroundTintList(ColorStateList.valueOf(color));
  }

  /** Sets the tint color state list of the background Drawable. */
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setBackgroundTintList(@Nullable ColorStateList colorStateList) {
    view.setBackgroundTintList(colorStateList);
    return this;
  }

  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
    view.setBackgroundTintMode(mode);
    return this;
  }

  /**
   * Set a callback to be called when this the visibility of this {@link Snackbar} changes. Note
   * that this method is deprecated and you should use {@link #addCallback(BaseCallback)} to add a
   * callback and {@link #removeCallback(BaseCallback)} to remove a registered callback.
   *
   * @param callback Callback to notify when transient bottom bar events occur.
   * @deprecated Use {@link #addCallback(BaseCallback)}
   * @see Callback
   * @see #addCallback(BaseCallback)
   * @see #removeCallback(BaseCallback)
   */
  @Deprecated
  @NonNull
  @CanIgnoreReturnValue
  public Snackbar setCallback(@Nullable Callback callback) {
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
    return this;
  }

  /**
   * @hide Note: this class is here to provide backwards-compatible way for apps written before the
   *     existence of the base {@link BaseTransientBottomBar} class.
   */
  @RestrictTo(LIBRARY_GROUP)
  public static final class SnackbarLayout extends BaseTransientBottomBar.SnackbarBaseLayout {
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

  private TextView getMessageView() {
    return getContentLayout().getMessageView();
  }

  private Button getActionView() {
    return getContentLayout().getActionView();
  }

  private SnackbarContentLayout getContentLayout() {
    return (SnackbarContentLayout) view.getChildAt(0);
  }

  @NonNull
  private MaterialButton getCloseViewOrThrow() {
    if (!(getContentLayout().getCloseView() instanceof MaterialButton)) {
      throw new IllegalStateException(
          "The layout of this snackbar does not include a close MaterialButton. This might be"
              + " because the context's theme is not a Material 3 or later theme or because the"
              + " Snackbar's layout has been replaced with a custom layout.");
    }
    return (MaterialButton) getContentLayout().getCloseView();
  }

  private SnackbarBaseLayout getSnackbarLayout() {
    return view;
  }
}
