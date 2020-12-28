/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

class ViewOverlayApi14 implements ViewOverlayImpl {

  /**
   * The actual container for the drawables (and views, if it's a ViewGroupOverlay). All of the
   * management and rendering details for the overlay are handled in OverlayViewGroup.
   */
  protected OverlayViewGroup overlayViewGroup;

  ViewOverlayApi14(Context context, ViewGroup hostView, View requestingView) {
    overlayViewGroup = new OverlayViewGroup(context, hostView, requestingView, this);
  }

  static ViewOverlayApi14 createFrom(View view) {
    ViewGroup contentView = ViewUtils.getContentView(view);
    if (contentView != null) {
      final int numChildren = contentView.getChildCount();
      for (int i = 0; i < numChildren; ++i) {
        View child = contentView.getChildAt(i);
        if (child instanceof OverlayViewGroup) {
          return ((OverlayViewGroup) child).viewOverlay;
        }
      }
      return new ViewGroupOverlayApi14(contentView.getContext(), contentView, view);
    }
    return null;
  }

  @Override
  public void add(@NonNull Drawable drawable) {
    overlayViewGroup.add(drawable);
  }

  @Override
  public void remove(@NonNull Drawable drawable) {
    overlayViewGroup.remove(drawable);
  }

  /**
   * OverlayViewGroup is a container that View and ViewGroup use to host drawables and views added
   * to their overlays ({@code ViewOverlay} and {@code ViewGroupOverlay}, respectively). Drawables
   * are added to the overlay via the add/remove methods in ViewOverlay, Views are added/removed via
   * ViewGroupOverlay. These drawable and view objects are drawn whenever the view itself is drawn;
   * first the view draws its own content (and children, if it is a ViewGroup), then it draws its
   * overlay (if it has one).
   *
   * <p>Besides managing and drawing the list of drawables, this class serves two purposes: (1) it
   * noops layout calls because children are absolutely positioned and (2) it forwards all
   * invalidation calls to its host view. The invalidation redirect is necessary because the overlay
   * is not a child of the host view and invalidation cannot therefore follow the normal path up
   * through the parent hierarchy.
   *
   * @see View#getOverlay()
   * @see ViewGroup#getOverlay()
   */
  @SuppressLint({"ViewConstructor", "PrivateApi"})
  static class OverlayViewGroup extends ViewGroup {

    static Method invalidateChildInParentFastMethod;

    static {
      try {
        //noinspection JavaReflectionMemberAccess
        invalidateChildInParentFastMethod =
            ViewGroup.class.getDeclaredMethod(
                "invalidateChildInParentFast", int.class, int.class, Rect.class);
      } catch (NoSuchMethodException ignored) {
        // Ignore exception if method does not exist
      }
    }

    /**
     * The View for which this is an overlay. Invalidations of the overlay are redirected to this
     * host view.
     */
    ViewGroup hostView;

    View requestingView;
    /** The set of drawables to draw when the overlay is rendered. */
    ArrayList<Drawable> drawables = null;
    /** Reference to the hosting overlay object */
    ViewOverlayApi14 viewOverlay;

    private boolean disposed;

    OverlayViewGroup(
        Context context, ViewGroup hostView, View requestingView, ViewOverlayApi14 viewOverlay) {
      super(context);
      this.hostView = hostView;
      this.requestingView = requestingView;
      setRight(hostView.getWidth());
      setBottom(hostView.getHeight());
      hostView.addView(this);
      this.viewOverlay = viewOverlay;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
      // Intercept and noop all touch events - overlays do not allow touch events
      return false;
    }

    @SuppressWarnings("deprecation")
    public void add(Drawable drawable) {
      assertNotDisposed();
      if (drawables == null) {

        drawables = new ArrayList<>();
      }
      if (!drawables.contains(drawable)) {
        // Make each drawable unique in the overlay; can't add it more than once
        drawables.add(drawable);
        invalidate(drawable.getBounds());
        drawable.setCallback(this);
      }
    }

    @SuppressWarnings("deprecation")
    public void remove(Drawable drawable) {
      if (drawables != null) {
        drawables.remove(drawable);
        invalidate(drawable.getBounds());
        drawable.setCallback(null);
        disposeIfEmpty();
      }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
      return super.verifyDrawable(who) || (drawables != null && drawables.contains(who));
    }

