/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.backlayer;

import static android.support.design.backlayer.BackLayerLayoutActions.collapse;
import static android.support.design.backlayer.BackLayerLayoutActions.expand;
import static android.support.design.backlayer.BackLayerLayoutActions.simpleClick;
import static android.support.design.backlayer.BackLayerLayoutActions.waitUntilIdle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.Gravity;
import android.view.View;

/**
 * This is the base class for all BackLayerLayout tests. There is a subclass for each of the
 * possible positions of the backlayer.
 *
 * <p>When adding a new test, add the base implemnetation here and then override it in the
 * orientation-specific subclasses with the {@code @Test @SmallTest} annotations while calling
 * {@code super.methodYouAreOverriding();}. See {@link TopBackLayerLayoutTest} for an example of
 * this.
 *
 * <p>Note: currently there is no subclass for EndBackLayerLayoutTest, it has not been committed
 * since there it is failing for reasons that do not seem directly related to the BackLayerLayout.
 */
public abstract class BackLayerLayoutTestBase {

  protected Activity activity;
  protected Resources resources;
  protected NestedScrollView contentLayer;
  protected BackLayerLayout backLayer;
  protected CoordinatorLayout coordinatorLayout;
  private View primaryExtraContent;
  private View secondaryExtraContent;

  protected boolean isHorizontal = false;
  protected boolean isBackLayerAtEndSide = false;

  @IdRes private int backLayerId;
  @IdRes private int contentLayerId;
  @IdRes private int primaryButtonId;
  @IdRes private int primaryExtraContentId;
  @IdRes private int secondaryButtonId;
  @IdRes private int secondaryExtraContentId;

  /**
   * Assert that all the lengths and positions for the dimension that doesn't move remais consntant.
   */
  private void assertStaticDimensionInvariants(
      int parentLength,
      int backLayerLength,
      int backLayerPosition,
      int contentLayerLength,
      int contentLayerPosition) {
    assertEquals(
        "The dimensions of the Back Layer must always match the dimensions of the parent"
            + " CoordinatorLayout",
        parentLength,
        backLayerLength);
    assertEquals(
        "The length of the content layer in the static dimension (height for horizontal back"
            + " layers, and width for vertical back layers) must match the corresponding dimension"
            + " of the parent CoordinatorLayout",
        parentLength,
        contentLayerLength);
    assertEquals("Position for the backlayer must always be (0,0)", 0, backLayerPosition);
    assertEquals(
        "Position for the content layer in the static dimension (height for horizontal back layers"
            + " and width for vertical back layers) must always be 0",
        0,
        contentLayerPosition);
  }

  /**
   * Assert that the positions and sizes in the dimension where movement happens are consistent with
   * the collapsed state.
   */
  private void assertMovingDimensionCollapsed(
      int parentLength,
      int backLayerLength,
      int backLayerMinimumLength,
      int backLayerPosition,
      int contentLayerLength,
      int contentLayerPosition) {
    assertEquals(
        "The dimensions of the Back Layer must always match the dimensions of the parent"
            + " CoordinatorLayout",
        parentLength,
        backLayerLength);
    assertEquals("Position for the backlayer must always be (0,0)", 0, backLayerPosition);
    assertEquals(
        "The sum of the BackLayer Minimum Length and the content Layer length in the moving"
            + " dimension (height in vertical backlayers, width in horizontal backlayers) must"
            + " match the length of the parent CoordinatorLayout.",
        parentLength,
        backLayerMinimumLength + contentLayerLength);
    if (isBackLayerAtEndSide) {
      // The backlayer is at position 0, meaning the end is uncovered by
      // backLayerMinimumLength. The assertion above this line guarantees that.
      assertEquals(
          "The content layer must be at position 0 in the moving dimension (height in vertical"
              + " backlayers, width in horizontal backlayers) when the backlayer is collapsed and"
              + " the backlayer is either at the bottom or end (left) side. This, coupled with the"
              + " previous assertions, means that at the end the backlayer is exposed by"
              + " backlayerMinimumLength.",
          0,
          contentLayerPosition);
    } else {
      // The backlayer is at position backLayerMinimumLength
      assertEquals(
          "The content layer must be at position backLayerMinimumLength in the moving dimension"
              + " (height in vertical backlayers, width in horizontal backlayers) when collapsed if"
              + " the backlayer is either at the top or the start (left) side. This leaves"
              + " backLayerMinimumLength of the backlayer uncovered by the content layer",
          backLayerMinimumLength,
          contentLayerPosition);
    }
  }

