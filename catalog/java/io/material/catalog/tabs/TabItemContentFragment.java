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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;

/** Fragment to display layout for each tab item in tabs demo for the Catalog app. */
public class TabItemContentFragment extends Fragment {
  private static final String TAB_NUMBER = "tab_number";

  public TabItemContentFragment() {}

  public static TabItemContentFragment newInstance(int tabNumber) {
    TabItemContentFragment fragment = new TabItemContentFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(TAB_NUMBER, tabNumber);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        inflater.inflate(R.layout.tab_item_content_fragment, viewGroup, false /* attachToRoot */);
    TextView textView = (TextView) view.findViewById(R.id.tab_number_textview);
    int tabNumber = getArguments().getInt(TAB_NUMBER, -1);
    textView.setText(
        String.format(getContext().getString(R.string.cat_tab_item_content), tabNumber));
    return view;
  }
}
