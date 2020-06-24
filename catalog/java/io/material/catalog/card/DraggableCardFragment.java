/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.card;

import io.material.catalog.R;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.card.MaterialCardView;
import io.material.catalog.draggable.DraggableCoordinatorLayout;
import io.material.catalog.draggable.DraggableCoordinatorLayout.ViewDragListener;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment with a draggable MaterialCardView
 */
public class DraggableCardFragment extends DemoFragment {

  private MaterialCardView card;

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_draggable_card;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_card_draggable_fragment, viewGroup, false /* attachToRoot */);
    DraggableCoordinatorLayout container = (DraggableCoordinatorLayout) view;
    LayoutTransition transition = ((CoordinatorLayout) view).getLayoutTransition();
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
      transition.enableTransitionType(LayoutTransition.CHANGING);
    }

    card = view.findViewById(R.id.draggable_card);
    card.setAccessibilityDelegate(cardDelegate);
    container.addDraggableChild(card);

    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      return view;
    }

    container.setViewDragListener(
        new ViewDragListener() {
          @Override
          public void onViewCaptured(@NonNull View view, int i) {
            card.setDragged(true);
          }

          @Override
          public void onViewReleased(@NonNull View view, float v, float v1) {
            card.setDragged(false);
          }
        });

    return view;
  }

  private final AccessibilityDelegate cardDelegate = new AccessibilityDelegate() {
    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
      super.onInitializeAccessibilityNodeInfo(host, info);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        return;
      }

      CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) card
          .getLayoutParams();
      int gravity = layoutParams.gravity;
      boolean isOnLeft = (gravity & Gravity.LEFT) == Gravity.LEFT;
      boolean isOnRight = (gravity & Gravity.RIGHT) == Gravity.RIGHT;
      boolean isOnTop = (gravity & Gravity.TOP) == Gravity.TOP;
      boolean isOnBottom = (gravity & Gravity.BOTTOM) == Gravity.BOTTOM;
      boolean isOnCenter = (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL;

      if (!(isOnTop && isOnLeft)) {
        info.addAction(new AccessibilityAction(R.id.move_card_top_left_action,
            getString(R.string.cat_card_action_move_top_left)));
      }
      if (!(isOnTop && isOnRight)) {
        info.addAction(new AccessibilityAction(R.id.move_card_top_right_action,
            getString(R.string.cat_card_action_move_top_right)));
      }
      if (!(isOnBottom && isOnLeft)) {
        info.addAction(new AccessibilityAction(R.id.move_card_bottom_left_action,
            getString(R.string.cat_card_action_move_bottom_left)));
      }
      if (!(isOnBottom && isOnRight)) {
        info.addAction(new AccessibilityAction(
            R.id.move_card_bottom_right_action,
            getString(R.string.cat_card_action_move_bottom_right)));
      }
      if (!isOnCenter) {
        info.addAction(new AccessibilityAction(
            R.id.move_card_center_action,
            getString(R.string.cat_card_action_move_center)));
      }
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
      int gravity;
      if (action == R.id.move_card_top_left_action) {
        gravity = Gravity.TOP | Gravity.LEFT;
      } else if (action == R.id.move_card_top_right_action) {
        gravity = Gravity.TOP | Gravity.RIGHT;
      } else if (action == R.id.move_card_bottom_left_action) {
        gravity = Gravity.BOTTOM | Gravity.LEFT;
      } else if (action == R.id.move_card_bottom_right_action) {
        gravity = Gravity.BOTTOM | Gravity.RIGHT;
      } else if (action == R.id.move_card_center_action) {
        gravity = Gravity.CENTER;
      } else {
        return super.performAccessibilityAction(host, action, arguments);
      }

      CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) card
          .getLayoutParams();
      if (layoutParams.gravity != gravity) {
        layoutParams.gravity = gravity;
        card.requestLayout();
      }

      return true;
    }
  };
}
