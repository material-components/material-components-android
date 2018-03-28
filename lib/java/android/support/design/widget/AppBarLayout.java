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

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @deprecated This version of the AppBarLayout is deprecated use {@link
 *     android.support.design.appbar.AppBarLayout} instead.
 */
@Deprecated
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior.class)
public class AppBarLayout extends android.support.design.appbar.AppBarLayout {

  /** @deprecated */
  @Deprecated
  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout>{
  }


  public AppBarLayout(Context context) {
    super(context);
  }

  public AppBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    super.addOnOffsetChangedListener(listener);
  }

  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    super.removeOnOffsetChangedListener(listener);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
      return new LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) p);
    }
    return new LayoutParams(p);
  }

  /** @deprecated */
  @Deprecated
  public static class Behavior
      extends android.support.design.appbar.AppBarLayout.Behavior<AppBarLayout> {
    /** @deprecated */
    @Deprecated
    public abstract static class DragCallback {
      public abstract boolean canDrag(@NonNull AppBarLayout appBarLayout);
    }

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public void setDragCallback(@Nullable final DragCallback callback) {
      setDragCallbackInternal(
          new android.support.design.appbar.AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(
                @NonNull android.support.design.appbar.AppBarLayout appBarLayout) {
              return callback.canDrag((AppBarLayout) appBarLayout);
            }
          });
    }
  }

  /** @deprecated */
  @Deprecated
  public static class ScrollingViewBehavior
      extends android.support.design.appbar.AppBarLayout.ScrollingViewBehavior {

    public ScrollingViewBehavior() {
      super();
    }

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  /** @deprecated */
  @Deprecated
  public static class LayoutParams extends android.support.design.appbar.AppBarLayout.LayoutParams {

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LinearLayout.LayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(android.support.design.appbar.AppBarLayout.LayoutParams source) {
      super(source);
    }
  }
}
