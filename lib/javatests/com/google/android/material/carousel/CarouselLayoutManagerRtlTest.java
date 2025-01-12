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

import com.google.android.material.test.R;

import static com.google.android.material.carousel.CarouselHelper.createDataSetWithSize;
import static com.google.android.material.carousel.CarouselHelper.scrollHorizontallyBy;
import static com.google.android.material.carousel.CarouselHelper.scrollToPosition;
import static com.google.android.material.carousel.CarouselHelper.setAdapterItems;
import static com.google.android.material.carousel.CarouselHelper.setViewSize;
import static com.google.android.material.testing.RtlTestUtils.applyRtlPseudoLocale;
import static com.google.android.material.testing.RtlTestUtils.checkAppSupportsRtl;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.carousel.CarouselHelper.CarouselTestAdapter;
import com.google.android.material.carousel.CarouselHelper.WrappedCarouselLayoutManager;
import com.google.android.material.carousel.CarouselStrategy.StrategyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** RTL tests for {@link CarouselLayoutManager}. */
@RunWith(RobolectricTestRunner.class)
@org.junit.Ignore("(b/265311943) Fix RTL support for Robolectric tests.")
public class CarouselLayoutManagerRtlTest {

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
    checkAppSupportsRtl();
    applyRtlPseudoLocale();
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
  public void testFirstAdapterItem_isDrawnAtRightOfContainer() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));

    MaskableFrameLayout firstChild = (MaskableFrameLayout) recyclerView.getChildAt(0);
    assertThat(recyclerView.getChildAdapterPosition(firstChild)).isEqualTo(0);

    assertThat(firstChild.getRight()).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Config(qualifiers = "sw1320dp-w1320dp")
  @Test
  public void testScrollBeyondMaxHorizontalScroll_shouldLimitToMaxScrollOffset() throws Throwable {
    KeylineState keylineState = getTestCenteredKeylineState();
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 200);

    KeylineState leftState =
        KeylineStateList.from(
                layoutManager, KeylineState.reverse(keylineState, DEFAULT_RECYCLER_VIEW_WIDTH),
                0, 0, 0, StrategyType.CONTAINED)
            .getStartState();

    MaskableFrameLayout child =
        (MaskableFrameLayout) recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    float childCenterX = child.getLeft() + (child.getWidth() / 2F);
    assertThat(childCenterX).isEqualTo(leftState.getFirstFocalKeyline().locOffset);
  }

  @Test
  public void testSingleItem_shouldBeInFocalRange() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(1));
    RectF maskRect = ((Maskable) recyclerView.getChildAt(0)).getMaskRectF();

    assertThat((int) (maskRect.right - maskRect.left)).isEqualTo(DEFAULT_ITEM_WIDTH);
  }

  @Test
  public void testSingleItem_shouldNotScrollLeft() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, 100);

    assertThat(recyclerView.getChildAt(0).getRight()).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testSingleItem_shouldNotScrollRight() throws Throwable {
    setAdapterItems(recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(1));
    scrollHorizontallyBy(recyclerView, layoutManager, -100);

    assertThat(recyclerView.getChildAt(0).getRight()).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testChangeAdapterItemCount_shouldAlignFirstItemToStart() throws Throwable {
    setAdapterItems(
        recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(200));
    scrollToPosition(recyclerView, layoutManager, 100);
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(1));

    assertThat(recyclerView.getChildCount()).isEqualTo(1);
    assertThat(recyclerView.getChildAt(0).getRight()).isEqualTo(DEFAULT_RECYCLER_VIEW_WIDTH);
  }

  @Test
  public void testScrollToEndThenToStart_childrenHaveValidOrder() throws Throwable {
    // TODO(b/271293808): Refactor to use parameterized tests.
    setAdapterItems(recyclerView, layoutManager, adapter, CarouselHelper.createDataSetWithSize(10));
    scrollToPosition(recyclerView, layoutManager, 9);
    scrollToPosition(recyclerView, layoutManager, 2);

    CarouselHelper.assertChildrenHaveValidOrder(layoutManager);
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

  /**
   * Assigns explicit sizes to fixtures being used to construct the testing environment.
   *
   * @param recyclerWidth The width of the recycler view being used
   * @param itemWidth The width each item added to the recycler view would like to be laid out with.
   *     What would be defined in xml as {@code android:layout_width}.
   */
  private void createAndSetFixtures(int recyclerWidth, int itemWidth) {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Material3_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    recyclerView = new RecyclerView(context);
    activity.addContentView(
        recyclerView, new LayoutParams(recyclerWidth, DEFAULT_RECYCLER_VIEW_HEIGHT));
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

    return new KeylineState.Builder(450F, DEFAULT_RECYCLER_VIEW_WIDTH)
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
