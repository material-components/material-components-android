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
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @deprecated This version of the FloatingActionButton is deprecated use {@link
 *     android.support.design.floatingactionbutton.FloatingActionButton} instead.
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
@Deprecated
public class FloatingActionButton
    extends android.support.design.floatingactionbutton.FloatingActionButton {

  /** @deprecated */
  @Deprecated
  public abstract static class OnVisibilityChangedListener
      extends android.support.design.floatingactionbutton.FloatingActionButton
          .OnVisibilityChangedListener {
    /**
     * Called when a FloatingActionButton has been {@link #show(OnVisibilityChangedListener) shown}.
     *
     * @param fab the FloatingActionButton that was shown.
     */
    public void onShown(FloatingActionButton fab) {}

    @Override
    public void onShown(android.support.design.floatingactionbutton.FloatingActionButton fab) {
      onShown((FloatingActionButton) fab);
    }

    /**
     * Called when a FloatingActionButton has been {@link #hide(OnVisibilityChangedListener)
     * hidden}.
     *
     * @param fab the FloatingActionButton that was hidden.
     */
    public void onHidden(FloatingActionButton fab) {}

    @Override
    public void onHidden(android.support.design.floatingactionbutton.FloatingActionButton fab) {
      onHidden((FloatingActionButton) fab);
    }
  }

  public FloatingActionButton(Context context) {
    super(context);
  }

  public FloatingActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void show(@Nullable OnVisibilityChangedListener onVisibilityChangedListener) {
    super.show(onVisibilityChangedListener);
  }

  public void hide(@Nullable OnVisibilityChangedListener onVisibilityChangedListener) {
    super.hide(onVisibilityChangedListener);
  }

  /** @deprecated */
  @Deprecated
  public static class Behavior
      extends android.support.design.floatingactionbutton.FloatingActionButton.Behavior<FloatingActionButton> {

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }
}
