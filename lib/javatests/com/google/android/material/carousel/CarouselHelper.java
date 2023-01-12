/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.carousel;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.CountDownLatch;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

/** A helper class to facilitate Carousel tests */
class CarouselHelper {

  private CarouselHelper() {}

  /**
   * Explicitly set a view's size.
   *
   * @param view the view to assign the size to
   * @param width the desired width of the view
   * @param height the desired height of the view
   */
  static void setViewSize(View view, int width, int height) {
    view.setLayoutParams(new LayoutParams(width, height));
    view.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  /**
   * Creates a list of {@link TestItem}s with length of {@code size} to be used as an adapter's data
   * set.
   */
  static ImmutableList<TestItem> createDataSetWithSize(int size) {
    ImmutableList.Builder<TestItem> builder = ImmutableList.builder();
    for (int i = 0; i < size; i++) {
      builder.add(new TestItem());
    }

    return builder.build();
  }

  /**
   * Handles scrolling the recycler view to an adapter position and waiting until the recycler view
   * has made a layout pass.
   */
  static void scrollToPosition(
      RecyclerView recyclerView, WrappedCarouselLayoutManager layoutManager, int pos)
      throws Throwable {
    layoutManager.expectLayouts(1);
    layoutManager.scrollToPosition(pos);
    // Ping the recycler view to do a measure and layout.
    recyclerView.measure(
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredWidth(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredHeight(), MeasureSpec.EXACTLY));
    // Force the recycler view to do a measure and layout.
    recyclerView.layout(0, 0, recyclerView.getMeasuredWidth(), recyclerView.getMeasuredHeight());
    layoutManager.waitForLayout(3L);
  }

  /**
   * Handles setting the items of the adapter and waiting until the recycler view has made a layout
   * pass.
   */
  static void setAdapterItems(
      RecyclerView recyclerView,
      WrappedCarouselLayoutManager layoutManager,
      CarouselTestAdapter adapter,
      ImmutableList<TestItem> items)
      throws Throwable {
    layoutManager.expectLayouts(1);
    adapter.setItems(items);
    // Ping the recycler view to do a measure and layout.
    recyclerView.measure(
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredWidth(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredHeight(), MeasureSpec.EXACTLY));
    recyclerView.layout(0, 0, recyclerView.getMeasuredWidth(), recyclerView.getMeasuredHeight());
    layoutManager.waitForLayout(3L);
  }

  /** Creates a {@link Carousel} with a specified {@code width}. */
  static Carousel createCarouselWithWidth(int width) {
    return new Carousel() {
      @Override
      public int getContainerWidth() {
        return width;
      }

      @Override
      public int getContainerPaddingStart() {
        return 0;
      }

      @Override
      public int getContainerPaddingTop() {
        return 0;
      }

      @Override
      public int getContainerPaddingEnd() {
        return 0;
      }

      @Override
      public int getContainerPaddingBottom() {
        return 0;
      }
    };
  }

  /**
   * Gets the percentage of an item's {@code unmaskedSize} that should be masked away when at a
   * keyline.
   *
   * <p>The larger the mask percentage, the smaller the size of the item when masked. If {@code
   * maskedSize} is 10 and {@code unmaskedSize} is 100, this will return a mask of .9. 90% of the
   * view should be masked.
   *
   * @param maskedSize The size of the item when masked.
   * @param unmaskedSize The size of an item when no mask is applied or is fully unmasked.
   * @return a percentage of the item's unmasked size that should be masked to create an item with a
   *     size of {@code maskedSize}
   */
  static float getKeylineMaskPercentage(float maskedSize, float unmaskedSize) {
    return 1F - (maskedSize / unmaskedSize);
  }

  /** An empty data class used to represent items in a list */
  static class TestItem {
    public TestItem() {}
  }

  /** A ViewHolder for {@link TestItem} */
  static class TestItemViewHolder extends RecyclerView.ViewHolder {

    TestItemViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  /** An adapter to be used for facilitating tests that use a RecyclerView. */
  static class CarouselTestAdapter extends RecyclerView.Adapter<TestItemViewHolder> {

    private final int itemWidth;
    private final int itemHeight;
    private ImmutableList<TestItem> items = ImmutableList.of();

    /**
     * Creates a {@link CarouselTestAdapter}.
     *
     * @param itemWidth the width each item in the adapter would like to be.
     * @param itemHeight the height each item in the adapter would like to be.
     */
    CarouselTestAdapter(int itemWidth, int itemHeight) {
      this.itemWidth = itemWidth;
      this.itemHeight = itemHeight;
    }

    /** Sets the items in this adapter. */
    void setItems(ImmutableList<TestItem> items) {
      this.items = items;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
      MaskableFrameLayout frameLayout = new MaskableFrameLayout(viewGroup.getContext());
      setViewSize(frameLayout, itemWidth, itemHeight);
      ImageView imageView = new ImageView(viewGroup.getContext());
      setViewSize(imageView, itemWidth, itemHeight);
      imageView.setImageDrawable(new ColorDrawable(Color.MAGENTA));
      frameLayout.addView(imageView);
      return new TestItemViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull TestItemViewHolder vh, int pos) {}

    @Override
    public int getItemCount() {
      return items.size();
    }
  }

  /** A wrapper around {@link CarouselLayoutManager} that is able to wait for layouts to happen. */
  static class WrappedCarouselLayoutManager extends CarouselLayoutManager {

    WrappedCarouselLayoutManager() {}

    CountDownLatch layoutLatch;

    /**
     * Sets up a new {@link CountDownLatch} that will wait for a specified number of events to
     * occur.
     *
     * @param count the number of events this latch should wait for.
     */
    void expectLayouts(int count) {
      layoutLatch = new CountDownLatch(count);
    }

    /**
     * Tells an active layout {@link CountDownLatch} to wait a number of seconds for its release
     * until throwing.
     */
    void waitForLayout(long seconds) throws Throwable {
      layoutLatch.await(seconds, SECONDS);
      MatcherAssert.assertThat(
          "all layouts should complete on time", layoutLatch.getCount(), CoreMatchers.is(0L));
      // use a runnable to ensure RV layout is finished
      InstrumentationRegistry.getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {}
              });
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {
      super.onLayoutChildren(recycler, state);
      layoutLatch.countDown();
    }
  }
}
