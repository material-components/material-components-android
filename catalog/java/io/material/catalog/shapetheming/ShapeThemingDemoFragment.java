/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.shapetheming;

import io.material.catalog.R;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import io.material.catalog.feature.DemoFragment;

/** A base class for Shape Theming demos in the Catalog app. */
public abstract class ShapeThemingDemoFragment extends DemoFragment {

  private int statusBarColor;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    ContextThemeWrapper wrappedContext = new ContextThemeWrapper(getContext(), getShapeTheme());
    LayoutInflater layoutInflaterWithThemedContext =
        layoutInflater.cloneInContext(wrappedContext);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getActivity().getWindow();
      statusBarColor = window.getStatusBarColor();
      final TypedValue value = new TypedValue();
      wrappedContext
          .getTheme()
          .resolveAttribute(R.attr.colorPrimaryDark, value, true);
      window.setStatusBarColor(value.data);
    }

    return super.onCreateView(layoutInflaterWithThemedContext, viewGroup, bundle);
  }

  @Override
  public void onDestroyView() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getActivity().getWindow();
      window.setStatusBarColor(statusBarColor);
    }
    super.onDestroyView();
  }

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_shape_theming_container, viewGroup, false /* attachToRoot */);
    ViewGroup container = view.findViewById(R.id.container);
    layoutInflater.inflate(R.layout.cat_shape_theming_content, container, true  /* attachToRoot */);
    return view;
  }

  @StyleRes
  protected abstract int getShapeTheme();
}
