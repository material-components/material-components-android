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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.stateful.ExtendableSavedState;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.expandable.ExpandableWidget;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link ViewGroup} that can be expanded to show more content.
 *
 * <p>Since its resting state is hidden (partially exposed), it keeps a copy of its original
 * dimensions.
 *
 * <p>Notice BackLayerLayout is a LinearLayout, so you need to make sure you're using the correct
 * orientation that matches the position you've chosen for the back layer (i.e. use {@code
 * android:orientation="vertical"} in conjunction with {@code android:gravity="top"} or {@code
 * android:gravity="bottom"}).
 *
 * <p><b>Usage guide:</b>
 *
 * <ul>
 *   <li>It must be a direct child of {@link CoordinatorLayout}
 *   <li>There has to be <b>exactly ONE</b> other direct child of the same CoordinatorLayout that
 *       uses {@link BackLayerSiblingBehavior} as its behavior (set {@code
 *       app:layout_behavior="@string/design_backlayer_sibling_behavior"}). This is the content
 *       layer. Clicks on the content layer while the back layer is exposed will cause the back
 *       layer to collapse.
 *   <li>The {@code BackLayerLayout} can contain an arbitrary number of subviews, however <b>exactly
 *       ONE</b> of them must be a {@link CollapsedBackLayerContents}, anything inside this view
 *       will be considered the contents of the back layer that will always be visible. All other
 *       views will be extra content under the content layer. You can support multiple experiences
 *       under the back layer by changing the visibility or swapping out these other views.
 *   <li>You must use match_parent for the {@code BackLayerLayout}'s width and height.
 *   <li>Set both {@code android:gravity} and {@code android:layout_gravity} for the {@code
 *       BackLayerLayout} to the same value. This value is the edge to which the back layer is
 *       attached and can be any of {@code left}, {@code start}, {@code left|start}, {@code top},
 *       {@code right}, {@code right|end}, {@code end}, {@code bottom}.
 *   <li>Set {@code BackLayerLayout}'s {@code android:orientation} to {@code vertical} or {@code
 *       horizontal} matching the gravity ({@code vertical} for gravities {@code top} or {@code
 *       bottom}, otherwise use {@code horizontal}).
 *   <li>Add UI elements and behavior to expose the back layer. {@code BackLayerLayout} does not try
 *       to be smart about when to expand, so you must add UI to expand the back layer (using an
 *       OnClickListener on a button, for example). {@code BackLayerLayout} offers a {@link
 *       #setExpanded(boolean)} method that you can call in response to clicks or other events.
 *   <li>Add {@link BackLayerCallback}s using {@link #addBackLayerCallback(BackLayerCallback)} in
 *       order to listen to changes in the back layer's status. This also may be useful if your back
 *       layer needs extra animations, you could use {@link BackLayerCallback#onBeforeExpand()} and
 *       {@link BackLayerCallback#onBeforeCollapse()} for this purpose.
 *   <li>If you {@link BackLayerCallback} at all you probably need to implement{@link
 *       BackLayerCallback#onRestoringExpandedBackLayer()}. This method must not use any animations
 *       while replicating the effects of calling {@link BackLayerCallback#onBeforeExpand()}
 *       followed by {@link BackLayerCallback#onAfterExpand()}. When restoring the expanded status
 *       on activity restarts, no animation will be used and thus {@link
 *       BackLayerCallback#onBeforeExpand()} and {@link BackLayerCallback#onAfterExpand()} will not
 *       be called.
 *   <li>You MUST NOT use a {@link ViewGroup.OnHierarchyChangedListener} on the back layer as it is
 *       used for internal housekeeping.
 * </ul>
 *
 * <pre>{@code
 * <CoordinatorLayout ...>
 *   <BackLayerLayout
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"
 *       android:layout_gravity="top"
 *       android:gravity="top"
 *       android:orientation="vertical">
 *     <CollapsedBackLayerContents
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content">
 *       <include layout="@layout/always_visible_content"/>
 *     </CollapsedBackLayerContents>
 *     <include layout="@layout/default_content_hidden_behind_content_layer"/>
 *     <include
 *         layout="@layout/secondary_content_hidden_behind_content_layer"
 *         android:visibility="GONE"/>
 *   </BackLayerLayout>
 *   <YourContentLayerView
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"
 *       app:layout_behavior="@string/design_backlayer_sibling_behavior"/>
 * </CoordinatorLayout>
 * }</pre>
 *
 * The reason you need to specify both {@code android:gravity} and {@code android:layout_gravity}
 * and they must match is that they are used for different purposes:
 *
 * <ul>
 *   <li>{@code layout_gravity} is used to specify to the {@link BackLayerSiblingBehavior} what edge
 *       the back layer is anchored to. {@code layout_gravity} is used by {@code BackLayerLayout} to
 *       corectly measure its expanded state (setting the moving dimension's {@link MeasureSpec} to
 *       {@link MeasureSpec#AT_MOST}). {@code layout_gravity} is also used by the {@link
 *       BackLayerSiblingBehavior} to measure and lay out the content layer view to cover the area
 *       of the back layer that does not contain the {@link CollapsedBackLayerContents} (when the
 *       back layer is collapsed).
 *   <li>{@code gravity} is used to have the contents of the back layer gravitate to the same edge,
 *       see {@link LinearLayout#setGravity(int)} for more information on this.
 * </ul>
 */
// Implementation detail ahead, since it's not relevant to the user this has been pulled out of the
// Javadoc:
// Considering the usages for gravity and layout_gravity spelled out above, and that both values
// must match in order for the BackLayerLayout to work correctly, we thought of ways to depend only
// on one of those two values. We decided to attempt to remove the dependency on layout_gravity for
// the following reasons:
// 1. The way we use gravity to have the contents of the back layer gravitate to the correct edge is
// actually implemented in LinearLayout and the dependence on gravity is deeply ingrained in this
// code, it would be prohibitively hard to rework layout_gravity for this purpose.
// 2. It is not recommended for widgets themselves to depend on LayoutParams, and we would only
// worsen the situation adding another dependency on layout_gravity. While it is true that
// BackLayerLayout is only supported while used inside a CoordinatorLayout and it is already tightly
// coupled to it, it seems backwards to try to retrofit this into code that comes from the
// superclass.
//
// When trying to depend only on gravity we found the following two issues:
// 1. LinearLayout does not expose getGravity() prior to API  24, that is solvable through
// reflection (and it would likely work in all devices though that is not guaranteed).
// 2. LinearLayout does not just take an edge gravity (like top, left, right....), if it is an edge
// gravity it forces it to become a corner gravity (top|start, for example). This is problematic
// because BackLayerLayout and BackLayerSiblingBehavior constantly check the edge gravity to do the
// following:
//   - Measure the expanded content of the back layer.
//   - Measure and layout the content layer.
//   - Slide the content layer out of view when the back layer is expanded.
// All of these operations depend on having an edge gravity instead of a corner gravity, so short of
// rewriting the relevant parts of LinearLayout in BackLayerLayout, using the same gravity value
// that LinearLayout depends on is not an option for BackLayerLayout and BackLayerSiblingBehavior.
public class BackLayerLayout extends LinearLayout implements ExpandableWidget {

  public static final String EXPANDED_STATE_KEY = "expanded";
  public static final String BACK_LAYER_LAYOUT_STATE_KEY = "BackLayerLayout";
  private final List<BackLayerCallback> callbacks = new CopyOnWriteArrayList<>();
  private boolean expanded = false;
  private int expandedHeight;
  private int expandedWidth;
  private boolean measuredCollapsedSize = false;
  private boolean originalMeasureSpecsSaved = false;
  private int originalHeightMeasureSpec;
  private int originalWidthMeasureSpec;
  private BackLayerSiblingBehavior sibling = null;
  private ChildViewAccessibilityHelper childViewAccessibilityHelper;

  public BackLayerLayout(@NonNull Context context) {
    super(context);
  }

  public BackLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    childViewAccessibilityHelper = new ChildViewAccessibilityHelper(this);
    setOnHierarchyChangeListener(childViewAccessibilityHelper);
    childViewAccessibilityHelper.disableChildFocus();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (!originalMeasureSpecsSaved) {
      originalWidthMeasureSpec = widthMeasureSpec;
      originalHeightMeasureSpec = heightMeasureSpec;
      originalMeasureSpecsSaved = true;
    }
    if (!measuredCollapsedSize) {
      // Measure the minimum size only if it's not previously set, for example in XML layout.
      if (ViewCompat.getMinimumHeight(this) == 0 && ViewCompat.getMinimumWidth(this) == 0) {
        // Find the CollapsedBackLayerContents
        boolean foundCollapsed = false;
        for (int i = 0; i < getChildCount(); i++) {
          View child = getChildAt(i);
          if (child instanceof CollapsedBackLayerContents) {
            if (foundCollapsed) {
              throw new IllegalStateException(
                  "More than one CollapsedBackLayerContents found inside BackLayerLayout");
            }
            foundCollapsed = true;
            LinearLayout.LayoutParams childLayoutParams =
                (LinearLayout.LayoutParams) child.getLayoutParams();
            child.measure(childLayoutParams.width, childLayoutParams.height);
            setMinimumHeight(
                child.getMeasuredHeight()
                    + childLayoutParams.bottomMargin
                    + childLayoutParams.topMargin);
            setMinimumWidth(
                child.getMeasuredWidth()
                    + childLayoutParams.leftMargin
                    + childLayoutParams.rightMargin);
          }
        }
        if (!foundCollapsed) {
          throw new IllegalStateException(
              "No CollapsedBackLayerContents found inside BackLayerLayout");
        }
      }
      measuredCollapsedSize = true;
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  /** Returns true if the initial measurement has already been done. */
  boolean hasMeasuredCollapsedSize() {
    return measuredCollapsedSize;
  }

  /** Add a new {@link BackLayerCallback} to listen to back layer events. */
  public void addBackLayerCallback(BackLayerCallback callback) {
    if (!callbacks.contains(callback)) {
      callbacks.add(callback);
    }
  }

  /** Stop {@code callback} from listening to future back layer events. */
  public void removeBackLayerCallback(BackLayerCallback callback) {
    callbacks.remove(callback);
  }

  void setSibling(BackLayerSiblingBehavior sibling) {
    this.sibling = sibling;
  }

  /**
   * Expands or collapses the back layer.
   *
   * <p>Notice that this method does not automatically change visibility on child views of the back
   * layer, the developer has to prepare the contents of the back layer either before calling this
   * method or in a {@link BackLayerCallback#onBeforeExpand()}/{@link
   * BackLayerCallback#onBeforeCollapse()}.
   */
  @Override
  public boolean setExpanded(boolean expanded) {
    if (this.expanded == expanded) {
      return expanded;
    }
    if (expanded) {
      for (BackLayerCallback callback : callbacks) {
        callback.onBeforeExpand();
      }
      measureExpanded();
      // Call the sibling's behavior onBeforeExpand to animate the expansion (if necessary) and,
      // after animation is done, call the onAfterExpand() callbacks.
      if (sibling != null) {
        sibling.onBeforeExpand();
      }
      this.expanded = true;
    } else {
      for (BackLayerCallback callback : callbacks) {
        callback.onBeforeCollapse();
      }
      sibling.onBeforeCollapse();
      this.expanded = false;
    }
    return this.expanded;
  }

  /**
   * Measures the expanded version of the back layer by measuring with one dimension set to
   * MeasureSpec.UNSPECIFIED and then undoing the changes by remeasuring with the original
   * configuration.
   */
  void measureExpanded() {
    CoordinatorLayout.LayoutParams layoutParams =
        (CoordinatorLayout.LayoutParams) getLayoutParams();
    final int absoluteGravity =
        Gravity.getAbsoluteGravity(layoutParams.gravity, ViewCompat.getLayoutDirection(this));
    int heightMeasureSpec = originalHeightMeasureSpec;
    int widthMeasureSpec = originalWidthMeasureSpec;
    // In order to know the measurements for a expanded version of the back layer we need to
    // measure the back layer with one dimension set to MeasureSpec.UNSPECIFIED instead of the
    // setting
    // that came in the original MeasureSpec (MeasureSpec.EXACTLY, since the BackLayerLayout must
    // use match_parent for both dimensions).
    //
    // While it would seem natural to use MeasureSpec.AT_MOST, this method can be called from
    // onRestoreInstanceState(Parcelable) which would happen before the first measure pass, and thus
    // the original measure specs would be 0, causing a wrong measurement.
    switch (absoluteGravity) {
      case Gravity.LEFT:
      case Gravity.RIGHT:
        widthMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.UNSPECIFIED);
        break;
      case Gravity.TOP:
      case Gravity.BOTTOM:
        int size = MeasureSpec.getSize(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.UNSPECIFIED);
        break;
      default:
        break;
    }
    measure(widthMeasureSpec, heightMeasureSpec);
    expandedHeight = getMeasuredHeight();
    expandedWidth = getMeasuredWidth();
    // Recalculate with the original measure specs, so it fits the entire coordinator layout.
    measure(originalWidthMeasureSpec, originalHeightMeasureSpec);
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    ExtendableSavedState state = new ExtendableSavedState(superState);
    Bundle bundle = new Bundle();
    bundle.putBoolean(EXPANDED_STATE_KEY, expanded);
    state.extendableStates.put(BACK_LAYER_LAYOUT_STATE_KEY, bundle);
    return state;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof ExtendableSavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    ExtendableSavedState ess = (ExtendableSavedState) state;
    super.onRestoreInstanceState(ess.getSuperState());

    Bundle bundle = ess.extendableStates.get(BACK_LAYER_LAYOUT_STATE_KEY);
    if (bundle != null) {
      this.expanded = bundle.getBoolean(EXPANDED_STATE_KEY);
    }
    if (expanded) {
      for (BackLayerCallback callback : callbacks) {
        callback.onRestoringExpandedBackLayer();
      }
      measureExpanded();
    }
  }

  /** Called by the BackLayerSiblingBehavior when the expand animation is done. */
  void onExpandAnimationDone() {
    for (BackLayerCallback callback : callbacks) {
      callback.onAfterExpand();
    }
  }

  /** Called by the BackLayerSiblingBehavior when the collapse animation is done. */
  void onCollapseAnimationDone() {
    childViewAccessibilityHelper.disableChildFocus();
    for (BackLayerCallback callback : callbacks) {
      callback.onAfterCollapse();
    }
  }

  @Override
  public boolean isExpanded() {
    return expanded;
  }

  /** The measured height for the expanded version of the back layer. */
  int getExpandedHeight() {
    return expandedHeight;
  }

  /** The measured width for the expanded version of the back layer. */
  int getExpandedWidth() {
    return expandedWidth;
  }
}
