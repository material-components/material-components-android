/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.feature;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import io.material.catalog.themeswitcher.ThemeSwitcherHelper;
import io.material.catalog.themeswitcher.ThemeSwitcherHelper.ThemeSwitcherFragment;
import javax.inject.Inject;

/** Base Fragment class that provides a demo screen structure for a single demo. */
public abstract class DemoFragment extends Fragment
    implements ThemeSwitcherFragment, HasAndroidInjector {

  public static final String ARG_DEMO_TITLE = "demo_title";

  private static final int MEMORY_POLLING_INTERVAL_MS = 1500;
  private static final float SWIPE_MIN_DISTANCE_PX = 50;
  private static final float SWIPE_MIN_VELOCITY = 50;

  private Toolbar toolbar;
  private ViewGroup demoContainer;

  @Inject DispatchingAndroidInjector<Object> childFragmentInjector;

  @Nullable private ThemeSwitcherHelper themeSwitcherHelper;
  @Nullable private GestureDetector gestureDetector;
  @Nullable private MemoryView memoryWidget;
  @Nullable private ViewScheduler viewScheduler;

  @Override
  public void onAttach(Context context) {
    safeInject();
    super.onAttach(context);

    themeSwitcherHelper = new ThemeSwitcherHelper(this);
  }

  @StringRes
  public int getDemoTitleResId() {
    return 0;
  }

  @Nullable
  @Override
  @SuppressLint("ClickableViewAccessibility") // Keep this hidden from a11y services for now.
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_demo_fragment, viewGroup, false /* attachToRoot */);

    Bundle arguments = getArguments();
    if (arguments != null) {
      String transitionName = arguments.getString(FeatureDemoUtils.ARG_TRANSITION_NAME);
      ViewCompat.setTransitionName(view, transitionName);
    }

    toolbar = view.findViewById(R.id.toolbar);
    // show a memory widget on Kitkat
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      gestureDetector = new GestureDetector(getContext(), new GestureListener());
      memoryWidget = view.findViewById(R.id.memorymonitor_widget);
      toolbar.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
      viewScheduler = new ViewScheduler();
    }

    demoContainer = view.findViewById(R.id.cat_demo_fragment_container);
    initDemoActionBar();
    demoContainer.addView(onCreateDemoView(layoutInflater, viewGroup, bundle));

    ViewGroup children = (ViewGroup) demoContainer.getChildAt(0);
    DemoUtils.addBottomSpaceInsetsIfNeeded(children, demoContainer);
    return view;
  }

  @Override
  public void onStop() {
    super.onStop();
    if (viewScheduler != null) {
      viewScheduler.cancel();
    }
  }

  /**
   * Whether this fragment wants to use the default demo action bar, or if the fragment wants to use
   * its own Toolbar as the action bar.
   */
  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return true;
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return childFragmentInjector;
  }

  private void safeInject() {
    try {
      AndroidSupportInjection.inject(this);
    } catch (Exception e) {
      // Ignore exception, not all DemoFragment subclasses need to inject
    }
  }

  private void initDemoActionBar() {
    if (shouldShowDefaultDemoActionBar()) {
      AppCompatActivity activity = (AppCompatActivity) getActivity();
      activity.setSupportActionBar(toolbar);
      setDemoActionBarTitle(activity.getSupportActionBar());
    } else {
      toolbar.setVisibility(View.GONE);
    }
  }

  private void setDemoActionBarTitle(ActionBar actionBar) {
    if (getDemoTitleResId() != 0) {
      actionBar.setTitle(getDemoTitleResId());
    } else {
      actionBar.setTitle(getDefaultDemoTitle());
    }
  }

  protected String getDefaultDemoTitle() {
    Bundle args = getArguments();
    if (args != null) {
      return args.getString(ARG_DEMO_TITLE, "");
    } else {
      return "";
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    super.onCreateOptionsMenu(menu, menuInflater);
    themeSwitcherHelper.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    return themeSwitcherHelper.onOptionsItemSelected(menuItem)
        || super.onOptionsItemSelected(menuItem);
  }

  public abstract View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle);

  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private final FragmentActivity activity = getActivity();

    private final Runnable listener =
        () ->
            activity.runOnUiThread(
                () -> {
                  memoryWidget.refreshMemStats(Runtime.getRuntime());
                });

    private boolean memoryWidgetShown;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      if (e2.getY() - e1.getY() < SWIPE_MIN_DISTANCE_PX
          || Math.abs(velocityY) < SWIPE_MIN_VELOCITY
          || memoryWidgetShown) {
        return false;
      }

      memoryWidgetShown = true;
      viewScheduler.start(listener, MEMORY_POLLING_INTERVAL_MS);
      memoryWidget.setVisibility(View.VISIBLE);

      return true;
    }
  }
}
