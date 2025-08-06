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

package com.google.android.material.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.SubMenuBuilder;
import android.util.SparseArray;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.internal.ParcelableSparseArray;

/**
 * For internal use only.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationBarPresenter implements MenuPresenter {
  private NavigationBarMenuView menuView;
  private boolean updateSuspended = false;
  private int id;

  public void setMenuView(@NonNull NavigationBarMenuView menuView) {
    this.menuView = menuView;
  }

  @Override
  public void initForMenu(@NonNull Context context, @NonNull MenuBuilder menu) {
    menuView.initialize(menu);
  }

  @Override
  @Nullable
  public MenuView getMenuView(@Nullable ViewGroup root) {
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
  public void setCallback(@Nullable Callback cb) {}

  @Override
  public boolean onSubMenuSelected(@Nullable SubMenuBuilder subMenu) {
    return false;
  }

  @Override
  public void onCloseMenu(@Nullable MenuBuilder menu, boolean allMenusAreClosing) {}

  @Override
  public boolean flagActionItems() {
    return false;
  }

  @Override
  public boolean expandItemActionView(@Nullable MenuBuilder menu, @Nullable MenuItemImpl item) {
    return false;
  }

  @Override
  public boolean collapseItemActionView(@Nullable MenuBuilder menu, @Nullable MenuItemImpl item) {
    return false;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return id;
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState();
    savedState.selectedItemId = menuView.getSelectedItemId();
    savedState.badgeSavedStates =
        BadgeUtils.createParcelableBadgeStates(menuView.getBadgeDrawables());
    return savedState;
  }

  @Override
  public void onRestoreInstanceState(@NonNull Parcelable state) {
    if (state instanceof SavedState) {
      menuView.tryRestoreSelectedItemId(((SavedState) state).selectedItemId);
      SparseArray<BadgeDrawable> badgeDrawables =
          BadgeUtils.createBadgeDrawablesFromSavedStates(
              menuView.getContext(), ((SavedState) state).badgeSavedStates);
      menuView.restoreBadgeDrawables(badgeDrawables);
    }
  }

  public void setUpdateSuspended(boolean updateSuspended) {
    this.updateSuspended = updateSuspended;
  }

  static class SavedState implements Parcelable {
    int selectedItemId;
    @Nullable ParcelableSparseArray badgeSavedStates;

    SavedState() {}

    SavedState(@NonNull Parcel in) {
      selectedItemId = in.readInt();
      badgeSavedStates = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      out.writeInt(selectedItemId);
      out.writeParcelable(badgeSavedStates, /* parcelableFlags= */ 0);
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
