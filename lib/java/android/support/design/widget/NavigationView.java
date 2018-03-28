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
import android.util.AttributeSet;

/**
 * @deprecated This version of the NavigationView is deprecated use {@link
 *     android.support.design.navigation.NavigationView} instead.
 */
@Deprecated
public class NavigationView extends android.support.design.navigation.NavigationView {

  public NavigationView(Context context) {
    super(context);
  }

  public NavigationView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /** @deprecated */
  @Deprecated
  public interface OnNavigationItemSelectedListener
      extends android.support.design.navigation.NavigationView.OnNavigationItemSelectedListener {}
}
