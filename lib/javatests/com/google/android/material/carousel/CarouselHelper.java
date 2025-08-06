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

import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
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

  private static final int DEFAULT_ITEM_COUNT = 10;

  private CarouselHelper() {}


  /** Ensure that as child index increases, adapter position also increases. */
  static void assertChildrenHaveValidOrder(WrappedCarouselLayoutManager layoutManager) {
    // CarouselLayoutManager keeps track of internal start position state and should always have
    // an accurate ordering where adapter position increases as child index increases.
    for (int i = 0; i < layoutManager.getChildCount() - 1; i++) {
      int currentAdapterPosition = layoutManager.getPosition(layoutManager.getChildAt(i));
      int nextAdapterPosition = layoutManager.getPosition(layoutManager.getChildAt(i + 1));
      assertWithMessage(
          "Child at index "
              + i
              + " had a greater adapter position ["
              + currentAdapterPosition
              + "] than child at index "
              + (i + 1)
              + " ["
              + nextAdapterPosition
              + "]")
          .that(currentAdapterPosition)
          .isLessThan(nextAdapterPosition);
    }
  }

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

  static void scrollHorizontallyBy(
      RecyclerView recyclerView, WrappedCarouselLayoutManager layoutManager, int dx)
      throws Throwable {
    layoutManager.expectScrolls(1);
    recyclerView.scrollBy(dx, 0);
    layoutManager.waitForScroll(3L);
  }

  static void scrollVerticallyBy(
      RecyclerView recyclerView, WrappedCarouselLayoutManager layoutManager, int dy)
      throws Throwable {
    layoutManager.expectScrolls(1);
    recyclerView.scrollBy(0, dy);
    layoutManager.waitForScroll(3L);
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

  /**
   * Handles setting the orientation of the carousel and waiting until the recycler view has made a
   * layout pass.
   */
  static void setVerticalOrientation(
      RecyclerView recyclerView,
      WrappedCarouselLayoutManager layoutManager)
      throws Throwable {
    layoutManager.expectLayouts(1);
    layoutManager.setOrientation(CarouselLayoutManager.VERTICAL);
    recyclerView.measure(
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredWidth(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(recyclerView.getMeasuredHeight(), MeasureSpec.EXACTLY));
    recyclerView.layout(0, 0, recyclerView.getMeasuredWidth(), recyclerView.getMeasuredHeight());
    layoutManager.waitForLayout(3L);
  }

  /** Creates a {@link Carousel} with a specified {@code width}. */
  static Carousel createCarouselWithWidth(int width) {
    return createCarouselWithSizeAndOrientation(width, CarouselLayoutManager.HORIZONTAL);
  }

  /**
   * Creates a {@link Carousel} with a specified {@code size} for both width and height and the
   * specified orientation.
   */
  static Carousel createCarouselWithSizeAndOrientation(int size, int orientation) {
    return new Carousel() {

      @Override
      public int getContainerWidth() {
        return size;
      }

      @Override
      public int getContainerHeight() {
        return size;
      }

      @Override
      public boolean isHorizontal() {
        return orientation == CarouselLayoutManager.HORIZONTAL;
      }

      @Override
      public int getCarouselAlignment() {
        return CarouselLayoutManager.ALIGNMENT_START;
      }

      @Override
      public int getItemCount() {
        return DEFAULT_ITEM_COUNT;
      }
    };
  }

  static Carousel createCenterAlignedCarouselWithSize(int size) {
    return new Carousel() {
      @Override
      public int getContainerWidth() {
        return size;
      }

      @Override
      public int getContainerHeight() {
        return size;
      }

      @Override
      public boolean isHorizontal() {
        return true;
      }

      @Override
      public int getCarouselAlignment() {
        return CarouselLayoutManager.ALIGNMENT_CENTER;
      }

      @Override
      public int getItemCount() {
        return DEFAULT_ITEM_COUNT;
      }
    };
  }

  /**
   * Creates a {@link Carousel} with a specified {@code size} for both width and height and the
   * specified alignment and orientation.
   */
  static Carousel createCarousel(int width, int height, int orientation, int alignment) {
    return new Carousel() {

      @Override
      public int getContainerWidth() {
        return width;
      }

      @Override
      public int getContainerHeight() {
        return height;
      }

      @Override
      public boolean isHorizontal() {
        return orientation == CarouselLayoutManager.HORIZONTAL;
      }

      @Override
      public int getCarouselAlignment() {
        return alignment;
      }

      @Override
      public int getItemCount() {
        return DEFAULT_ITEM_COUNT;
      }
    };
  }

  /**
   * Creates a {@link Carousel} with a specified {@code size} for both width and height and the
   * specified item count and alignment.
   */
  static Carousel createCarouselWithItemCount(int size, int alignment, int itemCount) {
    return new Carousel() {

      @Override
      public int getContainerWidth() {
        return size;
      }

      @Override
      public int getContainerHeight() {
        return size;
      }

      @Override
      public boolean isHorizontal() {
        return true;
      }

      @Override
      public int getCarouselAlignment() {
        return alignment;
      }

      @Override
      public int getItemCount() {
        return itemCount;
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

    CountDownLatch scrollLatch;
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

    void expectScrolls(int count) {
      scrollLatch = new CountDownLatch(count);
    }

    /**
     * Tells an active layout {@link CountDownLatch} to wait a number of seconds for its release
     * until throwing.
     */
    void waitForLayout(long seconds) throws Throwable {
      waitForLatch(layoutLatch, seconds, "layout");
    }

    /**
     * Tells an active scroll {@link CountDownLatch} to wait a number of seconds for its release
     * until throwing.
     */
    void waitForScroll(long seconds) throws Throwable {
      waitForLatch(scrollLatch, seconds, "scroll");
    }

    private void waitForLatch(CountDownLatch latch, long seconds, String tag) throws Throwable {
      latch.await(seconds, SECONDS);
      MatcherAssert.assertThat(
          "all " + tag + "s should complete on time", latch.getCount(), CoreMatchers.is(0L));
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

    @Override
    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
      int scroll = super.scrollHorizontallyBy(dx, recycler, state);
      scrollLatch.countDown();
      return scroll;
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
      int scroll = super.scrollVerticallyBy(dy, recycler, state);
      scrollLatch.countDown();
      return scroll;
    }
  }

  static KeylineState getTestCenteredKeylineState() {
    float smallSize = 56F;
    float extraSmallSize = 10F;
    float largeSize = 450F;
    float mediumSize = 88F;

    float extraSmallMask = getKeylineMaskPercentage(extraSmallSize, largeSize);
    float smallMask = getKeylineMaskPercentage(smallSize, largeSize);
    float mediumMask = getKeylineMaskPercentage(mediumSize, largeSize);

    return new KeylineState.Builder(450F, 1320)
        .addKeyline(5F, extraSmallMask, extraSmallSize)
        .addKeylineRange(38F, smallMask, smallSize, 2)
        .addKeyline(166F, mediumMask, mediumSize)
        .addKeylineRange(435F, 0F, largeSize, 2, true)
        .addKeyline(1154F, mediumMask, mediumSize)
        .addKeylineRange(1226F, smallMask, smallSize, 2)
        .addKeyline(1315F, extraSmallMask, extraSmallSize)
        .build();
  }

  static KeylineState getTestCenteredVerticalKeylineState() {
    // Keylines in the form small-medium-large-medium-small; the sizes of
    // these keylines add up to default recycler view height (200).
    float smallSize = 18F;
    float largeSize = 100F;
    float mediumSize = 32F;

    float smallMask = getKeylineMaskPercentage(smallSize, largeSize);
    float mediumMask = getKeylineMaskPercentage(mediumSize, largeSize);

    return new KeylineState.Builder(100F, 200)
        .addKeyline(9F, smallMask, smallSize)
        .addKeyline(25F, mediumMask, mediumSize)
        .addKeyline(66F, 0F, largeSize, true)
        .addKeyline(132F, mediumMask, mediumSize)
        .addKeyline(157F, smallMask, smallSize)
        .build();
  }

  static View createViewWithSize(Context context, int width, int height) {
    View view = new View(context);
    view.setLayoutParams(new RecyclerView.LayoutParams(width, height));
    view.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    return view;
  }
}
