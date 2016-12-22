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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.util.SparseArray;
import android.view.ViewGroup;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationPresenter implements MenuPresenter {

  private static final String STATE_HIERARCHY = "android:menu:list";

  private MenuBuilder mMenu;
  private BottomNavigationMenuView mMenuView;
  private boolean mUpdateSuspended = false;
  private int mId;

  public void setBottomNavigationMenuView(BottomNavigationMenuView menuView) {
    mMenuView = menuView;
  }

  @Override
  public void initForMenu(Context context, MenuBuilder menu) {
    mMenuView.initialize(mMenu);
    mMenu = menu;
  }

  @Override
  public MenuView getMenuView(ViewGroup root) {
    return mMenuView;
  }

  @Override
  public void updateMenuView(boolean cleared) {
    if (mUpdateSuspended) return;
    if (cleared) {
      mMenuView.buildMenuView();
    } else {
      mMenuView.updateMenuView();
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

  @Override
  public int getId() {
    return mId;
  }

  public void setId(int id) {
    mId = id;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    if (Build.VERSION.SDK_INT >= 11) {
      // API 9-10 does not support ClassLoaderCreator, therefore things can crash if they're
      // loaded via different loaders. Rather than crash we just won't save state on those
      // platforms
      final Bundle state = new Bundle();
      if (mMenuView != null) {
        SparseArray<Parcelable> hierarchy = new SparseArray<>();
        mMenuView.saveHierarchyState(hierarchy);
        state.putSparseParcelableArray(STATE_HIERARCHY, hierarchy);
      }
      return state;
    }
    return null;
  }

  @Override
  public void onRestoreInstanceState(Parcelable parcelable) {
    if (parcelable instanceof Bundle) {
      Bundle state = (Bundle) parcelable;
      SparseArray<Parcelable> hierarchy = state.getSparseParcelableArray(STATE_HIERARCHY);
      if (hierarchy != null) {
        mMenuView.restoreHierarchyState(hierarchy);
      }
    }
  }

  public void setUpdateSuspended(boolean updateSuspended) {
    mUpdateSuspended = updateSuspended;
  }
}
