/*
 * Copyright 2026 The Android Open Source Project
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
package com.google.android.material.search;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.internal.ToolbarUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SearchViewAnimationHelperTest {

  private Context context;
  private SearchBar searchBar;
  private SearchViewAnimationHelper helper;

  @Before
  public void setUp() {
    context = Robolectric.buildActivity(TestActivity.class).setup().get();
    SearchView searchView = new SearchView(context);
    searchBar = new SearchBar(context);
    helper = new SearchViewAnimationHelper(context, searchView, true);
    helper.setSearchBar(searchBar);
  }

  @Test
  public void getStartAndEndSiblingViews_withIds_returnsCorrectViews() throws Exception {
    AppBarLayout root = new AppBarLayout(context);
    View sibling1 = new View(context);
    sibling1.setId(android.R.id.button1);
    View sibling2 = new View(context);
    sibling2.setId(android.R.id.button2);

    root.addView(searchBar);
    root.addView(sibling1);
    root.addView(sibling2);

    searchBar.setStartSiblingViewId(sibling1.getId());
    searchBar.setEndSiblingViewId(sibling2.getId());

    assertThat(invokeGetStartSiblingView(root)).isEqualTo(sibling1);
    assertThat(invokeGetEndSiblingView(root)).isEqualTo(sibling2);
  }

  @Test
  public void getStartAndEndSiblingViews_withToolbar_returnsCorrectViews() throws Exception {
    MaterialToolbar toolbar = new MaterialToolbar(context);
    Drawable navIcon = new ColorDrawable(Color.RED);
    toolbar.setNavigationIcon(navIcon);
    ActionMenuView actionMenuView = new ActionMenuView(context);

    toolbar.addView(searchBar);
    toolbar.addView(actionMenuView);

    // Toolbar creates the navigation button internally when setNavigationIcon is called.
    ImageButton navButton = ToolbarUtils.getNavigationIconButton(toolbar);

    assertThat(invokeGetStartSiblingView(new AppBarLayout(context))).isEqualTo(navButton);
    assertThat(invokeGetEndSiblingView(new AppBarLayout(context))).isEqualTo(actionMenuView);
  }

  private View invokeGetStartSiblingView(AppBarLayout appBarLayout) throws Exception {
    java.lang.reflect.Field field =
        SearchViewAnimationHelper.class.getDeclaredField("animationDelegate");
    field.setAccessible(true);
    Object delegate = field.get(helper);
    java.lang.reflect.Method method =
        delegate.getClass().getDeclaredMethod("getStartSiblingView", AppBarLayout.class);
    method.setAccessible(true);
    return (View) method.invoke(delegate, appBarLayout);
  }

  private View invokeGetEndSiblingView(AppBarLayout appBarLayout) throws Exception {
    java.lang.reflect.Field field =
        SearchViewAnimationHelper.class.getDeclaredField("animationDelegate");
    field.setAccessible(true);
    Object delegate = field.get(helper);
    java.lang.reflect.Method method =
        delegate.getClass().getDeclaredMethod("getEndSiblingView", AppBarLayout.class);
    method.setAccessible(true);
    return (View) method.invoke(delegate, appBarLayout);
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_Material3_Light);
    }
  }
}