  /**
   * Assert that the positions and sizes in the dimension where movement happens are consistent with
   * the expanded state.
   */
  private void assertMovingDimensionExpanded(
      int parentLength,
      int backLayerLength,
      int backLayerMinimumLength,
      int backLayerExpandedLength,
      int backLayerPosition,
      int contentLayerLength,
      int contentLayerPosition) {
    assertEquals(
        "The dimensions of the Back Layer must always match the dimensions of the parent"
            + " CoordinatorLayout",
        parentLength,
        backLayerLength);
    assertEquals("Position for the backlayer must always be (0,0)", 0, backLayerPosition);
    assertEquals(
        "The sum of the BackLayer Minimum Length and the content Layer length in the moving"
            + " dimension (height in vertical backlayers, width in horizontal backlayers) must"
            + " match the length of the parent CoordinatorLayout, even when expanded.",
        parentLength,
        backLayerMinimumLength + contentLayerLength);
    assertThat(
        "The expanded length of the backlayer must be larger than the minimum length",
        backLayerMinimumLength,
        lessThan(backLayerExpandedLength));
    if (isBackLayerAtEndSide) {
      // The content layer has moved in the negative direction by an amount equal to the difference
      // between backLayerExpandedLength and backLayerMinimumLength, this way exposing a total of
      // backLayerExpandedLength at the end side.
      assertEquals(
          "The content must slide off-screen (negative coordinates) by the difference between the"
              + " Backlayer's minimum and expanded lengths in the moving dimension (height for"
              + " bottom, width for right/end) when the backlayer is expanded and located at the"
              + " bottom or right/end side of the screen. This guarantees that exactly"
              + " backLayerExpandedLength is exposed in this dimension.",
          backLayerMinimumLength - backLayerExpandedLength - 1,
          contentLayerPosition);
    } else {
      // The content layer  is at position backLayerExpandedLength
      assertEquals(
          "The content layer must be at position backLayerExpandedLength in the moving dimension"
              + " (height for top, width for left/start) when the backlayer is expanded and is"
              + " located at the top or left/start of the screen",
          backLayerExpandedLength,
          contentLayerPosition);
    }
  }

  /** Assert that the backlayer is collapsed. */
  protected void assertBackLayerCollapsed() {
    assertFalse("The backlayer is expected to be collapsed at this point.", backLayer.isExpanded());
    if (isHorizontal) {
      // If it's horizontal then the static dimension is height and the moving dimension is width.
      assertStaticDimensionInvariants(
          coordinatorLayout.getHeight() /* parentLength */,
          backLayer.getHeight() /* backLayerLength */,
          (int) backLayer.getY() /* backLayerPosition */,
          contentLayer.getHeight() /* contentLayerLength */,
          (int) contentLayer.getY() /* contentLayerPosition */);
      assertMovingDimensionCollapsed(
          coordinatorLayout.getWidth() /* parentLength */,
          backLayer.getWidth() /* backLayerLength */,
          ViewCompat.getMinimumWidth(backLayer) /* backLayerMinimumLength */,
          (int) backLayer.getX() /* backLayerPosition */,
          contentLayer.getWidth() /* contentLayerLength */,
          (int) contentLayer.getX() /* contentLayerPosition */);
    } else {
      // If it's vertical then the static dimension is width and the moving dimension is height.
      assertStaticDimensionInvariants(
          coordinatorLayout.getWidth() /* parentLength */,
          backLayer.getWidth() /* backLayerLength */,
          (int) backLayer.getX() /* backLayerPosition */,
          contentLayer.getWidth() /* contentLayerLength */,
          (int) contentLayer.getX() /* contentLayerPosition */);
      assertMovingDimensionCollapsed(
          coordinatorLayout.getHeight() /* parentLength */,
          backLayer.getHeight() /* backLayerLength */,
          ViewCompat.getMinimumHeight(backLayer) /* backLayerMinimumLength */,
          (int) backLayer.getY() /* backLayerPosition */,
          contentLayer.getHeight() /* contentLayerLength */,
          (int) contentLayer.getY() /* contentLayerPosition */);
    }
  }

