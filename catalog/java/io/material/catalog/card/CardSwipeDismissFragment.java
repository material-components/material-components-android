/*
 * Copyright (C) 2019 The Android Open Source Project
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

package io.material.catalog.card;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.behavior.SwipeDismissBehavior.OnDismissListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment that shows a card that can be swiped away.
 */
public final class CardSwipeDismissFragment extends DemoFragment {

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_swipe_dismiss;
  }

  /** Inflate fragment view and setup with {@link SwipeDismissBehavior} */
  @Override
  public View onCreateDemoView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_card_swipe_fragment, viewGroup, false);
    CoordinatorLayout container = view.findViewById(R.id.card_container);
    SwipeDismissBehavior<View> swipeDismissBehavior = new SwipeDismissBehavior<>();
    swipeDismissBehavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);

    MaterialCardView cardContentLayout = view.findViewById(R.id.card_content_layout);
    CoordinatorLayout.LayoutParams coordinatorParams =
        (CoordinatorLayout.LayoutParams) cardContentLayout.getLayoutParams();

    coordinatorParams.setBehavior(swipeDismissBehavior);

    OnDismissListener dismissListener = createDismissListener(container, cardContentLayout);
    swipeDismissBehavior.setListener(dismissListener);

    setDismissOnKeyListener(cardContentLayout, dismissListener);
    setDismissAccessibilityDelegate(cardContentLayout, dismissListener);

    return view;
  }

  private OnDismissListener createDismissListener(
      CoordinatorLayout container, MaterialCardView cardContentLayout) {
    return new OnDismissListener() {
      @Override
      public void onDismiss(View view) {
        Snackbar.make(container, R.string.cat_card_dismissed, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.cat_card_undo, v -> resetCard(cardContentLayout))
            .show();
      }

      @Override
      public void onDragStateChanged(int state) {
        CardSwipeDismissFragment.onDragStateChanged(state, cardContentLayout);
      }
    };
  }

  private void setDismissOnKeyListener(
      MaterialCardView cardContentLayout, OnDismissListener dismissListener) {
    cardContentLayout.setOnKeyListener(
        (view, keyCode, event) -> {
          if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
            dismissCard(view, dismissListener);
            return true;
          }
          return false;
        });
  }

  private void setDismissAccessibilityDelegate(
      MaterialCardView cardContentLayout, OnDismissListener dismissListener) {
    ViewCompat.setAccessibilityDelegate(
        cardContentLayout,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(
                new AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_DISMISS,
                    host.getResources().getString(R.string.cat_card_dismiss_action)));
          }

          @Override
          public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS) {
              dismissCard(host, dismissListener);
              return true;
            }
            return super.performAccessibilityAction(host, action, args);
          }
        });
  }

  private void dismissCard(View card, OnDismissListener dismissListener) {
    if (card.getVisibility() == View.VISIBLE) {
      dismissListener.onDismiss(card);
      card.setVisibility(View.GONE);
    }
  }

  private static void onDragStateChanged(int state, MaterialCardView cardContentLayout) {
    switch (state) {
      case SwipeDismissBehavior.STATE_DRAGGING:
      case SwipeDismissBehavior.STATE_SETTLING:
        cardContentLayout.setDragged(true);
        break;
      case SwipeDismissBehavior.STATE_IDLE:
        cardContentLayout.setDragged(false);
        break;
      default: // fall out
    }
  }

  private static void resetCard(MaterialCardView cardContentLayout) {
    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) cardContentLayout
        .getLayoutParams();
    params.setMargins(0, 0, 0, 0);
    cardContentLayout.setAlpha(1.0f);
    cardContentLayout.setVisibility(View.VISIBLE);
    cardContentLayout.requestLayout();
  }
}
