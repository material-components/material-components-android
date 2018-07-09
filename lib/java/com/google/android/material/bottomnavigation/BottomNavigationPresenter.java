/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.bottomnavigation;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.view.ViewGroup;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationPresenter implements MenuPresenter {
  private MenuBuilder menu;
  private BottomNavigationMenuView menuView;
  private boolean updateSuspended = false;
  private int id;

  public void setBottomNavigationMenuView(BottomNavigationMenuView menuView) {
    this.menuView = menuView;
  }

  @Override
  public void initForMenu(Context context, MenuBuilder menu) {
    this.menu = menu;
    menuView.initialize(this.menu);
  }

  @Override
  public MenuView getMenuView(ViewGroup root) {
    return menuView;
  }

  @Override
  public void updateMenuView(boolean cleared) {
    if (updateSuspended) {
      return;
    }
    if (cleared) {
      menuView.buildMenuView();
    } else {
      menuView.updateMenuView();
    }
  }

  @Override
  public void setCallback(Callback cb) {}

  @Override
  public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
    return false;
  }

  @Override
  public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {}

  @Override
  public boolean flagActionItems() {
    return false;
  }

  @Override
  public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  @Override
  public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState();
    savedState.selectedItemId = menuView.getSelectedItemId();
    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      menuView.tryRestoreSelectedItemId(((SavedState) state).selectedItemId);
    }
  }

  public void setUpdateSuspended(boolean updateSuspended) {
    this.updateSuspended = updateSuspended;
  }

  static class SavedState implements Parcelable {
    int selectedItemId;

    SavedState() {}

    SavedState(Parcel in) {
      selectedItemId = in.readInt();
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      out.writeInt(selectedItemId);
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