  /** Assert that the backlayer is expanded. */
  protected void assertBackLayerExpanded() {
    assertTrue("The backlayer is expected to be expanded at this point.", backLayer.isExpanded());
    if (isHorizontal) {
      // If it's horizontal then the static dimension is height and the moving dimension is width.
      assertStaticDimensionInvariants(
          coordinatorLayout.getHeight() /* parentLength */,
          backLayer.getHeight() /* backLayerLength */,
          (int) backLayer.getY() /* backLayerPosition */,
          contentLayer.getHeight() /* contentLayerLength */,
          (int) contentLayer.getY() /* contentLayerPosition */);
      assertMovingDimensionExpanded(
          coordinatorLayout.getWidth() /* parentLength */,
          backLayer.getWidth() /* backLayerLength */,
          ViewCompat.getMinimumWidth(backLayer) /* backLayerMinimumLength */,
          backLayer.getExpandedWidth() /*backLayerExpandedLength*/,
          (int) backLayer.getX() /* backLayerPosition */,
          contentLayer.getWidth() /* contentLayerLength */,
          (int) contentLayer.getX() /* contentLayerPosition */);
    } else {
      // If it's vertical then the static dimension is width and the moving dimension is height.
      assertStaticDimensionInvariants(
          coordinatorLayout.getWidth() /* parentLength */,
          backLayer.getWidth() /* backLayerLength */,
          (int) backLayer.getX() /* backLayerPosition */,
          contentLayer.getWidth() /* contentLayerLength */,
          (int) contentLayer.getX() /* contentLayerPosition */);
      assertMovingDimensionExpanded(
          coordinatorLayout.getHeight() /* parentLength */,
          backLayer.getHeight() /* backLayerLength */,
          ViewCompat.getMinimumHeight(backLayer) /* backLayerMinimumLength */,
          backLayer.getExpandedHeight() /*backLayerExpandedLength*/,
          (int) backLayer.getY() /* backLayerPosition */,
          contentLayer.getHeight() /* contentLayerLength */,
          (int) contentLayer.getY() /* contentLayerPosition */);
    }
  }

  protected void assertContentLayerNotObscuring(View view) {
    Rect contentLayerRect = getBoundingRectangleOnScreen(contentLayer);
    Rect viewRect = getBoundingRectangleOnScreen(view);
    assertFalse(
        "The boundaries of the extra content must not intersect"
            + " the boundaries of the content layer",
        Rect.intersects(contentLayerRect, viewRect));
  }

