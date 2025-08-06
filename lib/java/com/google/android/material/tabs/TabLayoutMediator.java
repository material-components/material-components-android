/*
 * Copyright 2018 The Android Open Source Project
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

package com.google.android.material.tabs;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import java.lang.ref.WeakReference;

/**
 * A mediator to link a TabLayout with a ViewPager2. The mediator will synchronize the ViewPager2's
 * position with the selected tab when a tab is selected, and the TabLayout's scroll position when
 * the user drags the ViewPager2. TabLayoutMediator will listen to ViewPager2's OnPageChangeCallback
 * to adjust tab when ViewPager2 moves. TabLayoutMediator listens to TabLayout's
 * OnTabSelectedListener to adjust VP2 when tab moves. TabLayoutMediator listens to RecyclerView's
 * AdapterDataObserver to recreate tab content when dataset changes.
 *
 * <p>Establish the link by creating an instance of this class, make sure the ViewPager2 has an
 * adapter and then call {@link #attach()} on it. Instantiating a TabLayoutMediator will only create
 * the mediator object, {@link #attach()} will link the TabLayout and the ViewPager2 together. When
 * creating an instance of this class, you must supply an implementation of {@link
 * TabConfigurationStrategy} in which you set the text of the tab, and/or perform any styling of the
 * tabs that you require. Changing ViewPager2's adapter will require a {@link #detach()} followed by
 * {@link #attach()} call. Changing the ViewPager2 or TabLayout will require a new instantiation of
 * TabLayoutMediator.
 */
public final class TabLayoutMediator {
  @NonNull private final TabLayout tabLayout;
  @NonNull private final ViewPager2 viewPager;
  private final boolean autoRefresh;
  private final boolean smoothScroll;
  private final TabConfigurationStrategy tabConfigurationStrategy;
  @Nullable private RecyclerView.Adapter<?> adapter;
  private boolean attached;

  @Nullable private TabLayoutOnPageChangeCallback onPageChangeCallback;
  @Nullable private TabLayout.OnTabSelectedListener onTabSelectedListener;
  @Nullable private RecyclerView.AdapterDataObserver pagerAdapterObserver;