    public void add(View child) {
      assertNotDisposed();
      if (child.getParent() instanceof ViewGroup) {
        ViewGroup parent = (ViewGroup) child.getParent();
        if (parent != hostView
            && parent.getParent() != null
            && ViewCompat.isAttachedToWindow(parent)) {
          // Moving to different container; figure out how to position child such that
          // it is in the same location on the screen
          int[] parentLocation = new int[2];
          int[] hostViewLocation = new int[2];
          parent.getLocationOnScreen(parentLocation);
          hostView.getLocationOnScreen(hostViewLocation);
          ViewCompat.offsetLeftAndRight(child, parentLocation[0] - hostViewLocation[0]);
          ViewCompat.offsetTopAndBottom(child, parentLocation[1] - hostViewLocation[1]);
        }
        parent.removeView(child);
        //                if (parent.getLayoutTransition() != null) {
        //                    // LayoutTransition will cause the child to delay removal - cancel it
        //                    parent.getLayoutTransition().cancel(LayoutTransition.DISAPPEARING);
        //                }
        // fail-safe if view is still attached for any reason
        if (child.getParent() != null) {
          parent.removeView(child);
        }
      }
      super.addView(child);
    }

    public void remove(View view) {
      super.removeView(view);
      disposeIfEmpty();
    }

    private void assertNotDisposed() {
      if (disposed) {
        throw new IllegalStateException(
            "This overlay was disposed already. "
                + "Please use a new one via ViewGroupUtils.getOverlay()");
      }
    }

    private void disposeIfEmpty() {
      if (getChildCount() == 0 && (drawables == null || drawables.size() == 0)) {
        disposed = true;
        hostView.removeView(this);
      }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
      invalidate(drawable.getBounds());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
      int[] contentViewLocation = new int[2];
      int[] hostViewLocation = new int[2];
      hostView.getLocationOnScreen(contentViewLocation);
      requestingView.getLocationOnScreen(hostViewLocation);
      canvas.translate(
          hostViewLocation[0] - contentViewLocation[0],
          hostViewLocation[1] - contentViewLocation[1]);
      canvas.clipRect(new Rect(0, 0, requestingView.getWidth(), requestingView.getHeight()));
      super.dispatchDraw(canvas);
      final int numDrawables = (drawables == null) ? 0 : drawables.size();
      for (int i = 0; i < numDrawables; ++i) {
        drawables.get(i).draw(canvas);
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      // Noop: children are positioned absolutely
    }

    /*
    The following invalidation overrides exist for the purpose of redirecting invalidation to
    the host view. The overlay is not parented to the host view (since a View cannot be a
    parent), so the invalidation cannot proceed through the normal parent hierarchy.
    There is a built-in assumption that the overlay exactly covers the host view, therefore
    the invalidation rectangles received do not need to be adjusted when forwarded to
    the host view.
    */

    private void getOffset(int[] offset) {
      int[] contentViewLocation = new int[2];
      int[] hostViewLocation = new int[2];
      hostView.getLocationOnScreen(contentViewLocation);
      requestingView.getLocationOnScreen(hostViewLocation);
      offset[0] = hostViewLocation[0] - contentViewLocation[0];
      offset[1] = hostViewLocation[1] - contentViewLocation[1];
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected ViewParent invalidateChildInParentFast(int left, int top, Rect dirty) {
      if (hostView != null && invalidateChildInParentFastMethod != null) {
        try {
          int[] offset = new int[2];
          getOffset(offset);
          invalidateChildInParentFastMethod.invoke(hostView, left, top, dirty);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
      if (hostView != null) {
        dirty.offset(location[0], location[1]);
        if (hostView != null) {
          location[0] = 0;
          location[1] = 0;
          int[] offset = new int[2];
          getOffset(offset);
          dirty.offset(offset[0], offset[1]);
          return super.invalidateChildInParent(location, dirty);
          //                    return hostView.invalidateChildInParent(location, dirty);
        } else {
          invalidate(dirty);
        }
      }
      return null;
    }
  }
}
