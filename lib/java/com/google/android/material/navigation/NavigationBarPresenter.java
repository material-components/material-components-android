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
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.animation.PathInterpolator;
import androidx.appcompat.view.menu.BaseMenuPresenter;
import androidx.appcompat.view.menu.MenuPopupHelper;

/**
 * <b>SESL variant</b><br><br>
 *
 * For internal use only.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationBarPresenter extends BaseMenuPresenter {//sesl
  //Sesl
  private static final int ANIM_UPDATE_DURATION = 400;
  private static final int ANIM_UPDATE_DELAY = 180;
  private static final int MSG_UPDATE_ANIMATION = 100;
  private Context mContext;
  private OverflowPopup mOverflowPopup;
  private OpenOverflowRunnable mPostedOpenRunnable;
  private boolean mSetAnim = false;
  private Handler mAnimationHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == MSG_UPDATE_ANIMATION) {
        updateMenuViewWithAnimate();
      }
    }
  };
  private final PopupPresenterCallback mPopupPresenterCallback = new PopupPresenterCallback();

  NavigationBarPresenter(Context context) {
    super(context, androidx.appcompat.R.layout.sesl_action_menu_layout, androidx.appcompat.R.layout.sesl_action_menu_item_layout);
  }
  //sesl

  private MenuBuilder menu;
  private NavigationBarMenuView menuView;
  private boolean updateSuspended = false;
  private int id;

  public void setMenuView(@NonNull NavigationBarMenuView menuView) {
    this.menuView = menuView;
  }

  @Override
  public void initForMenu(@NonNull Context context, @NonNull MenuBuilder menu) {
    this.menu = menu;
    menuView.initialize(this.menu);
    mContext = context;//sesl
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
    //Sesl
    if (mSetAnim) {
      if (cleared) {
        if (mAnimationHandler.hasMessages(MSG_UPDATE_ANIMATION)) {
          mAnimationHandler.removeMessages(MSG_UPDATE_ANIMATION);
        }
        mAnimationHandler.sendEmptyMessage(MSG_UPDATE_ANIMATION);
      } else {
        menuView.postDelayed(new Runnable() {
          @Override
          public void run() {
            menuView.updateMenuView();
          }
        }, ANIM_UPDATE_DELAY);
      }
    } else {
      if (cleared) {
        menuView.buildMenuView();
      } else {
        menuView.updateMenuView();
      }
    }
    //sesl
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
      menuView.setBadgeDrawables(badgeDrawables);//sesl
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

  //Sesl
  @Override
  public void bindItemView(MenuItemImpl item, MenuView.ItemView itemView) {
  }

  void setAnimationEnable(boolean enabled) {
    mSetAnim = enabled;
  }

  private void updateMenuViewWithAnimate() {
    if (menuView != null) {
      final PathInterpolator SINE_IN_OUT_90
          = new PathInterpolator(0.33f, 0.0f, 0.1f, 1.0f);

      ObjectAnimator anim = ObjectAnimator
          .ofFloat(menuView, "y", menuView.getHeight());
      anim.setDuration(ANIM_UPDATE_DURATION);
      anim.setInterpolator(SINE_IN_OUT_90);
      anim.start();

      anim.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          menuView.buildMenuView();

          ObjectAnimator anim = ObjectAnimator.ofFloat(menuView, "y", 0.0f);
          anim.setDuration(ANIM_UPDATE_DURATION);
          anim.setInterpolator(SINE_IN_OUT_90);
          anim.start();

          super.onAnimationEnd(animation);
        }
      });
    }
  }

  boolean showOverflowMenu(MenuBuilder menu) {
    if (isOverflowMenuShowing()) {
      return false;
    }

    if (menu != null && menuView != null) {
      if (mPostedOpenRunnable == null && !menu.getNonActionItems().isEmpty()) {
        mOverflowPopup
                = new OverflowPopup(mContext, menu, menuView.mOverflowButton, true);
        mPostedOpenRunnable = new OpenOverflowRunnable(mOverflowPopup);
        menuView.post(mPostedOpenRunnable);
        super.onSubMenuSelected(null);
        return true;
      }
    }

    return false;
  }

  boolean isOverflowMenuShowing() {
    return mOverflowPopup != null && mOverflowPopup.isShowing();
  }

  private class OverflowPopup extends MenuPopupHelper {
    private OverflowPopup(Context context, MenuBuilder builder,
                          View anchorView, boolean overflowOnly) {
      super(context, builder, anchorView, overflowOnly, androidx.appcompat.R.attr.actionOverflowBottomMenuStyle);
      setGravity(Gravity.END);
      setPresenterCallback(mPopupPresenterCallback);
      setAnchorView(anchorView);
      seslSetOverlapAnchor(false);
      seslForceShowUpper(true);
    }

    protected void onDismiss() {
      if (menu != null) {
        menu.close();
      }
      mOverflowPopup = null;
      super.onDismiss();
    }
  }

  boolean hideOverflowMenu() {
    if (mPostedOpenRunnable == null || mMenuView == null) {
      if (mOverflowPopup == null) {
        return false;
      }
      mOverflowPopup.dismiss();
      return true;
    } else {
      ((ViewGroup) mMenuView).removeCallbacks(mPostedOpenRunnable);
      mPostedOpenRunnable = null;
      return true;
    }
  }

  private class PopupPresenterCallback implements MenuPresenter.Callback {
    @Override
    public boolean onOpenSubMenu(MenuBuilder subMenu) {
      if (subMenu == null) {
        return false;
      }

      final int itemId = ((SubMenuBuilder) subMenu).getItem().getItemId();
      MenuPresenter.Callback callback = getCallback();
      return callback != null && callback.onOpenSubMenu(subMenu);
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
      if (menu instanceof SubMenuBuilder) {
        menu.getRootMenu().close(false);
      }
      MenuPresenter.Callback callback = getCallback();
      if (callback != null) {
        callback.onCloseMenu(menu, allMenusAreClosing);
      }
    }
  }

  private class OpenOverflowRunnable implements Runnable {
    private OverflowPopup mPopup;

    private OpenOverflowRunnable(OverflowPopup popup) {
      mPopup = popup;
    }

    @Override
    public void run() {
      if (menu != null) {
        menu.changeMenuMode();
      }

      if (menuView != null) {
        if (menuView.getWindowToken() != null && mPopup.tryShow(0, 0)) {
          mOverflowPopup = mPopup;
        }
      }

      mPostedOpenRunnable = null;
    }
  }
  //sesl
}