  /**
   * A callback interface that must be implemented to set the text and styling of newly created
   * tabs.
   */
  public interface TabConfigurationStrategy {
    /**
     * Called to configure the tab for the page at the specified position. Typically calls {@link
     * TabLayout.Tab#setText(CharSequence)}, but any form of styling can be applied.
     *
     * @param tab The Tab which should be configured to represent the title of the item at the given
     *     position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    void onConfigureTab(@NonNull TabLayout.Tab tab, int position);
  }

  public TabLayoutMediator(
      @NonNull TabLayout tabLayout,
      @NonNull ViewPager2 viewPager,
      @NonNull TabConfigurationStrategy tabConfigurationStrategy) {
    this(tabLayout, viewPager, /* autoRefresh= */ true, tabConfigurationStrategy);
  }

  public TabLayoutMediator(
      @NonNull TabLayout tabLayout,
      @NonNull ViewPager2 viewPager,
      boolean autoRefresh,
      @NonNull TabConfigurationStrategy tabConfigurationStrategy) {
    this(tabLayout, viewPager, autoRefresh, /* smoothScroll= */ true, tabConfigurationStrategy);
  }

  public TabLayoutMediator(
      @NonNull TabLayout tabLayout,
      @NonNull ViewPager2 viewPager,
      boolean autoRefresh,
      boolean smoothScroll,
      @NonNull TabConfigurationStrategy tabConfigurationStrategy) {
    this.tabLayout = tabLayout;
    this.viewPager = viewPager;
    this.autoRefresh = autoRefresh;
    this.smoothScroll = smoothScroll;
    this.tabConfigurationStrategy = tabConfigurationStrategy;
  }

  /**
   * Link the TabLayout and the ViewPager2 together. Must be called after ViewPager2 has an adapter
   * set. To be called on a new instance of TabLayoutMediator or if the ViewPager2's adapter
   * changes.
   *
   * @throws IllegalStateException If the mediator is already attached, or the ViewPager2 has no
   *     adapter.
   */
  public void attach() {
    if (attached) {
      throw new IllegalStateException("TabLayoutMediator is already attached");
    }
    adapter = viewPager.getAdapter();
    if (adapter == null) {
      throw new IllegalStateException(
          "TabLayoutMediator attached before ViewPager2 has an " + "adapter");
    }
    attached = true;

    // Add our custom OnPageChangeCallback to the ViewPager
    onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
    viewPager.registerOnPageChangeCallback(onPageChangeCallback);

    // Now we'll add a tab selected listener to set ViewPager's current item
    onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager, smoothScroll);
    tabLayout.addOnTabSelectedListener(onTabSelectedListener);

    // Now we'll populate ourselves from the pager adapter, adding an observer if
    // autoRefresh is enabled
    if (autoRefresh) {
      // Register our observer on the new adapter
      pagerAdapterObserver = new PagerAdapterObserver();
      adapter.registerAdapterDataObserver(pagerAdapterObserver);
    }

    populateTabsFromPagerAdapter();

    // Now update the scroll position to match the ViewPager's current item
    tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
  }

  /**
   * Unlink the TabLayout and the ViewPager. To be called on a stale TabLayoutMediator if a new one
   * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
   * called before {@link #attach()} when a ViewPager2's adapter is changed.
   */
  public void detach() {
    if (!attached) {
      return;
    }

    if (autoRefresh && adapter != null) {
      adapter.unregisterAdapterDataObserver(pagerAdapterObserver);
      pagerAdapterObserver = null;
    }
    tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
    onTabSelectedListener = null;
    onPageChangeCallback = null;
    adapter = null;
    attached = false;
  }

  /**
   * Returns whether the {@link TabLayout} and the {@link ViewPager2} are linked together.
   */
  public boolean isAttached() {
    return attached;
  }

  @SuppressWarnings("WeakerAccess")
  void populateTabsFromPagerAdapter() {
    tabLayout.removeAllTabs();

    if (adapter != null) {
      int adapterCount = adapter.getItemCount();
      for (int i = 0; i < adapterCount; i++) {
        TabLayout.Tab tab = tabLayout.newTab();
        tabConfigurationStrategy.onConfigureTab(tab, i);
        tabLayout.addTab(tab, false);
      }
      // Make sure we reflect the currently set ViewPager item
      if (adapterCount > 0) {
        int lastItem = tabLayout.getTabCount() - 1;
        int currItem = Math.min(viewPager.getCurrentItem(), lastItem);
        if (currItem != tabLayout.getSelectedTabPosition()) {
          tabLayout.selectTab(tabLayout.getTabAt(currItem));
        }
      }
    }
  }

  /**
   * A {@link ViewPager2.OnPageChangeCallback} class which contains the necessary calls back to the
   * provided {@link TabLayout} so that the tab position is kept in sync.
   *
   * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
   * ViewPager2#registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback)} without removing the
   * callback and not cause a leak.
   */
  private static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
    @NonNull private final WeakReference<TabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    TabLayoutOnPageChangeCallback(TabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
      reset();
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
      previousScrollState = scrollState;
      scrollState = state;
      TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        tabLayout.updateViewPagerScrollState(scrollState);
      }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        // Only update the tab view selection if we're not settling, or we are settling after
        // being dragged
        boolean updateSelectedTabView =
            scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
        // Update the indicator if we're not settling after being idle. This is caused
        // from a setCurrentItem() call and will be handled by an animation from
        // onPageSelected() instead.
        boolean updateIndicator =
            !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(
            position, positionOffset, updateSelectedTabView, updateIndicator, false);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        boolean updateIndicator =
            scrollState == SCROLL_STATE_IDLE
                || (scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
  }

  /**
   * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
   * provided {@link ViewPager2} so that the tab position is kept in sync.
   */
  private static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private final ViewPager2 viewPager;
    private final boolean smoothScroll;

    ViewPagerOnTabSelectedListener(ViewPager2 viewPager, boolean smoothScroll) {
      this.viewPager = viewPager;
      this.smoothScroll = smoothScroll;
    }

    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
      viewPager.setCurrentItem(tab.getPosition(), smoothScroll);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
      // No-op
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
      // No-op
    }
  }

  private class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      populateTabsFromPagerAdapter();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      populateTabsFromPagerAdapter();
    }
  }
}
