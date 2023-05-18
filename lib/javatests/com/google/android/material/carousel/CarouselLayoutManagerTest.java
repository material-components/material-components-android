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

import static com.google.android.material.carousel.CarouselHelper.assertChildrenHaveValidOrder;
import static com.google.android.material.carousel.CarouselHelper.createDataSetWithSize;
import static com.google.android.material.carousel.CarouselHelper.getKeylineMaskPercentage;
import static com.google.android.material.carousel.CarouselHelper.getTestCenteredKeylineState;
import static com.google.android.material.carousel.CarouselHelper.scrollHorizontallyBy;
import static com.google.android.material.carousel.CarouselHelper.scrollToPosition;
import static com.google.android.material.carousel.CarouselHelper.setAdapterItems;
import static com.google.android.material.carousel.CarouselHelper.setViewSize;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.carousel.CarouselHelper.CarouselTestAdapter;
import com.google.android.material.carousel.CarouselHelper.TestItem;
import com.google.android.material.carousel.CarouselHelper.WrappedCarouselLayoutManager;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link CarouselLayoutManager}. */
@RunWith(RobolectricTestRunner.class)
public class CarouselLayoutManagerTest {

  private static final int DEFAULT_RECYCLER_VIEW_WIDTH = 1320;
  private static final int DEFAULT_RECYCLER_VIEW_HEIGHT = 200;
  private static final int DEFAULT_ITEM_WIDTH = 450;
  private static final int DEFAULT_ITEM_HEIGHT = 200;

  private final Context context = ApplicationProvider.getApplicationContext();

  RecyclerView recyclerView;
  WrappedCarouselLayoutManager layoutManager;
  CarouselTestAdapter adapter;

