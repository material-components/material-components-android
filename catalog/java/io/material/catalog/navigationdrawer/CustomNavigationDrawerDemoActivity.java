/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.navigationdrawer;

import io.material.catalog.R;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.activity.BackEventCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.LayoutParams;
import com.google.android.material.motion.MaterialSideContainerBackHelper;
import com.google.android.material.navigation.DrawerLayoutUtils;
import io.material.catalog.feature.DemoActivity;

/** A fragment that displays a custom Navigation Drawer demo for the Catalog app. */
@SuppressWarnings("RestrictTo")
public class CustomNavigationDrawerDemoActivity extends DemoActivity {

  private final OnBackPressedCallback drawerOnBackPressedCallback =
      new OnBackPressedCallback(/* enabled= */ true) {
        @Override
        public void handleOnBackPressed() {
          drawerLayout.closeDrawers();
        }
      };

  @Nullable private OnBackAnimationCallback drawerOnBackAnimationCallback;

  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle toggle;
  private View currentDrawerView;
  private MaterialSideContainerBackHelper sideContainerBackHelper;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_navigationdrawer_custom, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    drawerLayout = view.findViewById(R.id.drawer);
    toggle =
        new ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.cat_navigationdrawer_button_show_content_description,
            R.string.cat_navigationdrawer_button_hide_content_description) {
          @Override
          public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            registerBackCallback(drawerView);
          }

          @Override
          public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            unregisterBackCallback();
          }
        };
    drawerLayout.addDrawerListener(toggle);

    View endDrawer = view.findViewById(R.id.custom_drawer_end);
    view.findViewById(R.id.show_end_drawer_gravity)
        .setOnClickListener(v -> drawerLayout.openDrawer(endDrawer));

    drawerLayout.post(
        () -> {
          View startDrawer = view.findViewById(R.id.custom_drawer_start);
          if (drawerLayout.isDrawerOpen(startDrawer)) {
            registerBackCallback(startDrawer);
          } else if (drawerLayout.isDrawerOpen(endDrawer)) {
            registerBackCallback(endDrawer);
          }
        });

    return view;
  }

  private void registerBackCallback(@NonNull View drawerView) {
    currentDrawerView = drawerView;
    sideContainerBackHelper = new MaterialSideContainerBackHelper(drawerView);

    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      if (drawerOnBackAnimationCallback == null) {
        drawerOnBackAnimationCallback = createOnBackAnimationCallback();
      }
      drawerLayout.post(
          () ->
              getOnBackInvokedDispatcher()
                  .registerOnBackInvokedCallback(
                      OnBackInvokedDispatcher.PRIORITY_OVERLAY, drawerOnBackAnimationCallback));
    } else {
      getOnBackPressedDispatcher().addCallback(this, drawerOnBackPressedCallback);
    }
  }

  private void unregisterBackCallback() {
    currentDrawerView = null;
    sideContainerBackHelper = null;

    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      if (drawerOnBackAnimationCallback != null) {
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(drawerOnBackAnimationCallback);
        drawerOnBackAnimationCallback = null;
      }
    } else {
      drawerOnBackPressedCallback.remove();
    }
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    toggle.syncState();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggle.onConfigurationChanged(newConfig);
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  private OnBackAnimationCallback createOnBackAnimationCallback() {
    return new OnBackAnimationCallback() {

      @Override
      public void onBackStarted(@NonNull BackEvent backEvent) {
        sideContainerBackHelper.startBackProgress(new BackEventCompat(backEvent));
      }

      @Override
      public void onBackProgressed(@NonNull BackEvent backEvent) {
        DrawerLayout.LayoutParams drawerLayoutParams =
            (LayoutParams) currentDrawerView.getLayoutParams();
        sideContainerBackHelper.updateBackProgress(
            new BackEventCompat(backEvent), drawerLayoutParams.gravity);
      }

      @Override
      public void onBackInvoked() {
        BackEventCompat backEvent = sideContainerBackHelper.onHandleBackInvoked();
        if (backEvent == null) {
          drawerLayout.closeDrawers();
          return;
        }

        DrawerLayout.LayoutParams drawerLayoutParams =
            (LayoutParams) currentDrawerView.getLayoutParams();
        int gravity = drawerLayoutParams.gravity;
        AnimatorListener scrimCloseAnimatorListener =
            DrawerLayoutUtils.getScrimCloseAnimatorListener(drawerLayout, currentDrawerView);
        AnimatorUpdateListener scrimCloseAnimatorUpdateListener =
            DrawerLayoutUtils.getScrimCloseAnimatorUpdateListener(drawerLayout);

        sideContainerBackHelper.finishBackProgress(
            backEvent, gravity, scrimCloseAnimatorListener, scrimCloseAnimatorUpdateListener);
      }

      @Override
      public void onBackCancelled() {
        sideContainerBackHelper.cancelBackProgress();
      }
    };
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent keyEvent) {
    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ESCAPE
        && (drawerLayout.isDrawerOpen(GravityCompat.START)
            || drawerLayout.isDrawerOpen(GravityCompat.END))) {
      drawerLayout.closeDrawers();
      return true;
    }
    return super.dispatchKeyEvent(keyEvent);
  }
}
