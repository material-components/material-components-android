/*
 * Copyright (C) 2018 The Android Open Source Project
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

package io.material.catalog.menu;

import io.material.catalog.R;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.PopupMenu;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main menu demo for the Catalog app. */
public class MenuMainDemoFragment extends DemoFragment {

  private static final int ICON_MARGIN = 8;
  private static final String CLIP_DATA_LABEL = "Sample text to copy";
  private static final String KEY_POPUP_ITEM_LAYOUT = "popup_item_layout";
  @LayoutRes private int popupItemLayout;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      popupItemLayout = savedInstanceState.getInt(KEY_POPUP_ITEM_LAYOUT);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(KEY_POPUP_ITEM_LAYOUT, popupItemLayout);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.popup_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_menu_fragment, viewGroup, false);
    Button button = view.findViewById(R.id.menu_button);
    Button iconMenuButton = view.findViewById(R.id.menu_button_with_icons);
    button.setOnClickListener(v -> showMenu(v, R.menu.popup_menu));
    iconMenuButton.setOnClickListener(v -> showMenu(v, R.menu.menu_with_icons));

    TextView contextMenuTextView = view.findViewById(R.id.context_menu_tv);
    registerForContextMenu(contextMenuTextView);

    Button listPopupWindowButton = view.findViewById(R.id.list_popup_window);
    ListPopupWindow listPopupWindow = initializeListPopupMenu(listPopupWindowButton);
    listPopupWindowButton.setOnClickListener(v -> listPopupWindow.show());

    return view;
  }

  @SuppressWarnings("RestrictTo")
  private void showMenu(View v, @MenuRes int menuRes) {
    PopupMenu popup = new PopupMenu(getContext(), v);
    // Inflating the Popup using xml file
    popup.getMenuInflater().inflate(menuRes, popup.getMenu());
    // There is no public API to make icons show on menus.
    // IF you need the icons to show this works however it's discouraged to rely on library only
    // APIs since they might disappear in future versions.
    if (popup.getMenu() instanceof MenuBuilder) {
      MenuBuilder menuBuilder = (MenuBuilder) popup.getMenu();
      //noinspection RestrictedApi
      menuBuilder.setOptionalIconsVisible(true);
      //noinspection RestrictedApi
      for (MenuItem item : menuBuilder.getVisibleItems()) {
        int iconMarginPx =
            (int)
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN, getResources().getDisplayMetrics());

        if (item.getIcon() != null) {
          if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
            item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
          } else {
            item.setIcon(
                new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0) {
                  @Override
                  public int getIntrinsicWidth() {
                    return getIntrinsicHeight() + iconMarginPx + iconMarginPx;
                  }
                });
          }
        }
      }
    }
    popup.setOnMenuItemClickListener(
        menuItem -> {
          Snackbar.make(
                  getActivity().findViewById(android.R.id.content),
                  menuItem.getTitle(),
                  Snackbar.LENGTH_LONG)
              .show();
          return true;
        });
    popup.show();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    TextView contextMenuTextView = (TextView) v;
    Context context = getContext();
    menu.add(android.R.string.copy)
        .setOnMenuItemClickListener(
            item -> {
              ClipboardManager clipboard =
                  (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
              clipboard.setPrimaryClip(
                  ClipData.newPlainText(CLIP_DATA_LABEL, contextMenuTextView.getText()));
              return true;
            });

    menu.add(R.string.context_menu_highlight)
        .setOnMenuItemClickListener(
            item -> {
              highlightText(contextMenuTextView);
              return true;
            });
  }

  public void setPopupItemLayoutRes(@LayoutRes int popupItemRes) {
    popupItemLayout = popupItemRes;
  }

  private ListPopupWindow initializeListPopupMenu(View v) {
    ListPopupWindow listPopupWindow =
        new ListPopupWindow(getContext(), null, R.attr.listPopupWindowStyle);
    ArrayAdapter<CharSequence> adapter =
        new ArrayAdapter<>(
            getContext(),
            popupItemLayout,
            getResources().getStringArray(R.array.cat_list_popup_window_content));
    listPopupWindow.setAdapter(adapter);
    listPopupWindow.setAnchorView(v);
    listPopupWindow.setOnItemClickListener(
        (parent, view, position, id) -> {
          Snackbar.make(
                  getActivity().findViewById(android.R.id.content),
                  adapter.getItem(position).toString(),
                  Snackbar.LENGTH_LONG)
              .show();
          listPopupWindow.dismiss();
        });
    return listPopupWindow;
  }

  private void highlightText(TextView textView) {
    Context context = textView.getContext();
    CharSequence text = textView.getText();
    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
    Spannable spanText = Spannable.Factory.getInstance().newSpannable(text);
    spanText.setSpan(
        new BackgroundColorSpan(value.data), 0, text.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    textView.setText(spanText);
  }
}
