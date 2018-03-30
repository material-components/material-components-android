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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * Snackbars provide lightweight feedback about an operation. They show a brief message at the
 * bottom of the screen on mobile and lower left on larger devices. Snackbars appear above all other
 * elements on screen and only one can be displayed at a time.
 *
 * <p>They automatically disappear after a timeout or after user interaction elsewhere on the
 * screen, particularly after interactions that summon a new surface or activity. Snackbars can be
 * swiped off screen.
 *
 * <p>Snackbars can contain an action which is set via {@link #setAction(CharSequence,
 * android.view.View.OnClickListener)}.
 *
 * <p>To be notified when a snackbar has been shown or dismissed, you can provide a {@link Callback}
 * via {@link BaseTransientBottomBar#addCallback(BaseCallback)}.
 */
public final class Snackbar extends BaseSnackbar<Snackbar> {

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

    @Override
    public void onShown(Snackbar sb) {
      // Stub implementation to make API check happy.
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
      // Stub implementation to make API check happy.
    }
  }

  private Snackbar(
      ViewGroup parent,
      View content,
      android.support.design.snackbar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
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
   * @param view The view to find a parent from.
   * @param text The text to show. Can be formatted text.
   * @param duration How long to display the message. Either {@link #LENGTH_SHORT} or {@link
   *     #LENGTH_LONG}
   */
  @NonNull
  public static Snackbar make(
      @NonNull View view, @NonNull CharSequence text, @Duration int duration) {
    final ViewGroup parent = findSuitableParent(view);
    if (parent == null) {
      throw new IllegalArgumentException(
          "No suitable parent found from the given view. Please provide a valid view.");
    }

    final SnackbarContentLayout content = makeSnackbarContentLayout(parent);
    final Snackbar snackbar = new Snackbar(parent, content, content);
    snackbar.setText(text);
    snackbar.setDuration(duration);
    return snackbar;
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
   * @param duration How long to display the message. Either {@link #LENGTH_SHORT} or {@link
   *     #LENGTH_LONG}
   */
  @NonNull
  public static Snackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
    return make(view, view.getResources().getText(resId), duration);
  }

  // TODO: Delete the following passthrough methods once custom Robolectric shadows no longer depend
  // on them being present (and instead properly utilize super class hierarchy).
  @Override
  public void show() {
    super.show();
  }

  @Override
  public void dismiss() {
    super.dismiss();
  }

  @Override
  public boolean isShown() {
    return super.isShown();
  }

  @NonNull
  @Override
  public Snackbar setText(@NonNull CharSequence message) {
    return super.setText(message);
  }

  @NonNull
  @Override
  public Snackbar setText(int resId) {
    return super.setText(resId);
  }

  @NonNull
  @Override
  public Snackbar setAction(int resId, OnClickListener listener) {
    return super.setAction(resId, listener);
  }

  @NonNull
  @Override
  public Snackbar setAction(CharSequence text, OnClickListener listener) {
    return super.setAction(text, listener);
  }

  @NonNull
  @Override
  public Snackbar setActionTextColor(int color) {
    return super.setActionTextColor(color);
  }

  @NonNull
  @Override
  public Snackbar setActionTextColor(ColorStateList colors) {
    return super.setActionTextColor(colors);
  }

  @Deprecated
  @NonNull
  public Snackbar setCallback(Callback callback) {
    return super.setCallback(callback);
  }

  /**
   * @hide Note: this class is here to provide backwards-compatible way for apps written before the
   *     existence of the base {@link BaseTransientBottomBar} class.
   */
  @RestrictTo(LIBRARY_GROUP)
  public static final class SnackbarLayout extends BaseSnackbar.SnackbarLayout {
    public SnackbarLayout(Context context) {
      super(context);
    }

    public SnackbarLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }
}
