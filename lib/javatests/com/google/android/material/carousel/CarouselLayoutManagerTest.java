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
import static com.google.android.material.carousel.CarouselHelper.getTestCenteredVerticalKeylineState;
import static com.google.android.material.carousel.CarouselHelper.scrollHorizontallyBy;
import static com.google.android.material.carousel.CarouselHelper.scrollToPosition;
import static com.google.android.material.carousel.CarouselHelper.scrollVerticallyBy;
import static com.google.android.material.carousel.CarouselHelper.setAdapterItems;
import static com.google.android.material.carousel.CarouselHelper.setVerticalOrientation;
import static com.google.android.material.carousel.CarouselHelper.setViewSize;
import static com.google.android.material.carousel.CarouselLayoutManager.HORIZONTAL;
import static com.google.android.material.carousel.CarouselLayoutManager.VERTICAL;
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
import com.google.android.material.carousel.CarouselStrategy.StrategyType;
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
          public KeylineState onFirstChildMeasuredWithMargins(
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
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return new KeylineState.Builder(DEFAULT_ITEM_WIDTH, DEFAULT_RECYCLER_VIEW_WIDTH)
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
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return new KeylineState.Builder(DEFAULT_ITEM_WIDTH, DEFAULT_RECYCLER_VIEW_WIDTH)
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
          public KeylineState onFirstChildMeasuredWithMargins(
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
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return keylineState;
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 200);

    KeylineState endState =
        KeylineStateList.from(layoutManager, keylineState, 0, 0, 0, StrategyType.CONTAINED)
            .getEndState();

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    float childCenterX = child.getLeft() + (child.getWidth() / 2F);
    assertThat(childCenterX).isEqualTo(endState.getLastFocalKeyline().locOffset);
  }

  @Test
  public void testScrollToPositionInVertical_movesChildToFocalStartKeyline() throws Throwable {
    KeylineState keylineState = getTestCenteredVerticalKeylineState();
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return keylineState;
          }
        });
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(40));
    scrollToPosition(recyclerView, layoutManager, 20);

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.findViewHolderForAdapterPosition(20).itemView;
    float childCenterY = child.getTop() + (child.getHeight() / 2F);
    assertThat(childCenterY).isEqualTo(keylineState.getFirstFocalKeyline().locOffset);
  }

  @Test
  public void testScrollBeyondMaxVerticalScroll_shouldLimitToScrollOffset() throws Throwable {
    KeylineState keylineState = getTestCenteredVerticalKeylineState();
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return keylineState;
          }
        });
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 200);

    KeylineState endState =
        KeylineStateList.from(layoutManager, keylineState, 0, 0, 0, StrategyType.CONTAINED)
            .getEndState();

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    float childCenterY = child.getTop() + (child.getHeight() / 2F);
    assertThat(childCenterY).isEqualTo(endState.getLastFocalKeyline().locOffset);
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
    RectF maskRect = ((Maskable) recyclerView.getChildAt(0)).getMaskRectF();

    assertThat((int) (maskRect.right - maskRect.left)).isEqualTo(DEFAULT_ITEM_WIDTH);
  }

  @Test
  public void testSingleItem_shouldNotScrollLeft() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldNotScrollUp() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildAt(0).getTop()).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldNotScrollRight() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, -100);

    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldNotScrollDown() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, -100);

    assertThat(recyclerView.getChildAt(0).getTop()).isEqualTo(0);
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
  public void testChangeAdapterItemCount_shouldAlignFirstItemToStartVertical() throws Throwable {
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    assertThat(recyclerView.getChildCount()).isEqualTo(1);
    assertThat(recyclerView.getChildAt(0).getTop()).isEqualTo(0);
  }

  @Test
  public void testScrollToEnd_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 9);

    assertChildrenHaveValidOrder(layoutManager);
  }

  @Test
  public void testScrollToEndVertical_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    setVerticalOrientation(recyclerView, layoutManager);
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
  public void testScrollToMiddleVertical_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    setVerticalOrientation(recyclerView, layoutManager);
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
  public void testScrollToEndThenToStartVertical_childrenHaveValidOrder() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollToPosition(recyclerView, layoutManager, 9);
    scrollToPosition(recyclerView, layoutManager, 2);

    assertChildrenHaveValidOrder(layoutManager);
  }

  @Test
  public void testContainedLayout_doesNotAllowFirstItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.CONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect firstChildMask = getFirstVisibleMask();
    assertThat(firstChildMask.left).isAtLeast(0);
  }

  @Test
  public void testContainedLayoutVertical_doesNotAllowFirstItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.CONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, 900);

    Rect firstChildMask = getFirstVisibleMask();

    assertThat(firstChildMask.top).isAtLeast(0);
  }

  @Test
  public void testContainedLayout_doesNotAllowLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.CONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 5);
    scrollHorizontallyBy(recyclerView, layoutManager, -165);

    Rect lastChildMask = getLastVisibleMask();
    assertThat(lastChildMask.right).isAtMost(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testContainedLayoutVertical_doesNotAllowLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.CONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 5);
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, -100);

    Rect lastChildMask = getLastVisibleMask();
    assertThat(lastChildMask.bottom).isAtMost(DEFAULT_RECYCLER_VIEW_HEIGHT);
  }

  @Test
  public void testUncontainedLayout_allowsFistItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect firstItemMask = getFirstVisibleMask();
    assertThat(firstItemMask.left).isLessThan(0);
  }

  @Test
  public void testUncontainedLayoutVertical_allowsFirstItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, 30);

    Rect firstItemMask = getFirstVisibleMask();

    assertThat(firstItemMask.top).isLessThan(0);
  }

  @Test
  public void testUncontainedLayout_allowsLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    Rect lastItemMask = getLastVisibleMask();
    assertThat(lastItemMask.right).isGreaterThan(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testUncontainedLayoutVertical_allowsLastItemToBleed() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    setVerticalOrientation(recyclerView, layoutManager);
    scrollVerticallyBy(recyclerView, layoutManager, 900);

    Rect lastItemMask = getLastVisibleMask();

    assertThat(lastItemMask.right).isGreaterThan(DEFAULT_RECYCLER_VIEW_HEIGHT);
  }

  @Test
  public void testMasksLeftOfParent_areRoundedDown() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollHorizontallyBy(recyclerView, layoutManager, 900);

    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      View child = recyclerView.getChildAt(i);
      Rect itemMask = getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) child);
      assertThat(itemMask.right).isNotEqualTo(0);
    }
  }

  @Test
  public void testMaskOnLeftParentEdge_areRoundedDown() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    // Scroll to end
    scrollToPosition(recyclerView, layoutManager, 9);

    // Carousel strategy at end is {small, large}. Last child will be large item, second last
    // child will be small item. So third last child's right mask edge should not show.
    Rect thirdLastChildMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 3));
    assertThat(thirdLastChildMask.right).isLessThan(0);
    assertThat(thirdLastChildMask.right).isAtLeast(thirdLastChildMask.left);
  }

  @Test
  public void testMasksTopOfParent_areRoundedDown() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollVerticallyBy(recyclerView, layoutManager, 900);

    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      View child = recyclerView.getChildAt(i);
      Rect itemMask = getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) child);
      assertThat(itemMask.bottom).isNotEqualTo(0);
    }
  }

  @Test
  public void testMaskOnTopParentEdge_areRoundedDown() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    // Scroll to end
    scrollToPosition(recyclerView, layoutManager, 9);

    // Carousel strategy at end is {small, large}. Last child will be large item, second last
    // child will be small item. So third last child's bottom mask edge should not show.
    Rect thirdLastChildMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 3));
    assertThat(thirdLastChildMask.bottom).isLessThan(0);
    assertThat(thirdLastChildMask.bottom).isAtLeast(thirdLastChildMask.top);

    // Assert that the other children masks are within bounds.
    Rect firstLastChildMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 2));
    assertThat(firstLastChildMask.bottom).isGreaterThan(0);
    assertThat(firstLastChildMask.bottom).isAtLeast(firstLastChildMask.top);

    Rect secondLastChildMask =
        getMaskRectOffsetToRecyclerViewCoords(
            (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1));
    assertThat(secondLastChildMask.bottom).isGreaterThan(0);
    assertThat(secondLastChildMask.bottom).isAtLeast(secondLastChildMask.top);
  }

  @Test
  public void testMaskOnRightParentEdge_areRoundedUp() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));

    // Carousel strategy is {large, small}. First child will be large item, second child will
    // be small item, so the third child's left mask edge should not show up at the right parent
    // edge.
    Rect thirdChildMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(2));
    assertThat(thirdChildMask.left).isGreaterThan(DEFAULT_RECYCLER_VIEW_WIDTH);
    assertThat(thirdChildMask.left).isAtMost(thirdChildMask.right);
  }

  @Test
  public void testMaskOnBottomParentEdge_areRoundedUp() throws Throwable {
    layoutManager.setCarouselStrategy(
        new TestContainmentCarouselStrategy(/* strategyType= */ StrategyType.UNCONTAINED));
    setVerticalOrientation(recyclerView, layoutManager);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));

    // Carousel strategy is {large, small}. First child will be large item, second child will
    // be small item, so the third child's top mask edge should not show up at the bottom parent
    // edge.
    Rect thirdChildMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(2));
    assertThat(thirdChildMask.top).isGreaterThan(DEFAULT_RECYCLER_VIEW_HEIGHT);
    assertThat(thirdChildMask.top).isAtMost(thirdChildMask.bottom);

    // Assert that the other children masks are within bounds.
    Rect firstChildMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(0));
    assertThat(firstChildMask.top).isLessThan(DEFAULT_RECYCLER_VIEW_HEIGHT);
    assertThat(firstChildMask.top).isAtMost(firstChildMask.bottom);

    Rect secondChildMask =
        getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) recyclerView.getChildAt(1));
    assertThat(secondChildMask.top).isLessThan(DEFAULT_RECYCLER_VIEW_HEIGHT);
    assertThat(secondChildMask.top).isAtMost(secondChildMask.bottom);
  }

  @Test
  public void testScrollOffset_isNotReset() throws Throwable {
    int itemCount = 10;
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(itemCount));
    assertThat(layoutManager.scrollOffset).isEqualTo(layoutManager.minScroll);

    scrollToPosition(recyclerView, layoutManager, itemCount / 2);

    setVerticalOrientation(recyclerView, layoutManager);
    assertThat(layoutManager.scrollOffset).isNotEqualTo(layoutManager.minScroll);
    assertThat(layoutManager.computeScrollVectorForPosition(itemCount / 2).y).isEqualTo(0f);
  }

  @Test
  public void testRequestChildRectangleOnScreen_doesntScrollIfChildIsFocal() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    assertThat(layoutManager.scrollOffset).isEqualTo(0);

    // Bring second child into focus
    layoutManager.requestChildRectangleOnScreen(
        recyclerView, recyclerView.getChildAt(1), new Rect(), /* immediate= */ true);

    // Test Keyline state has 2 focal keylines at the start; default item with is 450 and
    // focal keyline size is 450, so the scroll offset should be 0.
    assertThat(layoutManager.scrollOffset).isEqualTo(0);
  }

  @Test
  public void testSingleItem_shouldNotScrollWithPadding() throws Throwable {
    recyclerView.setPadding(50, 0, 50, 0);
    recyclerView.setClipToPadding(false);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    int originalLeft = recyclerView.getChildAt(0).getLeft();

    scrollHorizontallyBy(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildAt(0).getLeft()).isEqualTo(originalLeft);
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
   * Return the mask rect of the first child whose mask is within the recycler view bounds/is
   * visible.
   */
  private Rect getFirstVisibleMask() {
    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      View child = recyclerView.getChildAt(i);
      // Return the first view that is visible after masking.
      Rect maskRect = getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) child);
      if (layoutManager.getOrientation() == HORIZONTAL && maskRect.right >= 0) {
        return maskRect;
      }
      if (layoutManager.getOrientation() == VERTICAL && maskRect.bottom >= 0) {
        return maskRect;
      }
    }
    return null;
  }

  /**
   * Return the mask rect of the last child whose mask is within the recycler view bounds/is
   * visible.
   */
  private Rect getLastVisibleMask() {
    for (int i = recyclerView.getChildCount() - 1; i >= 0; i--) {
      View child = recyclerView.getChildAt(i);
      // Return the first view that is visible after masking.
      Rect maskRect = getMaskRectOffsetToRecyclerViewCoords((MaskableFrameLayout) child);
      if (layoutManager.getOrientation() == HORIZONTAL
          && maskRect.left <= DEFAULT_RECYCLER_VIEW_WIDTH) {
        return maskRect;
      }
      if (layoutManager.getOrientation() == VERTICAL
          && maskRect.top <= DEFAULT_RECYCLER_VIEW_HEIGHT) {
        return maskRect;
      }
    }
    return null;
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

    private final StrategyType strategyType;

    TestContainmentCarouselStrategy(StrategyType strategyType) {
      this.strategyType = strategyType;
    }

    @Override
    public KeylineState onFirstChildMeasuredWithMargins(@NonNull Carousel carousel,
        @NonNull View child) {
      int availableSpace = DEFAULT_RECYCLER_VIEW_HEIGHT;
      float xSmallSize = 15F;
      if (carousel.isHorizontal()) {
        availableSpace = DEFAULT_RECYCLER_VIEW_WIDTH;
        xSmallSize = 100F;
      }
      float largeSize = availableSpace * .75F; // 990F when horizontal, 150F when vertical
      float smallSize = availableSpace - largeSize; // 330F when horizontal, 50F when vertical

      float xSmallHead = xSmallSize / -2F;
      float focal = largeSize / 2F;
      float smallTail = focal + (largeSize / 2F) + (smallSize / 2F);
      float xSmallTail = availableSpace + (xSmallSize / 2F);
      return new KeylineState.Builder(largeSize, availableSpace)
          .addAnchorKeyline(xSmallHead, getKeylineMaskPercentage(xSmallSize, largeSize), xSmallSize)
          .addKeyline(focal, 0F, largeSize, true)
          .addKeyline(smallTail, getKeylineMaskPercentage(smallSize, largeSize), smallSize)
          .addAnchorKeyline(xSmallTail, getKeylineMaskPercentage(xSmallSize, largeSize), xSmallSize)
          .build();
    }

    @Override
    StrategyType getStrategyType() {
      return strategyType;
    }
  }
}
