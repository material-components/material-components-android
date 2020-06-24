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
package io.material.catalog.draggable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.customview.widget.ViewDragHelper;
import androidx.customview.widget.ViewDragHelper.Callback;
import java.util.ArrayList;
import java.util.List;

/** A CoordinatorLayout whose children can be dragged. */
public class DraggableCoordinatorLayout extends CoordinatorLayout {

  /** A listener to use when a child view is being dragged */
  public interface ViewDragListener {
    void onViewCaptured(@NonNull View view, int i);
    void onViewReleased(@NonNull View view, float v, float v1);
  }

  private final ViewDragHelper viewDragHelper;
  private final List<View> draggableChildren = new ArrayList<>();
  private ViewDragListener viewDragListener;

  public DraggableCoordinatorLayout(Context context) {
    this(context, null);
  }

  public DraggableCoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    viewDragHelper = ViewDragHelper.create(this, dragCallback);
  }

  public void addDraggableChild(View child) {
    if (child.getParent() != this) {
      throw new IllegalArgumentException();
    }
    draggableChildren.add(child);
  }

  public void removeDraggableChild(View child) {
    if (child.getParent() != this) {
      throw new IllegalArgumentException();
    }
    draggableChildren.remove(child);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return viewDragHelper.shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    viewDragHelper.processTouchEvent(ev);
    return super.onTouchEvent(ev);
  }

  private final Callback dragCallback =
      new Callback() {
        @Override
        public boolean tryCaptureView(View view, int i) {
          return view.getVisibility() == VISIBLE && viewIsDraggableChild(view);
        }

        @Override
        public void onViewCaptured(@NonNull View view, int i) {
          if (viewDragListener != null) {
            viewDragListener.onViewCaptured(view, i);
          }
        }

        @Override
        public void onViewReleased(@NonNull View view, float v, float v1) {
          if (viewDragListener != null) {
            viewDragListener.onViewReleased(view, v, v1);
          }
        }

        @Override
        public int getViewHorizontalDragRange(View view) {
          return view.getWidth();
        }

        @Override
        public int getViewVerticalDragRange(View view) {
          return view.getHeight();
        }

        @Override
        public int clampViewPositionHorizontal(View view, int left, int dx) {
          return left;
        }

        @Override
        public int clampViewPositionVertical(View view, int top, int dy) {
          return top;
        }
      };

  private boolean viewIsDraggableChild(View view) {
    return draggableChildren.isEmpty() || draggableChildren.contains(view);
  }

  public void setViewDragListener(
      ViewDragListener viewDragListener) {
    this.viewDragListener = viewDragListener;
  }
}
