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

package io.material.catalog.tabs;

import io.material.catalog.R;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.LabelVisibility;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.viewpager.widget.ViewPager;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** The main fragment that displays tabs demos for the Catalog app. */
public class TabsControllableDemoFragment extends DemoFragment {

  private static final int TAB_COUNT = 3;
  @DrawableRes private static final int ICON_DRAWABLE_RES = R.drawable.ic_tabs_24px;
  @StringRes private static final int LABEL_STRING_RES = R.string.cat_tab_item_label;

  private boolean showIcons = true;
  private boolean showLabels = true;
  private List<TabLayout> tabLayouts;
  private ViewPager pager;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_tabs_controllable_fragment, viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    View tabsContent = layoutInflater.inflate(getTabsContent(), content, false /* attachToRoot */);
    content.addView(tabsContent, 0);

    tabLayouts = DemoUtils.findViewsWithType(view, TabLayout.class);
    pager = view.findViewById(R.id.viewpager);

    setupViewPager();
    setAllTabLayoutIcons(ICON_DRAWABLE_RES);
    setAllTabLayoutText(LABEL_STRING_RES);

    SwitchCompat iconsToggle = view.findViewById(R.id.toggle_icons_switch);
    iconsToggle.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showIcons = isChecked;
            setAllTabLayoutIcons(ICON_DRAWABLE_RES);
          }
        });

    SwitchCompat labelsToggle = view.findViewById(R.id.toggle_labels_switch);
    labelsToggle.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showLabels = isChecked;
            if (isChecked) {
              for (TabLayout tabLayout : tabLayouts) {
                setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_LABELED);
              }
            } else {
              for (TabLayout tabLayout : tabLayouts) {
                setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
              }
            }
          }
        });

    RadioButton tabGravityFillButton = view.findViewById(R.id.tabs_gravity_fill_button);
    tabGravityFillButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            setAllTabLayoutGravity(TabLayout.GRAVITY_FILL);
          }
        });

    RadioButton tabGravityCenterButton = view.findViewById(R.id.tabs_gravity_center_button);
    tabGravityCenterButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            setAllTabLayoutGravity(TabLayout.GRAVITY_CENTER);
          }
        });

    SwitchCompat inlineToggle = view.findViewById(R.id.toggle_inline_switch);
    inlineToggle.setOnCheckedChangeListener(
        new OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setAllTabLayoutInline(isChecked);
          }
        });

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
    return R.layout.cat_tabs_controllable_content;
  }

  private void setupViewPager() {
    pager.setAdapter(new TabsPagerAdapter(getFragmentManager(), getContext(), TAB_COUNT));
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setupWithViewPager(pager);
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

  private void setAllTabLayoutText(@StringRes int stringResId) {
    for (TabLayout tabLayout : tabLayouts) {
      setTabLayoutText(tabLayout, stringResId);
    }
  }

  private void setTabLayoutText(TabLayout tabLayout, @StringRes int stringResId) {
    for (int i = 0; i < tabLayout.getTabCount(); i++) {
      // Convert tab index (zero-based) to readable tab label starting at 1.
      tabLayout.getTabAt(i).setText(getResources().getString(stringResId, i + 1));
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

  private void setAllTabLayoutInline(boolean inline) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setInlineLabel(inline);
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
}
