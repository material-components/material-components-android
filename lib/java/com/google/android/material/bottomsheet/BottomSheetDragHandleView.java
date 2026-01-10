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

package com.google.android.material.bottomsheet;

import com.google.android.material.R;

import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;

/**
 * A drag handle view that can be added to bottom sheets associated with {@link
 * BottomSheetBehavior}. This view will automatically handle the accessibility interaction when the
 * accessibility service is enabled. When you add a drag handle to a bottom sheet and the user
 * enables the accessibility service, the drag handle will become important for accessibility and
 * clickable. Clicking the drag handle will toggle the bottom sheet between its collapsed,
 * half expanded, and expanded states.
 */
public class BottomSheetDragHandleView extends AppCompatImageView implements
    AccessibilityStateChangeListener {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_BottomSheet_DragHandle;

  @Nullable private final AccessibilityManager accessibilityManager;

  @Nullable private BottomSheetBehavior<?> bottomSheetBehavior;

  private final GestureDetector gestureDetector;

  private boolean clickToExpand;

  /**
   * Track whether clients have set their own touch or click listeners on the drag handle.
   *
   * Setting a custom touch or click listener will override the default behavior of cycling through
   * bottom sheet states when tapped and dismissing the sheet when double tapped. Clients can
   * restore this behavior by setting their touch and click listeners back to null.
   */
  private boolean hasTouchListener = false;
  private boolean hasClickListener = false;

  private final String clickToExpandActionLabel =
      getResources().getString(R.string.bottomsheet_action_expand_description);
  private final String clickToHalfExpandActionLabel =
      getResources().getString(R.string.bottomsheet_action_half_expand_description);
  private final String clickToCollapseActionLabel =
      getResources().getString(R.string.bottomsheet_action_collapse_description);

  private final BottomSheetCallback bottomSheetCallback =
      new BottomSheetCallback() {
        @Override
        public void onStateChanged(
            @NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
          onBottomSheetStateChanged(newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
      };

  /**
   * A gesture listener that handles both single and double taps on the drag handle.
   *
   * Single taps cycle through the available states of the bottom sheet. A double tap hides
   * the sheet.
   */
  private final OnGestureListener gestureListener = new SimpleOnGestureListener() {

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
      return isClickable();
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
      return expandOrCollapseBottomSheetIfPossible();
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
      if (bottomSheetBehavior != null && bottomSheetBehavior.isHideable()) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        return true;
      }
      return super.onDoubleTap(e);
    }
  };

  public BottomSheetDragHandleView(@NonNull Context context) {
    this(context, /* attrs= */ null);
  }

  public BottomSheetDragHandleView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomSheetDragHandleStyle);
  }

  @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate
  public BottomSheetDragHandleView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    // Override the provided context with the wrapped one to prevent it from being used.
    context = getContext();

    gestureDetector =
        new GestureDetector(context, gestureListener, new Handler(Looper.getMainLooper()));

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    ViewCompat.setAccessibilityDelegate(
        this,
        new AccessibilityDelegateCompat() {
          @Override
          public void onPopulateAccessibilityEvent(View host, @NonNull AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
            if (event.getEventType() == TYPE_VIEW_CLICKED) {
              expandOrCollapseBottomSheetIfPossible();
            }
          }

          @Override
          public void onInitializeAccessibilityNodeInfo(
              @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            if (!hasAttachedBehavior()) {
              return;
            }

            CharSequence originalDescription = getContentDescription();
            String stateName = null;

            switch (bottomSheetBehavior.getState()) {
              case BottomSheetBehavior.STATE_COLLAPSED:
                stateName = getResources().getString(R.string.bottomsheet_state_collapsed);
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                stateName = getResources().getString(R.string.bottomsheet_state_expanded);
                break;
              case BottomSheetBehavior.STATE_HALF_EXPANDED:
                stateName = getResources().getString(R.string.bottomsheet_state_half_expanded);
                break;
              default: // fall out
            }

            if (!TextUtils.isEmpty(stateName)) {
              CharSequence newDescription =
                  TextUtils.isEmpty(originalDescription)
                      ? stateName
                      : stateName + ". " + originalDescription;
              info.setContentDescription(newDescription);
            }
          }
        });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    setBottomSheetBehavior(findParentBottomSheetBehavior());
    if (accessibilityManager != null) {
      accessibilityManager.addAccessibilityStateChangeListener(this);
      onAccessibilityStateChanged(accessibilityManager.isEnabled());
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    if (accessibilityManager != null) {
      accessibilityManager.removeAccessibilityStateChangeListener(this);
    }
    setBottomSheetBehavior(null);
    super.onDetachedFromWindow();
  }

  @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (hasClickListener || hasTouchListener) {
      // If clients have set their own click or touch listeners, do nothing.
      return super.onTouchEvent(event);
    }

    return gestureDetector.onTouchEvent(event);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void setOnTouchListener(OnTouchListener l) {
    hasTouchListener = l != null;
    super.setOnTouchListener(l);
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener l) {
    hasClickListener = l != null;
    super.setOnClickListener(l);
  }

  @Override
  public void onAccessibilityStateChanged(boolean enabled) {
    // Do nothing.
  }

  private void setBottomSheetBehavior(@Nullable BottomSheetBehavior<?> behavior) {
    if (bottomSheetBehavior != null) {
      bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback);
      bottomSheetBehavior.setAccessibilityDelegateView(null);
      bottomSheetBehavior.setDragHandleView(null);
    }
    bottomSheetBehavior = behavior;
    if (bottomSheetBehavior != null) {
      bottomSheetBehavior.setAccessibilityDelegateView(this);
      bottomSheetBehavior.setDragHandleView(this);
      onBottomSheetStateChanged(bottomSheetBehavior.getState());
      bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
    }
    setClickable(hasAttachedBehavior());
  }

  private void onBottomSheetStateChanged(@BottomSheetBehavior.State int state) {
    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
      clickToExpand = true;
    } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
      clickToExpand = false;
    } // Else keep the original settings
    int nextState = getNextState();
    String text = null;
    switch (nextState) {
      case BottomSheetBehavior.STATE_COLLAPSED:
        text = clickToCollapseActionLabel;
        break;
      case BottomSheetBehavior.STATE_EXPANDED:
        text = clickToExpandActionLabel;
        break;
      case BottomSheetBehavior.STATE_HALF_EXPANDED:
        text = clickToHalfExpandActionLabel;
        break;
      default: // fall out
    }
    ViewCompat.replaceAccessibilityAction(
        this,
        AccessibilityActionCompat.ACTION_CLICK,
        text,
        (v, args) -> expandOrCollapseBottomSheetIfPossible());
  }

  private boolean hasAttachedBehavior() {
    return bottomSheetBehavior != null;
  }

  /**
   * Expands or collapses the associated bottom sheet according to the current state and the
   * previous state when the drag handle is interactable, .
   *
   * <p>If the current state is COLLAPSED or EXPANDED and the bottom sheet can be half-expanded, it
   * will make the bottom sheet HALF_EXPANDED; if the bottom sheet cannot be half-expanded, it will
   * be EXPANDED (when it's COLLAPSED) or COLLAPSED (when it's EXPANDED) instead. On the other hand
   * when the bottom sheet is HALF_EXPANDED, it will make the bottom sheet either COLLAPSED (when
   * the previous state was EXPANDED) or EXPANDED (when the previous state was COLLAPSED.)
   */
  private boolean expandOrCollapseBottomSheetIfPossible() {
    if (!hasAttachedBehavior()) {
      return false;
    }
    int nextState = getNextState();
    if (nextState != -1) {
      bottomSheetBehavior.setState(nextState);
    }
    return true;
  }

  private int getNextState() {
    int nextState = -1;
    if (!hasAttachedBehavior()) {
      return nextState;
    }
    boolean canHalfExpand =
        !bottomSheetBehavior.isFitToContents()
            && !bottomSheetBehavior.shouldSkipHalfExpandedStateWhenDragging();
    int currentState = bottomSheetBehavior.getState();
    switch (currentState) {
      case BottomSheetBehavior.STATE_COLLAPSED:
        nextState =
            canHalfExpand
                ? BottomSheetBehavior.STATE_HALF_EXPANDED
                : BottomSheetBehavior.STATE_EXPANDED;
        break;
      case BottomSheetBehavior.STATE_EXPANDED:
        nextState =
            canHalfExpand
                ? BottomSheetBehavior.STATE_HALF_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED;
        break;
      case BottomSheetBehavior.STATE_HALF_EXPANDED:
        nextState =
            clickToExpand
                ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED;
        break;
      default: // fall out
    }
    return nextState;
  }

  /**
   * Finds the first ancestor associated with a {@link BottomSheetBehavior}. If none is found,
   * returns {@code null}.
   */
  @Nullable
  private BottomSheetBehavior<?> findParentBottomSheetBehavior() {
    View parent = this;
    while ((parent = getParentView(parent)) != null) {
      LayoutParams layoutParams = parent.getLayoutParams();
      if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
        CoordinatorLayout.Behavior<?> behavior =
            ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
        if (behavior instanceof BottomSheetBehavior) {
          return (BottomSheetBehavior<?>) behavior;
        }
      }
    }
    return null;
  }

  @Nullable
  private static View getParentView(View view) {
    ViewParent parent = view.getParent();
    return parent instanceof View ? (View) parent : null;
  }

  @Override
  public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    if (!isEnabled()) {
      return super.onKeyDown(keyCode, event);
    }

    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        if (hasClickListener) {
          return performClick();
        }
        return expandOrCollapseBottomSheetIfPossible();
      default:
        // Nothing to do in this case.
    }

    return super.onKeyDown(keyCode, event);
  }
}
