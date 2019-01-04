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

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main menu demo for the Catalog app. */
public class MenuMainDemoFragment extends DemoFragment {

  public static final int ICON_MARGIN = 8;

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
    button.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            showMenu(v, R.menu.popup_menu);
          }
        });
    iconMenuButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            showMenu(v, R.menu.menu_with_icons);
          }
        });

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
      for (MenuItem item : menuBuilder.getVisibleItems()) {
        int iconMarginPx =
            (int)
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN, getResources().getDisplayMetrics());
        item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
      }
    }
    popup.setOnMenuItemClickListener(
        new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem menuItem) {
            Snackbar.make(
                    getActivity().findViewById(android.R.id.content),
                    menuItem.getTitle(),
                    Snackbar.LENGTH_LONG)
                .show();
            return true;
          }
        });
    popup.show();
  }
}
