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
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
                  swapCards(fromPosition, fromPosition + 1, cardAdapter);
                  return true;
                } else if (action == R.id.move_card_up_action) {
                  swapCards(fromPosition, fromPosition - 1, cardAdapter);
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

  private static class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int[] cardNumbers;

    private ItemTouchHelper itemTouchHelper;

    private CardAdapter(int[] cardNumbers) {
      this.cardNumbers = cardNumbers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.cat_card_list_item, parent, /* attachToRoot= */ false);
      return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
      ((CardViewHolder) viewHolder).bind(cardNumbers[position], itemTouchHelper);
    }

    @Override
    public int getItemCount() {
      return cardNumbers.length;
    }

    private void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
      this.itemTouchHelper = itemTouchHelper;
    }

  private static class CardViewHolder extends RecyclerView.ViewHolder {

      private final TextView titleView;
      private final View dragHandleView;

      private CardViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.cat_card_list_item_title);
        dragHandleView = itemView.findViewById(R.id.cat_card_list_item_drag_handle);
      }

      private void bind(int cardNumber, final ItemTouchHelper itemTouchHelper) {
        titleView.setText(String.format(Locale.getDefault(), "Card #%02d", cardNumber));
        dragHandleView.setOnTouchListener(
            (v, event) -> {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(CardViewHolder.this);
                return true;
              }
              return false;
            });
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
      int fromPosition = viewHolder.getAdapterPosition();
      int toPosition = target.getAdapterPosition();

      swapCards(fromPosition, toPosition, cardAdapter);
      return true;
    }

    @Override
    public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {}

    @Override
    public void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState) {
      super.onSelectedChanged(viewHolder, actionState);

      if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
        dragCardView = (MaterialCardView) viewHolder.itemView;
        dragCardView.setDragged(true);
      } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && dragCardView != null) {
        dragCardView.setDragged(false);
        dragCardView = null;
      }
    }
  }

  private static void swapCards(int fromPosition, int toPosition, CardAdapter cardAdapter) {
    int fromNumber = cardAdapter.cardNumbers[fromPosition];
    cardAdapter.cardNumbers[fromPosition] = cardAdapter.cardNumbers[toPosition];
    cardAdapter.cardNumbers[toPosition] = fromNumber;
    cardAdapter.notifyItemMoved(fromPosition, toPosition);
  }
}
