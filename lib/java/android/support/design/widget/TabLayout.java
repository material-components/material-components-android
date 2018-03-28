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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * @deprecated This version of the TabLayout is deprecated use {@link
 *     android.support.design.tabs.TabLayout} instead.
 */
@Deprecated
@ViewPager.DecorView
public class TabLayout extends android.support.design.tabs.TabLayout {

  private static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool<>(16);

  public TabLayout(Context context) {
    super(context);
  }

  public TabLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /** @deprecated */
  @Deprecated
  public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {

  }

  @Override
  public Tab newTab() {
    return (Tab) super.newTab();
  }

  @Override
  public Tab getTabAt(int index) {
    return (Tab) super.getTabAt(index);
  }

  @Override
  protected Tab createTabFromPool() {
    Tab tab = tabPool.acquire();
    if (tab == null) {
      tab = new Tab();
    }
    return tab;
  }

  @Override
  protected boolean releaseFromTabPool(android.support.design.tabs.TabLayout.Tab tab) {
    return tabPool.release((Tab) tab);
  }

  /** @deprecated */
  @Deprecated
  public static final class Tab extends android.support.design.tabs.TabLayout.Tab {

    @NonNull
    public Tab setTag(@Nullable Object tag) {
      super.setTag(tag);
      return this;
    }

    @NonNull
    public Tab setCustomView(@Nullable View view) {
      super.setCustomView(view);
      return this;
    }

    @NonNull
    public Tab setCustomView(@LayoutRes int resId) {
      super.setCustomView(resId);
      return this;
    }

    @NonNull
    public Tab setIcon(@Nullable Drawable icon) {
      super.setIcon(icon);
      return this;
    }

    @NonNull
    public Tab setIcon(@DrawableRes int resId) {
      super.setIcon(resId);
      return this;
    }

    @NonNull
    public Tab setText(@Nullable CharSequence text) {
      super.setText(text);
      return this;
    }

    @NonNull
    public Tab setText(@StringRes int resId) {
      super.setText(resId);
      return this;
    }

    @NonNull
    public Tab setContentDescription(@StringRes int resId) {
      super.setContentDescription(resId);
      return this;
    }

    @NonNull
    public Tab setContentDescription(@Nullable CharSequence contentDesc) {
      super.setContentDescription(contentDesc);
      return this;
    }
  }

  /** @deprecated */
  @Deprecated
  public static class TabLayoutOnPageChangeListener
      extends android.support.design.tabs.TabLayout.TabLayoutOnPageChangeListener {

    public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
      super(tabLayout);
    }
  }

  /** @deprecated */
  @Deprecated
  public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
    private final ViewPager viewPager;

    public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
      this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(Tab tab) {
      viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab) {
      // No-op
    }

    @Override
    public void onTabReselected(Tab tab) {
      // No-op
    }
  }

  // Below are methods for curvular since they have to be methods set on this class

  @Override
  public void setTabGravity(int gravity) {
    super.setTabGravity(gravity);
  }

  @Override
  public void setTabMode(int mode) {
    super.setTabMode(mode);
  }

  @Override
  public void setSelectedTabIndicatorColor(int color) {
    super.setSelectedTabIndicatorColor(color);
  }

  @Override
  public void setSelectedTabIndicatorHeight(int height) {
    super.setSelectedTabIndicatorHeight(height);
  }

  @Override
  public void setTabTextColors(@Nullable ColorStateList textColor) {
    super.setTabTextColors(textColor);
  }
}
