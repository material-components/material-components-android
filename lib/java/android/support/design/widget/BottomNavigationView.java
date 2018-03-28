/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @deprecated This version of the BottomNavigationView is deprecated use {@link
 *     android.support.design.bottomnavigation.BottomNavigationView} instead.
 */
@Deprecated
public class BottomNavigationView
    extends android.support.design.bottomnavigation.BottomNavigationView {

  public BottomNavigationView(Context context) {
    super(context);
  }

  public BottomNavigationView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * @deprecated Use {@link
   *     android.support.design.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener}
   *     instead.
   */
  @Deprecated
  public interface OnNavigationItemSelectedListener
      extends android.support.design.bottomnavigation.BottomNavigationView
          .OnNavigationItemSelectedListener {}

  /**
   * @deprecated Use {@link
   *     android.support.design.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener}
   *     instead.
   */
  @Deprecated
  public interface OnNavigationItemReselectedListener
      extends android.support.design.bottomnavigation.BottomNavigationView
          .OnNavigationItemReselectedListener {}

  // Below are methods for curvular since they have to be methods set on this class

  @Override
  public void setSelectedItemId(int itemId) {
    super.setSelectedItemId(itemId);
  }

  @Override
  public void setLabelVisibilityMode(int labelVisibilityMode) {
    super.setLabelVisibilityMode(labelVisibilityMode);
  }

  @Override
  public void setItemBackgroundResource(int resId) {
    super.setItemBackgroundResource(resId);
  }

  @Override
  public void setOnNavigationItemSelectedListener(
      @Nullable
          android.support.design.bottomnavigation.BottomNavigationView
                  .OnNavigationItemSelectedListener
              listener) {
    super.setOnNavigationItemSelectedListener(listener);
  }

  @Override
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    super.setItemTextColor(textColor);
  }

  @Override
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    super.setItemIconTintList(tint);
  }
}
