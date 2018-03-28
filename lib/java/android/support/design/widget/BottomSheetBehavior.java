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

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated This version of the BottomSheetBehavior is deprecated use {@link
 *     android.support.design.bottomsheet.BottomSheetBehavior} instead.
 */
@Deprecated
public class BottomSheetBehavior<V extends View>
    extends android.support.design.bottomsheet.BottomSheetBehavior<V> {

  /**
   * This is deprecated. Use {@link android.support.design.bottomsheet.BottomSheetBehavior.State}
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
      STATE_EXPANDED,
      STATE_COLLAPSED,
      STATE_DRAGGING,
      STATE_SETTLING,
      STATE_HIDDEN,
      STATE_HALF_EXPANDED
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {}

  public BottomSheetBehavior() {
    super();
  }

  public BottomSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setBottomSheetCallback(@Nullable BottomSheetCallback bottomSheetCallback) {
    super.setBottomSheetCallback(bottomSheetCallback);
  }

  /** @deprecated */
  @Deprecated
  public abstract static class BottomSheetCallback
      extends android.support.design.bottomsheet.BottomSheetBehavior.BottomSheetCallback {}

  public static <V extends View> BottomSheetBehavior<V> from(V view) {
    return (BottomSheetBehavior<V>)
        android.support.design.bottomsheet.BottomSheetBehavior.from(view);
  }
}
