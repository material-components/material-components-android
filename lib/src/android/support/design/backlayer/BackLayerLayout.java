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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A ViewGroup that can be expanded to show more content.
 *
 * <p>Since its resting state is hidden (partially exposed), it keeps a copy of its original
 * dimensions.
 *
 * <p><b>Usage guide:</b>
 *
 * <ul>
 *   <li>It must be a direct child of {@link CoordinatorLayout}
 *   <li>There has to be <b>exactly ONE</b> other direct child of the same CoordinatorLayout that
 *       uses {@link BackLayerSiblingBehavior} as its behavior. This is the content layer. Clicks on
 *       the content layer while the BackLayer is exposed will cause the backlayer to collapse.
 *   <li>You must use match_parent for the BackLayerLayout's width and height
 *   <li>There must be a <b>exactly ONE</b> child view of the BackLayerLayout which is {@link
 *       CollapsedBackLayerContents}, anything inside this view will be considered the contents of
 *       the backlayer that will always be visible. You can play with extra content in the backlayer
 *       by changing visibilities of views outside CollapsedBackLayerContents.
 *   <li>Add UI to expose the backlayer. BackLayerLayout does not try to be smart about when to
 *       expand, so you must add UI to expand it (a button? OnClickListener?). BackLayerLayout
 *       offers a {@link #expand()} and {@link #collapse()} method for you to write your own logic.
 *   <li>Add {@link BackLayerCallback}s using {@link #addBackLayerCallback(BackLayerCallback)} in
 *       order to listen to changes in the backlayer's status. This also may be useful if your
 *       backlayer needs extra animations, you could use {@link BackLayerCallback#onBeforeExpand()}
 *       and {@link BackLayerCallback#onBeforeCollapse()} for this purpose.
 * </ul>
 *
 * <pre>{@code
 * <CoordinatorLayout ...>
 *   <BackLayerLayout
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent">
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
 */
public class BackLayerLayout extends RelativeLayout {

  private final List<BackLayerCallback> callbacks = new CopyOnWriteArrayList<>();
  private boolean expanded = false;
  private int expandedHeight;
  private int expandedWidth;
  private boolean measuredCollapsedSize = false;
  private boolean originalMeasureSpecsSaved = false;
  private int originalHeightMeasureSpec;
  private int originalWidthMeasureSpec;
  private BackLayerSiblingBehavior sibling = null;

  public BackLayerLayout(@NonNull Context context) {
    super(context);
  }

  public BackLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
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
            RelativeLayout.LayoutParams childLayoutParams =
                (RelativeLayout.LayoutParams) child.getLayoutParams();
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

  /** Add a new {@link BackLayerCallback} to listen to backlayer events. */
  public void addBackLayerCallback(BackLayerCallback callback) {
    if (!callbacks.contains(callback)) {
      callbacks.add(callback);
    }
  }

  /** Stop {@code callback} from listening to future backlayer events. */
  public void removeBackLayerCallback(BackLayerCallback callback) {
    callbacks.remove(callback);
  }

  void setSibling(BackLayerSiblingBehavior sibling) {
    this.sibling = sibling;
  }

  /**
   * Expand the back layer.
   *
   * <p>Notice that this method does not automatically change visibilities on child views of the
   * back layer, all of that has to be done by your code before calling this method or in a {@link
   * BackLayerCallback}.
   */
  public void expand() {
    if (!expanded) {
      for (BackLayerCallback callback : callbacks) {
        callback.onBeforeExpand();
      }
      CoordinatorLayout.LayoutParams layoutParams =
          (CoordinatorLayout.LayoutParams) getLayoutParams();
      final int absoluteGravity =
          Gravity.getAbsoluteGravity(layoutParams.gravity, getLayoutDirection());
      int heightMeasureSpec = originalHeightMeasureSpec;
      int widthMeasureSpec = originalWidthMeasureSpec;
      // Original measure specs are meant to be both match_parent, so we need to remeasure with one
      // of them being turned into at most parent size, so we can calculate what the expanded size
      // should be. This is stored in expandedHeight and expandedWidth.
      switch (absoluteGravity) {
        case Gravity.LEFT:
        case Gravity.RIGHT:
          widthMeasureSpec =
              MeasureSpec.makeMeasureSpec(
                  MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
          break;
        case Gravity.TOP:
        case Gravity.BOTTOM:
          int size = MeasureSpec.getSize(heightMeasureSpec);
          heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
          break;
      }
      measure(widthMeasureSpec, heightMeasureSpec);
      expandedHeight = getMeasuredHeight();
      expandedWidth = getMeasuredWidth();
      // Recalculate with the original measure specs, so it fits the entire coordinator layout.
      measure(originalWidthMeasureSpec, originalHeightMeasureSpec);
      sibling.onBeforeExpand();
    }
    expanded = true;
  }

  /** Called by the BackLayerSiblingBehavior when the expand animation is done. */
  void onExpandAnimationDone() {
    for (BackLayerCallback callback : callbacks) {
      callback.onAfterExpand();
    }
  }

  /**
   * Collapse the back layer.
   *
   * <p>Notice that this method does not automatically change visibilities on child views of the
   * back layer, all of that has to be done by your code before calling this method or in a {@link
   * BackLayerCallback}.
   */
  public void collapse() {
    if (expanded) {
      for (BackLayerCallback callback : callbacks) {
        callback.onBeforeCollapse();
      }
      sibling.onBeforeCollapse();
    }
    expanded = false;
  }

  void onCollapseAnimationDone() {
    for (BackLayerCallback callback : callbacks) {
      callback.onAfterCollapse();
    }
  }

  public boolean isExpanded() {
    return expanded;
  }

  /** The measured height for the expanded version of the backlayer. */
  int getExpandedHeight() {
    return expandedHeight;
  }

  /** The measured width for the expanded version of the backlayer. */
  int getExpandedWidth() {
    return expandedWidth;
  }
}
