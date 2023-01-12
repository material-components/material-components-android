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

import static com.google.android.material.carousel.CarouselHelper.createDataSetWithSize;
import static com.google.android.material.carousel.CarouselHelper.scrollToPosition;
import static com.google.android.material.carousel.CarouselHelper.setAdapterItems;
import static com.google.android.material.carousel.CarouselHelper.setViewSize;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
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
  }

  @Test
  public void testAddAdapterItem_isAddedByLayoutManager() throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, ImmutableList.of(new TestItem()));
    assertThat(recyclerView.getChildCount()).isEqualTo(1);
  }

  @Test
  public void testMeasureChild_usesStateItemSize() throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));
    assertThat(recyclerView.getChildAt(0).getMeasuredWidth()).isEqualTo(DEFAULT_ITEM_WIDTH);
  }

  @Test
  public void testMaskedChild_isStillGivenFullWidthBounds() throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
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
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
            return new KeylineState.Builder(DEFAULT_ITEM_WIDTH)
                .addKeyline(225F, .8F, 90F, true)
                .build();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    MaskableFrameLayout child = (MaskableFrameLayout) recyclerView.getChildAt(0);
    assertThat(child.getMaskRect().width()).isEqualTo(450F * .2F);
  }

  @Test
  public void testKnownArrangement_initialScrollPositionHasAllItemsWithinCarouselContainer()
      throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));

    MaskableFrameLayout firstChild = (MaskableFrameLayout) recyclerView.getChildAt(0);
    int maskLeft = (int) firstChild.getMaskRect().left;
    MaskableFrameLayout lastChild =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    int maskRight = (int) (lastChild.getWidth() - lastChild.getMaskRect().right);

    assertThat(firstChild.getLeft() + maskLeft).isEqualTo(0);
    assertThat(lastChild.getRight() - maskRight).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testScrollToPosition_movesChildToFocalStartKeyline() throws Throwable {
    KeylineState keylineState = getTestCenteredKeylineState();
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
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
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(View child) {
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
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(@NonNull View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));

    assertThat(recyclerView.getChildCount()).isEqualTo(11);
  }

  @Test
  public void testScrollAndFill_shouldRecycleAndFillMinimumItemCountForContainer()
      throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(@NonNull View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildCount()).isEqualTo(12);
  }

  @Test
  public void testEmptyAdapter_shouldClearAllViewsFromRecyclerView() throws Throwable {
    layoutManager.setCarouselConfiguration(
        new CarouselConfiguration(layoutManager) {
          @Override
          protected KeylineState onFirstChildMeasuredWithMargins(@NonNull View child) {
            return getTestCenteredKeylineState();
          }
        });

    // Fill the adapter and then empty it to make sure all views are removed and recycled
    setAdapterItems(
        recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(0));

    assertThat(recyclerView.getChildCount()).isEqualTo(0);
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

  private static KeylineState getTestCenteredKeylineState() {
    float smallSize = 56F;
    float extraSmallSize = 10F;
    float largeSize = 450F;
    float mediumSize = 88F;

    float extraSmallMask = 1F - (extraSmallSize / largeSize);
    float smallMask = 1F - (smallSize / largeSize);
    float mediumMask = 1F - (mediumSize / largeSize);

    return new KeylineState.Builder(450F)
        .addKeyline(5F, extraSmallMask, extraSmallSize)
        .addKeylineRange(38F, smallMask, smallSize, 2)
        .addKeyline(166F, mediumMask, mediumSize)
        .addKeylineRange(435F, 0F, largeSize, 2, true)
        .addKeyline(1154F, mediumMask, mediumSize)
        .addKeylineRange(1226F, smallMask, smallSize, 2)
        .addKeyline(1315F, extraSmallMask, extraSmallSize)
        .build();
  }
}
