/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.carousel;

import static com.google.android.material.carousel.CarouselHelper.createDataSetWithSize;
import static com.google.android.material.carousel.CarouselHelper.getTestCenteredKeylineState;
import static com.google.android.material.carousel.CarouselHelper.scrollHorizontallyBy;
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
import com.google.android.material.carousel.CarouselHelper.WrappedCarouselLayoutManager;
import com.google.android.material.carousel.CarouselStrategy.StrategyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link CarouselSnapHelper}. */
@RunWith(RobolectricTestRunner.class)
public class CarouselSnapHelperTest {
  RecyclerView recyclerView;
  WrappedCarouselLayoutManager layoutManager;
  CarouselTestAdapter adapter;

  private static final int DEFAULT_RECYCLER_VIEW_WIDTH = 1320;
  private static final int DEFAULT_RECYCLER_VIEW_HEIGHT = 200;
  private static final int DEFAULT_ITEM_WIDTH = 450;
  private static final int DEFAULT_ITEM_HEIGHT = 200;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void setUp() throws Throwable {
    recyclerView = new RecyclerView(context);
    setViewSize(recyclerView, DEFAULT_RECYCLER_VIEW_WIDTH, DEFAULT_RECYCLER_VIEW_HEIGHT);

    layoutManager = new WrappedCarouselLayoutManager();
    adapter = new CarouselTestAdapter(DEFAULT_ITEM_WIDTH, DEFAULT_ITEM_HEIGHT);

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    layoutManager.setCarouselStrategy(
        new CarouselStrategy() {
          @Override
          public KeylineState onFirstChildMeasuredWithMargins(
              @NonNull Carousel carousel, @NonNull View child) {
            return getTestCenteredKeylineState();
          }
        });
    setAdapterItems(recyclerView, layoutManager, adapter, createDataSetWithSize(10));
  }

  @Test
  public void testSnap_snapsCorrectView() throws Throwable {
    CarouselSnapHelper snapHelper = new CarouselSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);

    // Scroll to position to set the horizontal scroll offset to position 3
    scrollToPosition(recyclerView, layoutManager, 3);
    assertThat(layoutManager.getPosition(snapHelper.findSnapView(layoutManager))).isEqualTo(3);

    // The snap view should still be the item at position 3 with a small scroll offset
    scrollHorizontallyBy(recyclerView, layoutManager, 50);
    assertThat(layoutManager.getPosition(snapHelper.findSnapView(layoutManager))).isEqualTo(3);

    // Similarly, the snap view should still be the item at position 3 with a small scroll offset
    scrollHorizontallyBy(recyclerView, layoutManager, -100);
    assertThat(layoutManager.getPosition(snapHelper.findSnapView(layoutManager))).isEqualTo(3);

    // If scrolled enough, the snap view should be the item at position 4.
    scrollHorizontallyBy(recyclerView, layoutManager, DEFAULT_ITEM_WIDTH);
    assertThat(layoutManager.getPosition(snapHelper.findSnapView(layoutManager))).isEqualTo(4);
  }

  @Test
  public void testSnap_correctDistance() throws Throwable {
    CarouselSnapHelper snapHelper = new CarouselSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);

    // Scroll to position to set the horizontal scroll offset to position 3
    scrollToPosition(recyclerView, layoutManager, 3);

    int[] distance =
        snapHelper.calculateDistanceToFinalSnap(
            layoutManager, snapHelper.findSnapView(layoutManager));
    assertThat(distance[0]).isEqualTo(0);

    // The snap view should still be the item at position 3 with a small scroll offset
    scrollHorizontallyBy(recyclerView, layoutManager, 50);
    distance =
        snapHelper.calculateDistanceToFinalSnap(
            layoutManager, snapHelper.findSnapView(layoutManager));
    assertThat(distance[0]).isEqualTo(-50);

    // Similarly, the snap view should still be the item at position 3 with a small scroll offset
    scrollHorizontallyBy(recyclerView, layoutManager, -100);
    distance =
        snapHelper.calculateDistanceToFinalSnap(
            layoutManager, snapHelper.findSnapView(layoutManager));
    assertThat(distance[0]).isEqualTo(50);

    int horizontalScrollBefore = layoutManager.scrollOffset;
    // If scrolled enough, the snap view should be the item at position 4.
    // We scrolled by the item width, so the snap distance should still be 50.
    scrollHorizontallyBy(recyclerView, layoutManager, DEFAULT_ITEM_WIDTH);
    int horizontalScrollAfter = layoutManager.scrollOffset;

    distance =
        snapHelper.calculateDistanceToFinalSnap(
            layoutManager, snapHelper.findSnapView(layoutManager));

    // When shifting from position 3 -> position 4, the target keyline that we are snapping to
    // will also shift. We must account for this in the snap distance; the snap will be 50 (the
    // original snap distance if keylines stayed the same) minus the difference in focal keyline
    // location between keyline states.
    KeylineStateList stateList =
        KeylineStateList.from(
            layoutManager, getTestCenteredKeylineState(), 0, 0, 0, StrategyType.CONTAINED);
    KeylineState target1 =
        stateList.getShiftedState(
            horizontalScrollBefore,
            layoutManager.minScroll,
            layoutManager.maxScroll,
            true);
    float firstTargetKeylineLoc = target1.getFirstFocalKeyline().loc;
    KeylineState target2 =
        stateList.getShiftedState(
            horizontalScrollAfter,
            layoutManager.minScroll,
            layoutManager.maxScroll,
            true);
    float secondTargetKeylineLoc = target2.getFirstFocalKeyline().loc;

    assertThat(distance[0]).isEqualTo(50 - (int) (secondTargetKeylineLoc - firstTargetKeylineLoc));
  }

  @Test
  public void testSnapHelper_consumesFling() throws Throwable {
    CarouselSnapHelper snapHelper = new CarouselSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);

    scrollToPosition(recyclerView, layoutManager, 3);

    // Set horizontal scroll offset to be halfway between position 3 and 4.
    scrollHorizontallyBy(recyclerView, layoutManager, DEFAULT_ITEM_WIDTH / 2);

    // Velocity is negative, so closest item before keyline is position 3. Actual
    // velocity is not taken re: flinging.
    int position = snapHelper.findTargetSnapPosition(layoutManager, -1000, 0);
    assertThat(position).isEqualTo(3);

    // Velocity is positive, so closest item after keyline is position 4.  Actual
    // velocity is not taken re: flinging.
    position = snapHelper.findTargetSnapPosition(layoutManager, 1000, 0);
    assertThat(position).isEqualTo(4);
  }

  @Test
  public void testEnablingFling_doesNotConsumeFling() throws Throwable {
    CarouselSnapHelper snapHelper = new CarouselSnapHelper(false);
    snapHelper.attachToRecyclerView(recyclerView);

    int position = snapHelper.findTargetSnapPosition(layoutManager, -1, 0);
    assertThat(position).isEqualTo(RecyclerView.NO_POSITION);
  }
}
