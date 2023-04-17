/*
 * Copyright 2023 The Android Open Source Project
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

package io.material.catalog.tabs;

import io.material.catalog.R;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.LabelVisibility;
import com.google.android.material.tabs.TabLayoutMediator;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the viewpager tabs demos for the Catalog app. */
public class TabsViewPagerDemoFragment extends DemoFragment {

  @DrawableRes private static final int ICON_DRAWABLE_RES = R.drawable.ic_tabs_24px;

  private boolean showIcons = true;
  private List<TabLayout> tabLayouts;
  private ViewPager2 pager;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_tabs_viewpager_fragment, viewGroup, /* attachToRoot= */ false);

    ViewGroup content = view.findViewById(R.id.content);
    View tabsContent = layoutInflater.inflate(getTabsContent(), content, /* attachToRoot= */ false);
    content.addView(tabsContent, 0);

    tabLayouts = DemoUtils.findViewsWithType(view, TabLayout.class);
    pager = view.findViewById(R.id.viewpager);

    CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator_layout);
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insetsCompat) -> {
          setScrollablePadding(coordinatorLayout);
          return insetsCompat;
        });

    setupViewPager();
    setAllTabLayoutIcons(ICON_DRAWABLE_RES);

    SwitchCompat iconsToggle = view.findViewById(R.id.toggle_icons_switch);
    iconsToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          showIcons = isChecked;
          setAllTabLayoutIcons(ICON_DRAWABLE_RES);
        });

    SwitchCompat labelsToggle = view.findViewById(R.id.toggle_labels_switch);
    labelsToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            for (TabLayout tabLayout : tabLayouts) {
              setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_LABELED);
            }
          } else {
            for (TabLayout tabLayout : tabLayouts) {
              setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
            }
          }
        });

    RadioButton tabGravityFillButton = view.findViewById(R.id.tabs_gravity_fill_button);
    tabGravityFillButton.setOnClickListener(v -> setAllTabLayoutGravity(TabLayout.GRAVITY_FILL));

    RadioButton tabGravityCenterButton = view.findViewById(R.id.tabs_gravity_center_button);
    tabGravityCenterButton.setOnClickListener(
        v -> setAllTabLayoutGravity(TabLayout.GRAVITY_CENTER));

    RadioButton tabAnimationModeLinearButton =
        view.findViewById(R.id.tabs_animation_mode_linear_button);
    tabAnimationModeLinearButton.setOnClickListener(
        v -> setAllTabAnimationModes(TabLayout.INDICATOR_ANIMATION_MODE_LINEAR));

    RadioButton tabsAnimationModeElasticButton =
        view.findViewById(R.id.tabs_animation_mode_elastic_button);
    tabsAnimationModeElasticButton.setOnClickListener(
        v -> setAllTabAnimationModes(TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC));

    RadioButton tabsAnimationModeFadeButton =
        view.findViewById(R.id.tabs_animation_mode_fade_button);
    tabsAnimationModeFadeButton.setOnClickListener(
        v -> setAllTabAnimationModes(TabLayout.INDICATOR_ANIMATION_MODE_FADE));

    SwitchCompat inlineToggle = view.findViewById(R.id.toggle_inline_switch);
    inlineToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> setAllTabLayoutInline(isChecked));

    SwitchCompat fullWidthToggle = view.findViewById(R.id.toggle_full_width_switch);
    fullWidthToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> setAllTabLayoutFullWidthIndicators(isChecked));

    Spinner selectedIndicatorSpinner = (Spinner) view.findViewById(R.id.selector_spinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            selectedIndicatorSpinner.getContext(),
            getSelectedIndicatorDrawableTitles(),
            android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    selectedIndicatorSpinner.setAdapter(adapter);

    selectedIndicatorSpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setAllTabLayoutSelectedIndicators(position);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            setAllTabLayoutSelectedIndicators(0);
          }
        });

    return view;
  }

  void setScrollablePadding(CoordinatorLayout coordinatorLayout) {
    View scrollable = coordinatorLayout.findViewById(R.id.cat_tabs_controllable_scrollview);
    scrollable.setPadding(
        scrollable.getPaddingLeft(),
        0,
        scrollable.getPaddingRight(),
        scrollable.getPaddingBottom());
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawableTitles() {
    return R.array.cat_tabs_selected_indicator_drawable_titles;
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawables() {
    return R.array.cat_tabs_selected_indicator_drawables;
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawableGravities() {
    return R.array.cat_tabs_selected_indicator_drawable_gravities;
  }

  @LayoutRes
  protected int getTabsContent() {
    return R.layout.cat_tabs_viewpager_content;
  }

  private void setupViewPager() {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(), getLifecycle());
    adapter.addFragment(TabItemContentFragment.newInstance(1));
    adapter.addFragment(TabItemContentFragment.newInstance(2));
    adapter.addFragment(TabItemContentFragment.newInstance(3));
    pager.setAdapter(adapter);
    for (TabLayout tabLayout : tabLayouts) {
      new TabLayoutMediator(
              tabLayout, pager, (tab, position) -> tab.setText("Tab " + (position + 1)))
          .attach();
    }
  }

  private void setAllTabLayoutIcons(@DrawableRes int iconResId) {
    for (TabLayout tabLayout : tabLayouts) {
      setTabLayoutIcons(tabLayout, iconResId);
    }
  }

  private void setTabLayoutIcons(TabLayout tabLayout, @DrawableRes int iconResId) {
    for (int i = 0; i < tabLayout.getTabCount(); i++) {
      if (showIcons) {
        tabLayout.getTabAt(i).setIcon(iconResId);
      } else {
        tabLayout.getTabAt(i).setIcon(null);
      }
    }
  }

  private void setLabelVisibility(TabLayout tabLayout, @LabelVisibility int mode) {
     for (int i = 0; i < tabLayout.getTabCount(); i++) {
      tabLayout.getTabAt(i).setTabLabelVisibility(mode);
    }
  }

  private void setAllTabLayoutGravity(int gravity) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setTabGravity(gravity);
    }
  }

  private void setAllTabAnimationModes(int mode) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setTabIndicatorAnimationMode(mode);
    }
  }

  private void setAllTabLayoutInline(boolean inline) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setInlineLabel(inline);
    }
  }

  private void setAllTabLayoutFullWidthIndicators(boolean fullWidth) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setTabIndicatorFullWidth(fullWidth);
    }
  }

  private void setAllTabLayoutSelectedIndicators(int position) {
    TypedArray drawables = getResources().obtainTypedArray(getSelectedIndicatorDrawables());
    @DrawableRes int drawableResId = drawables.getResourceId(position, 0);
    drawables.recycle();

    TypedArray drawableGravities =
        getResources().obtainTypedArray(getSelectedIndicatorDrawableGravities());
    int drawableGravity = drawableGravities.getInt(position, 0);
    drawableGravities.recycle();

    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setSelectedTabIndicator(drawableResId);
      tabLayout.setSelectedTabIndicatorGravity(drawableGravity);
    }
  }

  static class ViewPagerAdapter extends FragmentStateAdapter {
    private final ArrayList<Fragment> arrayList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
      super(fragmentManager, lifecycle);
    }

    public void addFragment(Fragment fragment) {
      arrayList.add(fragment);
    }

    @Override
    public int getItemCount() {
      return arrayList.size();
    }

    @Override
    public Fragment createFragment(int position) {
      return arrayList.get(position);
    }
  }
}
