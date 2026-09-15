/*
 * Copyright 2019 The Android Open Source Project
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

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.material.card.MaterialCardView;
import io.material.catalog.feature.DemoFragment;
import java.util.Locale;

/**
 * A fragment showing a list of {@link MaterialCardView MaterialCardView's}.
 */
public class CardListDemoFragment extends DemoFragment {

  private static final int CARD_COUNT = 30;

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_list;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_card_list_fragment, viewGroup, /* attachToRoot= */ false);

    RecyclerView recyclerView = view.findViewById(R.id.cat_card_list_recycler_view);

    CardAdapter cardAdapter = new CardAdapter(generateCardNumbers());
    ItemTouchHelper itemTouchHelper =
        new ItemTouchHelper(new CardItemTouchHelperCallback(cardAdapter));
    // Provide an ItemTouchHelper to the Adapter; can't use constructor due to circular dependency.
    cardAdapter.setItemTouchHelper(itemTouchHelper);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(cardAdapter);
    recyclerView
        .setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(recyclerView) {
          @NonNull
          @Override
          public AccessibilityDelegateCompat getItemDelegate() {
            return new ItemDelegate(this) {

              @Override
              public void onInitializeAccessibilityNodeInfo(View host,
                  AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                int position = recyclerView.getChildLayoutPosition(host);
                if (position != 0) {
                  info.addAction(new AccessibilityActionCompat(R.id.move_card_up_action,
                      host.getResources().getString(R.string.cat_card_action_move_up)));
                }
                if (position != (CARD_COUNT - 1)) {
                  info.addAction(new AccessibilityActionCompat(
                      R.id.move_card_down_action,
                      host.getResources().getString(R.string.cat_card_action_move_down)));
                }
              }

              @Override
              public boolean performAccessibilityAction(View host, int action, Bundle args) {
                int fromPosition = recyclerView.getChildLayoutPosition(host);
                if (action == R.id.move_card_down_action) {
                  cardAdapter.swapCards(fromPosition, fromPosition + 1);
                  return true;
                } else if (action == R.id.move_card_up_action) {
                  cardAdapter.swapCards(fromPosition, fromPosition - 1);
                  return true;
                }

                return super.performAccessibilityAction(host, action, args);
              }
            };
          }
        });
    itemTouchHelper.attachToRecyclerView(recyclerView);

    return view;
  }

  private static int[] generateCardNumbers() {
    int[] cardNumbers = new int[CARD_COUNT];
    for (int i = 0; i < CARD_COUNT; i++) {
      cardNumbers[i] = i + 1;
    }
    return cardNumbers;
  }

  private static class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
      implements OnKeyboardDragListener {

    private final int[] cardNumbers;

    @Nullable private ViewHolder draggedViewHolder;

    private ItemTouchHelper itemTouchHelper;

    private CardAdapter(int[] cardNumbers) {
      this.cardNumbers = cardNumbers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.cat_card_list_item, parent, /* attachToRoot= */ false);
      return new CardViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
      ((CardViewHolder) viewHolder).bind(cardNumbers[position]);
    }

    @Override
    public int getItemCount() {
      return cardNumbers.length;
    }

    private void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
      this.itemTouchHelper = itemTouchHelper;
    }

    void swapCards(int fromPosition, int toPosition) {
      if (fromPosition < 0
          || fromPosition >= cardNumbers.length
          || toPosition < 0
          || toPosition >= cardNumbers.length) {
        return;
      }

      int fromNumber = cardNumbers[fromPosition];
      cardNumbers[fromPosition] = cardNumbers[toPosition];
      cardNumbers[toPosition] = fromNumber;
      notifyItemMoved(fromPosition, toPosition);
    }

    void cancelDrag() {
      if (draggedViewHolder != null) {
        ((MaterialCardView) draggedViewHolder.itemView).setDragged(false);
        draggedViewHolder = null;
      }
    }

    @Override
    public void onDragStarted(@NonNull ViewHolder viewHolder) {
      itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onKeyboardDragToggle(@NonNull ViewHolder viewHolder) {
      boolean isCurrentlyDragged = draggedViewHolder == viewHolder;
      cancelDrag();
      if (!isCurrentlyDragged) {
        draggedViewHolder = viewHolder;
        ((MaterialCardView) viewHolder.itemView).setDragged(true);
      }
    }

    @Override
    public boolean onKeyboardMoved(int keyCode) {
      if (draggedViewHolder == null) {
        return false;
      }

      int fromPosition = draggedViewHolder.getBindingAdapterPosition();
      if (fromPosition == RecyclerView.NO_POSITION) {
        return false;
      }

      switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
          swapCards(fromPosition, fromPosition - 1);
          return true;
        case KeyEvent.KEYCODE_DPAD_DOWN:
          swapCards(fromPosition, fromPosition + 1);
          return true;
        default:
          return false;
      }
    }

    private static class CardViewHolder extends RecyclerView.ViewHolder {

      private final TextView titleView;

      private CardViewHolder(View itemView, OnKeyboardDragListener listener) {
        super(itemView);

        MaterialCardView cardView = (MaterialCardView) itemView;
        cardView.setFocusable(true);
        cardView.setOnKeyListener(
            (v, keyCode, event) -> {
              if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
              }

              switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                  listener.onKeyboardDragToggle(this);
                  return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                  return listener.onKeyboardMoved(keyCode);
                default:
                  return false;
              }
            });

        View dragHandleView = itemView.findViewById(R.id.cat_card_list_item_drag_handle);
        dragHandleView.setOnTouchListener(
            (v, event) -> {
              switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                  listener.onDragStarted(this);
                  return true;
                case MotionEvent.ACTION_UP:
                  v.performClick();
                  break;
                default: // fall out
              }
              return false;
            });

        titleView = itemView.findViewById(R.id.cat_card_list_item_title);
      }

      private void bind(int cardNumber) {
        titleView.setText(String.format(Locale.getDefault(), "Card #%02d", cardNumber));
      }
    }
  }

  private static class CardItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private static final int DRAG_FLAGS = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    private static final int SWIPE_FLAGS = 0;

    private final CardAdapter cardAdapter;

    @Nullable private MaterialCardView dragCardView;

    private CardItemTouchHelperCallback(CardAdapter cardAdapter) {
      this.cardAdapter = cardAdapter;
    }

    @Override
    public int getMovementFlags(
        @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
      return makeMovementFlags(DRAG_FLAGS, SWIPE_FLAGS);
    }

    @Override
    public boolean onMove(
        @NonNull RecyclerView recyclerView,
        @NonNull ViewHolder viewHolder,
        @NonNull ViewHolder target) {
      int fromPosition = viewHolder.getBindingAdapterPosition();
      int toPosition = target.getBindingAdapterPosition();
      cardAdapter.swapCards(fromPosition, toPosition);
      return true;
    }

    @Override
    public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {}

    @Override
    public void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState) {
      super.onSelectedChanged(viewHolder, actionState);

      if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
        cardAdapter.cancelDrag();
        dragCardView = (MaterialCardView) viewHolder.itemView;
        dragCardView.setDragged(true);
      } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && dragCardView != null) {
        dragCardView.setDragged(false);
        dragCardView = null;
      }
    }
  }

  private interface OnKeyboardDragListener {
    void onDragStarted(@NonNull ViewHolder viewHolder);

    void onKeyboardDragToggle(@NonNull ViewHolder viewHolder);

    boolean onKeyboardMoved(int keyCode);
  }
}
