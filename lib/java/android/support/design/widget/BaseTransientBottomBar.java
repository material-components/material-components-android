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

package android.support.design.widget;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated This version of the BaseTransientBottomBar is deprecated use {@link
 *     android.support.design.snackbar.BaseTransientBottomBar} instead.
 */
@Deprecated
public abstract class BaseTransientBottomBar<B extends BaseTransientBottomBar<B>>
    extends android.support.design.snackbar.BaseTransientBottomBar<B> {
  /** @deprecated */
  @Deprecated
  public abstract static class BaseCallback<B>
      extends android.support.design.snackbar.BaseTransientBottomBar.BaseCallback<B> {
    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({
      DISMISS_EVENT_SWIPE,
      DISMISS_EVENT_ACTION,
      DISMISS_EVENT_TIMEOUT,
      DISMISS_EVENT_MANUAL,
      DISMISS_EVENT_CONSECUTIVE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DismissEvent {}
  }
  /**
   * Interface that defines the behavior of the main content of a transient bottom bar.
   *
   * @deprecated Use {@link android.support.design.snackbar.ContentViewCallback} instead.
   */
  @Deprecated
  public interface ContentViewCallback
      extends android.support.design.snackbar.ContentViewCallback {}

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
  @IntRange(from = 1)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Duration {}

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  interface OnLayoutChangeListener
      extends android.support.design.snackbar.BaseTransientBottomBar.OnLayoutChangeListener {}

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  interface OnAttachStateChangeListener
      extends android.support.design.snackbar.BaseTransientBottomBar.OnAttachStateChangeListener {}

  /**
   * Constructor for the transient bottom bar.
   *
   * @param parent The parent for this transient bottom bar.
   * @param content The content view for this transient bottom bar.
   * @param contentViewCallback The content view callback for this transient bottom bar.
   */
  protected BaseTransientBottomBar(
      @NonNull ViewGroup parent,
      @NonNull View content,
      @NonNull android.support.design.snackbar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
  }

  final class Behavior extends android.support.design.snackbar.BaseTransientBottomBar<B>.Behavior {}
}
