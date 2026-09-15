/*
 * Copyright 2025 The Android Open Source Project
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

package io.material.catalog.listitem;

import io.material.catalog.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemLayout;
import com.google.android.material.listitem.ListItemViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays a List demo with expandable items in a RecyclerView for the Catalog app.
 */
public class ExpandableListDemoFragment extends ListsMainDemoFragment {

  private static final String KEY_LIST_DATA = "key_list_data";
  private static final int EXPANDED_SECTION_COUNT = 3;
  private ArrayList<CustomListItemData> listData;
  private Drawable arrowDownDrawable;
  private Drawable arrowUpDrawable;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView)
            layoutInflater.inflate(R.layout.cat_lists_bright_background_fragment, viewGroup, false);
    view.setLayoutManager(new LinearLayoutManager(getContext()));
    if (bundle != null) {
      listData = bundle.getParcelableArrayList(KEY_LIST_DATA);
    } else {
      listData = new ArrayList<>();
      listData.add(
          new CustomListItemData(
              String.format(view.getContext().getString(R.string.cat_list_item_text), 1), 0, 1));

      listData.add(new CustomListItemData("Subheader 1"));

      for (int i = 0; i < 3; i++) {
        listData.add(
            new CustomListItemData(
                String.format(view.getContext().getString(R.string.cat_list_item_text), 1 + i),
                i,
                3));
      }

      listData.add(new CustomListItemData("Subheader 2"));

      for (int i = 0; i < 5; i++) {
        listData.add(
            new CustomListItemData(
                String.format(view.getContext().getString(R.string.cat_list_item_text), 1 + i),
                i,
                5));
      }
    }

    ListsAdapter adapter = new ListsAdapter(listData);
    view.setAdapter(adapter);
    view.setItemAnimator(new DefaultItemAnimator());
    view.addItemDecoration(new MarginItemDecoration(getContext()));

    arrowUpDrawable = getContext().getResources().getDrawable(R.drawable.cat_ic_arrow_up);
    arrowDownDrawable = getContext().getResources().getDrawable(R.drawable.cat_ic_arrow_down);

    return view;
  }

  /** An Adapter that shows custom list items */
  public class ListsAdapter extends Adapter<ViewHolder> {

    private static final int VIEW_TYPE_SUBHEADING = 1;
    private static final int VIEW_TYPE_LIST_ITEM = 2;
    private final List<CustomListItemData> items;

    public ListsAdapter(@NonNull List<CustomListItemData> items) {
      this.items = items;
    }

    @Nullable
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
      switch (getItemViewType(position)) {
        case VIEW_TYPE_LIST_ITEM:
          ViewGroup item =
              (ViewGroup)
                  LayoutInflater.from(parent.getContext())
                      .inflate(
                          R.layout.cat_list_item_expandable_viewholder,
                          parent,
                          /* attachToRoot= */ false);
          return new CustomItemViewHolder(item);
        case VIEW_TYPE_SUBHEADING:
          TextView subheader =
              (TextView)
                  LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.cat_list_item_subheader, parent, /* attachToRoot= */ false);
          return new SubheaderViewHolder(subheader);
        default: // fall out
      }
      return null;
    }

    @Override
    public int getItemViewType(int position) {
      CustomListItemData data = getItemAt(position);
      if (data.subheading != null) {
        return VIEW_TYPE_SUBHEADING;
      }
      return VIEW_TYPE_LIST_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
      CustomListItemData data = getItemAt(position);
      if (getItemViewType(position) == VIEW_TYPE_SUBHEADING) {
        ((SubheaderViewHolder) viewHolder).bind(data);
      } else if (getItemViewType(position) == VIEW_TYPE_LIST_ITEM) {
        ((CustomItemViewHolder) viewHolder).bind(data);
      }
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    @NonNull
    public CustomListItemData getItemAt(int i) {
      return items.get(i);
    }
  }

  static class MarginItemDecoration extends ItemDecoration {
    private final int itemMargin;

    public MarginItemDecoration(Context context) {
      itemMargin = context.getResources().getDimensionPixelSize(R.dimen.cat_list_item_margin);
    }

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int position = parent.getChildAdapterPosition(view);
      if (position != state.getItemCount() - 1) {
        outRect.bottom = itemMargin;
      }
    }
  }

  /** A ViewHolder that shows custom list items */
  public class CustomItemViewHolder extends ListItemViewHolder {

    private final TextView textView;
    private final ListItemCardView cardView;
    private final ImageView expandButton;
    private final LinearLayout expandableContent;
    private final ListItemLayout expandedListItemOne;
    private final ListItemLayout expandedListItemTwo;
    private final int expandButtonBackgroundColor;
    private final SpringForce spring;

    public CustomItemViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
      expandableContent = itemView.findViewById(R.id.cat_lists_expandable_content);
      expandButton = itemView.findViewById(R.id.cat_list_item_expand_button);
      // set the z-axis to below the cardView so it is placed under
      expandableContent.setZ(cardView.getZ() - 1);
      expandedListItemOne = itemView.findViewById(R.id.cat_list_first_expanded_item);
      expandedListItemTwo = itemView.findViewById(R.id.cat_list_second_expanded_item);
      expandButtonBackgroundColor = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainer);
      spring = new SpringForce().setStiffness(800f).setDampingRatio(0.8f);
    }

    public void bind(@NonNull CustomListItemData data) {
      super.bind(
          data.expanded ? 0 : data.indexInSection,
          data.expanded ? EXPANDED_SECTION_COUNT : data.sectionCount);
      textView.setText(data.text);
      expandButton.setImageDrawable(data.expanded ? arrowUpDrawable : arrowDownDrawable);
      expandButton.setBackgroundColor(data.expanded ? expandButtonBackgroundColor : 0);
      expandableContent.setVisibility(data.expanded ? VISIBLE : GONE);
      // Expanded list items are always the 2nd and 3rd items in its own section.
      expandedListItemOne.updateAppearance(1, EXPANDED_SECTION_COUNT);
      expandedListItemTwo.updateAppearance(2, EXPANDED_SECTION_COUNT);
      cardView.setOnClickListener(v -> toggleExpandedState(data));
      setUpAccessibility(data);
    }

    private void setUpAccessibility(@NonNull CustomListItemData data) {
      cardView.setAccessibilityDelegate(
          new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
              super.onInitializeAccessibilityNodeInfo(host, info);
              info.setClassName(ListItemCardView.class.getName());
              if (data.expanded) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE);
              } else {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
              }
            }

            @Override
            public boolean performAccessibilityAction(
                @NonNull View host, int action, @Nullable Bundle args) {
              if (action == AccessibilityNodeInfoCompat.ACTION_COLLAPSE) {
                toggleExpandedState(data);
                return true;
              } else if (action == AccessibilityNodeInfoCompat.ACTION_EXPAND) {
                toggleExpandedState(data);
                return true;
              }
              return super.performAccessibilityAction(host, action, args);
            }
          });
    }

    private void toggleExpandedState(@NonNull CustomListItemData data) {
      data.expanded = !data.expanded;
      expandButton.setImageDrawable(data.expanded ? arrowUpDrawable : arrowDownDrawable);
      expandButton.setBackgroundColor(data.expanded ? expandButtonBackgroundColor : 0);
      bind(
          data.expanded ? 0 : data.indexInSection,
          data.expanded ? EXPANDED_SECTION_COUNT : data.sectionCount);
      if (data.expanded) {
        runExpandAnimation();
      } else {
        runCollapseAnimation();
      }
    }

    private void runExpandAnimation() {
      expandableContent.setVisibility(VISIBLE);

      int cardHeight = cardView.getMeasuredHeight();

      // Set the translationY of the expanded content so it cascades in.
      expandedListItemOne.setTranslationY(-cardHeight);
      expandedListItemTwo.setTranslationY(-cardHeight * 2);

      new SpringAnimation(expandedListItemOne, SpringAnimation.TRANSLATION_Y)
          .setSpring(spring)
          .animateToFinalPosition(0f);
      SpringAnimation expandableCardTwoAnim =
          new SpringAnimation(expandedListItemTwo, SpringAnimation.TRANSLATION_Y).setSpring(spring);
      // Animate the height of the expandable content along with the cascading animation so that
      // the RecyclerView DefaultItemAnimator will move the rest of the items down. A custom
      // ItemAnimator can also be implemented to animate the other items.
      expandableCardTwoAnim.addUpdateListener(
          (animation, value, velocity) -> {
            ViewGroup.LayoutParams expandableContentParams = expandableContent.getLayoutParams();
            expandableContentParams.height =
                (int) Math.max(0, expandedListItemTwo.getY() + cardHeight);
            expandableContent.setLayoutParams(expandableContentParams);
          });
      expandableCardTwoAnim.animateToFinalPosition(0f);
    }

    private void runCollapseAnimation() {
      int cardHeight = cardView.getMeasuredHeight();
      new SpringAnimation(expandedListItemOne, SpringAnimation.TRANSLATION_Y)
          .setSpring(spring)
          .animateToFinalPosition(-cardHeight);
      SpringAnimation expandableCardTwoAnim =
          new SpringAnimation(expandedListItemTwo, SpringAnimation.TRANSLATION_Y).setSpring(spring);
      expandableCardTwoAnim.addUpdateListener(
          (animation, value, velocity) -> {
            ViewGroup.LayoutParams expandableContentParams = expandableContent.getLayoutParams();
            expandableContentParams.height =
                (int) Math.max(0, expandedListItemTwo.getY() + cardHeight);
            expandableContent.setLayoutParams(expandableContentParams);
          });
      expandableCardTwoAnim.addEndListener(
          (animation, canceled, value, velocity) -> {
            expandableContent.setVisibility(GONE);
          });
      expandableCardTwoAnim.animateToFinalPosition(-cardHeight * 2);
    }
  }

  /** A ViewHolder that shows a subheader list item */
  public static class SubheaderViewHolder extends ViewHolder {

    private final TextView text;

    public SubheaderViewHolder(@NonNull View itemView) {
      super(itemView);
      text = itemView.findViewById(R.id.cat_list_subheader_text);
    }

    public void bind(@NonNull CustomListItemData data) {
      text.setText(data.subheading);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList(KEY_LIST_DATA, listData);
  }
}