  private Rect getBoundingRectangleOnScreen(View view) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    Rect viewRect =
        new Rect(
            location[0],
            location[1],
            location[0] + view.getWidth(),
            location[1] + view.getHeight());
    return viewRect;
  }

  /**
   * Sets up the necessary members for this test class from a BackLayerLayoutActivity. Call this
   * method from a {@code @Before} method on the subclass.
   */
  public void setUp(
      ActivityTestRule<? extends Activity> activityTestRule,
      @IdRes int coordinatorLayoutId,
      @IdRes int backLayerId,
      @IdRes int contentLayerId,
      @IdRes int primaryButtonId,
      @IdRes int secondaryButtonId,
      @IdRes int primaryExtraContentId,
      @IdRes int secondaryExtraContentId)
      throws Exception {
    this.backLayerId = backLayerId;
    this.contentLayerId = contentLayerId;
    activity = activityTestRule.getActivity();
    resources = activity.getResources();

    coordinatorLayout = activity.findViewById(coordinatorLayoutId);
    backLayer = activity.findViewById(backLayerId);
    contentLayer = activity.findViewById(contentLayerId);
    primaryExtraContent = activity.findViewById(primaryExtraContentId);
    secondaryExtraContent = activity.findViewById(secondaryExtraContentId);

    this.primaryButtonId = primaryButtonId;
    this.secondaryButtonId = secondaryButtonId;
    this.primaryExtraContentId = primaryExtraContentId;
    this.secondaryExtraContentId = secondaryExtraContentId;

    CoordinatorLayout.LayoutParams layoutParams =
        (CoordinatorLayout.LayoutParams) backLayer.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(layoutParams.gravity, ViewCompat.getLayoutDirection(backLayer));
    isBackLayerAtEndSide = absoluteGravity == Gravity.RIGHT || absoluteGravity == Gravity.BOTTOM;
    isHorizontal = absoluteGravity == Gravity.RIGHT || absoluteGravity == Gravity.LEFT;

    if (backLayer.isExpanded()) {
      onView(withId(backLayerId)).perform(collapse());
    }
  }

  public void testTopBackLayerLaidOutCorrectly() {
    assertBackLayerCollapsed();
  }

  public void testExpandingSlidesContentLayerOut() throws InterruptedException {
    assertBackLayerCollapsed();
    onView(withId(backLayerId)).perform(expand());
    assertBackLayerExpanded();
  }

  public void testExpandAndCollapseBackLayer() throws InterruptedException {
    assertBackLayerCollapsed();
    onView(withId(backLayerId)).perform(expand());
    assertBackLayerExpanded();
    onView(withId(backLayerId)).perform(collapse());
    assertBackLayerCollapsed();
  }

  public void testBackLayerCollapsesOnContentLayerClick() throws InterruptedException {
    assertBackLayerCollapsed();
    onView(withId(backLayerId)).perform(expand());
    assertBackLayerExpanded();
    onView(withId(contentLayerId)).perform(simpleClick());
    onView(withId(backLayerId)).perform(waitUntilIdle());
    assertBackLayerCollapsed();
  }

  public void testBackLayerChangesFromOneExperienceToTheOther() throws InterruptedException {
    assertBackLayerCollapsed();
    onView(withId(primaryButtonId)).perform(click());
    onView(withId(backLayerId)).perform(waitUntilIdle());
    assertBackLayerExpanded();
    onView(withId(primaryExtraContentId)).check(matches(isDisplayed()));
    onView(withId(secondaryExtraContentId)).check(matches(not(isDisplayed())));
    assertContentLayerNotObscuring(primaryExtraContent);
    onView(withId(secondaryButtonId)).perform(click());
    onView(withId(backLayerId)).perform(waitUntilIdle());
    assertBackLayerExpanded();
    onView(withId(primaryExtraContentId)).check(matches(not(isDisplayed())));
    onView(withId(secondaryExtraContentId)).check(matches(isDisplayed()));
    assertContentLayerNotObscuring(secondaryExtraContent);
    onView(withId(primaryButtonId)).perform(click());
    onView(withId(backLayerId)).perform(waitUntilIdle());
    assertBackLayerExpanded();
    onView(withId(primaryExtraContentId)).check(matches(isDisplayed()));
    onView(withId(secondaryExtraContentId)).check(matches(not(isDisplayed())));
    assertContentLayerNotObscuring(primaryExtraContent);
  }
}
