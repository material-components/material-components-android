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
              toggleBottomSheetIfPossible();
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
    }
    bottomSheetBehavior = behavior;
    if (bottomSheetBehavior != null) {
      onBottomSheetStateChanged(bottomSheetBehavior.getState());
      bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
    }
    updateInteractableState();
  }

  private void onBottomSheetStateChanged(@BottomSheetBehavior.State int state) {
    String label =
        state == BottomSheetBehavior.STATE_COLLAPSED
            ? clickToExpandActionLabel
            : clickToCollapseActionLabel;
    ViewCompat.replaceAccessibilityAction(
        this,
        AccessibilityActionCompat.ACTION_CLICK,
        label,
        (v, args) -> toggleBottomSheetIfPossible());
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

  private boolean toggleBottomSheetIfPossible() {
    if (!interactable) {
      return false;
    }
    announceAccessibilityEvent(clickFeedback);
    boolean collapsed = bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    bottomSheetBehavior.setState(
        collapsed ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
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
