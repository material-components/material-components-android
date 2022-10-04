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

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
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
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;

/**
 * A drag handle view that can be added to bottom sheets associated with {@link
 * BottomSheetBehavior}. This view will automatically handle the accessibility interaction when the
 * accessibility service is enabled. When you add a drag handle to a bottom sheet and the user
 * enables the accessibility service, the drag handle will become important for accessibility and
 * clickable. Clicking the drag handle will toggle the bottom sheet between its collapsed and
 * expanded states.
 */
public class BottomSheetDragHandleView extends AppCompatImageView
    implements AccessibilityStateChangeListener {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_BottomSheet_DragHandle;

  @Nullable private final AccessibilityManager accessibilityManager;

  @Nullable private BottomSheetBehavior<?> bottomSheetBehavior;

  private boolean accessibilityServiceEnabled;
  private boolean interactable;
  private boolean clickToExpand;

  private final String clickToExpandActionLabel =
      getResources().getString(R.string.bottomsheet_action_expand);
  private final String clickToCollapseActionLabel =
      getResources().getString(R.string.bottomsheet_action_collapse);
  private final String clickFeedback =
      getResources().getString(R.string.bottomsheet_drag_handle_clicked);

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

  public BottomSheetDragHandleView(@NonNull Context context) {
    this(context, /* attrs= */ null);
  }

  public BottomSheetDragHandleView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomSheetDragHandleStyle);
  }

  public BottomSheetDragHandleView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    // Override the provided context with the wrapped one to prevent it from being used.
    context = getContext();

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    updateInteractableState();

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

  @Override
  public void onAccessibilityStateChanged(boolean enabled) {
    accessibilityServiceEnabled = enabled;
    updateInteractableState();
  }

  private void setBottomSheetBehavior(@Nullable BottomSheetBehavior<?> behavior) {
    if (bottomSheetBehavior != null) {
      bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback);
      bottomSheetBehavior.setAccessibilityDelegateView(null);
    }
    bottomSheetBehavior = behavior;
    if (bottomSheetBehavior != null) {
      bottomSheetBehavior.setAccessibilityDelegateView(this);
      onBottomSheetStateChanged(bottomSheetBehavior.getState());
      bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
    }
    updateInteractableState();
  }

  private void onBottomSheetStateChanged(@BottomSheetBehavior.State int state) {
    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
      clickToExpand = true;
    } else if (state == BottomSheetBehavior.STATE_EXPANDED) {
      clickToExpand = false;
    } // Else keep the original settings
    ViewCompat.replaceAccessibilityAction(
        this,
        AccessibilityActionCompat.ACTION_CLICK,
        clickToExpand ? clickToExpandActionLabel : clickToCollapseActionLabel,
        (v, args) -> expandOrCollapseBottomSheetIfPossible());
  }

  private void updateInteractableState() {
    interactable = accessibilityServiceEnabled && bottomSheetBehavior != null;
    ViewCompat.setImportantForAccessibility(
        this,
        bottomSheetBehavior != null
            ? ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
            : ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    setClickable(interactable);
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
    if (!interactable) {
      return false;
    }
    announceAccessibilityEvent(clickFeedback);
    boolean canHalfExpand =
        !bottomSheetBehavior.isFitToContents()
            && !bottomSheetBehavior.shouldSkipHalfExpandedStateWhenDragging();
    int currentState = bottomSheetBehavior.getState();
    int nextState;
    if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
      nextState =
          canHalfExpand
              ? BottomSheetBehavior.STATE_HALF_EXPANDED
              : BottomSheetBehavior.STATE_EXPANDED;
    } else if (currentState == BottomSheetBehavior.STATE_EXPANDED) {
      nextState =
          canHalfExpand
              ? BottomSheetBehavior.STATE_HALF_EXPANDED
              : BottomSheetBehavior.STATE_COLLAPSED;
    } else {
      nextState =
          clickToExpand ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED;
    }
    bottomSheetBehavior.setState(nextState);
    return true;
  }

  private void announceAccessibilityEvent(String announcement) {
    if (accessibilityManager == null) {
      return;
    }
    AccessibilityEvent announce =
        AccessibilityEvent.obtain(AccessibilityEventCompat.TYPE_ANNOUNCEMENT);
    announce.getText().add(announcement);
    accessibilityManager.sendAccessibilityEvent(announce);
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
}