  @Before
  public void setUp() {
    createAndSetFixtures(DEFAULT_RECYCLER_VIEW_WIDTH, DEFAULT_ITEM_WIDTH);
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return getTestCenteredKeylineState();
          }
        });
  }

  @Test
  public void testAddAdapterItem_isAddedByLayoutManager() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, ImmutableList.of(new TestItem()));
    assertThat(recyclerView.getChildCount()).isEqualTo(1);
  }

  @Test
  public void testMeasureChild_usesStateItemSize() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    assertThat(recyclerView.getChildAt(0).getMeasuredWidth()).isEqualTo(DEFAULT_ITEM_WIDTH);
  }

  @Test
  public void testMaskedChild_isStillGivenFullWidthBounds() throws Throwable {
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return new KeylineState.Builder(DEFAULT_ITEM_WIDTH)
                .addKeyline(225F, .5F, 225F, true)
                .build();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    MaskableFrameLayout child = (MaskableFrameLayout) recyclerView.getChildAt(0);
    assertThat(child.getLeft()).isEqualTo(0);
    assertThat(child.getRight()).isEqualTo(450);
  }

  @Test
  public void testMaskedChild_isMaskedToCorrectSize() throws Throwable {
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return new KeylineState.Builder(DEFAULT_ITEM_WIDTH)
                .addKeyline(225F, .8F, 90F, true)
                .build();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    MaskableFrameLayout child = (MaskableFrameLayout) recyclerView.getChildAt(0);
    assertThat(child.getMaskRectF().width()).isEqualTo(450F * .2F);
  }

  @Test
  public void testKnownArrangement_initialScrollPositionHasAllItemsWithinCarouselContainer()
      throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));

    MaskableFrameLayout firstChild = (MaskableFrameLayout) recyclerView.getChildAt(0);
    int maskLeft = (int) firstChild.getMaskRectF().left;
    MaskableFrameLayout lastChild =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    int maskRight = (int) (lastChild.getWidth() - lastChild.getMaskRectF().right);

    assertThat(firstChild.getLeft() + maskLeft).isEqualTo(0);
    assertThat(lastChild.getRight() - maskRight).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testScrollToPosition_movesChildToFocalStartKeyline() throws Throwable {
    KeylineState keylineState = getTestCenteredKeylineState();
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return keylineState;
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(40));

    scrollToPosition(recyclerView, layoutManager, 20);

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.findViewHolderForAdapterPosition(20).itemView;
    float childCenterX = child.getLeft() + (child.getWidth() / 2F);
    assertThat(childCenterX).isEqualTo(keylineState.getFirstFocalKeyline().locOffset);
  }

  @Test
  public void testScrollBeyondMaxHorizontalScroll_shouldLimitToMaxScrollOffset() throws Throwable {
    KeylineState keylineState = getTestCenteredKeylineState();
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return keylineState;
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 200);

    KeylineState endState = KeylineStateList.from(layoutManager, keylineState).getRightState();

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    float childCenterX = child.getLeft() + (child.getWidth() / 2F);
    assertThat(childCenterX).isEqualTo(endState.getLastFocalKeyline().locOffset);
  }

  @Test
  public void testInitialFill_shouldFillMinimumItemCountForContainer() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));

    assertThat(recyclerView.getChildCount()).isEqualTo(11);
  }

  @Test
  public void testScrollAndFill_shouldRecycleAndFillMinimumItemCountForContainer()
      throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildCount()).isEqualTo(12);
  }

  @Test
  public void testEmptyAdapter_shouldClearAllViewsFromRecyclerView() throws Throwable {
    // Fill the adapter and then empty it to make sure all views are removed and recycled
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(0));

    assertThat(recyclerView.getChildCount()).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldBeInFocalRange() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    assertThat(((Maskable) recyclerView.getChildAt(0)).getMaskXPercentage()).isEqualTo(0F);
  }

  @Test
  public void testSingleItem_shouldNotScrollLeft() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldNotScrollRight() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, -100);

    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(0);
  }

  @Test
  public void testChangeAdapterItemCount_shouldAlignFirstItemToStart() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    assertThat(recyclerView.getChildCount()).isEqualTo(1);
    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(0);
  }

  @Test
  public void testScrollToEnd_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 9);

    assertChildrenHaveValidOrder(layoutManager);
  }

  @Test
  public void testScrollToMiddle_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 99);

    assertChildrenHaveValidOrder(layoutManager);
  }

  @Test
  public void testScrollToEndThenToStart_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 9);
    scrollToPosition(recyclerView, layoutManager, 2);

    assertChildrenHaveValidOrder(layoutManager);
  }

  @Test
  public void testContainedLayout_doesNotAllowFirstItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(new TestContainmentCarouselStrategy(/* isContained= */ true));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect firstChildMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(0));
    assertThat(firstChildMask.left).isAtLeast(0);
  }

  @Test
  public void testContainedLayout_doesNotAllowLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(new TestContainmentCarouselStrategy(/* isContained= */ true));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 5);
    scrollHorizontallyBy(recyclerView, layoutManager, -165);

    Rect lastChildMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1));
    assertThat(lastChildMask.right).isAtMost(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testUncontainedLayout_allowsFistItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* isContained= */ false));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect firstItemMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(0));
    assertThat(firstItemMask.left).isLessThan(0);
  }

  @Test
  public void testUncontainedLayout_allowsLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* isContained= */ false));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect lastItemMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1));
    assertThat(lastItemMask.right).isGreaterThan(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  /**
   * Assigns explicit sizes to fixtures being used to construct the testing environment.
   *
   * @param recyclerWidth The width of the recycler view being used
   * @param itemWidth The width each item added to the recycler view would like to be laid out with.
   *     What would be defined in xml as {@code android:layout_width}.
   */
  private void createAndSetFixtures(int recyclerWidth, int itemWidth) {
    recyclerView = new RecyclerView(context);
    setViewSize(recyclerView, recyclerWidth, DEFAULT_RECYCLER_VIEW_HEIGHT);

    layoutManager = new WrappedCarouselLayoutManager();
    adapter = new CarouselTestAdapter(itemWidth, DEFAULT_ITEM_HEIGHT);

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  /**
   * Gets the bounds of {@code child}'s mask after they are offset to the parent RecyclerView's
   * coordinates
   */
  private Rect getMaskRectOffsetToRecyclerViewCoords(MaskableFrameLayout child) {
    RectF maskRect = child.getMaskRectF();
    Rect offsetRect =
        new Rect(
            (int) maskRect.left, (int) maskRect.top, (int) maskRect.right, (int) maskRect.bottom);
    recyclerView.offsetDescendantRectToMyCoords(child, offsetRect);
    return offsetRect;
  }

  /**
   * A CarouselStrategy used to test that items are masked correctly when contained vs. uncontained.
   */
  private static class TestContainmentCarouselStrategy extends CarouselStrategy {

    private final boolean isContained;

    TestContainmentCarouselStrategy(boolean isContained) {
      this.isContained = isContained;
    }

    @Override
    KeylineState onFirstChildMeasuredWithMargins(@NonNull Carousel carousel, @NonNull View child) {
      float largeSize = DEFAULT_RECYCLER_VIEW_WIDTH * .75F; // 990F
      float smallSize = DEFAULT_RECYCLER_VIEW_WIDTH - largeSize; // 330F
      float xSmallSize = 100F;

      float xSmallHead = xSmallSize / -2F;
      float focal = largeSize / 2F;
      float smallTail = focal + (largeSize / 2F) + (smallSize / 2F);
      float xSmallTail = DEFAULT_RECYCLER_VIEW_WIDTH + (xSmallSize / 2F);
      return new KeylineState.Builder(largeSize)
          .addKeyline(xSmallHead, getKeylineMaskPercentage(xSmallSize, largeSize), xSmallSize)
          .addKeyline(focal, 0F, largeSize, true)
          .addKeyline(smallTail, getKeylineMaskPercentage(smallSize, largeSize), smallSize)
          .addKeyline(xSmallTail, getKeylineMaskPercentage(xSmallSize, largeSize), xSmallSize)
          .build();
    }

    @Override
    boolean isContained() {
      return isContained;
    }
  }
}
